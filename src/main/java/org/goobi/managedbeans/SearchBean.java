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
package org.goobi.managedbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.beans.Institution;
import org.goobi.beans.Project;
import org.goobi.production.enums.UserRole;
import org.goobi.production.search.api.ExtendedSearchRow;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.InstitutionManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Named("SearchForm")
@WindowScoped
public class SearchBean implements Serializable {

    private static final long serialVersionUID = -4981330560006133964L;

    private static final String NOT_SELECTED = "notSelected";

    @Inject // NOSONAR needs to be a field injection, as the been constructor does not allow arguments
    private ProcessBean processBean;

    @Getter
    @Setter
    private List<String> projects = new ArrayList<>(); // proj:

    @Getter
    @Setter
    private List<String> processPropertyTitles = new ArrayList<>(); // processeig:

    @Getter
    @Setter
    private List<String> masterpiecePropertyTitles = new ArrayList<>(); // werk:

    @Getter
    @Setter
    private List<String> metadataTitles = new ArrayList<>();

    @Getter
    @Setter
    private List<String> templatePropertyTitles = new ArrayList<>();// vorl:

    @Getter
    @Setter
    private List<String> stepPropertyTitles = new ArrayList<>(); // stepeig:

    @Getter
    @Setter
    private List<String> stepTitles = new ArrayList<>(); // step:

    @Getter
    @Setter
    private List<StepStatus> stepstatus = new ArrayList<>();

    @Getter
    private List<ExtendedSearchRow> rowList = new ArrayList<>();

    @Getter
    private List<SelectItem> fieldnameList = new ArrayList<>();

    @Getter
    private List<String> institutionNames = new ArrayList<>();

    @Getter
    @Setter
    private ExtendedSearchRow currentRow;

    public SearchBean() {
        for (StepStatus s : StepStatus.values()) {
            this.stepstatus.add(s);
        }

        Institution inst = null;
        if (!Helper.getCurrentUser().isSuperAdmin()) {
            inst = Helper.getCurrentUser().getInstitution();
        }
        // projects
        String projectFilter = "";

        if (!Helper.getLoginBean().hasRole(UserRole.Workflow_Processes_Show_Deactivated_Projects.name())) {
            projectFilter = " projectIsArchived = false ";
        }
        this.projects.add(Helper.getTranslation(NOT_SELECTED));

        try {
            List<Project> projektList = ProjectManager.getProjects("titel", projectFilter, 0, Integer.MAX_VALUE, inst);
            for (Project p : projektList) {
                this.projects.add(p.getTitel());
            }
        } catch (DAOException exception) {
            log.error(exception);
        }

        this.masterpiecePropertyTitles.add(Helper.getTranslation(NOT_SELECTED));
        this.masterpiecePropertyTitles.addAll(PropertyManager.getDistinctMasterpiecePropertyTitles());

        this.templatePropertyTitles.add(Helper.getTranslation(NOT_SELECTED));
        this.templatePropertyTitles.addAll(PropertyManager.getDistinctTemplatePropertyTitles());

        this.processPropertyTitles.add(Helper.getTranslation(NOT_SELECTED));
        this.processPropertyTitles.addAll(PropertyManager.getDistinctProcessPropertyTitles());

        this.stepTitles.add(Helper.getTranslation(NOT_SELECTED));
        stepTitles.addAll(StepManager.getDistinctStepTitles());

        institutionNames.add(Helper.getTranslation(NOT_SELECTED));
        institutionNames.addAll(InstitutionManager.getInstitutionNames());

        initializeRowList();

        fieldnameList.add(new SelectItem("", Helper.getTranslation(NOT_SELECTED)));
        fieldnameList.add(new SelectItem("PROCESSID", Helper.getTranslation("id")));
        fieldnameList.add(new SelectItem("PROCESSTITLE", Helper.getTranslation("title")));

        fieldnameList.add(new SelectItem("PROCESSPROPERTY", Helper.getTranslation("processProperties")));

        fieldnameList.add(new SelectItem("STEP", Helper.getTranslation("step")));

        fieldnameList.add(new SelectItem("PROJECT", Helper.getTranslation("projects")));

        if (Helper.getCurrentUser().isSuperAdmin()) {
            fieldnameList.add(new SelectItem("INSTITUTION", Helper.getTranslation("institution")));
        }

        fieldnameList.add(new SelectItem("TEMPLATE", Helper.getTranslation("templateProperties")));

        fieldnameList.add(new SelectItem("WORKPIECE", Helper.getTranslation("masterpieceProperties")));
        fieldnameList.add(new SelectItem("BATCH", Helper.getTranslation("batch")));
        fieldnameList.add(new SelectItem("METADATA", Helper.getTranslation("metadata")));

        fieldnameList.add(new SelectItem("JOURNAL", Helper.getTranslation("journal")));

        fieldnameList.add(new SelectItem("PROCESSDATE", Helper.getTranslation("search_PROCESSDATE")));
        fieldnameList.add(new SelectItem("STEPSTARTDATE", Helper.getTranslation("search_STEPSTARTDATE")));
        fieldnameList.add(new SelectItem("STEPFINISHDATE", Helper.getTranslation("search_STEPFINISHDATE")));

        metadataTitles.add(Helper.getTranslation(NOT_SELECTED));
        metadataTitles.addAll(MetadataManager.getDistinctMetadataNames());

    }

