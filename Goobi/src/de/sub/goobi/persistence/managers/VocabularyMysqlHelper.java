package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.vocabulary.Definition;
import org.goobi.vocabulary.Field;
import org.goobi.vocabulary.VocabRecord;
import org.goobi.vocabulary.Vocabulary;

import com.google.gson.Gson;

class VocabularyMysqlHelper implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5141386688477409583L;

    static Vocabulary getVocabularyByTitle(String title) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM vocabularies v WHERE v.title = ?");
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();

            Vocabulary ret = new QueryRunner().query(connection, sql.toString(), VocabularyManager.resultSetToVocabularyHandler, title);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static Vocabulary getVocabularyById(Integer id) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM vocabularies v WHERE v.vocabId = ?");
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();

            Vocabulary ret = new QueryRunner().query(connection, sql.toString(), VocabularyManager.resultSetToVocabularyHandler, id);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static List<Vocabulary> getVocabularies(String order, String filter, Integer start, Integer count) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM vocabularies");
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

            List<Vocabulary> ret = new QueryRunner().query(connection, sql.toString(), VocabularyManager.resultSetToVocabularyListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static int getVocabularyCount(String order, String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) FROM vocabularies");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
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

    static boolean isTitleUnique(Vocabulary vocabulary) throws SQLException {
        String sql;
        if (vocabulary.getId() == null) {
            // new vocabulary, check against all vocabularies
            sql = "SELECT count(1) FROM vocabularies WHERE title = ?";
        } else {
            // existing vocabulary, exclude current vocabulary from check
            sql = "SELECT count(1) FROM vocabularies WHERE title = ? and vocabId !=" + vocabulary.getId();
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            int numberOfProcessesWithTitle =
                    new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, vocabulary.getTitle());
            return (numberOfProcessesWithTitle == 0);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static void saveVocabulary(Vocabulary vocabulary, Gson gson) throws SQLException {
        StringBuilder sql = new StringBuilder();

        if (vocabulary.getId() == null) {
            sql.append("INSERT INTO vocabularies(title, description, structure) ");
            sql.append("VALUES (?,?,?)");
        } else {
            sql.append("UPDATE vocabularies ");
            sql.append("SET title =  ?, description = ?, structure  = ? ");
            sql.append("WHERE vocabId = " + vocabulary.getId());
        }
        Connection connection = null;

        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            if (vocabulary.getId() == null) {
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, vocabulary.getTitle(),
                        vocabulary.getDescription(), gson.toJson(vocabulary.getStruct()));
                vocabulary.setId(id);
            } else {
                run.update(connection, sql.toString(), vocabulary.getTitle(), vocabulary.getDescription(), gson.toJson(vocabulary.getStruct()));
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    static void deleteVocabulary(Vocabulary vocabulary) throws SQLException {
        if (vocabulary.getId() != null) {
            String sql = "DELETE from vocabularies WHERE vocabId = ?";
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                run.update(connection, sql, vocabulary.getId());
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    static void loadRecordsForVocabulary(Vocabulary vocabulary) throws SQLException {
        String sql = "SELECT * FROM vocabularyRecords WHERE vocabId = ?";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            List<VocabRecord> records =
                    new QueryRunner().query(connection, sql.toString(), VocabularyManager.resultSetToVocabularyRecordListHandler, vocabulary.getId());
            for (VocabRecord rec : records) {
                // merge expected definitions with existing definitions
                mergeRecordAndVocabulary(vocabulary, rec);
            }
            vocabulary.setRecords(records);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static void mergeRecordAndVocabulary(Vocabulary vocabulary, VocabRecord rec) {
        for (Definition definition : vocabulary.getStruct()) {
            boolean fieldFound = false;
            for (Field f : rec.getFields()) {
                if (f.getLabel().equals(definition.getLabel())
                        && ((StringUtils.isBlank(f.getLanguage()) && StringUtils.isBlank(definition.getLanguage()))
                                || definition.getLanguage().equals(f.getLanguage()))) {
                    f.setDefinition(definition);
                    fieldFound = true;
                    break;
                }
            }
            if (!fieldFound) {
                Field field = new Field(definition.getLabel(),definition.getLanguage(), "", definition);
                rec.getFields().add(field);
            }
        }
        // check if field definition was deleted
        // if this is the case, remove the field as well
        List<Field> fieldsToDelete = new ArrayList<>();
        for (Field f : rec.getFields()) {
            if (f.getDefinition() == null) {
                fieldsToDelete.add(f);
            }
        }
        if (!fieldsToDelete.isEmpty()) {
            for (Field f : fieldsToDelete) {
                rec.getFields().remove(f);
            }
        }
    }

    static void deleteRecord(VocabRecord record) throws SQLException {
        if (record.getId() != null) {
            String sql = "DELETE from vocabularyRecords WHERE recordId = ?";
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                run.update(connection, sql, record.getId());
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    static void saveRecords(Vocabulary vocabulary, Gson gson) throws SQLException {
        String ingestSql = "INSERT INTO vocabularyRecords (vocabId, attr) VALUES (?,?)";
        String updateSql = "UPDATE vocabularyRecords set  attr = ? WHERE recordId = ?";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            for (VocabRecord record : vocabulary.getRecords()) {
                String strAttr = gson.toJson(record.getFields());

                if (record.getId() == null) {
                    Integer id = run.insert(connection, ingestSql, MySQLHelper.resultSetToIntegerHandler, vocabulary.getId(), strAttr);
                    record.setId(id);
                } else {
                    run.update(connection, updateSql, strAttr, record.getId());
                }
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static List<VocabRecord> findRecords(String vocabularyName, String searchValue, String... fieldNames) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT vocabularyRecords.* FROM vocabularyRecords LEFT JOIN vocabularies ON vocabularyRecords.vocabId=vocabularies.vocabId ");
        sb.append("WHERE vocabularies.title = ? AND ");
        searchValue = StringEscapeUtils.escapeSql(searchValue);
        if (fieldNames == null || fieldNames.length == 0) {
            sb.append("attr like '%\"value\":\"%" + searchValue + "%\"%'");
        } else {
            StringBuilder subQuery = new StringBuilder();
            for (String fieldName : fieldNames) {
                if (subQuery.length() == 0) {
                    subQuery.append("(");
                } else {
                    subQuery.append(" OR ");
                }
                subQuery.append("attr like '\"label\":\"" + fieldName + "\",\"value\":\"%" + searchValue + "%\"%' ");

            }
            subQuery.append(")");
            sb.append(subQuery.toString());
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            List<VocabRecord> records =
                    new QueryRunner().query(connection, sb.toString(), VocabularyManager.resultSetToVocabularyRecordListHandler, vocabularyName);

            Vocabulary vocab = getVocabularyByTitle(vocabularyName);
            for (VocabRecord rec : records) {
                // merge expected definitions with existing definitions
                mergeRecordAndVocabulary(vocab, rec);
            }

            return records;

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static VocabRecord getRecord(Integer vocabularyId, Integer recordId) throws SQLException {
        if (vocabularyId == null || recordId == null) {
            return null;
        }
        String sql = "SELECT * FROM vocabularyRecords WHERE vocabId = ? AND recordID = ?";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();

            VocabRecord record = new QueryRunner().query(connection, sql, VocabularyManager.resultSetToVocabularyRecordHandler, vocabularyId, recordId);

            Vocabulary vocabulary = getVocabularyById(vocabularyId);
            mergeRecordAndVocabulary(vocabulary, record);
            return record;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static List<VocabRecord> findRecords(String vocabularyName, List<StringPair> data) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT vocabularyRecords.* FROM vocabularyRecords LEFT JOIN vocabularies ON vocabularyRecords.vocabId=vocabularies.vocabId ");
        sb.append("WHERE vocabularies.title = ? AND ");
        StringBuilder subQuery = new StringBuilder();
        for (StringPair sp : data) {
            if (StringUtils.isNotBlank(sp.getTwo())) {
                if (subQuery.length() == 0) {
                    subQuery.append("(");
                } else {
                    subQuery.append(" OR ");
                }
                subQuery.append("attr like '%label\":\"" + StringEscapeUtils.escapeSql(sp.getOne()) + "\"%value\":\"%"
                        + StringEscapeUtils.escapeSql(sp.getTwo()) + "%' ");
            }
        }

        subQuery.append(")");
        sb.append(subQuery.toString());

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            List<VocabRecord> records =
                    new QueryRunner().query(connection, sb.toString(), VocabularyManager.resultSetToVocabularyRecordListHandler, vocabularyName);
            Vocabulary vocab = getVocabularyByTitle(vocabularyName);
            for (VocabRecord rec : records) {
                // merge expected definitions with existing definitions
                mergeRecordAndVocabulary(vocab, rec);
            }
            return records;

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }
}
