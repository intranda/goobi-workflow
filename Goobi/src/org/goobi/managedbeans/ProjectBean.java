package org.goobi.managedbeans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi
 * 			- http://digiverso.com
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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.goobi.beans.Institution;
import org.goobi.beans.Project;
import org.goobi.beans.ProjectFileGroup;
import org.goobi.production.chart.IProjectTask;
import org.goobi.production.chart.IProvideProjectTaskList;
import org.goobi.production.chart.ProjectStatusDataTable;
import org.goobi.production.chart.ProjectStatusDraw;
import org.goobi.production.chart.WorkflowProjectTaskList;
import org.goobi.production.flow.statistics.StatisticsManager;
import org.goobi.production.flow.statistics.StatisticsRenderingElement;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.goobi.production.flow.statistics.hibernate.StatQuestProjectProgressData;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;

import de.intranda.commons.chart.renderer.CSVRenderer;
import de.intranda.commons.chart.renderer.ChartRenderer;
import de.intranda.commons.chart.results.ChartDraw.ChartType;
import de.intranda.commons.chart.results.DataRow;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.InstitutionManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import lombok.Getter;
import lombok.Setter;

@ManagedBean(name = "ProjekteForm")
@SessionScoped
public class ProjectBean extends BasicBean {
    private static final long serialVersionUID = 6735912903249358786L;
    private static final Logger logger = Logger.getLogger(ProjectBean.class);

    private Project myProjekt = new Project();
    private ProjectFileGroup myFilegroup;

    // lists accepting the preliminary actions of adding and delting filegroups
    // it needs the execution of commit fileGroups to make these changes permanent
    private List<Integer> newFileGroups = new ArrayList<>();
    private List<Integer> deletedFileGroups = new ArrayList<>();

    private StatisticsManager statisticsManager1 = null;
    private StatisticsManager statisticsManager2 = null;
    private StatisticsManager statisticsManager3 = null;
    private StatisticsManager statisticsManager4 = null;
    private StatQuestProjectProgressData projectProgressData = null;

    private String projectProgressImage;
    private String projectStatImages;
    private String projectStatVolumes;
    private boolean showStatistics;
    private String displayMode = "";

    @Getter
    @Setter
    private String newProjectTitle;

    // making sure its cleaned up
    @Override
    public void finalize() {
        this.Cancel();
    }

    /**
     * this method deletes filegroups by their id's in the list
     * 
     * @param List <Integer> fileGroups
     */
    private void deleteFileGroups(List<Integer> fileGroups) {
        for (Integer id : fileGroups) {
            for (ProjectFileGroup f : this.myProjekt.getFilegroups()) {
                if (f.getId() == id) {
                    this.myProjekt.getFilegroups().remove(f);
                    ProjectManager.deleteProjectFileGroup(f);
                    break;
                }
            }
        }
    }

    /**
     * this method flushes the newFileGroups List, thus makes them permanent and deletes those marked for deleting, making the removal permanent
     */
    private void commitFileGroups() {
        // resetting the List of new fileGroups
        this.newFileGroups = new ArrayList<>();
        // deleting the fileGroups marked for deletion
        deleteFileGroups(this.deletedFileGroups);
        // resetting the List of fileGroups marked for deletion
        this.deletedFileGroups = new ArrayList<>();
    }

    /**
     * this needs to be executed in order to rollback adding of filegroups
     * 
     * @return
     */
    public String Cancel() {
        // flushing new fileGroups
        deleteFileGroups(this.newFileGroups);
        // resetting the List of new fileGroups
        this.newFileGroups = new ArrayList<>();
        // resetting the List of fileGroups marked for deletion
        this.deletedFileGroups = new ArrayList<>();
        this.projectProgressImage = null;
        this.projectStatImages = null;
        this.projectStatVolumes = null;
        displayMode = "";
        return "project_all";
    }

    public String Neu() {
        newProjectTitle = "";
        this.myProjekt = new Project();
        return "project_edit";
    }

    public String Speichern() {
        // call this to make saving and deleting permanent

        if (!checkProjectTitle()) {
            return "";
        }

        this.commitFileGroups();
        try {
            ProjectManager.saveProject(this.myProjekt);
            paginator.load();
            displayMode = "";
            return FilterKein();
        } catch (DAOException e) {
            Helper.setFehlerMeldung("could not save", e.getMessage());
            return "";
        }
    }

