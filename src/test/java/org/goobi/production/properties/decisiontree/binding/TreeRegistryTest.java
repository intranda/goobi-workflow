package org.goobi.production.properties.decisiontree.binding;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - https://goobi.io - https://www.intranda.com - https://github.com/intranda/goobi-workflow
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TreeRegistryTest {

    @TempDir
    Path dir;

    private static String file(String... trees) {
        return "<decisionTrees>" + String.join("", trees) + "</decisionTrees>";
    }

    private static String tree(String id, String value) {
        return "<decisionTree id=\"" + id + "\"><question label=\"Q\"><answer label=\"Yes\"><field name=\"A\">" + value
                + "</field><result id=\"r1\"/></answer><answer label=\"No\"><field name=\"A\">2</field><result id=\"r2\"/></answer></question></decisionTree>";
    }

    private Path write(String content, long millis) throws Exception {
        Path path = dir.resolve("goobi_decisionTrees.xml");
        Files.writeString(path, content, StandardCharsets.UTF_8);
        Files.setLastModifiedTime(path, FileTime.fromMillis(millis));
        return path;
    }

    @Test
    public void treesAreFoundById() throws Exception {
        TreeRegistry registry = new TreeRegistry(write(file(tree("one", "1"), tree("two", "1")), 1000));
        assertTrue(registry.tree("one").isPresent());
        assertTrue(registry.tree("two").isPresent());
        assertFalse(registry.tree("three").isPresent());
        assertTrue(registry.problems("one").isEmpty());
    }

    @Test
    public void unknownTreeIsExplained() throws Exception {
        TreeRegistry registry = new TreeRegistry(write(file(tree("one", "1")), 1000));
        List<String> problems = registry.problems("three");
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("three"));
    }

    @Test
    public void rejectedTreeReportsItsErrorsAndTheOthersStayUsable() throws Exception {
        String broken = "<decisionTree id=\"bad\"><question label=\"Q\"><answer label=\"x\"/></question></decisionTree>";
        TreeRegistry registry = new TreeRegistry(write(file(broken, tree("good", "1")), 1000));
        assertTrue(registry.tree("good").isPresent());
        assertFalse(registry.tree("bad").isPresent());
        assertFalse(registry.problems("bad").isEmpty());
        assertTrue(registry.problems("bad").get(0).contains("bad"));
    }

    @Test
    public void changedFileIsReloaded() throws Exception {
        Path path = write(file(tree("one", "1")), 1000);
        TreeRegistry registry = new TreeRegistry(path);
        assertEquals("1", registry.tree("one").orElseThrow().root().answers().get(0).fields().get("A"));
        write(file(tree("one", "changed")), 2000);
        assertEquals("changed", registry.tree("one").orElseThrow().root().answers().get(0).fields().get("A"));
    }

    @Test
    public void unchangedFileIsNotParsedAgain() throws Exception {
        TreeRegistry registry = new TreeRegistry(write(file(tree("one", "1")), 1000));
        assertTrue(registry.tree("one").get() == registry.tree("one").get(), "the same definition is returned while the file is unchanged");
    }

    @Test
    public void missingFileHasNoTreesAndAnError() {
        TreeRegistry registry = new TreeRegistry(dir.resolve("missing.xml"));
        assertFalse(registry.tree("one").isPresent());
        assertTrue(registry.problems("one").get(0).contains("cannot be read"));
    }

    @Test
    public void brokenFileHasNoTreesAndAnErrorAndRecoversWhenFixed() throws Exception {
        Path path = write("<decisionTrees>", 1000);
        TreeRegistry registry = new TreeRegistry(path);
        assertFalse(registry.tree("one").isPresent());
        assertFalse(registry.problems("one").isEmpty());
        write(file(tree("one", "1")), 2000);
        assertTrue(registry.tree("one").isPresent());
        assertTrue(registry.problems("one").isEmpty());
    }

    @Test
    public void fileThatAppearsLaterIsLoaded() throws Exception {
        Path path = dir.resolve("goobi_decisionTrees.xml");
        TreeRegistry registry = new TreeRegistry(path);
        assertFalse(registry.tree("one").isPresent());
        write(file(tree("one", "1")), 3000);
        assertTrue(registry.tree("one").isPresent());
    }
}
