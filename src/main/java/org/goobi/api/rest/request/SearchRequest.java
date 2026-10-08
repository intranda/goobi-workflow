/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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
package org.goobi.api.rest.request;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.goobi.api.db.RestDbHelper;
import org.goobi.api.rest.model.RestProcess;
import org.goobi.api.rest.utils.MetadataUtils;

import de.sub.goobi.helper.enums.StepStatus;
import lombok.Data;

@Data
public class SearchRequest {
    private List<SearchGroup> metadataFilters = new ArrayList<>();
    private boolean metadataConjunctive;
    private List<String> filterProjects;
    // ids of filterProjects, resolved before the search so that MariaDB can use the index on prozesse.ProjekteID
    private List<Integer> filterProjectIds;
    private List<Integer> filterTemplateIDs;
    private String filterStep;
    private String structureType;

    private Set<String> wantedFields;

    private String sortField;
    private boolean sortDescending;

    private String propName;
    private String propValue;

    private String stepName;
    private String stepStatus;

    private int limit;
    private int offset;

    private boolean cachedMetadata;

    public void newGroup() {
        SearchGroup group = new SearchGroup();
        group.newFilter();
        this.metadataFilters.add(group);
    }

    public void addSearchGroup(SearchGroup group) {
        this.metadataFilters.add(group);
    }

    public void deleteSearchGroup(int index) {
        this.metadataFilters.remove(index);
    }

    public int getNumGroups() {
        return this.metadataFilters.size();
    }

    public void setProperty(String propName, String propValue) {

        this.propName = propName;
        this.propValue = propValue;
    }

    public void setStepStatus(String stepName, String stepStatus) {

        this.stepName = stepName;
        this.stepStatus = stepStatus;
    }

    public List<RestProcess> search() throws SQLException {
        List<RestProcess> processes = RestDbHelper.searchProcesses(this);
        MetadataUtils.addMetadataToRestProcesses(processes, this);
        return processes;
    }

    private boolean addFilters(StringBuilder b, boolean inFirstWhere) {
        boolean firstWhere = inFirstWhere;
        if (this.filterProjectIds != null && !this.filterProjectIds.isEmpty()) {
            // constant ids let MariaDB use the index on prozesse.ProjekteID
            firstWhere = false;
            b.append("prozesse.ProjekteID IN (").append(placeholders(this.filterProjectIds.size())).append(") ");
        } else if (this.filterProjects != null && !this.filterProjects.isEmpty()) {
            firstWhere = false;
            b.append("prozesse.ProjekteID IN (SELECT ProjekteID FROM projekte WHERE Titel IN (")
                    .append(placeholders(this.filterProjects.size()))
                    .append(")) ");
        }
        // properties and steps are checked with EXISTS, a join would return a process once per matching row
        if (this.filterTemplateIDs != null && !this.filterTemplateIDs.isEmpty()) {
            if (!firstWhere) {
                b.append("AND ");
            }
            firstWhere = false;
            b.append("EXISTS (SELECT 1 FROM properties tp WHERE tp.object_type = 'process' AND tp.property_name = 'templateID' ");
            b.append("AND tp.object_id = prozesse.ProzesseID AND tp.property_value IN (");
            b.append(placeholders(this.filterTemplateIDs.size()));
            b.append(")) ");
        }
        if (hasPropertyFilter()) {
            if (!firstWhere) {
                b.append("AND ");
            }
            firstWhere = false;
            b.append("EXISTS (SELECT 1 FROM properties pp WHERE pp.object_type = 'process' AND pp.property_name = ? ");
            b.append("AND pp.object_id = prozesse.ProzesseID AND pp.property_value = ?) ");
        }
        if (hasStepFilter()) {
            if (!firstWhere) {
                b.append("AND ");
            }
            firstWhere = false;
            b.append("EXISTS (SELECT 1 FROM schritte s WHERE s.ProzesseID = prozesse.ProzesseID AND s.Titel = ? AND s.Bearbeitungsstatus = ?) ");
        }

        return firstWhere;
    }

