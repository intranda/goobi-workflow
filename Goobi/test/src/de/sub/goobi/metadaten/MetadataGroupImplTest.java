package de.sub.goobi.metadaten;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.sub.goobi.forms.NavigationForm.Theme;
import de.sub.goobi.mock.MockProcess;
import ugh.dl.MetadataGroup;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;

public class MetadataGroupImplTest {

    private Prefs prefs;
    private Process process;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {

        process = MockProcess.createProcess(folder);
        prefs = process.getRegelsatz().getPreferences();
    }

    @Test
    public void testMetadataGroupImpl() throws MetadataTypeNotAllowedException {

        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, Theme.ui, null);
        assertNotNull(fixture);
    }

    @Test
    public void testGetPersonList() throws MetadataTypeNotAllowedException {
        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, Theme.ui, null);
        List<MetaPerson> personList = fixture.getPersonList();
        assertFalse(personList.isEmpty());
        MetaPerson mp = personList.get(0);
        assertEquals("junitPerson", mp.getRolle());
    }

    @Test
    public void testSetPersonList() throws MetadataTypeNotAllowedException {
        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, Theme.ui, null);
        assertFalse(fixture.getPersonList().isEmpty());
        fixture.setPersonList(new ArrayList<MetaPerson>());
        assertTrue(fixture.getPersonList().isEmpty());
    }

    @Test
    public void testGetMetadataList() throws MetadataTypeNotAllowedException {
        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, Theme.ui, null);
        List<MetadatumImpl> metadataList = fixture.getMetadataList();
        assertFalse(metadataList.isEmpty());
        MetadatumImpl mp = metadataList.get(0);
        assertEquals("junitMetadata", mp.getTyp());
    }

    @Test
    public void testSetMetadataList() throws MetadataTypeNotAllowedException {
        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, Theme.ui, null);
        assertFalse(fixture.getMetadataList().isEmpty());
        fixture.setMetadataList(new ArrayList<MetadatumImpl>());
        assertTrue(fixture.getMetadataList().isEmpty());
    }

    @Test
    public void testGetMyPrefs() throws MetadataTypeNotAllowedException {
        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, Theme.ui, null);
        assertEquals(prefs, fixture.getMyPrefs());
    }

        @Test
        public void testSetMyPrefs() throws MetadataTypeNotAllowedException {
            MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
            MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, Theme.ui, null);
            fixture.setMyPrefs(null);
            assertNull(fixture.getMyPrefs());
        }
    
        @Test
        public void testGetMyProcess() throws MetadataTypeNotAllowedException {
            MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
            MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, Theme.ui, null);
            assertEquals(process,  fixture.getMyProcess());
        }
    
        @Test
        public void testSetMyProcess() throws MetadataTypeNotAllowedException {
            MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
            MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, Theme.ui, null);
            fixture.setMyProcess(null);
            assertNull(fixture.getMyProcess());
        }
    
        @Test
        public void testMetadataGroup() throws MetadataTypeNotAllowedException {
            MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
            MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, Theme.ui, null);
            fixture.setMetadataGroup(md);
            assertEquals(md, fixture.getMetadataGroup());
        }
    

    
        @Test
        public void testGetName() throws MetadataTypeNotAllowedException {
            MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
            MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, Theme.ui, null);
            assertEquals("junitgrp", fixture.getName());
        }

}
