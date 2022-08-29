package org.goobi.api.rest.process.thumbnail;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;

import org.goobi.api.rest.process.image.AbstractImageResource;

import de.sub.goobi.config.ConfigurationHelper;
import de.unigoettingen.sub.commons.contentlib.exceptions.IllegalRequestException;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerBinding;
import de.unigoettingen.sub.commons.util.PathConverter;
import lombok.extern.log4j.Log4j2;

@javax.ws.rs.Path("process/thumbs/{processId}/{foldername}/{filename}")
@ContentServerBinding
@Log4j2
public class GoobiThumbnailResource extends AbstractImageResource {

    private static final Path metadataFolderPath = Paths.get(ConfigurationHelper.getInstance().getMetadataFolder());

    public GoobiThumbnailResource(@Context ContainerRequestContext context, @Context HttpServletRequest request,
            @Context HttpServletResponse response, @PathParam("processId") String processId, @PathParam("foldername") String foldername,
            @PathParam("filename") String filename) throws IllegalRequestException {
        super(context, request, response, "-", filename);
        createResourceURI(request, processId, foldername, filename);
        createImageURI(processId, foldername, filename);
    }

    public void createImageURI(String processId, String foldername, String filename) {
        Path imagePath = metadataFolderPath.resolve(processId).resolve("thumbs").resolve(foldername).resolve(filename);
        try {
            this.imageURI = getUriFromPath(imagePath.toString());
        } catch (URISyntaxException e) {
            this.imageURI = imagePath.toUri();
        }
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
        return GoobiThumbnailResource.class.getAnnotation(javax.ws.rs.Path.class).value();
    }

    private static URI getUriFromPath(String path) throws URISyntaxException {
        URI uri = PathConverter.toURI(path);
        if (!uri.isAbsolute() && path.startsWith("/")) {
            String uriString = "file://" + path;
            try {
                uri = PathConverter.toURI(uriString);
            } catch (URISyntaxException e) {
                log.error("Failed to create absolute uri from " + path);
            }
        }
        return uri;
    }

}