    private static String placeholders(int count) {
        return String.join(", ", Collections.nCopies(count, "?"));
    }

    private boolean hasPropertyFilter() {
        return this.propName != null && this.propValue != null;
    }

    private boolean hasStepFilter() {
        return this.stepName != null && this.stepStatus != null;
    }

    public String createSql() {

        StringBuilder builder = new StringBuilder();
        createSelect(builder);
        createFrom(builder);
        createWhere(builder);
        return builder.toString();

    }

    private void createSelect(StringBuilder b) {
        b.append("SELECT prozesse.ProzesseID, metadatenkonfigurationen.Datei ");
    }

    private void createFrom(StringBuilder b) {
        b.append("FROM prozesse JOIN metadatenkonfigurationen ");
        b.append("ON metadatenkonfigurationen.MetadatenKonfigurationID = prozesse.MetadatenKonfigurationID ");
    }

    private void createWhere(StringBuilder b) {

        String conj = metadataConjunctive ? "AND " : "OR ";

        StringBuilder where = new StringBuilder();
        boolean firstWhere = addFilters(where, true);

        // groups are combined with OR/AND, keep them together so they do not bind to the other filters
        StringBuilder metadataClause = new StringBuilder();
        for (SearchGroup sg : metadataFilters) {
            StringBuilder groupClause = new StringBuilder();
            sg.createSqlClause(groupClause);
            if (StringUtils.isBlank(groupClause)) {
                continue;
            }
            if (metadataClause.length() > 0) {
                metadataClause.append(conj);
            }
            metadataClause.append("(").append(groupClause).append(") ");
        }
        if (metadataClause.length() > 0) {
            if (!firstWhere) {
                where.append("AND ");
            }
            where.append("(").append(metadataClause).append(") ");
        }
        if (where.length() > 0) {
            b.append("WHERE ");
            b.append(where);
        }

        if (StringUtils.isNotBlank(sortField)) {
            // the subquery only reads the sort value of the matching processes, the process id keeps the order stable for paging
            String direction = sortDescending ? "DESC" : "ASC";
            b.append(" ORDER BY (SELECT MAX(value) FROM metadata WHERE metadata.name = ? AND metadata.processid = prozesse.ProzesseID) ");
            b.append(direction).append(", prozesse.ProzesseID ").append(direction).append(" ");
        } else {
            b.append(" ORDER BY prozesse.ProzesseID ASC ");
        }
        if (limit != 0) {
            b.append("LIMIT ? OFFSET ? ");
        }
    }

    public Object[] createSqlParams() {
        List<Object> params = new ArrayList<>();
        addLegacyWhereParams(params);
        Object[] paramsArr = new Object[params.size()];
        params.toArray(paramsArr);
        return paramsArr;
    }

    private String resolveStepStatusValue(String input) {
        if (input == null) {
            return null;
        }
        StepStatus status = StepStatus.getStatusFromName(input);
        if (status != null) {
            return String.valueOf(status.getValue());
        }
        return input;
    }

    private void addLegacyWhereParams(List<Object> params) {
        if (this.filterProjectIds != null && !this.filterProjectIds.isEmpty()) {
            params.addAll(this.filterProjectIds);
        } else if (this.filterProjects != null) {
            params.addAll(this.filterProjects);
        }

        if (this.filterTemplateIDs != null) {
            for (Integer templateId : this.filterTemplateIDs) {
                params.add(String.valueOf(templateId));
            }
        }

        if (hasPropertyFilter()) {
            params.add(this.propName);
            params.add(this.propValue);
        }

        if (hasStepFilter()) {
            params.add(this.stepName);
            params.add(resolveStepStatusValue(this.stepStatus));
        }

        if (metadataFilters != null) {
            for (SearchGroup sg : metadataFilters) {
                sg.addParams(params);
            }
        }
        if (StringUtils.isNotBlank(sortField)) {
            params.add(sortField);
        }
        if (limit != 0) {
            params.add(limit);
            params.add(offset);
        }
    }

}
