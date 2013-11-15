package de.sub.goobi.converter;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ DocketConverterTest.class, ProcessConverterTest.class, StatisticsCalculationUnitConverterTest.class,
        StatisticsResultOutputConverterTest.class, StatisticsTimeUnitConverterTest.class })
public class TestAll {

}
