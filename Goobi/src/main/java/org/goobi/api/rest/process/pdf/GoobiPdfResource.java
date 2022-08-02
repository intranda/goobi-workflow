package org.goobi.api.rest.process.pdf;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;

import de.sub.goobi.config.ConfigurationHelper;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.servlet.controller.GetAction;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.MetsPdfResource;
import de.unigoettingen.sub.commons.util.PathConverter;
import lombok.extern.log4j.Log4j2;

@javax.ws.rs.Path("/process/pdf/{processId}")
@ContentServerBinding
@Log4j2
public class GoobiPdfResource extends MetsPdfResource {

    public GoobiPdfResource(
            @Context ContainerRequestContext context, @Context HttpServletRequest request, @Context HttpServletResponse response,
            @PathParam("processId") String processId)
            throws ContentLibException {
        super(context, request, response, "pdf", getMetsFilepath(processId, GetAction.parseParameters(request, context)));
    }

    private static String getMetsFilepath(String processId, Map<String, String> parameters) throws ContentLibException {
        if (parameters.containsKey("metsFile")) {
            return extractURI(parameters, "metsFile").toString();
        } else {
            String cleanedProcessId = Paths.get(processId).getFileName().toString();
            Path path = Paths.get(ConfigurationHelper.getInstance().getMetadataFolder(), cleanedProcessId, "meta.xml");
            try {
                return getUriFromPath(path.toString()).toString();
            } catch (URISyntaxException e) {
                throw new ContentLibException(e);
            }
        }
    }

    private static URI extractURI(Map<String, String> parameters, String parameterName) throws ContentLibException {
        String parameterValue = parameters.get(parameterName);
        try {
            URI uri = getUriFromPath(parameterValue);
            return uri; 
        } catch (URISyntaxException e) {
            throw new ContentLibException("Failed to create absolute uri from " + parameterValue);
        } catch(NullPointerException e) {
            throw new ContentLibException("Could not find parameter falue for " + parameterName); 
        }
    }

    private static URI getUriFromPath(String path) throws URISyntaxException {
        URI uri = PathConverter.toURI(path);
        if(!uri.isAbsolute() && path.startsWith("/")) {
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
