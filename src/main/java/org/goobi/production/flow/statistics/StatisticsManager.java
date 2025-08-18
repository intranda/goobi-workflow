package org.goobi.production.flow.statistics;

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

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Institution;
import org.goobi.beans.User;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.ResultOutput;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.goobi.production.flow.statistics.hibernate.StatQuestThroughput;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultValueDataset;

import de.intranda.commons.chart.results.DataRow;
import de.intranda.commons.chart.results.DataTable;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.statistik.StatistikLaufzeitSchritte;
import de.sub.goobi.statistik.StatistikStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * The Class StatisticsManager organizes all statistical questions by choosing the right implementation depening on {@link StatisticsMode}.
 * 
 * for old statistical question there will be generated jfreechart-datasets
 * 
 * @author Steffen Hankiewicz
 * @version 20.05.2009
 ****************************************************************************/
@Log4j2
public class StatisticsManager implements Serializable {
    private static final long serialVersionUID = -1070332559779545423L;
    /* simple JFreeChart Dataset for the old simple statistics */
    private transient Dataset jfreeDataset;
    @Getter
    private String jfreeImage;
    /* internal StatisticsMode */
    @Getter
    private StatisticsMode statisticMode;
    @Getter
    @Setter
    private Date sourceDateFrom;
    @Getter
    @Setter
    private Date sourceDateTo = new Date();
    private int sourceNumberOfTimeUnits;
    @Getter
    @Setter
    private TimeUnit sourceTimeUnit;
    @Getter
    @Setter
    private TimeUnit targetTimeUnit;
    @Getter
    @Setter
    private ResultOutput targetResultOutput;
    @Getter
    @Setter
    private CalculationUnit targetCalculationUnit;
    @Getter
    @Setter
    private boolean showAverage;
    private Date calculatedStartDate = new Date();
    private Date calculatedEndDate = new Date();
    @Getter
    private List<StatisticsRenderingElement> renderingElements;
    private static Locale myLocale = null;
    private Boolean includeLoops = null;
    private String filter = "";

    private boolean showClosedProcesses;
    private boolean showArchivedProjects;

    private StatisticsManager() {
        super();
    }

    /**
     * public constructor.
     * 
     * @param inMode as {@link StatisticsMode}
     * @param locale
     * @param inFilter
     * @param showClosedProcesses
     * @param showArchivedProjects
     * 
     ****************************************************************************/
    public StatisticsManager(StatisticsMode inMode, Locale locale, String inFilter, boolean showClosedProcesses, boolean showArchivedProjects) {
        this();
        statisticMode = inMode;
        String filter = inFilter;
        this.filter = filter;
        targetResultOutput = ResultOutput.chartAndTable;
        targetTimeUnit = TimeUnit.months;
        sourceTimeUnit = TimeUnit.months;
        this.showClosedProcesses = showClosedProcesses;
        this.showArchivedProjects = showArchivedProjects;
        myLocale = locale;
        /* for backward compatibility create old jfreechart datasets */
        Institution inst = null;
        User user = Helper.getCurrentUser();
        if (user != null && !user.isSuperAdmin()) {
            //             limit result to institution of current user
            inst = user.getInstitution();
        }

        if (Boolean.TRUE.equals(inMode.getIsSimple())) {
            filter = FilterHelper.criteriaBuilder(filter, false, null, null, null, true, false);

            if (!showClosedProcesses) {
                filter = filter + " AND  prozesse.sortHelperStatus <> '100000000' ";
            }
            if (!showArchivedProjects) {
                filter = filter + " AND projekte.projectIsArchived = false ";
            }

            if (inMode == StatisticsMode.SIMPLE_RUNTIME_STEPS) {
                try {
                    List<Integer> processList = ProcessManager.getProcessIdList(null, filter, 0, Integer.MAX_VALUE);
                    jfreeImage = StatistikLaufzeitSchritte.createChart(processList);
                } catch (IOException e) {
                    log.error(e);
                }
            } else {
                List<org.goobi.beans.Process> processList = ProcessManager.getProcesses(null, filter, 0, Integer.MAX_VALUE, inst);
                jfreeDataset = StatistikStatus.getDiagramm(processList);
            }
        }
        if (myLocale == null) {
            myLocale = Locale.of("de");
        }
    }

    /**
     * retrieve the jfreechart Dataset for old statistical questions.
     * 
     * @return {@link Dataset} for charting
     ****************************************************************************/
    public Dataset getJfreeDataset() {
        if (Boolean.TRUE.equals(statisticMode.getIsSimple())) {
            return jfreeDataset;
        } else {
            return new DefaultValueDataset();
        }
    }

