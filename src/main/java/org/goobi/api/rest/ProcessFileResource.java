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
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.goobi.api.rest.model.RestFileListResource;
import org.goobi.beans.Process;

import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Path("/process")

public class ProcessFileResource extends AbstractProcessResource implements IRestAuthentication {

    //  curl  -H 'token: XXXX'  http://localhost:8080/goobi/api/process/1492/files/master/0000001.tif
    @GET
    @Path("/{processid}/files/{folder}/{file}")
    @Operation(summary = "Download a file", description = "Download a file from a process folder")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    @Tag(name = "file")
    public Response downloadFle(@PathParam("processid") String processid, @PathParam("folder") final String folder,
            @PathParam("file") String inFilename) {
        Process process = ProcessManager.getProcessById(Integer.parseInt(processid));
        // process does not exist
        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        Response access = checkProcessAccess(process);
        if (access != null) {
            return access;
        }

        if (StringUtils.isBlank(folder)) {
            return Response.status(400).entity("Folder is missing").build();
        }
        if (StringUtils.isBlank(inFilename)) {
            return Response.status(400).entity("File is missing").build();
        }
        String destFolder = null;
        try {
            destFolder = process.getConfiguredImageFolder(folder);
        } catch (IOException | SwapException | DAOException e) {
            log.error(e);
            return Response.status(500).build();
        }
        if (destFolder == null) {
            return Response.status(404).entity("Folder is unknown").build();
        }
        java.nio.file.Path path = Paths.get(destFolder);
        if (!StorageProvider.getInstance().isFileExists(path)) {
            return Response.status(404).entity("Folder not found").build();
        }

        // sanitize filename
        String filename;
        try {
            filename = getFilename(URLDecoder.decode(inFilename, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity("Invalid filename").build();
        }
        java.nio.file.Path file = path.resolve(filename);

        if (!StorageProvider.getInstance().isFileExists(file)) {
            return Response.status(404).entity("File not found").build();
        }

        return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + file.getFileName() + "\"")
                .build();

    }

    // curl  -H 'token: XXXX' -H 'Accept: application/xml' http://localhost:8080/goobi/api/process/1492/files/master

    // curl  -H 'token: XXXX' -H 'Accept: application/json' http://localhost:8080/goobi/api/process/1492/files/master

    // get a list of files form the current folder
    @GET
    @Path("/{processid}/files/{folder}")
    @Operation(summary = "Get a list of all files", description = "Get a list of all files within the process folder")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Tag(name = "file")
    public Response getFileList(@PathParam("processid") String processid, @PathParam("folder") final String folder) {
        Process process = ProcessManager.getProcessById(Integer.parseInt(processid));
        // process does not exist
        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        Response access = checkProcessAccess(process);
        if (access != null) {
            return access;
        }

        if (StringUtils.isBlank(folder)) {
            return Response.status(400).entity("Folder is missing").build();
        }

        String destFolder = null;
        try {
            destFolder = process.getConfiguredImageFolder(folder);
        } catch (IOException | SwapException | DAOException e) {
            log.error(e);
            return Response.status(500).build();
        }
        if (destFolder == null) {
            return Response.status(404).entity("Folder is unknown").build();
        }
        java.nio.file.Path path = Paths.get(destFolder);
        if (!StorageProvider.getInstance().isFileExists(path)) {
            return Response.status(404).entity("Folder not found").build();
        }

        List<String> filesInFolder = StorageProvider.getInstance().list(path.toString());

        return Response.status(200).entity(new RestFileListResource(filesInFolder)).build();

    }

    // curl -i -X POST -H 'token: XXXX' -H 'Content-Type:multipart/form-data' -F 'file=@/path/to/file/abc.tif' -F 'filename=00000001.tif' http://localhost:8080/goobi/api/processes/1492/files/master

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{processid}/files/{folder}")
    @Operation(summary = "Add file to a folder", description = "Add a new file to an existing process folder")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "file")
    public Response uploadFile(@PathParam("processid") String processid, @PathParam("folder") final String folder,
            @FormDataParam("file") InputStream fileInputStream, @FormDataParam("file") FormDataContentDisposition fileMetaData,
            @FormDataParam("filename") String filename) {

        Process process = ProcessManager.getProcessById(Integer.parseInt(processid));
        // process does not exist
        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        Response access = checkProcessAccess(process);
        if (access != null) {
            return access;
        }

        if (StringUtils.isBlank(folder)) {
            return Response.status(400).entity("Folder is missing").build();
        }

        String destFolder = null;
        try {
            destFolder = process.getConfiguredImageFolder(folder);
        } catch (IOException | SwapException | DAOException e) {
            log.error(e);
            return Response.status(500).build();
        }
        if (destFolder == null) {
            return Response.status(404).entity("Folder is unknown").build();
        }
        java.nio.file.Path path = Paths.get(destFolder);
        if (!StorageProvider.getInstance().isFileExists(path)) {
            try {
                StorageProvider.getInstance().createDirectories(path);
            } catch (IOException e) {
                log.error(e);
                return Response.status(500).build();
            }
        }
        try {
            String file = filename == null || filename.isEmpty() ? fileMetaData.getFileName() : filename;
            file = URLDecoder.decode(file, StandardCharsets.UTF_8);
            file = getFilename(file);
            java.nio.file.Path dest = path.resolve(file);
            StorageProvider.getInstance().uploadFile(fileInputStream, dest, fileMetaData.getSize());
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity("Invalid filename").build();
        } catch (IOException e) {
            log.error(e);
            return Response.status(500).build();
        }
        return Response.ok().build();
    }

