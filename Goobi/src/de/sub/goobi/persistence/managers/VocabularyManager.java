package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
                    lstDefs.add(new Definition(strLabel, strLanguage, strType, strValidation, bRequired, bMainEntry, bUnique, selecteableValues));

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

    public static ResultSetHandler<VocabRecord> resultSetToVocabularyRecordHandler = new ResultSetHandler<VocabRecord>() {

        @Override
        public VocabRecord handle(ResultSet rs) throws SQLException {
            if (rs.next()) {
                return convertRecord(rs);
            }
            return null;
        }

    };

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
            VocabularyMysqlHelper.saveVocabulary(vocabulary, gson);
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

    public static void loadRecordsForVocabulary(Vocabulary vocabulary) {
        try {
            VocabularyMysqlHelper.loadRecordsForVocabulary(vocabulary);
        } catch (SQLException e) {
            log.error(e);
        }

    }

    public static void deleteRecord(VocabRecord record) {
        try {
            VocabularyMysqlHelper.deleteRecord(record);
        } catch (SQLException e) {
            log.error(e);
        }

    }

    public static void saveRecords(Vocabulary vocabulary) {
        try {
            VocabularyMysqlHelper.saveRecords(vocabulary, gson);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static List<VocabRecord> findRecords(String vocabularyName, String searchValue, String... fieldNames) {
        try {
            return VocabularyMysqlHelper.findRecords(vocabularyName, searchValue, fieldNames);
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

    public static List<VocabRecord> findRecords(String vocabularyName, List<StringPair> data) {
        try {
            return VocabularyMysqlHelper.findRecords(vocabularyName, data);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }
}
