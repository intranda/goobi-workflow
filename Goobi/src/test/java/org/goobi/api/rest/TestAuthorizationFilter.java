package org.goobi.api.rest;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
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
        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.getParent().getParent().toString()+ "/");
        ConfigurationHelper.CONFIG_FILE_NAME = goobiFolder.toString();
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
