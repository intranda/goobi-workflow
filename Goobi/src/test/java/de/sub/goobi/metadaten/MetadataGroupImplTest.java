package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.metadaten.search.ViafSearch;
import de.sub.goobi.mock.MockProcess;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ViafSearch.class, Helper.class})
public class MetadataGroupImplTest extends AbstractTest {

    private Prefs prefs;
    private Process process;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {

        process = MockProcess.createProcess();
        prefs = process.getRegelsatz().getPreferences();

        ViafSearch viafSearch = PowerMock.createMock(ViafSearch.class);
        PowerMock.expectNew(ViafSearch.class).andReturn(viafSearch).anyTimes();
        PowerMock.replay(viafSearch);

        PowerMock.mockStatic(Helper.class);
        EasyMock.expect(Helper.getLoginBean()).andReturn(null).anyTimes();
        EasyMock.expect(Helper.getMetadataLanguage()).andReturn("en").anyTimes();
        EasyMock.expect(Helper.getTranslation(EasyMock.anyString())).andReturn("").anyTimes();
        PowerMock.replay(Helper.class);

    }

    @Test
    public void testMetadataGroupImpl() throws MetadataTypeNotAllowedException {
        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
        assertNotNull(fixture);
    }

    @Test
    public void testGetPersonList() throws MetadataTypeNotAllowedException {
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

    @Test
    public void testSetPersonList() throws MetadataTypeNotAllowedException {
        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        Person p = new Person(prefs.getMetadataTypeByName("junitPerson"));
        p.setLastname("junit");
        md.addPerson(p);

        MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);

        assertFalse(fixture.getPersonList().isEmpty());
        fixture.setPersonList(new ArrayList<MetaPerson>());
        assertTrue(fixture.getPersonList().isEmpty());
    }

    @Test
    public void testGetMetadataList() throws MetadataTypeNotAllowedException {
        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        Metadata metadata = new Metadata (prefs.getMetadataTypeByName("junitMetadata"));
        md.addMetadata(metadata);
        MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
        List<MetadatumImpl> metadataList = fixture.getMetadataList();
        assertFalse(metadataList.isEmpty());
        MetadatumImpl mp = metadataList.get(0);
        assertEquals("junitMetadata", mp.getTyp());
    }

    @Test
    public void testSetMetadataList() throws MetadataTypeNotAllowedException {
        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        Metadata metadata = new Metadata (prefs.getMetadataTypeByName("junitMetadata"));
        md.addMetadata(metadata);

        MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
        assertFalse(fixture.getMetadataList().isEmpty());
        fixture.setMetadataList(new ArrayList<MetadatumImpl>());
        assertTrue(fixture.getMetadataList().isEmpty());
    }

    @Test
    public void testGetMyPrefs() throws MetadataTypeNotAllowedException {
        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
        assertEquals(prefs, fixture.getMyPrefs());
    }

    @Test
    public void testSetMyPrefs() throws MetadataTypeNotAllowedException {
        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
        fixture.setMyPrefs(null);
        assertNull(fixture.getMyPrefs());
    }

    @Test
    public void testGetMyProcess() throws MetadataTypeNotAllowedException {
        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
        assertEquals(process, fixture.getMyProcess());
    }

    @Test
    public void testSetMyProcess() throws MetadataTypeNotAllowedException {
        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
        fixture.setMyProcess(null);
        assertNull(fixture.getMyProcess());
    }

    @Test
    public void testMetadataGroup() throws MetadataTypeNotAllowedException {
        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
        fixture.setMetadataGroup(md);
        assertEquals(md, fixture.getMetadataGroup());
    }

    @Test
    public void testGetName() throws MetadataTypeNotAllowedException {
        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl fixture = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
        assertEquals("junitgrp", fixture.getName());
    }

}
