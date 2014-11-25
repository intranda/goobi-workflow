package de.sub.goobi.config;

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
import static org.junit.Assert.*;

import java.util.List;

import org.goobi.api.display.helper.NormDatabase;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ConfigNormdataTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @After
    public void tearDown() {
        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", "/opt/digiverso/junit/data/");
    }

    @Test
    public void testConstructor() {
        ConfigNormdata norm = new ConfigNormdata();
        assertNotNull(norm);
    }
    
    @Test
    public void testDefaultNormdatabase() {
        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", "/some/wrong/path/");
        List<NormDatabase> fixture = ConfigNormdata.getConfiguredNormdatabases();
        assertEquals("gnd", fixture.get(0).getAbbreviation());
    }

    @Test
    public void testNormdatafile() {
        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", "/opt/digiverso/junit/data/");
        List<NormDatabase> fixture = ConfigNormdata.getConfiguredNormdatabases();
        assertEquals("fixture", fixture.get(0).getAbbreviation());

    }

}
