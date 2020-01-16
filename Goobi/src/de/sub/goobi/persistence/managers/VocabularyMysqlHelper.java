package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.goobi.vocabulary.Vocabulary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
            int numberOfProcessesWithTitle = new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, vocabulary.getTitle());
            return (numberOfProcessesWithTitle == 0);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveVocabulary(Vocabulary vocabulary) throws SQLException {
        StringBuilder sql = new StringBuilder();
        Gson gson = new GsonBuilder().create();
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
}
