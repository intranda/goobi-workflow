package org.goobi.managedbeans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
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
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.production.enums.LogType;
import org.goobi.production.export.ExportDocket;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.goobi.beans.Batch;
import org.goobi.beans.LogEntry;
import org.goobi.beans.Process;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.BatchProcessHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.Data;
import lombok.EqualsAndHashCode;

//import de.sub.goobi.persistence.ProzessDAO;

@ManagedBean(name = "BatchForm")
@SessionScoped
@Data
@EqualsAndHashCode(callSuper = false)
public class BatchBean extends BasicBean {

    private static final long serialVersionUID = 8234897225425856549L;

    private static final Logger logger = Logger.getLogger(BatchBean.class);

    private List<Process> currentProcesses = new ArrayList<>();
    private List<Process> selectedProcesses = new ArrayList<>();
    private List<Batch> currentBatches = new ArrayList<>();
    private List<Batch> selectedBatches = new ArrayList<>();
    private String batchfilter;
    private String processfilter;
    //	private IEvaluableFilter myFilteredDataSource;

    //	private ProzessDAO dao = new ProzessDAO();
    private String modusBearbeiten = "";

    private BatchProcessHelper batchHelper;

    private int getBatchMaxSize() {
        int batchsize = ConfigurationHelper.getInstance().getBatchMaxSize();
        return batchsize;
    }

    public void loadBatchData() {
        this.currentBatches = new ArrayList<>();
        this.selectedBatches = new ArrayList<>();
        for (Process p : this.selectedProcesses) {
            if (p.getBatch() != null && !this.currentBatches.contains(p.getBatch())) {
                generateBatch(p.getBatch());
            }
        }
    }

    private Batch generateBatch(Batch batch) {
        //		Session session = Helper.getHibernateSession();
        String filter = "";
        if (batch != null) {
            filter = " batchID = " + batch.getBatchId() + " AND istTemplate = false ";
        } else {
            filter = " batchID is NULL AND istTemplate = false ";
        }

        String msg1 = Helper.getTranslation("batch");
        String msg2 = Helper.getTranslation("prozesse");
        if (batch != null) {
            if (StringUtils.isNotBlank(batch.getBatchName())) {
                batch.setBatchLabel(batch.getBatchName() + " (" + ProcessManager.countProcesses(filter) + " " + msg2 + ")");
            } else {
                String text = msg1 + " " + batch.getBatchId() + " (" + ProcessManager.countProcesses(filter) + " " + msg2 + ")";

                batch.setBatchLabel(text);
            }
            return batch;
        } else {
            String text = Helper.getTranslation("withoutBatch") + " (" + ProcessManager.countProcesses(filter) + " " + msg2 + ")";
            batch = new Batch();
            batch.setBatchId(null);
            batch.setBatchLabel(text);
            return batch;
        }
    }

    public void loadProcessData() {

        String filter = " istTemplate = false ";

        List<Integer> ids = new ArrayList<>();
        for (Batch b : this.selectedBatches) {
            if (b != null) {
                ids.add(b.getBatchId());
            }
        }

        if (this.selectedBatches.size() > 0) {

            if (ids.contains(null)) {
                filter += " AND batchID is null ";
            } else {
                filter += " AND (";
                for (Integer id : ids) {
                    filter += " batchID = " + id + " OR";
                }
                filter = filter.substring(0, filter.length() - 3) + ")";
            }
        }
        this.currentProcesses = ProcessManager.getProcesses(null, filter, 0, getBatchMaxSize());
    }

    public void filterProcesses() {

        if (this.processfilter == null) {
            this.processfilter = "";
        }
        String filter = FilterHelper.criteriaBuilder(processfilter, false, null, null, null, true, false);
        if (!filter.isEmpty()) {
            filter += " AND ";
        }
        filter += " istTemplate = false ";

        this.currentProcesses = ProcessManager.getProcesses("prozesse.titel", filter, 0, getBatchMaxSize());
    }