    /**
     * calculate statistics and retrieve List off {@link DataRow} from {@link StatisticsMode} for presention and rendering.
     * 
     ****************************************************************************/

    public void calculate() {
        StringBuilder filterString = new StringBuilder().append(FilterHelper.criteriaBuilder(filter, false, null, null, null, true, false));

        if (!showClosedProcesses) {
            filterString.append(" AND  prozesse.sortHelperStatus <> '100000000' ");
        }
        if (!showArchivedProjects) {
            filterString.append(" AND projekte.projectIsArchived = false ");
        }

        /*
         * -------------------------------- if to-date is before from-date, show
         * error message --------------------------------
         */
        if (sourceDateFrom != null && sourceDateTo != null && sourceDateFrom.after(sourceDateTo)) {
            Helper.setMeldung("myStatisticButton", "selectedDatesNotValide", "selectedDatesNotValide");
            return;
        }

        /*
         * -------------------------------- some debugging here
         * --------------------------------
         */
        if (log.isDebugEnabled()) {
            log.debug(sourceDateFrom + " - " + sourceDateTo + " - " + sourceNumberOfTimeUnits + " - " + sourceTimeUnit + "\n" + targetTimeUnit
                    + " - " + targetCalculationUnit + " - " + targetResultOutput + " - " + showAverage);
        }

        /*
         * -------------------------------- calulate the statistical results and
         * save it as List of DataTables (because some statistical questions
         * allow multiple tables and charts) --------------------------------
         */
        IStatisticalQuestion question = statisticMode.getStatisticalQuestion();
        try {
            setTimeFrameToStatisticalQuestion(question);

            // picking up users input regarding loop Options
            if (isRenderLoopOption()) {
                ((StatQuestThroughput) question).setIncludeLoops(includeLoops);
            }
            if (targetTimeUnit != null) {
                question.setTimeUnit(targetTimeUnit);
            }
            if (targetCalculationUnit != null) {
                question.setCalculationUnit(targetCalculationUnit);
            }
            renderingElements = new ArrayList<>();
            List<DataTable> myDataTables = question.getDataTables(filterString.toString(), filter, showClosedProcesses, showArchivedProjects);

            /*
             * -------------------------------- if DataTables exist analyze them
             * --------------------------------
             */
            if (myDataTables != null) {

                /*
                 * -------------------------------- localize time frame for gui
                 * --------------------------------
                 */
                StringBuilder subname = new StringBuilder();
                if (calculatedStartDate != null) {
                    subname.append(DateFormat.getDateInstance(DateFormat.SHORT, myLocale).format(calculatedStartDate));
                }
                subname.append(" - ");
                if (calculatedEndDate != null) {
                    subname.append(DateFormat.getDateInstance(DateFormat.SHORT, myLocale).format(calculatedEndDate));
                }
                if (calculatedStartDate == null && calculatedEndDate == null) {
                    subname = new StringBuilder();
                }

                /*
                 * -------------------------------- run through all DataTables
                 * --------------------------------
                 */
                for (DataTable dt : myDataTables) {
                    dt.setSubname(subname.toString());
                    StatisticsRenderingElement sre = new StatisticsRenderingElement(dt, question);
                    sre.createRenderer(showAverage);
                    renderingElements.add(sre);
                }
            }
        } catch (UnsupportedOperationException e) {
            Helper.setFehlerMeldung("StatisticButton", "errorOccuredWhileCalculatingStatistics", e);
        }
    }

