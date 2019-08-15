package de.sub.goobi.config;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - https://goobi.io
 *          - https://www.intranda.com 
 *          - https://github.com/intranda/goobi
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
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigProjectsTest {

    @Before
    public void setUp() throws URISyntaxException {
        String folder = System.getenv("junitdata");
        if (folder == null) {
            folder = "/opt/digiverso/junit/data/";
        }
        Path template = Paths.get(folder + "goobi_projects.xml");
        ConfigurationHelper.CONFIG_FILE_NAME = folder + "goobi_config.properties";
        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", template.getParent() + FileSystems.getDefault().getSeparator());
    }

    @Test
    public void testConstructor() throws IOException {
        ConfigProjects cp = new ConfigProjects("test");
        assertNotNull(cp);

        cp = new ConfigProjects("default");
        assertNotNull(cp);
    }

    @Test
    public void testParamString() throws IOException {
        ConfigProjects cp = new ConfigProjects("default");
        String fixture = cp.getParamString("createNewProcess.opac.catalogue");
        assertNotNull(fixture);
        assertEquals("LOC", fixture);
    }

    @Test
    public void testParamStringWithDefault() throws IOException {
        ConfigProjects cp = new ConfigProjects("default");
        String fixture = cp.getParamString("createNewProcess.opac.catalogue", "GBV");
        assertNotNull(fixture);
        assertEquals("LOC", fixture);

        fixture = cp.getParamString("wrongParameter", "GBV");
        assertNotNull(fixture);
        assertEquals("GBV", fixture);

    }

    @Test
    public void testParamBoolean() throws IOException {
        ConfigProjects cp = new ConfigProjects("default");
        boolean fixture = cp.getParamBoolean("createNewProcess.opac[@use]");
        assertTrue(fixture);

    }

    @Test
    public void testParamLong() throws IOException {
        ConfigProjects cp = new ConfigProjects("default");
        long fixture = cp.getParamLong("number");
        assertEquals(10, fixture);
    }

    @Test
    public void testParamList() throws IOException {
        ConfigProjects cp = new ConfigProjects("default");
        List<String> fixture = cp.getParamList("validate.metadata");
        assertNotNull(fixture);
        assertEquals(1, fixture.size());

        fixture = cp.getParamList("wrongParameter");
        assertNotNull(fixture);
        assertEquals(0, fixture.size());
    }

}
