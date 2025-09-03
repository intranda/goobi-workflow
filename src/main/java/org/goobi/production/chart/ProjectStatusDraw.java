package org.goobi.production.chart;

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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.intranda.commons.chart.results.DataRow;
import de.intranda.commons.chart.results.DataTable;
import de.sub.goobi.helper.FacesContextHelper;
import lombok.extern.log4j.Log4j2;

/*************************************************************************************
 * ProjectStatusDraw class creates and paints the chart depending on given parameters. The value parameters are transfered as
 * {@link ProjectStatusDataTable}-Object. Width and height have to be set as pixel values.
 * 
 * @author Karsten Köhler
 * @author Hendrik Söhnholz
 * @author Steffen Hankiewicz
 * @version 27.10.2009
 *************************************************************************************/

@Log4j2
public class ProjectStatusDraw {
    private static final long MILLICSECS_PER_DAY = 1000L * 60 * 60 * 24;
    private static final int BORDERTOP = 50;
    private static final int BORDERLEFT = 50;
    private static final int BORDERRIGHT = 50;
    private static final int BARWIDTH = 15;
    private static final int BARSPACING = 3 * BARWIDTH;

    private Graphics2D g2d;
    private ProjectStatusDataTable dataTable;

    // dimensions of the whole graphic
    // Only the width is given as an argument. The height of the image should be
    // adjusted according to the number of tasks to be shown.
    private int width;
    private int height;

    // dimensions of the chart
    private int chartWidth;

    private FontMetrics fm;

    /************************************************************************************
     * Instantiates a new ProjectStatusDraw.
     * 
     * @param inDataTable the {@link DataTable} (contains the {@link DataRow}-objects)
     * @param g2d the {@link Graphics2D}-object, where to paint
     * @param width the width of the image
     * @param height the height of the image
     ************************************************************************************/
    public ProjectStatusDraw(ProjectStatusDataTable inDataTable, Graphics2D g2d, int width, int height) {
        this.dataTable = inDataTable;
        this.g2d = g2d;
        this.width = width;
        // Only width will be given. See above.
        this.height = height;
        // FontMetrics is used to measure the height and length of strings
        fm = g2d.getFontMetrics();
    }

    /************************************************************************************
     * Paint the chart.
     ************************************************************************************/
    public void paint() {
        int w; // This is used to determine the width of strings in pixels.

        int maxTitleLength = 0; // Length of longest title string
        for (ProjectTask t : dataTable.getTasks()) {
            w = fm.stringWidth(t.getTitle());
            if (w > maxTitleLength) {
                maxTitleLength = w;
            }
        }

        // Adjust left border to length of task titles
        int borderLeft = maxTitleLength + BORDERLEFT;

        // Compute width of the chart (without the borders)
        chartWidth = width - borderLeft - BORDERRIGHT;

        // Set background color to white
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, height);

        // Determine current date
        Date today = (Calendar.getInstance()).getTime();

        // Dates of project begin and project end
        Date projectBegin = dataTable.getProjectBegin();
        Date projectEnd = dataTable.getProjectEnd();

        // Duration of the project given in days
        int duration = (int) ((projectEnd.getTime() - projectBegin.getTime()) / MILLICSECS_PER_DAY);

        // Current date given in days after project begin
        int datePosition = (int) ((today.getTime() - (dataTable.getProjectBegin()).getTime()) / MILLICSECS_PER_DAY);

        // Format dates as strings using SimpleDateFormat
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");

        g2d.setColor(Color.black);
        // Print date of project begin
        drawLeftAlignedString(dateFormatter.format(projectBegin), borderLeft, BORDERTOP - 1.5 * fm.getHeight());
        // Print date of project end
        drawRightAlignedString(dateFormatter.format(projectEnd), (double) width - BORDERRIGHT, BORDERTOP - 1.5 * fm.getHeight());

