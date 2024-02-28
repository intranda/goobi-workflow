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
import java.util.List;
import java.util.Set;

import org.goobi.api.db.RestDbHelper;
import org.goobi.api.rest.model.RestProcess;
import org.goobi.api.rest.utils.MetadataUtils;

import lombok.Data;

@Data
public class SearchRequest {
    private List<SearchGroup> metadataFilters = new ArrayList<>();
    private boolean metadataConjunctive;
    private List<String> filterProjects;
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

    public String createSql() {
        //example sql: select * from metadata left join prozesse on metadata.processid = prozesse.ProzesseID where prozesse.ProzesseID IN
        //(select processid from metadata where metadata.name="_dateDigitization" and metadata.value="2018"); (NOSONAR)
        StringBuilder b = new StringBuilder();
        createSelect(b);
        createFrom(b);
        createWhere(b);
        createOrderAndLimit(b);
        return b.toString();
    }

    private void createSelect(StringBuilder b) {
        b.append("SELECT prozesse.ProzesseID,  metadatenkonfigurationen.Datei ");
        if (this.filterProjects != null && !this.filterProjects.isEmpty()) {
            b.append(", projekte.Titel ");
        }
        if (this.filterTemplateIDs != null && !this.filterTemplateIDs.isEmpty()) {
            b.append(", prozesseeigenschaften.Titel, prozesseeigenschaften.WERT ");
        }
    }

    private void createFrom(StringBuilder b) {
        b.append(
                "FROM metadata_json LEFT JOIN prozesse ON metadata_json.processid = prozesse.ProzesseID LEFT JOIN metadatenkonfigurationen on metadatenkonfigurationen.MetadatenKonfigurationID=prozesse.MetadatenKonfigurationID ");
        if (this.filterProjects != null && !this.filterProjects.isEmpty()) {
            b.append("LEFT JOIN projekte ON prozesse.ProjekteID = projekte.ProjekteID ");
        }
        if (this.filterTemplateIDs != null && !this.filterTemplateIDs.isEmpty()) {
            b.append("LEFT JOIN prozesseeigenschaften ON prozesse.prozesseID = prozesseeigenschaften.prozesseID ");
        }
        if (this.stepName != null && !this.stepName.isEmpty()) {
            b.append("LEFT JOIN schritte ON prozesse.prozesseID = schritte.ProzesseID ");
        }
    }

    private void createWhere(StringBuilder b) {
        boolean firstWhere = true;
        String conj = metadataConjunctive ? "AND " : "OR ";
        b.append("WHERE ");

        firstWhere = addFilters(b, firstWhere);

        if (metadataFilters != null && !metadataFilters.isEmpty()) {
            if (!firstWhere) {
                b.append("AND ");
            }
            for (int i = 0; i < metadataFilters.size(); i++) {
                SearchGroup sg = metadataFilters.get(i);
                sg.createSqlClause(b);
                if (i + 1 < metadataFilters.size()) {
                    b.append(conj);
                }
            }
        }
    }

    private boolean addFilters(StringBuilder b, boolean firstWhere) {
        if (this.filterProjects != null && !this.filterProjects.isEmpty()) {
            firstWhere = false;
            b.append("projekte.Titel IN (");
            for (int i = 0; i < this.filterProjects.size(); i++) {
                b.append("?");
                if (i + 1 < this.filterProjects.size()) {
                    b.append(", ");
                }
            }
            b.append(") ");
        }
        if (this.filterTemplateIDs != null && !this.filterTemplateIDs.isEmpty()) {
            if (!firstWhere) {
                b.append("AND ");
            }
            firstWhere = false;
            b.append("prozesseeigenschaften.Titel=\"templateID\" AND prozesseeigenschaften.WERT IN (");
            for (int i = 0; i < this.filterTemplateIDs.size(); i++) {
                b.append("?");
                if (i + 1 < this.filterTemplateIDs.size()) {
                    b.append(", ");
                }
            }
            b.append(") ");
        }
        if (this.propName != null && this.propValue != null) {

            if (!firstWhere) {
                b.append("AND ");
            }
            firstWhere = false;
            b.append("prozesseeigenschaften.Titel IN (?) AND prozesseeigenschaften.WERT IN (?) ");
        }
        if (this.stepName != null && this.stepStatus != null) {

            if (!firstWhere) {
                b.append("AND ");
            }
            firstWhere = false;
            b.append("schritte.Titel IN (?) AND schritte.Bearbeitungsstatus IN (?) ");
        }

        return firstWhere;
    }

    private void createOrderAndLimit(StringBuilder b) {
        if (sortField != null && !sortField.isEmpty()) {
            b.append(" ORDER BY JSON_EXTRACT(value, ?) ");
            b.append(sortDescending ? "DESC " : "ASC ");
        } else {
            b.append(" ORDER BY metadata_json.processid ASC ");
        }
        if (limit != 0) {
            b.append("LIMIT ? OFFSET ?");
        }
    }

