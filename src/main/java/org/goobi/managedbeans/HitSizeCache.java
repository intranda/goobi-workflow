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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

import org.goobi.beans.Institution;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.IManager;

/**
 * Keeps the number of hits of expensive list queries for a short time.
 *
 * Counting the process or task list can take several seconds on large databases. The count is repeated whenever a list is opened again or sorted
 * differently. Counts that took longer than a threshold are kept for a configurable time, cheap counts are always exact.
 */
public final class HitSizeCache {

    private static final int MAX_ENTRIES = 1000;

    private static HitSizeCache instance;

    private final long timeToLiveMillis;
    private final long thresholdMillis;
    private final LongSupplier clock;
    private final Map<Key, Entry> entries = new ConcurrentHashMap<>();

    private record Key(String manager, String filter, Integer institution) {
    }

    private record Entry(int hits, long validUntil) {
    }

    HitSizeCache(long timeToLiveMillis, long thresholdMillis, LongSupplier clock) {
        this.timeToLiveMillis = timeToLiveMillis;
        this.thresholdMillis = thresholdMillis;
        this.clock = clock;
    }

    public static synchronized HitSizeCache getInstance() {
        if (instance == null) {
            ConfigurationHelper config = ConfigurationHelper.getInstance();
            instance = new HitSizeCache(config.getHitSizeCacheSeconds() * 1000L, config.getHitSizeCacheThresholdMillis(), System::currentTimeMillis);
        }
        return instance;
    }

    /**
     * Get the number of hits of a list. The sort order does not change the number, so it is not part of the cache key.
     */
    public int getHitSize(IManager manager, String order, String filter, Institution institution) throws DAOException {
        if (timeToLiveMillis <= 0) {
            return manager.getHitSize(order, filter, institution);
        }
        Key key = new Key(manager.getClass().getName(), Objects.toString(filter, ""), institution == null ? null : institution.getId());
        long now = clock.getAsLong();
        Entry entry = entries.get(key);
        if (entry != null && entry.validUntil() >= now) {
            return entry.hits();
        }
        int hits = manager.getHitSize(order, filter, institution);
        long finished = clock.getAsLong();
        if (finished - now >= thresholdMillis) {
            if (entries.size() >= MAX_ENTRIES) {
                entries.values().removeIf(e -> e.validUntil() < finished);
                if (entries.size() >= MAX_ENTRIES) {
                    entries.clear();
                }
            }
            entries.put(key, new Entry(hits, finished + timeToLiveMillis));
        } else {
            entries.remove(key);
        }
        return hits;
    }
}
