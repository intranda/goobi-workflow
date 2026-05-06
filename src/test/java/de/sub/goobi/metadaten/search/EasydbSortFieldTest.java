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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class EasydbSortFieldTest {

    @Test
    public void testDefaultValues() {
        EasydbSortField sortField = new EasydbSortField();
        assertNotNull(sortField);
        assertEquals("_system_object_id", sortField.getField());
        assertEquals("DESC", sortField.getOrder());
    }

    @Test
    public void testSetField() {
        EasydbSortField sortField = new EasydbSortField();
        sortField.setField("artefact.name");
        assertEquals("artefact.name", sortField.getField());
    }

    @Test
    public void testSetOrder() {
        EasydbSortField sortField = new EasydbSortField();
        sortField.setOrder("ASC");
        assertEquals("ASC", sortField.getOrder());
    }
}
