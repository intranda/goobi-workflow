package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class MetadatenSperrungTest extends AbstractTest {

    @Test
    public void testMetadatenSperrung() {
        MetadatenSperrung ms = new MetadatenSperrung();
        assertNotNull(ms);
    }

    @Test
    public void testSetFree() {
        MetadatenSperrung ms = new MetadatenSperrung();
        // process id is not in list
        ms.setFree(1);

        // add process id to list
        ms.setLocked(1, "1");
        ms.setFree(1);
    }

    @Test
    public void testIsLocked() {
        MetadatenSperrung ms = new MetadatenSperrung();
        ms.setFree(1);
        assertFalse(MetadatenSperrung.isLocked(1));

        ms.setLocked(1, "user");
        assertTrue(MetadatenSperrung.isLocked(1));

    }

    @Test
    public void testAlleBenutzerSperrungenAufheben() {
        MetadatenSperrung ms = new MetadatenSperrung();
        ms.setLocked(1, "1");
        ms.alleBenutzerSperrungenAufheben(1);
        assertFalse(MetadatenSperrung.isLocked(1));
    }

    @Test
    public void testGetLockBenutzer() {
        MetadatenSperrung ms = new MetadatenSperrung();
        ms.setLocked(1, "1");

        String fixture = ms.getLockBenutzer(1);
        assertEquals("1", fixture);
    }

    @Test
    public void testUnlockProcess() {
        MetadatenSperrung ms = new MetadatenSperrung();
        ms.setLocked(1, "1");
        assertTrue(MetadatenSperrung.isLocked(1));

        MetadatenSperrung.UnlockProcess(1);
        assertFalse(MetadatenSperrung.isLocked(1));

    }

    @Test
    public void testGetLockSekunden() throws InterruptedException {
        MetadatenSperrung ms = new MetadatenSperrung();
        ms.setLocked(1, "1");
        long time = ms.getLockSekunden(2);
        assertEquals(0l, time);
        Thread.sleep(1000);
        time = ms.getLockSekunden(1);
        assertEquals(1l, time);
    }
}
