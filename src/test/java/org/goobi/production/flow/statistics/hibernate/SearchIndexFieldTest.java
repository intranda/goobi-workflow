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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class SearchIndexFieldTest {

    @Test
    public void testConstructorSetsIndexName() {
        SearchIndexField field = new SearchIndexField("TitleDocMain", Arrays.asList("title", "TitleDocMain"));
        assertEquals("TitleDocMain", field.getIndexName());
    }

    @Test
    public void testConstructorSetsMetadataList() {
        List<String> metadata = Arrays.asList("Author", "TitleDocMain");
        SearchIndexField field = new SearchIndexField("Author", metadata);
        assertEquals(metadata, field.getMetadataList());
    }

    @Test
    public void testSetIndexName() {
        SearchIndexField field = new SearchIndexField("old", Arrays.asList());
        field.setIndexName("new");
        assertEquals("new", field.getIndexName());
    }

    @Test
    public void testSetMetadataList() {
        SearchIndexField field = new SearchIndexField("idx", Arrays.asList("a"));
        List<String> newList = Arrays.asList("x", "y", "z");
        field.setMetadataList(newList);
        assertEquals(3, field.getMetadataList().size());
    }

    @Test
    public void testConstructorWithEmptyList() {
        SearchIndexField field = new SearchIndexField("empty", Arrays.asList());
        assertNotNull(field.getMetadataList());
        assertEquals(0, field.getMetadataList().size());
    }
}
