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
package org.goobi.production.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ImportConflictsTest {

    private ImportConflicts conflict;

    @BeforeEach
    public void setUp() {
        conflict = new ImportConflicts("id-001", "title", "goobiTitle", "productionTitle");
    }

    @Test
    public void testConstructorSetsStoreIdentifier() {
        assertEquals("id-001", conflict.getStoreidentifier());
    }

    @Test
    public void testConstructorSetsProperty() {
        assertEquals("title", conflict.getProperty());
    }

    @Test
    public void testConstructorSetsGoobiValue() {
        assertEquals("goobiTitle", conflict.getGoobiValue());
    }

    @Test
    public void testConstructorSetsProductionValue() {
        assertEquals("productionTitle", conflict.getProductionValue());
    }

    @Test
    public void testSetStoreIdentifier() {
        conflict.setStoreidentifier("id-999");
        assertEquals("id-999", conflict.getStoreidentifier());
    }

    @Test
    public void testSetProperty() {
        conflict.setProperty("author");
        assertEquals("author", conflict.getProperty());
    }

    @Test
    public void testSetGoobiValue() {
        conflict.setGoobiValue("newGoobiValue");
        assertEquals("newGoobiValue", conflict.getGoobiValue());
    }

    @Test
    public void testSetProductionValue() {
        conflict.setProductionValue("newProductionValue");
        assertEquals("newProductionValue", conflict.getProductionValue());
    }
}
