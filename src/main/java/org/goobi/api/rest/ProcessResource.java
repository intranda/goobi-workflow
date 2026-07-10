/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.api.rest;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.goobi.api.rest.model.RestProcessQueryResource;
import org.goobi.api.rest.model.RestProcessQueryResult;
import org.goobi.api.rest.model.RestProcessResource;
import org.goobi.beans.Batch;
import org.goobi.beans.Docket;
import org.goobi.beans.Institution;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.DocketManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.UGHException;
import ugh.fileformats.mets.MetsMods;

@Log4j2
@Path("/process")
public class ProcessResource extends AbstractProcessResource implements IRestAuthentication {

    /*
    JSON:
    curl -H 'Accept: application/json' http://localhost:8080/goobi/api/process/15
    
    XML:
    curl -H 'Accept: application/xml' http://localhost:8080/goobi/api/process/15
     */
    @GET
    @Path("/{processid}")
    @Operation(summary = "Serves a process resource", description = "Serves a process resource consisting of a process name, id, project name")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Tag(name = "process")
    public Response getProcessData(@PathParam("processid") String processid) {
        // id is empty or value is not numeric
        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).build();
        }
        int id = Integer.parseInt(processid);
        Process process = ProcessManager.getProcessById(id);
        // process does not exist
        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        Response access = checkProcessAccess(process);
        if (access != null) {
            return access;
        }
        // return process resource object
        return Response.status(200).entity(new RestProcessResource(process)).build();
    }

    /*
    JSON:
    curl -H 'Accept: application/json' -X PUT http://localhost:8080/goobi/api/process/15/startsteps
    
    XML:
    curl -H 'Accept: application/xml' -X PUT http://localhost:8080/goobi/api/process/15/startsteps
     */
    @PUT
    @Path("/{processid}/startsteps")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Start open automatic steps of a process", description = "start open automatic steps of this process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "403", description = "Forbidden - some requirements are not fulfilled.")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "406", description = "New process title contains invalid character.")
    @ApiResponse(responseCode = "409", description = "New process title already exists.")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response startOpenAutomaticStepsOfTheProcess(@PathParam("processid") String processid) {
        // id is empty or value is not numeric
        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).build();
        }
        int id = Integer.parseInt(processid);
        Process process = ProcessManager.getProcessById(id);
        // process does not exist
        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        Response access = checkProcessAccess(process);
        if (access != null) {
            return access;
        }

        startOpenAutomaticTasks(process);

        Helper.addMessageToProcessJournal(process.getId(), LogType.DEBUG, "open automatic steps are started using REST-API.");
        return Response.status(200).entity(new RestProcessResource(process)).build();
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X PUT http://localhost:8080/goobi/api/process/query -d '{"filter" : " 'process_title' 'stepopen:file upload' 'meta:mmsId:1234' "}'
    
    XML:
    curl -H 'Content-Type: application/xml' -X PUT http://localhost:8080/goobi/api/process/query -d "<query><filter>'process_title' 'stepopen:file upload' 'meta:mmsId:1234'</filter></query>"
     */
    @PUT
    @Path("/query")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Retrieve ids of processes satisfying the input condition.",
            description = "retrieve ids of processes satisfying the input condition")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "403", description = "Forbidden - some requirements are not fulfilled.")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response retrieveProcessesSatisfyingCondition(RestProcessQueryResource resource) {
        String combinedFilter = String.join(" ", resource.getConditions());
        String criteria = FilterHelper.criteriaBuilder(combinedFilter, false, null, null, null, true, false);

        List<Process> processes = ProcessManager.getProcesses("prozesse.Titel", criteria, null);

        AuthenticationToken authToken = getRequest() != null ? (AuthenticationToken) getRequest().getAttribute("authToken") : null;
        if (authToken != null) {
            List<Process> filtered = new ArrayList<>();
            for (Process p : processes) {
                try {
                    if (ProjectManager.isUserMemberOfProject(authToken.getUserId(), p.getProjekt().getId())) {
                        filtered.add(p);
                    }
                } catch (DAOException e) {
                    log.error(e);
                }
            }
            processes = filtered;
        }

        return Response.status(200).entity(new RestProcessQueryResult(processes)).build();
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X PUT http://localhost:8080/goobi/api/process/ -d '{"id":15,"title":"990743934_1885","projectName":"Archive_Project",
    "creationDate":1643983095000,"status":"020040040","numberOfImages":248,"numberOfMetadata":804,"numberOfDocstructs":67,"rulesetName":"ruleset.xml",
    "docketName":"Standard"}'
    
    XML:
    curl -H 'Content-Type: application/xml' -X PUT http://localhost:8080/goobi/api/process/ -d '<process><creationDate>2022-02-04T14:58:15+01:00
    </creationDate><docketName>Standard</docketName><id>15</id><numberOfDocstructs>67</numberOfDocstructs><numberOfImages>248</numberOfImages>
    <numberOfMetadata>804 </numberOfMetadata><projectName>Archive_Project</projectName><rulesetName>ruleset.xml</rulesetName>
    <status>020040040</status><title>990743934_1885</title></process>'
     */
    @PUT
    @Path("/")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Update an existing process", description = "Update an existing process, set new name, project, ....")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "403", description = "Forbidden - some requirements are not fulfilled.")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "406", description = "New process title contains invalid character.")
    @ApiResponse(responseCode = "409", description = "New process title already exists.")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response updateProcess(RestProcessResource resource) {
        int id = resource.getId();
        if (id == 0) {
            return Response.status(400).build();
        }
        Process process = ProcessManager.getProcessById(id);
        // process does not exist
        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        Response access = checkProcessAccess(process);
        if (access != null) {
            return access;
        }
        // change project
        if (StringUtils.isNotBlank(resource.getProjectName()) && !process.getProjekt().getTitel().equals(resource.getProjectName())) {
            Response resp = changeProject(resource, process);
            if (resp != null) {
                // error found, break up
                return resp;
            }
        }
        changeProcessParameter(resource, process);
        // change ruleset
        Response resp = changeRuleset(resource, process);
        if (resp != null) {
            // error found, break up
            return resp;
        }
        // change docket
        resp = changeDocket(resource, process);
        if (resp != null) {
            // error found, break up
            return resp;
        }
        // change batch
        resp = changeBatch(resource, process);
        if (resp != null) {
            // error found, break up
            return resp;
        }
        // update process title
        if (StringUtils.isNotBlank(resource.getTitle()) && !process.getTitel().equals(resource.getTitle())) {
            String newTitle = resource.getTitle();
            resp = validateProcessTitle(newTitle, process.getProjekt().getInstitution());
            if (resp != null) {
                // error found, break up
                return resp;
            }
            // rename folder, update metadata etc
            process.changeProcessTitle(newTitle);
        } else {
            // save process
            ProcessManager.saveProcessInformation(process);
        }

        Helper.addMessageToProcessJournal(process.getId(), LogType.DEBUG, "Process changed using REST-API.");
        return Response.status(200).entity(new RestProcessResource(process)).build();
    }

    private Response changeBatch(RestProcessResource resource, Process process) {
        if (resource.getBatchNumber() != null && resource.getBatchNumber() != 0
                && (process.getBatch() == null || !process.getBatch().getBatchId().equals(resource.getBatchNumber()))) {
            Batch batch = ProcessManager.getBatchById(resource.getBatchNumber());
            if (batch == null) {
                return Response.status(404).entity("Batch not found").build();
            }
            process.setBatch(batch);
        }
        return null;
    }

    private Response changeDocket(RestProcessResource resource, Process process) {
        if (StringUtils.isNotBlank(resource.getDocketName()) && !process.getDocket().getName().equals(resource.getDocketName())) {
            try {
                Docket docket = DocketManager.getDocketByName(resource.getRulesetName());
                if (docket == null) {
                    return Response.status(404).entity("Docket not found").build();
                }
                process.setDocket(docket);
            } catch (DAOException e) {
                log.error(e);
            }
        }
        return null;
    }

    private Response changeRuleset(RestProcessResource resource, Process process) {
        if (StringUtils.isNotBlank(resource.getRulesetName()) && !process.getRegelsatz().getTitel().equals(resource.getRulesetName())) {
            try {
                Ruleset ruleset = RulesetManager.getRulesetByName(resource.getRulesetName());
                if (ruleset == null) {
                    return Response.status(404).entity("Ruleset not found").build();
                }
                process.setRegelsatz(ruleset);
            } catch (DAOException e) {
                log.error(e);
            }
        }
        return null;
    }

    private void changeProcessParameter(RestProcessResource resource, Process process) {
        // change creation date
        if (resource.getCreationDate() != null) {
            process.setErstellungsdatum(resource.getCreationDate());
        }
        // change process status
        if (StringUtils.isNotBlank(resource.getStatus()) && StringUtils.isNumeric(resource.getStatus())) {
            process.setSortHelperStatus(resource.getStatus());
        }

        // update number of images
        if (resource.getNumberOfImages() != null) {
            process.setSortHelperImages(resource.getNumberOfImages());
        }
        // update number of metadata
        if (resource.getNumberOfMetadata() != null) {
            process.setSortHelperMetadata(resource.getNumberOfMetadata());
        }
        // update number of docstructs
        if (resource.getNumberOfDocstructs() != null) {
            process.setSortHelperDocstructs(resource.getNumberOfDocstructs());
        }
    }

    private Response changeProject(RestProcessResource resource, Process process) {
        try {
            // search for new project
            Project newProject = ProjectManager.getProjectByName(resource.getProjectName());
            if (newProject == null) {
                return Response.status(404).entity("Project not found").build();
            }
            // check if owning institution is the same as in the old project
            if (!process.getProjekt().getInstitution().getShortName().equals(newProject.getInstitution().getShortName())) {
                return Response.status(403).entity("New project is owned by a different institution").build();
            }

            // update project
            process.setProjekt(newProject);
        } catch (DAOException e) {
            log.error(e);
        }
        return null;
    }

    /*
     * Validate the new process title.
     * Check if the title matches the requirements and is unique.
     *
     */
    private Response validateProcessTitle(String newTitle, Institution institution) {
        if (StringUtils.isBlank(newTitle)) {
            return Response.status(400).entity("No title found.").build();
        }
        // check if new title fulfills requirements
        String validateRegEx = ConfigurationHelper.getInstance().getProcessTitleValidationRegex();
        if (!newTitle.matches(validateRegEx)) {
            // invalid characters, return 406 - Not Acceptable
            return Response.status(406).entity("Title contains invalid characters").build();
        }
        // check if new title can be used
        if (ProcessManager.countProcessTitle(newTitle, institution) != 0) {
            // new title is not unique, return 409 - Conflict
            return Response.status(409).entity("Title is not unique").build();
        }
        return null;
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X POST http://localhost:8080/goobi/api/process/ -d '{"title":"1234", "processTemplateName": "template", "projectName": "Archive_Project",
    "rulesetName": "Standard", "docketName": "Standard", "documentType": "Monograph", "propertiesList": [
    {"name": "propertyName1", "value": "propertyValue1"},{"name": "propertyName2", "value": "propertyValue2", "container": "optionalContainer"} ],
    "metadataList": [ {"name": "TitleDocMainShort", "value": "short title", "authorityValue": "authorityValue1"},
    {"name": "Author", "value": "Jack Sparrow"},
    {"name": "Creator", "authorityValue": "authorityValue3", "firstName": "Nicolas", "lastName": "Bourbaki"} ]}'
    
    XML:
    curl -H 'Content-Type: application/xml' -X POST http://localhost:8080/goobi/api/process/ -d '<process><title>1234</title><processTemplateName>template</processTemplateName>
    <projectName>Archive_Project</projectName><rulesetName>Standard</rulesetName><docketName>Standard</docketName>
    <documentType>Monograph</documentType><propertiesList><element><name>propertyName1</name><value>propertyValue1</value>
    </element><element><name>propertyName2</name><value>propertyValue2</value><container>optionalContainer</container></element></propertiesList>
    <metadataList><element><authorityValue>authorityValue1</authorityValue><name>TitleDocMainShort</name><value>short title</value></element>
    <element><name>Author</name><value>Jack Sparrow</value></element>
    <element><authorityValue>authorityValue3</authorityValue><firstName>Nicolas</firstName><lastName>Bourbaki</lastName><name>Creator</name></element>
    </metadataList></process>'
     */
    @POST
    @Path("/")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Create a new process", description = "Create a new process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request - required data is missing")
    @ApiResponse(responseCode = "403", description = "Forbidden - some requirements are not fulfilled.")
    @ApiResponse(responseCode = "404", description = "Data not found")
    @ApiResponse(responseCode = "406", description = "New process title contains invalid character.")
    @ApiResponse(responseCode = "409", description = "New process title already exists.")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response createProcess(RestProcessResource resource) {
        //TODO save RestProcessResource object in import/ folder

        // validate required fields - template name and process title

        //  check template name
        if (StringUtils.isBlank(resource.getProcessTemplateName())) {
            return Response.status(400).entity("Process template name cannot be empty.").build();
        }
        Process template = ProcessManager.getProcessByExactTitle(resource.getProcessTemplateName());
        // process does not exist
        if (template == null) {
            return Response.status(404).entity("Process template not found").build();
        }

        // check process title
        String processTitle = resource.getTitle();
        Response resp = validateProcessTitle(processTitle, null);
        if (resp != null) {
            // error found, break up
            return resp;
        }

        // determine target project for access check
        Project targetProject;
        if (StringUtils.isNotBlank(resource.getProjectName())) {
            try {
                targetProject = ProjectManager.getProjectByName(resource.getProjectName());
            } catch (DAOException e) {
                log.error(e);
                targetProject = template.getProjekt();
            }
        } else {
            targetProject = template.getProjekt();
        }
        AuthenticationToken authToken = getRequest() != null ? (AuthenticationToken) getRequest().getAttribute("authToken") : null;
        if (authToken != null) {
            if (targetProject == null) {
                return Response.status(403).entity("Access denied").build();
            }
            try {
                if (!ProjectManager.isUserMemberOfProject(authToken.getUserId(), targetProject.getId())) {
                    return Response.status(403).entity("Access denied").build();
                }
            } catch (DAOException e) {
                log.error(e);
                return Response.status(500).entity("Internal error").build();
            }
        }

        Process process = prepareProcess(processTitle, template);

        // optional: change project
        if (StringUtils.isNotBlank(resource.getProjectName())) {
            resp = changeProject(resource, process);
            if (resp != null) {
                // error found, break up
                return resp;
            }
        }
        // optional: change ruleset
        resp = changeRuleset(resource, process);
        if (resp != null) {
            // error found, break up
            return resp;
        }
        // optional: change docket
        if (StringUtils.isNotBlank(resource.getDocketName())) {
            resp = changeDocket(resource, process);
            if (resp != null) {
                // error found, break up
                return resp;
            }
        }
        // optional: add process to a batch
        if (resource.getBatchNumber() != null) {
            resp = changeBatch(resource, process);
            if (resp != null) {
                // error found, break up
                return resp;
            }
        }

        changeProcessParameter(resource, process);

        try {
            // save process to create ids and directories
            ProcessManager.saveProcess(process);

            // create dummy metadata file
            if (StringUtils.isNotBlank(resource.getDocumentType())) {
                createMetadataFile(resource, process);
            }

        } catch (DAOException | UGHException e) {
            log.error(e);
        }

        // add process properties if there are any
        List<Map<String, String>> propertiesList = resource.getPropertiesList();
        for (Map<String, String> property : propertiesList) {
            String key = property.get("name");
            String value = property.get("value");
            String container = property.get("container");
            saveNewProcessproperty(process, key, value, null, container);
        }

        // add metadata if there are any
        try {
            // load metadata file
            Fileformat fileformat = process.readMetadataFile();
            DocStruct logical = fileformat.getDigitalDocument().getLogicalDocStruct();
            if (logical.getType().isAnchor() && "topstruct".equals(resource.getMetadataLevel())) {
                logical = logical.getAllChildren().get(0);
            }

            Prefs prefs = process.getRegelsatz().getPreferences();

            // add metadata items one by one
            List<Map<String, String>> metadataList = resource.getMetadataList();
            for (Map<String, String> metadataMap : metadataList) {
                String metadataName = metadataMap.get("name");
                MetadataType mdType = prefs.getMetadataTypeByName(metadataName);
                if (mdType == null) {
                    Helper.addMessageToProcessJournal(process.getId(), LogType.ERROR, "Metadata type '" + metadataName + "' is unknown in ruleset.");
                    continue;
                }

                String metadataValue = metadataMap.get("value");
                String authorityValue = metadataMap.get("authorityValue");
                String firstName = metadataMap.get("firstName");
                String lastName = metadataMap.get("lastName");

                if (mdType.isIdentifier()) {
                    // find existing metadata, replace value
                    for (Metadata md : logical.getAllMetadata()) {
                        if (md.getType().getName().equals(metadataName)) {
                            md.setValue(metadataValue);
                        }
                    }
                } else {
                    try {
                        // add new field
                        addNewMetadataToDocStruct(logical, mdType, metadataValue, authorityValue, firstName, lastName);
                    } catch (MetadataTypeNotAllowedException ex) {
                        Helper.addMessageToProcessJournal(process.getId(), LogType.ERROR, "Metadata type '" + metadataName + "' is not allowed.");
                    }
                }
            }
            // save metadata file
            process.writeMetadataFile(fileformat);

        } catch (IOException | SwapException | UGHException | NullPointerException e) {
            Helper.addMessageToProcessJournal(process.getId(), LogType.ERROR, "Cannot read or save metadata.");
        }

        // start open automatic steps
        startOpenAutomaticTasks(process);

        Helper.addMessageToProcessJournal(process.getId(), LogType.DEBUG, "Process created using REST-API.");
        return getProcessData(String.valueOf(process.getId()));
    }

    private void startOpenAutomaticTasks(Process process) {
        // start any open automatic tasks for the process
        for (Step s : process.getSchritteList()) {
            if (StepStatus.OPEN.equals(s.getBearbeitungsstatusEnum()) && s.isTypAutomatisch()) {
                ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(s);
                myThread.startOrPutToQueue();
            }
        }
    }

    private void createMetadataFile(RestProcessResource resource, Process newProcess)
            throws PreferencesException, TypeNotAllowedForParentException, MetadataTypeNotAllowedException {
        Prefs prefs = newProcess.getRegelsatz().getPreferences();
        Fileformat fileformat = new MetsMods(prefs);
        DigitalDocument digDoc = new DigitalDocument();
        fileformat.setDigitalDocument(digDoc);
        DocStruct logical = digDoc.createDocStruct(prefs.getDocStrctTypeByName(resource.getDocumentType()));
        digDoc.setLogicalDocStruct(logical);
        for (MetadataType mdt : prefs.getAllMetadataTypes()) {
            if (mdt.isIdentifier()) {
                Metadata meta = new Metadata(mdt);
                meta.setValue(resource.getTitle());
                logical.addMetadata(meta);
            }
        }
        DocStruct physical = digDoc.createDocStruct(prefs.getDocStrctTypeByName("BoundBook"));
        digDoc.setPhysicalDocStruct(physical);
        try {
            java.nio.file.Path f = Paths.get(newProcess.getProcessDataDirectoryIgnoreSwapping());
            if (!StorageProvider.getInstance().isFileExists(f)) {
                StorageProvider.getInstance().createDirectories(f);
            }
            newProcess.writeMetadataFile(fileformat);
        } catch (UGHException | IOException | SwapException e) {
            log.error(e);
        }
    }

    public static Process prepareProcess(String processName, Process template) {
        BeanHelper helper = new BeanHelper();
        Process newProcess = new Process();
        newProcess.setTitel(processName);
        newProcess.setIstTemplate(false);
        newProcess.setInAuswahllisteAnzeigen(false);
        newProcess.setProjekt(template.getProjekt());
        newProcess.setRegelsatz(template.getRegelsatz());
        newProcess.setDocket(template.getDocket());
        newProcess.setExportValidator(template.getExportValidator());
        helper.SchritteKopieren(template, newProcess);
        helper.EigenschaftenKopieren(template, newProcess);

        // update task edition dates
        for (Step step : newProcess.getSchritteList()) {

            step.setBearbeitungszeitpunkt(newProcess.getErstellungsdatum());
            step.setEditTypeEnum(StepEditType.AUTOMATIC);

            if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
                step.setBearbeitungsbeginn(newProcess.getErstellungsdatum());
                Date myDate = new Date();
                step.setBearbeitungszeitpunkt(myDate);
                step.setBearbeitungsende(myDate);
            }
        }

        return newProcess;
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X DELETE http://localhost:8080/goobi/api/process/ -d '{"id":"123"}'
    
    XML:
    curl -H 'Content-Type: application/xml' -X DELETE http://localhost:8080/goobi/api/process/ -d '<process><id>1234</id></process>'
     */

    @DELETE
    @Path("/")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Delete an existing process", description = "Delete an existing process and all its content")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response deleteProcess(RestProcessResource resource) {

        // get id from request
        int id = resource.getId();
        if (id == 0) {
            return Response.status(400).build();
        }
        // check if id exists
        Process process = ProcessManager.getProcessById(id);
        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        Response access = checkProcessAccess(process);
        if (access != null) {
            return access;
        }
        // delete process
        try {
            StorageProvider.getInstance().deleteDir(Paths.get(process.getProcessDataDirectory()));
            ProcessManager.deleteProcess(process);
        } catch (IOException | SwapException e) {
            log.error(e);
            return Response.status(500).entity("Process cannot be deleted.").build();
        }

        return Response.ok().build();
    }

    @Override
    public List<AuthenticationMethodDescription> getAuthenticationMethods() {
        List<AuthenticationMethodDescription> implementedMethods = new ArrayList<>();
        // process data
        AuthenticationMethodDescription md = new AuthenticationMethodDescription("GET", "Get process data", "/process/\\d+");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("PUT", "Update an existing process", "/process");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("POST", "Create a new process", "/process");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("DELETE", "Delete an existing process", "/process");
        implementedMethods.add(md);

        return implementedMethods;
    }
}
