package de.sub.goobi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;

@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*"})

public class AbstractTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        // setup log4j configuration
        String log4jFile = "src/test/resources/log4j2.test.xml"; // for junit tests in eclipse

        if (!Files.exists(Paths.get(log4jFile))) {
            log4jFile = "target/test-classes/log4j2.test.xml"; // to run mvn test from cli or in jenkins
        }

        System.setProperty("log4j.configurationFile", log4jFile);

        // use separate configuration files for tests
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        String goobiMainFolder =goobiFolder.getParent().getParent().toString();
        ConfigurationHelper.CONFIG_FILE_NAME = goobiFolder.toString();
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiMainFolder + "/");
    }
}
