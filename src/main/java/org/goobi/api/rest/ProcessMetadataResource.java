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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.goobi.api.rest.model.RestMetadataResource;
import org.goobi.beans.Process;

import de.sub.goobi.helper.exceptions.SwapException;
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
import ugh.dl.Corporate;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.UGHException;

@Path("/process")

public class ProcessMetadataResource extends AbstractProcessResource implements IRestAuthentication {

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
    @Tag(name = "metadata")
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
        Response access = checkProcessAccess(process);
        if (access != null) {
            return access;
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
    @Tag(name = "metadata")
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
        Response access = checkProcessAccess(process);
        if (access != null) {
            return access;
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
    @Tag(name = "metadata")
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
        Response access = checkProcessAccess(process);
        if (access != null) {
            return access;
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
    @Tag(name = "metadata")
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
        Response access = checkProcessAccess(process);
        if (access != null) {
            return access;
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
        List<AuthenticationMethodDescription> m = new ArrayList<>();
        m.add(new AuthenticationMethodDescription("GET", "Get the metadata for a process", "/process/\\d+/metadata"));
        m.add(new AuthenticationMethodDescription("PUT", "Update metadata for a process", "/process/\\d+/metadata"));
        m.add(new AuthenticationMethodDescription("POST", "Create metadata for a process", "/process/\\d+/metadata"));
        m.add(new AuthenticationMethodDescription("DELETE", "Delete metadata from a process", "/process/\\d+/metadata"));
        return m;
    }
}