    // curl -i -X DELETE -H 'token: XXXX' http://localhost:8080/goobi/api/process/1492/files/master/abc.tif

    @DELETE
    @Path("/{processid}/files/{folder}/{file}")
    @Operation(summary = "Delete a file from a folder", description = "Delete an existing file from a process folder")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "file")
    public Response deleteFile(@PathParam("processid") String processid, @PathParam("folder") final String folder,
            @PathParam("file") String inFilename) {
        Process process = ProcessManager.getProcessById(Integer.parseInt(processid));
        // process does not exist
        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        Response access = checkProcessAccess(process);
        if (access != null) {
            return access;
        }

        if (StringUtils.isBlank(folder)) {
            return Response.status(400).entity("Folder is missing").build();
        }
        if (StringUtils.isBlank(inFilename)) {
            return Response.status(400).entity("File is missing").build();
        }
        String destFolder = null;
        try {
            destFolder = process.getConfiguredImageFolder(folder);
        } catch (IOException | SwapException | DAOException e) {
            log.error(e);
            return Response.status(500).build();
        }
        if (destFolder == null) {
            return Response.status(404).entity("Folder is unknown").build();
        }
        java.nio.file.Path path = Paths.get(destFolder);
        if (!StorageProvider.getInstance().isFileExists(path)) {
            return Response.status(404).entity("Folder not found").build();
        }

        // sanitize filename
        String filename;
        try {
            filename = getFilename(URLDecoder.decode(inFilename, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity("Invalid filename").build();
        }
        java.nio.file.Path file = path.resolve(filename);

        if (!StorageProvider.getInstance().isFileExists(file)) {
            return Response.status(404).entity("File not found").build();
        }

        try {
            StorageProvider.getInstance().deleteFile(file);
        } catch (IOException e) {
            log.error(e);
            return Response.status(500).build();
        }

        return Response.ok().build();
    }

    public String getFilename(String file) {
        java.nio.file.Path p = java.nio.file.Path.of(file).getFileName();
        if (p == null) {
            throw new IllegalArgumentException("Invalid filename: " + file);
        }
        return p.toString();
    }

    @Override
    public List<AuthenticationMethodDescription> getAuthenticationMethods() {
        List<AuthenticationMethodDescription> implementedMethods = new ArrayList<>();
        // files
        AuthenticationMethodDescription md =
                new AuthenticationMethodDescription("GET", "Get a list of all files in a process folder", "/process/\\d+/files/\\w+");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("GET", "Download a single file", "/process/\\d+/files/\\w+/\\w+");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("POST", "Add a file to a process folder", "/process/\\d+/files/\\w+/\\w+");
        implementedMethods.add(md);
        md = new AuthenticationMethodDescription("DELETE", "Delete a file from a given process folder", "/process/\\d+/files/\\w+/\\w+");
        implementedMethods.add(md);

        return implementedMethods;
    }
}
