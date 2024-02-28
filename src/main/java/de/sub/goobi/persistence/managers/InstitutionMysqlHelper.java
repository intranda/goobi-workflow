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
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.goobi.beans.Institution;
import org.goobi.beans.InstitutionConfigurationObject;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;

import lombok.extern.log4j.Log4j2;

@Log4j2
class InstitutionMysqlHelper implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -3770765416080184593L;

    static List<Institution> getInstitutions(String order, String filter, Integer start, Integer count) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM institution");
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
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }

            return new QueryRunner().query(connection, sql.toString(), InstitutionManager.resultSetToInstitutionListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static int getInstitutionCount(String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) FROM institution");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static Institution getInstitutionById(int id) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM institution WHERE id = " + id);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), InstitutionManager.resultSetToInstitutionHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static Institution getInstitutionByName(String longName) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM institution WHERE longName  = ?");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), InstitutionManager.resultSetToInstitutionHandler, longName);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static void saveInstitution(Institution ro) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();

            String additionalData = MySQLHelper.convertDataToString(ro.getAdditionalData());

            if (ro.getId() == null) {
                sql.append("INSERT INTO institution ( ");
                sql.append("shortName, longName, allowAllRulesets, allowAllDockets, allowAllAuthentications, allowAllPlugins, additional_data ");
                sql.append(") VALUES (");
                sql.append("?,?,?,?,?,?,?");
                sql.append(")");

                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, ro.getShortName(), ro.getLongName(),
                        ro.isAllowAllRulesets(), ro.isAllowAllDockets(), ro.isAllowAllAuthentications(), ro.isAllowAllPlugins(), additionalData);
                if (id != null) {
                    ro.setId(id);
                }
            } else {
                sql.append("update institution set ");
                sql.append("shortName = ?, longName = ?,  allowAllRulesets = ?, allowAllDockets = ?, allowAllAuthentications = ?, ");
                sql.append("allowAllPlugins = ?, additional_data = ? ");
                sql.append(" WHERE id = ?");
                run.update(connection, sql.toString(), ro.getShortName(), ro.getLongName(), ro.isAllowAllRulesets(), ro.isAllowAllDockets(),
                        ro.isAllowAllAuthentications(), ro.isAllowAllPlugins(), additionalData, ro.getId());
            }

            // save list of configured rulests, dockets, auth
            if (!ro.isAllowAllRulesets()) {
                saveConfiguration(ro.getAllowedRulesets(), ro.getId());
            }
            if (!ro.isAllowAllDockets()) {
                saveConfiguration(ro.getAllowedDockets(), ro.getId());
            }
            if (!ro.isAllowAllAuthentications()) {
                saveConfiguration(ro.getAllowedAuthentications(), ro.getId());
            }
            if (!ro.isAllowAllPlugins()) {
                saveConfiguration(ro.getAllowedAdministrationPlugins(), ro.getId());
                saveConfiguration(ro.getAllowedWorkflowPlugins(), ro.getId());
                saveConfiguration(ro.getAllowedDashboardPlugins(), ro.getId());
                saveConfiguration(ro.getAllowedStatisticsPlugins(), ro.getId());
            }

            for (JournalEntry logEntry : ro.getJournal()) {
                JournalManager.saveJournalEntry(logEntry);
            }

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static void saveConfiguration(List<InstitutionConfigurationObject> allowedItems, Integer institutionId) throws SQLException {
        StringBuilder insert = new StringBuilder();
        insert.append("INSERT INTO institution_configuration (institution_id, object_id, object_type,  object_name, selected  )");
        insert.append(" VALUES (?,?,?,?,?)");

        StringBuilder update = new StringBuilder();
        update.append("UPDATE institution_configuration SET institution_id = ?, object_id = ?,  object_type = ?, ");
        update.append("object_name = ?, selected = ? WHERE id = ?");
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            for (InstitutionConfigurationObject ico : allowedItems) {
                if (ico.getId() == null) {
                    Integer id = run.insert(connection, insert.toString(), MySQLHelper.resultSetToIntegerHandler, institutionId, ico.getObject_id(),
                            ico.getObject_type(), ico.getObject_name(), ico.isSelected());
                    if (id != null) {
                        ico.setId(id);
                    }
                } else {
                    run.update(connection, update.toString(), institutionId, ico.getObject_id(), ico.getObject_type(), ico.getObject_name(),
                            ico.isSelected(), ico.getId());
                }
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    static void deleteInstitution(Institution ro) throws SQLException {
        if (ro.getId() != null) {

            JournalManager.deleteAllJournalEntries(ro.getId(), EntryType.INSTITUTION);

            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM institution WHERE id = " + ro.getId() + ";";
                if (log.isTraceEnabled()) {
                    log.trace(sql);
                }
                run.update(connection, sql);
                // delete list of configured rulests, dockets, auth
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    static List<Integer> getIdList(String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id FROM institution");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static List<Institution> getAllInstitutionsAsList() throws SQLException {
        String sql = "SELECT * FROM institution order by shortName";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql);
            }
            return new QueryRunner().query(connection, sql, InstitutionManager.resultSetToInstitutionListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static List<InstitutionConfigurationObject> getConfiguredRulesets(Integer institutionId) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    id, ");
        sql.append("    institution_id, ");
        sql.append("    MetadatenKonfigurationID AS object_id, ");
        sql.append("    'ruleset' AS object_type, ");
        sql.append("    titel AS object_name, ");
        sql.append("    selected ");
        sql.append("FROM ");
        sql.append("    metadatenkonfigurationen ");
        sql.append("        LEFT JOIN ");
        sql.append("    institution_configuration ON object_id = MetadatenkonfigurationID ");
        sql.append("        AND object_type = 'ruleset' ");
        sql.append("        AND (institution_id IS NULL OR institution_id = ? ) ORDER by Titel");
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), new BeanListHandler<>(InstitutionConfigurationObject.class), institutionId);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static List<InstitutionConfigurationObject> getConfiguredDockets(Integer institutionId) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    id, ");
        sql.append("    institution_id, ");
        sql.append("    docketID AS object_id, ");
        sql.append("    'docket' AS object_type, ");
        sql.append("    name AS object_name, ");
        sql.append("    selected ");
        sql.append("FROM ");
        sql.append("    dockets ");
        sql.append("        LEFT JOIN ");
        sql.append("    institution_configuration ON object_id = docketID ");
        sql.append("        AND object_type = 'docket' ");
        sql.append("        AND (institution_id IS NULL ");
        sql.append("        OR institution_id = ?) ");
        sql.append("ORDER BY name ");
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), new BeanListHandler<>(InstitutionConfigurationObject.class), institutionId);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static List<InstitutionConfigurationObject> getConfiguredAuthentications(Integer institutionId) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    id, ");
        sql.append("    institution_id, ");
        sql.append("    ldapgruppenID AS object_id, ");
        sql.append("    'authentication' AS object_type, ");
        sql.append("    titel AS object_name, ");
        sql.append("    selected ");
        sql.append("FROM ");
        sql.append("    ldapgruppen ");
        sql.append("        LEFT JOIN ");
        sql.append("    institution_configuration ON object_id = ldapgruppenID ");
        sql.append("        AND object_type = 'authentication' ");
        sql.append("        AND (institution_id IS NULL ");
        sql.append("        OR institution_id = ?) ");
        sql.append("ORDER BY titel ");
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), new BeanListHandler<>(InstitutionConfigurationObject.class), institutionId);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static List<String> getInstitutionNames() throws SQLException {
        String sql = "SELECT shortName FROM institution ORDER BY shortName";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql);
            }
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToStringListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static List<InstitutionConfigurationObject> getConfiguredAdministrationPlugins(Integer institutionId, List<String> pluginNames)
            throws SQLException {
        StringBuilder sql = getPluginStatement(pluginNames, "administrationPlugin");
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), new BeanListHandler<>(InstitutionConfigurationObject.class), institutionId);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static List<InstitutionConfigurationObject> getConfiguredWorkflowPlugins(Integer institutionId, List<String> pluginNames) throws SQLException {
        StringBuilder sql = getPluginStatement(pluginNames, "workflowPlugin");
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), new BeanListHandler<>(InstitutionConfigurationObject.class), institutionId);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static List<InstitutionConfigurationObject> getConfiguredDashboardPlugins(Integer institutionId, List<String> pluginNames) throws SQLException {
        StringBuilder sql = getPluginStatement(pluginNames, "dashboardPlugin");
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), new BeanListHandler<>(InstitutionConfigurationObject.class), institutionId);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static List<InstitutionConfigurationObject> getConfiguredStatisticsPlugins(Integer institutionId, List<String> pluginNames) throws SQLException {
        StringBuilder sql = getPluginStatement(pluginNames, "statisticsPlugin");
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), new BeanListHandler<>(InstitutionConfigurationObject.class), institutionId);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static StringBuilder getPluginStatement(List<String> pluginNames, String pluginType) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    id, ");
        sql.append("    institution_id, ");
        sql.append("    0 as object_id, ");
        sql.append("    '");
        sql.append(pluginType);
        sql.append("' AS object_type, ");
        sql.append("    pluginName AS object_name, ");
        sql.append("    selected ");
        sql.append("FROM ");
        for (int i = 0; i < pluginNames.size(); i++) {
            if (i == 0) {
                sql.append("(SELECT '");
                sql.append(pluginNames.get(i));
                sql.append("' AS pluginName ");
            } else {
                sql.append("UNION SELECT '");
                sql.append(pluginNames.get(i));
                sql.append("' ");
            }
        }
        sql.append(") AS plugins ");
        sql.append("        LEFT JOIN ");
        sql.append("    institution_configuration ON object_name = pluginName ");
        sql.append("        AND object_type = '");
        sql.append(pluginType);
        sql.append("' ");
        sql.append("        AND (institution_id IS NULL ");
        sql.append("        OR institution_id = ?) ");
        sql.append("ORDER BY pluginName ");
        return sql;
    }

}
