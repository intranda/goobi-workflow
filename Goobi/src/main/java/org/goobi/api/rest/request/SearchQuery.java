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

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
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
