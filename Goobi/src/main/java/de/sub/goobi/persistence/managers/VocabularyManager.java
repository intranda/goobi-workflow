package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
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
import lombok.extern.log4j.Log4j;

@Log4j
public class VocabularyManager implements IManager, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3577063138324090483L;

    @SuppressWarnings("deprecation")
    private static JsonParser jsonParser = new JsonParser();;

    private static Gson gson = new GsonBuilder().create();

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        try {
            return VocabularyMysqlHelper.getVocabularyCount(order, filter);
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
            return VocabularyMysqlHelper.getVocabularyCount(order, filter);
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

    @Deprecated
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
    @Deprecated
    public static ResultSetHandler<Vocabulary> resultSetToVocabularyHandler = new ResultSetHandler<Vocabulary>() {

        @Override
        public Vocabulary handle(ResultSet rs) throws SQLException {
            if (rs.next()) {
                Vocabulary vocabulary = convert(rs);
                return vocabulary;
            }
            return null;
        }
    };

    @SuppressWarnings("unchecked")
    @Deprecated
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
        vocab.setRecords(new ArrayList<VocabRecord>());
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
    @Deprecated
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
    @Deprecated
    public static ResultSetHandler<VocabRecord> resultSetToVocabularyRecordHandler = new ResultSetHandler<VocabRecord>() {

        @Override
        public VocabRecord handle(ResultSet rs) throws SQLException {
            if (rs.next()) {
                return convertRecord(rs);
            }
            return null;
        }

    };

    @Deprecated
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
                VocabRecord rec = new VocabRecord(iRecordId, iVocabId, lstFields);
                return rec;
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

    @Deprecated
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

    public static void deleteRecord(VocabRecord record) {
        try {
            VocabularyMysqlHelper.deleteRecord(record);
            setVocabularyLastAltered(record.getVocabularyId());
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

    public static void saveRecord(Integer vocabularyId, VocabRecord record) {
        try {
            VocabularyMysqlHelper.saveRecord(vocabularyId, record);
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

    @Deprecated
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
