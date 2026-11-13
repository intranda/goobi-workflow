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

package io.goobi.workflow.ruleseteditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Ruleset;
import org.goobi.managedbeans.ProcessBean;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import jakarta.servlet.http.Part;
import ugh.dl.Prefs;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.RulesetManager;
import de.sub.goobi.persistence.managers.ProcessManager;

/**
 * Tests the parts of the ruleset editor that belong to the process check and the validation box.
 */
public class RulesetEditorBeanTest extends AbstractTest {

    @TempDir
    Path rulesetFolder;

    private static final String RULESET = "src/test/resources/rulesets/ruleset.xml";

    private RulesetEditorBean bean;

    @BeforeEach
    public void setUp() {
        bean = new RulesetEditorBean();
    }

    @Test
    public void testProcessCheckQueryWithoutFilter() throws Exception {
        assertEquals(" WHERE prozesse.MetadatenKonfigurationID = 5", startProcessCheckAndGetQuery(""));
    }

    @Test
    public void testProcessCheckQueryKeepsTheFilterInBrackets() throws Exception {
        // the ruleset condition must not weaken an OR in the filter, so the filter stays in brackets
        String criteria = "left join schritte ON prozesse.ProzesseID = schritte.ProzesseID WHERE a = 1 OR b = 2";
        assertEquals("left join schritte ON prozesse.ProzesseID = schritte.ProzesseID WHERE (a = 1 OR b = 2) AND prozesse.MetadatenKonfigurationID = 5",
                startProcessCheckAndGetQuery(criteria));
    }

    @Test
    public void testProcessCheckDoesNotStartWithInvalidRuleset() throws Exception {
        setField("currentRuleset", createRuleset(5));
        setField("currentRulesetFileContent", "this is no ruleset");
        try (MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProcessManager> processManager = Mockito.mockStatic(ProcessManager.class)) {
            bean.startProcessCheck();

            assertNull(bean.getProcessCheck());
            helper.verify(() -> Helper.setFehlerMeldung(Mockito.eq("rulesetEditor"), Mockito.any(), Mockito.anyString()));
            processManager.verifyNoInteractions();
        }
    }

    @Test
    public void testShowErrorsInProcessListWithoutTrailingSpace() throws Exception {
        assertEquals("\"id:1 2 3\"", showErrorsInProcessListAndGetFilter(1, 2, 3));
    }

    @Test
    public void testShowErrorsInProcessListWithOneError() throws Exception {
        assertEquals("\"id:7\"", showErrorsInProcessListAndGetFilter(7));
    }