    private void initializeRowList() {
        ExtendedSearchRow row1 = new ExtendedSearchRow();
        row1.setFieldName("PROCESSID");
        rowList.add(row1);

        ExtendedSearchRow row2 = new ExtendedSearchRow();
        row2.setFieldName("PROCESSTITLE");
        rowList.add(row2);

        ExtendedSearchRow row3 = new ExtendedSearchRow();
        row3.setFieldName("PROJECT");
        rowList.add(row3);

        ExtendedSearchRow row4 = new ExtendedSearchRow();
        row4.setFieldName("METADATA");
        rowList.add(row4);

        ExtendedSearchRow row5 = new ExtendedSearchRow();
        row5.setFieldName("STEP");
        rowList.add(row5);
    }

    public List<SelectItem> getOperands() {
        List<SelectItem> answer = new ArrayList<>();
        SelectItem and = new SelectItem("", Helper.getTranslation("AND"));
        SelectItem not = new SelectItem("-", Helper.getTranslation("NOT"));
        answer.add(and);
        answer.add(not);
        return answer;
    }

    public List<SelectItem> getOperandsForID() {
        List<SelectItem> answer = new ArrayList<>();
        SelectItem and = new SelectItem("", Helper.getTranslation("IS"));
        SelectItem not = new SelectItem("-", Helper.getTranslation("IS NOT"));
        answer.add(and);
        answer.add(not);
        return answer;
    }

    public void addRow() {
        rowList.add(new ExtendedSearchRow());
    }

    public void deleteRow() {
        if (rowList.contains(currentRow)) {
            rowList.remove(currentRow);
        }
    }

    public int getSizeOfRowList() {
        return rowList.size();
    }

    public String resetFilter() {
        rowList = new ArrayList<>();
        initializeRowList();
        return "";
    }

    public String createFilter() {
        StringBuilder searchBuilder = new StringBuilder();

        for (ExtendedSearchRow row : rowList) {
            searchBuilder.append(row.createSearchString());
        }

        processBean.setFilter(searchBuilder.toString());
        processBean.setModusAnzeige("aktuell");
        return processBean.FilterAlleStart();

    }

    public List<SelectItem> getOperandsForDates() {
        List<SelectItem> answer = new ArrayList<>();
        answer.add(new SelectItem("=", Helper.getTranslation("IS")));
        answer.add(new SelectItem("!=", Helper.getTranslation("IS NOT")));
        answer.add(new SelectItem(">", Helper.getTranslation("GREATER")));
        answer.add(new SelectItem("<", Helper.getTranslation("SMALLER")));
        return answer;
    }

}