        // Read ProjectStatusDataTable and draw the chart
        for (ProjectTask t : dataTable.getTasks()) {

            int y = BORDERTOP + (dataTable.getTaskIndex(t.getTitle())) * BARSPACING;

            g2d.setColor(Color.black);
            // Print task title
            drawRightAlignedString(t.getTitle(), (double) borderLeft - fm.getHeight(), y);

            // Choose color of the bar depending on current date
            ChartColor chartcolor = ChartColor.red;
            int nonNullMaxSteps = t.getStepsMax();
            if (nonNullMaxSteps == 0) {
                nonNullMaxSteps = 1;
            }

            if (t.getStepsCompleted().equals(t.getStepsMax())) {
                chartcolor = ChartColor.green;
            } else if (Math.abs((1.0 * t.getStepsCompleted() / nonNullMaxSteps) - (1.0 * datePosition / duration)) < 0.01) {
                // Deviation of max 1.0 percent leads to a yellow bar
                chartcolor = ChartColor.yellow;
            } else if (t.getStepsCompleted() * duration / nonNullMaxSteps < datePosition) {
                chartcolor = ChartColor.red;
            } else if (t.getStepsCompleted() * duration / nonNullMaxSteps > datePosition) {
                chartcolor = ChartColor.green;
            }

            // Draw the bar
            // fixed width: 15 pixels
            drawHorizontalBar(borderLeft, y, t.getStepsCompleted() * chartWidth / nonNullMaxSteps, BARWIDTH, chartcolor.getColor());

            // Print number of steps completed
            NumberFormat formatter = NumberFormat.getInstance(FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale());

            String stepsCompletedString =
                    formatter.format(t.getStepsCompleted()) + " (" + (formatter.format(t.getStepsCompleted() - (double) t.getConfiguredMax())) + ")";
            if ((borderLeft + t.getStepsCompleted() * chartWidth / nonNullMaxSteps + fm.getHeight()
                    + fm.stringWidth(stepsCompletedString)) >= borderLeft + chartWidth) {
                g2d.setColor(Color.white);
                drawRightAlignedString(stepsCompletedString,
                        borderLeft + t.getStepsCompleted() * chartWidth / (double) nonNullMaxSteps - fm.getHeight(), y);
            } else {
                g2d.setColor(Color.black);
                drawLeftAlignedString(stepsCompletedString,
                        borderLeft + t.getStepsCompleted() * chartWidth / (double) nonNullMaxSteps + fm.getHeight(), y);
            }
        }

        // Draw a line showing the current date
        if (duration == 0) {
            duration = 1;
        }
        if (log.isDebugEnabled()) {
            log.debug(datePosition + " / " + duration);
        }
        float[] dash1 = { 2.0f };
        BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, dash1, 0.0f);
        g2d.setStroke(dashed);
        g2d.setColor(Color.black);
        g2d.draw(new Line2D.Double(borderLeft + (double) datePosition * chartWidth / duration,
                BORDERTOP + dataTable.getNumberOfTasks() * BARSPACING - (double) BARWIDTH, borderLeft + (double) datePosition * chartWidth / duration,
                BORDERTOP - 1d * fm.getHeight()));
        drawCenteredString(dateFormatter.format(today), borderLeft + (double) datePosition * chartWidth / duration, BORDERTOP - 2.5 * fm.getHeight());
    }

    /************************************************************************************
     * Draw horizontal bar with given color
     ************************************************************************************/
    private void drawHorizontalBar(int xpos, int ypos, int length, int width, Color col) {
        int padding = 3;
        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke());
        g2d.draw(new Rectangle2D.Double(xpos, ypos - width / 2d - padding, chartWidth, width + 2d * padding));

        GradientPaint verlauf = new GradientPaint(xpos - length / 2f, ypos, Color.white, xpos + (float) length, ypos, col);
        g2d.setPaint(verlauf);
        g2d.fill(new Rectangle2D.Double(xpos + (double) padding, ypos - width / 2d, length - 2d * padding, width + 1d));

    }

    /************************************************************************************
     * Draw centered string.
     * 
     * @param str the string to show
     * @param xpos the x-position (middle of string)
     * @param ypos the y-position
     ************************************************************************************/
    private void drawCenteredString(String str, double xpos, double ypos) {
        g2d.drawString(str, (int) (xpos - fm.stringWidth(str) / 2.0), (int) (ypos + 0.5 * fm.getAscent() - 1));
    }

    /************************************************************************************
     * Draw left aligned string.
     * 
     * @param str the string to show
     * @param xpos the x-position (start of string)
     * @param ypos the y-position
     ************************************************************************************/
    private void drawLeftAlignedString(String str, double xpos, double ypos) {
        g2d.drawString(str, (int) (xpos), (int) (ypos + 0.5 * fm.getAscent() - 1));
    }

    /************************************************************************************
     * Draw right aligned string.
     * 
     * @param str the string to show
     * @param xpos the x-position (end of string)
     * @param ypos the y-position
     ************************************************************************************/
    private void drawRightAlignedString(String str, double xpos, double ypos) {
        g2d.drawString(str, (int) (xpos - fm.stringWidth(str)), (int) (ypos + 0.5 * fm.getAscent() - 1));
    }

    /************************************************************************************
     * Get size of Image for rendering.
     * 
     * @param count
     * @return size
     ************************************************************************************/
    public static int getImageHeight(int count) {
        return BORDERTOP + count * BARSPACING;
    }
}
