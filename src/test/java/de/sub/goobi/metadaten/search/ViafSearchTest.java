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
package de.sub.goobi.metadaten.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

public class ViafSearchTest extends AbstractTest {

    private ViafSearch viafSearch;

    @BeforeEach
    public void setUp() {
        viafSearch = new ViafSearch();
    }

    @Test
    public void testConstructor() {
        assertNotNull(viafSearch);
        assertNotNull(viafSearch.getSearchSources());
        assertNotNull(viafSearch.getSearchFields());
        assertNotNull(viafSearch.getRelations());
        assertNotNull(viafSearch.getMainTagList());
        assertNotNull(viafSearch.getVisibleTagList());
        assertFalse(viafSearch.getSearchSources().isEmpty());
        assertFalse(viafSearch.getSearchFields().isEmpty());
        assertFalse(viafSearch.getRelations().isEmpty());
    }

    @Test
    public void testSetSourceSixCharTag() {
        // tag length > 5: mainTag=100, ind1=_, ind2=_, subCode=abc
        viafSearch.setSource("100__a");
        assertFalse(viafSearch.getMainTagList().isEmpty());
    }

    @Test
    public void testSetSourceWithSemicolon() {
        // both tokens have length > 5 → both get added
        viafSearch.setSource("100__a;200__b");
        assertEquals(2, viafSearch.getMainTagList().size());
    }

    @Test
    public void testSetSourceWithLabel() {
        // format: tag=label, tag is 6-char so matches > 5
        viafSearch.setSource("100__a=Personal Name");
        assertFalse(viafSearch.getMainTagList().isEmpty());
    }

    @Test
    public void testSetSourceShortTag() {
        // 3-char tag has no subfield code → TagDescription with null subCode
        viafSearch.setSource("100");
        assertFalse(viafSearch.getMainTagList().isEmpty());
    }

    @Test
    public void testSetSourceInvalidTagIgnored() {
        // a 4-char tag doesn't match either branch (length 3 or >5) → returns null, not added
        viafSearch.setSource("1234");
        assertTrue(viafSearch.getMainTagList().isEmpty());
    }

    @Test
    public void testSetFieldSimple() {
        // tag length > 5 → valid
        viafSearch.setField("245__a");
        assertFalse(viafSearch.getVisibleTagList().isEmpty());
    }

    @Test
    public void testSetFieldWithSemicolon() {
        viafSearch.setField("245__a;400__b");
        assertEquals(2, viafSearch.getVisibleTagList().size());
    }

    @Test
    public void testClearResults() {
        viafSearch.setRecords(null);
        viafSearch.clearResults();
        assertNull(viafSearch.getRecords());
        assertNotNull(viafSearch.getViafSearchRequest());
    }

    @Test
    public void testSortingDefaultTrue() {
        assertTrue(viafSearch.isSorting());
    }

    @Test
    public void testSetSorting() {
        viafSearch.setSorting(false);
        assertFalse(viafSearch.isSorting());
    }
}
