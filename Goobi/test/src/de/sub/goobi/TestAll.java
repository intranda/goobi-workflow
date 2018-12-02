package de.sub.goobi;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - https://goobi.io
 *          - https://www.intranda.com 
 *          - https://github.com/intranda/goobi
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
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.exceptions.ExceptionTest;

@RunWith(Suite.class)
@SuiteClasses({ de.sub.goobi.config.TestAll.class, de.sub.goobi.converter.TestAll.class, de.sub.goobi.export.dms.TestAll.class,
        de.sub.goobi.forms.TestAll.class, OldTests.class, de.sub.goobi.export.download.TestAll.class, de.sub.goobi.helper.TestAll.class,
        de.sub.goobi.helper.enums.TestAll.class, ExceptionTest.class, de.sub.goobi.helper.ldap.TestAll.class,
        de.sub.goobi.helper.servletfilter.TestAll.class, de.sub.goobi.metadaten.TestAll.class })
public class TestAll {

    @Before
    public void setUp() {
        String configFolder = System.getenv("junitdata");
        if (configFolder == null) {
            configFolder = "/opt/digiverso/junit/data/";
        }
        ConfigurationHelper.CONFIG_FILE_NAME = configFolder + "goobi_config.properties";

    }
}
