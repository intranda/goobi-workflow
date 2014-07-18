package de.sub.goobi.helper;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Observer;

import org.jdom2.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.sub.goobi.config.ConfigurationHelper;

public class HelperTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File currentFolder;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        String configFolder = System.getenv("junitdata");

        if (configFolder == null) {
            configFolder = "/opt/digiverso/junit/data/";
        }
        ConfigurationHelper.CONFIG_FILE_NAME = configFolder + "goobi_config.properties";
        currentFolder = temporaryFolder.newFolder("temp");
        currentFolder.createNewFile();
        File tif = new File(currentFolder, "00000001.tif");
        tif.createNewFile();
    }

    @Test
    public void testGetDateAsFormattedString() {
        String value = Helper.getDateAsFormattedString(null);
        assertEquals("-", value);
        Date current = new Date();

        value = Helper.getDateAsFormattedString(current);
        assertNotNull(value);

    }

    @Test
    public void testCreateObserver() {
        Helper helper = new Helper();
        Observer second = helper.createObserver();
        assertEquals(helper, second);
    }

    @Test
    public void testUpdate() {

        Helper helper = new Helper();
        helper.update(null, new Object());
        helper.update(null, "some message");
    }

    @Test
    public void testDeleteInDir() {
        assertEquals(1, currentFolder.list().length);
        Helper.deleteInDir(currentFolder);
        assertEquals(0, currentFolder.list().length);
    }

    @Test
    public void testDeleteDataInDir() throws IOException {
        assertEquals(1, currentFolder.list().length);
        Helper.deleteDataInDir(currentFolder);
        assertEquals(0, currentFolder.list().length);

    }

    @Test
    public void testCopyDirectoryWithCrc32Check() throws IOException {
        File dest = temporaryFolder.newFolder("dest");
        Element element = new Element("test");
        Helper.copyDirectoryWithCrc32Check(currentFolder, dest, 10, element);
        assertTrue(dest.exists());
        assertTrue(!element.getChildren().isEmpty());
    }
}
