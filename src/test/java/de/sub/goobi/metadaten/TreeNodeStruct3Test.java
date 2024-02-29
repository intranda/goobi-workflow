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
package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.tuple.MutablePair;
import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.mock.MockProcess;
import ugh.dl.DocStruct;

public class TreeNodeStruct3Test extends AbstractTest {

    private Process process;
    private DocStruct docstruct;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {

        process = MockProcess.createProcess();
        docstruct = process.readMetadataFile().getDigitalDocument().getLogicalDocStruct();
    }

    @Test
    public void testConstructor1() {
        TreeNodeStruct3 tree = new TreeNodeStruct3();
        assertNotNull(tree);
    }

    @Test
    public void testConstructor2() {
        TreeNodeStruct3 tree = new TreeNodeStruct3(true, "label", "id");
        assertNotNull(tree);
    }

    @Test
    public void testConstructor3() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
    }

    @Test
    public void testMaintitle() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
        tree.setMainTitle("title");
        assertEquals("title", tree.getMainTitle());
    }

    @Test
    public void testFirstImage() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
        tree.setFirstImage(new MutablePair<>("aaa", "bbb"));
        assertEquals("aaa", tree.getFirstImage().getLeft());
        assertEquals("bbb", tree.getFirstImage().getRight());
    }

    @Test
    public void testLastImage() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
        tree.setLastImage(new MutablePair<>("aaa", "bbb"));
        assertEquals("aaa", tree.getLastImage().getLeft());
        assertEquals("bbb", tree.getLastImage().getRight());
    }

    @Test
    public void testStruct() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
        tree.setStruct(docstruct);
        assertEquals(docstruct, tree.getStruct());
    }

    @Test
    public void testDescription() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
        tree.setDescription("fixture");
        assertEquals("fixture", tree.getDescription());
    }

    @Test
    public void testEinfuegenErlaubt() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
        assertTrue(tree.isEinfuegenErlaubt());
        tree.setEinfuegenErlaubt(false);
        assertFalse(tree.isEinfuegenErlaubt());
    }
}
