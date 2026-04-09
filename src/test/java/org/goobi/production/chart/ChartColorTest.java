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
package org.goobi.production.chart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Color;

import org.junit.Test;

public class ChartColorTest {

    @Test
    public void testValuesCount() {
        assertEquals(3, ChartColor.values().length);
    }

    @Test
    public void testGreenExists() {
        assertNotNull(ChartColor.green);
    }

    @Test
    public void testYellowExists() {
        assertNotNull(ChartColor.yellow);
    }

    @Test
    public void testRedExists() {
        assertNotNull(ChartColor.red);
    }

    @Test
    public void testGreenColor() {
        Color c = ChartColor.green.getColor();
        assertEquals(0, c.getRed());
        assertEquals(130, c.getGreen());
        assertEquals(80, c.getBlue());
    }

    @Test
    public void testYellowColor() {
        Color c = ChartColor.yellow.getColor();
        assertEquals(250, c.getRed());
        assertEquals(220, c.getGreen());
        assertEquals(50, c.getBlue());
    }

    @Test
    public void testRedColor() {
        Color c = ChartColor.red.getColor();
        assertEquals(200, c.getRed());
        assertEquals(0, c.getGreen());
        assertEquals(0, c.getBlue());
    }

    @Test
    public void testValueOf() {
        assertEquals(ChartColor.green, ChartColor.valueOf("green"));
        assertEquals(ChartColor.yellow, ChartColor.valueOf("yellow"));
        assertEquals(ChartColor.red, ChartColor.valueOf("red"));
    }
}
