package org.goobi.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.mock.MockProcess;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;

public class ProcessTest extends AbstractTest {

    @Before
    public void setUp() {

    }

    @Test
    public void testConstructor() {
        Process process = new Process();
        assertNotNull(process);
    }

    @Test
    public void testReadMetadata() throws Exception {
        Process process = MockProcess.createProcess();
        Fileformat ff = process.readMetadataFile();
        assertNotNull(ff);
        DocStruct logical = ff.getDigitalDocument().getLogicalDocStruct();
        assertEquals("Monograph", logical.getType().getName());
    }

    @Test
    public void testId() throws Exception {
        Process process = new Process();
        assertNull(process.getId());
        process.setId(1);
        assertEquals(1, process.getId().intValue());
    }

    @Test
    public void testTitle() throws Exception {
        Process process = new Process();
        assertEquals("", process.getTitel());
        process.setTitel("fixture");
        assertEquals("fixture", process.getTitel());
    }

    @Test
    public void testIssueName() throws Exception {
        Process process = new Process();
        assertNull(process.getAusgabename());
        process.setAusgabename("fixture");
        assertEquals("fixture", process.getAusgabename());
    }

    @Test
    public void testTemplate() throws Exception {
        Process process = new Process();
        assertFalse(process.isIstTemplate());
        process.setIstTemplate(true);
        assertTrue(process.isIstTemplate());
        process.setIstTemplate(false);
        assertFalse(process.isIstTemplate());
    }

    @Test
    public void testDisplayInSelection() throws Exception {
        Process process = new Process();
        assertFalse(process.isInAuswahllisteAnzeigen());
        process.setInAuswahllisteAnzeigen(true);
        assertTrue(process.isInAuswahllisteAnzeigen());
        process.setInAuswahllisteAnzeigen(false);
        assertFalse(process.isInAuswahllisteAnzeigen());
    }

    @Test
    public void testProject() throws Exception {
        Process process = new Process();
        Project project = new Project();
        project.setTitel("fixture");
        assertNull(process.getProjekt());
        process.setProjekt(project);
        assertEquals("fixture", process.getProjekt().getTitel());
    }

    @Test
    public void testCreationDate() throws Exception {
        Process process = new Process();
        assertNotNull(process.getErstellungsdatum());
        assertNotNull(process.getErstellungsdatumAsString());
    }

}
