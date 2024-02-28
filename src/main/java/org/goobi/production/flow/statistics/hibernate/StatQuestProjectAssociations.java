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
import java.util.List;

import org.goobi.production.flow.statistics.IStatisticalQuestion;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.enums.TimeUnit;

import de.intranda.commons.chart.renderer.HtmlTableRenderer;
import de.intranda.commons.chart.renderer.IRenderer;
import de.intranda.commons.chart.results.DataRow;
import de.intranda.commons.chart.results.DataTable;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;

/*****************************************************************************
 * Implementation of {@link IStatisticalQuestion}. Statistical Request with predefined Values in data Table
 * 
 * @author Wulf Riebensahm
 ****************************************************************************/
public class StatQuestProjectAssociations implements IStatisticalQuestion {

    private static final long serialVersionUID = 6334004036614755383L;

    /*
     * (non-Javadoc)
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#getDataTables(org.goobi.production.flow.statistics.IDataSource)
     */
    @Override
    public List<DataTable> getDataTables(String filter, String originalFilter, boolean showClosedProcesses, boolean showArchivedProjects) {

        List<DataTable> allTables = new ArrayList<>();

        List<Integer> idList = ProcessManager.getIdsForFilter(filter);

        if (idList == null || idList.isEmpty()) {
            return null;
        }
        String condition = "prozesse.prozesseId in (";

        for (Integer i : idList) {
            condition = condition.concat(i.toString() + ",");
        }
        condition = condition.substring(0, condition.length() - ",".length()) + ")";

        String sql = " select projekte.titel, count(prozesse.projekteId) from prozesse, projekte where prozesse.ProjekteID = projekte.ProjekteID AND "
                + condition + " group by prozesse.projekteId";

        @SuppressWarnings("rawtypes")
        List rawData = ProcessManager.runSQL(sql);
        StringBuilder title = new StringBuilder(StatisticsMode.getByClassName(this.getClass()).getTitle());

        DataTable dtbl = new DataTable(title.toString());
        dtbl.setShowableInPieChart(true);
        DataRow dRow = new DataRow(Helper.getTranslation("count"));

        for (Object obj : rawData) {
            Object[] objArr = (Object[]) obj;
            dRow.addValue((String) objArr[0], new Converter(new Converter(objArr[1]).getInteger()).getDouble());
        }
        dtbl.addDataRow(dRow);

        dtbl.setUnitLabel(Helper.getTranslation("project"));
        allTables.add(dtbl);
        return allTables;
    }

    /*
     * (non-Javadoc)
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#isRendererInverted(de.intranda.commons.chart.renderer.IRenderer)
     */
    @Override
    public Boolean isRendererInverted(IRenderer inRenderer) {
        return inRenderer instanceof HtmlTableRenderer;
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
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#setTimeUnit(org.goobi.production.flow.statistics.enums.TimeUnit)
     */
    @Override
    public void setTimeUnit(TimeUnit timeUnit) {
    }

    /*
     * (non-Javadoc)
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#getNumberFormatPattern()
     */
    @Override
    public String getNumberFormatPattern() {
        return "#";
    }

}
