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

public class FileUtilsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path currentFolder;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        String configFolder = System.getenv("junitdata");

        if (configFolder == null) {
            configFolder = "/opt/digiverso/junit/data/";
        }
        ConfigurationHelper.CONFIG_FILE_NAME = configFolder + "goobi_config.properties";
        currentFolder = folder.newFolder("temp").toPath();
        Path tif = Paths.get(currentFolder.toString(), "00000001.tif");
        Files.createFile(tif);
    }

    @Test
    public void testGetNumberOfFiles() throws IOException {

        Path notExistingFile = Paths.get("somewhere");
        long value = NIOFileUtils.getNumberOfFiles(notExistingFile);
        assertEquals(0, value);

        Path file = Paths.get(ConfigurationHelper.getInstance().getConfigurationFolder() + "plugin_JunitImportPluginError.xml");
        value = NIOFileUtils.getNumberOfFiles(file);
        assertEquals(0, value);

        value = NIOFileUtils.getNumberOfFiles(currentFolder);
        assertEquals(1, value);

    }

    @Test
    public void testGetNumberOfFilesString() throws IOException {

        String folderName = currentFolder.toString();
        int value = NIOFileUtils.getNumberOfFiles(folderName);
        assertEquals(1, value);

    }
}
