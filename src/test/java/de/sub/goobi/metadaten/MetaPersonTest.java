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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.metadaten.search.ViafSearch;
import de.sub.goobi.mock.MockProcess;
import jakarta.faces.model.SelectItem;
import ugh.dl.DocStruct;
import ugh.dl.MetadataType;
import ugh.dl.NamePart;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
@ExtendWith(MockitoExtension.class)
public class MetaPersonTest extends AbstractTest {

    private Prefs prefs;
    private Process process;
    private DocStruct docstruct;

    private static final String METADATA_TYPE = "junitPerson";

    @BeforeEach
    public void setUp() throws Exception {

        process = MockProcess.createProcess();
        prefs = process.getRegelsatz().getPreferences();
        docstruct = process.readMetadataFile().getDigitalDocument().getLogicalDocStruct();

        ViafSearch viafSearch = EasyMock.createMock(ViafSearch.class);
        // TODO: /* TODO: migrate /* TODO: migrate PowerMock.expectNew */ PowerMock.expectNew */ /* TODO: migrate PowerMock.expectNew */ PowerMock.expectNew needs manual migration to Mockito.mockConstruction
        // /* TODO: migrate /* TODO: migrate PowerMock.expectNew */ PowerMock.expectNew */ /* TODO: migrate PowerMock.expectNew */ PowerMock.expectNew(ViafSearch.class).andReturn(viafSearch).anyTimes();


    }

    @Test
    public void testMetaPerson() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
            MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
            assertNotNull(mp);
    
        }
}

    @Test
    public void testIdentifier() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
            MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
            assertNotNull(mp);
            assertEquals(0, mp.getIdentifier());
            mp.setIdentifier(1);
            assertNotEquals(0, mp.getIdentifier());
    
        }
}

    @Test
    public void testPerson() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
            MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
            assertNotNull(mp);
            mp.setP(p);
            assertSame(p, mp.getP());
    
        }
}

    @Test
    public void testFirstname() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
            MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
            assertNotNull(mp);
            assertEquals("", mp.getVorname());
            mp.setVorname("fixture");
            assertEquals("fixture", mp.getVorname());
            mp.setVorname(null);
            assertEquals("", mp.getVorname());
    
        }
}

    @Test
    public void testLastname() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
            MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
            assertNotNull(mp);
            assertEquals("", mp.getNachname());
            mp.setNachname("fixture");
            assertEquals("fixture", mp.getNachname());
            mp.setNachname(null);
            assertEquals("", mp.getNachname());
    
        }
}

    @Test
    public void testRole() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
            MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
            assertNotNull(mp);
            assertEquals(METADATA_TYPE, mp.getRolle());
            mp.setRolle(METADATA_TYPE);
            assertEquals(METADATA_TYPE, mp.getRolle());
    
        }
}

    @Test
    public void testAddableRollen() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
            MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
            assertNotNull(mp);
            List<SelectItem> fixture = mp.getAddableRollen();
            assertNotNull(fixture);
            assertEquals(6, fixture.size());
    
        }
}

    @Test
    public void testAdditionalNameParts() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
            MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
            assertNotNull(mp);
            List<NamePart> fixture = mp.getAdditionalNameParts();
            assertNull(fixture);
            mp.addNamePart();
            fixture = mp.getAdditionalNameParts();
            assertEquals(1, mp.getAdditionalNameParts().size());
            mp.setAdditionalNameParts(fixture);
            assertEquals(1, mp.getAdditionalNameParts().size());
    
        }
}

    @Test
    public void testPossibleDatabases() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
            MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
            assertNotNull(mp);
            List<String> fixture = mp.getPossibleDatabases();
            assertEquals("gnd", fixture.get(0));
    
        }
}

    @Test
    public void testPossibleNameparts() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
            MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
            assertNotNull(mp);
            List<String> fixture = mp.getPossibleNamePartTypes();
            assertEquals("date", fixture.get(0));
            assertEquals("termsOfAddress", fixture.get(1));
    
        }
}

    @Test
    public void testNormdataValue() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
            MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
            assertNotNull(mp);
            String fixture = "value";
            mp.setNormdataValue(fixture);
            assertEquals(fixture, mp.getNormdataValue());
    
        }
}

    @Test
    public void testNormDatabase() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
            MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
            assertNotNull(mp);
            String fixture = "gnd";
            mp.setNormDatabase(fixture);
            assertEquals(fixture, mp.getNormDatabase());
    
        }
}

    @Test
    public void testAdditionalParts() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
            MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
            assertNotNull(mp);
            assertTrue(mp.isShowAdditionalParts());
            p.getType().setAllowNameParts(false);
            assertFalse(mp.isShowAdditionalParts());
    
        }
}

    @Test
    public void testNormdata() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
            MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
            assertNotNull(mp);
            assertTrue(mp.isNormdata());
            p.getType().setAllowNormdata(false);
            assertFalse(mp.isNormdata());
    
        }
}

    @Test
    public void testDisplayRestrictions() throws Exception {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            MetadataType type = prefs.getMetadataTypeByName(METADATA_TYPE);
            Person p = new Person(type);
            MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);

            assertFalse(mp.isDisplayRestrictions());
            type.setAllowAccessRestriction(true);
            assertTrue(mp.isDisplayRestrictions());

    
        }
}

    @Test
    public void testRestricted() throws Exception {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            MetadataType type = prefs.getMetadataTypeByName(METADATA_TYPE);
            type.setAllowAccessRestriction(true);
            Person p = new Person(type);
            MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);

            assertFalse(mp.isRestricted());
            mp.setRestricted(true);
            assertTrue(mp.isRestricted());
    
        }
}

    @Test
    public void testAddNamePart() throws Exception {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
            MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
            mp.addNamePart();
            assertNotNull(mp.getAdditionalNameParts());
            assertEquals(1, mp.getAdditionalNameParts().size());
            assertEquals("date", mp.getAdditionalNameParts().get(0).getType());
    
        }
}
}
