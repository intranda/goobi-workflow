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
package org.goobi.production.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class ImportTypeTest {

    @Test
    public void testValuesCount() {
        assertEquals(4, ImportType.values().length);
    }

    @Test
    public void testRecordId() {
        assertEquals("1", ImportType.Record.getId());
    }

    @Test
    public void testRecordTitle() {
        assertEquals("record", ImportType.Record.getTitle());
    }

    @Test
    public void testIdId() {
        assertEquals("2", ImportType.ID.getId());
    }

    @Test
    public void testFileId() {
        assertEquals("3", ImportType.FILE.getId());
    }

    @Test
    public void testFolderId() {
        assertEquals("4", ImportType.FOLDER.getId());
    }

    @Test
    public void testGetByTitleRecord() {
        assertEquals(ImportType.Record, ImportType.getByTitle("record"));
    }

    @Test
    public void testGetByTitleId() {
        assertEquals(ImportType.ID, ImportType.getByTitle("id"));
    }

    @Test
    public void testGetByTitleFile() {
        assertEquals(ImportType.FILE, ImportType.getByTitle("file"));
    }

    @Test
    public void testGetByTitleFolder() {
        assertEquals(ImportType.FOLDER, ImportType.getByTitle("folder"));
    }

    @Test
    public void testGetByTitleUnknownReturnsNull() {
        assertNull(ImportType.getByTitle("unknown"));
    }

    @Test
    public void testGetByTitleNullReturnsNull() {
        assertNull(ImportType.getByTitle(null));
    }
}
