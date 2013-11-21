package de.sub.goobi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({de.sub.goobi.config.TestAll.class, de.sub.goobi.converter.TestAll.class, de.sub.goobi.export.dms.TestAll.class, OldTests.class})
public class TestAll {

  

}
