package de.sub.goobi.config;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConfigMainTest {

    @Test
    public void testGetTempImagesPath() {
        String fixture = ConfigMain.getTempImagesPath();
        assertNotNull(fixture);
        assertEquals("/imagesTemp/", fixture);
    }

    @Test
    public void testGetTempImagesPathAsCompleteDirectory() {
        ConfigMain.setImagesPath("test");
        String fixture = ConfigMain.getTempImagesPathAsCompleteDirectory();
        assertEquals("test", fixture);
    }

    @Test
    public void testSetImagesPath() {
        ConfigMain.setImagesPath("test");
        String fixture = ConfigMain.getTempImagesPathAsCompleteDirectory();
        assertEquals("test", fixture);
    }
    

    @Test
    public void testGetParameterString() {
        ConfigMain.setParameter("KonfigurationVerzeichnis", "/opt/digiverso/goobi/config/");
        String fixture = ConfigMain.getParameter("KonfigurationVerzeichnis");
        assertNotNull(fixture);
        assertEquals("/opt/digiverso/goobi/config/", fixture);
        
        fixture = ConfigMain.getParameter("wrongParameter");
        assertNull(fixture);

    }
    

    @Test
    public void testGetParameterStringString() {
        String fixture = ConfigMain.getParameter("MetadatenVerzeichnis", "different default");
        assertNotNull(fixture);
        assertEquals("/opt/digiverso/goobi/metadata/", fixture);

        fixture = ConfigMain.getParameter("wrongParameter", "intranda");
        assertNotNull(fixture);
        assertEquals("intranda", fixture);

    }

    @Test
    public void testGetBooleanParameterString() {
        boolean fixture = ConfigMain.getBooleanParameter("automaticExportWithOcr");
        assertTrue(fixture);
    }

    @Test
    public void testGetBooleanWithWrongParameterString() {
        boolean fixture = ConfigMain.getBooleanParameter("wrongParameter");
        assertFalse(fixture);
    }

    @Test
    public void testGetBooleanParameterStringBoolean() {
        boolean fixture = ConfigMain.getBooleanParameter("automaticExportWithOcr", false);
        assertTrue(fixture);

        fixture = ConfigMain.getBooleanParameter("wrongParameter", true);
        assertTrue(fixture);
    }

    @Test
    public void testGetLongParameter() {
        long fixture = ConfigMain.getLongParameter("MetsEditorLockingTime", 30 * 60 * 1000);
        assertEquals(1800000l, fixture);
    }

    @Test
    public void testGetIntParameterString() {
        int fixture = ConfigMain.getIntParameter("numberOfMetaBackups");
        assertEquals(9, fixture);
        fixture = ConfigMain.getIntParameter("wrongParameter");
        assertEquals(0, fixture);
    }

    @Test
    public void testGetIntParameterStringInt() {
        int fixture = ConfigMain.getIntParameter("numberOfMetaBackups", 9);
        assertEquals(9, fixture);
        fixture = ConfigMain.getIntParameter("wrongParameter", 9);
        assertEquals(9, fixture);

    }

}
