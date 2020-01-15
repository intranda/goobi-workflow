package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.beans.DatabaseObject;
import org.goobi.vocabulary.Definition;
import org.goobi.vocabulary.VocabRecord;
import org.goobi.vocabulary.Vocabulary;

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

    @Override
    public int getHitSize(String order, String filter) throws DAOException {
        try {
            return VocabularyMysqlHelper.getVocabularyCount(order, filter);
        } catch (SQLException e) {
            log.error(e);
        }
        return 0;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count) throws DAOException {
        try {
            return VocabularyMysqlHelper.getVocabularies(order, filter, start, count);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    @Override
    public List<Integer> getIdList(String order, String filter) {
        return null;
    }

    public Vocabulary getVocabularyByTitle(String title) {

        try {
            return VocabularyMysqlHelper.getVocabularyByTitle(title);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
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
                String strType = jsonObj.get("type").getAsString();
                String strValidation = jsonObj.get("validation").getAsString();
                String strSelect = jsonObj.get("select").getAsString();

                if (strLabel != null) {
                    lstDefs.add(new Definition(strLabel, strType, strValidation, strSelect));
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
}
