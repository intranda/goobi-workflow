package org.goobi.api.display;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.goobi.api.display.enums.DisplayType;
import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Test;

import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.mock.MockProcess;
import ugh.dl.MetadataType;

public class DisplayCaseTest {

    private Process process;

    @Before
    public void setUp() throws Exception {

        Path path = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        String goobiFolder = path.getParent().getParent().getParent().toString() + "/test/resources/";
        ConfigurationHelper.CONFIG_FILE_NAME = goobiFolder + "config/goobi_config.properties";
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder);

        process = MockProcess.createProcess();

    }

    @Test
    public void testConstructorPerson() {
        MetadataType type = new MetadataType();
        type.setIsPerson(true);
        DisplayCase dc = new DisplayCase(process, type);
        assertNotNull(dc);
        assertEquals(DisplayType.person, dc.getDisplayType());
    }

    @Test
    public void testConstructorMetadataNotConfigured() {
        MetadataType type = new MetadataType();
        type.setName("Something");
        DisplayCase dc = new DisplayCase(process, type);
        assertNotNull(dc);
        assertEquals(DisplayType.input, dc.getDisplayType());
    }

    @Test
    public void testConstructorMetadataReadOnly() {
        MetadataType type = new MetadataType();
        type.setName("physPageNumber");
        DisplayCase dc = new DisplayCase(process, type);
        assertNotNull(dc);
        assertEquals(DisplayType.readonly, dc.getDisplayType());
    }


    @Test
    public void testConstructorMetadataSelection() {
        MetadataType type = new MetadataType();
        type.setName("UseAndReproductionLicense");
        DisplayCase dc = new DisplayCase(process, type);
        assertNotNull(dc);
        assertEquals(DisplayType.select1, dc.getDisplayType());

        assertEquals(10, dc.getItemList().size());
    }

    @Test
    public void testConstructorMetadataMultiselect() {
        MetadataType type = new MetadataType();
        type.setName("RestrictionOnAccessLicense");
        DisplayCase dc = new DisplayCase(process, type);
        assertNotNull(dc);
        assertEquals(DisplayType.select, dc.getDisplayType());
        assertEquals(2, dc.getItemList().size());
    }

    @Test
    public void testConstructorMetadataGnd() {
        MetadataType type = new MetadataType();
        type.setName("Location");
        DisplayCase dc = new DisplayCase(process, type);
        assertNotNull(dc);
        assertEquals(DisplayType.gnd, dc.getDisplayType());
    }

    @Test
    public void testConstructorMetadataGeonames() {
        MetadataType type = new MetadataType();
        type.setName("PlaceOfPublication");
        DisplayCase dc = new DisplayCase(process, type);
        assertNotNull(dc);
        assertEquals(DisplayType.geonames, dc.getDisplayType());
        assertEquals("DE; AT; CH;", dc.getItemList().get(0).getSource());
    }

    @Test
    public void testConstructorMetadataDante() {
        MetadataType type = new MetadataType();
        type.setName("DocLanguage");
        DisplayCase dc = new DisplayCase(process, type);
        assertNotNull(dc);
        assertEquals(DisplayType.dante, dc.getDisplayType());
        assertEquals("iso_639", dc.getItemList().get(0).getSource());
        assertEquals("NORM_LABEL_en, NORM_LABEL_de", dc.getItemList().get(0).getField());
    }

    // TODO
    //    process,
    //    htmlInput,
    //    viaf,
    //    easydb,
    //    vocabularySearch,
    //    vocabularyList;



}
