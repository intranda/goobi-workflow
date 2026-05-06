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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.goobi.production.cli.helper.StringPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ShowStepConditionTest {

    private ShowStepCondition condition;

    @BeforeEach
    public void setUp() {
        condition = new ShowStepCondition();
    }

    @Test
    public void testConstructor() {
        assertNotNull(condition);
    }

    @Test
    public void testDefaultAccessConditionIsRead() {
        assertEquals(AccessCondition.READ, condition.getAccessCondition());
    }

    @Test
    public void testDefaultDuplicationIsFalse() {
        assertFalse(condition.isDuplication());
    }

    @Test
    public void testDefaultDisplayConditionIsEmptyList() {
        assertNotNull(condition.getDisplayCondition());
        assertEquals(0, condition.getDisplayCondition().size());
    }

    @Test
    public void testDefaultNameIsNull() {
        assertNull(condition.getName());
    }

    @Test
    public void testSetName() {
        condition.setName("Scannen");
        assertEquals("Scannen", condition.getName());
    }

    @Test
    public void testSetAccessCondition() {
        condition.setAccessCondition(AccessCondition.WRITE);
        assertEquals(AccessCondition.WRITE, condition.getAccessCondition());
    }

    @Test
    public void testSetDuplication() {
        condition.setDuplication(true);
        assertEquals(true, condition.isDuplication());
    }

    @Test
    public void testAddDisplayCondition() {
        StringPair pair = new StringPair();
        pair.setOne("key");
        pair.setTwo("value");
        condition.getDisplayCondition().add(pair);
        assertEquals(1, condition.getDisplayCondition().size());
        assertEquals("key", condition.getDisplayCondition().get(0).getOne());
        assertEquals("value", condition.getDisplayCondition().get(0).getTwo());
    }
}
