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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.goobi.beans.Batch;
import org.goobi.production.enums.ImportReturnValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ImportObjectTest {

    private ImportObject importObject;

    @BeforeEach
    public void setUp() {
        importObject = new ImportObject();
    }

    @Test
    public void testDefaultProcessTitleIsEmpty() {
        assertEquals("", importObject.getProcessTitle());
    }

    @Test
    public void testDefaultMetsFilenameIsEmpty() {
        assertEquals("", importObject.getMetsFilename());
    }

    @Test
    public void testDefaultImportFileNameIsEmpty() {
        assertEquals("", importObject.getImportFileName());
    }

    @Test
    public void testDefaultErrorMessageIsEmpty() {
        assertEquals("", importObject.getErrorMessage());
    }

    @Test
    public void testDefaultImportReturnValueIsExportFinished() {
        assertEquals(ImportReturnValue.ExportFinished, importObject.getImportReturnValue());
    }

    @Test
    public void testDefaultBatchIsNull() {
        assertNull(importObject.getBatch());
    }

    @Test
    public void testDefaultProcessPropertiesIsEmptyList() {
        assertNotNull(importObject.getProcessProperties());
        assertTrue(importObject.getProcessProperties().isEmpty());
    }

    @Test
    public void testSetProcessTitle() {
        importObject.setProcessTitle("myprocess.xml");
        assertEquals("myprocess.xml", importObject.getProcessTitle());
    }

    @Test
    public void testSetMetsFilename() {
        importObject.setMetsFilename("/path/to/file.xml");
        assertEquals("/path/to/file.xml", importObject.getMetsFilename());
    }

    @Test
    public void testSetImportFileName() {
        importObject.setImportFileName("import.xml");
        assertEquals("import.xml", importObject.getImportFileName());
    }

    @Test
    public void testSetErrorMessage() {
        importObject.setErrorMessage("Something went wrong");
        assertEquals("Something went wrong", importObject.getErrorMessage());
    }

    @Test
    public void testSetImportReturnValue() {
        importObject.setImportReturnValue(ImportReturnValue.WriteError);
        assertEquals(ImportReturnValue.WriteError, importObject.getImportReturnValue());
    }

    @Test
    public void testSetBatch() {
        Batch batch = new Batch();
        importObject.setBatch(batch);
        assertEquals(batch, importObject.getBatch());
    }
}
