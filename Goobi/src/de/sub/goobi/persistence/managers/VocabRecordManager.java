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
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.commons.dbutils.ResultSetHandler;
//import org.apache.log4j.Logger;
//import org.goobi.beans.DatabaseObject;
//import org.goobi.managedbeans.VocabularyBean;
//import org.goobi.vocabulary.Definition;
//import org.goobi.vocabulary.Field;
//import org.goobi.vocabulary.VocabRecord;
//import org.goobi.vocabulary.VocabularyManager;
//
//import com.google.gson.Gson;
//
//import de.sub.goobi.helper.exceptions.DAOException;
//
//public class VocabRecordManager implements IManager, Serializable {
//    /**
//     * 
//     */
//    private static final long serialVersionUID = -7947612330017761755L;
//
//    private static final Logger logger = Logger.getLogger(VocabRecordManager.class);
//
//    public static VocabRecord getVocabRecordById(int id) throws DAOException {
//        VocabRecord o = null;
//        try {
//            o = VocabRecordMysqlHelper.getVocabRecordForId(id);
//        } catch (SQLException e) {
//            logger.error("error while getting VocabRecord with id " + id, e);
//            throw new DAOException(e);
//        }
//        return o;
//    }
//
//    public static void saveVocabRecord(VocabRecord vocab, String strVocabularyId) throws DAOException {
//        try {
//            VocabRecordMysqlHelper.saveVocabRecord(vocab, strVocabularyId);
//        } catch (SQLException e) {
//            logger.error("error while saving VocabRecord with id " + vocab.getId(), e);
//            throw new DAOException(e);
//        }
//    }
//
//    public static void deleteVocabRecord(VocabRecord vocab) throws DAOException {
//        try {
//            VocabRecordMysqlHelper.deleteVocabRecord(vocab);
//        } catch (SQLException e) {
//            logger.error("error while deleting VocabRecord with id " + vocab.getId(), e);
//            throw new DAOException(e);
//        }
//    }
//
//    public static List<VocabRecord> getVocabRecords(String order, String filter, Integer start, Integer count) throws DAOException {
//        List<VocabRecord> answer = new ArrayList<>();
//        try {
//            answer = VocabRecordMysqlHelper.getVocabRecords(order, filter, start, count);
//        } catch (SQLException e) {
//            logger.error("error while getting VocabRecords", e);
//            throw new DAOException(e);
//        }
//        return answer;
//    }
//
//    public static List<VocabRecord> getAllVocabRecords() {
//        List<VocabRecord> answer = new ArrayList<>();
//        try {
//            answer = VocabRecordMysqlHelper.getAllVocabRecords();
//        } catch (SQLException e) {
//            logger.error("error while getting rulesets", e);
//        }
//        return answer;
//    }
//
//    @Override
//    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count) throws DAOException {
//        return getVocabRecords(order, filter, start, count);
//    }
//
//    @Override
//    public int getHitSize(String order, String filter) throws DAOException {
//        int num = 0;
//        try {
//            num = VocabRecordMysqlHelper.getVocabRecordCount(order, filter);
//        } catch (SQLException e) {
//            logger.error("error while getting ruleset hit size", e);
//            throw new DAOException(e);
//        }
//        return num;
//    }
//
//    /* +++++++++++++++++++++++++++++++++++++++++ Converter +++++++++++++++++++++++++++++++++++++++++++++++ */
//
//    public static VocabRecord convert(ResultSet rs) throws SQLException {
//        
////        String strVocabularyId = rs.getString("VocabId");
//        String strJson = rs.getString("jsonRecord");
//        
//        Gson gson = new Gson();
//        VocabRecord record = gson.fromJson(strJson, VocabRecord.class);
//        
//        return record;
//        
////        VocabularyBean vb = new VocabularyBean();
////        vb.getVm().loadVocabulary(strVocabularyId);
////        
////        List <Field> fields = new ArrayList<>();
////        for (Definition d : vb.getVm().getDefinitions()) {
////            fields.add(new Field(d.getLabel(), "",d));
////        } 
////
////        VocabRecord  record = new VocabRecord(rs.getString("RecordId"), fields);
////                
////        VocabRecord r = new VocabRecord();
////        r.setId(rs.getInt("MetadatenKonfigurationID"));
////        r.setTitel(rs.getString("Titel"));
////        r.setDatei(rs.getString("Datei"));
////        r.setOrderMetadataByVocabRecord(rs.getBoolean("orderMetadataByVocabRecord"));
////        return r;
//    }
//
//    public static ResultSetHandler<VocabRecord> resultSetToVocabRecordHandler = new ResultSetHandler<VocabRecord>() {
//        @Override
//        public VocabRecord handle(ResultSet rs) throws SQLException {
//            try {
//                if (rs.next()) {
//                    return convert(rs);
//                }
//            } finally {
//                if (rs != null) {
//                    rs.close();
//                }
//            }
//            return null;
//        }
//
//    };
//
//    public static ResultSetHandler<List<VocabRecord>> resultSetToVocabRecordListHandler = new ResultSetHandler<List<VocabRecord>>() {
//        @Override
//        public List<VocabRecord> handle(ResultSet rs) throws SQLException {
//            List<VocabRecord> answer = new ArrayList<>();
//            try {
//                while (rs.next()) {
//                    VocabRecord o = convert(rs);
//                    if (o != null) {
//                        answer.add(o);
//                    }
//                }
//            } finally {
//                if (rs != null) {
//                    rs.close();
//                }
//            }
//            return answer;
//        }
//    };
//
//    @Override
//    public List<Integer> getIdList(String order, String filter) {
//        return null;
//    }
//
//    public static VocabRecord getVocabRecordByName(String rulesetName) throws DAOException {
//        VocabRecord o = null;
//        try {
//            o = VocabRecordMysqlHelper.getVocabRecordByName(rulesetName);
//        } catch (SQLException e) {
//            logger.error("error while getting ruleset with name " + rulesetName, e);
//            throw new DAOException(e);
//        }
//        return o;
//    }
//
//}
