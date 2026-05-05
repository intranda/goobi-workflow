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
package de.sub.goobi.media.pdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class PDFGeneratorTest extends AbstractTest {

    private Process mockProcess;
    private PDFGenerator generator;

    @Before
    public void setUp() throws Exception {
        mockProcess = PowerMock.createMock(Process.class);
        EasyMock.expect(mockProcess.getId()).andReturn(42).anyTimes();
        EasyMock.expect(mockProcess.getImagesDirectory()).andReturn("/tmp/goobi/images/").anyTimes();
        EasyMock.expect(mockProcess.getOcrAltoDirectory()).andReturn("/tmp/goobi/ocr/alto/").anyTimes();
        EasyMock.replay(mockProcess);

        generator = new PDFGenerator(mockProcess);
    }

    @Test
    public void testConstructor() {
        assertNotNull(generator);
    }

    @Test
    public void testGetProcessId() {
        assertEquals("42", generator.getProcessId());
    }

    @Test
    public void testGetImagesDirectory() {
        assertEquals("/tmp/goobi/images/", generator.getImagesDirectory());
    }

    @Test
    public void testGetAltoDirectory() {
        assertEquals("/tmp/goobi/ocr/alto/", generator.getAltoDirectory());
    }
}
