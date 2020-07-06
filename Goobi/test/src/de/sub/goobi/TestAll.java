package de.sub.goobi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.sub.goobi.helper.exceptions.ExceptionTest;

@RunWith(Suite.class)
@SuiteClasses({ OldTests.class, de.sub.goobi.config.TestAll.class, de.sub.goobi.export.dms.TestAll.class, de.sub.goobi.converter.TestAll.class,
        de.sub.goobi.forms.TestAll.class, org.goobi.api.mq.TestQueueType.class, de.sub.goobi.export.download.TestAll.class,
        de.sub.goobi.helper.TestAll.class, de.sub.goobi.helper.enums.TestAll.class, ExceptionTest.class, de.sub.goobi.helper.ldap.TestAll.class,
        de.sub.goobi.helper.servletfilter.TestAll.class, de.sub.goobi.metadaten.TestAll.class, org.goobi.api.rest.TestRestConfig.class })
public class TestAll {

}
