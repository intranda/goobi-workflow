package org.goobi.api.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

import de.sub.goobi.helper.Helper;

public class RestConfig {

    private static XMLConfiguration config = null;

    public static RestEndpointConfig getConfigForPath(String path) throws ConfigurationException {
        RestEndpointConfig conf = null;
        if (config == null) {
            config = new XMLConfiguration(new Helper().getGoobiConfigDirectory() + "goobi_rest.xml");
            config.setListDelimiter('&');
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
            config.setExpressionEngine(new XPathExpressionEngine());
        }

        SubnodeConfiguration endpoint = config.configurationAt("//endpoint[@path='" + path + "']");
        if (endpoint != null) {
            conf = new RestEndpointConfig();
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
