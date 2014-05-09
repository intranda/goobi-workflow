package de.sub.goobi;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.sub.goobi.config.ConfigurationHelper;

@RunWith(Suite.class)
@SuiteClasses({ de.sub.goobi.config.TestAll.class, de.sub.goobi.converter.TestAll.class, de.sub.goobi.export.dms.TestAll.class,
        de.sub.goobi.forms.TestAll.class, OldTests.class, de.sub.goobi.export.download.TestAll.class })
public class TestAll {

    
    @Before
    public void setUp() {
        String configFolder = System.getenv("junitdata");;
        if (configFolder == null) {
            configFolder = "/opt/digiverso/junit/data/";
        }
        ConfigurationHelper.CONFIG_FILE_NAME =configFolder + "goobi_config.properties";
        
    }
}
