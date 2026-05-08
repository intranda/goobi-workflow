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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.metadaten.search.ViafSearch;
import de.sub.goobi.mock.MockProcess;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
@ExtendWith(MockitoExtension.class)
public class MetadataGroupImplTest extends AbstractTest {

    private Prefs prefs;
    private Process process;

    @BeforeEach
    public void setUp() throws Exception {

        process = MockProcess.createProcess();
        prefs = process.getRegelsatz().getPreferences();

        ViafSearch viafSearch = EasyMock.createMock(ViafSearch.class);
        // TODO: /* TODO: migrate /* TODO: migrate PowerMock.expectNew */ PowerMock.expectNew */ /* TODO: migrate PowerMock.expectNew */ PowerMock.expectNew needs manual migration to Mockito.mockConstruction
        // /* TODO: migrate /* TODO: migrate PowerMock.expectNew */ PowerMock.expectNew */ /* TODO: migrate PowerMock.expectNew */ PowerMock.expectNew(ViafSearch.class).andReturn(viafSearch).anyTimes();


    }

    @Test
    public void testMetadataGroupImpl() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
            MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
            assertNotNull(fixture);
    
        }
}

    @Test
    public void testGetPersonList() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
            Person p = new Person(prefs.getMetadataTypeByName("junitPerson"));
            p.setLastname("junit");
            md.addPerson(p);

            MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);

            List<MetaPerson> personList = fixture.getPersonList();
            assertFalse(personList.isEmpty());
            MetaPerson mp = personList.get(0);
            assertEquals("junitPerson", mp.getRolle());
    
        }
}

    @Test
    public void testSetPersonList() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
            Person p = new Person(prefs.getMetadataTypeByName("junitPerson"));
            p.setLastname("junit");
            md.addPerson(p);

            MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);

            assertFalse(fixture.getPersonList().isEmpty());
            fixture.setPersonList(new ArrayList<MetaPerson>());
            assertTrue(fixture.getPersonList().isEmpty());
    
        }
}

    @Test
    public void testGetMetadataList() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
            Metadata metadata = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
            md.addMetadata(metadata);
            MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
            List<MetadatumImpl> metadataList = fixture.getMetadataList();
            assertFalse(metadataList.isEmpty());
            MetadatumImpl mp = metadataList.get(0);
            assertEquals("junitMetadata", mp.getTyp());
    
        }
}

    @Test
    public void testSetMetadataList() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
            Metadata metadata = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
            md.addMetadata(metadata);

            MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
            assertFalse(fixture.getMetadataList().isEmpty());
            fixture.setMetadataList(new ArrayList<MetadatumImpl>());
            assertTrue(fixture.getMetadataList().isEmpty());
    
        }
}

    @Test
    public void testGetMyPrefs() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
            MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
            assertEquals(prefs, fixture.getMyPrefs());
    
        }
}

    @Test
    public void testSetMyPrefs() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
            MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
            fixture.setMyPrefs(null);
            assertNull(fixture.getMyPrefs());
    
        }
}

    @Test
    public void testGetMyProcess() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
            MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
            assertEquals(process, fixture.getMyProcess());
    
        }
}

    @Test
    public void testSetMyProcess() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
            MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
            fixture.setMyProcess(null);
            assertNull(fixture.getMyProcess());
    
        }
}

    @Test
    public void testMetadataGroup() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
            MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
            fixture.setMetadataGroup(md);
            assertEquals(md, fixture.getMetadataGroup());
    
        }
}

    @Test
    public void testGetName() throws MetadataTypeNotAllowedException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");


            MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
            MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
            assertEquals("junitgrp", fixture.getName());
    
        }
}

}
