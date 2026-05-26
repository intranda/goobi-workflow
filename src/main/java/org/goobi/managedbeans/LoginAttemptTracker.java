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

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

public class LoginAttemptTracker {

    private static final int MAX_ATTEMPTS = 3;
    private static final long BLOCK_DURATION_MS = 60_000L;

    private static class AttemptRecord {
        int failures;
        long blockedAt;
    }

    private final ConcurrentHashMap<String, AttemptRecord> records = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    public LoginAttemptTracker(LongSupplier clock) {
        this.clock = clock;
    }

    public boolean isBlocked(String ip) {
        AttemptRecord record = records.get(ip);
        if (record == null || record.failures < MAX_ATTEMPTS) {
            return false;
        }
        return clock.getAsLong() - record.blockedAt < BLOCK_DURATION_MS;
    }

    public void recordFailure(String ip) {
        AttemptRecord record = records.computeIfAbsent(ip, k -> new AttemptRecord());
        synchronized (record) {
            record.failures++;
            if (record.failures >= MAX_ATTEMPTS) {
                record.blockedAt = clock.getAsLong();
            }
        }
    }

    public void recordSuccess(String ip) {
        records.remove(ip);
    }
}
