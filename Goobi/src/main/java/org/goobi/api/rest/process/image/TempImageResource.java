package org.goobi.api.rest.process.image;

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

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.metadaten.Image;
import de.unigoettingen.sub.commons.contentlib.exceptions.IllegalRequestException;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerBinding;
import lombok.extern.log4j.Log4j2;

@javax.ws.rs.Path("/tmp/image/{foldername}/{filename}")
@ContentServerBinding
@Log4j2
public class TempImageResource extends AbstractImageResource {

    public TempImageResource(@Context ContainerRequestContext context, @Context HttpServletRequest request, @Context HttpServletResponse response,
            @PathParam("foldername") String foldername, @PathParam("filename") String filename) throws IllegalRequestException {
        super(context, request, response, "-", getTempFilePath(foldername, filename));
        createResourceURI(request, Image.getCleanedName(foldername), Image.getCleanedName(filename));
    }

    private static String getTempFilePath(String foldername, String filename) {
        Path path =
                Paths.get(ConfigurationHelper.getInstance().getTemporaryFolder(), Image.getCleanedName(foldername), Image.getCleanedName(filename));
        return path.toString();
    }

    @Override
    public void createResourceURI(HttpServletRequest request, String foldername, String filename) throws IllegalRequestException {

        if (request != null) {
            try {
                URI uriBase = getUriBase(request);
                resourceURI = new URI(uriBase.toString()
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
        return TempImageResource.class.getAnnotation(javax.ws.rs.Path.class).value();
    }
}
