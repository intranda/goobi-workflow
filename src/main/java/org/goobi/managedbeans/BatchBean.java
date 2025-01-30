package org.goobi.managedbeans;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.beans.Batch;
import org.goobi.beans.Institution;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Process;
import org.goobi.beans.User;
import org.goobi.production.enums.LogType;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.BatchProcessHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import io.goobi.workflow.xslt.XsltToPdf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

@Named("BatchForm")
@WindowScoped
@Data
@EqualsAndHashCode(callSuper = false)
@Log4j2
public class BatchBean extends BasicBean implements Serializable {

    private static final long serialVersionUID = 8234897225425856549L;

    private static final String NO_BATCH_SELECTED = "noBatchSelected";
    private static final String TOO_MANY_BATCHES_SELECTED = "tooManyBatchesSelected";

    private static final String DEFAULT_BATCH_NAME = "-batch-";

    private List<Process> currentProcesses = new ArrayList<>();
    private List<Process> selectedProcesses = new ArrayList<>();
    private List<Batch> currentBatches = new ArrayList<>();
    private List<Batch> selectedBatches = new ArrayList<>();
    private String batchfilter;
    private String processfilter;

    private String modusBearbeiten = "";

    private BatchProcessHelper batchHelper;

    private int getBatchMaxSize() {
        return ConfigurationHelper.getInstance().getBatchMaxSize();
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

        StringBuilder filterBuilder = new StringBuilder(" istTemplate = false ");

        List<Integer> ids = new ArrayList<>();
        for (Batch b : this.selectedBatches) {
            if (b != null) {
                ids.add(b.getBatchId());
            }
        }

        if (!this.selectedBatches.isEmpty()) {

            if (ids.contains(null)) {
                filterBuilder.append(" AND batchID is null ");
            } else {
                filterBuilder.append(" AND (");
                for (Integer id : ids) {
                    filterBuilder.append(" batchID = ").append(id).append(" OR");
                }
                // delete the last " OR"
                filterBuilder.delete(filterBuilder.length() - 3, filterBuilder.length());
                filterBuilder.append(")");
            }
        }

        Institution inst = null;
        User user = Helper.getCurrentUser();
        if (user != null && !user.isSuperAdmin()) {
            //             limit result to institution of current user
            inst = user.getInstitution();
        }

        this.currentProcesses = ProcessManager.getProcesses(null, filterBuilder.toString(), 0, getBatchMaxSize(), inst);
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
        Institution inst = null;
        User user = Helper.getCurrentUser();
        if (user != null && !user.isSuperAdmin()) {
            //             limit result to institution of current user
            inst = user.getInstitution();
        }
        this.currentProcesses = ProcessManager.getProcesses("prozesse.titel", filter, 0, getBatchMaxSize(), inst);
    }

    public void filterBatches() {

        if (StringUtils.isNotBlank(batchfilter)) {
            List<Batch> allBatches = ProcessManager.getBatches(getBatchMaxSize());
            this.currentBatches = new ArrayList<>();
            for (Batch in : allBatches) {
                if ((in.getBatchName() != null && in.getBatchName().toLowerCase().contains(this.batchfilter.toLowerCase()))
                        || Integer.toString(in.getBatchId()).contains(this.batchfilter)) {
                    this.currentBatches.add(generateBatch(in));
                }
            }
        } else {
            currentBatches = ProcessManager.getBatches(getBatchMaxSize());
            for (Batch in : currentBatches) {
                generateBatch(in);
            }
        }
    }

    public List<SelectItem> getCurrentBatchesAsSelectItems() {
        List<SelectItem> answer = new ArrayList<>();
        for (Batch p : this.currentBatches) {
            answer.add(new SelectItem(String.valueOf(p.getBatchId()), p.getBatchLabel()));
        }
        return answer;
    }

    public void setSelectedBatchIds(List<String> processIds) {
        selectedBatches = new ArrayList<>();
        for (String idString : processIds) {
            Integer id = Integer.valueOf(idString);

            selectedBatches.add(ProcessManager.getBatchById(id));
        }
    }

    public List<String> getSelectedBatchIds() {
        List<String> idList = new ArrayList<>();
        for (Batch p : selectedBatches) {
            idList.add(String.valueOf(p.getBatchId()));
        }
        return idList;
    }

    public List<SelectItem> getCurrentProcessesAsSelectItems() {
        List<SelectItem> answer = new ArrayList<>();
        for (Process p : this.currentProcesses) {
            answer.add(new SelectItem(String.valueOf(p.getId()), p.getTitel()));
        }
        return answer;
    }

    public void setSelectedProcessIds(List<String> processIds) {
        selectedProcesses = new ArrayList<>();
        for (String idString : processIds) {
            Integer id = Integer.valueOf(idString);

            selectedProcesses.add(ProcessManager.getProcessById(id));
        }
    }

