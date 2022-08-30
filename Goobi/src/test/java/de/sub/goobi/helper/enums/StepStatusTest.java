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
package de.sub.goobi.helper.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;

public class StepStatusTest extends AbstractTest {

    @Test
    public void testStepStatusGetValue() {
        assertTrue(StepStatus.LOCKED.getValue() == 0);
    }

    // TODO fails in eclipse, but not in jenkins/mvn
    // Can't find bundle for base name messages, locale en
    @Test
    public void testStepStatusGetTitle() {
        assertEquals(Helper.getTranslation("statusGesperrt"), StepStatus.LOCKED.getTitle());
        assertEquals(Helper.getTranslation("statusOffen"), StepStatus.OPEN.getTitle());
        assertEquals(Helper.getTranslation("statusInBearbeitung"), StepStatus.INWORK.getTitle());
        assertEquals(Helper.getTranslation("statusAbgeschlossen"), StepStatus.DONE.getTitle());
    }

    @Test
    public void testStepStatusGetSmallImagePath() {
        assertEquals("images/status/red_10.gif", StepStatus.LOCKED.getSmallImagePath());
        assertEquals("images/status/orange_10.gif", StepStatus.OPEN.getSmallImagePath());
        assertEquals("images/status/yellow_10.gif", StepStatus.INWORK.getSmallImagePath());
        assertEquals("images/status/green_10.gif", StepStatus.DONE.getSmallImagePath());
    }

    @Test
    public void testStepStatusGetBigImagePath() {
        assertEquals("images/status/red_15a.gif", StepStatus.LOCKED.getBigImagePath());
        assertEquals("images/status/orange_15a.gif", StepStatus.OPEN.getBigImagePath());
        assertEquals("images/status/yellow_15a.gif", StepStatus.INWORK.getBigImagePath());
        assertEquals("images/status/green_15a.gif", StepStatus.DONE.getBigImagePath());
    }

    @Test
    public void testStepStatusGetStatusFromValue() {
        assertEquals(StepStatus.LOCKED, StepStatus.getStatusFromValue(0));
        assertEquals(StepStatus.OPEN, StepStatus.getStatusFromValue(1));
        assertEquals(StepStatus.INWORK, StepStatus.getStatusFromValue(2));
        assertEquals(StepStatus.DONE, StepStatus.getStatusFromValue(3));
        assertEquals(StepStatus.LOCKED, StepStatus.getStatusFromValue(666));
    }

    @Test
    public void testStepStatusGetSearchString() {
        assertEquals("steplocked", StepStatus.LOCKED.getSearchString());
        assertEquals("stepopen", StepStatus.OPEN.getSearchString());
        assertEquals("stepinwork", StepStatus.INWORK.getSearchString());
        assertEquals("stepdone", StepStatus.DONE.getSearchString());

    }
}
