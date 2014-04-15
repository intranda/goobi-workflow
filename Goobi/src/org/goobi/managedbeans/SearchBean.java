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
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.goobi.beans.Project;

import org.goobi.production.search.api.ExtendedSearchRow;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;

@ManagedBean(name = "SearchForm")
@SessionScoped
public class SearchBean {

    private List<String> projects = new ArrayList<String>(); // proj:

    private List<String> processPropertyTitles = new ArrayList<String>(); // processeig:
 

    private List<String> masterpiecePropertyTitles = new ArrayList<String>(); // werk:


    private List<String> metadataTitles = new ArrayList<>();

    
    private List<String> templatePropertyTitles = new ArrayList<String>();// vorl:


    private List<String> stepPropertyTitles = new ArrayList<String>(); // stepeig:

    private List<String> stepTitles = new ArrayList<String>(); // step:
    private List<StepStatus> stepstatus = new ArrayList<StepStatus>();


    List<ExtendedSearchRow> rowList = new ArrayList<ExtendedSearchRow>();

    List<SelectItem> fieldnameList = new ArrayList<SelectItem>();

    private ExtendedSearchRow currentRow;


    public SearchBean() {
        for (StepStatus s : StepStatus.values()) {
            this.stepstatus.add(s);
        }
        int restriction = ((LoginBean) Helper.getManagedBeanValue("#{LoginForm}")).getMaximaleBerechtigung();
        //		Session session = Helper.getHibernateSession();

        // projects
        String projectFilter = "";
        //		Criteria crit = session.createCriteria(Project.class);
        //		crit.addOrder(Order.asc("titel"));
        if (restriction > 2) {
            projectFilter = " projectIsArchived = false ";
            //			crit.add(Restrictions.not(Restrictions.eq("projectIsArchived", true)));
        }
        this.projects.add(Helper.getTranslation("notSelected"));

        try {
            List<Project> projektList = ProjectManager.getProjects("titel", projectFilter, 0, Integer.MAX_VALUE);
            for (Project p : projektList) {
                this.projects.add(p.getTitel());
            }
        } catch (DAOException e1) {
        }

        //		crit = session.createCriteria(Werkstueckeigenschaft.class);
        //		crit.addOrder(Order.asc("titel"));
        //		crit.setProjection(Projections.distinct(Projections.property("titel")));
        this.masterpiecePropertyTitles.add(Helper.getTranslation("notSelected"));
        this.masterpiecePropertyTitles.addAll(PropertyManager.getDistinctMasterpiecePropertyTitles());
        //		for (Iterator<Object> it = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list().iterator(); it.hasNext();) {
        //			this.masterpiecePropertyTitles.add((String) it.next());
        //		}

        //		crit = session.createCriteria(Vorlageeigenschaft.class);
        //		crit.addOrder(Order.asc("titel"));
        //		crit.setProjection(Projections.distinct(Projections.property("titel")));
        this.templatePropertyTitles.add(Helper.getTranslation("notSelected"));
        this.templatePropertyTitles.addAll(PropertyManager.getDistinctTemplatePropertyTitles());
        //		for (Iterator<Object> it = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list().iterator(); it.hasNext();) {
        //			this.templatePropertyTitles.add((String) it.next());
        //		}

        //		crit = session.createCriteria(Prozesseigenschaft.class);
        //		crit.addOrder(Order.asc("titel"));
        //		crit.setProjection(Projections.distinct(Projections.property("titel")));
        this.processPropertyTitles.add(Helper.getTranslation("notSelected"));
        this.processPropertyTitles.addAll(PropertyManager.getDistinctProcessPropertyTitles());
        //		for (Iterator<Object> it = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list().iterator(); it.hasNext();) {
        //			String itstr = (String) it.next();
        //			if (itstr!=null){
        //				this.processPropertyTitles.add(itstr);
        //			}
        //		}

        //		crit = session.createCriteria(Schritteigenschaft.class);
        //		crit.addOrder(Order.asc("titel"));
        //		crit.setProjection(Projections.distinct(Projections.property("titel")));
        //        		this.stepPropertyTitles.add(Helper.getTranslation("notSelected"));
        //		for (Iterator<Object> it = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list().iterator(); it.hasNext();) {
        //			this.stepPropertyTitles.add((String) it.next());
        //		}
        this.stepTitles.add(Helper.getTranslation("notSelected"));
        stepTitles.addAll(StepManager.getDistinctStepTitles());
        //		crit = session.createCriteria(Step.class);
        //		crit.addOrder(Order.asc("titel"));
        //		crit.setProjection(Projections.distinct(Projections.property("titel")));
        //		this.stepTitles.add(Helper.getTranslation("notSelected"));
        //		for (Iterator<Object> it = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list().iterator(); it.hasNext();) {
        //			this.stepTitles.add((String) it.next());
        //		}

        rowList.add(new ExtendedSearchRow());
        rowList.add(new ExtendedSearchRow());
        rowList.add(new ExtendedSearchRow());
        rowList.add(new ExtendedSearchRow());
        rowList.add(new ExtendedSearchRow());
        fieldnameList.add(new SelectItem("", Helper.getTranslation("notSelected")));
        fieldnameList.add(new SelectItem("PROCESSID", Helper.getTranslation("id")));
        fieldnameList.add(new SelectItem("PROCESSTITLE", Helper.getTranslation("title")));

        fieldnameList.add(new SelectItem("PROCESSPROPERTY", Helper.getTranslation("processProperties")));
        
        fieldnameList.add(new SelectItem("STEP", Helper.getTranslation("step")));

        fieldnameList.add(new SelectItem("PROJECT", Helper.getTranslation("projects")));
        fieldnameList.add(new SelectItem("TEMPLATE", Helper.getTranslation("templateProperties")));

        fieldnameList.add(new SelectItem("WORKPIECE", Helper.getTranslation("masterpieceProperties")));
        fieldnameList.add(new SelectItem("BATCH", Helper.getTranslation("batch")));
        fieldnameList.add(new SelectItem("METADATA", Helper.getTranslation("metadata")));

        
       
        metadataTitles.add(Helper.getTranslation("notSelected"));
        metadataTitles.addAll(MetadataManager.getDistinctMetadataNames());
        
    }

