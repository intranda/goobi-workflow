package de.sub.goobi.persistence.managers;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
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
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;

class UsergroupMysqlHelper implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -6209215029673643876L;
    private static final Logger logger = Logger.getLogger(UsergroupMysqlHelper.class);

    public static List<Usergroup> getUsergroups(String order, String filter, Integer start, Integer count) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM benutzergruppen");
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
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }
            List<Usergroup> ret = new QueryRunner().query(connection, sql.toString(), UsergroupManager.resultSetToUsergroupListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Usergroup> getUsergroupsForUser(User user) throws SQLException {
        return getUsergroups("titel", "BenutzergruppenID IN (SELECT BenutzerGruppenID FROM benutzergruppenmitgliedschaft WHERE BenutzerID="
                + user.getId() + ")", null, null);
    }

    public static int getUsergroupCount(String order, String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(BenutzergruppenID) FROM benutzergruppen");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Usergroup getUsergroupById(int id) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM benutzergruppen WHERE BenutzergruppenID = " + id);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString());
            }
            Usergroup ret = new QueryRunner().query(connection, sql.toString(), UsergroupManager.resultSetToUsergroupHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveUsergroup(Usergroup ro) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();

            if (ro.getId() == null) {
                Object[] param = { ro.getTitel(), ro.getBerechtigung() };
                String propNames = "titel, berechtigung";
                String propValues = "? ,?";
                sql.append("INSERT INTO benutzergruppen (");
                sql.append(propNames);
                sql.append(") VALUES (");
                sql.append(propValues);
                sql.append(")");
                if (logger.isDebugEnabled()) {
                    logger.debug(sql.toString() + ", " + Arrays.toString(param));
                }
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, param);
                if (id != null) {
                    ro.setId(id);
                }
            } else {
                Object[] param = { ro.getTitel(), ro.getBerechtigung() };
                sql.append("UPDATE benutzergruppen SET ");
                sql.append("titel = ?, ");
                sql.append("berechtigung = ? ");
                sql.append(" WHERE BenutzergruppenID = " + ro.getId() + ";");
                if (logger.isDebugEnabled()) {
                    logger.debug(sql.toString() + ", " + Arrays.toString(param));
                }

                run.update(connection, sql.toString(), param);
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteUsergroup(Usergroup ro) throws SQLException {
        if (ro.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM benutzergruppen WHERE BenutzergruppenID = " + ro.getId() + ";";
                if (logger.isDebugEnabled()) {
                    logger.debug(sql);
                }
                run.update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static List<Usergroup> getUserGroupsForStep(Integer stepId) throws SQLException {
        String sql =
                "select * from benutzergruppen where BenutzergruppenID in (select BenutzergruppenID from schritteberechtigtegruppen where  schritteberechtigtegruppen.schritteID = ? )";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            Object[] param = { stepId };
            if (logger.isDebugEnabled()) {
                logger.debug(sql.toString() + ", " + Arrays.toString(param));
            }
            return run.query(connection, sql, UsergroupManager.resultSetToUsergroupListHandler, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }
}
