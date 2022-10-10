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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Dimension;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class ImageLevelTest extends AbstractTest {

    private String url = "https://example.com/test/";
    private Dimension size = new Dimension(200, 200);

    @Test
    public void testImageLevelConstructorWithDimension() {
        ImageLevel fixture = new ImageLevel(url, size);
        assertNotNull(fixture);
    }

    @Test
    public void testImageLevelConstructorWithPx() {
        ImageLevel fixture = new ImageLevel(url, 200, 200);
        assertNotNull(fixture);
    }

    @Test
    public void testSize() {
        ImageLevel fixture = new ImageLevel(url, size);
        assertEquals(size, fixture.getSize());
    }

    @Test
    public void testWidth() {
        ImageLevel fixture = new ImageLevel(url, size);
        assertEquals(200, fixture.getWidth());
    }

    @Test
    public void testHeight() {
        ImageLevel fixture = new ImageLevel(url, size);
        assertEquals(200, fixture.getHeight());
    }

    @Test
    public void testToString() {
        ImageLevel fixture = new ImageLevel(url, size);
        assertEquals("{\"url\" : \"https://example.com/test/\",\"width\" : 200,\"height\" : 200}", fixture.toString());
    }

    @Test
    public void testCompareTo() {
        ImageLevel fixture = new ImageLevel(url, size);
        ImageLevel sameSize = new ImageLevel(url, 200, 200);
        ImageLevel smaller = new ImageLevel(url, 100, 100);
        ImageLevel larger = new ImageLevel(url, 300, 300);

        assertEquals(0, fixture.compareTo(sameSize));
        assertEquals(1, fixture.compareTo(smaller));
        assertEquals(-1, fixture.compareTo(larger));
    }
}
