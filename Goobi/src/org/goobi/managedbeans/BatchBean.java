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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.goobi.production.cli.helper.WikiFieldHelper;
import org.goobi.production.export.ExportDocket;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.goobi.beans.Process;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Batch;
import de.sub.goobi.helper.BatchProcessHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;

//import de.sub.goobi.persistence.ProzessDAO;

@ManagedBean(name = "BatchForm")
@SessionScoped
public class BatchBean extends BasicBean {

    private static final long serialVersionUID = 8234897225425856549L;

    private static final Logger logger = Logger.getLogger(BatchBean.class);

    private List<Process> currentProcesses = new ArrayList<Process>();
    private List<Process> selectedProcesses = new ArrayList<Process>();
    private List<Batch> currentBatches = new ArrayList<Batch>();
    private List<String> selectedBatches = new ArrayList<String>();
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

    public List<Process> getCurrentProcesses() {
        return this.currentProcesses;
    }

    public void setCurrentProcesses(List<Process> currentProcesses) {
        this.currentProcesses = currentProcesses;
    }

    public void loadBatchData() {
        this.currentBatches = new ArrayList<Batch>();
        this.selectedBatches = new ArrayList<String>();
        for (Process p : this.selectedProcesses) {
            if (p.getBatchID() != null && !this.currentBatches.contains(p.getBatchID())) {
                this.currentBatches.add(generateBatch(p.getBatchID()));
            }
        }
    }

    private Batch generateBatch(Integer id) {
        //		Session session = Helper.getHibernateSession();
        String filter = "";
        if (id != null) {
            filter = " batchID = " + id + " AND istTemplate = false ";
        } else {
            filter = " batchID is NULL AND istTemplate = false ";
        }

        String msg1 = Helper.getTranslation("batch");
        String msg2 = Helper.getTranslation("prozesse");
        if (id != null) {
            String text = msg1 + " " + id + " (" + ProcessManager.countProcesses(filter) + " " + msg2 + ")";
            return new Batch(id, text);
        } else {
            String text = Helper.getTranslation("withoutBatch") + " (" + ProcessManager.countProcesses(filter) + " " + msg2 + ")";
            return new Batch(null, text);
        }
    }

