package de.sub.goobi.converter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
 */

import org.junit.Test;
import org.threeten.extra.chrono.BritishCutoverDate;
import org.threeten.extra.chrono.JulianDate;

public class CalendarConverterTest {

    @Test
    public void callExample() {
        // Day 1 JUL 02.09.1752 <-> Day 1 GRE 02.09.1752
        // Day 2 JUL 03.09.1752 <-> Day 2 GRE 14.09.1752

        // JUL 01.03.1740 <-> GRE 12.03.1741
        // JUL 01.04.1741 <-> GRE 12.04.1741

        // Day 1 JUL 31.12.1751 <-> GRE 31.12.1751
        // Day 2 JUL 01.01.1751 <-> GRE 01.01.1752
        // Day 1 JUL 24.03.1751 <-> GRE 24.03.1752
        // Day 2 JUL 25.03.1752 <-> GRE 25.03.1752

        List<String> dates = new ArrayList<>();

        dates.add("1752-09-02");
        dates.add("1752-09-03");
        dates.add("1752-09-14");

        dates.add("1740-03-01");
        dates.add("1741-03-12");
        dates.add("1741-04-01");
        dates.add("1741-04-12");

        dates.add("1751-12-31");
        dates.add("1751-01-01");
        dates.add("1752-01-01");
        dates.add("1751-03-24");
        dates.add("1752-03-24");
        dates.add("1752-03-25");

        dates.add("1752-09-01");
        dates.add("1752-09-02");
        dates.add("1752-09-03");
        dates.add("1752-09-04");
        dates.add("1752-09-09");
        dates.add("1752-09-13");
        dates.add("1752-09-14");

        dates.add("1752-04-02");

        // setGregorianChange 3/13 Sept 1752
        // default GregorianChange: 15 October, 1582

        // GregorianCalendar gc = GregorianCalendar.from(ZonedDateTime.of(ldt, ZoneId.systemDefault()));
        // Pure Jul gc.setGregorianChange(new Date(Long.MAX_VALUE));
        // Pure GRE gc.setGregorianChange(new Date(Long.MIN_VALUE));

        for (String date : dates) {
            LocalDate ld = LocalDate.parse(date);
            JulianDate jd = JulianDate.from(ld);
            BritishCutoverDate bd = BritishCutoverDate.from(ld);
            System.out.println(ld);
            System.out.println(jd);
            System.out.println(bd);
        }
    }

}