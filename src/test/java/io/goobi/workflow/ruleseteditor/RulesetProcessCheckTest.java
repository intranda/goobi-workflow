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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.persistence.managers.ProcessManager;
import ugh.dl.Prefs;

/**
 * Tests the process check of the ruleset editor. The check runs directly with run(), without its own thread, because static mocks only work in
 * the thread that created them.
 */
public class RulesetProcessCheckTest extends AbstractTest {

    private static final String RULESET = "src/test/resources/rulesets/ruleset.xml";
    private static final String META_FILE = "src/test/resources/metadata/1/meta.xml";

    @TempDir
    private Path tempFolder;

    // the ruleset as it is saved, the test metadata file fits it
    private Prefs savedPrefs;

    // the ruleset as it is in the editor, without PlaceOfPublication in any structure type
    private Prefs editorPrefs;

    @BeforeEach
    public void setUp() throws Exception {
        savedPrefs = loadPrefs(readTestFile(RULESET));
        String withoutPlace = readTestFile(RULESET).replaceAll("<metadata[^>]*>PlaceOfPublication</metadata>", "");
        editorPrefs = loadPrefs(withoutPlace);
    }

    @Test
    public void testReadableProcess() throws Exception {
        Process process = createProcess(1, testFile(META_FILE).toString());
        try (MockedStatic<ProcessManager> processManager = Mockito.mockStatic(ProcessManager.class)) {
            processManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            RulesetProcessCheck check = new RulesetProcessCheck(Arrays.asList(1), savedPrefs);
            check.run();

            assertTrue(check.getErrors().isEmpty());
            assertEquals(1, check.getDone());
            assertEquals(1, check.getTotal());
            assertFalse(check.isRunning());
        }
    }

    @Test
    public void testErrorWhenOnlyTheEditorContentBreaksTheProcess() throws Exception {
        Process process = createProcess(1, testFile(META_FILE).toString());
        try (MockedStatic<ProcessManager> processManager = Mockito.mockStatic(ProcessManager.class)) {
            processManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            RulesetProcessCheck check = new RulesetProcessCheck(Arrays.asList(1), editorPrefs);
            check.run();

            List<RulesetProcessCheck.ProcessError> errors = check.getErrors();
            assertEquals(1, errors.size());
            RulesetProcessCheck.ProcessError error = errors.get(0);
            assertEquals(1, error.getProcessId());
            assertEquals("process 1", error.getTitle());
            assertEquals("project", error.getProject());
            assertNotNull(error.getMessage());
            assertTrue(error.getMessage().contains("PlaceOfPublication"));
            // the saved ruleset still fits, so the metadata editor can open the process
            assertTrue(error.isOpenable());
        }
    }

    @Test
    public void testErrorWhenTheSavedRulesetDoesNotFitEither() throws Exception {
        // a metadata file with a field no ruleset knows
        String brokenContent = readTestFile(META_FILE).replace("name=\"PlaceOfPublication\"", "name=\"UnknownTestField\"");
        Path brokenFile = tempFolder.resolve("meta.xml");
        Files.writeString(brokenFile, brokenContent, StandardCharsets.UTF_8);

        Process process = createProcess(1, brokenFile.toString());
        try (MockedStatic<ProcessManager> processManager = Mockito.mockStatic(ProcessManager.class)) {
            processManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            RulesetProcessCheck check = new RulesetProcessCheck(Arrays.asList(1), editorPrefs);
            check.run();

            assertEquals(1, check.getErrors().size());
            assertFalse(check.getErrors().get(0).isOpenable());
        }
    }

    @Test
    public void testBrokenSavedRulesetRepairedInEditor() throws Exception {
        // the saved ruleset file is broken, so it gives no preferences; the editor content is fine again
        Process process = createProcess(1, testFile(META_FILE).toString());
        Mockito.when(process.getRegelsatz().getPreferences()).thenReturn(null);
        try (MockedStatic<ProcessManager> processManager = Mockito.mockStatic(ProcessManager.class)) {
            processManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            RulesetProcessCheck check = new RulesetProcessCheck(Arrays.asList(1), savedPrefs);
            check.run();

            assertTrue(check.getErrors().isEmpty());
        }
    }

    @Test
    public void testBrokenSavedRulesetMakesProcessNotOpenable() throws Exception {
        // editor content does not fit and the saved ruleset is broken: the metadata editor cannot open the process
        Process process = createProcess(1, testFile(META_FILE).toString());
        Mockito.when(process.getRegelsatz().getPreferences()).thenReturn(null);
        try (MockedStatic<ProcessManager> processManager = Mockito.mockStatic(ProcessManager.class)) {
            processManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            RulesetProcessCheck check = new RulesetProcessCheck(Arrays.asList(1), editorPrefs);
            check.run();

            assertEquals(1, check.getErrors().size());
            assertTrue(check.getErrors().get(0).getMessage().contains("PlaceOfPublication"));
            assertFalse(check.getErrors().get(0).isOpenable());
        }
    }

    @Test
    public void testDeletedProcessIsSkipped() throws Exception {
        try (MockedStatic<ProcessManager> processManager = Mockito.mockStatic(ProcessManager.class)) {
            processManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(null);

            RulesetProcessCheck check = new RulesetProcessCheck(Arrays.asList(1), savedPrefs);
            check.run();

            assertTrue(check.getErrors().isEmpty());
            assertEquals(1, check.getDone());
        }
    }

    @Test
    public void testCancelBeforeRun() throws Exception {
        try (MockedStatic<ProcessManager> processManager = Mockito.mockStatic(ProcessManager.class)) {
            RulesetProcessCheck check = new RulesetProcessCheck(Arrays.asList(1, 2), savedPrefs);
            check.cancel();
            check.run();

            assertEquals(0, check.getDone());
            assertTrue(check.isCancelled());
            processManager.verifyNoInteractions();
        }
    }

    private Process createProcess(int id, String metadataFile) throws Exception {
        Ruleset ruleset = Mockito.mock(Ruleset.class);
        Mockito.when(ruleset.getPreferences()).thenReturn(savedPrefs);
        Project project = Mockito.mock(Project.class);
        Mockito.when(project.getTitel()).thenReturn("project");

        Process process = Mockito.mock(Process.class);
        Mockito.when(process.getTitel()).thenReturn("process " + id);
        Mockito.when(process.getProjekt()).thenReturn(project);
        Mockito.when(process.getRegelsatz()).thenReturn(ruleset);
        Mockito.when(process.getMetadataFilePath()).thenReturn(metadataFile);
        return process;
    }

    private Prefs loadPrefs(String content) throws Exception {
        Path file = Files.createTempFile(tempFolder, "ruleset", ".xml");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        Prefs prefs = new Prefs();
        prefs.loadPrefs(file.toString());
        return prefs;
    }

    private String readTestFile(String file) throws Exception {
        return Files.readString(testFile(file), StandardCharsets.UTF_8);
    }

    private Path testFile(String file) {
        Path path = Paths.get(file);
        if (!Files.exists(path)) {
            // when run from the command line
            path = Paths.get(file.replace("src/test/resources", "target/test-classes"));
        }
        return path;
    }
}
