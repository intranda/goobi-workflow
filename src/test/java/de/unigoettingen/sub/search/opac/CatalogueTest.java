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
package de.unigoettingen.sub.search.opac;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class CatalogueTest {

    @Test
    public void testCustomConstructor5Args() throws IOException {
        Catalogue cat = new Catalogue("My Description", "server.example.com", 9090, "cbs_param", "mydb");
        assertNotNull(cat);
        assertEquals("My Description", cat.getDescription());
        assertEquals("server.example.com", cat.getServerAddress());
        assertEquals(9090, cat.getPort());
        assertEquals("cbs_param", cat.getCbs());
        assertEquals("mydb", cat.getDataBase());
        assertEquals("iso-8859-1", cat.getCharset());
    }

    @Test
    public void testCustomConstructor6Args() throws IOException {
        Catalogue cat = new Catalogue("desc", "server.example.com", 80, "utf-8", "cbs", "db");
        assertEquals("utf-8", cat.getCharset());
        assertEquals("server.example.com", cat.getServerAddress());
    }

    @Test
    public void testVerboseDefaultFalse() throws IOException {
        Catalogue cat = new Catalogue("desc", "server.example.com", 80, "utf-8", "cbs", "db");
        assertFalse(cat.isVerbose());
    }

    @Test
    public void testSetVerbose() throws IOException {
        Catalogue cat = new Catalogue("desc", "server.example.com", 80, "utf-8", "cbs", "db");
        cat.setVerbose(true);
        assertTrue(cat.isVerbose());
    }

    @Test
    public void testProtocolDefault() throws IOException {
        Catalogue cat = new Catalogue("desc", "server.example.com", 80, "utf-8", "cbs", "db");
        assertEquals("http://", cat.getProtocol());
    }

    @Test
    public void testSetProtocol() throws IOException {
        Catalogue cat = new Catalogue("desc", "server.example.com", 80, "utf-8", "cbs", "db");
        cat.setProtocol("https://");
        assertEquals("https://", cat.getProtocol());
    }

    @Test
    public void testSetCbs() throws IOException {
        Catalogue cat = new Catalogue("desc", "server.example.com", 80, "utf-8", "cbs", "db");
        cat.setCbs("UCNF=NFC");
        assertEquals("UCNF=NFC", cat.getCbs());
    }

}
