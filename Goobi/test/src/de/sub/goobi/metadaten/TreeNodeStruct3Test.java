package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    public void testIdentifier() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
        tree.setIdentifier("1");
        assertEquals("1", tree.getIdentifier());
    }

    @Test
    public void testMaintitle() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
        tree.setMainTitle("title");
        assertEquals("title", tree.getMainTitle());
    }

    @Test
    public void testPpnDigital() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
        tree.setPpnDigital("123");
        assertEquals("123", tree.getPpnDigital());
    }

    @Test
    public void testFirstImage() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
        tree.setFirstImage("fixture");
        assertEquals("fixture", tree.getFirstImage());
    }

    @Test
    public void testLastImage() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
        tree.setLastImage("fixture");
        assertEquals("fixture", tree.getLastImage());
    }

    @Test
    public void testStruct() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
        tree.setStruct(docstruct);
        assertEquals(docstruct, tree.getStruct());
    }

    @Test
    public void testZblNummer() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
        tree.setZblNummer("fixture");
        assertEquals("fixture", tree.getZblNummer());
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

    @Test
    public void testZblSeiten() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
        tree.setZblSeiten("fixture");
        assertEquals("fixture", tree.getZblSeiten());
    }

    @Test
    public void testDateIssued() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
        tree.setDateIssued("fixture");
        assertEquals("fixture", tree.getDateIssued());
    }

    @Test
    public void testPartNumber() {
        TreeNodeStruct3 tree = new TreeNodeStruct3("label", docstruct);
        assertNotNull(tree);
        tree.setPartNumber("fixture");
        assertEquals("fixture", tree.getPartNumber());
    }
}