    public String Apply() {
        // call this to make saving and deleting permanent
        logger.trace("Apply wird aufgerufen...");
        if (!checkProjectTitle()) {
            return "";
        }
        this.commitFileGroups();
        try {
            ProjectManager.saveProject(this.myProjekt);
            paginator.load();
            return "";
        } catch (DAOException e) {
            Helper.setFehlerMeldung("could not save", e.getMessage());
            return "";
        }
    }

    private boolean checkProjectTitle() {
        if (StringUtils.isBlank(myProjekt.getTitel()) || !myProjekt.getTitel().equals(newProjectTitle)) {
            if (ProjectManager.countProjectTitle(newProjectTitle) != 0) {
                Helper.setFehlerMeldung("project_error_titleIsInUse");
                return false;
            } else {
                myProjekt.setTitel(newProjectTitle);
            }
        }
        return true;
    }

    public String Loeschen() {
        if (ProjectManager.getNumberOfProcessesForProject(myProjekt.getId()) != 0) {
            Helper.setFehlerMeldung("projectssAssignedError");
            return "";
        }

        try {
            ProjectManager.deleteProject(this.myProjekt);
            paginator.load();
            displayMode = "";
        } catch (DAOException e) {
            Helper.setFehlerMeldung("could not delete", e.getMessage());
            return "";
        }

        return FilterKein();

    }

    public String FilterKein() {
        displayMode = "";
        sortierung = "Titel";
        ProjectManager m = new ProjectManager();
        paginator = new DatabasePaginator(sortierung, filter, m, "project_all");
        return "project_all";
    }

    public String FilterKeinMitZurueck() {
        FilterKein();
        return this.zurueck;
    }

    public String filegroupAdd() {
        this.myFilegroup = new ProjectFileGroup();
        this.myFilegroup.setProject(this.myProjekt);
        this.newFileGroups.add(this.myFilegroup.getId());
        return "";
    }

    public String filegroupSave() {
        if (this.myProjekt.getFilegroups() == null) {
            this.myProjekt.setFilegroups(new ArrayList<ProjectFileGroup>());
        }
        if (!this.myProjekt.getFilegroups().contains(this.myFilegroup)) {
            this.myProjekt.getFilegroups().add(this.myFilegroup);
        }
        if (myProjekt.getId() == null) {
            try {
                ProjectManager.saveProject(myProjekt);
            } catch (DAOException e) {
                logger.error(e);
            }
        }
        ProjectManager.saveProjectFileGroup(myFilegroup);
        return "";
    }

    public String filegroupEdit() {
        return "";
    }

    public String filegroupCancel() {
        return "";
    }

    public String filegroupDelete() {
        // to be deleted fileGroups ids are listed
        // and deleted after a commit
        this.deletedFileGroups.add(this.myFilegroup.getId());
        // original line
        // myProjekt.getFilegroups().remove(myFilegroup);
        return "";

    }

    public Project getMyProjekt() {
        return this.myProjekt;
    }

    public void setMyProjekt(Project inProjekt) {
        // has to be called if a page back move was done
        this.Cancel();
        this.myProjekt = inProjekt;
        newProjectTitle = myProjekt.getTitel();
    }

    /**
     * The need to commit deleted fileGroups only after the save action requires a filter, so that those filegroups marked for delete are not shown
     * anymore
     * 
     * @return modified ArrayList
     */
    public ArrayList<ProjectFileGroup> getFileGroupList() {
        ArrayList<ProjectFileGroup> filteredFileGroupList = new ArrayList<>(this.myProjekt.getFilegroups());

        for (Integer id : this.deletedFileGroups) {
            for (ProjectFileGroup f : this.myProjekt.getFilegroups()) {
                if (f.getId() == id) {
                    filteredFileGroupList.remove(f);
                    break;
                }
            }
        }
        return filteredFileGroupList;
    }

    public ProjectFileGroup getMyFilegroup() {
        return this.myFilegroup;
    }

    public void setMyFilegroup(ProjectFileGroup myFilegroup) {
        this.myFilegroup = myFilegroup;
    }

    /**
     * 
     * @return instance of {@link StatisticsMode.PRODUCTION} {@link StatisticsManager}
     */

