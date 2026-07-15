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
import org.goobi.api.rest.model.RestPropertyResource;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Process;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
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

public class ProcessPropertyResource extends AbstractProcessResource implements IRestAuthentication {

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
    @Tag(name = "property")

    public Response getProperties(@PathParam("processid") String processid) {
        if (StringUtils.isBlank(processid) || !StringUtils.isNumeric(processid)) {
            return Response.status(400).entity("Process id is missing.").build();
        }
        Process process = ProcessManager.getProcessById(Integer.parseInt(processid));
        if (process != null) {
            Response access = checkProcessAccess(process);
            if (access != null) {
                return access;
            }
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
    @Tag(name = "property")

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
        Process propProcess = ProcessManager.getProcessById(property.getObjectId());
        if (propProcess != null) {
            Response access = checkProcessAccess(propProcess);
            if (access != null) {
                return access;
            }
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
    @Tag(name = "property")
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
        Process propProcess = ProcessManager.getProcessById(property.getObjectId());
        if (propProcess != null) {
            Response access = checkProcessAccess(propProcess);
            if (access != null) {
                return access;
            }
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
    @Tag(name = "property")
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
        Response access = checkProcessAccess(process);
        if (access != null) {
            return access;
        }

        String propertyName = resource.getName();
        String propertyValue = resource.getValue();
        Date creationDate = resource.getCreationDate(); // maybe null but it doesn't matter
        GoobiProperty property = saveNewProcessproperty(process, propertyName, propertyValue, creationDate, null);

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
    @Tag(name = "property")
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
        Process propProcess = ProcessManager.getProcessById(property.getObjectId());
        if (propProcess != null) {
            Response access = checkProcessAccess(propProcess);
            if (access != null) {
                return access;
            }
        }
        if (property.getObjectId().intValue() != Integer.parseInt(processid)) {
            return Response.status(409).entity("Property belongs to a different process.").build();
        }

        PropertyManager.deleteProperty(property);

        Helper.addMessageToProcessJournal(property.getObjectId(), LogType.DEBUG, "Property deleted using REST-API: " + property.getPropertyName());
        return Response.status(200).build();
    }

    @Override
    public List<AuthenticationMethodDescription> getAuthenticationMethods() {
        List<AuthenticationMethodDescription> implementedMethods = new ArrayList<>();
        // properties
        AuthenticationMethodDescription md =
                new AuthenticationMethodDescription("GET", "Get a list of all properties for a given process", "/process/\\d+/properties");
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
