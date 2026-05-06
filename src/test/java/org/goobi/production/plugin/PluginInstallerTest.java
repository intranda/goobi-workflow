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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.goobi.production.plugin.PluginInstallConflict.ResolveTactic;
import org.junit.jupiter.api.Test;

public class PluginInstallerTest {

    @Test
    public void testEndingWhiteListContainsJs() {
        assertTrue(PluginInstaller.ENDING_WHITE_LIST.contains(".js"));
    }

    @Test
    public void testEndingWhiteListContainsCss() {
        assertTrue(PluginInstaller.ENDING_WHITE_LIST.contains(".css"));
    }

    @Test
    public void testEndingWhiteListContainsJar() {
        assertTrue(PluginInstaller.ENDING_WHITE_LIST.contains(".jar"));
    }

    @Test
    public void testEndingWhiteListSize() {
        assertEquals(3, PluginInstaller.ENDING_WHITE_LIST.size());
    }

    @Test
    public void testPathBlackListContainsPomXml() {
        assertTrue(PluginInstaller.PATH_BLACK_LIST.contains("pom.xml"));
    }

    @Test
    public void testSetNumbersForAllConflictsAssignsOneBasedNumbers() {
        PluginInstallConflict c1 = new PluginInstallConflict("path1", ResolveTactic.unknown, "e1", "u1", "a1");
        PluginInstallConflict c2 = new PluginInstallConflict("path2", ResolveTactic.unknown, "e2", "u2", "a2");
        PluginInstallConflict c3 = new PluginInstallConflict("path3", ResolveTactic.unknown, "e3", "u3", "a3");

        PluginInstaller.setNumbersForAllConflicts(new Object[] { c1, c2, c3 });

        assertEquals(1, c1.getNumber());
        assertEquals(2, c2.getNumber());
        assertEquals(3, c3.getNumber());
    }

    @Test
    public void testSetNumbersForAllConflictsSingleElement() {
        PluginInstallConflict c1 = new PluginInstallConflict("path1", ResolveTactic.unknown, "e", "u", "a");
        PluginInstaller.setNumbersForAllConflicts(new Object[] { c1 });
        assertEquals(1, c1.getNumber());
    }
}
