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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package de.sub.goobi.metadaten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.goobi.api.display.enums.DisplayType;
import org.goobi.beans.Process;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.mock.MockProcess;
import ugh.dl.Corporate;
import ugh.dl.DocStruct;
import ugh.dl.NamePart;
import ugh.dl.Prefs;

@ExtendWith(MockitoExtension.class)
public class MetaCorporateTest extends AbstractTest {

    private Prefs prefs;
    private Process process;
    private static final String METADATA_TYPE = "junitCorporate";
    private Corporate c;

    @BeforeEach
    public void setUp() throws Exception {
        process = MockProcess.createProcess();
        prefs = process.getRegelsatz().getPreferences();
        c = new Corporate(prefs.getMetadataTypeByName(METADATA_TYPE));

    }

    @Test
    public void testConstructor() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            MetaCorporate fixture = new MetaCorporate(c, prefs, null, null);
            assertNotNull(fixture);

        }
    }

    @Test
    public void testDisplaytype() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            MetaCorporate fixture = new MetaCorporate(c, prefs, null, null);
            assertEquals(DisplayType.corporate, fixture.getMetadataDisplaytype());

        }
    }

    @Test
    public void testRole() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            MetaCorporate fixture = new MetaCorporate(c, prefs, null, null);
            fixture.setRole(METADATA_TYPE);
            assertEquals(METADATA_TYPE, fixture.getRole());

        }
    }

    @Test
    public void testAddableRoles() throws Exception {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            DocStruct ds = process.readMetadataFile().getDigitalDocument().getLogicalDocStruct();
            ds.addCorporate(c);
            MetaCorporate fixture = new MetaCorporate(c, prefs, ds, null);

            assertEquals(1, fixture.getAddableRoles().size());
            assertEquals(METADATA_TYPE, fixture.getAddableRoles().get(0).getValue());

        }
    }

    @Test
    public void testMainName() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            MetaCorporate fixture = new MetaCorporate(c, prefs, null, null);
            fixture.setMainName("main name");
            assertEquals("main name", fixture.getMainName());

        }
    }

    @Test
    public void testPartName() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            MetaCorporate fixture = new MetaCorporate(c, prefs, null, null);
            fixture.setPartName("part name");
            assertEquals("part name", fixture.getPartName());

        }
    }

    @Test
    public void testSubNames() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            MetaCorporate fixture = new MetaCorporate(c, prefs, null, null);
            assertEquals(1, fixture.getSubNames().size());
            fixture.getSubNames().get(0).setValue("val");
            fixture.addSubName();
            assertEquals(2, fixture.getSubNameSize());
            NamePart np = fixture.getSubNames().get(1);
            fixture.removeSubName(np);
            assertEquals(1, fixture.getSubNameSize());

        }
    }

    @Test
    public void testGetPossibleDatabases() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            MetaCorporate fixture = new MetaCorporate(c, prefs, null, null);
            assertEquals("gnd", fixture.getPossibleDatabases().get(0));

        }
    }

    @Test
    public void testIsNormdata() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            MetaCorporate fixture = new MetaCorporate(c, prefs, null, null);
            assertTrue(fixture.isNormdata());

        }
    }

    @Test
    public void testNormdataValue() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            MetaCorporate fixture = new MetaCorporate(c, prefs, null, null);
            fixture.setNormdataValue("test");
            assertEquals("test", fixture.getNormdataValue());

        }
    }

    @Test
    public void testNormDatabase() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            MetaCorporate fixture = new MetaCorporate(c, prefs, null, null);
            fixture.setNormDatabase("gnd");
            assertEquals("gnd", fixture.getNormDatabase());

        }
    }

    @Test
    public void testDisplayRestrictions() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            MetaCorporate fixture = new MetaCorporate(c, prefs, null, null);

            assertFalse(fixture.isDisplayRestrictions());
            c.getType().setAllowAccessRestriction(true);
            assertTrue(fixture.isDisplayRestrictions());

        }
    }

    @Test
    public void testRestricted() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            MetaCorporate fixture = new MetaCorporate(c, prefs, null, null);
            c.getType().setAllowAccessRestriction(true);

            assertFalse(fixture.isRestricted());
            fixture.setRestricted(true);
            assertTrue(fixture.isRestricted());

        }
    }

    //    search
    //    getData
    //    isShowNoHitFound
}
