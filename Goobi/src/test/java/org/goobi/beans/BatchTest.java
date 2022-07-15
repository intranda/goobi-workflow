package org.goobi.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - https://goobi.io - https://www.intranda.com - https://github.com/intranda/goobi-workflow
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

public class BatchTest extends AbstractTest {

    @Test
    public void testConstructor() {
        Batch fixture = new Batch();
        assertNotNull(fixture);
    }

    @Test
    public void testBatchId() {
        Batch fixture = new Batch();
        fixture.setBatchId(null);
        assertNull(fixture.getBatchId());
        fixture.setBatchId(666);
        assertEquals(666, fixture.getBatchId().intValue());
    }

    @Test
    public void testBatchName() {
        Batch fixture = new Batch();
        fixture.setBatchName(null);
        assertNull(fixture.getBatchName());
        fixture.setBatchName("666");
        assertSame("666", fixture.getBatchName());
    }

    @Test
    public void testBatchLabel() {
        Batch fixture = new Batch();
        fixture.setBatchLabel(null);
        assertNull(fixture.getBatchLabel());
        fixture.setBatchLabel("666");
        assertSame("666", fixture.getBatchLabel());
    }

    @Test
    public void testStartDate() throws Exception {
        Date d = new SimpleDateFormat("yyyy-MM-dd").parse("2000-01-01");
        Batch fixture = new Batch();
        fixture.setStartDate(d);
        assertEquals(d, fixture.getStartDate());
    }

    @Test
    public void testEndDate() throws Exception {
        Date d = new SimpleDateFormat("yyyy-MM-dd").parse("2000-12-31");
        Batch fixture = new Batch();
        fixture.setEndDate(d);
        assertEquals(d, fixture.getEndDate());
    }
}
