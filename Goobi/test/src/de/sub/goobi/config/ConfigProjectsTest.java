package de.sub.goobi.config;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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
        File template = new File(folder + "goobi_projects.xml");
        ConfigurationHelper.CONFIG_FILE_NAME =folder + "goobi_config.properties";
        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", template.getParent() + File.separator);
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
