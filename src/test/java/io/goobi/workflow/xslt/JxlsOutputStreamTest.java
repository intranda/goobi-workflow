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
package io.goobi.workflow.xslt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.jupiter.api.Test;

public class JxlsOutputStreamTest {

    @Test
    public void testConstructor() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JxlsOutputStream jxlsOutputStream = new JxlsOutputStream(bos);
        assertNotNull(jxlsOutputStream);
    }

    @Test
    public void testGetOutputStream() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JxlsOutputStream jxlsOutputStream = new JxlsOutputStream(bos);
        OutputStream result = jxlsOutputStream.getOutputStream();
        assertEquals(bos, result);
    }

    @Test
    public void testGetOutputStreamIsWritable() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JxlsOutputStream jxlsOutputStream = new JxlsOutputStream(bos);
        OutputStream result = jxlsOutputStream.getOutputStream();
        result.write(new byte[] { 1, 2, 3 });
        assertEquals(3, bos.size());
    }

}
