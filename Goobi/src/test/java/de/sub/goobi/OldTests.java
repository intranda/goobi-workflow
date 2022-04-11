package de.sub.goobi;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - https://goobi.io
 *          - https://www.intranda.com 
 *          - https://github.com/intranda/goobi-workflow
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
import org.goobi.pagination.IntegerSequenceTest;
import org.goobi.pagination.RomanNumberSequenceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.sub.goobi.helper.archive.ProcessSwapOutTaskTest;
import de.sub.goobi.helper.encryption.DesEncrypterTest;
import de.sub.goobi.helper.importer.ImportOpacTest;

@RunWith(Suite.class)
@SuiteClasses({ ProcessSwapOutTaskTest.class, DesEncrypterTest.class, ImportOpacTest.class, IntegerSequenceTest.class,
        RomanNumberSequenceTest.class })
public class OldTests {

}
