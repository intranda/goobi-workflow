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
import lombok.extern.log4j.Log4j2;

@Log4j2
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
