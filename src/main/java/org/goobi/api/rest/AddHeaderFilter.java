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
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.configuration.ConfigurationException;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.log4j.Log4j2;

@Provider
@Log4j2
public class AddHeaderFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext resp) throws IOException {
        if (resp.getStatus() >= 400) {
            return;
        }
        String path = req.getUriInfo().getPath();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String method = req.getMethod();
        RestEndpointConfig rec = null;
        try {
            rec = RestConfig.getConfigForPath(path);
        } catch (ConfigurationException e) {
            log.error(e);
        }
        if (rec != null) {
            if (rec.getCorsMethods().contains(method)) {
                resp.getHeaders().addAll("Access-Control-Allow-Origin", rec.getCorsOrigins().toArray());
            }
            for (RestMethodConfig rmc : rec.getMethodConfigs()) {
                if (rmc.getMethod().equalsIgnoreCase(method)) {
                    for (Entry<String, List<String>> headerEntry : rmc.getCustomHeaders().entrySet()) {
                        resp.getHeaders().addAll(headerEntry.getKey(), headerEntry.getValue().toArray());
                    }
                }
            }
        }
    }

}
