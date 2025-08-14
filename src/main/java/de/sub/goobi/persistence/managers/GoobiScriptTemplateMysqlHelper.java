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
 */
package de.sub.goobi.persistence.managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.goobi.goobiScript.GoobiScriptTemplate;

public final class GoobiScriptTemplateMysqlHelper {

    private GoobiScriptTemplateMysqlHelper() {
        // do nothing
    }

    public static int getGoobiScriptTemplateCount(String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) FROM goobiscript_template");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();

            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static GoobiScriptTemplate getGoobiScriptTemplateById(int id) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) FROM goobiscript_template WHERE id = ?");

        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql.toString(), GoobiScriptTemplateManager.resultSetToGoobiScriptTemplatetHandler, id);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveGoobiScriptTemplate(GoobiScriptTemplate template) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();

            if (template.getId() == null) {

                sql.append("INSERT INTO goobiscript_template (");
                sql.append("title, description, goobiscript");
                sql.append(") VALUES (");
                sql.append("?,? ,?");
                sql.append(")");

                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, template.getTitle(),
                        template.getDescription(), template.getGoobiScripts());
                if (id != null) {
                    template.setId(id);
                }
            } else {
                sql.append("UPDATE goobiscript_template SET ");
                sql.append("title = ?, ");
                sql.append("description = ?, ");
                sql.append("goobiscript = ?");
                sql.append(" WHERE id = " + template.getId());
                run.update(connection, sql.toString(), template.getTitle(), template.getDescription(), template.getGoobiScripts());
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteGoobiScriptTemplate(GoobiScriptTemplate template) throws SQLException {
        if (template.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM goobiscript_template WHERE id = " + template.getId();

                run.update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }

    }

    public static List<GoobiScriptTemplate> getGoobiScriptTemplates(String order, String filter, Integer start, Integer count) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM goobiscript_template");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }

        if (order != null && !order.isEmpty()) {
            sql.append(" ORDER BY " + order);
        }
        if (start != null && count != null) {
            sql.append(" LIMIT " + start + ", " + count);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();

            return new QueryRunner().query(connection, sql.toString(), GoobiScriptTemplateManager.resultSetToGoobiScriptTemplateListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<GoobiScriptTemplate> getAllGoobiScriptTemplates() throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM goobiscript_template order by title");
        try {
            connection = MySQLHelper.getInstance().getConnection();

            return new QueryRunner().query(connection, sql.toString(), GoobiScriptTemplateManager.resultSetToGoobiScriptTemplateListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

}
