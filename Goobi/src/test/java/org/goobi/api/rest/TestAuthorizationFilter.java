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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.naming.ConfigurationException;

import org.junit.Before;
import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.JwtHelper;

public class TestAuthorizationFilter extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        String goobiMainFolder = goobiFolder.getParent().getParent().toString() + "/";
        ConfigurationHelper.CONFIG_FILE_NAME = goobiFolder.toString();
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiMainFolder);
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

    @Test
    public void testCheckPermissions() {
        assertTrue(Files.exists(Paths.get(ConfigurationHelper.getInstance().getConfigurationFolder(), "goobi_rest.xml")));

        // access possible without token
        assertTrue(AuthorizationFilter.checkPermissions("127.0.0.1", null, "/process/image/123", "get"));
        // access possible with correct token
        assertTrue(AuthorizationFilter.checkPermissions("127.0.0.1", "token", "/closestep/123", "post"));
        // access not possible without token
        assertFalse(AuthorizationFilter.checkPermissions("127.0.0.1", "", "/closestep/123", "post"));
        assertFalse(AuthorizationFilter.checkPermissions("127.0.0.1", null, "/closestep/123", "post"));
        // access not possible with wrong token
        assertFalse(AuthorizationFilter.checkPermissions("127.0.0.1", "wrong", "/closestep/123", "post"));
        // access not possible with wrong method
        assertFalse(AuthorizationFilter.checkPermissions("127.0.0.1", "token", "/closestep/123", "get"));
        // access not possible with wrong ip
        assertFalse(AuthorizationFilter.checkPermissions("192.168.0.1", "token", "/closestep/123", "post"));
        // access not possible with wrong path
        assertFalse(AuthorizationFilter.checkPermissions("127.0.0.1", "token", "/wrong", "post"));
    }
}