    public Object[] createSqlParams() {
        List<Object> params = new ArrayList<>();
        addWhereParams(params);
        addOrderAndLimitParams(params);
        Object[] paramsArr = new Object[params.size()];
        params.toArray(paramsArr);
        return paramsArr;
    }

    private void addWhereParams(List<Object> params) {
        if (this.filterProjects != null) {
            for (String project : this.filterProjects) {
                params.add(project);
            }
        }
        if (this.filterTemplateIDs != null) {
            for (Integer templateId : this.filterTemplateIDs) {
                params.add(templateId.toString());
            }
        }
        if (this.propName != null) {
            params.add(this.propName);
            params.add(this.propValue);
        }

        if (this.stepName != null) {
            params.add(this.stepName);
            params.add(this.stepStatus);
        }
        for (SearchGroup sg : metadataFilters) {
            sg.addParams(params);
        }
    }

    private void addOrderAndLimitParams(List<Object> params) {
        if (sortField != null && !sortField.isEmpty()) {
            params.add("$." + sortField);
        }
        if (limit != 0) {
            params.add(limit);
            params.add(offset);
        }
    }

    public String createLegacySql() {
        //example sql: select * from metadata left join prozesse on metadata.processid = prozesse.ProzesseID where prozesse.ProzesseID IN
        //(select processid from metadata where metadata.name="_dateDigitization" and metadata.value="2018");  (NOSONAR)
        StringBuilder builder = new StringBuilder();
        createLegacySelect(builder);
        createLegacyFrom(builder);
        createLegacyWhere(builder);
        return builder.toString();

        //        return "SELECT * FROM ( SELECT processid FROM metadata WHERE (name=? and value LIKE ?)) as t"; (NOSONAR)
    }

    private void createLegacySelect(StringBuilder b) {
        b.append("SELECT prozesse.ProzesseID,  metadatenkonfigurationen.Datei ");
    }

    private void createLegacyFrom(StringBuilder b) {
        b.append(
                "FROM metadata LEFT JOIN prozesse ON metadata.processid = prozesse.ProzesseID LEFT JOIN metadatenkonfigurationen on metadatenkonfigurationen.MetadatenKonfigurationID=prozesse.MetadatenKonfigurationID ");
        if (this.filterProjects != null && !this.filterProjects.isEmpty()) {
            b.append("LEFT JOIN projekte ON prozesse.ProjekteID = projekte.ProjekteID ");
        }
        if ((this.filterTemplateIDs != null && !this.filterTemplateIDs.isEmpty()) || (this.propName != null && !this.propName.isEmpty())) {
            b.append("LEFT JOIN prozesseeigenschaften ON prozesse.prozesseID = prozesseeigenschaften.prozesseID ");
        }
        if (this.stepName != null && !this.stepName.isEmpty()) {
            b.append("LEFT JOIN schritte ON prozesse.prozesseID = schritte.ProzesseID ");
        }
    }

    private void createLegacyWhere(StringBuilder b) {

        boolean firstWhere = true;
        String conj = metadataConjunctive ? "AND " : "OR ";

        b.append("WHERE ");

        firstWhere = addFilters(b, firstWhere);

        if (!firstWhere) {
            b.append("AND ");
        }

        b.append("prozesse.ProzesseID IN ( SELECT * FROM ( SELECT processid FROM metadata WHERE (");
        for (int i = 0; i < metadataFilters.size(); i++) {
            SearchGroup sg = metadataFilters.get(i);
            sg.createLegacySqlClause(b);
            if (i + 1 < metadataFilters.size()) {
                b.append(conj);
            }
        }
        b.append(") ORDER BY metadata.processid ASC ");
        if (limit != 0) {
            b.append("LIMIT ? OFFSET ? ");
        }
        b.append(") as t) ");
    }

    public Object[] createLegacySqlParams() {
        List<Object> params = new ArrayList<>();
        addLegacyWhereParams(params);
        Object[] paramsArr = new Object[params.size()];
        params.toArray(paramsArr);
        return paramsArr;
    }

    private void addLegacyWhereParams(List<Object> params) {
        if (this.filterProjects != null) {
            for (String project : this.filterProjects) {
                params.add(project);
            }
        }

        if (this.propName != null) {
            params.add(this.propName);
            params.add(this.propValue);
        }

        if (this.stepName != null) {
            params.add(this.stepName);
            params.add(this.stepStatus);
        }

        if (metadataFilters != null) {
            for (SearchGroup sg : metadataFilters) {
                sg.addLegacyParams(params);
            }
        }
        if (limit != 0) {
            params.add(limit);
            params.add(offset);
        }
    }

}