    public StatisticsManager getStatisticsManager1() {
        if (this.statisticsManager1 == null) {
            this.statisticsManager1 = new StatisticsManager(StatisticsMode.PRODUCTION, FacesContext.getCurrentInstance().getViewRoot().getLocale(),
                    "\"project:" + StringEscapeUtils.escapeSql(myProjekt.getTitel()) + "\"");
        }
        return this.statisticsManager1;
    }

    /**
     * 
     * @return instance of {@link StatisticsMode.THROUGHPUT} {@link StatisticsManager}
     */
    public StatisticsManager getStatisticsManager2() {
        if (this.statisticsManager2 == null) {
            this.statisticsManager2 = new StatisticsManager(StatisticsMode.THROUGHPUT, FacesContext.getCurrentInstance().getViewRoot().getLocale(),
                    "\"project:" + StringEscapeUtils.escapeSql(myProjekt.getTitel()) + "\"");
        }
        return this.statisticsManager2;
    }

    /**
     * 
     * @return instance of {@link StatisticsMode.CORRECTIONS} {@link StatisticsManager}
     */
    public StatisticsManager getStatisticsManager3() {
        if (this.statisticsManager3 == null) {
            this.statisticsManager3 = new StatisticsManager(StatisticsMode.CORRECTIONS, FacesContext.getCurrentInstance().getViewRoot().getLocale(),
                    "\"project:" + StringEscapeUtils.escapeSql(myProjekt.getTitel()) + "\"");
        }
        return this.statisticsManager3;
    }

    /**
     * 
     * @return instance of {@link StatisticsMode.STORAGE} {@link StatisticsManager}
     */
    public StatisticsManager getStatisticsManager4() {
        if (this.statisticsManager4 == null) {
            this.statisticsManager4 = new StatisticsManager(StatisticsMode.STORAGE, FacesContext.getCurrentInstance().getViewRoot().getLocale(),
                    "\"project:" + StringEscapeUtils.escapeSql(myProjekt.getTitel()) + "\"");
        }
        return this.statisticsManager4;
    }

    /**
     * generates values for count of volumes and images for statistics
     */

    public void GenerateValuesForStatistics() {
        String projectFilter = FilterHelper.criteriaBuilder("\"project:" + StringEscapeUtils.escapeSql(myProjekt.getTitel()) + "\"", false, null,
                null, null, true, false) + " AND prozesse.istTemplate = false ";
        Long images = ProcessManager.getSumOfFieldValue("sortHelperImages", projectFilter);
        Long volumes = ProcessManager.getCountOfFieldValue("sortHelperImages", projectFilter);
        //		ProjectionList pl = Projections.projectionList();
        //		pl.add(Projections.sum("sortHelperImages"));
        //		pl.add(Projections.count("sortHelperImages"));
        //		crit.setProjection(pl);
        //		List list = crit.list();
        //		Long images = 0l;
        //		Long volumes = 0l;
        //		for (Object obj : list) {
        //			Object[] row = (Object[]) obj;
        //			images = (Long) row[0];
        //			volumes = (Long) row[1];
        //		}
        this.myProjekt.setNumberOfPages(images.intValue());
        this.myProjekt.setNumberOfVolumes(volumes.intValue());
    }

    /**
     * calculate pages per volume depending on given values, requested multiple times via ajax
     * 
     * @return Integer of calculation
     */
    public Integer getCalcImagesPerVolume() {
        int volumes = this.myProjekt.getNumberOfVolumes();
        int pages = this.myProjekt.getNumberOfPages();
        if (volumes == 0) {
            return pages;
        }
        int i = pages / volumes;
        return i;
    }

    /**
     * get calculated duration from start and end date
     * 
     * @return String of duration
     */
    public Integer getCalcDuration() {
        DateTime start = new DateTime(this.myProjekt.getStartDate().getTime());
        DateTime end = new DateTime(this.myProjekt.getEndDate().getTime());
        return Months.monthsBetween(start, end).getMonths();
    }

    /**
     * calculate throughput of volumes per year
     * 
     * @return calculation
     */

    public Integer getCalcThroughputPerYear() {
        DateTime start = new DateTime(this.myProjekt.getStartDate().getTime());
        DateTime end = new DateTime(this.myProjekt.getEndDate().getTime());
        int years = Years.yearsBetween(start, end).getYears();
        if (years < 1) {
            years = 1;
        }
        return this.myProjekt.getNumberOfVolumes() / years;
    }

