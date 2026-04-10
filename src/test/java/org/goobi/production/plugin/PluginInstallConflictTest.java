package org.goobi.production.plugin;

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
 */
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.goobi.production.plugin.PluginInstallConflict.ResolveTactic;
import org.junit.Before;
import org.junit.Test;

public class PluginInstallConflictTest {

    private PluginInstallConflict conflict;

    @Before
    public void setUp() {
        conflict = new PluginInstallConflict(
                "/opt/digiverso/goobi/config/plugin_test.xml",
                ResolveTactic.unknown,
                "existing content",
                "uploaded content",
                "archived content");
    }

    @Test
    public void testConstructor() {
        assertNotNull(conflict);
    }

    @Test
    public void testConstructorSetsPath() {
        assertEquals("/opt/digiverso/goobi/config/plugin_test.xml", conflict.getPath());
    }

    @Test
    public void testConstructorSetsResolveTactic() {
        assertEquals(ResolveTactic.unknown, conflict.getResolveTactic());
    }

    @Test
    public void testConstructorSetsVersions() {
        assertEquals("existing content", conflict.getExistingVersion());
        assertEquals("uploaded content", conflict.getUploadedVersion());
        assertEquals("archived content", conflict.getArchivedVersion());
    }

    @Test
    public void testConstructorInitializesEditedVersions() {
        // resetTextEditor() is called in constructor
        assertEquals("existing content", conflict.getEditedExistingVersion());
        assertEquals("uploaded content", conflict.getEditedUploadedVersion());
    }

    @Test
    public void testConstructorDefaultNotFixed() {
        assertFalse(conflict.isFixed());
    }

    @Test
    public void testDefaultDiffMode() {
        assertEquals(PluginInstallConflict.SHOW_OLD_AND_NEW_FILE, conflict.getDiffMode());
    }

    @Test
    public void testDefaultConflictsMode() {
        assertEquals(PluginInstallConflict.EDIT_EXISTING_FILE, conflict.getConflictsMode());
    }

    // --- getFileName ---

    @Test
    public void testGetFileNameWithPath() {
        assertEquals("plugin_test.xml", conflict.getFileName());
    }

    @Test
    public void testGetFileNameWithoutPath() {
        conflict.setPath("standalone.xml");
        assertEquals("standalone.xml", conflict.getFileName());
    }

    // --- resetTextEditor ---

    @Test
    public void testResetTextEditorRestoresOriginalVersions() {
        conflict.setEditedExistingVersion("modified existing");
        conflict.setEditedUploadedVersion("modified uploaded");
        conflict.resetTextEditor();
        assertEquals("existing content", conflict.getEditedExistingVersion());
        assertEquals("uploaded content", conflict.getEditedUploadedVersion());
    }

    // --- getCurrentVersion / setCurrentVersion ---

    @Test
    public void testGetCurrentVersionInEditExistingMode() {
        conflict.setConflictsMode(PluginInstallConflict.EDIT_EXISTING_FILE);
        assertEquals("existing content", conflict.getCurrentVersion());
    }

    @Test
    public void testGetCurrentVersionInOtherMode() {
        conflict.setConflictsMode(PluginInstallConflict.SHOW_OLD_AND_NEW_FILE);
        assertEquals("uploaded content", conflict.getCurrentVersion());
    }

    @Test
    public void testSetCurrentVersionInEditExistingMode() {
        conflict.setConflictsMode(PluginInstallConflict.EDIT_EXISTING_FILE);
        conflict.setCurrentVersion("new existing content");
        assertEquals("new existing content", conflict.getEditedExistingVersion());
        assertEquals("uploaded content", conflict.getEditedUploadedVersion());
    }

    @Test
    public void testSetCurrentVersionInOtherMode() {
        conflict.setConflictsMode(PluginInstallConflict.SHOW_OLD_AND_NEW_FILE);
        conflict.setCurrentVersion("new uploaded content");
        assertEquals("existing content", conflict.getEditedExistingVersion());
        assertEquals("new uploaded content", conflict.getEditedUploadedVersion());
    }

    // --- fixConflict / isFixed ---

    @Test
    public void testFixConflict() {
        assertFalse(conflict.isFixed());
        conflict.fixConflict();
        assertTrue(conflict.isFixed());
    }

    // --- ResolveTactic enum ---

    @Test
    public void testResolveTacticValues() {
        assertEquals(4, ResolveTactic.values().length);
        assertNotNull(ResolveTactic.unknown);
        assertNotNull(ResolveTactic.useMaintainer);
        assertNotNull(ResolveTactic.useCurrent);
        assertNotNull(ResolveTactic.editedVersion);
    }

    // --- string constants ---

    @Test
    public void testStringConstants() {
        assertEquals("show_old_and_new_file", PluginInstallConflict.SHOW_OLD_AND_NEW_FILE);
        assertEquals("show_default_and_custom_file", PluginInstallConflict.SHOW_DEFAULT_AND_CUSTOM_FILE);
        assertEquals("edit_existing_file", PluginInstallConflict.EDIT_EXISTING_FILE);
    }
}
