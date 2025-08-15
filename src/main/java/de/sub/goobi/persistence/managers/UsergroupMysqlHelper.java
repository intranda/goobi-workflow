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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.goobi.beans.Institution;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;

import lombok.extern.log4j.Log4j2;

@Log4j2
final class UsergroupMysqlHelper implements Serializable {

    private UsergroupMysqlHelper() {
        // hide implicit public constructor
    }

    private static final long serialVersionUID = -6209215029673643876L;

    public static List<Usergroup> getUsergroups(String order, String filter, Integer start, Integer count, Institution institution)
            throws SQLException {
        boolean whereSet = false;
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM benutzergruppen");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
            whereSet = true;
        }
        if (institution != null) {
            if (whereSet) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
            }
            sql.append("institution_id = ");
            sql.append(institution.getId());
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
            return new QueryRunner().query(connection, sql.toString(), UsergroupManager.resultSetToUsergroupListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Usergroup> getUsergroupsForUser(User user) throws SQLException {
        return getUsergroups("titel",
                "BenutzergruppenID IN (SELECT BenutzerGruppenID FROM benutzergruppenmitgliedschaft WHERE BenutzerID=" + user.getId() + ")", null,
                null, null);
    }

    public static int getUsergroupCount(String filter, Institution institution) throws SQLException {
        boolean whereSet = false;
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) FROM benutzergruppen");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
            whereSet = true;
        }

        if (institution != null) {
            if (whereSet) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
            }
            sql.append("institution_id = ");
            sql.append(institution.getId());
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

    public static Usergroup getUsergroupById(int id) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM benutzergruppen WHERE BenutzergruppenID = " + id);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), UsergroupManager.resultSetToUsergroupHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveUsergroup(Usergroup ro) throws SQLException {
        Connection connection = null;
        StringBuilder userRoles = new StringBuilder();
        if (ro.getUserRoles() != null) {
            for (String userRole : ro.getUserRoles()) {
                userRoles.append(userRole);
                userRoles.append(";");
            }
        }

        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();

            if (ro.getId() == null) {
                Object[] param =
                        { ro.getTitel(), ro.getBerechtigung(), userRoles.length() == 0 ? null : userRoles.toString(), ro.getInstitution().getId() };
                String propNames = "titel, berechtigung, roles, institution_id";
                String propValues = "? ,?, ?, ?";
                sql.append("INSERT INTO benutzergruppen (");
                sql.append(propNames);
                sql.append(") VALUES (");
                sql.append(propValues);
                sql.append(")");
                if (log.isTraceEnabled()) {
                    log.trace(sql.toString() + ", " + Arrays.toString(param));
                }
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, param);
                if (id != null) {
                    ro.setId(id);
                }
            } else {
                Object[] param =
                        { ro.getTitel(), ro.getBerechtigung(), userRoles.length() == 0 ? null : userRoles.toString(), ro.getInstitution().getId() };
                sql.append("UPDATE benutzergruppen SET ");
                sql.append("titel = ?, ");
                sql.append("berechtigung = ?, ");
                sql.append("roles = ?, institution_id = ? ");
                sql.append(" WHERE BenutzergruppenID = " + ro.getId() + ";");
                if (log.isTraceEnabled()) {
                    log.trace(sql.toString() + ", " + Arrays.toString(param));
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
                if (log.isTraceEnabled()) {
                    log.trace(sql);
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
                "select * from benutzergruppen where BenutzergruppenID in "
                        + "(select BenutzergruppenID from schritteberechtigtegruppen where  schritteberechtigtegruppen.schritteID = ? )";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            Object[] param = { stepId };
            if (log.isTraceEnabled()) {
                log.trace(sql + ", " + Arrays.toString(param));
            }
            return run.query(connection, sql, UsergroupManager.resultSetToUsergroupListHandler, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static List<String> getAllUsergroupNames() throws SQLException {
        String sql = "SELECT titel FROM benutzergruppen ORDER BY titel";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            return run.query(connection, sql, MySQLHelper.resultSetToStringListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static Usergroup getUsergroupByName(String name) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM benutzergruppen WHERE titel = ?");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), UsergroupManager.resultSetToUsergroupHandler, name);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Usergroup> getAllUsergroups() throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM benutzergruppen");

        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), UsergroupManager.resultSetToUsergroupListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }
}
