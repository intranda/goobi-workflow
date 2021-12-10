package de.sub.goobi;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.BeforeClass;

public class AbstractTest {

    @BeforeClass
    public static void setUpClass() throws Exception {

        String log4jFile = "test/src/log4j2.xml"; // for junit tests in eclipse

        if (!Files.exists(Paths.get(log4jFile))) {
            log4jFile = "target/test-classes/log4j2.xml"; // to run mvn test from cli or in jenkins
        }

        System.setProperty("log4j.configurationFile", log4jFile);
    }
}
