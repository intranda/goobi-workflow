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
import org.goobi.beans.Masterpiece;
import org.goobi.beans.Masterpieceproperty;

import de.sub.goobi.persistence.managers.MySQLHelper.SQLTYPE;

class MasterpieceMysqlHelper implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4634402187657044322L;

    public static List<Masterpiece> getMasterpiecesForProcess(int processId) throws SQLException {
        Connection connection = null;

        String sql = " SELECT * from werkstuecke WHERE ProzesseID = " + processId;

        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, resultSetToMasterpieceListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static final ResultSetHandler<List<Masterpiece>> resultSetToMasterpieceListHandler = new ResultSetHandler<>() {
        @Override
        public List<Masterpiece> handle(ResultSet rs) throws SQLException {
            List<Masterpiece> answer = new ArrayList<>();
            try {
                while (rs.next()) { // implies that rs != null, while the case rs == null will be thrown as an Exception
                    int id = rs.getInt("WerkstueckeID");
                    int processId = rs.getInt("ProzesseID");
                    Masterpiece object = new Masterpiece();
                    object.setId(id);
                    object.setProcessId(processId);
                    List<Masterpieceproperty> properties = PropertyManager.getMasterpieceProperties(id);
                    object.setEigenschaften(properties);
                    answer.add(object);
                }
            } finally {
                rs.close();
            }
            return answer;
        }
    };

    public static final ResultSetHandler<Masterpiece> resultSetToMasterpieceHandler = new ResultSetHandler<>() {
        @Override
        public Masterpiece handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) { // implies that rs != null, while the case rs == null will be thrown as an Exception
                    int id = rs.getInt("WerkstueckeID");
                    int processId = rs.getInt("ProzesseID");
                    Masterpiece object = new Masterpiece();
                    object.setId(id);
                    object.setProcessId(processId);
                    List<Masterpieceproperty> properties = PropertyManager.getMasterpieceProperties(id);
                    object.setEigenschaften(properties);
                    return object;
                }
            } finally {
                rs.close();
            }
            return null;
        }
    };

    public static Masterpiece getMasterpieceForTemplateID(int templateId) throws SQLException {
        Connection connection = null;
        String sql = " SELECT * from werkstuecke WHERE WerkstueckeID = " + templateId;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, resultSetToMasterpieceHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int countMasterpieces() throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder("SELECT count(1) from werkstuecke ");
        if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.MYSQL) {
            sql.append("WHERE WerkstueckeID > 0;");
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

    public static void saveMasterpiece(Masterpiece object) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            String sql = "";

            if (object.getId() == null) {
                sql = "INSERT INTO werkstuecke (  ProzesseID ) VALUES ( ?)";
                Object[] param = { object.getProzess().getId() };

                int id = run.insert(connection, sql, MySQLHelper.resultSetToIntegerHandler, param);
                object.setId(id);
            } else {
                sql = "UPDATE werkstuecke set  ProzesseID = ? WHERE WerkstueckeID =" + object.getId();
                try {
                    Object[] param = { object.getProzess().getId() };
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

        List<Masterpieceproperty> templateProperties = object.getEigenschaften();
        for (Masterpieceproperty property : templateProperties) {
            property.setObjectId(object.getId());
            PropertyManager.saveMasterpieceProperty(property);
        }
    }

    public static void deleteMasterpiece(Masterpiece object) throws SQLException {
        if (object.getId() != null) {
            for (Masterpieceproperty property : object.getEigenschaften()) {
                PropertyManager.deleteMasterpieceProperty(property);
            }
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM werkstuecke WHERE WerkstueckeID = " + object.getId();
                run.update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }
}
