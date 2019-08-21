package de.sub.goobi.metadaten;

import static org.junit.Assert.*;

import java.util.List;

import javax.faces.model.SelectItem;

import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ugh.dl.Metadata;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import de.sub.goobi.mock.MockProcess;

public class MetadatumImplTest {

    private Prefs prefs;
    private Process process;
    private static final String METADATA_TYPE = "junitMetadata";
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {

        process = MockProcess.createProcess(folder);
        prefs = process.getRegelsatz().getPreferences();
    }

    @Test
    public void testMetadatumImpl() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        assertNotNull(md);
    }

    @Test
    public void testWert() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        String value = "test";
        md.setWert(value);
        assertEquals(value, m.getValue());
    }

    @Test
    public void testTyp() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        md.setTyp(METADATA_TYPE);
        assertEquals(METADATA_TYPE, md.getTyp());
    }

    @Test
    public void testGetIdentifier() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        md.setIdentifier(1);
        assertEquals(1, md.getIdentifier());
    }

    @Test
    public void testtMd() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);

        md.setMd(m);
        assertEquals(m, md.getMd());

    }

    @Test
    public void testGetOutputType() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        assertEquals("input", md.getOutputType());
    }

    @Test
    public void testGetItems() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        List<SelectItem> items = md.getItems();
        assertNotNull(items);

    }

    @Test
    public void testGetSelectedItems() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        List<String> items = md.getSelectedItems();
        assertNotNull(items);
    }

    @Test
    public void testGetSelectedItem() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        String item = md.getSelectedItem();
        assertNotNull(item);
    }

    @Test
    public void testGetValue() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);

        md.setValue("value");
        assertEquals("value", md.getValue());

    }

    @Test
    public void testGetPossibleDatabases() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        List<String> databases = md.getPossibleDatabases();
        assertNotNull(databases);
    }

    @Test
    public void testGetNormdataValue() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        String id = md.getNormdataValue();
        assertNull(id);
    }

    @Test
    public void testSetNormdataValue() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        String id = md.getNormdataValue();
        assertNull(id);
        md.setNormdataValue("value");
        assertEquals("value", md.getNormdataValue());

    }

    @Test
    public void testNormDatabase() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        List<String> databases = md.getPossibleDatabases();
        md.setNormDatabase(databases.get(0));
        assertEquals("gnd", md.getNormDatabase());
    }

    @Test
    public void testIsNormdata() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        assertFalse(md.isNormdata());
    }

}