    @Test
    public void testShowProcessesOfRulesetUsesTheWholeTitle() throws Exception {
        ProcessBean processBean = Mockito.mock(ProcessBean.class);
        try (MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class)) {
            helper.when(() -> Helper.getBeanByClass(ProcessBean.class)).thenReturn(processBean);

            bean.showProcessesOfRuleset(rulesetFor("Standard (Kopie)", "standard_kopie.xml"));
        }
        // in quotes, otherwise the filter would end at the space
        Mockito.verify(processBean).setFilter("\"ruleset:Standard (Kopie)\"");
    }

    @Test
    public void testShowDetailsOfDeletedProcessStaysInTheEditor() throws Exception {
        try (MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProcessManager> processManager = Mockito.mockStatic(ProcessManager.class)) {
            assertEquals("", bean.showProcessDetails(7));

            helper.verify(() -> Helper.setFehlerMeldung(Mockito.eq("rulesetEditor"), Mockito.any(), Mockito.anyString()));
            helper.verify(() -> Helper.getBeanByClass(ProcessBean.class), Mockito.never());
        }
    }

    @Test
    public void testProcessCheckOutdated() throws Exception {
        RulesetProcessCheck check = Mockito.mock(RulesetProcessCheck.class);
        setField("processCheck", check);
        setField("processCheckContent", "checked content");

        // unchanged editor content
        setField("currentRulesetFileContent", "checked content");
        assertFalse(bean.isProcessCheckOutdated());

        // changed editor content
        setField("currentRulesetFileContent", "changed content");
        assertTrue(bean.isProcessCheckOutdated());

        // a running check is never outdated, it shows its progress
        Mockito.when(check.isRunning()).thenReturn(true);
        assertFalse(bean.isProcessCheckOutdated());
    }

    @Test
    public void testProcessCheckNotOutdatedWithoutCheck() {
        assertFalse(bean.isProcessCheckOutdated());
    }

    @Test
    public void testInfoMessagesAvailable() throws Exception {
        // not validated yet
        assertFalse(bean.isInfoMessagesAvailable());

        List<RulesetValidationError> errors = new ArrayList<>();
        errors.add(new RulesetValidationError(1, 0, "ERROR", "error", null, null));
        setField("validationErrors", errors);
        assertFalse(bean.isInfoMessagesAvailable());

        errors.add(new RulesetValidationError(2, 0, "INFO", "info", null, null));
        assertTrue(bean.isInfoMessagesAvailable());
    }

    @Test
    public void testFileNameForName() {
        assertEquals("zeitungen_19_jh.xml", RulesetEditorBean.fileNameFor("Zeitungen 19. Jh."));
        assertEquals("groesse_uebersicht.xml", RulesetEditorBean.fileNameFor("  Größe / Übersicht "));
        assertEquals("standard_kopie.xml", RulesetEditorBean.fileNameFor("Standard (Kopie)"));
        assertEquals("cafe_zuerich.xml", RulesetEditorBean.fileNameFor("Café Zürich"));
        assertEquals("francais_espanol.xml", RulesetEditorBean.fileNameFor("Français Español"));
        assertEquals("", RulesetEditorBean.fileNameFor(" -- "));
        assertEquals("", RulesetEditorBean.fileNameFor(null));
    }

    @Test
    public void testValidFileName() {
        assertTrue(RulesetEditorBean.isValidFileName("zeitungen_2026-v2.xml"));
        assertFalse(RulesetEditorBean.isValidFileName("meine datei.xml"));
        assertFalse(RulesetEditorBean.isValidFileName("zeitungen.txt"));
        assertFalse(RulesetEditorBean.isValidFileName("../ruleset.xml"));
        assertFalse(RulesetEditorBean.isValidFileName(".xml"));
    }

    @Test
    public void testCreateNeedsAName() throws Exception {
        bean.openCreateEmpty();
        assertNull(typeName(""));
        assertFalse(bean.isCreatePossible());
    }

    @Test
    public void testCreateProblemForNameWithoutFileName() throws Exception {
        bean.openCreateEmpty();
        // no letters or digits, so no file name: the hint says why creating is not possible
        assertEquals("ruleset_create_name_no_file", typeName("???"));
        assertFalse(bean.isCreatePossible());
    }

    @Test
    public void testCreateProblemForExistingName() throws Exception {
        bean.openCreateEmpty();
        // names are compared without case
        assertEquals("ruleset_create_name_exists", typeName("standard"));
    }

    @Test
    public void testCreateFileNameComesFromTheName() throws Exception {
        bean.openCreateEmpty();
        assertNull(typeName("Zeitungen 19. Jh."));
        assertEquals("zeitungen_19_jh.xml", withRulesets(bean::getCreateFileName));
    }

    @Test
    public void testCreateTakesNextFileWhenTheFileBelongsToAnotherRuleset() throws Exception {
        bean.openCreateEmpty();
        // ruleset.xml belongs to "Standard", but the name "Ruleset" is free and must not be blocked by a file nobody sees in the dialog
        assertNull(typeName("Ruleset"));
        assertEquals("ruleset_2.xml", withRulesets(bean::getCreateFileName));
        assertTrue(withRulesets(bean::isCreatePossible));
    }

    @Test
    public void testCreateTakesNextFileWhenTheFileLiesInTheFolder() throws Exception {
        bean.openCreateEmpty();
        assertNull(typeName("Alt 2019"));
        assertEquals("alt_2019_2.xml", withRulesets(bean::getCreateFileName));
    }

    @Test
    public void testCreateFromExistingFileKeepsTheFile() throws Exception {
        bean.openCreateFromFile("alt_2019.xml");
        // the existing file is the content, so its existence is no problem
        assertEquals("alt_2019", bean.getCreateName());
        assertNull(withRulesets(bean::getCreateProblem));
        assertEquals("alt_2019.xml", bean.getCreateFileName());
    }

    @Test
    public void testDuplicateSuggestsCopyName() throws Exception {
        Ruleset zeitschriften = rulesetFor("Zeitschriften", "zeitschriften.xml");
        zeitschriften.setOrderMetadataByRuleset(true);
        assertEquals("zeitschriften_kopie.xml", duplicate(zeitschriften));
        assertEquals("Zeitschriften (Kopie)", bean.getCreateName());
        assertTrue(bean.isCreateOrderByRuleset());
    }

    @Test
    public void testDuplicateCountsUpWhenTheCopyExists() throws Exception {
        // "Standard (Kopie)" exists as a ruleset, so the second copy is suggested; its file standard_kopie_2.xml lies unused in the folder,
        // which only changes the file, not the name
        String fileName = duplicate(rulesetFor("Standard", "ruleset.xml"), rulesetFor("Standard (Kopie)", "standard_kopie.xml"));
        assertEquals("Standard (Kopie 2)", bean.getCreateName());
        assertEquals("standard_kopie_2_2.xml", fileName);
    }

    @Test
    public void testUploadIsNeededToCreate() throws Exception {
        bean.openCreateUpload();
        assertNull(typeName("Zeitungen"));
        // a free name is not enough, the uploaded file is missing
        assertFalse(withRulesets(bean::isCreatePossible));
    }

    @Test
    public void testSettingsKeepOwnFile() throws Exception {
        assertNull(settingsProblems("Standard", "ruleset.xml")[1]);
    }

    @Test
    public void testSettingsRenameToFreeFileName() throws Exception {
        String[] problems = settingsProblems("Standard", "standard.xml");
        assertNull(problems[0]);
        assertNull(problems[1]);
    }

    @Test
    public void testSettingsRenameToTakenFileName() throws Exception {
        assertEquals("ruleset_file_used", settingsProblems("Standard", "zeitschriften.xml")[1]);
        assertEquals("ruleset_file_unused", settingsProblems("Standard", "alt_2019.xml")[1]);
    }

    @Test
    public void testSettingsNameOfAnotherRuleset() throws Exception {
        // the own name is fine, the name of another ruleset is not
        assertNull(settingsProblems("standard", "ruleset.xml")[0]);
        assertEquals("ruleset_create_name_exists", settingsProblems("Zeitschriften", "ruleset.xml")[0]);
    }

    @Test
    public void testRenameIsUndoneWhenSavingFails() throws Exception {
        Ruleset standard = rulesetFor("Standard", "ruleset.xml");
        setField("rulesets", new ArrayList<>(Arrays.asList(standard)));
        setField("settingsRuleset", rulesetFor("Standard", "ruleset.xml"));
        bean.setSettingsFileName("standard.xml");

        StorageProviderInterface storage = Mockito.mock(StorageProviderInterface.class);
        try (MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class);
                MockedStatic<StorageProvider> storageProvider = Mockito.mockStatic(StorageProvider.class);
                MockedStatic<RulesetManager> rulesetManager = Mockito.mockStatic(RulesetManager.class)) {
            storageProvider.when(StorageProvider::getInstance).thenReturn(storage);
            rulesetManager.when(() -> RulesetManager.saveRuleset(Mockito.any())).thenThrow(new DAOException("database down"));

            bean.saveSettings();
        }

        // renamed first, then back again, because the database still knows the old name
        ArgumentCaptor<Path> from = ArgumentCaptor.forClass(Path.class);
        ArgumentCaptor<Path> to = ArgumentCaptor.forClass(Path.class);
        Mockito.verify(storage, Mockito.times(2)).move(from.capture(), to.capture());
        assertEquals("ruleset.xml", from.getAllValues().get(0).getFileName().toString());
        assertEquals("standard.xml", to.getAllValues().get(0).getFileName().toString());
        assertEquals("standard.xml", from.getAllValues().get(1).getFileName().toString());
        assertEquals("ruleset.xml", to.getAllValues().get(1).getFileName().toString());
        assertEquals("ruleset.xml", bean.getSettingsRuleset().getDatei());
    }

    @Test
    public void testUploadOfValidRulesetSuggestsName() throws Exception {
        upload("zeitungen_2026.xml", readRuleset());

        assertNull(bean.getCreateUploadError());
        assertEquals("zeitungen_2026.xml", bean.getCreateUploadedFileName());
        assertEquals("zeitungen_2026", bean.getCreateName());
    }

    @Test
    public void testUploadOfInvalidFileShowsError() throws Exception {
        // well-formed XML, but no ruleset
        upload("goobi_config.xml", "<config><setting>1</setting></config>");
        assertEquals("ruleset_create_upload_invalid", bean.getCreateUploadError());
        assertNull(bean.getCreateUploadedFileName());
        assertTrue(StringUtils.isBlank(bean.getCreateName()));

        // cut off XML
        upload("kaputt.xml", readRuleset().substring(0, 500));
        assertEquals("ruleset_create_upload_invalid", bean.getCreateUploadError());
        assertNull(bean.getCreateUploadedFileName());
    }

    @Test
    public void testUploadKeepsTypedName() throws Exception {
        bean.setCreateName("Meine Zeitungen");
        upload("zeitungen_2026.xml", readRuleset());
        assertEquals("Meine Zeitungen", bean.getCreateName());
    }

    @Test
    public void testSecondUploadReplacesSuggestedName() throws Exception {
        upload("zeitungen_2026.xml", readRuleset());
        upload("zeitschriften.xml", readRuleset());
        assertEquals("zeitschriften", bean.getCreateName());
        assertEquals("zeitschriften.xml", bean.getCreateUploadedFileName());
    }

    @Test
    public void testUploadUsesFileNameWithoutPath() throws Exception {
        upload("ordner/unterordner/zeitungen_2026.xml", readRuleset());
        assertEquals("zeitungen_2026.xml", bean.getCreateUploadedFileName());
        assertEquals("zeitungen_2026", bean.getCreateName());
    }

    @Test
    public void testUploadUsesFileNameWithoutWindowsPath() throws Exception {
        upload("C:\\Users\\paul\\zeitungen_2026.xml", readRuleset());
        assertEquals("zeitungen_2026.xml", bean.getCreateUploadedFileName());
        assertEquals("zeitungen_2026", bean.getCreateName());
    }

    /**
     * Uploads a file with the given name and content in the create dialog. Translations come back as their keys.
     */
    private void upload(String submittedFileName, String content) throws Exception {
        Part part = Mockito.mock(Part.class);
        Mockito.when(part.getSubmittedFileName()).thenReturn(submittedFileName);
        Mockito.when(part.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        bean.setCreateUploadedFile(part);
        try (MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class,
                i -> "getTranslation".equals(i.getMethod().getName()) ? i.getArgument(0) : null)) {
            bean.uploadCreateFile();
        }
    }

    @Test
    public void testEmptyRulesetIsALoadableRuleset() throws Exception {
        Path file = rulesetFolder.resolve("leer.xml");
        try (InputStream emptyRuleset = RulesetEditorBean.class.getResourceAsStream("/ruleset_empty.xml")) {
            assertNotNull(emptyRuleset);
            Files.copy(emptyRuleset, file);
        }
        Prefs prefs = new Prefs();
        prefs.loadPrefs(file.toString());
        assertNotNull(prefs.getDocStrctTypeByName("Monograph"));
        assertNotNull(prefs.getMetadataTypeByName("TitleDocMain"));
    }

    @Test
    public void testCreatedFileIsRemovedWhenSavingFails() throws Exception {
        bean.openCreateEmpty();
        bean.setCreateName("Zeitungen");
        assertFalse(createAndGetSaved(true));
        assertFalse(Files.exists(rulesetFolder.resolve("zeitungen.xml")));
    }

    @Test
    public void testCopyIsRemovedWhenSavingFails() throws Exception {
        Files.writeString(rulesetFolder.resolve("ruleset.xml"), "vorlage");
        setField("createMode", RulesetCreateMode.DUPLICATE);
        setField("createSource", rulesetFor("Standard", "ruleset.xml"));
        bean.setCreateName("Standard Kopie");
        createAndGetSaved(true);
        assertFalse(Files.exists(rulesetFolder.resolve("standard_kopie.xml")));
        assertEquals("vorlage", Files.readString(rulesetFolder.resolve("ruleset.xml")));
    }

    @Test
    public void testExistingFileStaysWhenSavingFails() throws Exception {
        // created from a file that was there before: it must stay
        Files.writeString(rulesetFolder.resolve("alt_2019.xml"), "alt");
        bean.openCreateFromFile("alt_2019.xml");
        createAndGetSaved(true);
        assertEquals("alt", Files.readString(rulesetFolder.resolve("alt_2019.xml")));
    }

    @Test
    public void testCreateNeverOverwritesAFile() throws Exception {
        // the file appeared after the check in the dialog, for example created at the same time by someone else
        Files.writeString(rulesetFolder.resolve("zeitungen.xml"), "alt");
        bean.openCreateEmpty();
        bean.setCreateName("Zeitungen");
        assertFalse(createAndGetSaved(false));
        assertEquals("alt", Files.readString(rulesetFolder.resolve("zeitungen.xml")));
    }

    /**
     * Creates the ruleset of the create dialog in the temporary ruleset folder, with only "Standard" known and the check in the dialog finding no
     * file. Translations come back as their keys.
     *
     * @param databaseFails true if saving the ruleset in the database fails
     * @return true if the ruleset reached the database
     */
    private boolean createAndGetSaved(boolean databaseFails) throws Exception {
        setField("rulesets", new ArrayList<>(Arrays.asList(rulesetFor("Standard", "ruleset.xml"))));
        StorageProviderInterface storage = Mockito.mock(StorageProviderInterface.class);
        ConfigurationHelper configuration = Mockito.mock(ConfigurationHelper.class);
        Mockito.when(configuration.getRulesetFolder()).thenReturn(rulesetFolder.toString());
        boolean[] saved = { false };
        try (MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class,
                i -> "getTranslation".equals(i.getMethod().getName()) ? i.getArgument(0) : null);
                MockedStatic<StorageProvider> storageProvider = Mockito.mockStatic(StorageProvider.class);
                MockedStatic<ConfigurationHelper> configurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<RulesetManager> rulesetManager = Mockito.mockStatic(RulesetManager.class)) {
            storageProvider.when(StorageProvider::getInstance).thenReturn(storage);
            configurationHelper.when(ConfigurationHelper::getInstance).thenReturn(configuration);
            rulesetManager.when(() -> RulesetManager.saveRuleset(Mockito.any())).thenAnswer(i -> {
                if (databaseFails) {
                    throw new DAOException("database down");
                }
                saved[0] = true;
                return null;
            });

            bean.createRuleset();

            helper.verify(() -> Helper.setFehlerMeldung(Mockito.eq("rulesetEditor"), Mockito.anyString(), Mockito.anyString()));
        }
        return saved[0];
    }

    /**
     * Types a name in the create dialog and returns the problem shown below it.
     */
    private String typeName(String name) throws Exception {
        bean.setCreateName(name);
        return withRulesets(bean::getCreateProblem);
    }

    /**
     * Opens the create dialog for a copy with the given rulesets, the unused file standard_kopie_2.xml in the folder and German copy names.
     *
     * @return the file the copy would get
     */
    private String duplicate(Ruleset source, Ruleset... others) throws Exception {
        List<Ruleset> rulesets = new ArrayList<>();
        rulesets.add(source);
        rulesets.addAll(Arrays.asList(others));
        setField("rulesets", rulesets);
        StorageProviderInterface storage = Mockito.mock(StorageProviderInterface.class);
        Mockito.when(storage.isFileExists(Mockito.any())).thenAnswer(i -> i.getArgument(0).toString().endsWith("standard_kopie_2.xml"));
        try (MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class, i -> {
            if (!"getTranslation".equals(i.getMethod().getName())) {
                return null;
            }
            Object[] args = i.getArguments();
            if ("ruleset_create_copy_name".equals(args[0])) {
                return args[1] + " (Kopie)";
            }
            if ("ruleset_create_copy_name_number".equals(args[0])) {
                return args[1] + " (Kopie " + args[2] + ")";
            }
            return args[0];
        });
                MockedStatic<StorageProvider> storageProvider = Mockito.mockStatic(StorageProvider.class)) {
            storageProvider.when(StorageProvider::getInstance).thenReturn(storage);
            bean.openCreateDuplicate(source);
            return bean.getCreateFileName();
        }
    }

    private Ruleset rulesetFor(String title, String file) {
        Ruleset ruleset = createRuleset(title.length());
        ruleset.setTitel(title);
        ruleset.setDatei(file);
        return ruleset;
    }

    /**
     * Opens the settings of "Standard" with the given name and file name and returns the problems of both.
     */
    private String[] settingsProblems(String name, String fileName) throws Exception {
        Ruleset standard = createRuleset(1);
        standard.setTitel(name);
        standard.setDatei("ruleset.xml");
        setField("settingsRuleset", standard);
        bean.setSettingsFileName(fileName);
        return withRulesets(() -> new String[] { bean.getSettingsNameProblem(), bean.getSettingsFileProblem() });
    }

    /**
     * Runs a check with the rulesets "Standard" (ruleset.xml) and "Zeitschriften" (zeitschriften.xml) and the unused file alt_2019.xml in the
     * ruleset folder. Translations come back as their keys.
     */
    private <T> T withRulesets(Callable<T> check) throws Exception {
        Ruleset standard = createRuleset(1);
        standard.setTitel("Standard");
        standard.setDatei("ruleset.xml");
        Ruleset zeitschriften = createRuleset(2);
        zeitschriften.setTitel("Zeitschriften");
        zeitschriften.setDatei("zeitschriften.xml");
        setField("rulesets", new ArrayList<>(Arrays.asList(standard, zeitschriften)));

        StorageProviderInterface storage = Mockito.mock(StorageProviderInterface.class);
        Mockito.when(storage.isFileExists(Mockito.any())).thenAnswer(i -> i.getArgument(0).toString().endsWith("alt_2019.xml"));
        try (MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class,
                i -> "getTranslation".equals(i.getMethod().getName()) ? i.getArgument(0) : null);
                MockedStatic<StorageProvider> storageProvider = Mockito.mockStatic(StorageProvider.class)) {
            storageProvider.when(StorageProvider::getInstance).thenReturn(storage);
            return check.call();
        }
    }

    /**
     * Starts the process check for ruleset 5 with a valid ruleset in the editor and returns the query that reaches the database.
     */
    private String startProcessCheckAndGetQuery(String criteria) throws Exception {
        setField("currentRuleset", createRuleset(5));
        setField("currentRulesetFileContent", readRuleset());
        try (MockedStatic<FilterHelper> filterHelper = Mockito.mockStatic(FilterHelper.class);
                MockedStatic<ProcessManager> processManager = Mockito.mockStatic(ProcessManager.class)) {
            filterHelper.when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.any(), Mockito.any(),
                    Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(criteria);
            processManager.when(() -> ProcessManager.getIdsForFilter(Mockito.anyString())).thenReturn(new ArrayList<>());

            bean.startProcessCheck();

            ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
            processManager.verify(() -> ProcessManager.getIdsForFilter(query.capture()));
            return query.getValue();
        }
    }

    /**
     * Opens the process list for a finished check with the given failed processes and returns the filter passed to the process list.
     */
    private String showErrorsInProcessListAndGetFilter(Integer... processIds) throws Exception {
        List<RulesetProcessCheck.ProcessError> errors = new ArrayList<>();
        for (Integer processId : Arrays.asList(processIds)) {
            errors.add(new RulesetProcessCheck.ProcessError(processId, "title", "project", "message", true));
        }
        RulesetProcessCheck check = Mockito.mock(RulesetProcessCheck.class);
        Mockito.when(check.getErrors()).thenReturn(errors);
        setField("processCheck", check);

        ProcessBean processBean = Mockito.mock(ProcessBean.class);
        try (MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class)) {
            helper.when(() -> Helper.getBeanByClass(ProcessBean.class)).thenReturn(processBean);

            bean.showProcessCheckErrorsInProcessList();

            ArgumentCaptor<String> filter = ArgumentCaptor.forClass(String.class);
            Mockito.verify(processBean).setFilter(filter.capture());
            return filter.getValue();
        }
    }

    private Ruleset createRuleset(int id) {
        Ruleset ruleset = new Ruleset();
        ruleset.setId(id);
        return ruleset;
    }

    private String readRuleset() throws Exception {
        Path path = Paths.get(RULESET);
        if (!Files.exists(path)) {
            // when run from the command line
            path = Paths.get("target/test-classes/rulesets/ruleset.xml");
        }
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = RulesetEditorBean.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(bean, value);
    }
}
