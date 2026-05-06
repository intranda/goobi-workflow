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
package org.goobi.production.flow.statistics.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StepDateFilterGroupTest {

    private StepDateFilterGroup group;

    @BeforeEach
    public void setUp() {
        group = new StepDateFilterGroup();
    }

    @Test
    public void testDefaultGroupIdIsZero() {
        assertEquals(0, group.getGroupId());
    }

    @Test
    public void testDefaultStepFilterPresentIsFalse() {
        assertFalse(group.isStepFilterPresent());
    }

    @Test
    public void testDefaultDateFilterIsEmptyList() {
        assertNotNull(group.getDateFilter());
        assertTrue(group.getDateFilter().isEmpty());
    }

    @Test
    public void testSetGroupId() {
        group.setGroupId(3);
        assertEquals(3, group.getGroupId());
    }

    @Test
    public void testSetStepFilterPresent() {
        group.setStepFilterPresent(true);
        assertTrue(group.isStepFilterPresent());
    }

    @Test
    public void testAddFilter() {
        group.addFilter("date > '2024-01-01'");
        assertEquals(1, group.getDateFilter().size());
        assertEquals("date > '2024-01-01'", group.getDateFilter().get(0));
    }

    @Test
    public void testAddMultipleFilters() {
        group.addFilter("filter1");
        group.addFilter("filter2");
        assertEquals(2, group.getDateFilter().size());
    }
}
