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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.goobi.api.mq.QueueType;
import org.goobi.api.rest.model.RestReportProblem;
import org.goobi.api.rest.model.RestReportProblemResponse;
import org.goobi.api.rest.model.RestStepQueryResource;
import org.goobi.api.rest.model.RestStepResource;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.Usergroup;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.CloseStepHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.HistoryEventType;
import de.sub.goobi.helper.enums.PropertyType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UsergroupManager;
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
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Path("/process")
public class ProcessStepResource extends AbstractProcessResource implements IRestAuthentication {

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
        Response access = checkProcessAccess(process);
        if (access != null) {
            return access;
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
        Response access = checkProcessAccess(process);
        if (access != null) {
            return access;
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
        Process stepProcess = ProcessManager.getProcessById(step.getProcessId());
        if (stepProcess != null) {
            Response access = checkProcessAccess(stepProcess);
            if (access != null) {
                return access;
            }
        }

        if (resource.getOrder() != null) {
            if (!StringUtils.isNumeric(resource.getOrder()) && !"end".equalsIgnoreCase(resource.getOrder())) {
                return Response.status(400).entity("Order must be numeric or the keyword 'end'").build();
            }

            Process process = ProcessManager.getProcessById(step.getProcessId());
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
        Response access = checkProcessAccess(process);
        if (access != null) {
            return access;
            // check required fields
        }

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
        Process stepProcess = ProcessManager.getProcessById(step.getProcessId());
        if (stepProcess != null) {
            Response access = checkProcessAccess(stepProcess);
            if (access != null) {
                return access;
            }
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
        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        Response access = checkProcessAccess(process);
        if (access != null) {
            return access;
        }

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
        Process stepProcess = ProcessManager.getProcessById(procId);
        if (stepProcess != null) {
            Response access = checkProcessAccess(stepProcess);
            if (access != null) {
                return access;
            }
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
    curl -H 'Content-Type: application/json' -X PUT http://localhost:8080/goobi/api/process/120/step/413/reportproblem -d '{"destinationStepName": "Scanning", "errorText": "Image damaged"}'
     */

    @PUT
    @Path("/{processid}/step/{stepid}/reportproblem")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Report a problem", description = "Send a step back to a correction step")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "403", description = "Forbidden - some requirements are not fulfilled.")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "409", description = "Step belongs to a different process.")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response reportProblem(@PathParam("processid") String processid, @PathParam("stepid") String stepid, RestReportProblem body) {

        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }
        if (StringUtils.isBlank(stepid) || !StringUtils.isNumeric(stepid)) {
            return Response.status(400).entity("Step id is missing.").build();
        }
        int procId = Integer.parseInt(processid);
        int taslId = Integer.parseInt(stepid);
        Step source = StepManager.getStepById(taslId);
        // step does not exist
        if (source == null) {
            return Response.status(404).entity("Step not found").build();
        }
        if (source.getProcessId().intValue() != procId) {
            return Response.status(409).entity("Step belongs to a different process.").build();
        }
        Process stepProcess = ProcessManager.getProcessById(procId);
        if (stepProcess != null) {
            Response access = checkProcessAccess(stepProcess);
            if (access != null) {
                return access;
            }
        }

        String destinationStepName = body == null ? null : body.getDestinationStepName();
        String errorMessage = body == null ? null : body.getErrorText();

        Date myDate = new Date();
        RestReportProblemResponse response = new RestReportProblemResponse();
        response.setErrorStepId(source.getId());
        response.setErrorStepName(source.getTitel());

        source.setBearbeitungsstatusEnum(StepStatus.LOCKED);
        source.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
        source.setBearbeitungszeitpunkt(new Date());
        source.setBearbeitungsbeginn(null);

        Step temp = null;
        for (Step s : source.getProzess().getSchritteList()) {
            if (s.getTitel().equals(destinationStepName)) {
                temp = s;
            }
        }

        if (temp == null) {
            response.setErrorText("Destination step not found");
            return Response.status(400).entity(response).build();
        }

        response.setDestinationStepId(temp.getId());
        response.setDestinationStepName(temp.getTitel());

        temp.setBearbeitungsstatusEnum(StepStatus.OPEN);
        temp.setCorrectionStep();
        temp.setBearbeitungsende(null);

        Helper.addMessageToProcessJournal(temp.getProzess().getId(), LogType.ERROR,
                Helper.getTranslation("Korrektur notwendig") + " [automatic] " + errorMessage, "webapi");

        HistoryManager.addHistory(myDate, temp.getReihenfolge().doubleValue(), temp.getTitel(), HistoryEventType.stepError.getValue(),
                temp.getProzess().getId());

        List<Step> alleSchritteDazwischen = new ArrayList<>();
        for (Step s : source.getProzess().getSchritteList()) {
            if (s.getReihenfolge() <= source.getReihenfolge() && s.getReihenfolge() > temp.getReihenfolge()) {
                alleSchritteDazwischen.add(s);
            }
        }

        for (Step step : alleSchritteDazwischen) {
            step.setBearbeitungsstatusEnum(StepStatus.LOCKED);
            step.setCorrectionStep();
            step.setBearbeitungsende(null);
            GoobiProperty seg = new GoobiProperty(PropertyOwnerType.ERROR);
            seg.setPropertyName(Helper.getTranslation("Korrektur notwendig"));
            seg.setPropertyValue(Helper.getTranslation("KorrekturFuer") + temp.getTitel() + ": " + errorMessage);
            seg.setOwnerObject(step);
            seg.setType(PropertyType.MESSAGE_IMPORTANT);
            seg.setCreationDate(new Date());
            step.getEigenschaften().add(seg);
        }

        try {
            for (Step step : alleSchritteDazwischen) {
                StepManager.saveStep(step);
            }
            ProcessManager.saveProcess(source.getProzess());
        } catch (DAOException e) {
            log.error(e);
            return Response.status(500).entity("Error while saving process/step: " + e.getMessage()).build();
        }
        response.setProcessId(source.getProzess().getId());
        response.setProcessName(source.getProzess().getTitel());
        response.setStatus("ok");

        return Response.ok().entity(response).build();
    }

    /*
    curl -H 'Content-Type: application/json' -X PUT http://localhost:8080/goobi/api/process/120/step/413/error
     */

    @PUT
    @Path("/{processid}/step/{stepid}/error")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Set a step to error status", description = "Set a step to error status")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "403", description = "Forbidden - some requirements are not fulfilled.")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "409", description = "Step belongs to a different process.")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "process")
    public Response setStepToError(@PathParam("processid") String processid, @PathParam("stepid") String stepid) {

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
        Process stepProcess = ProcessManager.getProcessById(procId);
        if (stepProcess != null) {
            Response access = checkProcessAccess(stepProcess);
            if (access != null) {
                return access;
            }
        }

        step.setBearbeitungsstatusEnum(StepStatus.ERROR);
        step.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
        step.setBearbeitungszeitpunkt(new Date());
        step.setBearbeitungsbeginn(null);
        step.setBearbeitungsende(new Date());

        try {
            StepManager.saveStep(step);
        } catch (DAOException e) {
            log.error(e);
            return Response.status(500).entity("Error while saving step: " + e.getMessage()).build();
        }

        return Response.ok().build();
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
            String scriptName = set.get(i).getKey();
            switch (i) {
                case 0:
                    step.setScriptname1(scriptName);
                    break;
                case 1:
                    step.setScriptname2(scriptName);
                    break;
                case 2:
                    step.setScriptname3(scriptName);
                    break;
                case 3:
                    step.setScriptname4(scriptName);
                    break;
                case 4:
                    step.setScriptname5(scriptName);
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

    @Override
    public List<AuthenticationMethodDescription> getAuthenticationMethods() {
        List<AuthenticationMethodDescription> implementedMethods = new ArrayList<>();
        // step data
        AuthenticationMethodDescription md =
                new AuthenticationMethodDescription("GET", "Get a list of all steps for a given process", "/process/\\d+/steps");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("GET", "Get a specific step", "/process/\\d+/step/\\d+");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("PUT", "Update an existing step", "/process/\\d+/step");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("POST", "Add a new step to an existing process", "/process/\\d+/step");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("DELETE", "Delete a step", "/process/\\d+/step");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("PUT", "Report a problem and send a step back to a correction step",
                "/process/\\d+/step/\\d+/reportproblem");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("PUT", "Set a step to error status", "/process/\\d+/step/\\d+/error");
        implementedMethods.add(md);

        return implementedMethods;
    }
}
