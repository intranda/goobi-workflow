package org.goobi.api.rest.client;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentExceptionMapper;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentExceptionMapper.ErrorMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Catches general exceptions encountered during rest-api calls and creates an error response
 *
 * @author Florian Alpers
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger logger = LogManager.getLogger(WebApplicationExceptionMapper.class);

    @Context
    HttpServletResponse response;
    @Context
    HttpServletRequest request;

    /** {@inheritDoc} */
    @Override
    public Response toResponse(WebApplicationException eParent) {
        Response.Status status;
        boolean printStackTrace = false;
        Throwable e = eParent.getCause();
        if (e == null) {
            e = eParent;
        }
        if (e instanceof WebApplicationException wae) {
            status = wae.getResponse().getStatusInfo().toEnum();
        } else if (e instanceof NotFoundException || e instanceof FileNotFoundException) {
            status = Status.NOT_FOUND;
        } else if (e instanceof ContentLibException cle) {
            return new ContentExceptionMapper(request, response).toResponse(cle);
        } else if (e instanceof TimeoutException) {
            status = Status.INTERNAL_SERVER_ERROR;
        } else if (e instanceof RuntimeException) {
            status = Status.INTERNAL_SERVER_ERROR;
            logger.error("Error on request {};\t ERROR MESSAGE: {} (method: {})", request.getRequestURI(), e.getMessage(), request.getMethod());
        } else {
            //unknown error. Probably request error
            status = Status.BAD_REQUEST;
            printStackTrace = true;
            logger.error(e.getMessage(), e);
        }

        String mediaType = MediaType.APPLICATION_JSON;
        return Response.status(status).type(mediaType).entity(new ErrorMessage(status, e, printStackTrace)).build();
    }

    /**
     * <p>
     * getRequestHeaders.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRequestHeaders() {
        return Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(headerName -> headerName, headerName -> request.getHeader(headerName)))
                .entrySet()
                .stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));
    }

}
