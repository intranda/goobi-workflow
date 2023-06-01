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
package org.goobi.production.flow.statistics.hibernate;

import java.util.Date;
import java.util.List;

import javax.enterprise.inject.Alternative;

import org.goobi.production.flow.statistics.enums.TimeUnit;

import de.sub.goobi.helper.enums.HistoryEventType;

/**
 * Class provides SQL for Step Requests statistics on the history table it offers a little more functionallity compared to the other SQL Source
 * classes. There are a little more parameters which can be set
 * 
 * @author Wulf Riebensahm
 *
 */

@Alternative
public class H2StepRequestByName extends H2Generator implements IStepRequestByName {

    public H2StepRequestByName(Date timeFrom, Date timeTo, TimeUnit timeUnit, List<Integer> ids) {
        // "history.processid - overrides the default value of prozesse.prozesseID
        // which is set in super class SQLGenerator
        super(timeFrom, timeTo, timeUnit, ids, "processid");
    }

    /**
     * This is an extended SQL generator for an SQL extracting data from the historyEvent log. depending on the parameters the query returns up to
     * four fields
     * 
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.statistics.hibernate.SQLGenerator#getSQL()
     * 
     * @param typeSelection - operates as additional filter
     * @param stepName - operates as additional filter
     * @param stepOrderGrouping - 'stepName' fields in select and in group by clause
     * @param includeCorrections - adding additional stepOpen from Correction and other loops
     * 
     * @return SQLExpression for MySQL DBMS - default fields stepCount and intervall
     */
    @Override
    public String getSQL(HistoryEventType typeSelection, String stepName, Boolean stepOrderGrouping, Boolean includeLoops) {

        String timeLimiter = "h.date";
        String groupInnerSelect = "";

        //evaluate if groupingFunction comes along with HistoryEventType
        // and if so implement this function in sql
        if (typeSelection.getGroupingFunction() != null && !includeLoops) {
            timeLimiter = typeSelection.getGroupingFunction() + "(h.date)";
            groupInnerSelect = " GROUP BY processid, numericvalue, stringvalue ";
        }

        String subQuery = "";
        String outerWhereClauseTimeFrame = getWhereClauseForTimeFrame(this.myTimeFrom, this.myTimeTo, "timeLimiter");
        String outerWhereClause = "";

        if (outerWhereClauseTimeFrame.length() > 0) {
            outerWhereClause = "WHERE " + outerWhereClauseTimeFrame;
        }

        //inner table -> alias "table_1"
        String innerWhereClause;

        if (this.myIdsCondition != null) {
            // adding ids to the where clause
            innerWhereClause = "(h.type=" + typeSelection.getValue().toString() + ")  AND (" + this.myIdsCondition + ") ";
        } else {
            innerWhereClause = "(h.type=" + typeSelection.getValue().toString() + ") ";
        }

        // adding a stepOrder filter to numericvalue if parameter is set
        if (stepName != null) {
            innerWhereClause = innerWhereClause + " AND h.stringvalue='" + stepName + "' ";
        }

        subQuery = "(SELECT numericvalue AS stepOrder, " + getIntervallExpression(this.myTimeUnit, "history.date") + " "
                + "AS intervall, history.date AS timeLimiter, history.stringvalue AS stepName " + "FROM "

                + "(SELECT DISTINCT h.numericvalue, h.stringvalue, " + timeLimiter + " as date, h.processid, h.type " + "FROM history h " + "WHERE "
                + innerWhereClause + groupInnerSelect + ") AS history " + ") AS table_1";

        this.mySql =
                "SELECT count(table_1.stepName) AS stepCount, table_1.intervall AS intervall " + addedListing(stepOrderGrouping) + "FROM " + subQuery
                        + " " + outerWhereClause + " GROUP BY table_1.intervall" + addedGrouping(stepOrderGrouping) + " ORDER BY  table_1.intervall"
                        + addedSorting(stepOrderGrouping);

        return this.mySql;
    }

    /**
     * Method is purposfully not implemented. Method getSQL is overloaded with parametered method.
     * 
     * @see org.goobi.production.flow.statistics.hibernate.SQLGenerator#getSQL()
     */
    @Override
    public String getSQL() {
        throw new UnsupportedOperationException("The class " + this.getClass().getName()
                + " does not support the parameterless getSQL() method. Instead you need to use getSQL() with parameters.");
    }

    /**
     * 
     * @param include
     * @return SQL snippet for Order by clause
     */

    private String addedSorting(Boolean include) {
        if (Boolean.TRUE.equals(include)) {
            return ", table_1.stepOrder";
        } else {
            return "";
        }
    }

    /**
     * 
     * @param include
     * @return SQL snippet for Select clause
     */
    private String addedListing(Boolean include) {
        if (Boolean.TRUE.equals(include)) {
            return ",table_1.stepName ";
        } else {
            return "";
        }
    }

    /**
     * 
     * @param include
     * @return SQL snippet for Group by clause
     */
    private String addedGrouping(Boolean include) {
        if (Boolean.TRUE.equals(include)) {
            return ", table_1.stepName ";
        } else {
            return "";
        }
    }

    /**
     * Returns the SQL String to get the highest numeric value (stepOrder) for the event defined in eventSelection
     *
     * @param eventSelection The event selection object
     * @return The SQL String to get the highest numeric value (stepOrder) for the event defined in eventSelection
     */
    public String SQLMaxStepOrder(HistoryEventType eventSelection) {
        return this.createMinOrMaxStepOrder(eventSelection, true);
    }

    /**
     * Returns the SQL String to get the lowest numeric value (stepOrder) for the event defined in eventSelection
     *
     * @param eventSelection The event selection object
     * @return The SQL String to get the lowest numeric value (stepOrder) for the event defined in eventSelection
     */
    public String SQLMinStepOrder(HistoryEventType eventSelection) {
        return this.createMinOrMaxStepOrder(eventSelection, false);
    }

}
