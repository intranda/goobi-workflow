package org.goobi.production.properties;

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
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AccessConditionTest {

    @Test
    public void testGetAccessConditionByNameWrite() {
        assertEquals(AccessCondition.WRITE, AccessCondition.getAccessConditionByName("write"));
    }

    @Test
    public void testGetAccessConditionByNameWriteUpperCase() {
        assertEquals(AccessCondition.WRITE, AccessCondition.getAccessConditionByName("WRITE"));
    }

    @Test
    public void testGetAccessConditionByNameWriteMixedCase() {
        assertEquals(AccessCondition.WRITE, AccessCondition.getAccessConditionByName("Write"));
    }

    @Test
    public void testGetAccessConditionByNameWriteRequired() {
        assertEquals(AccessCondition.WRITEREQUIRED, AccessCondition.getAccessConditionByName("writerequired"));
    }

    @Test
    public void testGetAccessConditionByNameWriteRequiredUpperCase() {
        assertEquals(AccessCondition.WRITEREQUIRED, AccessCondition.getAccessConditionByName("WRITEREQUIRED"));
    }

    @Test
    public void testGetAccessConditionByNameRead() {
        assertEquals(AccessCondition.READ, AccessCondition.getAccessConditionByName("read"));
    }

    @Test
    public void testGetAccessConditionByNameUnknownDefaultsToRead() {
        assertEquals(AccessCondition.READ, AccessCondition.getAccessConditionByName("unknown"));
    }

    @Test
    public void testGetAccessConditionByNameEmptyDefaultsToRead() {
        assertEquals(AccessCondition.READ, AccessCondition.getAccessConditionByName(""));
    }

    @Test
    public void testEnumValuesCount() {
        assertEquals(3, AccessCondition.values().length);
    }
}
