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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.persistence.managers.IManager;

public class HitSizeCacheTest extends AbstractTest {

    private static final long TIME_TO_LIVE = 60_000;
    private static final long THRESHOLD = 1_000;

    private final AtomicLong now = new AtomicLong(1_000_000);

    /**
     * manager whose count takes the given time and returns the given number
     */
    private IManager manager(long countDurationMillis, int hits) throws Exception {
        IManager manager = Mockito.mock(IManager.class);
        Mockito.when(manager.getHitSize(Mockito.any(), Mockito.any(), Mockito.any())).thenAnswer(invocation -> {
            now.addAndGet(countDurationMillis);
            return hits;
        });
        return manager;
    }

    private HitSizeCache cache() {
        return new HitSizeCache(TIME_TO_LIVE, THRESHOLD, now::get);
    }

    @Test
    public void testSlowCountIsReused() throws Exception {
        IManager manager = manager(3_000, 42);
        HitSizeCache cache = cache();

        assertEquals(42, cache.getHitSize(manager, "prozesse.titel", "WHERE x", null));
        // another sort order of the same list
        assertEquals(42, cache.getHitSize(manager, "prozesse.ProzesseID desc", "WHERE x", null));

        Mockito.verify(manager, Mockito.times(1)).getHitSize(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testFastCountIsAlwaysExact() throws Exception {
        IManager manager = manager(10, 42);
        HitSizeCache cache = cache();

        cache.getHitSize(manager, null, "WHERE x", null);
        cache.getHitSize(manager, null, "WHERE x", null);

        Mockito.verify(manager, Mockito.times(2)).getHitSize(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testExpiredCountIsCountedAgain() throws Exception {
        IManager manager = manager(3_000, 42);
        HitSizeCache cache = cache();

        cache.getHitSize(manager, null, "WHERE x", null);
        now.addAndGet(TIME_TO_LIVE + 1);
        cache.getHitSize(manager, null, "WHERE x", null);

        Mockito.verify(manager, Mockito.times(2)).getHitSize(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testCountsOfOtherFiltersAndInstitutionsAreNotShared() throws Exception {
        IManager manager = manager(3_000, 42);
        HitSizeCache cache = cache();
        Institution institution = new Institution();
        institution.setId(3);

        cache.getHitSize(manager, null, "WHERE x", null);
        cache.getHitSize(manager, null, "WHERE y", null);
        cache.getHitSize(manager, null, "WHERE x", institution);

        Mockito.verify(manager, Mockito.times(3)).getHitSize(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testCountsOfOtherManagersAreNotShared() throws Exception {
        IManager first = manager(3_000, 42);
        IManager second = new OtherManager();
        HitSizeCache cache = cache();

        cache.getHitSize(first, null, "WHERE x", null);

        assertEquals(7, cache.getHitSize(second, null, "WHERE x", null));
    }

    @Test
    public void testDisabledCacheAlwaysCounts() throws Exception {
        IManager manager = manager(3_000, 42);
        HitSizeCache cache = new HitSizeCache(0, THRESHOLD, now::get);

        cache.getHitSize(manager, null, "WHERE x", null);
        cache.getHitSize(manager, null, "WHERE x", null);

        Mockito.verify(manager, Mockito.times(2)).getHitSize(Mockito.any(), Mockito.any(), Mockito.any());
    }

    /**
     * a second manager class, counting 7 hits
     */
    private static class OtherManager implements IManager {
        private static final long serialVersionUID = 1L;

        @Override
        public int getHitSize(String order, String filter, Institution institution) {
            return 7;
        }

        @Override
        public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution) {
            return new ArrayList<>();
        }

        @Override
        public List<Integer> getIdList(String order, String filter, Institution institution) {
            return new ArrayList<>();
        }
    }
}
