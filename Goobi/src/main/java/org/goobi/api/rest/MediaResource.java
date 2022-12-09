/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.api.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.message.internal.ReaderWriter;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
@javax.ws.rs.Path("/view/media")
public class MediaResource {

    private static final Path metadataFolderPath = Paths.get(ConfigurationHelper.getInstance().getMetadataFolder());

    @GET
    @javax.ws.rs.Path("{process}/{folder}/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Serves a media resource",
            description = "Serves a media resource consisting of a process name, a directory name and a resource name")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "500", description = "Internal error")

    public Response serveMediaContent(@PathParam("process") String processIdString, @PathParam("folder") String folder,
            @PathParam("filename") String filename) {

        Path processFolder = metadataFolderPath.resolve(processIdString);

        Path mediaFolder = getImagesFolder(processFolder, folder);
        Path mediaResource = mediaFolder.resolve(filename);

        StreamingOutput entity = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try (InputStream in = StorageProvider.getInstance().newInputStream(mediaResource)) {
                    ReaderWriter.writeTo(in, output);
                }
            }
        };
        return Response.ok(entity).build();
    }

    private Path getImagesFolder(Path processFolder, String folder) {
        try {
            switch (folder.toLowerCase()) {
                case "master":
                case "orig":
                    return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getImagesOrigDirectory(false));
                case "media":
                case "tif":
                    return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getImagesTifDirectory(false));
                case "thumbnails_large":
                    return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getImagesDirectory(), "layoutWizzard-temp",
                            "thumbnails_large");
                case "thumbnails_small":
                    return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getImagesDirectory(), "layoutWizzard-temp",
                            "thumbnails_small");
                default:
                    if (!folder.contains("_")) {
                        return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getImagesDirectory(), folder);
                    } else {
                        return processFolder.resolve("images").resolve(folder);
                    }
            }
        } catch (IOException | SwapException | DAOException e) {
            log.error(e);
        }
        return processFolder.resolve("images").resolve(folder);
    }

    /**
     * @param processIdString
     * @return
     */
    private synchronized org.goobi.beans.Process getGoobiProcess(String processIdString) {
        int processId = Integer.parseInt(processIdString);
        return ProcessManager.getProcessById(processId);
    }

}
