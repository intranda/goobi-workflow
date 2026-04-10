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
package org.goobi.api.rest.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ObjectFormatTest {

    @Test
    public void testGetByFileExtensionPly() {
        assertEquals(ObjectFormat.PLY, ObjectFormat.getByFileExtension("model.ply"));
    }

    @Test
    public void testGetByFileExtensionObj() {
        assertEquals(ObjectFormat.OBJ, ObjectFormat.getByFileExtension("model.obj"));
    }

    @Test
    public void testGetByFileExtensionStl() {
        assertEquals(ObjectFormat.STL, ObjectFormat.getByFileExtension("model.stl"));
    }

    @Test
    public void testGetByFileExtension3ds() {
        assertEquals(ObjectFormat.TDS, ObjectFormat.getByFileExtension("model.3ds"));
    }

    @Test
    public void testGetByFileExtensionUpperCase() {
        assertEquals(ObjectFormat.PLY, ObjectFormat.getByFileExtension("model.PLY"));
    }

    @Test
    public void testGetByFileExtensionUnknownReturnsNull() {
        assertNull(ObjectFormat.getByFileExtension("model.xyz"));
    }

    @Test
    public void testGetByFileExtensionNoExtensionReturnsNull() {
        assertNull(ObjectFormat.getByFileExtension("modelwithoutextension"));
    }

    @Test
    public void testEnumValues() {
        assertEquals(4, ObjectFormat.values().length);
    }
}
