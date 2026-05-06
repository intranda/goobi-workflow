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
package org.goobi.beans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RulesetTest {

    private Ruleset ruleset;

    @BeforeEach
    public void setUp() {
        ruleset = new Ruleset();
    }

    @Test
    public void testDefaultIdIsNull() {
        assertNull(ruleset.getId());
    }

    @Test
    public void testSetAndGetId() {
        ruleset.setId(3);
        assertEquals(Integer.valueOf(3), ruleset.getId());
    }

    @Test
    public void testSetAndGetTitel() {
        ruleset.setTitel("MODS");
        assertEquals("MODS", ruleset.getTitel());
    }

    @Test
    public void testSetAndGetDatei() {
        ruleset.setDatei("ruleset.xml");
        assertEquals("ruleset.xml", ruleset.getDatei());
    }

    @Test
    public void testDefaultOrderMetadataByRulesetIsFalse() {
        assertFalse(ruleset.isOrderMetadataByRuleset());
    }

    @Test
    public void testSetOrderMetadataByRuleset() {
        ruleset.setOrderMetadataByRuleset(true);
        assertTrue(ruleset.isOrderMetadataByRuleset());
    }

    @Test
    public void testLazyLoadDoesNotThrow() {
        ruleset.lazyLoad(); // must not throw
    }
}
