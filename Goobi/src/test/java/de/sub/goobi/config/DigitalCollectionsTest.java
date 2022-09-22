package de.sub.goobi.config;

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
 */
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.jdom2.JDOMException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class DigitalCollectionsTest extends AbstractTest {

    @BeforeClass
    public static void setUp() throws URISyntaxException {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.getParent().getParent().toString() + "/");
    }

    @Test
    public void testPossibleDigitalCollectionsForProcess() throws JDOMException, IOException {
        Process p = new Process();
        Project project = new Project();
        project.setTitel("Project");
        p.setProjekt(project);
        List<String> fixture = DigitalCollections.possibleDigitalCollectionsForProcess(p);
        assertNotNull(fixture);
        assertEquals("Collection", fixture.get(0));
    }

    @Test
    public void testDefaultDigitalCollectionForProcess() throws JDOMException, IOException {
        Process p = new Process();
        Project project = new Project();
        project.setTitel("Project");
        p.setProjekt(project);
        String fixture = DigitalCollections.getDefaultDigitalCollectionForProcess(p);
        assertEquals("Collection", fixture);
    }

}