    public void filterBatches() {

        if (StringUtils.isNotBlank(batchfilter)) {
            List<Batch> allBatches = ProcessManager.getBatches(getBatchMaxSize());
            this.currentBatches = new ArrayList<Batch>();
            for (Batch in : allBatches) {
                if ((in.getBatchName() != null && in.getBatchName().toLowerCase().contains(this.batchfilter.toLowerCase())) || Integer.toString(in.getBatchId()).contains(
                        this.batchfilter)) {
                    this.currentBatches.add(generateBatch(in));
                }
            }
        } else {
            currentBatches = ProcessManager.getBatches(getBatchMaxSize());
        }
        for (Batch in : currentBatches) {
            generateBatch(in);
        }
    }

    public List<SelectItem> getCurrentBatchesAsSelectItems() {
        List<SelectItem> answer = new ArrayList<SelectItem>();
        for (Batch p : this.currentBatches) {
            answer.add(new SelectItem(String.valueOf(p.getBatchId()), p.getBatchLabel()));
        }
        return answer;
    }

    public void setSelectedBatchIds(List<String> processIds) {
        selectedBatches = new ArrayList<>();
        for (String idString : processIds) {
            Integer id = new Integer(idString);

            selectedBatches.add(ProcessManager.getBatchById(id));
        }
    }

    public List<String> getSelectedBatchIds() {
        List<String> idList = new ArrayList<String>();
        for (Batch p : selectedBatches) {
            idList.add(String.valueOf(p.getBatchId()));
        }
        return idList;
    }

    public List<SelectItem> getCurrentProcessesAsSelectItems() {
        List<SelectItem> answer = new ArrayList<SelectItem>();
        for (Process p : this.currentProcesses) {
            answer.add(new SelectItem(String.valueOf(p.getId()), p.getTitel()));
        }
        return answer;
    }

    public void setSelectedProcessIds(List<String> processIds) {
        selectedProcesses = new ArrayList<Process>();
        for (String idString : processIds) {
            Integer id = new Integer(idString);

            selectedProcesses.add(ProcessManager.getProcessById(id));
        }
    }

    public List<String> getSelectedProcessIds() {
        List<String> idList = new ArrayList<String>();
        for (Process p : selectedProcesses) {
            idList.add(String.valueOf(p.getId()));
        }
        return idList;
    }

    public String FilterAlleStart() {
        filterBatches();
        filterProcesses();
        return "batch_all";
    }

