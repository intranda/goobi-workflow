package de.sub.goobi.helper;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

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

        File srcFile = new File("/opt/digiverso/junit/data/plugin_JunitImportPluginError.xml");
        File destFile = folder.newFile("destination");

        NIOFileUtils.copyFile(srcFile.toPath(), destFile.toPath());
        assertTrue(destFile.exists());
    }

    @Test
    public void testCreateChecksum() throws IOException {
        File srcFile = new File("/opt/digiverso/junit/data/plugin_JunitImportPluginError.xml");
        long checksum = NIOFileUtils.createChecksum(srcFile.toPath());

        assertEquals(219427218, checksum);
    }

    
    @Test
    public void testStart() throws IOException {

        File srcFile = new File("/opt/digiverso/junit/data/plugin_JunitImportPluginError.xml");
        File destFile = folder.newFile("destination");

        NIOFileUtils.start(srcFile.toPath(), destFile.toPath());
        assertTrue(destFile.exists());
    }
    
    @Test
    public void testCopyDirectory() throws IOException {
        File srcDir = new File("/opt/digiverso/junit/data/");
        File dstDir = folder.newFolder("dest");
        
        NIOFileUtils.copyDirectory(srcDir.toPath(), dstDir.toPath());
        assertTrue(dstDir.exists());
        assertTrue(dstDir.isDirectory());
        assertTrue(dstDir.list().length > 0);
    }
    
}