    /**
     * calculate throughput of pages per year
     * 
     * @return calculation
     */
    public Integer getCalcThroughputPagesPerYear() {
        DateTime start = new DateTime(this.myProjekt.getStartDate().getTime());
        DateTime end = new DateTime(this.myProjekt.getEndDate().getTime());
        int years = Years.yearsBetween(start, end).getYears();
        if (years < 1) {
            years = 1;
        }
        return this.myProjekt.getNumberOfPages() / years;
    }

    /**
     * calculate throughput of volumes per quarter
     * 
     * @return calculation
     */

    public Integer getCalcThroughputPerQuarter() {
        int month = getCalcDuration();
        if (month < 1) {
            month = 1;
        }
        return this.myProjekt.getNumberOfVolumes() * 3 / month;
    }

    /**
     * calculate throughput of pages per quarter
     * 
     * @return calculation
     */
    public Integer getCalcTroughputPagesPerQuarter() {
        int month = getCalcDuration();
        if (month < 1) {
            month = 1;
        }
        return this.myProjekt.getNumberOfPages() * 3 / month;
    }

    /**
     * calculate throughput of volumes per month
     * 
     * @return calculation
     */
    public Integer getCalcThroughputPerMonth() {
        int month = getCalcDuration();
        if (month < 1) {
            month = 1;
        }
        return this.myProjekt.getNumberOfVolumes() / month;
    }

    /**
     * calculate throughput of pages per month
     * 
     * @return calculation
     */
    public Integer getCalcThroughputPagesPerMonth() {
        int month = getCalcDuration();
        if (month < 1) {
            month = 1;
        }
        return this.myProjekt.getNumberOfPages() / month;
    }

    private Double getThroughputPerDay() {
        DateTime start = new DateTime(this.myProjekt.getStartDate().getTime());
        DateTime end = new DateTime(this.myProjekt.getEndDate().getTime());
        Weeks weeks = Weeks.weeksBetween(start, end);
        logger.trace(weeks.getWeeks());
        int days = (weeks.getWeeks() * 5);

        if (days < 1) {
            days = 1;
        }
        double back = (double) this.myProjekt.getNumberOfVolumes() / (double) days;
        return back;
    }

    /**
     * calculate throughput of volumes per day
     * 
     * @return calculation
     */

    public Integer getCalcThroughputPerDay() {
        return Math.round(this.getThroughputPerDay().floatValue());
    }

    /**
     * calculate throughput of pages per day
     * 
     * @return calculation
     */

    private Double getThroughputPagesPerDay() {
        DateTime start = new DateTime(this.myProjekt.getStartDate().getTime());
        DateTime end = new DateTime(this.myProjekt.getEndDate().getTime());

        Weeks weeks = Weeks.weeksBetween(start, end);
        int days = (weeks.getWeeks() * 5);
        if (days < 1) {
            days = 1;
        }
        double back = (double) this.myProjekt.getNumberOfPages() / (double) days;
        return back;
    }

    /**
     * calculate throughput of pages per day
     * 
     * @return calculation
     */
    public Integer getCalcPagesPerDay() {
        return Math.round(this.getThroughputPagesPerDay().floatValue());
    }

    /**
     * @returns a StatQuestThroughputCommonFlow for the generation of projekt progress data
     */
    public StatQuestProjectProgressData getProjectProgressInterface() {

        if (this.projectProgressData == null) { // initialize datasource with default selection
            this.projectProgressData = new StatQuestProjectProgressData();
        }
        synchronized (this.projectProgressData) {
            try {

                this.projectProgressData.setCommonWorkflow(this.myProjekt.getWorkFlow());
                this.projectProgressData.setCalculationUnit(CalculationUnit.volumes);
                this.projectProgressData.setRequiredDailyOutput(this.getThroughputPerDay());
                this.projectProgressData.setTimeFrame(this.getMyProjekt().getStartDate(), this.getMyProjekt().getEndDate());
                this.projectProgressData
                .setDataSource(FilterHelper.criteriaBuilder("\"project:" + StringEscapeUtils.escapeSql(myProjekt.getTitel()) + "\"", false,
                        null, null, null, true, false) + " AND prozesse.istTemplate = false ");

                if (this.projectProgressImage == null) {
                    this.projectProgressImage = "";
                }
            } catch (Exception e) {
                // this.projectProgressData = null;
            }
        }
        return this.projectProgressData;
    }