    public String downloadDocket() {
        if (logger.isDebugEnabled()) {
            logger.debug("generate docket for process list");
        }
        String rootpath = ConfigurationHelper.getInstance().getXsltFolder();
        Path xsltfile = Paths.get(rootpath, "docket_multipage.xsl");
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        List<Process> docket = new ArrayList<Process>();
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung("noBatchSelected");
        } else if (this.selectedBatches.size() == 1) {

            //            Session session = Helper.getHibernateSession();
            //            Criteria crit = session.createCriteria(Process.class);
            docket = ProcessManager.getProcesses(null, " istTemplate = false AND batchID = " + this.selectedBatches.get(0).getBatchId(), 0,
                    getBatchMaxSize());

        } else {
            Helper.setFehlerMeldung("tooManyBatchesSelected");
        }
        if (docket.size() > 0) {
            if (!facesContext.getResponseComplete()) {
                HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
                String fileName = "batch_docket" + ".pdf";
                ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
                String contentType = servletContext.getMimeType(fileName);
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");

                try {
                    ServletOutputStream out = response.getOutputStream();
                    ExportDocket ern = new ExportDocket();
                    ern.startExport(docket, out, xsltfile.toString());
                    out.flush();
                } catch (IOException e) {
                    logger.error("IOException while exporting run note", e);
                }

                facesContext.responseComplete();
            }
        }
        return "";
    }

    public void deleteBatch() {
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung("noBatchSelected");
        } else if (this.selectedBatches.size() == 1) {
            if (this.selectedBatches.get(0) != null && !this.selectedBatches.get(0).equals("") && !this.selectedBatches.get(0).equals("null")) {

                List<Process> deleteList = ProcessManager.getProcesses(null, " istTemplate = false AND batchID = " + this.selectedBatches.get(0)
                        .getBatchId(), 0, getBatchMaxSize());
                {
                    for (Process p : deleteList) {
                        p.setBatch(null);
                        ProcessManager.saveProcessInformation(p);

                    }

                }
            } else {
                Helper.setFehlerMeldung("noBatchSelected");
            }
        } else {
            Helper.setFehlerMeldung("tooḾanyBatchesSelected");
        }
        FilterAlleStart();

    }

    public void addProcessesToBatch() {
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung("noBatchSelected");
        } else if (this.selectedBatches.size() > 1) {
            Helper.setFehlerMeldung("tooḾanyBatchesSelected");
        } else {
            try {
                //				Session session = Helper.getHibernateSession();
                Batch batch = this.selectedBatches.get(0);
                for (Process p : this.selectedProcesses) {
                    p.setBatch(batch);

                    LogEntry logEntry = new LogEntry();
                    logEntry.setContent("added process to batch " + batch.getBatchId());
                    logEntry.setCreationDate(new Date());
                    logEntry.setProcessId(p.getId());
                    logEntry.setType(LogType.DEBUG);
                    logEntry.setUserName("-batch-");
                    ProcessManager.saveLogEntry(logEntry);

                    ProcessManager.saveProcessInformation(p);
                }

            } catch (Exception e) {
                Helper.setFehlerMeldung("noBatchSelected");
            }
        }
        FilterAlleStart();
    }

    public void removeProcessesFromBatch() {
        //		Session session = Helper.getHibernateSession();
        for (Process p : this.selectedProcesses) {
            LogEntry logEntry = new LogEntry();
            logEntry.setContent("removed process from batch " + p.getBatch().getBatchId());
            logEntry.setCreationDate(new Date());
            logEntry.setProcessId(p.getId());
            logEntry.setType(LogType.DEBUG);
            logEntry.setUserName("-batch-");
            ProcessManager.saveLogEntry(logEntry);

            p.setBatch(null);
            ProcessManager.saveProcessInformation(p);
        }

        FilterAlleStart();
    }

    public void createNewBatch() {
        if (this.selectedProcesses.size() > 0) {

            Batch batch = new Batch();
            for (Process p : this.selectedProcesses) {
                p.setBatch(batch);
                ProcessManager.saveProcessInformation(p);
                LogEntry logEntry = new LogEntry();
                logEntry.setContent("added process to batch " + batch.getBatchId());
                logEntry.setCreationDate(new Date());
                logEntry.setProcessId(p.getId());
                logEntry.setType(LogType.DEBUG);
                logEntry.setUserName("-batch-");
                ProcessManager.saveLogEntry(logEntry);
            }

        }
        FilterAlleStart();
    }

    public String editProperties() {
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung("noBatchSelected");
            return "";
        } else if (this.selectedBatches.size() > 1) {
            Helper.setFehlerMeldung("tooḾanyBatchesSelected");
            return "";
        } else {
            if (this.selectedBatches.get(0) != null && !this.selectedBatches.get(0).equals("") && !this.selectedBatches.get(0).equals("null")) {
                List<Process> propertyBatch = ProcessManager.getProcesses(null, " istTemplate = false AND batchID = " + this.selectedBatches.get(0)
                        .getBatchId(), 0, getBatchMaxSize());
                this.batchHelper = new BatchProcessHelper(propertyBatch, selectedBatches.get(0));
                return "batch_edit";
            } else {
                Helper.setFehlerMeldung("noBatchSelected");
                return "";
            }
        }
    }

}
