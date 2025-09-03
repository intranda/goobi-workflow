package de.sub.goobi.statistik;

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
import java.awt.Color;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.goobi.beans.Step;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

public final class StatistikLaufzeitSchritte {

    private StatistikLaufzeitSchritte() {
        // hide implicit public constructor
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Dataset getDiagramm(List inProzesse) {
        DefaultCategoryDataset categoryDataSet = new DefaultCategoryDataset();
        for (Integer processId : (List<Integer>) inProzesse) {
            List<Step> stepList = StepManager.getStepsForProcess(processId);
            String processTitle = ProcessManager.getProcessTitle(processId);
            for (Step step : stepList) {
                if (step.getBearbeitungsbeginn() != null && step.getBearbeitungsende() != null) {
                    String kurztitel = (step.getTitel().length() > 60 ? step.getTitel().substring(0, 60) + "..." : step.getTitel());
                    categoryDataSet.addValue(dateDifference(step.getBearbeitungsbeginn(), step.getBearbeitungsende()), kurztitel, processTitle);
                }

            }
        }

        return categoryDataSet;
    }

    private static int dateDifference(Date datoStart, Date datoEnd) {
        if (datoStart.before(datoEnd)) {
            long difference = datoEnd.getTime() - datoStart.getTime();
            Date datoDifference = new Date(difference);
            Calendar differenz = Calendar.getInstance();
            differenz.setTime(datoDifference);

            return differenz.get(Calendar.DAY_OF_YEAR);
        } else {
            return 1;
        }
    }

    @SuppressWarnings("rawtypes")
    public static String createChart(List inProzesse) throws IOException {
        String imageUrl = System.currentTimeMillis() + ".png";

        DefaultCategoryDataset categoryDataSet = (DefaultCategoryDataset) getDiagramm(inProzesse);
        JFreeChart chart = ChartFactory.createStackedBarChart("", "", "", categoryDataSet, PlotOrientation.HORIZONTAL, true, false, false);

        chart.setBackgroundPaint(Color.white);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setForegroundAlpha(0.6f);
        ChartUtilities.saveChartAsPNG(Paths.get(ConfigurationHelper.getTempImagesPathAsCompleteDirectory() + imageUrl).toFile(), chart, 800,
                inProzesse.size() * 50);

        return imageUrl;
    }

}
