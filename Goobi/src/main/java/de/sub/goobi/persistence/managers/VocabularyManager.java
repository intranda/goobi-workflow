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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.vocabulary.Definition;
import org.goobi.vocabulary.Field;
import org.goobi.vocabulary.VocabRecord;
import org.goobi.vocabulary.Vocabulary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.sub.goobi.helper.exceptions.DAOException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class VocabularyManager implements IManager, Serializable {

    private static final long serialVersionUID = 3577063138324090483L;

    @SuppressWarnings("deprecation")
    private static JsonParser jsonParser = new JsonParser();

    private static Gson gson = new GsonBuilder().create();

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        try {
            return VocabularyMysqlHelper.getVocabularyCount(filter);
        } catch (SQLException e) {
            log.error(e);
        }
        return 0;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution)
            throws DAOException {
        try {
            return VocabularyMysqlHelper.getVocabularies(order, filter, start, count);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    public int getHitSize(String order, String filter) throws DAOException {
        try {
            return VocabularyMysqlHelper.getVocabularyCount(filter);
        } catch (SQLException e) {
            log.error(e);
        }
        return 0;
    }

    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count) throws DAOException {
        try {
            return VocabularyMysqlHelper.getVocabularies(order, filter, start, count);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        return null;
    }

    public static Vocabulary getVocabularyByTitle(String title) {
        try {
            return VocabularyMysqlHelper.getVocabularyByTitle(title);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    public static Vocabulary getVocabularyById(Integer id) {
        try {
            return VocabularyMysqlHelper.getVocabularyById(id);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    public static boolean isTitleUnique(Vocabulary vocabulary) {
        try {
            return VocabularyMysqlHelper.isTitleUnique(vocabulary);
        } catch (SQLException e) {
            log.error(e);
        }
        return false;
    }

    /**
     * @deprecated This handler is not used anymore
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public static ResultSetHandler<List<Vocabulary>> resultSetToVocabularyListHandler = new ResultSetHandler<List<Vocabulary>>() {

        @Override
        public List<Vocabulary> handle(ResultSet rs) throws SQLException {
            List<Vocabulary> answer = new ArrayList<>();
            while (rs.next()) {
                Vocabulary vocabulary = convert(rs);
                answer.add(vocabulary);
            }
            return answer;
        }
    };

    /**
     * @deprecated This handler is not used anymore
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public static ResultSetHandler<Vocabulary> resultSetToVocabularyHandler = new ResultSetHandler<Vocabulary>() {

        @Override
        public Vocabulary handle(ResultSet rs) throws SQLException {
            if (rs.next()) {
                return convert(rs);
            }
            return null;
        }
    };

    /**
     * @deprecated This method is not used anymore
     *
     * @param rs
     * @return
     */
    @Deprecated(since = "23.05", forRemoval = false)
    @SuppressWarnings("unchecked")
    private static Vocabulary convert(ResultSet rs) throws SQLException {
        int vocabId = rs.getInt("vocabId");
        String strVocabTitle = rs.getString("title");
        String strDescription = rs.getString("description");
        String jsonStruct = rs.getString("structure");
        ArrayList<Definition> lstDefs = new ArrayList<>();

        JsonArray struct = jsonParser.parse(jsonStruct).getAsJsonArray();

        if (struct != null) {
            for (JsonElement jsonElt : struct) {

                JsonObject jsonObj = jsonElt.getAsJsonObject();

                String strLabel = jsonObj.get("label").getAsString();
                String strLanguage = jsonObj.get("language").getAsString();
                String strType = jsonObj.get("type").getAsString();
                String strValidation = jsonObj.get("validation").getAsString();

                boolean bRequired = jsonObj.get("required").getAsBoolean();
                boolean bMainEntry = jsonObj.get("mainEntry").getAsBoolean();
                boolean bUnique = jsonObj.get("unique").getAsBoolean();

                ArrayList<String> selecteableValues = new ArrayList<>();
                JsonElement obj = jsonObj.get("selecteableValues");
                if (obj != null) {
                    JsonArray array = obj.getAsJsonArray();
                    if (array != null) {
                        selecteableValues = gson.fromJson(array, ArrayList.class);
                    }
                }
                if (strLabel != null) {
                    Definition def = new Definition(strLabel, strLanguage, strType, strValidation, bRequired, bMainEntry, bUnique, bMainEntry);
                    def.setSelecteableValues(selecteableValues);
                    lstDefs.add(def);

                }
            }
        }

        Vocabulary vocab = new Vocabulary();
        vocab.setId(vocabId);
        vocab.setTitle(strVocabTitle);
        vocab.setDescription(strDescription);
        vocab.setStruct(lstDefs);
        vocab.setRecords(new ArrayList<>());
        return vocab;
    }

    public static ResultSetHandler<List<VocabRecord>> vocabularyRecordListHandler = new ResultSetHandler<List<VocabRecord>>() {

        @Override
        public List<VocabRecord> handle(ResultSet rs) throws SQLException {

            Map<Integer, VocabRecord> recordMap = new HashMap<>();
            List<VocabRecord> records = new LinkedList<>();

            while (rs.next()) {
                Integer fieldId = rs.getInt("id");
                Integer recordId = rs.getInt("record_id");
                Integer vocabularyId = rs.getInt("vocabulary_id");
                Integer definitionId = rs.getInt("definition_id");
                String label = rs.getString("label");
                String language = rs.getString("language");
                String value = rs.getString("value");

                Field field = new Field();
                field.setId(fieldId);
                field.setLabel(label);
                field.setLanguage(language);
                field.setValue(value);
                field.setDefinitionId(definitionId);
                VocabRecord rec = null;
                if (recordMap.containsKey(recordId)) {
                    rec = recordMap.get(recordId);
                } else {
                    rec = new VocabRecord();
                    rec.setId(recordId);
                    rec.setVocabularyId(vocabularyId);
                    recordMap.put(recordId, rec);
                    records.add(rec);
                }
                rec.getFields().add(field);
            }

            return records;
        }

    };

    public final static ResultSetHandler<Map<String, VocabRecord>> vocabularyRecordMapHandler = new ResultSetHandler<Map<String, VocabRecord>>() {

        @Override
        public Map<String, VocabRecord> handle(ResultSet rs) throws SQLException {

            Map<Integer, VocabRecord> recordMap = new HashMap<>();
            Map<String, VocabRecord> records = new HashMap<>();

            while (rs.next()) {
                Integer fieldId = rs.getInt("id");
                Integer recordId = rs.getInt("record_id");
                Integer vocabularyId = rs.getInt("vocabulary_id");
                Integer definitionId = rs.getInt("definition_id");
                String label = rs.getString("label");
                String language = rs.getString("language");
                String value = rs.getString("value");

                String idField = rs.getString("identifier");

                Field field = new Field();
                field.setId(fieldId);
                field.setLabel(label);
                field.setLanguage(language);
                field.setValue(value);
                field.setDefinitionId(definitionId);
                VocabRecord rec = null;
                if (recordMap.containsKey(recordId)) {
                    rec = recordMap.get(recordId);
                } else {
                    rec = new VocabRecord();
                    rec.setId(recordId);
                    rec.setVocabularyId(vocabularyId);
                    recordMap.put(recordId, rec);
                    records.put(idField, rec);
                }
                rec.getFields().add(field);
            }

            return records;
        }

    };

    /**
     * @deprecated This handler is not used anymore
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public static ResultSetHandler<List<VocabRecord>> resultSetToVocabularyRecordListHandler = new ResultSetHandler<List<VocabRecord>>() {

        @Override
        public List<VocabRecord> handle(ResultSet rs) throws SQLException {
            List<VocabRecord> records = new LinkedList<>();
            while (rs.next()) {
                VocabRecord rec = convertRecord(rs);
                if (rec != null) {
                    records.add(rec);
                }
            }
            return records;
        }

    };

    /**
     * @deprecated This handler is not used anymore
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public static ResultSetHandler<VocabRecord> resultSetToVocabularyRecordHandler = new ResultSetHandler<VocabRecord>() {

        @Override
        public VocabRecord handle(ResultSet rs) throws SQLException {
            if (rs.next()) {
                return convertRecord(rs);
            }
            return null;
        }

    };

    /**
     * @deprecated This method is not used anymore
     *
     * @param rs
     * @return
     */
    @Deprecated(since = "23.05", forRemoval = false)
    private static VocabRecord convertRecord(ResultSet rs) throws SQLException {

        int iRecordId = rs.getInt("recordId");
        List<Field> lstFields = new ArrayList<>();

        int iVocabId = rs.getInt("vocabId");

        JsonElement eltAttr = null;
        if (rs.getString("attr") != null) {
            eltAttr = jsonParser.parse(rs.getString("attr"));
        }

        if (eltAttr != null) {
            try {
                JsonArray attr = eltAttr.getAsJsonArray();
                if (attr != null) {
                    for (JsonElement jsonElt : attr) {

                        JsonObject jsonField = jsonElt.getAsJsonObject();
                        lstFields.add(gson.fromJson(jsonField, Field.class));
                    }
                }
                return new VocabRecord(iRecordId, iVocabId, lstFields);
            } catch (Exception e) {
                log.error(e);
            }
        }
        return null;
    }

    public static void saveVocabulary(Vocabulary vocabulary) {
        try {
            VocabularyMysqlHelper.saveVocabulary(vocabulary);
            setVocabularyLastAltered(vocabulary);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void deleteVocabulary(Vocabulary vocabulary) {
        try {
            VocabularyMysqlHelper.deleteVocabulary(vocabulary);
        } catch (SQLException e) {
            log.error(e);
        }

    }

    /**
     * @deprecated This method is replaced by getAllRecords(Vocabulary)
     *
     * @param vocabulary
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public static void loadRecordsForVocabulary(Vocabulary vocabulary) {
        try {
            VocabularyMysqlHelper.loadRecordsForVocabulary(vocabulary);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void getAllRecords(Vocabulary vocabulary) {
        try {
            VocabularyMysqlHelper.getAllRecords(vocabulary);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void deleteRecord(VocabRecord vocabRecord) {
        try {
            VocabularyMysqlHelper.deleteRecord(vocabRecord);
            setVocabularyLastAltered(vocabRecord.getVocabularyId());
        } catch (SQLException e) {
            log.error(e);
        }

    }

    public static void deleteAllRecords(Vocabulary vocabulary) {
        try {
            VocabularyMysqlHelper.deleteAllRecords(vocabulary);
            setVocabularyLastAltered(vocabulary);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void saveRecords(Vocabulary vocabulary) {
        try {
            VocabularyMysqlHelper.saveRecords(vocabulary);
            setVocabularyLastAltered(vocabulary);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void saveRecord(Integer vocabularyId, VocabRecord vocabRecord) {
        try {
            VocabularyMysqlHelper.saveRecord(vocabularyId, vocabRecord);
            setVocabularyLastAltered(vocabularyId);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    /**
     * Find the vocabulary records which contain a given string in given fields. This search does not search for exact string match. It does a
     * 'contains'-search
     * 
     * @param vocabularyName the vocabulary to search for
     * @param searchValue the value to be searched as term that must be contained within the defined field
     * @param fieldNames the list of fields to search in
     * @return a list of vocabulary records
     * 
     * @throws SQLException
     */
    public static List<VocabRecord> findRecords(String vocabularyName, String searchValue, String... fieldNames) {
        try {
            return VocabularyMysqlHelper.findRecords(vocabularyName, searchValue, false, fieldNames);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    /**
     * Find the vocabulary records which match exactly the given string in the defined fields. This search does search for exact string match.
     * 
     * @param vocabularyName the vocabulary to search for
     * @param searchValue the value to be searched as term that must be identical in the defined field
     * @param fieldNames the list of fields to search in
     * @return a list of vocabulary records
     * 
     * @throws SQLException
     */
    public static List<VocabRecord> findExactRecords(String vocabularyName, String searchValue, String... fieldNames) {
        try {
            return VocabularyMysqlHelper.findRecords(vocabularyName, searchValue, true, fieldNames);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    public static VocabRecord getRecord(Integer vocabularyId, Integer recordId) {
        try {
            return VocabularyMysqlHelper.getRecord(vocabularyId, recordId);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    /**
     * Search in the vocabulary for String Pairs which contain the searched terms. This search does not contain exact matches as it does a
     * contains-search
     * 
     * @param vocabularyName the vocabulary to search within
     * @param data the StringPair to use for searching
     * @return Vocabulary records
     * 
     * @throws SQLException
     */
    public static List<VocabRecord> findRecords(String vocabularyName, List<StringPair> data) {
        try {
            return VocabularyMysqlHelper.findRecords(vocabularyName, data, false);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    /**
     * Search in the vocabulary for String Pairs which match exactly the searched terms. This search lists only exact matches.
     * 
     * @param vocabularyName the vocabulary to search within
     * @param data the StringPair to use for searching
     * @return Vocabulary records
     * 
     * @throws SQLException
     */
    public static List<VocabRecord> findExactRecords(String vocabularyName, List<StringPair> data) {
        try {
            return VocabularyMysqlHelper.findRecords(vocabularyName, data, true);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    public static Map<String, VocabRecord> getRecordMap(int vacabularyId, int definitionId) {
        try {
            return VocabularyMysqlHelper.getRecordMap(vacabularyId, definitionId);
        } catch (SQLException e) {
            log.error(e);
        }
        return Collections.emptyMap();
    }

    /**
     * @deprecated This method is not used anymore
     *
     * @param vocabulary
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public static void getPaginatedRecords(Vocabulary vocabulary) {
        try {
            VocabularyMysqlHelper.getRecords(vocabulary);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void saveDefinition(Integer vocabularyId, Definition definition) {
        try {
            VocabularyMysqlHelper.saveDefinition(vocabularyId, definition);
            setVocabularyLastAltered(vocabularyId);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void deleteDefinition(Definition definition) {
        try {
            VocabularyMysqlHelper.deleteDefinition(definition);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void insertNewRecords(List<VocabRecord> records, Integer vocabularyID) {
        try {
            VocabularyMysqlHelper.insertNewRecords(records, vocabularyID);
            setVocabularyLastAltered(vocabularyID);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void batchUpdateRecords(List<VocabRecord> records, Integer vocabularyID) {
        try {
            VocabularyMysqlHelper.batchUpdateRecords(records, vocabularyID);
            setVocabularyLastAltered(vocabularyID);
        } catch (SQLException e) {
            log.error(e);
        }

    }

    private static void setVocabularyLastAltered(int vocabularyId) {
        Vocabulary vocab = getVocabularyById(vocabularyId);
        setVocabularyLastAltered(vocab);
    }

    public static void setVocabularyLastAltered(Vocabulary vocabulary) {
        try {
            VocabularyMysqlHelper.setVocabularyLastAltered(vocabulary);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void setVocabularyLastUploaded(Vocabulary vocabulary) {
        try {
            VocabularyMysqlHelper.setVocabularyLastUploaded(vocabulary);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static Timestamp getVocabularyLastAltered(Vocabulary vocabulary) {
        try {
            return VocabularyMysqlHelper.getVocabularyLastAltered(vocabulary);
        } catch (SQLException e) {
            log.error(e);
        }

        return null;
    }

    public static Timestamp getVocabularyLastUploaded(Vocabulary vocabulary) {
        try {
            return VocabularyMysqlHelper.getVocabularyLastUploaded(vocabulary);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    public static ResultSetHandler<Timestamp> resultSetGetLastUploadedHandler = new ResultSetHandler<Timestamp>() {

        @Override
        public Timestamp handle(ResultSet rs) throws SQLException {
            if (rs.next()) {
                return rs.getTimestamp("lastUploaded");
            }
            return null;
        }

    };

    public static ResultSetHandler<Timestamp> resultSetGetLastAlteredHandler = new ResultSetHandler<Timestamp>() {

        @Override
        public Timestamp handle(ResultSet rs) throws SQLException {
            if (rs.next()) {
                return rs.getTimestamp("lastAltered");
            }
            return null;
        }

    };
}
