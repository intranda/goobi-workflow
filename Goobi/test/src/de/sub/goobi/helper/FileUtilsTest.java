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

public class FileUtilsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    private File currentFolder;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        String configFolder = System.getenv("junitdata");

        if (configFolder == null) {
            configFolder = "/opt/digiverso/junit/data/";
        }
        ConfigurationHelper.CONFIG_FILE_NAME = configFolder + "goobi_config.properties";
        currentFolder = folder.newFolder("temp");
        File tif = new File(currentFolder, "00000001.tif");
        tif.createNewFile();
    }

    @Test
    public void testGetNumberOfFiles() throws IOException {

        File notExistingFile = new File("somewhere");
        long value = FileUtils.getNumberOfFiles(notExistingFile);
        assertEquals(0, value);

        File file = new File(ConfigurationHelper.getInstance().getConfigurationFolder() + "plugin_JunitImportPluginError.xml");
        value = FileUtils.getNumberOfFiles(file);
        assertEquals(0, value);

        value = FileUtils.getNumberOfFiles(currentFolder);
        assertEquals(1, value);

    }

    @Test
    public void testGetNumberOfFilesString() throws IOException {
        
        String folderName = currentFolder.getAbsolutePath();
        int value = FileUtils.getNumberOfFiles(folderName);
        assertEquals(1, value);
        
    }
}
