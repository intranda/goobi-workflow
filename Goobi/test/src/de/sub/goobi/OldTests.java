package de.sub.goobi;

import org.goobi.io.BackupFileRotationTest;
import org.goobi.production.GoobiVersionTest;
import org.goobi.production.flow.statistics.enums.CalculationUnitTest;
import org.goobi.production.flow.statistics.enums.ResultOutputTest;
import org.goobi.production.flow.statistics.enums.StatisticsModeTest;
import org.goobi.production.flow.statistics.enums.TimeUnitTest;
import org.goobi.production.flow.statistics.hibernate.ConverterTest;
import org.goobi.production.flow.statistics.hibernate.SQLHelperTest;
import org.goobi.production.flow.statistics.hibernate.SQLProductionTest;
import org.goobi.production.flow.statistics.hibernate.SQLStepRequestsTest;
import org.goobi.production.flow.statistics.hibernate.SQLStorageTest;
import org.goobi.webapi.beans.IdentifierPPNTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.sub.goobi.helper.FilesystemHelperTest;
import de.sub.goobi.helper.archive.ProcessSwapOutTaskTest;
import de.sub.goobi.helper.encryption.DesEncrypterTest;
import de.sub.goobi.helper.importer.ImportOpacTest;
import de.sub.goobi.metadaten.PaginatorTest;
import de.sub.goobi.samples.BenutzerTest;
import de.sub.goobi.samples.BenutzergruppenTest;

@RunWith(Suite.class)
@SuiteClasses({ FilesystemHelperTest.class, ProcessSwapOutTaskTest.class, DesEncrypterTest.class, ImportOpacTest.class, PaginatorTest.class,
        BenutzergruppenTest.class, BenutzerTest.class, BackupFileRotationTest.class, GoobiVersionTest.class, CalculationUnitTest.class,
        ResultOutputTest.class, StatisticsModeTest.class, TimeUnitTest.class, ConverterTest.class, SQLHelperTest.class, SQLProductionTest.class,
        SQLStepRequestsTest.class, SQLStorageTest.class, IdentifierPPNTest.class })
public class OldTests {

}