    public List<String> getProjects() {
        return this.projects;
    }

    public void setProjects(List<String> projects) {
        this.projects = projects;
    }

    public List<String> getMasterpiecePropertyTitles() {
        return this.masterpiecePropertyTitles;
    }

    public void setMasterpiecePropertyTitles(List<String> masterpiecePropertyTitles) {
        this.masterpiecePropertyTitles = masterpiecePropertyTitles;
    }

    public List<String> getMetadataTitles() {
        return metadataTitles;
    }
    
    public void setMetadataTitles(List<String> metadataTitles) {
        this.metadataTitles = metadataTitles;
    }
    
    public List<String> getTemplatePropertyTitles() {
        return this.templatePropertyTitles;
    }

    public void setTemplatePropertyTitles(List<String> templatePropertyTitles) {
        this.templatePropertyTitles = templatePropertyTitles;
    }

    public List<String> getProcessPropertyTitles() {
        return this.processPropertyTitles;
    }

    public void setProcessPropertyTitles(List<String> processPropertyTitles) {
        this.processPropertyTitles = processPropertyTitles;
    }

    public List<String> getStepPropertyTitles() {
        return this.stepPropertyTitles;
    }

    public void setStepPropertyTitles(List<String> stepPropertyTitles) {
        this.stepPropertyTitles = stepPropertyTitles;
    }

    public List<String> getStepTitles() {
        return this.stepTitles;
    }

    public void setStepTitles(List<String> stepTitles) {
        this.stepTitles = stepTitles;
    }

    public List<StepStatus> getStepstatus() {
        return this.stepstatus;
    }

    public void setStepstatus(List<StepStatus> stepstatus) {
        this.stepstatus = stepstatus;
    }


   

    public List<SelectItem> getOperands() {
        List<SelectItem> answer = new ArrayList<SelectItem>();
        SelectItem and = new SelectItem("", Helper.getTranslation("AND"));
        SelectItem not = new SelectItem("-", Helper.getTranslation("NOT"));
        answer.add(and);
        answer.add(not);
        return answer;
    }



    public List<SelectItem> getFieldnameList() {
        return fieldnameList;
    }

    public List<ExtendedSearchRow> getRowList() {
        return rowList;
    }

    public void addRow() {
        rowList.add(new ExtendedSearchRow());
    }

    public void deleteRow() {
        if (rowList.contains(currentRow)) {
            rowList.remove(currentRow);
        }
    }

    public ExtendedSearchRow getCurrentRow() {
        return currentRow;
    }

    public void setCurrentRow(ExtendedSearchRow currentRow) {
        this.currentRow = currentRow;
    }

    public int getSizeOfRowList() {
        return rowList.size();
    }

    
    public String resetFilter() {
        rowList = new ArrayList<>();
        rowList.add(new ExtendedSearchRow());
        rowList.add(new ExtendedSearchRow());
        rowList.add(new ExtendedSearchRow());
        rowList.add(new ExtendedSearchRow());
        rowList.add(new ExtendedSearchRow());
        
        return "";
    }
    
    public String createFilter() {
        String search = "";

        for (ExtendedSearchRow row : rowList) {
            search += row.createSearchString();
        }
     
        ProcessBean form = (ProcessBean) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("ProzessverwaltungForm");
        if (form != null) {
            form.filter = search;
            form.setModusAnzeige("aktuell");
            return form.FilterAlleStart();
        }
        return "";
    }

}
