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
    private String filterStep;
    private String structureType;

    private Set<String> wantedFields;

    private String sortField;
    private boolean sortDescending;

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

    public List<RestProcess> search() throws SQLException {
        List<RestProcess> processes = RestDbHelper.searchProcesses(this);
        MetadataUtils.addMetadataToRestProcesses(processes, this);
        return processes;
    }

    public String createSql() {
        //example sql: select * from metadata left join prozesse on metadata.processid = prozesse.ProzesseID where prozesse.ProzesseID IN 
        //(select processid from metadata where metadata.name="_dateDigitization" and metadata.value="2018");
        StringBuilder b = new StringBuilder();
        createySelect(b);
        createFrom(b);
        createWhere(b);
        createOrderAndLimit(b);
        return b.toString();
    }

    private void createySelect(StringBuilder b) {
        b.append("SELECT prozesse.ProzesseID,  metadatenkonfigurationen.Datei ");
        if (this.filterProjects != null && !this.filterProjects.isEmpty()) {
            b.append(", projekte.Titel ");
        }
    }

    private void createFrom(StringBuilder b) {
        b.append(
                "FROM metadata_json LEFT JOIN prozesse ON metadata_json.processid = prozesse.ProzesseID LEFT JOIN metadatenkonfigurationen on metadatenkonfigurationen.MetadatenKonfigurationID=prozesse.MetadatenKonfigurationID ");
        if (this.filterProjects != null && !this.filterProjects.isEmpty()) {
            b.append("LEFT JOIN projekte ON prozesse.ProjekteID = projekte.ProjekteID ");
        }
    }

    private void createWhere(StringBuilder b) {
        String conj = metadataConjunctive ? "AND " : "OR ";
        b.append("WHERE ");
        if (this.filterProjects != null && !this.filterProjects.isEmpty()) {
            b.append("projekte.Titel IN (");
            for (int i = 0; i < this.filterProjects.size(); i++) {
                b.append("?");
                if (i + 1 < this.filterProjects.size()) {
                    b.append(", ");
                }
            }
            b.append(") AND ");
        }
        for (int i = 0; i < metadataFilters.size(); i++) {
            SearchGroup sg = metadataFilters.get(i);
            sg.createSqlClause(b);
            if (i + 1 < metadataFilters.size()) {
                b.append(conj);
            }
        }
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
        //(select processid from metadata where metadata.name="_dateDigitization" and metadata.value="2018");
        StringBuilder builder = new StringBuilder();
        createLegacySelect(builder);
        createLegacyFrom(builder);
        createLegacyWhere(builder);
        return builder.toString();
    }

    private void createLegacySelect(StringBuilder b) {
        b.append("SELECT prozesse.ProzesseID,  metadatenkonfigurationen.Datei ");
    }

    private void createLegacyFrom(StringBuilder b) {
        b.append(
                "FROM metadata LEFT JOIN prozesse ON metadata.processid = prozesse.ProzesseID LEFT JOIN metadatenkonfigurationen on metadatenkonfigurationen.MetadatenKonfigurationID=prozesse.MetadatenKonfigurationID ");
    }

    private void createLegacyWhere(StringBuilder b) {
        String conj = metadataConjunctive ? "AND " : "OR ";
        b.append("WHERE prozesse.ProzesseID IN ( SELECT * FROM ( SELECT processid FROM metadata WHERE (");
        for (int i = 0; i < metadataFilters.size(); i++) {
            SearchGroup sg = metadataFilters.get(i);
            sg.createLegacySqlClause(b);
            if (i + 1 < metadataFilters.size()) {
                b.append(conj);
            }
        }
        b.append(") ORDER BY processid ASC ");
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
        for (String project : this.filterProjects) {
            params.add(project);
        }
        for (SearchGroup sg : metadataFilters) {
            sg.addLegacyParams(params);
        }
        if (limit != 0) {
            params.add(limit);
            params.add(offset);
        }
    }

}
