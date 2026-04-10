package org.goobi.production.plugin;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SpanTagTest {

    @Test
    public void testConstructor() {
        SpanTag tag = new SpanTag("hello", SpanTag.TEXT_NORMAL);
        assertNotNull(tag);
        assertEquals("hello", tag.getText());
        assertEquals(SpanTag.TEXT_NORMAL, tag.getType());
    }

    @Test
    public void testTextConstantValues() {
        assertEquals("TEXT_NORMAL", SpanTag.TEXT_NORMAL);
        assertEquals("TEXT_INSERTED_PASSIVE", SpanTag.TEXT_INSERTED_PASSIVE);
        assertEquals("TEXT_INSERTED_ACTIVE", SpanTag.TEXT_INSERTED_ACTIVE);
        assertEquals("TEXT_DELETED_PASSIVE", SpanTag.TEXT_DELETED_PASSIVE);
        assertEquals("TEXT_DELETED_ACTIVE", SpanTag.TEXT_DELETED_ACTIVE);
    }

    @Test
    public void testSpaceConstantValues() {
        assertEquals("SPACE_NORMAL", SpanTag.SPACE_NORMAL);
        assertEquals("SPACE_INSERTED_PASSIVE", SpanTag.SPACE_INSERTED_PASSIVE);
        assertEquals("SPACE_INSERTED_ACTIVE", SpanTag.SPACE_INSERTED_ACTIVE);
        assertEquals("SPACE_DELETED_PASSIVE", SpanTag.SPACE_DELETED_PASSIVE);
        assertEquals("SPACE_DELETED_ACTIVE", SpanTag.SPACE_DELETED_ACTIVE);
    }

    @Test
    public void testGetText() {
        SpanTag tag = new SpanTag("content", SpanTag.TEXT_INSERTED_ACTIVE);
        assertEquals("content", tag.getText());
    }

    @Test
    public void testGetType() {
        SpanTag tag = new SpanTag("content", SpanTag.TEXT_DELETED_ACTIVE);
        assertEquals(SpanTag.TEXT_DELETED_ACTIVE, tag.getType());
    }
}
