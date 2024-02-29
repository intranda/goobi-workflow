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

package org.goobi.api.display;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.goobi.api.display.enums.DisplayType;
import org.goobi.api.display.helper.MetadataGeneration;
import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.mock.MockProcess;
import ugh.dl.MetadataType;

public class DisplayCaseTest extends AbstractTest {

    private Process process;

    @Before
    public void setUp() throws Exception {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.getParent().getParent().toString() + "/");

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

    @Test
    public void testConstructorMetadataProcess() {
        MetadataType type = new MetadataType();
        type.setName("RelatedItemId");
        DisplayCase dc = new DisplayCase(process, type);
        assertNotNull(dc);
        assertEquals(DisplayType.process, dc.getDisplayType());
    }

    @Test
    public void testConstructorMetadataHtml() {
        MetadataType type = new MetadataType();
        type.setName("Description");
        DisplayCase dc = new DisplayCase(process, type);
        assertNotNull(dc);
        assertEquals(DisplayType.htmlInput, dc.getDisplayType());
    }

    @Test
    public void testConstructorMetadataViaf() {
        MetadataType type = new MetadataType();
        type.setName("PersonName");
        DisplayCase dc = new DisplayCase(process, type);
        assertNotNull(dc);
        assertEquals(DisplayType.viaf, dc.getDisplayType());
        assertEquals("100__a; 700__a;", dc.getItemList().get(0).getSource());
        assertEquals("001=NORM_IDENTIFIER; 0247_a=URI; 1001_a=NORM_NAME; 1001_d=NORM_LIFEPERIOD; 1001_q=NORM_SEX; 375__a=NORM_SEX;",
                dc.getItemList().get(0).getField());
    }

    @Test
    public void testConstructorMetadataEasyDB() {
        MetadataType type = new MetadataType();
        type.setName("Format");
        DisplayCase dc = new DisplayCase(process, type);
        assertNotNull(dc);
        assertEquals(DisplayType.easydb, dc.getDisplayType());
        assertEquals("1", dc.getItemList().get(0).getSource());
        assertEquals("simpleSearch", dc.getItemList().get(0).getField());
    }

    @Test
    public void testConstructorMetadataVocabularySearch() {
        MetadataType type = new MetadataType();
        type.setName("Subjects");
        DisplayCase dc = new DisplayCase(process, type);
        assertNotNull(dc);
        assertEquals(DisplayType.vocabularySearch, dc.getDisplayType());
        assertEquals("Subject terms", dc.getItemList().get(0).getSource());
        assertEquals("original value;corrected value", dc.getItemList().get(0).getField());
    }

    @Test
    public void testConstructorMetadataVocabularyList() {
        MetadataType type = new MetadataType();
        type.setName("SubjectPerson");
        DisplayCase dc = new DisplayCase(process, type);
        assertNotNull(dc);
        assertEquals(DisplayType.vocabularyList, dc.getDisplayType());
        assertEquals("Subject terms", dc.getItemList().get(0).getSource());
        assertEquals("type=Person", dc.getItemList().get(0).getField());
    }

    @Test
    public void testConstructorMetadataConvertibleDate() {
        MetadataType type = new MetadataType();
        type.setName("GregorianDate");
        DisplayCase dc = new DisplayCase(process, type);
        assertNotNull(dc);
        assertEquals(DisplayType.convertibleDate, dc.getDisplayType());
        assertEquals("Testquelle", dc.getItemList().get(0).getSource());
        assertEquals("1983-12-01", dc.getItemList().get(0).getField());

    }

    @Test
    public void testConstructorMetadataGenerate() {
        MetadataType type = new MetadataType();
        type.setName("junitGenerationMetadata");
        DisplayCase dc = new DisplayCase(process, type);
        assertNotNull(dc);
        assertEquals(DisplayType.generate, dc.getDisplayType());
        Item fixture = dc.getItemList().get(0);
        assertEquals("abcdef [VALUE] gehij", fixture.getLabel());
        assertEquals("", fixture.getValue());
        MetadataGeneration mg = (MetadataGeneration) fixture.getAdditionalData();
        assertNotNull(mg);
        assertEquals("goobi:metadata[@name='TitleDocMain'][text()='main title']", mg.getCondition());
    }

}
