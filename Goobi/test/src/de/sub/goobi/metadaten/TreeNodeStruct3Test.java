package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.solr.common.util.Pair;
import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.sub.goobi.mock.MockProcess;
import ugh.dl.DocStruct;

public class TreeNodeStruct3Test {

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
        tree.setFirstImage(new Pair("aaa", "bbb"));
        assertEquals("aaa", tree.getFirstImage().first());
        assertEquals("bbb", tree.getFirstImage().first());
    }

    @Test
    public void testLastImage() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
        tree.setLastImage(new Pair("aaa", "bbb"));
        assertEquals("aaa", tree.getLastImage().first());
        assertEquals("bbb", tree.getLastImage().first());
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
