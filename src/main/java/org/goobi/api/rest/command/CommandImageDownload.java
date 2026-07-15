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
package org.goobi.api.rest.command;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;
import lombok.extern.log4j.Log4j2;

@Path("/process")
@Log4j2
@Deprecated
public class CommandImageDownload {

    @Context
    private UriInfo uriInfo;

    @Path("download/id/{processId}")
    @GET
    @Produces("application/zip")
    @Deprecated
    public Response download(@PathParam("processId") int processId) {
        org.goobi.beans.Process process = ProcessManager.getProcessById(processId);
        return startDownload(process);

    }

    @Path("download/title/{processTitle}")
    @GET
    @Produces("application/zip")
    @Deprecated
    public Response download(@PathParam("processTitle") String processTitle) {
        org.goobi.beans.Process process = ProcessManager.getProcessByTitle(processTitle);
        return startDownload(process);

    }

    private Response startDownload(org.goobi.beans.Process process) {
        if (process == null) {
            ResponseBuilder response = Response.status(Status.BAD_REQUEST);
            return response.build();
        }
        List<java.nio.file.Path> images = null;
        try {
            images = StorageProvider.getInstance().listFiles(process.getImagesTifDirectory(true));
        } catch (IOException | SwapException e1) {
            log.error(e1);
        }

        if (images == null || images.isEmpty()) {
            ResponseBuilder response = Response.status(Status.NO_CONTENT);
            return response.build();
        }

        final List<java.nio.file.Path> filesToZip = new ArrayList<>();
        for (java.nio.file.Path file : images) {
            if (!StorageProvider.getInstance().isDirectory(file)) {
                filesToZip.add(file);
            }
        }
        if (filesToZip.isEmpty()) {
            ResponseBuilder response = Response.status(Status.NO_CONTENT);
            return response.build();
        }

        StreamingOutput fileStream = output -> {
            try (ZipOutputStream zos = new ZipOutputStream(output)) {
                byte[] buffer = new byte[8192];
                for (java.nio.file.Path file : filesToZip) {
                    zos.putNextEntry(new ZipEntry(file.getFileName().toString()));
                    try (InputStream in = StorageProvider.getInstance().newInputStream(file)) {
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            zos.write(buffer, 0, read);
                        }
                    }
                    zos.closeEntry();
                }
            } catch (IOException e) {
                log.error(e);
                throw new WebApplicationException(e);
            }
        };

        return Response.ok(fileStream, "application/zip")
                .header("content-disposition", "attachment; filename = " + process.getTitel() + ".zip")
                .build();
    }
}
