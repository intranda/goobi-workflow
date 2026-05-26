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
 */
package org.goobi.managedbeans;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LoginAttemptTrackerTest {

    @Test
    public void testNotBlockedInitially() {
        LoginAttemptTracker tracker = new LoginAttemptTracker(() -> 0L);
        assertFalse(tracker.isBlocked("192.168.1.1"), "IP must not be blocked without any failed attempts");
    }

    @Test
    public void testNotBlockedAfterTwoFailures() {
        LoginAttemptTracker tracker = new LoginAttemptTracker(() -> 0L);
        tracker.recordFailure("192.168.1.1");
        tracker.recordFailure("192.168.1.1");
        assertFalse(tracker.isBlocked("192.168.1.1"), "IP must not be blocked after only 2 failed attempts");
    }

    @Test
    public void testBlockedAfterThreeFailures() {
        LoginAttemptTracker tracker = new LoginAttemptTracker(() -> 0L);
        tracker.recordFailure("192.168.1.1");
        tracker.recordFailure("192.168.1.1");
        tracker.recordFailure("192.168.1.1");
        assertTrue(tracker.isBlocked("192.168.1.1"), "IP must be blocked after 3 failed attempts");
    }

    @Test
    public void testBlockExpiresAfterOneMinute() {
        long[] now = { 0L };
        LoginAttemptTracker tracker = new LoginAttemptTracker(() -> now[0]);
        tracker.recordFailure("192.168.1.1");
        tracker.recordFailure("192.168.1.1");
        tracker.recordFailure("192.168.1.1");

        now[0] = 60_001L;

        assertFalse(tracker.isBlocked("192.168.1.1"), "IP block must expire after 1 minute");
    }

    @Test
    public void testBlockStillActiveBeforeExpiry() {
        long[] now = { 0L };
        LoginAttemptTracker tracker = new LoginAttemptTracker(() -> now[0]);
        tracker.recordFailure("192.168.1.1");
        tracker.recordFailure("192.168.1.1");
        tracker.recordFailure("192.168.1.1");

        now[0] = 59_999L;

        assertTrue(tracker.isBlocked("192.168.1.1"), "IP must still be blocked before 1-minute expiry");
    }

    @Test
    public void testSuccessResetsCounter() {
        LoginAttemptTracker tracker = new LoginAttemptTracker(() -> 0L);
        tracker.recordFailure("192.168.1.1");
        tracker.recordFailure("192.168.1.1");
        tracker.recordSuccess("192.168.1.1");
        tracker.recordFailure("192.168.1.1");
        tracker.recordFailure("192.168.1.1");
        assertFalse(tracker.isBlocked("192.168.1.1"), "successful login must reset the failure counter");
    }

    @Test
    public void testDifferentIpsAreIndependent() {
        LoginAttemptTracker tracker = new LoginAttemptTracker(() -> 0L);
        tracker.recordFailure("192.168.1.1");
        tracker.recordFailure("192.168.1.1");
        tracker.recordFailure("192.168.1.1");
        assertFalse(tracker.isBlocked("10.0.0.1"), "blocking one IP must not affect a different IP");
    }
}
