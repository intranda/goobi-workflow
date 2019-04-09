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
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;
import org.goobi.beans.Docket;

class DocketMysqlHelper implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8079331483121462356L;
    private static final Logger logger = Logger.getLogger(DocketMysqlHelper.class);

    public static List<Docket> getDockets(String order, String filter, Integer start, Integer count) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM dockets");
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
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            List<Docket> ret = new QueryRunner().query(connection, sql.toString(), DocketManager.resultSetToDocketListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int getDocketCount(String order, String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(docketID) FROM dockets");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
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

    public static Docket getDocketById(int id) throws SQLException {
        Connection connection = null;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM dockets WHERE docketID = " + id);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            Docket ret = new QueryRunner().query(connection, sql.toString(), DocketManager.resultSetToDocketHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveDocket(Docket ro) throws SQLException {
        Connection connection = null;

        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();

            if (ro.getId() == null) {

                String propNames = "name, file";
                String propValues = "'" + ro.getName() + "','" + ro.getFile() + "'";
                sql.append("INSERT INTO dockets (");
                sql.append(propNames);
                sql.append(") VALUES (");
                sql.append(propValues);
                sql.append(")");
                if (logger.isTraceEnabled()) {
                    logger.trace(sql.toString());
                }
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
                if (id != null) {
                    ro.setId(id);
                }
            } else {
                sql.append("UPDATE dockets SET ");
                sql.append("name = '" + ro.getName() + "', ");
                sql.append("file = '" + ro.getFile() + "' ");
                sql.append(" WHERE docketID = " + ro.getId() + ";");
                if (logger.isTraceEnabled()) {
                    logger.trace(sql.toString());
                }
                run.update(connection, sql.toString());
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteDocket(Docket ro) throws SQLException {
        if (ro.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM dockets WHERE docketID = " + ro.getId() + ";";
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

    public static List<Docket> getAllDockets() throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM dockets");

        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            List<Docket> ret = new QueryRunner().query(connection, sql.toString(), DocketManager.resultSetToDocketListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Docket getDocketByName(String name) throws SQLException {
        Connection connection = null;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM dockets WHERE name = ?" );
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            Docket ret = new QueryRunner().query(connection, sql.toString(), DocketManager.resultSetToDocketHandler, name);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }
}
