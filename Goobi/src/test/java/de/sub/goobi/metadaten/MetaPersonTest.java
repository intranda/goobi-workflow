package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.faces.model.SelectItem;

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
import ugh.dl.DocStruct;
import ugh.dl.NamePart;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ViafSearch.class, Helper.class})
public class MetaPersonTest extends AbstractTest {

    private Prefs prefs;
    private Process process;
    private DocStruct docstruct;

    private static final String METADATA_TYPE = "junitPerson";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {

        process = MockProcess.createProcess();
        prefs = process.getRegelsatz().getPreferences();
        docstruct = process.readMetadataFile().getDigitalDocument().getLogicalDocStruct();

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
    public void testMetaPerson() throws MetadataTypeNotAllowedException {
        Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
        assertNotNull(mp);
    }

    @Test
    public void testIdentifier() throws MetadataTypeNotAllowedException {
        Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
        assertNotNull(mp);
        assertEquals(0, mp.getIdentifier());
        mp.setIdentifier(1);
        assertNotEquals(0, mp.getIdentifier());
    }

    @Test
    public void testPerson() throws MetadataTypeNotAllowedException {
        Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
        assertNotNull(mp);
        mp.setP(p);
        assertSame(p, mp.getP());
    }

    @Test
    public void testFirstname() throws MetadataTypeNotAllowedException {
        Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
        assertNotNull(mp);
        assertEquals("", mp.getVorname());
        mp.setVorname("fixture");
        assertEquals("fixture", mp.getVorname());
        mp.setVorname(null);
        assertEquals("", mp.getVorname());
    }

    @Test
    public void testLastname() throws MetadataTypeNotAllowedException {
        Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
        assertNotNull(mp);
        assertEquals("", mp.getNachname());
        mp.setNachname("fixture");
        assertEquals("fixture", mp.getNachname());
        mp.setNachname(null);
        assertEquals("", mp.getNachname());
    }

    @Test
    public void testRole() throws MetadataTypeNotAllowedException {
        Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
        assertNotNull(mp);
        assertEquals(METADATA_TYPE, mp.getRolle());
        mp.setRolle(METADATA_TYPE);
        assertEquals(METADATA_TYPE, mp.getRolle());
    }

    @Test
    public void testAddableRollen() throws MetadataTypeNotAllowedException {
        Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
        assertNotNull(mp);
        List<SelectItem> fixture = mp.getAddableRollen();
        assertNotNull(fixture);
        assertEquals(6, fixture.size());
    }

    @Test
    public void testAdditionalNameParts() throws MetadataTypeNotAllowedException {
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

    @Test
    public void testPossibleDatabases() throws MetadataTypeNotAllowedException {
        Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
        assertNotNull(mp);
        List<String> fixture = mp.getPossibleDatabases();
        assertEquals("gnd", fixture.get(0));
    }

    @Test
    public void testPossibleNameparts() throws MetadataTypeNotAllowedException {
        Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
        assertNotNull(mp);
        List<String> fixture = mp.getPossibleNamePartTypes();
        assertEquals("date", fixture.get(0));
        assertEquals("termsOfAddress", fixture.get(1));
    }

    @Test
    public void testNormdataValue() throws MetadataTypeNotAllowedException {
        Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
        assertNotNull(mp);
        String fixture = "value";
        mp.setNormdataValue(fixture);
        assertEquals(fixture, mp.getNormdataValue());
    }

    @Test
    public void testNormDatabase() throws MetadataTypeNotAllowedException {
        Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
        assertNotNull(mp);
        String fixture = "gnd";
        mp.setNormDatabase(fixture);
        assertEquals(fixture, mp.getNormDatabase());
    }

    @Test
    public void testAdditionalParts() throws MetadataTypeNotAllowedException {
        Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
        assertNotNull(mp);
        assertTrue(mp.isAdditionalParts());
        p.getType().setAllowNameParts(false);
        assertFalse(mp.isAdditionalParts());
    }

    @Test
    public void testNormdata() throws MetadataTypeNotAllowedException {
        Person p = new Person(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetaPerson mp = new MetaPerson(p, 0, prefs, docstruct, process, null);
        assertNotNull(mp);
        assertTrue(mp.isNormdata());
        p.getType().setAllowNormdata(false);
        assertFalse(mp.isNormdata());
    }
}
