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
package org.goobi.api.rest.process.image;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.goobi.beans.Process;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentNotFoundException;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerImageInfoBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * A resource listing all IIIF image urls of the images in the given folder
 * 
 * @author Florian Alpers
 *
 */
@Path("/image/{process}/{folder}")
@ContentServerBinding
public class GoobiImageFolderResource {

    private final java.nio.file.Path folderPath;

    public GoobiImageFolderResource(HttpServletRequest request, String directory) {
        this.folderPath = Paths.get(directory);
    }

    public GoobiImageFolderResource(@Context HttpServletRequest request, @PathParam("process") String process, @PathParam("folder") String folder)
            throws ContentLibException, IOException, InterruptedException, SwapException, DAOException {
        this.folderPath = getImagesFolder(getProcess(process), folder);
    }

    private Process getProcess(String processIdString) {
        int processId = Integer.parseInt(processIdString);
        return ProcessManager.getProcessById(processId);
    }

    private java.nio.file.Path getImagesFolder(Process process, String folder)
            throws ContentNotFoundException, IOException, InterruptedException, SwapException, DAOException {
        switch (folder.toLowerCase()) {
            case "master":
            case "orig":
                return Paths.get(process.getImagesOrigDirectory(false));
            case "media":
            case "tif":
                return Paths.get(process.getImagesTifDirectory(false));
            case "thumbnails_large":
                return Paths.get(process.getImagesDirectory(), "layoutWizzard-temp", "thumbnails_large");
            case "thumbnails_small":
                return Paths.get(process.getImagesDirectory(), "layoutWizzard-temp", "thumbnails_small");
            default:
                return Paths.get(process.getImagesTifDirectory(false).replaceAll("_tif|_media", "_" + folder));
        }
    }

    @GET
    @Path("/list")
    @Operation(summary = "Returns information about image directories",
            description = "Returns information about image directories in JSON or JSONLD format")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Produces({ ContentServerResource.MEDIA_TYPE_APPLICATION_JSONLD, MediaType.APPLICATION_JSON })
    @ContentServerImageInfoBinding
    public List<URI> getListAsJson(@Context ContainerRequestContext requestContext, @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws ContentLibException, IOException {
        String requestURL = request.getRequestURL().toString();
        if (!requestURL.endsWith("/")) {
            requestURL += "/";
        }
        String imageURL = requestURL.replace("/list/", "/");
        try (Stream<java.nio.file.Path> imagePaths = Files.list(folderPath)) {
            return imagePaths.map(path -> URI.create(imageURL.replace("list.json", "") + path.getFileName().toString()))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

}