    /**
     * Depending on selected Dates oder time range, set the dates for the statistical question here
     * 
     * @param question the {@link IStatisticalQuestion} where the dates should be set, if it is an implementation of
     *            {@link IStatisticalQuestionLimitedTimeframe}
     *************************************************************************************/
    private void setTimeFrameToStatisticalQuestion(IStatisticalQuestion question) {
        /* only add a date, if correct interface is implemented here */
        if (question instanceof IStatisticalQuestionLimitedTimeframe) {
            /* if a timeunit is selected calculate the dates */
            Calendar cl = Calendar.getInstance();

            if (sourceNumberOfTimeUnits > 0) {

                cl.setFirstDayOfWeek(Calendar.MONDAY);
                cl.setTime(new Date());

                switch (sourceTimeUnit) {
                    case days:
                        calculatedEndDate = calulateStartDateForTimeFrame(cl);
                        calculatedStartDate = calculateEndDateForTimeFrame(cl, Calendar.DATE, -1);
                        break;

                    case weeks:
                        cl.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                        calculatedEndDate = calulateStartDateForTimeFrame(cl);
                        calculatedStartDate = calculateEndDateForTimeFrame(cl, Calendar.DATE, -7);
                        break;

                    case months:
                        cl.set(Calendar.DAY_OF_MONTH, 1);
                        calculatedEndDate = calulateStartDateForTimeFrame(cl);
                        calculatedStartDate = calculateEndDateForTimeFrame(cl, Calendar.MONTH, -1);
                        break;

                    case quarters:
                        cl.set(Calendar.DAY_OF_MONTH, 1);
                        setCalendarToMidnight(cl);
                        while ((cl.get(Calendar.MONTH) + 0) % 3 > 0) {
                            cl.add(Calendar.MONTH, -1);
                        }
                        cl.add(Calendar.MILLISECOND, -1);
                        calculatedEndDate = cl.getTime();
                        calculatedStartDate = calculateEndDateForTimeFrame(cl, Calendar.MONTH, -3);
                        break;

                    case years:
                        cl.set(Calendar.DAY_OF_YEAR, 1);
                        calculatedEndDate = calulateStartDateForTimeFrame(cl);
                        calculatedStartDate = calculateEndDateForTimeFrame(cl, Calendar.YEAR, -1);
                        break;
                    default:
                        log.trace("Unused field in StatisticsManager.setTimeFrameToStatisticalQuestion(): " + sourceTimeUnit);
                }

            } else {
                /* take start and end date */
                cl.setTime(sourceDateTo);
                cl.add(Calendar.DATE, 1);
                setCalendarToMidnight(cl);
                cl.add(Calendar.MILLISECOND, -1);
                calculatedStartDate = sourceDateFrom;
                calculatedEndDate = cl.getTime();
            }
            ((IStatisticalQuestionLimitedTimeframe) question).setTimeFrame(calculatedStartDate, calculatedEndDate);
        } else {
            calculatedStartDate = null;
            calculatedEndDate = null;
        }
    }

    private void setCalendarToMidnight(Calendar cl) {
        cl.set(Calendar.HOUR_OF_DAY, 0);
        cl.set(Calendar.MINUTE, 0);
        cl.set(Calendar.SECOND, 0);
        cl.set(Calendar.MILLISECOND, 0);
    }

    private Date calculateEndDateForTimeFrame(Calendar cl, int calenderUnit, int faktor) {
        cl.add(Calendar.MILLISECOND, 1);
        cl.add(calenderUnit, sourceNumberOfTimeUnits * faktor);
        return cl.getTime();
    }

    private Date calulateStartDateForTimeFrame(Calendar cl) {
        setCalendarToMidnight(cl);
        cl.add(Calendar.MILLISECOND, -1);
        return cl.getTime();
    }

    /**
     * Get all {@link TimeUnit} from enum.
     * 
     * @return all timeUnits
     *************************************************************************************/
    public List<TimeUnit> getAllTimeUnits() {
        return Arrays.asList(TimeUnit.values());
    }

    /**
     * Get all {@link CalculationUnit} from enum.
     * 
     * @return all calculationUnit
     *************************************************************************************/
    public List<CalculationUnit> getAllCalculationUnits() {
        return Arrays.asList(CalculationUnit.values());
    }

    /**
     * Get all {@link ResultOutput} from enum.
     * 
     * @return all resultOutput
     *************************************************************************************/
    public List<ResultOutput> getAllResultOutputs() {
        return Arrays.asList(ResultOutput.values());
    }

    /**
     * @return the sourceNumberOfTimeUnitsAsString
     *************************************************************************************/
    public String getSourceNumberOfTimeUnitsAsString() {
        if (sourceNumberOfTimeUnits == 0) {
            return "";
        } else {
            return String.valueOf(sourceNumberOfTimeUnits);
        }
    }

    /**
     * @param inUnits the sourceNumberOfTimeUnits to set given as String to show an empty text field in gui
     *************************************************************************************/
    public void setSourceNumberOfTimeUnitsAsString(String inUnits) {
        if (StringUtils.isNotBlank(inUnits) && StringUtils.isNumericSpace(inUnits)) {
            sourceNumberOfTimeUnits = Integer.parseInt(inUnits);
        } else {
            sourceNumberOfTimeUnits = 0;
        }
    }

    /**
     * @return includeLoops flag
     */
    public boolean isIncludeLoops() {
        if (includeLoops == null) {
            includeLoops = false;
        }
        return includeLoops;
    }

    public boolean isRenderLoopOption() {
        return statisticMode.isRenderIncludeLoops();
    }

    public void setIncludeLoops(boolean includeLoops) {
        this.includeLoops = includeLoops;
    }

    public static Locale getLocale() {
        if (myLocale != null) {
            return myLocale;
        } else {
            return Locale.of("de");
        }
    }
}
