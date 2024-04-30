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
import java.util.Collections;
import java.util.Comparator;
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
import lombok.extern.log4j.Log4j2;

/*****************************************************************************
 * Imlpementation of {@link IStatisticalQuestion}. Statistical Request with predefined Values in data Table
 ****************************************************************************/
@Log4j2
public class StatQuestThroughput implements IStatisticalQuestionLimitedTimeframe {

    private static final long serialVersionUID = 4042861398961616967L;
    private Date timeFilterFrom;
    private TimeUnit timeGrouping;
    private Date timeFilterTo;
    private List<Integer> myIDlist;
    private Boolean flagIncludeLoops = false;

    /**
     * loops included means that all step open all stepdone are considered loops not included means that only min(date) or max(date) - depending on
     * option in
     * 
     * @see historyEventType
     * 
     * @return status of loops included or not
     */
    public Boolean getIncludeLoops() {
        return this.flagIncludeLoops;
    }

    /**
     * Set status of loops included
     * 
     * @param includeLoops
     */
    public void setIncludeLoops(Boolean includeLoops) {
        this.flagIncludeLoops = includeLoops;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.goobi.production.flow.statistics.IStatisticalQuestion#setTimeUnit
     * (org.goobi.production.flow.statistics.enums.TimeUnit)
     */
    @Override
    public void setTimeUnit(TimeUnit timeGrouping) {
        this.timeGrouping = timeGrouping;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.goobi.production.flow.statistics.IStatisticalQuestion#getDataTables
     * (org.goobi.production.flow.statistics.IDataSource)
     */
    @Override
    public List<DataTable> getDataTables(String filter, String originalFilter, boolean showClosedProcesses, boolean showArchivedProjects) {

        List<DataTable> allTables = new ArrayList<>();

        // gathering IDs from the filter passed by dataSource
        this.myIDlist = ProcessManager.getIdsForFilter(filter);

        /*
         * ======================================================================
         * ==============
         */

        // a list of DataTables is expected as return Object, even if there is
        // only one
        // Data Table as it is here in this implementation
        DataTable tableStepOpenAndDone = getAllSteps(HistoryEventType.stepOpen);
        tableStepOpenAndDone.setUnitLabel(Helper.getTranslation(this.timeGrouping.getSingularTitle()));
        tableStepOpenAndDone.setName(StatisticsMode.getByClassName(this.getClass()).getTitle() + " (" + Helper.getTranslation("openSteps") + ")");
        tableStepOpenAndDone = tableStepOpenAndDone.getDataTableInverted();
        tableStepOpenAndDone = tableStepOpenAndDone.getDataTableInverted();
        tableStepOpenAndDone.setShowableInChart(false);
        allTables.add(tableStepOpenAndDone);

        tableStepOpenAndDone = getAllSteps(HistoryEventType.stepDone);
        tableStepOpenAndDone.setUnitLabel(Helper.getTranslation(this.timeGrouping.getSingularTitle()));
        tableStepOpenAndDone.setName(StatisticsMode.getByClassName(this.getClass()).getTitle() + " (" + Helper.getTranslation("doneSteps") + ")");
        tableStepOpenAndDone.setShowableInChart(false);
        tableStepOpenAndDone = tableStepOpenAndDone.getDataTableInverted();
        tableStepOpenAndDone = tableStepOpenAndDone.getDataTableInverted();
        allTables.add(tableStepOpenAndDone);

        /*
         * ======================================================================
         * ==============
         */

        // what do we do here?
        // okay ... first we find out how many steps the selected set has
        // finding lowest step and highest step (no step name discrimination)
        Integer uBound;
        Integer uBoundOpen = getMaxStepCount(HistoryEventType.stepOpen);
        Integer uBoundDone = getMaxStepCount(HistoryEventType.stepDone);
        if (uBoundOpen < uBoundDone) {
            uBound = uBoundDone;
        } else {
            uBound = uBoundOpen;
        }

        Integer lBound;
        Integer lBoundOpen = getMinStepCount(HistoryEventType.stepOpen);
        Integer lBoundDone = getMinStepCount(HistoryEventType.stepDone);

        if (lBoundOpen < lBoundDone) {
            lBound = lBoundDone;
        } else {
            lBound = lBoundOpen;
        }

        // then for each step we get both the open and the done count within the
        // selected intervalls and merge it within one table
        for (Integer i = lBound; i <= uBound; i++) {

            DataTable tableStepOpen;
            tableStepOpen = getSpecificSteps(i, HistoryEventType.stepOpen);

            tableStepOpen.setShowableInTable(true);

            DataTable tableStepDone;
            tableStepDone = getSpecificSteps(i, HistoryEventType.stepDone);

            tableStepDone.setShowableInTable(true);

            // to merge we just take each table and dump the entire content in a
            // row for the open step
            DataRow rowOpenSteps = new DataRow(Helper.getTranslation("openSteps") + " " + i.toString());
            for (DataRow dtr : tableStepOpen.getDataRows()) {
                rowOpenSteps.addValue(dtr.getName(), dtr.getValue(0));
            }

            // adding the first row
            String title = "";
            if (tableStepOpen.getName().length() > 0) {
                title = tableStepOpen.getName();
            } else {
                title = tableStepDone.getName();
            }

            tableStepOpenAndDone = new DataTable(Helper.getTranslation("throughput") + " " + Helper.getTranslation("steps") + " " + title);
            tableStepOpenAndDone.addDataRow(rowOpenSteps);

            // row for the done step
            rowOpenSteps = new DataRow(Helper.getTranslation("doneSteps") + " " + i.toString());
            for (DataRow dtr : tableStepDone.getDataRows()) {
                rowOpenSteps.addValue(dtr.getName(), dtr.getValue(0));
            }

            // adding that row
            tableStepOpenAndDone.addDataRow(rowOpenSteps);

            // turning off table rendering
            tableStepOpenAndDone.setShowableInTable(false);

            // inverting the orientation
            tableStepOpenAndDone = tableStepOpenAndDone.getDataTableInverted();
            tableStepOpenAndDone.setUnitLabel(Helper.getTranslation(this.timeGrouping.getSingularTitle()));

            // Dates may not be all in the right order because of it's
            // composition from 2 tables
            List<DataRow> allTempRows = tableStepOpenAndDone.getDataRows();
            // this fixes the sorting problem
            Collections.sort(allTempRows, new DataTableComparator());

            allTables.add(tableStepOpenAndDone);

        }

        return allTables;
    }

    private static class DataTableComparator implements Comparator<DataRow> {
        @Override
        public int compare(DataRow o1, DataRow o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.goobi.production.flow.statistics.IStatisticalQuestion#setCalculationUnit
     * (org.goobi.production.flow.statistics.enums.CalculationUnit)
     */
    @Override
    public void setCalculationUnit(CalculationUnit cu) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.goobi.production.flow.statistics.IStatisticalQuestionLimitedTimeframe
     * #setTimeFrame(java.util.Date, java.util.Date)
     */
    @Override
    public void setTimeFrame(Date timeFrom, Date timeTo) {
        this.timeFilterFrom = timeFrom;
        this.timeFilterTo = timeTo;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.goobi.production.flow.statistics.IStatisticalQuestion#isRendererInverted
     * (de.intranda.commons.chart.renderer.IRenderer)
     */
    @Override
    public Boolean isRendererInverted(IRenderer inRenderer) {
        return inRenderer instanceof ChartRenderer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#
     * getNumberFormatPattern()
     */
    @Override
    public String getNumberFormatPattern() {
        return "#";
    }

    /**
     * returns a DataTable populated with the specified events
     * 
     * @param requestedType
     * @return
     */
    private DataTable getAllSteps(HistoryEventType requestedType) {

        IStepRequestsImprovedDiscrimination sqlGenerator =
                StatisticsFactory.getStepRequestsImprovedDiscrimination(timeFilterFrom, timeFilterTo, timeGrouping, myIDlist);

        // adding time restrictions
        String natSQL = sqlGenerator.getSQL(requestedType, null, true, this.flagIncludeLoops);

        // this one is supposed to make sure, that all possible headers will be
        // thrown out in the first row to build header columns
        String headerFromSQL = sqlGenerator.getSQL(requestedType, null, true, true);

        log.trace(natSQL);
        log.trace(headerFromSQL);

        return buildDataTableFromSQL(natSQL, headerFromSQL);
    }

    /**
     * returns a DataTable populated with the specified events
     * 
     * @param step
     * @param requestedType
     * @return
     */

    private DataTable getSpecificSteps(Integer step, HistoryEventType requestedType) {
        IStepRequestsImprovedDiscrimination sqlGenerator =
                StatisticsFactory.getStepRequestsImprovedDiscrimination(timeFilterFrom, timeFilterTo, timeGrouping, myIDlist);

        // adding time restrictions
        String natSQL = sqlGenerator.getSQL(requestedType, step, true, this.flagIncludeLoops);
        log.trace(natSQL);
        return buildDataTableFromSQL(natSQL, null);
    }

    /**
     * Method generates a DataTable based on the input SQL. Methods success is depending on a very specific data structure ... so don't use it if you
     * don't exactly understand it
     * 
     * 
     * @param natSQL , headerFromSQL -> to be used, if headers need to be read in first in order to get a certain sorting
     * @return DataTable
     */

    // TODO Remove redundant code
    private DataTable buildDataTableFromSQL(String natSQL, String headerFromSQL) {

        // creating header row from headerSQL (gets all columns in one row
        DataRow headerRow = null;
        if (headerFromSQL != null) {
            headerRow = new DataRow(null);

            @SuppressWarnings("rawtypes")
            List headerList = ProcessManager.runSQL(headerFromSQL);
            for (Object obj : headerList) {
                Object[] objArr = (Object[]) obj;
                try {
                    headerRow.setName(new Converter(objArr[3]).getString() + "");
                    headerRow.addValue(new Converter(objArr[2]).getString() + " (" + new Converter(objArr[3]).getString() + ")",
                            (new Converter(objArr[0]).getDouble()));

                } catch (Exception e) {
                    headerRow.addValue(e.getMessage(), Double.valueOf(0));
                }
            }

        }

        @SuppressWarnings("rawtypes")
        List list = ProcessManager.runSQL(natSQL);
        DataTable dtbl = new DataTable("");

        // if headerRow is set then add it to the DataTable to set columns
        // needs to be removed later
        if (headerRow != null) {
            dtbl.addDataRow(headerRow);
        }

        DataRow dataRow = null;

        // each data row comes out as an Array of Objects
        // the only way to extract the data is by knowing
        // in which order they come out

        // checks if intervall has changed which then triggers the start for a
        // new row
        // intervall here is the timeGroup Expression (e.g. "2006/05" or
        // "2006-10-05")
        String observeIntervall = "";

        for (Object obj : list) {
            Object[] objArr = (Object[]) obj;
            try {
                // objArr[1]
                if (!observeIntervall.equals(new Converter(objArr[1]).getString())) {
                    observeIntervall = new Converter(objArr[1]).getString();

                    // row cannot be added before it is filled because the add
                    // process triggers
                    // a testing for header alignement -- this is where we add
                    // it after iterating it first
                    if (dataRow != null) {
                        dtbl.addDataRow(dataRow);
                    }

                    dataRow = new DataRow(null);
                    // setting row name with localized time group and the
                    // date/time extraction based on the group
                    dataRow.setName(new Converter(objArr[1]).getString() + "");
                }
                dataRow.addValue(new Converter(objArr[2]).getString() + " (" + new Converter(objArr[3]).getString() + ")", // NOSONAR, its not nullable here
                        new Converter(objArr[0]).getDouble());

            } catch (Exception e) {
                dataRow.addValue(e.getMessage(), Double.valueOf(0)); // NOSONAR, its not nullable here
            }
        }
        // to add the last row
        if (dataRow != null) {
            dtbl.addDataRow(dataRow);
        }

        // now removing headerRow
        if (headerRow != null) {
            dtbl.removeDataRow(headerRow);

        }

        return dtbl;
    }

    /**
     * method retrieves the highest step order in the requested history range
     * 
     * @param requestedType
     */
    private Integer getMaxStepCount(HistoryEventType requestedType) {
        return 0;
    }

    /**
     * method retrieves the lowest step order in the requested history range
     * 
     * @param requestedType
     */
    private Integer getMinStepCount(HistoryEventType requestedType) {
        return 0;
    }

}
