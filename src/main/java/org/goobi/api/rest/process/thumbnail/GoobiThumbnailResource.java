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
package org.goobi.api.rest.process.thumbnail;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.goobi.api.rest.process.image.AbstractImageResource;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.metadaten.Image;
import de.unigoettingen.sub.commons.contentlib.exceptions.IllegalRequestException;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerBinding;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.log4j.Log4j2;

@jakarta.ws.rs.Path("process/thumbs/{processId}/{foldername}/{filename}")
@ContentServerBinding
@Log4j2
public class GoobiThumbnailResource extends AbstractImageResource {

    private static final Path METADATA_PATH = Paths.get(ConfigurationHelper.getInstance().getMetadataFolder());

    public GoobiThumbnailResource(@Context ContainerRequestContext context, @Context HttpServletRequest request,
            @Context HttpServletResponse response, @PathParam("processId") String processId, @PathParam("foldername") String foldername,
            @PathParam("filename") String filename) throws IllegalRequestException {
        super(context, request, response, "-", filename);
        createResourceURI(request, processId, foldername, filename);
        createImageURI(processId, foldername, filename);
    }

    public void createImageURI(String processId, String foldername, String filename) {
        Path imagePath = METADATA_PATH.resolve(processId).resolve("thumbs").resolve(foldername).resolve(filename);
        this.imageURI = Image.toURI(imagePath);

    }

    public void createResourceURI(HttpServletRequest request, String processId, String foldername, String filename) throws IllegalRequestException {

        if (request != null) {
            try {
                URI uriBase = getUriBase(request);
                resourceURI = new URI(uriBase.toString()
                        .replace(URLEncoder.encode("{processId}", StandardCharsets.UTF_8), URLEncoder.encode(processId, StandardCharsets.UTF_8))
                        .replace(URLEncoder.encode("{foldername}", StandardCharsets.UTF_8), URLEncoder.encode(foldername, StandardCharsets.UTF_8))
                        .replace(URLEncoder.encode("{filename}", StandardCharsets.UTF_8), URLEncoder.encode(filename, StandardCharsets.UTF_8)));
            } catch (URISyntaxException e) {
                log.error("Failed to create image request uri");
                throw new IllegalRequestException("Unable to evaluate request to '" + foldername + "/" + filename + "'");
            }
        } else {
            try {
                resourceURI = new URI("");
            } catch (URISyntaxException e) {
                // do nothing
            }
        }
    }

    @Override
    public String getGoobiURIPrefix() {
        return GoobiThumbnailResource.class.getAnnotation(jakarta.ws.rs.Path.class).value();
    }

    /**
     * This needs to override the createResourceURI method of the ImageResource class which otherwise throws an error due to different number of path
     * variables The actual value of the URI is created in {@link #createResourceURI(HttpServletRequest, String, String, String)
     */
    @Override
    protected URI createResourceURI(URI baseURI, String directory, String filename) throws IllegalRequestException {
        try {
            return UriBuilder.fromUri(baseURI)
                    .path(getURIPrefix())
                    .build("", URLEncoder.encode(directory, "utf-8"), URLEncoder.encode(filename, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to create image request uri");
            throw new IllegalRequestException("Unable to evaluate request to '" + directory + "', '" + filename + "'");
        }
    }
}