    public void loadProcessData() {

        String filter = " istTemplate = false ";

        List<Integer> ids = new ArrayList<Integer>();
        for (String s : this.selectedBatches) {
            if (s != null && !s.equals("") && !s.equals("null")) {
                ids.add(new Integer(s));
            }
        }
        if (this.selectedBatches.size() > 0) {
            if (this.selectedBatches.contains(null) || this.selectedBatches.contains("null")) {
                filter += " AND batchID is null ";
                //				crit.add(Restrictions.isNull("batchID"));
            } else {
                filter += " AND (";
                for (Integer id : ids) {
                    filter += " batchID = " + id + " OR";
                }
                filter = filter.substring(0, filter.length() - 3) + ")";
                //				crit.add(Restrictions.in("batchID", ids));
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
        Integer number = null;
        try {
            number = new Integer(this.batchfilter);
        } catch (Exception e) {
            logger.trace("NAN Exception: " + this.batchfilter);
        }
        if (number != null) {
            List<Integer> allBatches = ProcessManager.getBatchIds(getBatchMaxSize());
            this.currentBatches = new ArrayList<Batch>();
            for (Integer in : allBatches) {
                if (in != null && Integer.toString(in).contains(this.batchfilter)) {
                    this.currentBatches.add(generateBatch(in));
                }
            }
        } else {
            List<Integer> ids = ProcessManager.getBatchIds(getBatchMaxSize());
            this.currentBatches = new ArrayList<Batch>();
            for (Integer in : ids) {
                this.currentBatches.add(generateBatch(in));
            }
        }
    }

    public List<SelectItem> getCurrentProcessesAsSelectItems() {
        List<SelectItem> answer = new ArrayList<SelectItem>();
        for (Process p : this.currentProcesses) {
            answer.add(new SelectItem(String.valueOf(p.getId()), p.getTitel()));
        }
        return answer;
    }

    public String getBatchfilter() {
        return this.batchfilter;
    }

    public void setBatchfilter(String batchfilter) {
        this.batchfilter = batchfilter;
    }

    public String getProcessfilter() {
        return this.processfilter;
    }

    public void setProcessfilter(String processfilter) {
        this.processfilter = processfilter;
    }

    public List<Batch> getCurrentBatches() {
        return this.currentBatches;
    }

    public void setCurrentBatches(List<Batch> currentBatches) {
        this.currentBatches = currentBatches;
    }

    public List<Process> getSelectedProcesses() {
        return this.selectedProcesses;
    }

    public void setSelectedProcesses(List<Process> selectedProcesses) {
        this.selectedProcesses = selectedProcesses;
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
    
    
    public List<String> getSelectedBatches() {
        return this.selectedBatches;
    }

    
    public void setSelectedBatches(List<String> selectedBatches) {
        this.selectedBatches = selectedBatches;
    }

    public String FilterAlleStart() {
        filterBatches();
        filterProcesses();
        return "batch_all";
    }

    public String downloadDocket() {
        logger.debug("generate docket for process list");
        String rootpath = ConfigurationHelper.getInstance().getXsltFolder();
        File xsltfile = new File(rootpath, "docket_multipage.xsl");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        List<Process> docket = new ArrayList<Process>();
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung("noBatchSelected");
        } else if (this.selectedBatches.size() == 1) {

            //            Session session = Helper.getHibernateSession();
            //            Criteria crit = session.createCriteria(Process.class);
            docket =
                    ProcessManager.getProcesses(null, " istTemplate = false AND batchID = " + new Integer(this.selectedBatches.get(0)), 0,
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
                    ern.startExport(docket, out, xsltfile.getAbsolutePath());
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
              
                List<Process> deleteList =
                        ProcessManager.getProcesses(null, " istTemplate = false AND batchID = " + new Integer(this.selectedBatches.get(0)), 0,
                                getBatchMaxSize());
                {
                    for (Process p : deleteList) {
                        p.setBatchID(null);
                        ProcessManager.saveProcessInformation(p);
                        
                    }
                    
                }
            } else {
                Helper.setFehlerMeldung("noBatchSelected");
            }
        } else {
            Helper.setFehlerMeldung("toḾanyBatchesSelected");
        }
        FilterAlleStart();

    }

    public void addProcessesToBatch() {
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung("noBatchSelected");
        } else if (this.selectedBatches.size() > 1) {
            Helper.setFehlerMeldung("toḾanyBatchesSelected");
        } else {
            try {
                //				Session session = Helper.getHibernateSession();
                Integer batchid = new Integer(this.selectedBatches.get(0));
                for (Process p : this.selectedProcesses) {
                    p.setBatchID(batchid);
                    p.setWikifield(WikiFieldHelper.getWikiMessage(p, p.getWikifield(), "debug", "added process to batch " + batchid));
                 
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
            p.setWikifield(WikiFieldHelper.getWikiMessage(p, p.getWikifield(), "debug", "removed process from batch " + p.getBatchID()));
            p.setBatchID(null);

          
            ProcessManager.saveProcessInformation(p);
        }
      
        FilterAlleStart();
    }

    public void createNewBatch() {
        if (this.selectedProcesses.size() > 0) {
            //            Session session = Helper.getHibernateSession();
            Integer newBatchId = 1;
            try {
                newBatchId += ProcessManager.getMaxBatchNumber();
            } catch (Exception e1) {
            }

            for (Process p : this.selectedProcesses) {
                p.setBatchID(newBatchId);
                p.setWikifield(WikiFieldHelper.getWikiMessage(p, p.getWikifield(), "debug", "added process to batch " + newBatchId));
                ProcessManager.saveProcessInformation(p);
		}
            
  
        }
        FilterAlleStart();
    }

    public String editProperties() {
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung("noBatchSelected");
            return "";
        } else if (this.selectedBatches.size() > 1) {
            Helper.setFehlerMeldung("toḾanyBatchesSelected");
            return "";
        } else {
            if (this.selectedBatches.get(0) != null && !this.selectedBatches.get(0).equals("") && !this.selectedBatches.get(0).equals("null")) {
                //                Session session = Helper.getHibernateSession();
                //                Criteria crit = session.createCriteria(Process.class);
                //                crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
                //                //				List<Integer> ids = new ArrayList<Integer>();
                //                crit.add(Restrictions.eq("batchID", new Integer(this.selectedBatches.get(0))));
                //                List<Process> propertyBatch = crit.list();

                List<Process> propertyBatch =
                        ProcessManager.getProcesses(null, " istTemplate = false AND batchID = " + new Integer(this.selectedBatches.get(0)), 0,
                                getBatchMaxSize());
                this.batchHelper = new BatchProcessHelper(propertyBatch);
                return "batch_edit";
            } else {
                Helper.setFehlerMeldung("noBatchSelected");
                return "";
            }
        }
    }

    public BatchProcessHelper getBatchHelper() {
        return this.batchHelper;
    }

    public void setBatchHelper(BatchProcessHelper batchHelper) {
        this.batchHelper = batchHelper;
    }

    public String getModusBearbeiten() {
        return this.modusBearbeiten;
    }

    public void setModusBearbeiten(String modusBearbeiten) {
        this.modusBearbeiten = modusBearbeiten;
    }
}
