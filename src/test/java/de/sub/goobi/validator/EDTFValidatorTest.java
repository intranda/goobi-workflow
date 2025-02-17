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
package de.sub.goobi.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EDTFValidatorTest {

    private EDTFValidator validator = new EDTFValidator();

    @Test
    public void failToParseRandomNonCompliantStringTest() {

        assertFalse(validator.isValid("This String is quite random."));
    }

    @Test
    public void correctlyParseEDTFCompliantString() {
        assertTrue(validator.isValid("1984"));
    }

    @Test
    public void testLevel0Date() {
        // complete representation
        assertTrue(validator.isValid("1984-04-04"));
        // reduced precision for year and month
        assertTrue(validator.isValid("1984-04"));
        // reduced precision for year
        assertTrue(validator.isValid("1984"));

        // invalid month
        assertFalse(validator.isValid("1984-00"));
        assertFalse(validator.isValid("1984-13"));
        // invalid date
        assertFalse(validator.isValid("1984-04-32"));
    }

    @Test
    public void testLevel0DateTime() {
        // [date][“T”][time]
        assertTrue(validator.isValid("1985-04-12T23:20:30"));
        // [dateI][“T”][time][“Z”]
        assertTrue(validator.isValid("1985-04-12T23:20:30Z"));
        // [dateI][“T”][time][shiftHour]
        assertTrue(validator.isValid("1985-04-12T23:20:30-04"));
        // [dateI][“T”][time][shiftHourMinute]
        assertTrue(validator.isValid("1985-04-12T23:20:30+04:30"));
    }

    @Test
    public void testLevel0TimeInterval() {
        // calendar year precision
        assertTrue(validator.isValid("1964/2008"));
        // calendar month precision
        assertTrue(validator.isValid("2004-06/2006-08"));
        // calendar day precision
        assertTrue(validator.isValid("2004-02-01/2005-02-08"));
        assertTrue(validator.isValid("2004-02-01/2005-02"));
        assertTrue(validator.isValid("2004-02-01/2005"));
        assertTrue(validator.isValid("2005/2006-02"));
    }

    @Test
    public void testLevel1Season() {
        assertFalse(validator.isValid("2001-20"));
        assertTrue(validator.isValid("2001-21")); // spring
        assertTrue(validator.isValid("2001-22")); // summer
        assertTrue(validator.isValid("2001-23")); // autumn
        assertTrue(validator.isValid("2001-24")); // winter
    }

    @Test
    public void testLevel1Qualification() {
        // year uncertain
        assertTrue(validator.isValid("1984?"));
        // year-month approximate
        assertTrue(validator.isValid("2004-06~'"));
        // entire date (year-month-day) uncertain and approximate
        assertTrue(validator.isValid("2004-06-11%"));
        assertTrue(validator.isValid("2004-06-~11"));
    }

    @Test
    public void testLevel1Unspecified() {
        assertTrue(validator.isValid("201X"));
        assertTrue(validator.isValid("20XX"));
        assertTrue(validator.isValid("2004-XX"));
        assertTrue(validator.isValid("1985-04-XX"));
        assertTrue(validator.isValid("1985-XX-XX"));

    }

    @Test
    public void testLevel1ExtendedInterval() {
        // Open end time interval
        assertTrue(validator.isValid("1985-04-12/.."));
        assertTrue(validator.isValid("1985-04/.."));
        assertTrue(validator.isValid("1985/.."));
        //  Open start time interval
        assertTrue(validator.isValid("../1985-04-12"));
        assertTrue(validator.isValid("../1985-04"));
        assertTrue(validator.isValid("../1985"));
        // Time interval with unknown end
        assertTrue(validator.isValid("1985-04-12/"));
        assertTrue(validator.isValid("1985-04/"));
        assertTrue(validator.isValid("1985/"));
        // Time interval with unknown start
        assertTrue(validator.isValid("/1985-04-12"));
        assertTrue(validator.isValid("/1985-04"));
        assertTrue(validator.isValid("/1985"));
    }

    @Test
    public void testLevel2SetRepresentation() {
        // One of the years 1667, 1668, 1670, 1671, 1672
        assertTrue(validator.isValid("[1667,1668,1670,1671,1672]"));
        assertTrue(validator.isValid("[1667,1668,1670..1672]"));
        // December 3, 1760; or some earlier date
        assertTrue(validator.isValid("[..1760-12-03]"));
        // December 1760, or some later month
        assertTrue(validator.isValid("[1760-12..]"));
        // January or February of 1760 or December 1760 or some later month
        assertTrue(validator.isValid("[1760-01,1760-02,1760-12..]"));
        //  Either the year 1667 or the month December of 1760.
        assertTrue(validator.isValid("[1667,1760-12]"));
        //  The year 1984 or an earlier year
        assertTrue(validator.isValid("[..1984]"));
        //  All of the years 1667, 1668, 1670, 1671, 1672
        assertTrue(validator.isValid("{1667,1668,1670..1672}"));
        // The year 1960 and the month December of 1961.
        assertTrue(validator.isValid("{1960,1961-12}"));
        //  The year 1984 and all earlier years
        assertTrue(validator.isValid("{..1984}"));
    }

    @Test
    public void testLevel2Interval() {
        // An interval in June 2004 beginning approximately the first and ending approximately the 20th
        assertTrue(validator.isValid("2004-06-01~/2004-06-20~"));
        assertTrue(validator.isValid("2004-06-~01/2004-06-~20"));
        //  An interval beginning on an unspecified day in June 2004 and ending July 3.
        assertTrue(validator.isValid("2004-06-XX/2004-07-03"));
    }
}
