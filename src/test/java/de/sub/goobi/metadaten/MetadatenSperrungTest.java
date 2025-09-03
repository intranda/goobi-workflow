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

        MetadatenSperrung.unlockProcess(1);
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
