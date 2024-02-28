package org.goobi.api.rest.client;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentExceptionMapper;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentExceptionMapper.ErrorMessage;

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
        if(e instanceof WebApplicationException) {
            status = ((WebApplicationException) e).getResponse().getStatusInfo().toEnum();
        } else if (e instanceof NotFoundException || e instanceof FileNotFoundException) {
            status = Status.NOT_FOUND;
        } else if (e instanceof ContentLibException) {
            return new ContentExceptionMapper(request, response).toResponse((ContentLibException) e);
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

