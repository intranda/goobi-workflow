package de.sub.goobi.config;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

public class DigitalCollectionsTest {

    @Before
    public void setUp() throws URISyntaxException {
        String folder = System.getenv("junitdata");
        File template = new File(folder + "goobi_projects.xml");
        ConfigMain.setParameter("KonfigurationVerzeichnis", template.getParent() + File.separator);
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
