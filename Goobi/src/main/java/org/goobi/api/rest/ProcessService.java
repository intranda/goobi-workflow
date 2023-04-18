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
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.goobi.api.mq.QueueType;
import org.goobi.api.rest.model.RestJournalResource;
import org.goobi.api.rest.model.RestProcessResource;
import org.goobi.api.rest.model.RestPropertyResource;
import org.goobi.api.rest.model.RestStepResource;
import org.goobi.beans.Batch;
import org.goobi.beans.Docket;
import org.goobi.beans.Institution;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.beans.Usergroup;
import org.goobi.production.enums.LogType;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.CloseStepHelper;
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

public class ProcessService {
    @Context
    HttpServletRequest request;

    /*
    JSON:
    curl -H 'Accept: application/json' http://localhost:8080/goobi/api/process/15

    XML:
    curl -H 'Accept: application/xml' http://localhost:8080/goobi/api/process/15

     */
    @Path("/{processid}")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Serves a process resource", description = "Serves a process resource consisting of a process name, id, project name")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
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
    curl -H 'Content-Type: application/json' -X POST http://localhost:8080/goobi/api/process/ -d '{"id":15,"title":"990743934_1885","projectName":"Archive_Project",
    "creationDate":1643983095000,"status":"020040040","numberOfImages":248,"numberOfMetadata":804,"numberOfDocstructs":67,"rulesetName":"ruleset.xml",
    "docketName":"Standard"}'

    XML:
    curl -H 'Content-Type: application/xml' -X POST http://localhost:8080/goobi/api/process/ -d '<process><creationDate>2022-02-04T14:58:15+01:00
    </creationDate><docketName>Standard</docketName><id>15</id><numberOfDocstructs>67</numberOfDocstructs><numberOfImages>248</numberOfImages>
    <numberOfMetadata>804 </numberOfMetadata><projectName>Archive_Project</projectName><rulesetName>ruleset.xml</rulesetName>
    <status>020040040</status><title>990743934_1885</title></process>'
     */
    @POST
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
            // search for new project
            try {
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
        }
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
        // change ruleset
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
        // change docket
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
        // change batch
        if (resource.getBatchNumber() != null && resource.getBatchNumber() != 0
                && (process.getBatch() == null || process.getBatch().getBatchId() != resource.getBatchNumber())) {
            Batch batch = ProcessManager.getBatchById(resource.getBatchNumber());
            if (batch == null) {
                return Response.status(404).entity("Batch not found").build();
            }
            process.setBatch(batch);
        }

        // update process title
        if (StringUtils.isNotBlank(resource.getTitle()) && !process.getTitel().equals(resource.getTitle())) {
            String newTitle = resource.getTitle();
            Response resp = validateProcessTitle(newTitle, process.getProjekt().getInstitution());
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
        return getProcessData(String.valueOf(id));
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
    curl -H 'Content-Type: application/json' -X PUT http://localhost:8080/goobi/api/process/ -d '{"title":"1234", "processTemplateName": "template",
    "documentType": "Monograph"}'

    XML:
    curl -H 'Content-Type: application/xml' -X PUT http://localhost:8080/goobi/api/process/ -d '<process><title>1234</title><processTemplateName>template
    </processTemplateName><documentType>Monograph</documentType></process>'
     */

    @PUT
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
    public Response createProcess(RestProcessResource resource) {

        //TODO optional metadata
        //TODO optional properties
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

        Process newProcess = prepareProcess(processTitle, template);

        try {
            // optional: change project
            if (StringUtils.isNotBlank(resource.getProjectName())) {
                Project newProject = ProjectManager.getProjectByName(resource.getProjectName());
                if (newProject == null) {
                    return Response.status(404).entity("Project not found").build();
                }
                newProcess.setProjekt(newProject);
            }
            // optional: change ruleset
            if (StringUtils.isNotBlank(resource.getRulesetName())) {
                Ruleset ruleset = RulesetManager.getRulesetByName(resource.getRulesetName());
                if (ruleset == null) {
                    return Response.status(404).entity("Ruleset not found").build();
                }
                newProcess.setRegelsatz(ruleset);
            }
            // optional: change docket
            if (StringUtils.isNotBlank(resource.getDocketName())) {
                Docket docket = DocketManager.getDocketByName(resource.getRulesetName());
                if (docket == null) {
                    return Response.status(404).entity("Docket not found").build();
                }
                newProcess.setDocket(docket);
            }
            // optional: add process to a batch
            if (resource.getBatchNumber() != null) {
                Batch batch = ProcessManager.getBatchById(resource.getBatchNumber());
                if (batch == null) {
                    batch = new Batch();
                }
                newProcess.setBatch(batch);
            }
        } catch (DAOException e) {
            log.error(e);
        }

        // add optional data
        if (resource.getCreationDate() != null) {
            newProcess.setErstellungsdatum(resource.getCreationDate());
        }
        if (StringUtils.isNotBlank(resource.getStatus()) && StringUtils.isNumeric(resource.getStatus())) {
            newProcess.setSortHelperStatus(resource.getStatus());
        }
        if (resource.getNumberOfImages() != null) {
            newProcess.setSortHelperImages(resource.getNumberOfImages());
        }
        if (resource.getNumberOfMetadata() != null) {
            newProcess.setSortHelperMetadata(resource.getNumberOfMetadata());
        }
        if (resource.getNumberOfDocstructs() != null) {
            newProcess.setSortHelperDocstructs(resource.getNumberOfDocstructs());
        }

        try {
            // save process to create ids and directories
            ProcessManager.saveProcess(newProcess);

            // create dummy metadata file
            if (StringUtils.isNotBlank(resource.getDocumentType())) {
                createMetadataFile(resource, newProcess);
            }
        } catch (DAOException | UGHException e) {
            log.error(e);
        }

        return getProcessData(String.valueOf(newProcess.getId()));
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

    private Process prepareProcess(String processName, Process template) {
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

        GenericEntity<List<RestStepResource>> entity = new GenericEntity<List<RestStepResource>>(stepList) {
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
    curl -H 'Content-Type: application/json' -X POST http://localhost:8080/goobi/api/process/15/step -d '{"stepId": 67, "stepName": "new step name", "processId": 15}'

    XML:
    curl -H 'Content-Type: application/xml' -X POST http://localhost:8080/goobi/api/process/15/step -d '<step><processId>15</processId><stepId>67</stepId><stepName>new step name</stepName></step>'
     */
    @POST
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

        if (StringUtils.isNotBlank(resource.getStepName())) {
            step.setTitel(resource.getStepName());
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
        return getStep(String.valueOf(resource.getProcessId()), String.valueOf(resource.getStepId()));
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X PUT http://localhost:8080/goobi/api/process/15/step -d '{"stepName": "new step name", "processId": 15, "order": 10,"usergroups": ["Administration"]}'


    XML:
    curl -H 'Content-Type: application/xml' -X PUT http://localhost:8080/goobi/api/process/15/step -d '<step><order>10</order><stepName>new step name</stepName><usergroups>Administration</usergroups></step>'
     */

    @PUT
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
    public Response createStep(@PathParam("processid") String processid, RestStepResource resource) {

        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).build();
        }
        int id = Integer.parseInt(processid);
        Process process = ProcessManager.getProcessById(id);
        // process does not exist
        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        // check required fields

        if (StringUtils.isBlank(resource.getStepName())) {
            return Response.status(400).entity("Step name is missing").build();
        }
        if (resource.getOrder() == null) {
            return Response.status(400).entity("Step order is missing").build();
        }

        if (resource.getUsergroups().isEmpty()) {
            return Response.status(400).entity("Assigned usergroups are missing").build();
        }

        Step step = new Step();
        step.setTitel(resource.getStepName());
        step.setReihenfolge(resource.getOrder());
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
    public Response deleteStep(RestStepResource resource) {

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

        return Response.ok().build();
    }

    /*
    curl -H 'Content-Type: application/json' -X POST http://localhost:8080/goobi/api/process/120/step/413/close
     */

    @POST
    @Path("/{processid}/step/{stepid}/close")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Close a step", description = "Close a step")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
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

        GenericEntity<List<RestJournalResource>> entity = new GenericEntity<List<RestJournalResource>>(answer) {
        };
        return Response.status(200).entity(entity).build();

    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X POST http://localhost:8080/goobi/api/process/15/journal -d '{"id": 70, "userName": "Doe, John", "logType": "info", "content": "content"}'

    XML:
    curl -H 'Content-Type: application/xml' -X POST http://localhost:8080/goobi/api/process/15/journal -d '<journal><id>70</id><userName>Doe, John</userName><logType>info</logType><content>content</content></journal>'
     */

    @Path("/{processid}/journal")
    @POST
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Update a journal entry", description = "Update an existing journal entry for a given process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
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
        if (StringUtils.isNotBlank(resource.getContent())) {
            entry.setContent(resource.getContent());
        }
        if (StringUtils.isNotBlank(resource.getUserName())) {
            entry.setUserName(resource.getUserName());
        }
        if (StringUtils.isNotBlank(resource.getLogType())) {
            entry.setType(LogType.getByTitle(resource.getLogType()));
        }
        if (StringUtils.isNotBlank(resource.getFilename())) {
            entry.setFilename(resource.getFilename());
        }
        JournalManager.saveJournalEntry(entry);
        return Response.status(200).entity(new RestJournalResource(entry)).build();
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X PUT http://localhost:8080/goobi/api/process/15/journal -d '{"userName": "Doe, John", "logType": "info", "content": "content"}'

    XML:
    curl -H 'Content-Type: application/xml' -X PUT http://localhost:8080/goobi/api/process/15/journal -d '<journal><userName>Doe, John</userName><logType>info</logType><content>content</content></journal>'
     */

    @Path("/{processid}/journal")
    @PUT
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Create a new journal entry", description = "Create a new journal entry for a given process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
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
        if (StringUtils.isNotBlank(resource.getLogType())) {
            logType = LogType.getByTitle(resource.getLogType());
        } else {
            logType = LogType.DEBUG;
        }

        String content = resource.getContent();
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

    public Response getProperties(@PathParam("processid") String processid) {
        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }

        List<Processproperty> properties = PropertyManager.getProcessPropertiesForProcess(Integer.parseInt(processid));

        List<RestPropertyResource> answer = new ArrayList<>(properties.size());

        for (Processproperty entry : properties) {
            answer.add(new RestPropertyResource(entry));
        }

        GenericEntity<List<RestPropertyResource>> entity = new GenericEntity<List<RestPropertyResource>>(answer) {
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

    public Response getProperty(@PathParam("processid") String processid, @PathParam("propertyid") String propertyid) {
        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }
        if (StringUtils.isBlank(propertyid) || !StringUtils.isNumeric(propertyid)) {
            return Response.status(400).entity("Property id is missing.").build();
        }
        int propId = Integer.parseInt(propertyid);
        Processproperty property = PropertyManager.getProcessPropertyById(propId);
        if (property == null) {
            return Response.status(404).entity("Property not found").build();
        }

        return Response.status(200).entity(new RestPropertyResource(property)).build();
    }


    /*
    JSON:
    curl -H 'Content-Type: application/json' -X POST http://localhost:8080/goobi/api/process/15/property -d '{"id":76,"name":"name","value":"value"}'

    XML:
    curl -H 'Content-Type: application/xml' -X POST http://localhost:8080/goobi/api/process/15/property -d '<property><id>76</id><name>name</name><value>value</value></property>'
     */

    @Path("/{processid}/property")
    @POST
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Update a property", description = "Update an existing property for a given process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    public Response updateProperty(@PathParam("processid") String processid, RestPropertyResource resource) {
        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }
        if (resource.getId() == null || resource.getId().intValue() == 0) {
            return Response.status(400).entity("Property id is missing.").build();
        }
        Processproperty property = PropertyManager.getProcessPropertyById(resource.getId());
        if (property == null) {
            return Response.status(404).entity("Property not found").build();
        }
        if (property.getProcessId().intValue() != Integer.parseInt(processid)) {
            return Response.status(409).entity("Property belongs to a different process.").build();
        }
        if (StringUtils.isNotBlank(resource.getName())) {
            property.setTitel(resource.getName());
        }
        if (StringUtils.isNotBlank(resource.getValue())) {
            property.setWert(resource.getValue());
        }

        PropertyManager.saveProcessProperty(property);
        return Response.status(200).entity(new RestPropertyResource(property)).build();
    }

    /*
    JSON:
    curl -H 'Content-Type: application/json' -X PUT http://localhost:8080/goobi/api/process/15/property -d '{"name":"name","value":"value"}'

    XML:
    curl -H 'Content-Type: application/xml' -X PUT http://localhost:8080/goobi/api/process/15/property -d '<property><name>name</name><value>value</value></property>'
     */

    @Path("/{processid}/property")
    @PUT
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Create a property", description = "Create a new property for a given process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
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

        Processproperty property = new Processproperty();
        property.setTitel(resource.getName());
        property.setWert(resource.getValue());
        property.setProzess(process);
        property.setType(PropertyType.STRING);
        if (resource.getCreationDate() != null) {
            property.setCreationDate(resource.getCreationDate());
        } else {
            property.setCreationDate(new Date());
        }
        PropertyManager.saveProcessProperty(property);
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
    public Response deleteProperty(@PathParam("processid") String processid, RestPropertyResource resource) {
        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }
        if (resource.getId() == null || resource.getId().intValue() == 0) {
            return Response.status(400).entity("Property id is missing.").build();
        }
        Processproperty property = PropertyManager.getProcessPropertyById(resource.getId());
        if (property == null) {
            return Response.status(404).entity("Property not found").build();
        }
        if (property.getProcessId().intValue() != Integer.parseInt(processid)) {
            return Response.status(409).entity("Property belongs to a different process.").build();
        }

        PropertyManager.deleteProcessProperty(property);

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
        if (resource.getOrder() != null) {
            step.setReihenfolge(resource.getOrder());
        }
        if (resource.getStartDate() != null) {
            step.setBearbeitungsbeginn(resource.getStartDate());
        }

        if (resource.getFinishDate() != null) {
            step.setBearbeitungsende(resource.getFinishDate());
        }
        if (StringUtils.isNotBlank(resource.getStepPlugin())) {
            step.setStepPlugin(resource.getStepPlugin());
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
}
