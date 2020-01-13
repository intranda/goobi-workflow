//package de.sub.goobi.persistence.managers;
//
///**
// * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
// * 
// * Visit the websites for more information.
// *          - https://goobi.io
// *          - https://www.intranda.com
// *          - https://github.com/intranda/goobi
// * 
// * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
// * Software Foundation; either version 2 of the License, or (at your option) any later version.
// * 
// * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
// * Temple Place, Suite 330, Boston, MA 02111-1307 USA
// * 
// */
//import java.io.Serializable;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.Arrays;
//import java.util.List;
//
//import org.apache.commons.dbutils.QueryRunner;
//import org.apache.log4j.Logger;
//import org.goobi.vocabulary.VocabRecord;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//
//class VocabRecordMysqlHelper implements Serializable {
//
//    private static final long serialVersionUID = -5843980584376188001L;
//    private static final Logger logger = Logger.getLogger(VocabRecordMysqlHelper.class);
//
//    public static List<VocabRecord> getVocabRecords(String order, String filter, Integer start, Integer count) throws SQLException {
//        Connection connection = null;
//        StringBuilder sql = new StringBuilder();
//        sql.append("SELECT * FROM metadatenkonfigurationen");
//        if (filter != null && !filter.isEmpty()) {
//            sql.append(" WHERE " + filter);
//        }
//        if (order != null && !order.isEmpty()) {
//            sql.append(" ORDER BY " + order);
//        }
//        if (start != null && count != null) {
//            sql.append(" LIMIT " + start + ", " + count);
//        }
//        try {
//            connection = MySQLHelper.getInstance().getConnection();
//            if (logger.isTraceEnabled()) {
//                logger.trace(sql.toString());
//            }
//            List<VocabRecord> ret = new QueryRunner().query(connection, sql.toString(), VocabRecordManager.resultSetToVocabRecordListHandler);
//            return ret;
//        } finally {
//            if (connection != null) {
//                MySQLHelper.closeConnection(connection);
//            }
//        }
//    }
//
//    public static List<VocabRecord> getAllVocabRecords() throws SQLException {
//        Connection connection = null;
//        StringBuilder sql = new StringBuilder();
//        sql.append("SELECT * FROM metadatenkonfigurationen order by titel");
//        try {
//            connection = MySQLHelper.getInstance().getConnection();
//            if (logger.isTraceEnabled()) {
//                logger.trace(sql.toString());
//            }
//            List<VocabRecord> ret = new QueryRunner().query(connection, sql.toString(), VocabRecordManager.resultSetToVocabRecordListHandler);
//            return ret;
//        } finally {
//            if (connection != null) {
//                MySQLHelper.closeConnection(connection);
//            }
//        }
//    }
//
//    public static int getVocabRecordCount(String order, String filter) throws SQLException {
//        Connection connection = null;
//        StringBuilder sql = new StringBuilder();
//        sql.append("SELECT COUNT(MetadatenKonfigurationID) FROM metadatenkonfigurationen");
//        if (filter != null && !filter.isEmpty()) {
//            sql.append(" WHERE " + filter);
//        }
//        try {
//            connection = MySQLHelper.getInstance().getConnection();
//            if (logger.isTraceEnabled()) {
//                logger.trace(sql.toString());
//            }
//            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
//        } finally {
//            if (connection != null) {
//                MySQLHelper.closeConnection(connection);
//            }
//        }
//    }
//
//    public static VocabRecord getVocabRecordForId(int vocabId) throws SQLException {
//        Connection connection = null;
//        StringBuilder sql = new StringBuilder();
//        sql.append("SELECT * FROM metadatenkonfigurationen WHERE MetadatenKonfigurationID = ? ");
//        try {
//            connection = MySQLHelper.getInstance().getConnection();
//            Object[] params = { vocabId };
//            if (logger.isTraceEnabled()) {
//                logger.trace(sql.toString() + ", " + vocabId);
//            }
//            VocabRecord ret = new QueryRunner().query(connection, sql.toString(), VocabRecordManager.resultSetToVocabRecordHandler, params);
//            return ret;
//        } finally {
//            if (connection != null) {
//                MySQLHelper.closeConnection(connection);
//            }
//        }
//    }
//
//    public static void saveVocabRecord(VocabRecord ro, String strVocabularyId) throws SQLException {
//        Connection connection = null;
//        try {
//            connection = MySQLHelper.getInstance().getConnection();
//            QueryRunner run = new QueryRunner();
//            StringBuilder sql = new StringBuilder();
//
//            if (ro.getId() == null) {
//                String propNames = "VocabId, RecordId, jsonRecord";
//                String propValues = "?,? ,?";
//
//                Gson gson = new GsonBuilder().create();
//                String strJson = gson.toJson(ro);
//
//                Object[] param = { strVocabularyId, ro.getId(), strJson };
//
//                sql.append("INSERT INTO metadatenkonfigurationen (");
//                sql.append(propNames);
//                sql.append(") VALUES (");
//                sql.append(propValues);
//                sql.append(")");
//
//                if (logger.isTraceEnabled()) {
//                    logger.trace(sql.toString() + ", " + Arrays.toString(param));
//                }
//                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, param);
//                if (id != null) {
//                    ro.setId(id);
//                }
//            } else {
//                Gson gson = new GsonBuilder().create();
//                String strJson = gson.toJson(ro);
//                
//                Object[] param = { strVocabularyId, ro.getId(), strJson };
//                sql.append("UPDATE metadatenkonfigurationen SET ");
//                sql.append("VocabId = ?, ");
//                sql.append("RecordId = ?, ");
//                sql.append("jsonRecord = ?");
//                sql.append(" WHERE MetadatenKonfigurationID = " + ro.getId() + ";");
//                if (logger.isTraceEnabled()) {
//                    logger.trace(sql.toString() + ", " + Arrays.toString(param));
//                }
//                run.update(connection, sql.toString(), param);
//            }
//        } finally {
//            if (connection != null) {
//                MySQLHelper.closeConnection(connection);
//            }
//        }
//    }
//
//    public static void deleteVocabRecord(VocabRecord ro) throws SQLException {
//        if (ro.getId() != null) {
//            Connection connection = null;
//            try {
//                connection = MySQLHelper.getInstance().getConnection();
//                QueryRunner run = new QueryRunner();
//                String sql = "DELETE FROM metadatenkonfigurationen WHERE MetadatenKonfigurationID = " + ro.getId() + ";";
//                if (logger.isTraceEnabled()) {
//                    logger.trace(sql);
//                }
//                run.update(connection, sql);
//            } finally {
//                if (connection != null) {
//                    MySQLHelper.closeConnection(connection);
//                }
//            }
//        }
//    }
//
//    public static VocabRecord getVocabRecordByName(String rulesetName) throws SQLException {
//        Connection connection = null;
//        StringBuilder sql = new StringBuilder();
//        sql.append("SELECT * FROM metadatenkonfigurationen WHERE Titel = ? ");
//        try {
//            connection = MySQLHelper.getInstance().getConnection();
//            VocabRecord ret = new QueryRunner().query(connection, sql.toString(), VocabRecordManager.resultSetToVocabRecordHandler, rulesetName);
//            return ret;
//        } finally {
//            if (connection != null) {
//                MySQLHelper.closeConnection(connection);
//            }
//        }
//    }
//}
