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

import org.apache.commons.lang3.StringUtils;
import org.goobi.api.rest.model.RestJournalResource;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Process;
import org.goobi.production.enums.LogType;

import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Path("/process")
public class ProcessJournalResource extends AbstractProcessResource implements IRestAuthentication {

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
        Process process = ProcessManager.getProcessById(id);
        if (process != null) {
            Response access = checkProcessAccess(process);
            if (access != null) {
                return access;
            }
        }
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
        int id = Integer.parseInt(processid);
        Process process = ProcessManager.getProcessById(id);
        if (process != null) {
            Response access = checkProcessAccess(process);
            if (access != null) {
                return access;
            }
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
        int id = Integer.parseInt(processid);
        Process process = ProcessManager.getProcessById(id);
        if (process != null) {
            Response access = checkProcessAccess(process);
            if (access != null) {
                return access;
            }
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
        int id = Integer.parseInt(processid);
        Process process = ProcessManager.getProcessById(id);
        if (process != null) {
            Response access = checkProcessAccess(process);
            if (access != null) {
                return access;
            }
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

    @Override
    public List<AuthenticationMethodDescription> getAuthenticationMethods() {
        List<AuthenticationMethodDescription> implementedMethods = new ArrayList<>();
        // journal
        AuthenticationMethodDescription md = new AuthenticationMethodDescription("GET", "Get the journal for a process", "/process/\\d+/journal");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("PUT", "Update an existing journal entry for a given process", "/process/\\d+/journal");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("POST", "Create a new journal entry for a given process", "/process/\\d+/journal");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("DELETE", "Delete an existing journal entry", "/process/\\d+/journal");
        implementedMethods.add(md);

        return implementedMethods;
    }
}
