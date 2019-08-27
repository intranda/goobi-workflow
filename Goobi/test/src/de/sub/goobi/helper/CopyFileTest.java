package de.sub.goobi.helper;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.sub.goobi.config.ConfigurationHelper;

public class CopyFileTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException, URISyntaxException {
        String configFolder = System.getenv("junitdata");

        if (configFolder == null) {
            configFolder = "/opt/digiverso/junit/data/";
        }
        ConfigurationHelper.CONFIG_FILE_NAME = configFolder + "goobi_config.properties";
    }

    @Test
    public void testCopyFile() throws IOException {

        Path srcFile = Paths.get("/opt/digiverso/junit/data/plugin_JunitImportPluginError.xml");
        Path destFile = folder.newFile("destination").toPath();

        StorageProvider.getInstance().copyFile(srcFile, destFile);
        assertTrue(Files.exists(destFile));
    }

    @Test
    public void testCreateChecksum() throws IOException {
        Path srcFile = Paths.get("/opt/digiverso/junit/data/plugin_JunitImportPluginError.xml");
        long checksum = StorageProvider.getInstance().createChecksum(srcFile);

        assertEquals(219427218, checksum);
    }

    @Test
    public void testStart() throws IOException {

        Path srcFile = Paths.get("/opt/digiverso/junit/data/plugin_JunitImportPluginError.xml");
        Path destFile = folder.newFile("destination").toPath();

        StorageProvider.getInstance().start(srcFile, destFile);
        assertTrue(Files.exists(destFile));
    }

    @Test
    public void testCopyDirectory() throws IOException {
        Path srcDir = Paths.get("/opt/digiverso/junit/data/");
        Path dstDir = folder.newFolder("dest").toPath();

        StorageProvider.getInstance().copyDirectory(srcDir, dstDir);
        assertTrue(Files.exists(dstDir));
        assertTrue(Files.isDirectory(dstDir));
        assertTrue(!StorageProvider.getInstance().list(dstDir.toString()).isEmpty());
    }

}
