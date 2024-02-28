package de.sub.goobi.persistence.managers;

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
 */
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.beans.Template;
import org.goobi.beans.Templateproperty;

import de.sub.goobi.persistence.managers.MySQLHelper.SQLTYPE;

class TemplateMysqlHelper implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7884412786643552896L;

    public static List<Template> getTemplatesForProcess(int processId) throws SQLException {
        Connection connection = null;

        String sql = " SELECT * from vorlagen WHERE ProzesseID = " + processId;

        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, resultSetToTemplateListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static final ResultSetHandler<List<Template>> resultSetToTemplateListHandler = new ResultSetHandler<List<Template>>() {
        @Override
        public List<Template> handle(ResultSet rs) throws SQLException {
            List<Template> answer = new ArrayList<>();
            try {
                while (rs.next()) { // implies that rs != null
                    int templateId = rs.getInt("VorlagenID");
                    String herkunft = rs.getString("Herkunft");
                    int processId = rs.getInt("ProzesseID");
                    Template template = new Template();
                    template.setId(templateId);
                    template.setHerkunft(herkunft);
                    template.setProcessId(processId);
                    List<Templateproperty> properties = PropertyManager.getTemplateProperties(templateId);
                    template.setEigenschaften(properties);
                    answer.add(template);
                }
            } finally {
                rs.close();
            }
            return answer;
        }
    };

    public static final ResultSetHandler<Template> resultSetToTemplateHandler = new ResultSetHandler<Template>() {
        @Override
        public Template handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) {
                    int templateId = rs.getInt("VorlagenID");
                    String herkunft = rs.getString("Herkunft");
                    int processId = rs.getInt("ProzesseID");
                    Template template = new Template();
                    template.setId(templateId);
                    template.setHerkunft(herkunft);
                    template.setProcessId(processId);
                    List<Templateproperty> properties = PropertyManager.getTemplateProperties(templateId);
                    template.setEigenschaften(properties);
                    return template;
                }
            } finally {
                rs.close();
            }
            return null;
        }
    };

    public static Template getTemplateForTemplateID(int templateId) throws SQLException {
        Connection connection = null;
        String sql = " SELECT * from vorlagen WHERE VorlagenID = " + templateId;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, resultSetToTemplateHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int getCountOfTemplates() throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder(" SELECT count(1) from vorlagen ");
        if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.MYSQL) {
            sql.append(" WHERE VorlagenID > 0 ");
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

    public static void saveTemplate(Template template) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            String sql = "";

            if (template.getId() == null) {
                sql = "INSERT INTO vorlagen ( Herkunft, ProzesseID ) VALUES ( ?, ?)";
                Object[] param = { template.getHerkunft() == null ? null : template.getHerkunft(), template.getProzess().getId() };

                int id = run.insert(connection, sql, MySQLHelper.resultSetToIntegerHandler, param);
                template.setId(id);
            } else {
                sql = "UPDATE vorlagen set Herkunft = ?, ProzesseID = ? WHERE VorlagenID =" + template.getId();
                try {
                    Object[] param = { template.getHerkunft() == null ? null : template.getHerkunft(), template.getProzess().getId() };
                    run.update(connection, sql, param);
                } catch (NullPointerException e) {
                    // Null pointer seems just to happen in embedded database
                }
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

        List<Templateproperty> templateProperties = template.getEigenschaften();
        for (Templateproperty property : templateProperties) {
            property.setTemplateId(template.getId());
            property = PropertyManager.saveTemplateProperty(property);
        }
    }

    public static void deleteTemplate(Template template) throws SQLException {
        if (template.getId() != null) {
            for (Templateproperty property : template.getEigenschaften()) {
                PropertyManager.deleteTemplateProperty(property);
            }
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM vorlagen WHERE VorlagenID = " + template.getId();
                run.update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }

    }
}
