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
package org.goobi.api.rest.process.pdf;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import de.sub.goobi.config.ConfigurationHelper;
import de.unigoettingen.sub.commons.cache.ContentServerCacheManager;
import de.unigoettingen.sub.commons.cache.NoCacheConfiguration;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.servlet.controller.GetAction;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.MetsPdfResource;
import de.unigoettingen.sub.commons.util.PathConverter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import lombok.extern.log4j.Log4j2;

@jakarta.ws.rs.Path("/process/pdf/{processId}")
@ContentServerBinding
@Log4j2
public class GoobiPdfResource extends MetsPdfResource {

    public GoobiPdfResource(
            @Context ContainerRequestContext context, @Context HttpServletRequest request, @Context HttpServletResponse response,
            @PathParam("processId") String processId)
            throws ContentLibException {
        super(context, request, response, "pdf", getMetsFilepath(processId, GetAction.parseParameters(request, context)),
                new ContentServerCacheManager(new NoCacheConfiguration()));
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
            return getUriFromPath(parameterValue);
        } catch (URISyntaxException e) {
            throw new ContentLibException("Failed to create absolute uri from " + parameterValue);
        } catch (NullPointerException e) {
            throw new ContentLibException("Could not find parameter falue for " + parameterName);
        }
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
