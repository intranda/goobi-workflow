package de.sub.goobi.persistence.managers;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;
import org.goobi.beans.Institution;
import org.goobi.beans.Ruleset;

class RulesetMysqlHelper implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -4768263760579941811L;
    private static final Logger logger = Logger.getLogger(RulesetMysqlHelper.class);

    public static List<Ruleset> getRulesets(String order, String filter, Integer start, Integer count, Institution institution) throws SQLException {
        boolean whereSet = false;
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM metadatenkonfigurationen");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
            whereSet = true;
        }
        if (institution != null && !institution.isAllowAllRulesets()) {
            if (whereSet) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
            }
            sql.append("MetadatenKonfigurationID in (SELECT object_id FROM institution_configuration where object_type = 'ruleset' and selected = true and institution_id = ");
            sql.append(institution.getId());
            sql.append(") ");
        }

        if (order != null && !order.isEmpty()) {
            sql.append(" ORDER BY " + order);
        }
        if (start != null && count != null) {
            sql.append(" LIMIT " + start + ", " + count);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            List<Ruleset> ret = new QueryRunner().query(connection, sql.toString(), RulesetManager.resultSetToRulesetListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Ruleset> getAllRulesets() throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM metadatenkonfigurationen order by titel");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            List<Ruleset> ret = new QueryRunner().query(connection, sql.toString(), RulesetManager.resultSetToRulesetListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int getRulesetCount(String order, String filter, Institution institution) throws SQLException {
        boolean whereSet = false;
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(MetadatenKonfigurationID) FROM metadatenkonfigurationen");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
            whereSet = true;
        }
        if (institution != null && !institution.isAllowAllRulesets()) {
            if (whereSet) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
            }
            sql.append("MetadatenKonfigurationID in (SELECT object_id FROM institution_configuration where object_type = 'ruleset' and selected = true and institution_id = ");
            sql.append(institution.getId());
            sql.append(") ");
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Ruleset getRulesetForId(int rulesetId) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM metadatenkonfigurationen WHERE MetadatenKonfigurationID = ? ");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Object[] params = { rulesetId };
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString() + ", " + rulesetId);
            }
            Ruleset ret = new QueryRunner().query(connection, sql.toString(), RulesetManager.resultSetToRulesetHandler, params);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveRuleset(Ruleset ro) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();

            if (ro.getId() == null) {
                String propNames = "Titel, Datei, orderMetadataByRuleset";
                String propValues = "?,? ,?";
                Object[] param = { ro.getTitel(), ro.getDatei(), ro.isOrderMetadataByRuleset() };

                sql.append("INSERT INTO metadatenkonfigurationen (");
                sql.append(propNames);
                sql.append(") VALUES (");
                sql.append(propValues);
                sql.append(")");

                if (logger.isTraceEnabled()) {
                    logger.trace(sql.toString() + ", " + Arrays.toString(param));
                }
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, param);
                if (id != null) {
                    ro.setId(id);
                }
            } else {
                Object[] param = { ro.getTitel(), ro.getDatei(), ro.isOrderMetadataByRuleset() };
                sql.append("UPDATE metadatenkonfigurationen SET ");
                sql.append("Titel = ?, ");
                sql.append("Datei = ?, ");
                sql.append("orderMetadataByRuleset = ?");
                sql.append(" WHERE MetadatenKonfigurationID = " + ro.getId() + ";");
                if (logger.isTraceEnabled()) {
                    logger.trace(sql.toString() + ", " + Arrays.toString(param));
                }
                run.update(connection, sql.toString(), param);
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteRuleset(Ruleset ro) throws SQLException {
        if (ro.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM metadatenkonfigurationen WHERE MetadatenKonfigurationID = " + ro.getId() + ";";
                if (logger.isTraceEnabled()) {
                    logger.trace(sql);
                }
                run.update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static Ruleset getRulesetByName(String rulesetName) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM metadatenkonfigurationen WHERE Titel = ? ");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Ruleset ret = new QueryRunner().query(connection, sql.toString(), RulesetManager.resultSetToRulesetHandler, rulesetName);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }
}
