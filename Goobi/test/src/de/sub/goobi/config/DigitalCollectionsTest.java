package de.sub.goobi.config;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
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
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.jdom2.JDOMException;
import org.junit.Before;
import org.junit.Test;

public class DigitalCollectionsTest {

    @Before
    public void setUp() throws URISyntaxException {
        String folder = System.getenv("junitdata");
        if (folder == null) {
            folder = "/opt/digiverso/junit/data/";
        }
        Path template = Paths.get(folder + "goobi_projects.xml");
        ConfigurationHelper.CONFIG_FILE_NAME =folder + "goobi_config.properties";
        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", template.getParent() + FileSystems.getDefault().getSeparator());
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

}
