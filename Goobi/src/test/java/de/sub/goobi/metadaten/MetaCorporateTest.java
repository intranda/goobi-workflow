package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.goobi.api.display.enums.DisplayType;
import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.metadaten.search.ViafSearch;
import de.sub.goobi.mock.MockProcess;
import ugh.dl.Corporate;
import ugh.dl.DocStruct;
import ugh.dl.NamePart;
import ugh.dl.Prefs;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ViafSearch.class, Helper.class })
public class MetaCorporateTest extends AbstractTest {

    private Prefs prefs;
    private Process process;
    private static final String METADATA_TYPE = "junitCorporate";
    private Corporate c;

    @Before
    public void setUp() throws Exception {
        process = MockProcess.createProcess();
        prefs = process.getRegelsatz().getPreferences();
        c = new Corporate(prefs.getMetadataTypeByName(METADATA_TYPE));
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
    public void testConstructor() throws Exception {
        MetaCorporate fixture = new MetaCorporate(c, prefs, null, process, null);
        assertNotNull(fixture);
    }

    @Test
    public void testDisplaytype() throws Exception {
        MetaCorporate fixture = new MetaCorporate(c, prefs, null, process, null);
        assertEquals(DisplayType.corporate, fixture.getMetadataDisplaytype());
    }

    @Test
    public void testRole() throws Exception {
        MetaCorporate fixture = new MetaCorporate(c, prefs, null, process, null);
        fixture.setRole(METADATA_TYPE);
        assertEquals(METADATA_TYPE, fixture.getRole());
    }

    @Test
    public void testAddableRoles() throws Exception {
        DocStruct ds = process.readMetadataFile().getDigitalDocument().getLogicalDocStruct();
        ds.addCorporate(c);
        MetaCorporate fixture = new MetaCorporate(c, prefs, ds, process, null);

        assertEquals(1, fixture.getAddableRoles().size());
        assertEquals(METADATA_TYPE, fixture.getAddableRoles().get(0).getValue());
    }

    @Test
    public void testMainName() throws Exception {
        MetaCorporate fixture = new MetaCorporate(c, prefs, null, process, null);
        fixture.setMainName("main name");
        assertEquals("main name", fixture.getMainName());
    }

    @Test
    public void testPartName() throws Exception {
        MetaCorporate fixture = new MetaCorporate(c, prefs, null, process, null);
        fixture.setPartName("part name");
        assertEquals("part name", fixture.getPartName());
    }

    @Test
    public void testSubNames() throws Exception {
        MetaCorporate fixture = new MetaCorporate(c, prefs, null, process, null);
        assertEquals(1, fixture.getSubNameSize());
        fixture.getSubNames().get(0).setValue("val");
        fixture.addSubName();
        assertEquals(2, fixture.getSubNameSize());
        NamePart np = fixture.getSubNames().get(1);
        fixture.removeSubName(np);
        assertEquals(1, fixture.getSubNameSize());
    }

    @Test
    public void testGetPossibleDatabases() throws Exception {
        MetaCorporate fixture = new MetaCorporate(c, prefs, null, process, null);
        assertEquals("gnd", fixture.getPossibleDatabases().get(0));
    }

    @Test
    public void testIsNormdata() throws Exception {
        MetaCorporate fixture = new MetaCorporate(c, prefs, null, process, null);
        assertTrue(fixture.isNormdata());
    }

    @Test
    public void testNormdataValue() throws Exception {
        MetaCorporate fixture = new MetaCorporate(c, prefs, null, process, null);
        fixture.setNormdataValue("test");
        assertEquals("test", fixture.getNormdataValue());
    }

    @Test
    public void testNormDatabase() throws Exception {
        MetaCorporate fixture = new MetaCorporate(c, prefs, null, process, null);
        fixture.setNormDatabase("gnd");
        assertEquals("gnd", fixture.getNormDatabase());
    }

    //    search
    //    getData
    //    isShowNoHitFound
}
