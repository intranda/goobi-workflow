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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

public class ObjectTest {

    @Test
    public void testConstructorFromURIWithPly() throws URISyntaxException {
        URI uri = new URI("http://example.com/files/model.ply");
        Object obj = new Object(uri);
        assertNotNull(obj);
        assertEquals(uri, obj.getUri());
        assertEquals(ObjectFormat.PLY, obj.getType());
    }

    @Test
    public void testConstructorFromURIWithObj() throws URISyntaxException {
        URI uri = new URI("http://example.com/files/model.obj");
        Object obj = new Object(uri);
        assertEquals(ObjectFormat.OBJ, obj.getType());
    }

    @Test
    public void testConstructorFromStringWithStl() throws URISyntaxException {
        Object obj = new Object("http://example.com/files/model.stl");
        assertNotNull(obj);
        assertEquals(ObjectFormat.STL, obj.getType());
        assertEquals(new URI("http://example.com/files/model.stl"), obj.getUri());
    }

    @Test
    public void testConstructorFromStringWithUnknownExtension() throws URISyntaxException {
        Object obj = new Object("http://example.com/files/model.xyz");
        assertNull(obj.getType());
    }

    @Test
    public void testDefaultCenterIsOrigin() throws URISyntaxException {
        Object obj = new Object("http://example.com/files/model.ply");
        assertNotNull(obj.getCenter());
    }

    @Test
    public void testDefaultRotationIsOrigin() throws URISyntaxException {
        Object obj = new Object("http://example.com/files/model.ply");
        assertNotNull(obj.getRotation());
    }

    @Test
    public void testDefaultDistance() throws URISyntaxException {
        Object obj = new Object("http://example.com/files/model.ply");
        assertEquals(0.0, obj.getDistance(), 0.0001);
    }
}
