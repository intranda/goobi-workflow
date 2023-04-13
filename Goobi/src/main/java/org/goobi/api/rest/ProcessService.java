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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.goobi.api.rest.model.RestProcessResource;
import org.goobi.beans.Batch;
import org.goobi.beans.Docket;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.DocketManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.log4j.Log4j2;

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
            // check if new title fulfills requirements
            String validateRegEx = ConfigurationHelper.getInstance().getProcessTitleValidationRegex();
            if (!newTitle.matches(validateRegEx)) {
                // invalid characters, return 406 - Not Acceptable
                return Response.status(406).entity("Title contains invalid characters").build();
            }
            // check if new title can be used
            if (ProcessManager.countProcessTitle(newTitle, process.getProjekt().getInstitution()) != 0) {
                // new title is not unique, return 409 - Conflict
                return Response.status(409).entity("Title is not unique").build();
            }
            // rename folder, update metadata etc
            process.changeProcessTitle(newTitle);
        } else {
            // save process
            ProcessManager.saveProcessInformation(process);
        }
        return getProcessData(String.valueOf(id));
    }

    @PUT
    @Path("/")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Create a new process", description = "Create a new process")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "500", description = "Internal error")
    public Response createProcess(RestProcessResource resource) {

        // validate input data


        return null;
    }

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

}
