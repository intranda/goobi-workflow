package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang.StringEscapeUtils;
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

    public static Vocabulary getVocabularyByTitle(String title) throws SQLException {
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

    public static Vocabulary getVocabularyById(Integer id) throws SQLException {
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

    public static List<Vocabulary> getVocabularies(String order, String filter, Integer start, Integer count) throws SQLException {
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

    public static int getVocabularyCount(String order, String filter) throws SQLException {
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

    public static boolean isTitleUnique(Vocabulary vocabulary) throws SQLException {
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

    public static void saveVocabulary(Vocabulary vocabulary, Gson gson) throws SQLException {
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

    public static void deleteVocabulary(Vocabulary vocabulary) throws SQLException {
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

    public static void loadRecordsForVocabulary(Vocabulary vocabulary) throws SQLException {
        String sql = "SELECT * FROM vocabularyRecords WHERE vocabId = ?";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            List<VocabRecord> records =
                    new QueryRunner().query(connection, sql.toString(), VocabularyManager.resultSetToVocabularyRecordListHandler, vocabulary.getId());
            for (VocabRecord rec : records) {
                // merge expected definitions with existing definitions
                for (Definition definition : vocabulary.getStruct()) {
                    boolean fieldFound = false;
                    for (Field f : rec.getFields()) {
                        if (f.getLabel().equals(definition.getLabel())) {
                            f.setDefinition(definition);
                            fieldFound = true;
                            continue;
                        }
                    }
                    if (!fieldFound) {
                        Field field = new Field(definition.getLabel(), "", definition);
                        rec.getFields().add(field);
                    }
                }
            }
            vocabulary.setRecords(records);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteRecord(VocabRecord record) throws SQLException {
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

    public static void saveRecords(Vocabulary vocabulary, Gson gson) throws SQLException {
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

    public static List<VocabRecord> findRecords(String vocabularyName, String searchValue, String... fieldNames) throws SQLException {
        //select * from vocabularyRecords where attr like '%"value":"%Titel%"%';
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
                sb.append("attr like '\"label\":\"" + fieldName + "\",\"value\":\"%" + searchValue + "%\"%' ");

            }
            subQuery.append(")");
            sb.append(subQuery.toString());
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            List<VocabRecord> records =
                    new QueryRunner().query(connection, sb.toString(), VocabularyManager.resultSetToVocabularyRecordListHandler, vocabularyName);
            return records;

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static VocabRecord getRecord(Integer vocabularyId, Integer recordId) throws SQLException {
        if (vocabularyId == null || recordId == null) {
            return null;
        }
        String sql = "SELECT * FROM vocabularyRecords WHERE vocabId = ? AND recordID = ?";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();

            return new QueryRunner().query(connection, sql,VocabularyManager.resultSetToVocabularyRecordHandler,vocabularyId ,recordId);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }
}
