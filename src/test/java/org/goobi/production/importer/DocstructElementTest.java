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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DocstructElementTest {

    @Test
    public void testConstructorSetsDocStruct() {
        DocstructElement element = new DocstructElement("Monograph", 1);
        assertEquals("Monograph", element.getDocStruct());
    }

    @Test
    public void testConstructorSetsOrder() {
        DocstructElement element = new DocstructElement("Chapter", 3);
        assertEquals(3, element.getOrder());
    }

    @Test
    public void testSetDocStruct() {
        DocstructElement element = new DocstructElement("Monograph", 1);
        element.setDocStruct("Volume");
        assertEquals("Volume", element.getDocStruct());
    }

    @Test
    public void testSetOrder() {
        DocstructElement element = new DocstructElement("Monograph", 1);
        element.setOrder(5);
        assertEquals(5, element.getOrder());
    }

    @Test
    public void testOrderZero() {
        DocstructElement element = new DocstructElement("Article", 0);
        assertEquals(0, element.getOrder());
    }
}
