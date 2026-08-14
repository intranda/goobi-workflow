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
package org.goobi.beans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigurationHelper;
import ugh.dl.Prefs;
import ugh.fileformats.mets.MetsMods;

public class RulesetTest extends AbstractTest {

    private static final String RULESET_FILE = "ruleset.xml";

    private Ruleset ruleset;

    @TempDir
    private Path tempDir;

    private String originalGoobiFolder;

    @BeforeEach
    public void setUp() {
        ruleset = new Ruleset();
        originalGoobiFolder = ConfigurationHelper.getInstance().getGoobiFolder();
    }

    @AfterEach
    public void resetGoobiFolder() {
        ConfigurationHelper.getInstance().setParameter("goobiFolder", originalGoobiFolder);
    }

    /**
     * Copies the test ruleset into a folder of its own, so that changes to it cannot affect any other test.
     *
     * @return a ruleset pointing to the copy
     */
    private Ruleset createRulesetInTemporaryFolder(String folderName) throws IOException {
        Path source = Paths.get("src/test/resources/rulesets/" + RULESET_FILE);
        if (!Files.exists(source)) {
            source = Paths.get("target/test-classes/rulesets/" + RULESET_FILE); // when run from the command line
        }
        Path goobiFolder = tempDir.resolve(folderName);
        Path rulesetFolder = goobiFolder.resolve("rulesets");
        Files.createDirectories(rulesetFolder);
        Files.copy(source, rulesetFolder.resolve(RULESET_FILE), StandardCopyOption.REPLACE_EXISTING);

        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.toString() + "/");

        Ruleset result = new Ruleset();
        result.setDatei(RULESET_FILE);
        return result;
    }

    /**
     * Parsing a ruleset is expensive and every parsed ruleset keeps its complete XML document in memory. Loading it again for every single call filled
     * the heap with hundreds of identical copies as soon as many export threads ran at the same time.
     */
    @Test
    public void testPreferencesAreCachedBetweenCalls() throws IOException {
        Ruleset fixture = createRulesetInTemporaryFolder("cached");
        assertSame(fixture.getPreferences(), fixture.getPreferences());
    }

    @Test
    public void testPreferencesAreCachedAcrossRulesetInstances() throws IOException {
        Ruleset first = createRulesetInTemporaryFolder("shared");
        Ruleset second = new Ruleset();
        second.setDatei(RULESET_FILE);
        assertSame(first.getPreferences(), second.getPreferences());
    }

    @Test
    public void testDifferentRulesetFilesGetTheirOwnPreferences() throws IOException {
        Ruleset first = createRulesetInTemporaryFolder("first");
        Prefs firstPrefs = first.getPreferences();

        Ruleset second = createRulesetInTemporaryFolder("second");
        assertNotNull(firstPrefs);
        assertNotSame(firstPrefs, second.getPreferences());
    }

    /**
     * A ruleset can be replaced while Goobi is running, the cached preferences must not survive that.
     */
    @Test
    public void testChangedRulesetFileIsLoadedAgain() throws IOException {
        Ruleset fixture = createRulesetInTemporaryFolder("changed");
        Prefs before = fixture.getPreferences();
        assertNotNull(before.getDocStrctTypeByName("Monograph"));

        Path file = Paths.get(ConfigurationHelper.getInstance().getRulesetFolder() + RULESET_FILE);
        String content = Files.readString(file).replace("<Name>Monograph</Name>", "<Name>Renamed</Name>");
        Files.writeString(file, content);
        Files.setLastModifiedTime(file, FileTime.fromMillis(Files.getLastModifiedTime(file).toMillis() + 5000));

        Prefs after = fixture.getPreferences();
        assertNotSame(before, after);
        assertNull(after.getDocStrctTypeByName("Monograph"));
        assertNotNull(after.getDocStrctTypeByName("Renamed"));
    }

    @Test
    public void testMissingRulesetFileReturnsNull() throws IOException {
        Ruleset fixture = createRulesetInTemporaryFolder("missing");
        fixture.setDatei("does_not_exist.xml");
        assertNull(fixture.getPreferences());
    }

    @Test
    public void testDefaultPreferencesWithoutFileNameReturnNull() {
        assertNull(ruleset.getPreferences());
    }

    /**
     * Every file format reads its configuration from the XML nodes of the preferences. As those nodes are Xerces DOM nodes, which are not thread safe
     * even for pure reading, each caller must get a copy of its own. Otherwise the shared preferences break as soon as two export threads create their
     * file format at the same time.
     */
    @Test
    public void testPreferenceNodeIsACopyForEveryCall() throws IOException {
        Ruleset fixture = createRulesetInTemporaryFolder("nodecopy");
        Prefs prefs = fixture.getPreferences();

        assertNotSame(prefs.getPreferenceNode("METS"), prefs.getPreferenceNode("METS"));
        assertEquals("METS", prefs.getPreferenceNode("METS").getNodeName());
        assertTrue(prefs.getPreferenceNode("METS").hasChildNodes());
        assertNull(prefs.getPreferenceNode("does not exist"));
    }

    @Test
    public void testFileFormatsCanBeCreatedFromTheSamePreferencesInParallel() throws Exception {
        Ruleset fixture = createRulesetInTemporaryFolder("parallel");
        Prefs prefs = fixture.getPreferences();

        int numberOfThreads = 32;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(numberOfThreads);
        List<Throwable> errors = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            new Thread(() -> {
                try {
                    startSignal.await();
                    new MetsMods(prefs);
                } catch (Throwable e) {
                    synchronized (errors) {
                        errors.add(e);
                    }
                } finally {
                    finished.countDown();
                }
            }).start();
        }
        startSignal.countDown();
        finished.await();

        assertEquals(List.of(), errors);
    }

    @Test
    public void testDefaultIdIsNull() {
        assertNull(ruleset.getId());
    }

    @Test
    public void testSetAndGetId() {
        ruleset.setId(3);
        assertEquals(Integer.valueOf(3), ruleset.getId());
    }

    @Test
    public void testSetAndGetTitel() {
        ruleset.setTitel("MODS");
        assertEquals("MODS", ruleset.getTitel());
    }

    @Test
    public void testSetAndGetDatei() {
        ruleset.setDatei("ruleset.xml");
        assertEquals("ruleset.xml", ruleset.getDatei());
    }

    @Test
    public void testDefaultOrderMetadataByRulesetIsFalse() {
        assertFalse(ruleset.isOrderMetadataByRuleset());
    }

    @Test
    public void testSetOrderMetadataByRuleset() {
        ruleset.setOrderMetadataByRuleset(true);
        assertTrue(ruleset.isOrderMetadataByRuleset());
    }

    @Test
    public void testLazyLoadDoesNotThrow() {
        ruleset.lazyLoad(); // must not throw
    }
}
