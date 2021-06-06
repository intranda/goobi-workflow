package org.goobi.api.rest;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.naming.ConfigurationException;

import org.junit.Before;
import org.junit.Test;

import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.JwtHelper;

public class TestAuthorizationFilter {

    @Before
    public void setUp() throws Exception {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        String goobiFolder = template.getParent().getParent().getParent().toString() + "/test/resources/";
        ConfigurationHelper.CONFIG_FILE_NAME = goobiFolder + "config/goobi_config.properties";
        ConfigurationHelper.resetConfigurationFile();
    }

    @Test
    public void testCheckJwt() throws ConfigurationException {
        String jwt = JwtHelper.createApiToken("/processes/\\d+", new String[] { "GET" });

        assertTrue(AuthorizationFilter.checkJwt(jwt, "/processes/136", "GET"));

        assertTrue(!AuthorizationFilter.checkJwt(jwt, "/processes/136", "POST"));
        assertTrue(!AuthorizationFilter.checkJwt(jwt, "/processes/bla", "GET"));
        assertTrue(!AuthorizationFilter.checkJwt(jwt, "/process/136", "GET"));
        assertTrue(!AuthorizationFilter.checkJwt(jwt, "/processes/136/log", "GET"));
    }
}