    public List<String> getSelectedProcessIds() {
        List<String> idList = new ArrayList<>();
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

        Institution inst = null;
        User user = Helper.getCurrentUser();
        if (user != null && !user.isSuperAdmin()) {
            // limit result to institution of current user
            inst = user.getInstitution();
        }

        if (log.isDebugEnabled()) {
            log.debug("generate docket for process list");
        }
        String rootpath = ConfigurationHelper.getInstance().getXsltFolder();
        Path xsltfile = Paths.get(rootpath, "docket_multipage.xsl");
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        List<Process> docket = new ArrayList<>();
        if (this.selectedBatches.isEmpty()) {
            Helper.setFehlerMeldung(NO_BATCH_SELECTED);
        } else if (this.selectedBatches.size() == 1) {
            docket = ProcessManager.getProcesses(null, " istTemplate = false AND batchID = " + this.selectedBatches.get(0).getBatchId(), 0,
                    getBatchMaxSize(), inst);

        } else {
            Helper.setFehlerMeldung(TOO_MANY_BATCHES_SELECTED);
        }
        if (!docket.isEmpty() && !facesContext.getResponseComplete()) {
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            String fileName = "batch_" + this.selectedBatches.get(0).getBatchId() + ".pdf";
            ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
            String contentType = servletContext.getMimeType(fileName);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");

            try {
                ServletOutputStream out = response.getOutputStream();
                XsltToPdf ern = new XsltToPdf();
                ern.startExport(docket, out, xsltfile.toString());
                out.flush();
            } catch (IOException e) {
                log.error("IOException while exporting run note", e);
            }

            facesContext.responseComplete();
        }
        return "";
    }

    public void deleteBatch() {
        if (this.selectedBatches.isEmpty()) {
            Helper.setFehlerMeldung(NO_BATCH_SELECTED);
        } else if (this.selectedBatches.size() == 1) {
            if (this.selectedBatches.get(0) != null) {
                ProcessManager.deleteBatch(selectedBatches.get(0));
            } else {
                Helper.setFehlerMeldung(NO_BATCH_SELECTED);
            }
        } else {
            Helper.setFehlerMeldung(TOO_MANY_BATCHES_SELECTED);
        }
        FilterAlleStart();

    }

    public void addProcessesToBatch() {
        if (this.selectedBatches.isEmpty()) {
            Helper.setFehlerMeldung(NO_BATCH_SELECTED);
        } else if (this.selectedBatches.size() > 1) {
            Helper.setFehlerMeldung(TOO_MANY_BATCHES_SELECTED);
        } else {
            try {
                Batch batch = this.selectedBatches.get(0);
                for (Process p : this.selectedProcesses) {
                    p.setBatch(batch);
                    JournalEntry logEntry = new JournalEntry(p.getId(), new Date(), DEFAULT_BATCH_NAME, LogType.DEBUG,
                            "added process to batch " + batch.getBatchId(), EntryType.PROCESS);
                    JournalManager.saveJournalEntry(logEntry);

                    ProcessManager.saveProcessInformation(p);
                }

            } catch (Exception e) {
                Helper.setFehlerMeldung(NO_BATCH_SELECTED);
            }
        }
        FilterAlleStart();
    }

    public void removeProcessesFromBatch() {
        for (Process p : this.selectedProcesses) {
            if (p.getBatch() != null) {

                JournalEntry logEntry = new JournalEntry(p.getId(), new Date(), DEFAULT_BATCH_NAME, LogType.DEBUG,
                        "removed process from batch " + p.getBatch().getBatchId(), EntryType.PROCESS);
                JournalManager.saveJournalEntry(logEntry);

                p.setBatch(null);
                ProcessManager.saveProcessInformation(p);
            }
        }

        FilterAlleStart();
    }

    public void createNewBatch() {
        if (!this.selectedProcesses.isEmpty()) {

            Batch batch = new Batch();
            for (Process p : this.selectedProcesses) {
                p.setBatch(batch);
                ProcessManager.saveProcessInformation(p);

                JournalEntry logEntry = new JournalEntry(p.getId(), new Date(), DEFAULT_BATCH_NAME, LogType.DEBUG,
                        "added process to batch " + batch.getBatchId(), EntryType.PROCESS);
                JournalManager.saveJournalEntry(logEntry);
            }

        }
        FilterAlleStart();
    }

    public String editProperties() {
        Institution inst = null;
        User user = Helper.getCurrentUser();
        if (user != null && !user.isSuperAdmin()) {
            // limit result to institution of current user
            inst = user.getInstitution();
        }
        if (selectedBatches.isEmpty()) {
            Helper.setFehlerMeldung(NO_BATCH_SELECTED);
        } else if (this.selectedBatches.size() > 1) {
            Helper.setFehlerMeldung(TOO_MANY_BATCHES_SELECTED);
        } else if (this.selectedBatches.get(0) != null) {
            String sql = " istTemplate = false AND batchID = " + this.selectedBatches.get(0).getBatchId();
            List<Process> propertyBatch = ProcessManager.getProcesses(null, sql, 0, getBatchMaxSize(), inst);
            this.batchHelper = new BatchProcessHelper(propertyBatch, selectedBatches.get(0));
            return "batch_edit";
        } else {
            Helper.setFehlerMeldung(NO_BATCH_SELECTED);
        }
        return "";
    }

}
