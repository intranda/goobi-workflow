package de.sub.goobi.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;

public class CopyFileTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException, URISyntaxException {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        String goobiFolder = template.getParent().getParent().getParent().toString() + "/test/resources/";
        ConfigurationHelper.CONFIG_FILE_NAME = goobiFolder + "config/goobi_config.properties";
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder);
    }

    @Test
    public void testCopyFile() throws IOException {

        Path srcFile = Paths.get(ConfigurationHelper.getInstance().getConfigurationFolder() + "/plugin_JunitImportPluginError.xml");
        Path destFile = folder.newFile("destination").toPath();

        StorageProvider.getInstance().copyFile(srcFile, destFile);
        assertTrue(Files.exists(destFile));
    }

    @Test
    public void testCreateChecksum() throws IOException {
        Path srcFile = Paths.get(ConfigurationHelper.getInstance().getConfigurationFolder() + "plugin_JunitImportPluginError.xml");
        long checksum = StorageProvider.getInstance().createChecksum(srcFile);

        assertEquals(2827966374l, checksum);
    }

    @Test
    public void testStart() throws IOException {

        Path srcFile = Paths.get(ConfigurationHelper.getInstance().getConfigurationFolder() + "plugin_JunitImportPluginError.xml");
        Path destFile = folder.newFile("destination").toPath();

        StorageProvider.getInstance().start(srcFile, destFile);
        assertTrue(Files.exists(destFile));
    }

    @Test
    public void testCopyDirectory() throws IOException {
        Path srcDir =  Paths.get(ConfigurationHelper.getInstance().getConfigurationFolder());
        Path dstDir = folder.newFolder("dest").toPath();

        StorageProvider.getInstance().copyDirectory(srcDir, dstDir);
        assertTrue(Files.exists(dstDir));
        assertTrue(Files.isDirectory(dstDir));
        assertTrue(!StorageProvider.getInstance().list(dstDir.toString()).isEmpty());
    }

}
