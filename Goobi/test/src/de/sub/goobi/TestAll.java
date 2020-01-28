package de.sub.goobi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ OldTests.class, de.sub.goobi.config.TestAll.class, de.sub.goobi.export.dms.TestAll.class, de.sub.goobi.converter.TestAll.class,
        de.sub.goobi.forms.TestAll.class, org.goobi.api.mq.TestQueueType.class })
public class TestAll {

    //    de.sub.goobi.forms.TestAll.class,  de.sub.goobi.export.download.TestAll.class, de.sub.goobi.helper.TestAll.class,
    //    de.sub.goobi.helper.enums.TestAll.class, ExceptionTest.class, de.sub.goobi.helper.ldap.TestAll.class,
    //    de.sub.goobi.helper.servletfilter.TestAll.class, de.sub.goobi.metadaten.TestAll.class

    //    @Before
    //    public void setUp() {
    //        String configFolder = System.getenv("junitdata");
    //        if (configFolder == null) {
    //            configFolder = "/opt/digiverso/junit/data/";
    //        }
    //        ConfigurationHelper.CONFIG_FILE_NAME = configFolder + "goobi_config.properties";
    //
    //    }
}
