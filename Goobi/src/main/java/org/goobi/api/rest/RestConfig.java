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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

import de.sub.goobi.helper.Helper;

public class RestConfig {

    private static XMLConfiguration config = null;

    public static RestEndpointConfig getConfigForPath(String path) throws ConfigurationException {
        return getConfigForPath(path, null);
    }

    public static RestEndpointConfig getConfigForPath(String path, InputStream configIn) throws ConfigurationException {
        RestEndpointConfig conf = null;
        if (config == null) {
            config = new XMLConfiguration();
            config.setDelimiterParsingDisabled(true);
            if (configIn != null) {
                config.load(configIn);
            } else {
                config.load(new Helper().getGoobiConfigDirectory() + "goobi_rest.xml");
            }
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
            config.setExpressionEngine(new XPathExpressionEngine());
        }

        SubnodeConfiguration endpoint = null;
        List<?> endpoints = config.configurationsAt("//endpoint");
        for (int i = 0; i < endpoints.size(); i++) {
            SubnodeConfiguration possibleEp = (SubnodeConfiguration) endpoints.get(i);
            String regex = possibleEp.getString("@path");
            if (Pattern.matches(regex, path)) {
                endpoint = possibleEp;
                break;
            }
        }
        if (endpoint != null) {
            conf = new RestEndpointConfig();
            conf.setJwtAuth(endpoint.getBoolean("@jwtAuth", false));
            conf.setCorsMethods(Arrays.asList(endpoint.getStringArray("cors/method")));
            conf.setCorsOrigins(Arrays.asList(endpoint.getStringArray("cors/origin")));
            List<RestMethodConfig> restMethodConfigs = new ArrayList<>();
            List<?> xmlMethodConfigs = endpoint.configurationsAt("method");
            for (int i = 0; i < xmlMethodConfigs.size(); i++) {
                SubnodeConfiguration methodC = (SubnodeConfiguration) xmlMethodConfigs.get(i);
                restMethodConfigs.add(RestMethodConfig.fromSubnodeConfig(methodC));
            }
            conf.setMethodConfigs(restMethodConfigs);
        }

        return conf;
    }
}
