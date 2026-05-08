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
 */
package org.goobi.production.cli.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Process;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.production.importer.ImportObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.forms.AdditionalField;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;
import ugh.dl.Fileformat;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class CopyProcessTest extends AbstractTest {

    private Process processTemplate;
    private Fileformat mockFileformat;

    private MockedStatic<MetadatenHelper> mockedMetadatenHelper;
    private MockedStatic<ProcessManager> mockedProcessManager;
    private MockedStatic<PropertyManager> mockedPropertyManager;
    private MockedStatic<StepManager> mockedStepManager;
    private MockedStatic<Helper> mockedHelper;

    @BeforeEach
    public void setUp() throws Exception {
        // prepare process template
        processTemplate = MockProcess.createProcess();

        User user = new User();
        user.setLogin("testuser");
        List<User> users = new ArrayList<>();
        users.add(user);

        Step step = new Step();
        step.setTitel("Step 1");
        step.setReihenfolge(1);
        step.setBenutzer(users);
        step.setBenutzergruppen(new ArrayList<>());
        step.setBearbeitungsstatusEnum(StepStatus.OPEN);
        processTemplate.setSchritte(Collections.singletonList(step));

        mockFileformat = Mockito.mock(Fileformat.class);
        Mockito.when(mockFileformat.read(Mockito.anyString())).thenReturn(true);
        prepareMocking();
    }

    @AfterEach
    public void tearDown() {
        if (mockedHelper != null) {
            mockedHelper.close();
        }
        if (mockedStepManager != null) {
            mockedStepManager.close();
        }
        if (mockedPropertyManager != null) {
            mockedPropertyManager.close();
        }
        if (mockedProcessManager != null) {
            mockedProcessManager.close();
        }
        if (mockedMetadatenHelper != null) {
            mockedMetadatenHelper.close();
        }
    }

    private void prepareMocking() throws Exception {
        mockedMetadatenHelper = Mockito.mockStatic(MetadatenHelper.class);
        mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
        mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
        mockedStepManager = Mockito.mockStatic(StepManager.class);
        mockedHelper = Mockito.mockStatic(Helper.class);

        mockedMetadatenHelper.when(() -> MetadatenHelper.getMetaFileType(Mockito.anyString())).thenReturn("metsmods");
        mockedMetadatenHelper.when(() -> MetadatenHelper.getFileformatByName(Mockito.anyString(), Mockito.any(Ruleset.class)))
                .thenReturn(mockFileformat);

        mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);

        List<GoobiProperty> emptyProps = new ArrayList<>();
        mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                .thenReturn(emptyProps);

        mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());

        mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
        mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString(), Mockito.anyString())).thenReturn("");
        mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
    }

    @Test
    public void testConstructor() {
        CopyProcess cp = new CopyProcess();
        assertNotNull(cp);
    }

    @Test
    public void testConstants() {
        assertEquals("firstchild", CopyProcess.FIELD_FIRSTCHILD);
        assertEquals("boundbook", CopyProcess.FIELD_BOUNDBOOK);
        assertEquals("ListOfCreators", CopyProcess.FIELD_LIST_OF_CREATORS);
        assertEquals("singleDigCollection", CopyProcess.FIELD_SINGLE_DIG_COLLECTION);
        assertEquals("_tif", CopyProcess.DIRECTORY_SUFFIX);
    }

    @Test
    public void testGettersAndSetters() {
        CopyProcess cp = new CopyProcess();

        cp.setOpacSuchfeld("02");
        assertEquals("02", cp.getOpacSuchfeld());

        cp.setOpacSuchbegriff("12345");
        assertEquals("12345", cp.getOpacSuchbegriff());

        cp.setOpacKatalog("KXP");
        assertEquals("KXP", cp.getOpacKatalog());

        cp.setDocType("monograph");
        assertEquals("monograph", cp.getDocType());

        cp.setAuswahl(3);
        assertEquals(3, cp.getAuswahl().intValue());

        cp.setMetadataFile("/path/to/meta.xml");
        assertEquals("/path/to/meta.xml", cp.getMetadataFile());

        cp.setTifHeaderDocumentname("docname");
        assertEquals("docname", cp.getTifHeaderDocumentname());

        cp.setTifHeaderImagedescription("image description");
        assertEquals("image description", cp.getTifHeaderImagedescription());

        cp.setProzessVorlage(processTemplate);
        assertEquals(processTemplate, cp.getProzessVorlage());

        List<String> cols = new ArrayList<>();
        cols.add("Collection1");
        cp.setDigitalCollections(cols);
        assertEquals(cols, cp.getDigitalCollections());
    }

    @Test
    public void testOpenFirstPageReturnsNullInitially() {
        CopyProcess cp = new CopyProcess();
        assertNull(cp.openFirstPage());
    }

    @Test
    public void testPrepareReturnsEmptyWhenProcessHasNoSteps() throws Exception {
        // Prozess ohne Schritte → getContainsUnreachableSteps() = true
        Process processNoSteps = MockProcess.createProcess();
        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(processNoSteps);
        assertEquals("", cp.prepare(new ImportObject()));
    }

    @Test
    public void testPrepareWithImportObjectInitializesProzessKopie() {
        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(processTemplate);
        cp.setMetadataFile("dummy/meta.xml");
        cp.prepare(new ImportObject());

        assertNotNull(cp.getProzessKopie());
        assertEquals("", cp.getProzessKopie().getTitel());
        assertFalse(cp.getProzessKopie().isIstTemplate());
        assertEquals(processTemplate.getProjekt(), cp.getProzessKopie().getProjekt());
    }

    @Test
    public void testPrepareWithImportObjectInitializesAdditionalFields() {
        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(processTemplate);
        cp.setMetadataFile("dummy/meta.xml");
        cp.prepare(new ImportObject());

        assertNotNull(cp.getAdditionalFields());
    }

    @Test
    public void testPrepareWithImportObjectInitializesStandardFields() {
        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(processTemplate);
        cp.setMetadataFile("dummy/meta.xml");
        cp.prepare(new ImportObject());

        assertNotNull(cp.getStandardFields());
        assertTrue(cp.getStandardFields().get("collections"));
        assertTrue(cp.getStandardFields().get("doctype"));
    }

    @Test
    public void testPrepareWithoutImportObjectInitializesPossibleDigitalCollections() {
        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(processTemplate);
        cp.setMetadataFile("dummy/meta.xml");
        cp.prepare();

        assertNotNull(cp.getPossibleDigitalCollections());
    }

    @Test
    public void testIsSingleChoiceCollectionFalseInitially() {
        CopyProcess cp = new CopyProcess();
        assertFalse(cp.isSingleChoiceCollection());
    }

    @Test
    public void testIsSingleChoiceCollectionTrueAfterPrepareFindsSingleProjectCollection() {
        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(processTemplate);
        cp.setMetadataFile("dummy/meta.xml");
        cp.prepare();

        assertTrue(cp.isSingleChoiceCollection());
        assertEquals("Collection", cp.getDigitalCollectionIfSingleChoice());
    }

    @Test
    public void testIsSingleChoiceCollectionFalseAfterPrepareWithImportObject() {
        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(processTemplate);
        cp.setMetadataFile("dummy/meta.xml");
        cp.prepare(new ImportObject());

        assertFalse(cp.isSingleChoiceCollection());
        assertNull(cp.getPossibleDigitalCollections());
    }

    @Test
    public void testTestTitleReturnsFalseForEmptyTitle() {
        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(processTemplate);
        Process kopie = new Process();
        kopie.setProjekt(processTemplate.getProjekt());
        kopie.setTitel("");
        cp.setProzessKopie(kopie);

        assertFalse(cp.testTitle());
    }

    @Test
    public void testTestTitleReturnsFalseForTitleWithSpaces() {
        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(processTemplate);
        Process kopie = new Process();
        kopie.setProjekt(processTemplate.getProjekt());
        kopie.setTitel("invalid title with spaces");
        cp.setProzessKopie(kopie);

        assertFalse(cp.testTitle());
    }

    @Test
    public void testTestTitleReturnsTrueForValidUniqueTitle() {
        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(processTemplate);
        Process kopie = new Process();
        kopie.setProjekt(processTemplate.getProjekt());
        kopie.setTitel("valid_title_123");
        cp.setProzessKopie(kopie);

        assertTrue(cp.testTitle());
    }

    @Test
    public void testTestTitleReturnsFalseForDuplicateTitle() {
        // Override default stub: title already exists
        mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(1);

        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(processTemplate);
        Process kopie = new Process();
        kopie.setProjekt(processTemplate.getProjekt());
        kopie.setTitel("already_existing_title");
        cp.setProzessKopie(kopie);

        assertFalse(cp.testTitle());
    }

    @Test
    public void testCalculateProcessTitleWithEmptyFields() {
        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(processTemplate);
        cp.setMetadataFile("dummy/meta.xml");
        cp.prepare(new ImportObject());
        cp.setDocType("monograph");

        cp.calculateProcessTitle();

        assertNotNull(cp.getProzessKopie().getTitel());
    }

    @Test
    public void testCalculateProcessTitleUsesFieldValues() {
        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(processTemplate);
        cp.setMetadataFile("dummy/meta.xml");
        cp.prepare(new ImportObject());
        cp.setDocType("monograph");

        AdditionalField ats = new AdditionalField();
        ats.setTitel("ATS");
        ats.setWert("ab");

        AdditionalField tsl = new AdditionalField();
        tsl.setTitel("TSL");
        tsl.setWert("cd");

        AdditionalField identifier = new AdditionalField();
        identifier.setTitel("Identifier analog a-set");
        identifier.setWert("PPN123");

        cp.getAdditionalFields().add(ats);
        cp.getAdditionalFields().add(tsl);
        cp.getAdditionalFields().add(identifier);

        cp.calculateProcessTitle();

        assertEquals("abcd_PPN123", cp.getProzessKopie().getTitel());
    }

    @Test
    public void testCalcTiffheaderSetsDocumentname() {
        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(processTemplate);
        cp.setMetadataFile("dummy/meta.xml");
        cp.prepare(new ImportObject());
        cp.setDocType("monograph");
        cp.getProzessKopie().setTitel("test_process");

        cp.calcTiffheader();

        assertEquals("test_process", cp.getTifHeaderDocumentname());
    }

    @Test
    public void testCalcTiffheaderContainsDoctypeInImagedescription() {
        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(processTemplate);
        cp.setMetadataFile("dummy/meta.xml");
        cp.prepare(new ImportObject());
        cp.setDocType("monograph");
        cp.getProzessKopie().setTitel("test_process");

        cp.calcTiffheader();

        assertTrue(cp.getTifHeaderImagedescription().contains("monograph"));
    }
}
