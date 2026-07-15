/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.api.rest;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Path("/messages")
public class MessagesResource {

    @GET
    @Path("/{language}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get the message bundle for a language",
            description = "Returns the translated message bundle (default bundle plus optional local overrides) for the given language code. "
                    + "An unknown or invalid language code yields an empty map.")
    @ApiResponse(responseCode = "200", description = "OK")
    @Tag(name = "messages")
    public Map<String, String> getBundleForLanguage(@PathParam("language") String language) {
        if (!language.matches("[a-zA-Z]{2,8}")) {
            return new HashMap<>();
        }
        Locale locale = Locale.forLanguageTag(language);
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        Map<String, String> bundleMap = new HashMap<>();
        for (Enumeration<String> keys = bundle.getKeys(); keys.hasMoreElements();) {
            String key = keys.nextElement();
            bundleMap.put(key, bundle.getString(key));
        }
        java.nio.file.Path file = Paths.get(ConfigurationHelper.getInstance().getPathForLocalMessages());
        if (StorageProvider.getInstance().isFileExists(file)) {
            // Load local message bundle from file system only if file exists;
            // if value not exists in bundle, use default bundle from classpath

            try {
                final URL resourceURL = file.toUri().toURL();
                URLClassLoader urlLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                    @Override
                    public URLClassLoader run() {
                        return new URLClassLoader(new URL[] { resourceURL });
                    }
                });
                ResourceBundle localBundle = ResourceBundle.getBundle("messages", locale, urlLoader);
                if (localBundle != null) {
                    for (Enumeration<String> keys = localBundle.getKeys(); keys.hasMoreElements();) {
                        String key = keys.nextElement();
                        bundleMap.put(key, localBundle.getString(key));
                    }
                }

            } catch (Exception e) {
                log.error(e);
            }
        }
        return bundleMap;
    }
}