    /**
     * 
     * @return true if calculation is finished
     */

    public Boolean getIsProgressCalculated() {
        if (this.projectProgressData == null) {
            return false;
        }
        return this.projectProgressData.isDataComplete();
    }

    /**
     * 
     * @return path to rendered image of statistics
     */
    public String getProjectProgressImage() {

        if (this.projectProgressImage == null || this.projectProgressData == null || this.projectProgressData.hasChanged()) {
            try {
                calcProgressCharts();
            } catch (Exception e) {
                Helper.setFehlerMeldung("noImageRendered");
            }
        }
        return this.projectProgressImage;
    }

    private void calcProgressCharts() {
        if (this.getProjectProgressInterface().isDataComplete()) {
            ChartRenderer cr = new ChartRenderer();
            cr.setChartType(ChartType.LINE);
            cr.setDataTable(this.projectProgressData.getSelectedTable());
            BufferedImage bi = (BufferedImage) cr.getRendering();
            this.projectProgressImage = System.currentTimeMillis() + ".png";
            String localImagePath = ConfigurationHelper.getTempImagesPathAsCompleteDirectory();

            Path outputfile = Paths.get(localImagePath + this.projectProgressImage);
            try {
                ImageIO.write(bi, "png", outputfile.toFile());
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("couldn't write project progress chart to file", e);
                }
            }
        }
    }

    /*********************************************************
     * Static Statistics
     *********************************************************/

    public String getProjectStatImages() throws IOException, InterruptedException {
        if (this.projectStatImages == null) {
            this.projectStatImages = System.currentTimeMillis() + "images.png";
            calcProjectStats(this.projectStatImages, true);
        }
        return this.projectStatImages;
    }

    /**
     * 
     * @return string of image file projectStatVolumes
     * @throws IOException
     * @throws InterruptedException
     */

    public String getProjectStatVolumes() throws IOException, InterruptedException {
        if (this.projectStatVolumes == null) {
            this.projectStatVolumes = System.currentTimeMillis() + "volumes.png";
            calcProjectStats(this.projectStatVolumes, false);
        }
        return this.projectStatVolumes;
    }

    private synchronized void calcProjectStats(String inName, Boolean countImages) throws IOException {
        int width = 750;
        Date start = this.myProjekt.getStartDate();
        Date end = this.myProjekt.getEndDate();

        Integer inMax;
        if (countImages) {
            inMax = this.myProjekt.getNumberOfPages();
        } else {
            inMax = this.myProjekt.getNumberOfVolumes();
        }

        ProjectStatusDataTable pData = new ProjectStatusDataTable(this.myProjekt.getTitel(), start, end);

        IProvideProjectTaskList ptl = new WorkflowProjectTaskList();

        List<? extends IProjectTask> tasklist = ptl.calculateProjectTasks(this.myProjekt, countImages, inMax);
        for (IProjectTask pt : tasklist) {
            pData.addTask(pt);
        }

        // Determine height of the image
        int height = ProjectStatusDraw.getImageHeight(pData.getNumberOfTasks());

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        ProjectStatusDraw projectStatusDraw = new ProjectStatusDraw(pData, g2d, width, height);
        projectStatusDraw.paint();

        // write image to temporary file
        String localImagePath = ConfigurationHelper.getTempImagesPathAsCompleteDirectory();
        Path outputfile = Paths.get(localImagePath + inName);
        ImageIO.write(image, "png", outputfile.toFile());
    }

    private StatisticsRenderingElement myCurrentTable;

    public void setMyCurrentTable(StatisticsRenderingElement myCurrentTable) {
        this.myCurrentTable = myCurrentTable;
    }

    public StatisticsRenderingElement getMyCurrentTable() {
        return this.myCurrentTable;
    }

    public void downloadStatisticsAsExcel() {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {

            /*
             *  Vorbereiten der Header-Informationen
             */
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            try {
                ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
                String contentType = servletContext.getMimeType("export.xls");
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=\"export.xls\"");
                ServletOutputStream out = response.getOutputStream();
                HSSFWorkbook wb = (HSSFWorkbook) this.myCurrentTable.getExcelRenderer().getRendering();
                wb.write(out);
                out.flush();
                facesContext.responseComplete();

            } catch (IOException e) {

            }
        }
    }

    public void downloadStatisticsAsCsv() {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        CSVPrinter csvFilePrinter = null;
        if (!facesContext.getResponseComplete()) {
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            try {
                ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
                String contentType = servletContext.getMimeType("export.csv");
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=\"export.csv\"");

                CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
                csvFilePrinter = new CSVPrinter(response.getWriter(), csvFileFormat);
                CSVRenderer csvr = this.myCurrentTable.getCsvRenderer();

                // add all headers
                List<Object> csvHead = new ArrayList<>();
                csvHead.add(csvr.getDataTable().getUnitLabel());
                for (String s : csvr.getDataTable().getDataRows().get(0).getLabels()) {
                    csvHead.add(s);
                }
                csvFilePrinter.printRecord(csvHead);

                // add all rows
                for (DataRow dr : csvr.getDataTable().getDataRows()) {
                    List<Object> csvColumns = new ArrayList<>();
                    csvColumns.add(dr.getName());
                    for (int j = 0; j < dr.getNumberValues(); j++) {
                        csvColumns.add(dr.getValue(j));
                    }
                    csvFilePrinter.printRecord(csvColumns);
                }

                facesContext.responseComplete();
            } catch (Exception e) {

            } finally {
                try {
                    csvFilePrinter.close();
                } catch (IOException e) {

                }
            }
        }
    }

    /*************************************************************************************
     * Getter for showStatistics
     * 
     * @return the showStatistics
     *************************************************************************************/
    public boolean getShowStatistics() {
        return this.showStatistics;
    }

    /**************************************************************************************
     * Setter for showStatistics
     * 
     * @param showStatistics the showStatistics to set
     **************************************************************************************/
    public void setShowStatistics(boolean showStatistics) {
        this.showStatistics = showStatistics;
    }

    public String getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(String displayMode) {
        this.displayMode = displayMode;
    }

    public Integer getProjektAuswahl() {
        if (this.myProjekt != null && myProjekt.getId() != null) {
            return this.myProjekt.getId();
        } else {
            return Integer.valueOf(0);
        }
    }

    public void setProjektAuswahl(Integer inProjektAuswahl) {
        if (inProjektAuswahl.intValue() != 0) {
            try {
                Project p = ProjectManager.getProjectById(inProjektAuswahl);
                if (myProjekt == null || myProjekt.getId() == null || !myProjekt.equals(p)) {
                    myProjekt = p;
                    resetStatistics();
                }
            } catch (DAOException e) {
                Helper.setFehlerMeldung("Projekt kann nicht zugewiesen werden", "");
                logger.error(e);
            }
        } else {
            myProjekt = null;
            resetStatistics();
        }
    }

    public void resetStatistics() {
        statisticsManager1 = null;
        statisticsManager2 = null;
        statisticsManager3 = null;
        statisticsManager4 = null;
        projectProgressData = null;

        projectProgressImage = null;
        projectStatImages = null;
        projectStatVolumes = null;
    }

    public String cloneProject() {
        myProjekt.clone();
        Cancel();
        return FilterKein();
    }

    public Integer getCurrentInstitutionID() {
        if (this.myProjekt.getInstitution() != null) {
            return this.myProjekt.getInstitution().getId();
        } else {
            return Integer.valueOf(0);
        }
    }

    public void setCurrentInstitutionID(Integer id) {
        if (id != null && id.intValue() != 0) {
            Institution institution = InstitutionManager.getInstitutionById(id);
            myProjekt.setInstitution(institution);
        }
    }

    public List<SelectItem> getInstitutionsAsSelectList() throws DAOException {
        List<SelectItem> institutions = new ArrayList<>();
        List<Institution> temp = null;
        //        LoginBean login = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
        //        if (login != null && !login.hasRole(UserRole.Workflow_General_Show_All_Projects.name())) {
        temp = InstitutionManager.getAllInstitutionsAsList();
        //        } else {
        //            temp = ProjectManager.getAllProjects();
        //
        //        }

        for (Institution proj : temp) {
            institutions.add(new SelectItem(proj.getId(), proj.getShortName(), null));
        }
        return institutions;
    }

}
