package de.sub.goobi.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import javax.inject.Inject;

import org.jdom2.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;

public class HelperTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path currentFolder;

    @Inject
    private Helper helper;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        String goobiFolder = template.getParent().getParent().getParent().toString() + "/test/resources/";
        ConfigurationHelper.CONFIG_FILE_NAME = goobiFolder + "config/goobi_config.properties";
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder);
        currentFolder = temporaryFolder.newFolder("temp").toPath();
        Files.createDirectories(currentFolder);
        Path tif = Paths.get(currentFolder.toString(), "00000001.tif");
        Files.createFile(tif);
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
    public void testDeleteInDir() {
        assertEquals(1, StorageProvider.getInstance().list(currentFolder.toString()).size());
        StorageProvider.getInstance().deleteInDir(currentFolder);
        assertEquals(0, StorageProvider.getInstance().list(currentFolder.toString()).size());
    }

    @Test
    public void testDeleteDataInDir() throws IOException {
        assertEquals(1, StorageProvider.getInstance().list(currentFolder.toString()).size());
        StorageProvider.getInstance().deleteDataInDir(currentFolder);
        assertEquals(0, StorageProvider.getInstance().list(currentFolder.toString()).size());

    }

    @Test
    public void testCopyDirectoryWithCrc32Check() throws IOException {
        Path dest = temporaryFolder.newFolder("dest").toPath();
        Element element = new Element("test");
        Helper.copyDirectoryWithCrc32Check(currentFolder, dest, 10, element);
        assertTrue(Files.exists(dest));
        assertTrue(!element.getChildren().isEmpty());
    }
}
