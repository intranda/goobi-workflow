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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.goobi.production.flow.statistics.IStatisticalQuestion;
import org.goobi.production.flow.statistics.IStatisticalQuestionLimitedTimeframe;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.enums.TimeUnit;

import de.intranda.commons.chart.renderer.ChartRenderer;
import de.intranda.commons.chart.renderer.IRenderer;
import de.intranda.commons.chart.results.DataRow;
import de.intranda.commons.chart.results.DataTable;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.HistoryEventType;
import de.sub.goobi.persistence.managers.ProcessManager;

/*****************************************************************************
 * Implementation of {@link IStatisticalQuestion}. Statistical Request with predefined Values in data Table
 * 
 * @author Wulf Riebensahm
 ****************************************************************************/
public class StatQuestCorrections implements IStatisticalQuestionLimitedTimeframe {

    private static final long serialVersionUID = 2816273753644901605L;
    private Date timeFilterFrom;
    private TimeUnit timeGrouping;
    private Date timeFilterTo;

    /*
     * (non-Javadoc)
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#setTimeUnit(org.goobi.production.flow.statistics.enums.TimeUnit)
     */
    @Override
    public void setTimeUnit(TimeUnit timeGrouping) {
        this.timeGrouping = timeGrouping;
    }

    private TimeUnit getTimeUnit() {
        if (this.timeGrouping == null) {
            throw new NullPointerException("The called method in StatQuestCorrection requires that TimeUnit was set");
        }
        return this.timeGrouping;
    }

    /*
     * (non-Javadoc)
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#getDataTables(org.goobi.production.flow.statistics.IDataSource)
     */
    @Override
    public List<DataTable> getDataTables(String filter, String originalFilter, boolean showClosedProcesses, boolean showArchivedProjects) {

        List<DataTable> allTables = new ArrayList<>();

        //gathering IDs from the filter passed by dataSource
        List<Integer> idList = null;
        idList = ProcessManager.getIdsForFilter(filter);

        IStepRequests sqlGenerator = StatisticsFactory.getStepRequests(timeFilterFrom, timeFilterTo, timeGrouping, idList);

        // adding time restrictions
        String natSQL = sqlGenerator.getSQL(HistoryEventType.stepError, null, false, false);

        @SuppressWarnings("rawtypes")
        List list = ProcessManager.runSQL(natSQL);

        DataTable dtbl = new DataTable(StatisticsMode.getByClassName(this.getClass()).getTitle() + Helper.getTranslation("statistics_(number)"));

        DataRow dataRow;

        // each data row comes out as an Array of Objects
        // the only way to extract the data is by knowing
        // in which order they come out
        for (Object obj : list) {
            dataRow = new DataRow(null);
            Object[] objArr = (Object[]) obj;
            try {

                // getting localized time group unit

                //setting row name with date/time extraction based on the group

                dataRow.setName(new Converter(objArr[1]).getString() + "");

                dataRow.addValue(Helper.getTranslation("Corrections/Errors"), (new Converter(objArr[0]).getDouble()));

            } catch (NullPointerException e) {
                dataRow.addValue(e.getMessage(), Double.valueOf(0));
            }

            //finally adding dataRow to DataTable and fetching next row
            dtbl.addDataRow(dataRow);
        }

        // a list of DataTables is expected as return Object, even if there is only one
        // Data Table as it is here in this implementation
        dtbl.setUnitLabel(Helper.getTranslation(getTimeUnit().getSingularTitle()));
        allTables.add(dtbl);
        return allTables;
    }

    /*
     * (non-Javadoc)
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#setCalculationUnit(org.goobi.production.flow.statistics.enums.CalculationUnit)
     */
    @Override
    public void setCalculationUnit(CalculationUnit cu) {
    }

    /*
     * (non-Javadoc)
     * @see org.goobi.production.flow.statistics.IStatisticalQuestionLimitedTimeframe#setTimeFrame(java.util.Date, java.util.Date)
     */
    @Override
    public void setTimeFrame(Date timeFrom, Date timeTo) {
        this.timeFilterFrom = timeFrom;
        this.timeFilterTo = timeTo;

    }

    /*
     * (non-Javadoc)
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#isRendererInverted(de.intranda.commons.chart.renderer.IRenderer)
     */
    @Override
    public Boolean isRendererInverted(IRenderer inRenderer) {
        return inRenderer instanceof ChartRenderer;
    }

    @Override
    public String getNumberFormatPattern() {
        return "#";
    }

}
