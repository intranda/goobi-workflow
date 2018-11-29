package org.goobi.api.rest.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchQuery {
    private String field;
    private String value;
    private RelationalOperator relation;

    public void createLegacySqlClause(StringBuilder b) {
        b.append('(');
        b.append("name=? and value");
        b.append(relation.toString());
        b.append("?");
        b.append(')');
    }

    public enum RelationalOperator {
        EQUAL("="),
        NEQUAL("!="),
        LIKE(" LIKE "),
        NLIKE(" NOT LIKE ");

        private final String sqlStr;

        private RelationalOperator(String value) {
            this.sqlStr = value;
        }

        @Override
        public String toString() {
            return sqlStr;
        }

    }

    public RelationalOperator[] getOperators() {
        return RelationalOperator.values();
    }

    public void addLegacyParams(List<Object> params) {
        params.add(field);
        params.add(value);
    }

    public void createSqlClause(StringBuilder b) {
        b.append('(');
        switch (relation) {
            case EQUAL:
                b.append("JSON_CONTAINS(value, ?, ?)");
                break;
            case NEQUAL:
                b.append("NOT JSON_CONTAINS(value, ?, ?)");
                break;
            case LIKE:
                b.append("JSON_EXTRACT(value, ?) LIKE ?");
                break;
            case NLIKE:
                b.append("JSON_EXTRACT(value, ?) NOT LIKE ?");
                break;
        }
        b.append(')');
    }

    public void addParams(List<Object> params) {
        if (relation == RelationalOperator.EQUAL || relation == RelationalOperator.NEQUAL) {
            params.add("\"" + value + "\"");
            params.add("$." + field);
        } else {
            params.add("$." + field);
            params.add("%" + value + "%");
        }

    }
}
