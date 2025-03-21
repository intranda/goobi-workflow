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
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.goobi.api.mq.QueueType;
import org.goobi.api.rest.model.RestJournalResource;
import org.goobi.api.rest.model.RestMetadataResource;
import org.goobi.api.rest.model.RestProcessQueryResource;
import org.goobi.api.rest.model.RestProcessQueryResult;
import org.goobi.api.rest.model.RestProcessResource;
import org.goobi.api.rest.model.RestPropertyResource;
import org.goobi.api.rest.model.RestStepQueryResource;
import org.goobi.api.rest.model.RestStepResource;
import org.goobi.beans.Batch;
import org.goobi.beans.Docket;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Institution;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.beans.Usergroup;
import org.goobi.production.enums.LogType;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.CloseStepHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.enums.PropertyType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.DocketManager;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UsergroupManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.log4j.Log4j2;
import ugh.dl.Corporate;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.UGHException;
import ugh.fileformats.mets.MetsMods;

@Log4j2
@Path("/process")

public class ProcessService implements IRestAuthentication {
    @Context
    HttpServletRequest request;

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
        String[] splittedConditions = resource.getConditions();

        StringBuilder builder = new StringBuilder();
        for (String condition : splittedConditions) {
            String filterCondition = FilterHelper.criteriaBuilder(condition, false, null, null, null, true, false);
            builder.append(filterCondition);
            builder.append(" AND ");
        }
        builder.append("TRUE");
        String criteria = builder.toString();

