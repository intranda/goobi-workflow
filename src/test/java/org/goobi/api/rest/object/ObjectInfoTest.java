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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.junit.Test;

public class ObjectInfoTest {

    @Test
    public void testConstructorFromURIWithPly() throws URISyntaxException {
        URI uri = new URI("http://example.com/files/scan.ply");
        ObjectInfo info = new ObjectInfo(uri);
        assertNotNull(info);
        assertEquals(uri, info.getUri());
        assertEquals(ObjectFormat.PLY, info.getFormat());
    }

    @Test
    public void testConstructorFromURIWithObj() throws URISyntaxException {
        URI uri = new URI("http://example.com/files/scan.obj");
        ObjectInfo info = new ObjectInfo(uri);
        assertEquals(ObjectFormat.OBJ, info.getFormat());
    }

    @Test
    public void testConstructorFromStringWith3ds() throws URISyntaxException {
        ObjectInfo info = new ObjectInfo("http://example.com/files/scan.3ds");
        assertNotNull(info);
        assertEquals(ObjectFormat.TDS, info.getFormat());
        assertEquals(new URI("http://example.com/files/scan.3ds"), info.getUri());
    }

    @Test
    public void testConstructorFromStringWithUnknownExtension() throws URISyntaxException {
        ObjectInfo info = new ObjectInfo("http://example.com/files/scan.dat");
        assertNull(info.getFormat());
    }

    @Test
    public void testResourcesDefaultNull() throws URISyntaxException {
        ObjectInfo info = new ObjectInfo("http://example.com/files/scan.stl");
        assertNull(info.getResources());
    }

    @Test
    public void testSetResources() throws URISyntaxException {
        ObjectInfo info = new ObjectInfo("http://example.com/files/scan.ply");
        URI texture = new URI("http://example.com/files/texture.jpg");
        info.setResources(Arrays.asList(texture));
        assertEquals(1, info.getResources().size());
        assertEquals(texture, info.getResources().get(0));
    }

    @Test
    public void testDefaultCenterNotNull() throws URISyntaxException {
        ObjectInfo info = new ObjectInfo("http://example.com/files/scan.ply");
        assertNotNull(info.getCenter());
    }

    @Test
    public void testDefaultRotationNotNull() throws URISyntaxException {
        ObjectInfo info = new ObjectInfo("http://example.com/files/scan.ply");
        assertNotNull(info.getRotation());
    }
}
