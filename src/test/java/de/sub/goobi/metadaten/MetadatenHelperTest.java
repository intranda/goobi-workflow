package de.sub.goobi.metadaten;

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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;

import org.goobi.beans.Ruleset;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import ugh.dl.ExportFileformat;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.MetsModsImportExport;

public class MetadatenHelperTest extends AbstractTest {

    private static final String RULESET = "src/test/resources/rulesets/ruleset.xml";

    private Ruleset createRulesetMock() throws PreferencesException {
        String ruleset = RULESET;
        if (!Paths.get(ruleset).toFile().exists()) {
            ruleset = Paths.get("target/test-classes/rulesets/ruleset.xml").toString(); // when run from the command line
        }
        Prefs prefs = new Prefs();
        prefs.loadPrefs(ruleset);
        Ruleset rulesetMock = Mockito.mock(Ruleset.class);
        Mockito.when(rulesetMock.getPreferences()).thenReturn(prefs);
        return rulesetMock;
    }

    /**
     * The scan for the export file formats uses a package prefix, which only resolves to any URL inside a servlet container. Outside of it - like in
     * this test - the scan finds nothing, so the tests depending on its result cannot run everywhere.
     */
    private void assumeExportFileformatsAreFound() {
        Assumptions.assumeFalse(MetadatenHelper.getExportFileformatImplementations().isEmpty(),
                "the classpath scan for 'ugh.fileformats.*' finds no implementations in this environment");
    }

    /**
     * The classpath scan for the available implementations must happen only once, its result is immutable at runtime. Scanning it again for every
     * single call brought the application down when many automatic export steps ran in parallel.
     */
    @Test
    public void testFileformatImplementationsAreScannedOnlyOnce() {
        assertSame(MetadatenHelper.getFileformatImplementations(), MetadatenHelper.getFileformatImplementations());
    }

    @Test
    public void testExportFileformatImplementationsAreScannedOnlyOnce() {
        assertSame(MetadatenHelper.getExportFileformatImplementations(), MetadatenHelper.getExportFileformatImplementations());
    }

    @Test
    public void testFileformatImplementationsContainMetsMods() {
        assertTrue(MetadatenHelper.getFileformatImplementations().contains(MetsMods.class));
    }

    @Test
    public void testExportFileformatImplementationsContainMetsModsImportExport() {
        assumeExportFileformatsAreFound();
        assertTrue(MetadatenHelper.getExportFileformatImplementations().contains(MetsModsImportExport.class));
    }

    @Test
    public void testGetFileformatByName() throws PreferencesException {
        Fileformat fileformat = MetadatenHelper.getFileformatByName("Mets", createRulesetMock());
        assertInstanceOf(MetsMods.class, fileformat);
    }

    @Test
    public void testGetExportFileformatByName() throws PreferencesException {
        assumeExportFileformatsAreFound();
        ExportFileformat fileformat = MetadatenHelper.getExportFileformatByName("Mets", createRulesetMock());
        assertInstanceOf(MetsModsImportExport.class, fileformat);
    }

    /**
     * Only the list of implementing classes may be cached. Every caller gets its own instance, as the returned file format holds state (the prefs and
     * later the digital document).
     */
    @Test
    public void testGetExportFileformatByNameReturnsANewInstanceForEveryCall() throws PreferencesException {
        assumeExportFileformatsAreFound();
        ExportFileformat first = MetadatenHelper.getExportFileformatByName("Mets", createRulesetMock());
        ExportFileformat second = MetadatenHelper.getExportFileformatByName("Mets", createRulesetMock());
        assertNotNull(first);
        assertNotNull(second);
        assertNotSame(first, second);
    }

    @Test
    public void testGetFileformatByNameReturnsANewInstanceForEveryCall() throws PreferencesException {
        Fileformat first = MetadatenHelper.getFileformatByName("Mets", createRulesetMock());
        Fileformat second = MetadatenHelper.getFileformatByName("Mets", createRulesetMock());
        assertNotNull(first);
        assertNotNull(second);
        assertNotSame(first, second);
    }

    @Test
    public void testGetFileformatByUnknownName() {
        assertNull(MetadatenHelper.getFileformatByName("unknown format", Mockito.mock(Ruleset.class)));
    }

    @Test
    public void testGetExportFileformatByUnknownName() {
        assertNull(MetadatenHelper.getExportFileformatByName("unknown format", Mockito.mock(Ruleset.class)));
    }
}
