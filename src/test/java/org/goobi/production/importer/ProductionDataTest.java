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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Date;

import org.junit.Before;
import org.junit.Test;

public class ProductionDataTest {

    private ProductionData data;

    @Before
    public void setUp() {
        data = new ProductionData();
    }

    @Test
    public void testDefaultFieldsAreNull() {
        assertNull(data.getWERKNR());
        assertNull(data.getWERKATS());
        assertNull(data.getSCANPFAD());
        assertNull(data.getBEMERKUNG());
        assertNull(data.getPatenname());
    }

    @Test
    public void testSetWERKNR() {
        data.setWERKNR(42);
        assertEquals(Integer.valueOf(42), data.getWERKNR());
    }

    @Test
    public void testSetWERKATS() {
        data.setWERKATS("ATS-001");
        assertEquals("ATS-001", data.getWERKATS());
    }

    @Test
    public void testSetSCANPFAD() {
        data.setSCANPFAD("/mnt/scans/project1");
        assertEquals("/mnt/scans/project1", data.getSCANPFAD());
    }

    @Test
    public void testSetWERKSCANSEITEN() {
        data.setWERKSCANSEITEN(250);
        assertEquals(Integer.valueOf(250), data.getWERKSCANSEITEN());
    }

    @Test
    public void testSetWERKMB() {
        data.setWERKMB(1024);
        assertEquals(Integer.valueOf(1024), data.getWERKMB());
    }

    @Test
    public void testSetDATUMAUFNAHMEWERK() {
        Date date = new Date(0);
        data.setDATUMAUFNAHMEWERK(date);
        assertEquals(date, data.getDATUMAUFNAHMEWERK());
    }

    @Test
    public void testSetWERKSCANDATUM() {
        Date date = Date.valueOf("2024-06-15");
        data.setWERKSCANDATUM(date);
        assertEquals(date, data.getWERKSCANDATUM());
    }

    @Test
    public void testSetBEMERKUNG() {
        data.setBEMERKUNG("Test remark");
        assertEquals("Test remark", data.getBEMERKUNG());
    }

    @Test
    public void testSetPatennennung() {
        data.setPatennennung(true);
        assertTrue(data.getPatennennung());
    }

    @Test
    public void testSetPatenname() {
        data.setPatenname("Max Mustermann");
        assertEquals("Max Mustermann", data.getPatenname());
    }

    @Test
    public void testSetWERKPROJEKT() {
        data.setWERKPROJEKT("digitization-2024");
        assertEquals("digitization-2024", data.getWERKPROJEKT());
    }

    @Test
    public void testSetSCANNERTYP() {
        data.setSCANNERTYP("Zeutschel OS 12000");
        assertEquals("Zeutschel OS 12000", data.getSCANNERTYP());
    }
}