        List<Process> processes = ProcessManager.getProcesses("prozesse.Titel", criteria, null);

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
    "rulesetName": "Standard", "docketName": "Standard", "documentType": "Monograph", "propertiesList": [ {"name": "propertyName1", "value": "propertyValue1"}, {"name": "propertyName2", "value": "propertyValue2"} ],
    "metadataList": [ {"name": "TitleDocMainShort", "value": "short title", "authorityValue": "authorityValue1"}, {"name": "Author", "value": "Jack Sparrow"},
    {"name": "Creator", "authorityValue": "authorityValue3", "firstName": "Nicolas", "lastName": "Bourbaki"} ]}'
    
    XML:
    curl -H 'Content-Type: application/xml' -X POST http://localhost:8080/goobi/api/process/ -d '<process><title>1234</title><processTemplateName>template</processTemplateName>
    <projectName>Archive_Project</projectName><rulesetName>Standard</rulesetName><docketName>Standard</docketName><documentType>Monograph</documentType>
    <propertiesList><element><name>propertyName1</name><value>propertyValue1</value></element><element><name>propertyName2</name><value>propertyValue2</value></element></propertiesList>
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
            saveNewProcessproperty(process, key, value, null);
        }

        // add metadata if there are any
        try {
            // load metadata file
            Fileformat fileformat = process.readMetadataFile();
            DocStruct logical = fileformat.getDigitalDocument().getLogicalDocStruct();
            if (logical.getType().isAnchor() && "topstruct".equals(resource.getMetadataLevel())) {
                logical = logical.getAllChildren().get(process.getId());
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

                try {
                    addNewMetadataToDocStruct(logical, mdType, metadataValue, authorityValue, firstName, lastName);
                } catch (MetadataTypeNotAllowedException ex) {
                    Helper.addMessageToProcessJournal(process.getId(), LogType.ERROR, "Metadata type '" + metadataName + "' is not allowed.");
                }

            }
            // save metadata file
            process.writeMetadataFile(fileformat);

        } catch (IOException | SwapException | UGHException e) {
            Helper.addMessageToProcessJournal(process.getId(), LogType.ERROR, "Cannot read or save metadata.");
        } catch (Exception e) {
            Helper.addMessageToProcessJournal(process.getId(), LogType.ERROR, "Unknown error occurred: " + e.getMessage());
        }

        // start open automatic steps
        startOpenAutomaticTasks(process);

        Helper.addMessageToProcessJournal(process.getId(), LogType.DEBUG, "Process created using REST-API.");
        return getProcessData(String.valueOf(process.getId()));
    }

    private GoobiProperty saveNewProcessproperty(Process process, String key, String value, Date creationDate) {
        GoobiProperty property = new GoobiProperty(PropertyOwnerType.PROCESS);
        property.setPropertyName(key);
        property.setPropertyValue(value);
        property.setOwner(process);
        property.setType(PropertyType.STRING);
        if (creationDate != null) {
            property.setCreationDate(creationDate);
        } else {
            property.setCreationDate(new Date());
        }
        Helper.addMessageToProcessJournal(property.getObjectId(), LogType.DEBUG, "Property added using REST-API: " + property.getPropertyName());

        PropertyManager.saveProperty(property);

        return property;
    }

    private void addNewMetadataToDocStruct(DocStruct dst, MetadataType mdType, String metadataValue, String authorityValue, String firstName,
            String lastName) throws MetadataTypeNotAllowedException {
        if (mdType.getIsPerson()) {
            Person p = new Person(mdType);
            // if firstName and lastName are both blank, create them using metadataValue
            if (StringUtils.isBlank(firstName) && StringUtils.isBlank(lastName) && metadataValue != null) {
                String[] nameParts = metadataValue.split(" ", 2);
                firstName = nameParts[0];
                lastName = nameParts.length > 1 ? nameParts[1] : "";
            }
            p.setFirstname(firstName);
            p.setLastname(lastName);
            p.setAuthorityValue(authorityValue);
            dst.addPerson(p);
        } else if (mdType.isCorporate()) {
            Corporate c = new Corporate(mdType);
            c.setMainName(metadataValue);
            c.setAuthorityValue(authorityValue);
            dst.addCorporate(c);
        } else {
            Metadata md = new Metadata(mdType);
            md.setValue(metadataValue);
            md.setAuthorityValue(authorityValue);
            dst.addMetadata(md);
        }
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
        helper.ScanvorlagenKopieren(template, newProcess);
        helper.WerkstueckeKopieren(template, newProcess);
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

    /*
    JSON:
    curl -H 'Accept: application/json' http://localhost:8080/goobi/api/process/15/steps
    
    XML:
    curl -H 'Accept: application/xml' http://localhost:8080/goobi/api/process/15/steps
     */

    @Path("/{processid}/steps")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Serves a step list resource", description = "Get a list of all steps for a given process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response getStepList(@PathParam("processid") String processid) {
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

        List<RestStepResource> stepList = new ArrayList<>();
        for (Step step : process.getSchritte()) {
            stepList.add(new RestStepResource(process, step));
        }

        GenericEntity<List<RestStepResource>> entity = new GenericEntity<>(stepList) {
        };
        return Response.status(200).entity(entity).build();
    }

    /*
    JSON:
    curl -H 'Accept: application/json' http://localhost:8080/goobi/api/process/15/step/67
    
    XML:
    curl -H 'Accept: application/xml' http://localhost:8080/goobi/api/process/15/step/67
     */

    @Path("/{processid}/step/{stepid}")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Serves a step resource", description = "Get a specific step")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response getStep(@PathParam("processid") String processid, @PathParam("stepid") String stepid) {
        // id is empty or value is not numeric
        if (StringUtils.isBlank(stepid) || !StringUtils.isNumeric(stepid)) {
            return Response.status(400).build();
        }
        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).build();
        }
        int id = Integer.parseInt(processid);
        Process process = ProcessManager.getProcessById(id);
        // process does not exist
        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        id = Integer.parseInt(stepid);
        Step step = StepManager.getStepById(id);
        // process does not exist
        if (step == null) {
            return Response.status(404).entity("Step not found").build();
        }
        return Response.status(200).entity(new RestStepResource(process, step)).build();
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X PUT http://localhost:8080/goobi/api/process/15/step -d '{"stepId": 67, "steptitle": "new step name", "processId": 15}'
    
    XML:
    curl -H 'Content-Type: application/xml' -X PUT http://localhost:8080/goobi/api/process/15/step -d '<step><processId>15</processId><stepId>67</stepId><steptitle>new step name</steptitle></step>'
     */
    @PUT
    @Path("/{processid}/step")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Update an existing step", description = "Update an existing step")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "403", description = "Forbidden - some requirements are not fulfilled.")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "406", description = "New process title contains invalid character.")
    @ApiResponse(responseCode = "409", description = "New process title already exists.")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response updateStep(@PathParam("processid") String processid, RestStepResource resource) {
        Integer id = resource.getStepId();
        if (id == null || id.intValue() == 0) {
            return Response.status(400).build();
        }
        Step step = StepManager.getStepById(id);
        // step does not exist
        if (step == null) {
            return Response.status(404).entity("Step not found").build();
        }

        if (resource.getOrder() != null) {
            if (!StringUtils.isNumeric(resource.getOrder()) && !"end".equalsIgnoreCase(resource.getOrder())) {
                return Response.status(400).entity("Order must be numeric or the keyword 'end'").build();
            }

            Process process = ProcessManager.getProcessById(id);
            int orderNumber = 0;
            if ("end".equalsIgnoreCase(resource.getOrder())) {
                orderNumber = process.getSchritte().get(process.getSchritteSize() - 1).getReihenfolge() + 1;
            } else {
                orderNumber = Integer.parseInt(resource.getOrder());
            }
            step.setReihenfolge(orderNumber);
        }

        if (StringUtils.isNotBlank(resource.getSteptitle())) {
            step.setTitel(resource.getSteptitle());
        }
        if (StringUtils.isNotBlank(resource.getStatus())) {
            for (StepStatus status : StepStatus.values()) {
                if (status.getSearchString().equals(resource.getStatus())) {
                    step.setBearbeitungsstatusEnum(status);
                }
            }
        }
        setStepParameter(resource, step);
        setStepProperties(resource, step);
        // scripts
        setScripts(resource, step);
        // httpStepConfiguration
        setStepHttpConfiguration(resource, step);
        // usergroups
        setUserGroups(resource, step);
        try {
            StepManager.saveStep(step);
        } catch (DAOException e) {
            log.error(e);
        }
        Helper.addMessageToProcessJournal(step.getProcessId(), LogType.DEBUG, "Step changed using REST-API: " + step.getTitel());
        return getStep(String.valueOf(resource.getProcessId()), String.valueOf(resource.getStepId()));
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X POST http://localhost:8080/goobi/api/process/15/step -d '{"steptitle": "new step name", "processId": 15, "order": 10,"usergroups": ["Administration"]}'
    
    XML:
    curl -H 'Content-Type: application/xml' -X POST http://localhost:8080/goobi/api/process/15/step -d '<step><order>10</order><steptitle>new step name</steptitle><usergroups>Administration</usergroups></step>'
     */

    @POST
    @Path("/{processid}/step")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Add a new step", description = "Add a new step to an existing process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "403", description = "Forbidden - some requirements are not fulfilled.")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "406", description = "New process title contains invalid character.")
    @ApiResponse(responseCode = "409", description = "New process title already exists.")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response createStep(@PathParam("processid") String processid, RestStepResource resource) {

        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).build();
        }

        if (!StringUtils.isNumeric(resource.getOrder()) && !"end".equalsIgnoreCase(resource.getOrder())) {
            return Response.status(400).build();
        }

        int id = Integer.parseInt(processid);
        Process process = ProcessManager.getProcessById(id);
        // process does not exist
        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        // check required fields

        if (StringUtils.isBlank(resource.getSteptitle())) {
            return Response.status(400).entity("Step name is missing").build();
        }
        if (resource.getOrder() == null) {
            return Response.status(400).entity("Step order is missing").build();
        }

        if (resource.getUsergroups().isEmpty()) {
            return Response.status(400).entity("Assigned usergroups are missing").build();
        }

        Step step = new Step();
        step.setTitel(resource.getSteptitle());

        int orderNumber = 0;
        if ("end".equalsIgnoreCase(resource.getOrder())) {
            orderNumber = process.getSchritte().get(process.getSchritteSize() - 1).getReihenfolge() + 1;
        } else {
            orderNumber = Integer.parseInt(resource.getOrder());
        }

        step.setReihenfolge(orderNumber);
        step.setProzess(process);
        step.setProcessId(process.getId());
        process.getSchritte().add(step);

        if (StringUtils.isNotBlank(resource.getStatus())) {
            for (StepStatus status : StepStatus.values()) {
                if (status.getSearchString().equals(resource.getStatus())) {
                    step.setBearbeitungsstatusEnum(status);
                }
            }
        }
        setStepParameter(resource, step);
        setStepProperties(resource, step);
        // scripts
        setScripts(resource, step);
        // httpStepConfiguration
        setStepHttpConfiguration(resource, step);
        // usergroups
        setUserGroups(resource, step);
        try {
            StepManager.saveStep(step);
        } catch (DAOException e) {
            log.error(e);
        }

        Helper.addMessageToProcessJournal(step.getProcessId(), LogType.DEBUG, "Step added using REST-API: " + step.getTitel());
        return Response.status(200).entity(new RestStepResource(process, step)).build();
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X DELETE http://localhost:8080/goobi/api/process/15/step -d '{"stepId":"123"}'
    
    XML:
    curl -H 'Content-Type: application/xml' -X DELETE http://localhost:8080/goobi/api/process/15/step -d '<step><stepId>1234</stepId></step>'
     */

    @DELETE
    @Path("/{processid}/step")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Delete a step", description = "Delete a step")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "409", description = "Step belongs to a different process.")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "query")
    public Response deleteStep(@PathParam("processid") String processid, RestStepResource resource) {

        // get id from request
        Integer id = resource.getStepId();
        if (id == null || id == 0) {
            return Response.status(400).build();
        }
        // check if id exists
        Step step = StepManager.getStepById(id);
        // step does not exist
        if (step == null) {
            return Response.status(404).entity("Step not found").build();
        }
        // delete step
        StepManager.deleteStep(step);
        Helper.addMessageToProcessJournal(step.getProcessId(), LogType.DEBUG, "Step deleted using REST-API: " + step.getTitel());
        return Response.ok().build();
    }

    /*
     * JSON:
    curl -H 'Content-Type: application/json' -X PUT http://localhost:8080/goobi/api/process/10/step/close -d '{"stepname":"file upload"}'
    
     * XML:
    curl -H 'Content-Type: application/xml' -X PUT http://localhost:8080/goobi/api/process/10/step/close -d '<step><stepname>file upload</stepname></step>'
     */

    @PUT
    @Path("/{processid}/step/close")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Close the first step matching the given name", description = "Close the first step matching the given name")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response closeStepGivenName(@PathParam("processid") String processid, RestStepQueryResource resource) {

        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }

        int processId = Integer.parseInt(processid);
        // get process by id
        Process process = ProcessManager.getProcessById(processId);

        // get the list of all steps of this process
        List<Step> steps = process.getSchritteList();

        // find the first match and close it
        Step stepFound = null;
        String targetStepname = resource.getStepname();

        for (Step step : steps) {
            String title = step.getTitel();
            if (targetStepname.equals(title)) {
                stepFound = step;
                break;
            }
        }

        // step does not exist
        if (stepFound == null) {
            return Response.status(404).entity("Step not found").build();
        }

        switch (stepFound.getBearbeitungsstatusEnum()) {
            case DEACTIVATED:
            case DONE:
            case LOCKED:
                // wrong status
                return Response.status(409).entity("Step is not in work.").build();
            case ERROR:
            case INFLIGHT:
            case INWORK:
            case OPEN:
            default:
                Helper.addMessageToProcessJournal(stepFound.getProcessId(), LogType.DEBUG, "Step closed using REST-API: " + stepFound.getTitel());
                CloseStepHelper.closeStep(stepFound, null);
                return Response.ok().build();
        }

    }

    /*
    curl -H 'Content-Type: application/json' -X PUT http://localhost:8080/goobi/api/process/120/step/413/close
     */

    @PUT
    @Path("/{processid}/step/{stepid}/close")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Close a step", description = "Close a step")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response closeStep(@PathParam("processid") String processid, @PathParam("stepid") String stepid) {

        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }
        if (StringUtils.isBlank(stepid) || !StringUtils.isNumeric(stepid)) {
            return Response.status(400).entity("Step id is missing.").build();
        }
        int procId = Integer.parseInt(processid);
        int taslId = Integer.parseInt(stepid);
        Step step = StepManager.getStepById(taslId);
        // step does not exist
        if (step == null) {
            return Response.status(404).entity("Step not found").build();
        }
        if (step.getProcessId().intValue() != procId) {
            return Response.status(409).entity("Step belongs to a different process.").build();
        }

        switch (step.getBearbeitungsstatusEnum()) {
            case DEACTIVATED:
            case DONE:
            case LOCKED:
                // wrong status
                return Response.status(409).entity("Step is not in work.").build();
            case ERROR:
            case INFLIGHT:
            case INWORK:
            case OPEN:
            default:
                Helper.addMessageToProcessJournal(step.getProcessId(), LogType.DEBUG, "Step closed using REST-API: " + step.getTitel());
                CloseStepHelper.closeStep(step, null);
                return Response.ok().build();
        }

    }

    /*
    JSON:
    curl -H 'Accept: application/json' http://localhost:8080/goobi/api/process/15/journal
    
    XML:
    curl -H 'Accept: application/xml' http://localhost:8080/goobi/api/process/15/journal
     */

    @Path("/{processid}/journal")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Get the journal for a process resource", description = "Get a list of all journal entries for a given process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response getJournal(@PathParam("processid") String processid) {
        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }
        int id = Integer.parseInt(processid);
        List<JournalEntry> entries = JournalManager.getLogEntriesForProcess(id);

        List<RestJournalResource> answer = new ArrayList<>(entries.size());

        for (JournalEntry entry : entries) {
            answer.add(new RestJournalResource(entry));
        }

        GenericEntity<List<RestJournalResource>> entity = new GenericEntity<>(answer) {
        };
        return Response.status(200).entity(entity).build();

    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X PUT http://localhost:8080/goobi/api/process/15/journal -d '{"id": 70, "userName": "Doe, John", "type": "info", "message": "content"}'
    
    XML:
    curl -H 'Content-Type: application/xml' -X PUT http://localhost:8080/goobi/api/process/15/journal -d '<journal><id>70</id><userName>Doe, John</userName><type>info</type><message>content</message></journal>'
     */

    @Path("/{processid}/journal")
    @PUT
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Update a journal entry", description = "Update an existing journal entry for a given process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response updateJournalEntry(@PathParam("processid") String processid, RestJournalResource resource) {
        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }
        Integer journalId = resource.getId();
        if (journalId == null || journalId == 0) {
            return Response.status(400).entity("Journal id is missing.").build();
        }

        JournalEntry entry = JournalManager.getJournalEntryById(journalId);
        if (entry == null) {
            return Response.status(404).entity("Journal entry not found").build();
        }
        // update parameter
        if (StringUtils.isNotBlank(resource.getMessage())) {
            entry.setContent(resource.getMessage());
        }
        if (StringUtils.isNotBlank(resource.getUserName())) {
            entry.setUserName(resource.getUserName());
        }
        if (StringUtils.isNotBlank(resource.getType())) {
            entry.setType(LogType.getByTitle(resource.getType()));
        }
        if (StringUtils.isNotBlank(resource.getFilename())) {
            entry.setFilename(resource.getFilename());
        }
        JournalManager.saveJournalEntry(entry);
        return Response.status(200).entity(new RestJournalResource(entry)).build();
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X POST http://localhost:8080/goobi/api/process/15/journal -d '{"userName": "Doe, John", "type": "info", "message": "content"}'
    
    XML:
    curl -H 'Content-Type: application/xml' -X POST http://localhost:8080/goobi/api/process/15/journal -d '<journal><userName>Doe, John</userName><type>info</type><message>content</message></journal>'
     */

    @Path("/{processid}/journal")
    @POST
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Create a new journal entry", description = "Create a new journal entry for a given process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response createJournalEntry(@PathParam("processid") String processid, RestJournalResource resource) {
        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }

        Date creationDate = resource.getCreationDate();
        if (creationDate == null) {
            creationDate = new Date();
        }
        String userName = resource.getUserName();
        if (StringUtils.isBlank(userName)) {
            userName = "rest api";
        }
        LogType logType = null;
        if (StringUtils.isNotBlank(resource.getType())) {
            logType = LogType.getByTitle(resource.getType());
        } else {
            logType = LogType.DEBUG;
        }

        String content = resource.getMessage();
        String filename = resource.getFilename();

        JournalEntry entry = new JournalEntry(Integer.parseInt(processid), creationDate, userName, logType, content, EntryType.PROCESS);
        entry.setFilename(filename);
        JournalManager.saveJournalEntry(entry);
        return Response.status(200).entity(new RestJournalResource(entry)).build();
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X DELETE http://localhost:8080/goobi/api/process/15/journal -d '{"id": 70}'
    
    XML:
    curl -H 'Content-Type: application/xml' -X DELETE http://localhost:8080/goobi/api/process/15/journal -d '<journal><id>70</id></journal>'
     */

    @Path("/{processid}/journal")
    @DELETE
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Delete an existing journal entry", description = "Delete an existing journal entry")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Journal entry not found")
    @ApiResponse(responseCode = "409", description = "Journal entry belongs to a different process.")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response deleteJournalEntry(@PathParam("processid") String processid, RestJournalResource resource) {
        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }
        Integer journalId = resource.getId();
        if (journalId == null || journalId == 0) {
            return Response.status(400).entity("Journal id is missing.").build();
        }

        JournalEntry entry = JournalManager.getJournalEntryById(journalId);
        if (entry == null) {
            return Response.status(404).entity("Journal entry not found").build();
        }
        if (entry.getObjectId().intValue() != Integer.parseInt(processid)) {
            return Response.status(409).entity("Journal entry belongs to a different process.").build();
        }

        JournalManager.deleteJournalEntry(entry);
        return Response.status(200).build();
    }

    /*
    JSON:
    curl -H 'Accept: application/json' http://localhost:8080/goobi/api/process/15/properties
    
    XML:
    curl -H 'Accept: application/xml' http://localhost:8080/goobi/api/process/15/properties
     */

    @Path("/{processid}/properties")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Get all properties for a process resource", description = "Get a list of all properties for a given process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")

    public Response getProperties(@PathParam("processid") String processid) {
        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }

        List<GoobiProperty> properties = PropertyManager.getPropertiesForObject(Integer.parseInt(processid), PropertyOwnerType.PROCESS);

        List<RestPropertyResource> answer = new ArrayList<>(properties.size());

        for (GoobiProperty entry : properties) {
            answer.add(new RestPropertyResource(entry));
        }

        GenericEntity<List<RestPropertyResource>> entity = new GenericEntity<>(answer) {
        };
        return Response.status(200).entity(entity).build();
    }

    /*
    JSON:
    curl -H 'Accept: application/json' http://localhost:8080/goobi/api/process/15/property/76
    
    XML:
    curl -H 'Accept: application/xml' http://localhost:8080/goobi/api/process/15/property/76
     */

    @Path("/{processid}/property/{propertyid}")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Get the property for a process resource", description = "Get a property for a given process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")

    public Response getProperty(@PathParam("processid") String processid, @PathParam("propertyid") String propertyid) {
        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }
        if (StringUtils.isBlank(propertyid) || !StringUtils.isNumeric(propertyid)) {
            return Response.status(400).entity("Property id is missing.").build();
        }
        int propId = Integer.parseInt(propertyid);
        GoobiProperty property = PropertyManager.getPropertById(propId);
        if (property == null) {
            return Response.status(404).entity("Property not found").build();
        }

        return Response.status(200).entity(new RestPropertyResource(property)).build();
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X PUT http://localhost:8080/goobi/api/process/15/property -d '{"id":76,"name":"name","value":"value"}'
    
    XML:
    curl -H 'Content-Type: application/xml' -X PUT http://localhost:8080/goobi/api/process/15/property -d '<property><id>76</id><name>name</name><value>value</value></property>'
     */

    @Path("/{processid}/property")
    @PUT
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Update a property", description = "Update an existing property for a given process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response updateProperty(@PathParam("processid") String processid, RestPropertyResource resource) {
        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }
        if (resource.getId() == null || resource.getId().intValue() == 0) {
            return Response.status(400).entity("Property id is missing.").build();
        }
        GoobiProperty property = PropertyManager.getPropertById(resource.getId());
        if (property == null) {
            return Response.status(404).entity("Property not found").build();
        }
        if (property.getObjectId().intValue() != Integer.parseInt(processid)) {
            return Response.status(409).entity("Property belongs to a different process.").build();
        }
        if (StringUtils.isNotBlank(resource.getName())) {
            property.setPropertyName(resource.getName());
        }
        if (StringUtils.isNotBlank(resource.getValue())) {
            property.setPropertyValue(resource.getValue());
        }
        Helper.addMessageToProcessJournal(property.getObjectId(), LogType.DEBUG, "Property changed using REST-API: " + property.getPropertyName());

        PropertyManager.saveProperty(property);
        return Response.status(200).entity(new RestPropertyResource(property)).build();
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X POST http://localhost:8080/goobi/api/process/15/property -d '{"name":"name","value":"value"}'
    
    XML:
    curl -H 'Content-Type: application/xml' -X POST http://localhost:8080/goobi/api/process/15/property -d '<property><name>name</name><value>value</value></property>'
     */

    @Path("/{processid}/property")
    @POST
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Create a property", description = "Create a new property for a given process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response createProperty(@PathParam("processid") String processid, RestPropertyResource resource) {
        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }
        if (StringUtils.isBlank(resource.getName())) {
            return Response.status(400).entity("Property name is missing.").build();
        }
        if (StringUtils.isBlank(resource.getValue())) {
            return Response.status(400).entity("Property value is missing.").build();
        }

        Process process = ProcessManager.getProcessById(Integer.parseInt(processid));
        // process does not exist
        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }

        String propertyName = resource.getName();
        String propertyValue = resource.getValue();
        Date creationDate = resource.getCreationDate(); // maybe null but it doesn't matter
        GoobiProperty property = saveNewProcessproperty(process, propertyName, propertyValue, creationDate);

        return Response.status(200).entity(new RestPropertyResource(property)).build();
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X DELETE http://localhost:8080/goobi/api/process/15/property -d '{"id":"697"}'
    
    XML:
    curl -H 'Content-Type: application/xml' -X DELETE http://localhost:8080/goobi/api/process/15/property -d '<property><id>697</id></property>'
     */

    @Path("/{processid}/property")
    @DELETE
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Delete a property", description = "Delete a property from a given process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response deleteProperty(@PathParam("processid") String processid, RestPropertyResource resource) {
        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }
        if (resource.getId() == null || resource.getId().intValue() == 0) {
            return Response.status(400).entity("Property id is missing.").build();
        }
        GoobiProperty property = PropertyManager.getPropertById(resource.getId());
        if (property == null) {
            return Response.status(404).entity("Property not found").build();
        }
        if (property.getObjectId().intValue() != Integer.parseInt(processid)) {
            return Response.status(409).entity("Property belongs to a different process.").build();
        }

        PropertyManager.deleteProperty(property);

        Helper.addMessageToProcessJournal(property.getObjectId(), LogType.DEBUG, "Property deleted using REST-API: " + property.getPropertyName());
        return Response.status(200).build();
    }

    private void setStepHttpConfiguration(RestStepResource resource, Step step) {
        if (resource.getHttpStepConfiguration().size() > 0) {
            step.setHttpStep(true);
            step.setHttpUrl(resource.getHttpStepConfiguration().get("url"));
            step.setHttpMethod(resource.getHttpStepConfiguration().get("method"));
            step.setHttpJsonBody(resource.getHttpStepConfiguration().get("body"));
            step.setHttpCloseStep(Boolean.parseBoolean(resource.getHttpStepConfiguration().get("closeStep")));
            step.setHttpEscapeBodyJson(Boolean.parseBoolean(resource.getHttpStepConfiguration().get("escapeBody")));
        }
    }

    private void setUserGroups(RestStepResource resource, Step step) {
        if (!resource.getUsergroups().isEmpty()) {
            // remove all assigned groups
            step.getBenutzergruppen().clear();
            // add all configured groups
            for (String usergroupName : resource.getUsergroups()) {
                Usergroup ug = UsergroupManager.getUsergroupByName(usergroupName);
                if (ug != null) {
                    step.getBenutzergruppen().add(ug);
                }
            }
        }
    }

    private void setStepParameter(RestStepResource resource, Step step) {
        if (resource.getPriority() != null) {
            step.setPrioritaet(resource.getPriority());
        }

        if (resource.getStartDate() != null) {
            step.setBearbeitungsbeginn(resource.getStartDate());
        }

        if (resource.getFinishDate() != null) {
            step.setBearbeitungsende(resource.getFinishDate());
        }
        if (StringUtils.isNotBlank(resource.getPlugin())) {
            step.setStepPlugin(resource.getPlugin());
        }
        if (StringUtils.isNotBlank(resource.getValidationPlugin())) {
            step.setValidationPlugin(resource.getValidationPlugin());
        }
        if (StringUtils.isNotBlank(resource.getQueueType())) {
            step.setMessageQueue(QueueType.getByName(resource.getQueueType()));
        }
    }

    private void setScripts(RestStepResource resource, Step step) {
        List<Entry<String, String>> set = new ArrayList<>(resource.getScripts().entrySet());

        for (int i = 0; i < set.size(); i++) {
            Entry<String, String> entry = set.get(i);
            switch (i) {
                case 0:
                    step.setScriptname1(entry.getKey());
                    step.setTypAutomatischScriptpfad(entry.getValue());
                    break;
                case 1:
                    step.setScriptname2(entry.getKey());
                    step.setTypAutomatischScriptpfad2(entry.getValue());
                    break;
                case 2:
                    step.setScriptname3(entry.getKey());
                    step.setTypAutomatischScriptpfad3(entry.getValue());
                    break;
                case 3:
                    step.setScriptname4(entry.getKey());
                    step.setTypAutomatischScriptpfad4(entry.getValue());
                    break;
                case 4:
                    step.setScriptname5(entry.getKey());
                    step.setTypAutomatischScriptpfad5(entry.getValue());
                    break;
                default:
                    break;
            }
        }
    }

    private void setStepProperties(RestStepResource resource, Step step) {
        Boolean val = resource.getProperties().get("metadata");
        if (val != null) {
            step.setTypMetadaten(val.booleanValue());
        }
        val = resource.getProperties().get("automatic");
        if (val != null) {
            step.setTypAutomatisch(val.booleanValue());
        }
        val = resource.getProperties().get("thumbnailGeneration");
        if (val != null) {
            step.setTypAutomaticThumbnail(val.booleanValue());
        }
        val = resource.getProperties().get("readAccess");
        if (val != null) {
            step.setTypImagesLesen(val.booleanValue());
        }
        val = resource.getProperties().get("writeAccess");
        if (val != null) {
            step.setTypImagesSchreiben(val.booleanValue());
        }
        val = resource.getProperties().get("export");
        if (val != null) {
            step.setTypExportDMS(val.booleanValue());
        }
        val = resource.getProperties().get("script");
        if (val != null) {
            step.setTypScriptStep(val.booleanValue());
        }
        val = resource.getProperties().get("validate");
        if (val != null) {
            step.setTypBeimAbschliessenVerifizieren(val.booleanValue());
        }
        val = resource.getProperties().get("batch");
        if (val != null) {
            step.setBatchStep(val);
        }
        val = resource.getProperties().get("delayStep");
        if (val != null) {
            step.setDelayStep(val.booleanValue());
        }
        val = resource.getProperties().get("updateMetadataIndex");
        if (val != null) {
            step.setUpdateMetadataIndex(val.booleanValue());
        }
        val = resource.getProperties().get("generateDocket");
        if (val != null) {
            step.setGenerateDocket(val.booleanValue());
        }
    }

    /*
    JSON:
    curl -H 'Accept: application/json' http://localhost:8080/goobi/api/process/15/metadata
    
    XML:
    curl -H 'Accept: application/xml' http://localhost:8080/goobi/api/process/15/metadata
     */

    @Path("/{processid}/metadata")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Get metadata for a process resource", description = "Get a list of metadata for a given process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response getMetadata(@PathParam("processid") String processid) {

        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }

        int id = Integer.parseInt(processid);
        Process process = ProcessManager.getProcessById(id);
        // process does not exist
        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        // load metadata file
        try {
            Fileformat fileformat = process.readMetadataFile();

            DocStruct logical = fileformat.getDigitalDocument().getLogicalDocStruct();
            List<RestMetadataResource> metadataList = new ArrayList<>();
            if (logical.getType().isAnchor()) {

                metadataList.addAll(extractMetadata(logical, "anchor"));

                DocStruct child = logical.getAllChildren().get(0);

                metadataList.addAll(extractMetadata(child, "topstruct"));
            } else {
                metadataList.addAll(extractMetadata(logical, "topstruct"));
            }
            GenericEntity<List<RestMetadataResource>> entity = new GenericEntity<>(metadataList) {
            };
            return Response.status(200).entity(entity).build();
        } catch (IOException | SwapException | UGHException e) {
            return Response.status(500).entity("Cannot read metadata").build();
        }
    }

    private List<RestMetadataResource> extractMetadata(DocStruct docstruct, String metadataLevel) {
        List<RestMetadataResource> metadataList = new ArrayList<>();
        if (docstruct.getAllMetadata() != null) {
            for (Metadata md : docstruct.getAllMetadata()) {
                metadataList.add(new RestMetadataResource(md, metadataLevel));
            }
        }

        if (docstruct.getAllPersons() != null) {
            for (Person md : docstruct.getAllPersons()) {
                metadataList.add(new RestMetadataResource(md, metadataLevel));
            }
        }

        if (docstruct.getAllCorporates() != null) {
            for (Corporate md : docstruct.getAllCorporates()) {
                metadataList.add(new RestMetadataResource(md, metadataLevel));
            }
        }

        return metadataList;
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X PUT http://localhost:8080/goobi/api/process/119/metadata -d '{"name":"_VolumeBoxNumber","value":"19","metadataLevel":"topstruct"}'
    
    XML:
    curl -H 'Content-Type: application/xml' -X PUT http://localhost:8080/goobi/api/process/119/metadata -d '<metadata><metadataLevel>topstruct</metadataLevel><name>_VolumeBoxNumber</name><value>19</value></metadata>'
     */

    @Path("/{processid}/metadata")
    @PUT
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Update a metadata", description = "Update an existing metadata for a given process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response updateMetadata(@PathParam("processid") String processid, RestMetadataResource resource) {

        if (StringUtils.isBlank(resource.getName())) {
            return Response.status(400).entity("Metadata name is missing.").build();
        }

        if (StringUtils.isBlank(resource.getMetadataLevel())) {
            return Response.status(400).entity("Metadata level is missing.").build();
        }

        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }
        int id = Integer.parseInt(processid);
        Process process = ProcessManager.getProcessById(id);
        // process does not exist
        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        // load metadata file
        try {
            Fileformat fileformat = process.readMetadataFile();
            DocStruct logical = fileformat.getDigitalDocument().getLogicalDocStruct();
            if (logical.getType().isAnchor() && "topstruct".equals(resource.getMetadataLevel())) {
                logical = logical.getAllChildren().get(0);
            }

            if (updateMetadata(logical, resource)) {
                process.writeMetadataFile(fileformat);
                return Response.status(200).entity("Metadata updated").build();
            } else {
                return Response.status(500).entity("Metadata to update not found").build();
            }

        } catch (IOException | SwapException | UGHException e) {
            return Response.status(500).entity("Cannot read metadata").build();
        }
    }

    private boolean updateMetadata(DocStruct docstruct, RestMetadataResource resource) {
        if (docstruct.getAllMetadata() != null) {
            for (Metadata md : docstruct.getAllMetadata()) {
                if (md.getType().getName().equals(resource.getName())) {
                    md.setAuthorityValue(resource.getAuthorityValue());
                    md.setValue(resource.getValue());
                    return true;
                }
            }
        }

        if (docstruct.getAllPersons() != null) {
            for (Person md : docstruct.getAllPersons()) {
                if (md.getType().getName().equals(resource.getName())) {
                    md.setAuthorityValue(resource.getAuthorityValue());
                    md.setFirstname(resource.getFirstname());
                    md.setLastname(resource.getLastname());
                    return true;
                }
            }
        }

        if (docstruct.getAllCorporates() != null) {
            for (Corporate md : docstruct.getAllCorporates()) {
                if (md.getType().getName().equals(resource.getName())) {
                    md.setAuthorityValue(resource.getAuthorityValue());
                    md.setMainName(resource.getValue());
                    return true;
                }
            }
        }

        return false;
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X POST http://localhost:8080/goobi/api/process/119/metadata -d '{"name":"DisplayLayout","value":"1","metadataLevel":"topstruct"}'
    
    XML:
    curl -H 'Content-Type: application/xml' -X POST http://localhost:8080/goobi/api/process/119/metadata -d '<metadata><metadataLevel>topstruct</metadataLevel><name>DisplayLayout</name><value>1</value></metadata>'
     */

    @Path("/{processid}/metadata")
    @POST
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Add metadata to a docstruct", description = "Create a new metadata field and add it to the given docstruct")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response createMetadata(@PathParam("processid") String processid, RestMetadataResource resource) {

        if (StringUtils.isBlank(resource.getName())) {
            return Response.status(400).entity("Metadata name is missing.").build();
        }

        if (StringUtils.isBlank(resource.getMetadataLevel())) {
            return Response.status(400).entity("Metadata level is missing.").build();
        }

        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }
        int id = Integer.parseInt(processid);
        Process process = ProcessManager.getProcessById(id);
        // process does not exist
        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }

        try {
            // load metadata file
            Fileformat fileformat = process.readMetadataFile();
            DocStruct logical = fileformat.getDigitalDocument().getLogicalDocStruct();
            if (logical.getType().isAnchor() && "topstruct".equals(resource.getMetadataLevel())) {
                logical = logical.getAllChildren().get(0);
            }

            Prefs prefs = process.getRegelsatz().getPreferences();
            MetadataType type = prefs.getMetadataTypeByName(resource.getName());
            if (type == null) {
                return Response.status(400).entity("Metadata type is unknown in ruleset.").build();
            }

            // add metadata
            String metadataValue = resource.getValue();
            String authorityValue = resource.getAuthorityValue();
            String firstName = resource.getFirstname();
            String lastName = resource.getLastname();
            addNewMetadataToDocStruct(logical, type, metadataValue, authorityValue, firstName, lastName);

            // save metadata file
            process.writeMetadataFile(fileformat);
            return Response.status(200).entity("Metadata added").build();

        } catch (IOException | SwapException | UGHException e) {
            return Response.status(500).entity("Cannot read metadata").build();
        }
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X DELETE http://localhost:8080/goobi/api/process/119/metadata -d '{"name":"DisplayLayout","metadataLevel":"topstruct"}'
    
    XML:
    curl -H 'Content-Type: application/xml' -X DELETE http://localhost:8080/goobi/api/process/119/metadata -d '<metadata><metadataLevel>topstruct</metadataLevel><name>DisplayLayout</name></metadata>'
     */

    @Path("/{processid}/metadata")
    @DELETE
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Add metadata to a docstruct", description = "Create a new metadata field and add it to the given docstruct")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response deleteMetadata(@PathParam("processid") String processid, RestMetadataResource resource) {
        if (StringUtils.isBlank(resource.getName())) {
            return Response.status(400).entity("Metadata name is missing.").build();
        }

        if (StringUtils.isBlank(resource.getMetadataLevel())) {
            return Response.status(400).entity("Metadata level is missing.").build();
        }

        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }
        int id = Integer.parseInt(processid);
        Process process = ProcessManager.getProcessById(id);
        // process does not exist
        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        // load metadata file
        try {
            Fileformat fileformat = process.readMetadataFile();
            DocStruct docstruct = fileformat.getDigitalDocument().getLogicalDocStruct();
            if (docstruct.getType().isAnchor() && "topstruct".equals(resource.getMetadataLevel())) {
                docstruct = docstruct.getAllChildren().get(id);
            }
            boolean metadataDeleted = false;
            if (docstruct.getAllMetadata() != null) {
                for (Metadata md : docstruct.getAllMetadata()) {
                    if (md.getType().getName().equals(resource.getName())) {
                        docstruct.removeMetadata(md, true);
                        metadataDeleted = true;
                        break;
                    }
                }
            }

            if (docstruct.getAllPersons() != null) {
                for (Person md : docstruct.getAllPersons()) {
                    if (md.getType().getName().equals(resource.getName())) {
                        docstruct.removePerson(md, true);
                        metadataDeleted = true;
                        break;
                    }
                }
            }

            if (docstruct.getAllCorporates() != null) {
                for (Corporate md : docstruct.getAllCorporates()) {
                    if (md.getType().getName().equals(resource.getName())) {
                        docstruct.removeCorporate(md);
                        metadataDeleted = true;
                        break;
                    }
                }
            }
            if (metadataDeleted) {
                process.writeMetadataFile(fileformat);
                return Response.status(200).entity("Metadata deleted.").build();
            } else {
                return Response.status(400).entity("Metadata not found.").build();
            }
        } catch (IOException | SwapException | UGHException e) {
            return Response.status(500).entity("Cannot read metadata").build();
        }

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

        // step data
        md = new AuthenticationMethodDescription("GET", "Get a list of all steps for a given process", "/process/\\d+/steps");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("GET", "Get a specific step", "/process/\\d+/step/\\d+");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("PUT", "Update an existing step", "/process/\\d+/step");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("POST", "Add a new step to an existing process", "/process/\\d+/step");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("DELETE", "Delete a step", "/process/\\d+/step");
        implementedMethods.add(md);

        // journal
        md = new AuthenticationMethodDescription("GET", "Get the journal for a process", "/process/\\d+/journal");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("PUT", "Update an existing journal entry for a given process", "/process/\\d+/journal");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("POST", "Create a new journal entry for a given process", "/process/\\d+/journal");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("DELETE", "Delete an existing journal entry", "/process/\\d+/journal");
        implementedMethods.add(md);

        // properties
        md = new AuthenticationMethodDescription("GET", "Get a list of all properties for a given process", "/process/\\d+/properties");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("GET", "Get a property for a given process", "/process/\\d+/property/\\d+");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("PUT", "Update an existing property for a given process", "/process/\\d+/property");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("POST", "Create a new property for a given process", "/process/\\d+/property");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("DELETE", "Delete a property from a given process", "/process/\\d+/property");
        implementedMethods.add(md);

        return implementedMethods;
    }
}
