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
package org.goobi.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.enums.HistoryEventType;

public class HistoryEventTest extends AbstractTest {

    private static HistoryEvent event1;
    private static HistoryEvent event2;
    private static HistoryEvent event3;

    /**
     * event1 and event2 are equal and have the same hash code. event3 is different and has an other hash code.
     */
    @BeforeClass
    public static void setupHistoryEvents() {
        Date now = new Date();
        Process process = new Process();
        HistoryEventTest.event1 = new HistoryEvent(now, 1, "duplicated event", HistoryEventType.stepInWork, process);
        HistoryEventTest.event2 = new HistoryEvent(now, 1, "duplicated event", HistoryEventType.stepInWork, process);
        HistoryEventTest.event3 = new HistoryEvent(now, 2, "different event", HistoryEventType.stepInWork, new Process());
    }

    /**
     * This unit test tests whether the setter and getter for for HistoryEventType work. The HistoryEventType is internally stored via its integer
     * value, not directly as enum item.
     */
    @Test
    public void testHistoryEventType() {
        HistoryEvent event = new HistoryEvent(new Date(), 1, "test event", HistoryEventType.stepOpen, new Process());
        assertEquals(event.getHistoryType(), HistoryEventType.stepOpen);
        event.setHistoryType(HistoryEventType.stepLocked);
        assertEquals(event.getHistoryType(), HistoryEventType.stepLocked);
    }

    @Test
    public void testHashCode() {

        // Assert that event has the same hash code as itself
        assertEquals(HistoryEventTest.event1.hashCode(), HistoryEventTest.event1.hashCode());

        // Assert that event has the same hash code as an equal event
        assertEquals(HistoryEventTest.event1.hashCode(), HistoryEventTest.event2.hashCode());

        // Assert that event has not the same hash code with a different event
        assertNotEquals(HistoryEventTest.event1.hashCode(), HistoryEventTest.event3.hashCode());
    }

    @Test
    public void testEquals() {

        // Assert that event is equal to itself
        assertEquals(HistoryEventTest.event1, HistoryEventTest.event1);

        // Assert that equal events are equal
        assertEquals(HistoryEventTest.event1, HistoryEventTest.event2);

        // Assert that different events are not equal
        assertNotEquals(HistoryEventTest.event1, HistoryEventTest.event3);
    }

}
