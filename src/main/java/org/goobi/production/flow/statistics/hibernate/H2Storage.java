package org.goobi.production.flow.statistics.hibernate;

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

import java.util.Date;
import java.util.List;

import org.goobi.production.flow.statistics.enums.TimeUnit;

import de.sub.goobi.helper.enums.HistoryEventType;
import jakarta.enterprise.inject.Alternative;

/**
 * Class provides SQL for storage statistics.
 * 
 * 
 * @author Wulf Riebensahm
 *
 */

@Alternative
public class H2Storage extends H2Generator implements IStorage {

    public H2Storage(Date timeFrom, Date timeTo, TimeUnit timeUnit, List<Integer> ids) {
        // "history.processid overrides the defautl value of prozesseID
        super(timeFrom, timeTo, timeUnit, ids, "history.processID");
    }

    /*
     * (non-Javadoc)
     * @see org.goobi.production.flow.statistics.hibernate.SQLGenerator#getSQL()
     */
    @Override
    public String getSQL() {

        String subQuery = "";
        String outerWhereClause = "";
        String outerWhereClauseTimeFrame = getWhereClauseForTimeFrame(this.myTimeFrom, this.myTimeTo, "timeLimiter");

        if (outerWhereClauseTimeFrame.length() > 0) {
            outerWhereClause = "WHERE".concat(outerWhereClauseTimeFrame);
        }

        //inner table -> alias "table_1"
        String innerWhereClause;

        if (this.myIdsCondition != null) {
            // adding ids to the where clause
            innerWhereClause = "(history.type=" + HistoryEventType.storageDifference.getValue().toString() + ")  AND (" + this.myIdsCondition + ")";
        } else {
            innerWhereClause = "(history.type=" + HistoryEventType.storageDifference.getValue().toString() + ") ";
        }

        subQuery = "(SELECT numericvalue AS storage, " + getIntervallExpression(this.myTimeUnit, "history.date") + " "
                + "AS intervall, history.date AS timeLimiter FROM history WHERE " + innerWhereClause + ") AS table_1";

        this.mySql = "SELECT sum(table_1.storage) AS storage, table_1.intervall AS intervall FROM " + subQuery + " " + outerWhereClause
                + " GROUP BY table_1.intervall " + "ORDER BY table_1.intervall";

        return this.mySql;
    }

}
