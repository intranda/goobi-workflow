package org.goobi.production.flow.statistics.enums;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import de.sub.goobi.helper.Helper;
import lombok.Getter;

/**
 * Enum of all time units for the statistics.
 * 
 * @author Steffen Hankiewicz
 * @version 21.05.2009
 ****************************************************************************/
public enum TimeUnit {

    days("1", "days", "day", "day", true, 1.0),
    weeks("2", "weeks", "week", "week", true, 5.0),
    months("3", "months", "month", "month", true, 21.3),
    quarters("4", "quarters", "quarter", "quarter", true, 64.0),
    years("5", "years", "year", "year", true, 256.0),
    simpleSum("6", "alltime", "alltime", "alltime", false, -1.0);

    @Getter
    private String id;
    private String title;
    @Getter
    private String sqlKeyword;
    @Getter
    private String singularTitle;
    private boolean visible;
    @Getter
    private Double dayFactor;

    /**
     * private constructor for setting id and title
     * 
     * @param inTitle title as String
     ****************************************************************************/
    TimeUnit(String inId, String inTitle, String inKeyword, String inSingularTitle, Boolean visible, Double dayFactor) {
        id = inId;
        title = inTitle;
        singularTitle = inSingularTitle;
        sqlKeyword = inKeyword;
        this.visible = visible;
        this.dayFactor = dayFactor;
    }

    /**
     * return localized title for TimeUnit from standard-jsf-messages-files.
     * 
     * @return localized title
     ****************************************************************************/
    public String getTitle() {
        return Helper.getTranslation(title);
    }

    /**
     * return the internal String representing the Title, use this for localisation.
     * 
     * @return the internal title
     ****************************************************************************/
    @Override
    public String toString() {
        return title;
    }

    /**
     * get TimeUnit by unique ID.
     * 
     * @param inId the unique ID
     * @return {@link TimeUnit} with given ID
     ****************************************************************************/
    public static TimeUnit getById(String inId) {
        for (TimeUnit unit : TimeUnit.values()) {
            if (unit.getId().equals(inId)) {
                return unit;
            }
        }
        return days;
    }

    public static List<TimeUnit> getAllVisibleValues() {
        ArrayList<TimeUnit> mylist = new ArrayList<>();
        for (TimeUnit tu : TimeUnit.values()) {
            if (tu.visible) {
                mylist.add(tu);
            }
        }
        return mylist;
    }

    /**
     * function allows to retrieve a datarow based on startdaten enddate and intervall.
     * 
     * @param start
     * @param end
     * @return list
     */
    public List<String> getDateRow(Date start, Date end) {
        List<String> dateRow = new ArrayList<>();

        Date nextDate = start;

        while (nextDate.before(end)) {
            dateRow.add(getTimeFormat(nextDate));
            nextDate = getNextDate(nextDate);
        }

        return dateRow;
    }

    @SuppressWarnings("deprecation")
    private String getTimeFormat(Date inDate) {

        switch (this) {

            case days:
            case months:
            case simpleSum:
            case weeks:
            case years:
                return new DateTime(inDate).toString(getFormatter());

            case quarters:
                return new DateTime(inDate).toString(getFormatter()) + "/"
                        + Integer.toString((inDate.getMonth() - 1) / 3 + 1);
            default:
                // do nothing

        }
        return inDate.toString();

    }

    private Date getNextDate(Date inDate) {

        switch (this) {

            case days:
                return new DateTime(inDate).plusDays(1).toDate();

            case months:
                return new DateTime(inDate).plusMonths(1).toDate();

            case quarters:
                return new DateTime(inDate).plusMonths(3).toDate();

            case simpleSum:
                return inDate;

            case weeks:
                return new DateTime(inDate).plusWeeks(1).toDate();

            case years:
                return new DateTime(inDate).plusYears(1).toDate();
            default:
                // do nothing
        }
        return inDate;
    }

    private DateTimeFormatter getFormatter() {

        switch (this) {

            case days:
                return DateTimeFormat.forPattern("yyyy-MM-dd");
            case months:
                return DateTimeFormat.forPattern("yyyy/MM");
            case weeks:
                return DateTimeFormat.forPattern("yyyy/ww");
            case years:
                return DateTimeFormat.forPattern("yyyy");
            case quarters:
                // has to be extended by the calling function
                return DateTimeFormat.forPattern("yyyy");
            default:
                return null;
        }
    }

}
