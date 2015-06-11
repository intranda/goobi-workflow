package de.sub.goobi;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */
import org.goobi.io.BackupFileRotationTest;
import org.goobi.pagination.IntegerSequenceTest;
import org.goobi.pagination.RomanNumberSequenceTest;
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
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.sub.goobi.helper.archive.ProcessSwapOutTaskTest;
import de.sub.goobi.helper.encryption.DesEncrypterTest;
import de.sub.goobi.helper.importer.ImportOpacTest;
import de.sub.goobi.samples.BenutzerTest;
import de.sub.goobi.samples.BenutzergruppenTest;

@RunWith(Suite.class)
@SuiteClasses({ ProcessSwapOutTaskTest.class, DesEncrypterTest.class, ImportOpacTest.class, BenutzergruppenTest.class, BenutzerTest.class,
        BackupFileRotationTest.class, GoobiVersionTest.class, CalculationUnitTest.class, ResultOutputTest.class, StatisticsModeTest.class,
        TimeUnitTest.class, ConverterTest.class, SQLHelperTest.class, SQLProductionTest.class, SQLStepRequestsTest.class, SQLStorageTest.class,
         IntegerSequenceTest.class, RomanNumberSequenceTest.class })
public class OldTests {

}
