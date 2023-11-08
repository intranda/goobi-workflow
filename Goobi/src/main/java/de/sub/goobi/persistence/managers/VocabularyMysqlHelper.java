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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.StatementConfiguration;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.vocabulary.Definition;
import org.goobi.vocabulary.Field;
import org.goobi.vocabulary.VocabRecord;
import org.goobi.vocabulary.Vocabulary;

import com.google.gson.Gson;

import de.sub.goobi.persistence.managers.MySQLHelper.SQLTYPE;

/**
 * @author steffen
 *
 */
class VocabularyMysqlHelper implements Serializable {

    private static final long serialVersionUID = 5141386688477409583L;

    private static final Integer MAXIMAL_QUERY_RESULTS = 10_000;

    private static final Integer QUERY_TIMEOUT_IN_SECONDS = 20;

    private static String vocabTable = "vocabulary";

    static Vocabulary getVocabularyByTitle(String title) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM vocabulary v WHERE v.title = ?");
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Vocabulary ret = new QueryRunner().query(connection, sql.toString(), new BeanHandler<>(Vocabulary.class), title);
            if (ret != null) {
                ret.setStruct(getDefinitionsForVocabulary(ret.getId()));
            }
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static List<Definition> getDefinitionsForVocabulary(Integer vocabularyId) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM vocabulary_structure v WHERE v.vocabulary_id = ?");
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql.toString(), new BeanListHandler<>(Definition.class), vocabularyId);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static Vocabulary getVocabularyById(Integer id) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM vocabulary v WHERE v.id = ?");
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Vocabulary ret = new QueryRunner().query(connection, sql.toString(), new BeanHandler<>(Vocabulary.class), id);
            ret.setStruct(getDefinitionsForVocabulary(ret.getId()));
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
        sql.append("SELECT * FROM vocabulary");
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

            List<Vocabulary> ret = new QueryRunner().query(connection, sql.toString(), new BeanListHandler<>(Vocabulary.class));
            for (Vocabulary v : ret) {
                v.setStruct(getDefinitionsForVocabulary(v.getId()));
            }
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static int getVocabularyCount(String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) FROM vocabulary");
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
            sql = "SELECT count(1) FROM vocabulary WHERE title = ?";
        } else {
            // existing vocabulary, exclude current vocabulary from check
            sql = "SELECT count(1) FROM vocabulary WHERE title = ? and id !=" + vocabulary.getId();
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            int numberOfProcessesWithTitle = new QueryRunner().query(connection, sql, MySQLHelper.resultSetToIntegerHandler, vocabulary.getTitle());
            return (numberOfProcessesWithTitle == 0);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static void saveVocabulary(Vocabulary vocabulary) throws SQLException {
        StringBuilder sql = new StringBuilder();
        if (vocabulary.getId() == null) {
            sql.append("INSERT INTO vocabulary (title, description) ");
            sql.append("VALUES (?,?)");
        } else {
            sql.append("UPDATE vocabulary ");
            sql.append("SET title =  ?, description = ? ");
            sql.append("WHERE id = " + vocabulary.getId());
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            if (vocabulary.getId() == null) {
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, vocabulary.getTitle(),
                        vocabulary.getDescription());
                vocabulary.setId(id);
            } else {
                run.update(connection, sql.toString(), vocabulary.getTitle(), vocabulary.getDescription());
            }

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
        for (Definition definition : vocabulary.getStruct()) {
            saveDefinition(vocabulary.getId(), definition);
        }
    }

    static void saveDefinition(Integer vocabularyId, Definition definition) throws SQLException {
        StringBuilder sql = new StringBuilder();
        if (definition.getId() == null) {
            sql.append("INSERT INTO vocabulary_structure (vocabulary_id, label,language, type,validation,");
            sql.append("required ,mainEntry,distinctive,selection, titleField) ");
            sql.append("VALUES (?,?,?,?,?,?,?,?,?,?)");
        } else {
            sql.append("UPDATE vocabulary_structure ");
            sql.append("SET vocabulary_id =  ?, label = ?, ");
            sql.append("language =  ?, type = ?, ");
            sql.append("validation =  ?, required = ?, ");
            sql.append("mainEntry =  ?, distinctive = ?, selection = ?, titleField = ? ");
            sql.append("WHERE id = " + definition.getId());
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            if (definition.getId() == null) {
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, vocabularyId, definition.getLabel(),
                        definition.getLanguage(), definition.getType(), definition.getValidation(), definition.isRequired(), definition.isMainEntry(),
                        definition.isDistinctive(), definition.getSelection(), definition.isTitleField());
                definition.setId(id);
            } else {
                run.update(connection, sql.toString(), vocabularyId, definition.getLabel(), definition.getLanguage(), definition.getType(),
                        definition.getValidation(), definition.isRequired(), definition.isMainEntry(), definition.isDistinctive(),
                        definition.getSelection(), definition.isTitleField());
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static void deleteDefinition(Definition definition) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            run.update(connection, "DELETE FROM vocabulary_structure WHERE id = ?", definition.getId());
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    /**
     * @deprecated This method is not used anymore
     *
     * @param vocabulary
     * @param gson
     */
    @Deprecated(since = "23.05", forRemoval = true)
    static void saveVocabulary(Vocabulary vocabulary, Gson gson) throws SQLException {
        StringBuilder sql = new StringBuilder();

        if (vocabulary.getId() == null) {
            sql.append("INSERT INTO " + vocabTable + "(title, description, structure) ");
            sql.append("VALUES (?,?,?)");
        } else {
            sql.append("UPDATE " + vocabTable + " ");
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
            String deleteRecords = "DELETE FROM vocabulary_record WHERE vocabulary_id = ?";
            String deleteRecordData = "DELETE FROM vocabulary_record_data WHERE vocabulary_id = ?";
            String deleteVocabulary = "DELETE from vocabulary WHERE id = ?";
            String deleteDefinitions = "DELETE from vocabulary_structure WHERE vocabulary_id = ?";
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                run.update(connection, deleteRecordData, vocabulary.getId());
                run.update(connection, deleteRecords, vocabulary.getId());
                run.update(connection, deleteDefinitions, vocabulary.getId());
                run.update(connection, deleteVocabulary, vocabulary.getId());
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    static void getAllRecords(Vocabulary vocabulary) throws SQLException {
        if (vocabulary != null && vocabulary.getId() != null) {
            String sql = "SELECT * FROM vocabulary_record_data WHERE vocabulary_id = ?";
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                List<VocabRecord> records =
                        new QueryRunner().query(connection, sql, VocabularyManager.vocabularyRecordListHandler, vocabulary.getId());
                for (VocabRecord rec : records) {
                    setDefinitionsToRecord(rec, vocabulary);
                }
                vocabulary.setRecords(records);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    /**
     * @deprecated This method is not used anymore
     *
     * @param vocabulary
     */
    @Deprecated(since = "23.05", forRemoval = true)
    static void loadRecordsForVocabulary(Vocabulary vocabulary) throws SQLException {
        if (vocabulary != null && vocabulary.getId() != null) {
            String sql = "SELECT * FROM vocabularyRecords WHERE vocabId = ?";
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                List<VocabRecord> records =
                        new QueryRunner().query(connection, sql, VocabularyManager.resultSetToVocabularyRecordListHandler, vocabulary.getId());
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
    }

    /**
     * @deprecated This method is not used anymore
     *
     * @param vocabulary
     * @param rec
     */
    @Deprecated(since = "23.05", forRemoval = true)
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
                Field field = new Field(definition.getLabel(), definition.getLanguage(), "", definition);
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
        // set field order
        List<Field> orderedList = new ArrayList<>(rec.getFields().size());
        for (Definition definition : vocabulary.getStruct()) {
            for (Field f : rec.getFields()) {
                if (f.getDefinition().equals(definition)) {
                    orderedList.add(f);
                    break;
                }
            }
        }
        rec.setFields(orderedList);
    }

    static void deleteRecord(VocabRecord vocabRecord) throws SQLException {
        if (vocabRecord.getId() != null) {
            String deleteFields = "DELETE from vocabulary_record_data WHERE record_id = ?";
            String deleteRecord = "DELETE from vocabulary_record WHERE id = ?";
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                run.update(connection, deleteFields, vocabRecord.getId());
                run.update(connection, deleteRecord, vocabRecord.getId());
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    static void deleteAllRecords(Vocabulary vocabulary) throws SQLException {
        if (vocabulary.getId() != null) {
            String deleteFields = "DELETE from vocabulary_record_data WHERE vocabulary_id = ? ";
            String deleteRecords = "DELETE from vocabulary_record WHERE vocabulary_id = ? ";
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                run.update(connection, deleteFields, vocabulary.getId());
                run.update(connection, deleteRecords, vocabulary.getId());
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    static void saveRecords(Vocabulary vocabulary) throws SQLException {
        String insertRecord = "INSERT INTO vocabulary_record (vocabulary_id) VALUES (?)";
        String insertField =
                "INSERT INTO vocabulary_record_data (record_id,vocabulary_id, definition_id, label, language, value) VALUES (?,?,?,?,?,?)";
        String updateField =
                "UPDATE vocabulary_record_data set record_id=?,vocabulary_id=?, definition_id=?, label=?, language=?, value=? WHERE id = ?";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            for (VocabRecord vocabRecord : vocabulary.getRecords()) {
                if (vocabRecord.getId() == null) {
                    Integer id = run.insert(connection, insertRecord, MySQLHelper.resultSetToIntegerHandler, vocabulary.getId());
                    vocabRecord.setId(id);
                }
                for (Field field : vocabRecord.getFields()) {
                    if (field.getId() == null) {
                        Integer id = run.insert(connection, insertField, MySQLHelper.resultSetToIntegerHandler, vocabRecord.getId(),
                                vocabulary.getId(), field.getDefinition().getId(), field.getLabel(), field.getLanguage(), field.getValue());
                        field.setId(id);
                    } else {
                        run.update(connection, updateField, vocabRecord.getId(), vocabulary.getId(), field.getDefinition().getId(), field.getLabel(),
                                field.getLanguage(), field.getValue(), field.getId());
                    }
                }
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static void saveRecord(Integer vocabularyId, VocabRecord vocabRecord) throws SQLException {
        String insertRecord = "INSERT INTO vocabulary_record (vocabulary_id) VALUES (?)";
        String insertField =
                "INSERT INTO vocabulary_record_data (record_id,vocabulary_id, definition_id, label, language, value) VALUES (?,?,?,?,?,?)";
        String updateField =
                "UPDATE vocabulary_record_data set record_id=?,vocabulary_id=?, definition_id=?, label=?, language=?, value=? WHERE id = ?";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            if (vocabRecord.getId() == null) {
                Integer id = run.insert(connection, insertRecord, MySQLHelper.resultSetToIntegerHandler, vocabularyId);
                vocabRecord.setId(id);
            }
            for (Field field : vocabRecord.getFields()) {
                if (field.getId() == null) {
                    Integer id = run.insert(connection, insertField, MySQLHelper.resultSetToIntegerHandler, vocabRecord.getId(), vocabularyId,
                            field.getDefinition().getId(), field.getLabel(), field.getLanguage(), field.getValue());
                    field.setId(id);
                } else {
                    run.update(connection, updateField, vocabRecord.getId(), vocabularyId, field.getDefinition().getId(), field.getLabel(),
                            field.getLanguage(), field.getValue(), field.getId());
                }
            }

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static List<VocabRecord> findRecords(String vocabularyName, String searchValue, boolean exact, String... fieldNames) throws SQLException {
        String likeStr = "like";
        if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.H2) {
            likeStr = "ilike";
        }

        searchValue = StringEscapeUtils.escapeSql(searchValue.replace("\"", "_"));
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT distinct record_id FROM vocabulary_record_data r LEFT JOIN vocabulary v ON v.id = r.vocabulary_id WHERE v.title = ? ");
        sb.append("AND r.value ");
        sb.append(likeStr);
        sb.append(" '");
        if (!exact) {
            sb.append("%");
        }
        sb.append(searchValue);
        if (!exact) {
            sb.append("%");
        }
        sb.append("' ");
        if (fieldNames != null && fieldNames.length > 0) {
            sb.append(" AND (");
            StringBuilder subQuery = new StringBuilder();
            for (String fieldName : fieldNames) {
                if (subQuery.length() > 0) {
                    subQuery.append(" OR ");
                }
                subQuery.append("r.label ='" + StringEscapeUtils.escapeSql(fieldName) + "'");
            }
            sb.append(subQuery.toString());
            sb.append(")");

        }

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            List<Integer> idList =
                    runner.query(connection, sb.toString(), MySQLHelper.resultSetToIntegerListHandler, StringEscapeUtils.escapeSql(vocabularyName));

            if (idList.isEmpty()) {
                return Collections.emptyList();
            }
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM vocabulary_record_data r LEFT JOIN vocabulary v ON v.id = r.vocabulary_id WHERE r.record_id in (");
            StringBuilder sub = new StringBuilder();

            for (Integer id : idList) {
                if (sub.length() > 0) {
                    sub.append(", ");
                }
                sub.append(id);
            }

            query.append(sub.toString());
            query.append(")");
            List<VocabRecord> records = new QueryRunner().query(connection, query.toString(), VocabularyManager.vocabularyRecordListHandler);

            Vocabulary vocabulary = getVocabularyByTitle(vocabularyName);
            for (VocabRecord rec : records) {
                setDefinitionsToRecord(rec, vocabulary);
            }

            return records;

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static void setDefinitionsToRecord(VocabRecord rec, Vocabulary vocabulary) {
        for (Definition def : vocabulary.getStruct()) {
            boolean fieldFound = false;
            for (Field field : rec.getFields()) {
                if (def.getId().equals(field.getDefinitionId())) {
                    field.setDefinition(def);
                    fieldFound = true;
                    break;
                }
            }
            if (!fieldFound) {
                Field field = new Field();
                field.setDefinition(def);
                field.setLabel(def.getLabel());
                field.setLanguage(def.getLanguage());
                rec.getFields().add(field);
            }
        }
    }

    static VocabRecord getRecord(Integer vocabularyId, Integer recordId) throws SQLException {
        if (vocabularyId == null || recordId == null) {
            return null;
        }
        String sql = "SELECT * FROM vocabulary_record_data WHERE vocabulary_id = ? AND record_id = ?";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();

            List<VocabRecord> recordList =
                    new QueryRunner().query(connection, sql, VocabularyManager.vocabularyRecordListHandler, vocabularyId, recordId);
            VocabRecord rec = null;
            if (!recordList.isEmpty()) {
                Vocabulary vocabulary = getVocabularyById(vocabularyId);
                rec = recordList.get(0);
                setDefinitionsToRecord(rec, vocabulary);
            }
            return rec;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    /**
     * @deprecated This method is not used anymore
     *
     * @param vocabulary
     */
    @Deprecated(since = "23.05", forRemoval = true)
    static void getRecords(Vocabulary vocabulary) throws SQLException {
        String likeStr = "like";
        if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.H2) {
            likeStr = "ilike";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT vocabularyRecords.* FROM vocabularyRecords LEFT JOIN " + vocabTable + " ON vocabularyRecords.vocabId=" + vocabTable
                + ".vocabId ");
        sb.append("WHERE " + vocabTable + ".vocabId = ? ");
        StringBuilder subQuery = new StringBuilder();

        if (StringUtils.isNotBlank(vocabulary.getSearchField())) {
            if (subQuery.length() == 0) {
                subQuery.append(" AND ");
                subQuery.append("(");
            } else {
                subQuery.append(" OR ");
            }
            subQuery.append("attr ");
            subQuery.append(likeStr);
            subQuery.append(" '%label\":\"" + StringEscapeUtils.escapeSql(vocabulary.getMainFieldName()) + "\",\"language\":\"%\",\"value\":\"%"
                    + StringEscapeUtils.escapeSql(vocabulary.getSearchField().replace("\"", "_")) + "%' ");
        }

        if (subQuery.length() > 0) {
            subQuery.append(")");
            sb.append(subQuery.toString());
        }

        Connection connection = null;

        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();

            // total number of records

            int numberOfRecords = runner.query(connection, "SELECT COUNT(1) FROM (" + sb.toString() + ") a", MySQLHelper.resultSetToIntegerHandler,
                    vocabulary.getId());
            vocabulary.setTotalNumberOfRecords(numberOfRecords);

            // order
            if (MySQLHelper.isJsonCapable()) {
                String sqlPathToField = "SELECT REPLACE(JSON_SEARCH(attr, 'one', '" + vocabulary.getMainFieldName()
                + "'), 'label','value') from vocabularyRecords WHERE vocabId= ? limit 1";
                String field = runner.query(connection, sqlPathToField, MySQLHelper.resultSetToStringHandler, vocabulary.getId());
                sb.append(" ORDER BY " + "JSON_EXTRACT(attr, " + field + ") ");
                if (StringUtils.isNotBlank(vocabulary.getOrder())) {
                    sb.append(vocabulary.getOrder());
                }
            }
            // limit
            sb.append(" LIMIT " + (vocabulary.getPageNo() * vocabulary.getNumberOfRecordsPerPage()) + ", " + vocabulary.getNumberOfRecordsPerPage());

            List<VocabRecord> records =
                    runner.query(connection, sb.toString(), VocabularyManager.resultSetToVocabularyRecordListHandler, vocabulary.getId());
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

    static List<VocabRecord> findRecords(String vocabularyName, List<StringPair> data, boolean exactSearch) throws SQLException {
        String likeStr = "like";
        if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.H2) {
            likeStr = "ilike";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT r.* FROM vocabulary_record_data r LEFT JOIN vocabulary v ON v.id = r.vocabulary_id WHERE v.title = ? ");

        StringBuilder subQuery = new StringBuilder();
        for (StringPair sp : data) {
            if (StringUtils.isNotBlank(sp.getTwo())) {
                if (subQuery.length() > 0) {
                    subQuery.append(" OR ");
                }
                subQuery.append("(value ");
                subQuery.append(likeStr);
                subQuery.append(" '");
                if (!exactSearch) {
                    subQuery.append("%");
                }
                subQuery.append(StringEscapeUtils.escapeSql(sp.getTwo().replace("\"", "_")));
                if (!exactSearch) {
                    subQuery.append("%");
                }
                subQuery.append("' AND ");
                subQuery.append("label ='" + StringEscapeUtils.escapeSql(sp.getOne()) + "') ");
            }
        }
        if (subQuery.length() > 0) {
            sb.append(" AND r.record_id in (select distinct record_id from vocabulary_record_data where ");
            sb.append(subQuery.toString());
            sb.append(")");
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            StatementConfiguration stmtConfig =
                    new StatementConfiguration.Builder().maxRows(MAXIMAL_QUERY_RESULTS).queryTimeout(QUERY_TIMEOUT_IN_SECONDS).build();
            QueryRunner queryRunner = new QueryRunner(stmtConfig);
            List<VocabRecord> records = queryRunner.query(connection, sb.toString(), VocabularyManager.vocabularyRecordListHandler, vocabularyName);
            Vocabulary vocabulary = getVocabularyByTitle(vocabularyName);
            for (VocabRecord rec : records) {
                setDefinitionsToRecord(rec, vocabulary);
            }
            return records;

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void insertNewRecords(List<VocabRecord> records, Integer vocabularyID) throws SQLException {
        StringBuilder insertRecordQuery = new StringBuilder();
        insertRecordQuery.append("INSERT INTO vocabulary_record (id, vocabulary_id) VALUES ");

        for (int i = 0; i < records.size(); i++) {

            if (i == 0) {
                insertRecordQuery.append(" (?, ?)");
            } else {
                insertRecordQuery.append(", (?, ?)");
            }
        }
        Connection connection = null;
        QueryRunner runner = new QueryRunner();
        try {
            connection = MySQLHelper.getInstance().getConnection();
            try {
                if (MySQLHelper.getInstance().getSqlType() != SQLTYPE.H2) {
                    runner.execute(connection, "Lock tables vocabulary_record write");
                }
                int id = runner.query(connection, "SELECT MAX(id) +1 FROM vocabulary_record", MySQLHelper.resultSetToIntegerHandler);
                Object[] parameter = new Object[records.size() * 2];
                for (int i = 0; i < records.size(); i++) {
                    VocabRecord rec = records.get(i);
                    rec.setId(id);
                    parameter[i * 2] = id;
                    parameter[i * 2 + 1] = vocabularyID;
                    id = id + 1;
                }
                runner.execute(connection, insertRecordQuery.toString(), parameter);
            } finally {
                if (MySQLHelper.getInstance().getSqlType() != SQLTYPE.H2) {
                    runner.execute(connection, "unlock tables");
                }
            }
            fieldsBatchInsertion(records, vocabularyID, connection, runner);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    private static void fieldsBatchInsertion(List<VocabRecord> records, Integer vocabularyID, Connection connection, QueryRunner runner)
            throws SQLException {
        //  create a single query for all fields
        String fieldQuery = "INSERT INTO vocabulary_record_data (record_id,vocabulary_id, definition_id, label, language, value) VALUES ";
        int totalNumberOfRecords = records.size();
        int currentBatchNo = 0;
        int numberOfRecordsPerBatch = 50;
        while (totalNumberOfRecords > (currentBatchNo * numberOfRecordsPerBatch)) {
            List<VocabRecord> subList;
            if (totalNumberOfRecords > (currentBatchNo * numberOfRecordsPerBatch) + numberOfRecordsPerBatch) {
                subList = records.subList(currentBatchNo * numberOfRecordsPerBatch,
                        (currentBatchNo * numberOfRecordsPerBatch) + numberOfRecordsPerBatch);
            } else {
                subList = records.subList(currentBatchNo * numberOfRecordsPerBatch, totalNumberOfRecords);
            }

            List<Object> parameter = new ArrayList<>();

            StringBuilder insertFieldQuery = new StringBuilder();
            insertFieldQuery.append(fieldQuery);
            boolean isFirst = true;
            for (int i = 0; i < subList.size(); i++) {
                VocabRecord rec = subList.get(i);
                for (int j = 0; j < rec.getFields().size(); j++) {
                    if (isFirst) {
                        isFirst = false;
                        insertFieldQuery.append("(?,?,?,?,?,?) ");
                    } else {
                        insertFieldQuery.append(", (?,?,?,?,?,?) ");
                    }
                    Field f = rec.getFields().get(j);
                    parameter.add(rec.getId());
                    parameter.add(vocabularyID);
                    parameter.add(f.getDefinition().getId());
                    parameter.add(f.getLabel());
                    parameter.add(f.getLanguage());
                    parameter.add(f.getValue());
                }
            }
            runner.execute(connection, insertFieldQuery.toString(), parameter.toArray());
            currentBatchNo = currentBatchNo + 1;
        }
    }

    //        private static void fieldsBatchInsertion(List<VocabRecord> records, Integer vocabularyID, Connection connection, QueryRunner runner)
    //                throws SQLException {
    //            //  create a single query for all fields
    //            int totalNumberOfRecords = records.size();
    //            int currentBatchNo = 0;
    //            int numberOfRecordsPerBatch = 200;
    //            connection.setAutoCommit(false);
    //            PreparedStatement pstmt = connection.prepareStatement(
    //                    "INSERT INTO vocabulary_record_data (record_id,vocabulary_id, definition_id, label, language, value) VALUES (?,?,?,?,?,?)");
    //            while (totalNumberOfRecords > (currentBatchNo * numberOfRecordsPerBatch)) {
    //                List<VocabRecord> subList;
    //                if (totalNumberOfRecords > (currentBatchNo * numberOfRecordsPerBatch) + numberOfRecordsPerBatch) {
    //                    subList = records.subList(currentBatchNo * numberOfRecordsPerBatch,
    //                            (currentBatchNo * numberOfRecordsPerBatch) + numberOfRecordsPerBatch);
    //                } else {
    //                    subList = records.subList(currentBatchNo * numberOfRecordsPerBatch, totalNumberOfRecords);
    //                }
    //
    //
    //                pstmt.clearParameters();
    //                pstmt.clearBatch();
    //                for (int i = 0; i < subList.size(); i++) {
    //                    VocabRecord rec = subList.get(i);
    //                    for (int j = 0; j < rec.getFields().size(); j++) {
    //
    //                        Field f = rec.getFields().get(j);
    //                        pstmt.setInt(1, rec.getId());
    //                        pstmt.setInt(2, vocabularyID);
    //                        pstmt.setInt(3, f.getDefinition().getId());
    //                        pstmt.setString(4, f.getLabel());
    //                        pstmt.setString(5, f.getLanguage());
    //                        pstmt.setString(6, StringEscapeUtils.escapeSql(f.getValue()));
    //                        // Add row to the batch.
    //                        pstmt.addBatch();
    //                    }
    //                }
    //                pstmt.executeBatch();
    //                currentBatchNo = currentBatchNo + 1;
    //            }
    //            connection.commit();
    //        }

    public static void batchUpdateRecords(List<VocabRecord> records, Integer vocabularyID) throws SQLException {
        //        1.) delete old fields
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE from vocabulary_record_data WHERE record_id IN (");

        StringBuilder ids = new StringBuilder();

        for (VocabRecord rec : records) {
            if (ids.length() > 0) {
                ids.append(", ");
            }
            ids.append(rec.getId());
        }
        sql.append(ids.toString());
        sql.append(")");

        Connection connection = null;
        try {
            QueryRunner runner = new QueryRunner();
            connection = MySQLHelper.getInstance().getConnection();
            runner.execute(connection, sql.toString());
            //        2.) insert new fields
            fieldsBatchInsertion(records, vocabularyID, connection, runner);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static Timestamp getVocabularyLastAltered(Vocabulary vocabulary) throws SQLException {

        if (vocabulary == null) {
            return null;
        }
        String sql = "SELECT * FROM vocabulary WHERE id = ? ";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();

            return new QueryRunner().query(connection, sql, VocabularyManager.resultSetGetLastAlteredHandler, vocabulary.getId());

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void setVocabularyLastAltered(Vocabulary vocabulary) throws SQLException {

        if (vocabulary == null) {
            return;
        }

        String updateSql = "UPDATE vocabulary SET lastAltered = ? WHERE id = ?";
        Connection connection = null;

        try {
            Calendar calendar = Calendar.getInstance();
            Timestamp timeNow = new Timestamp(calendar.getTime().getTime());
            connection = MySQLHelper.getInstance().getConnection();

            new QueryRunner().update(connection, updateSql, timeNow, vocabulary.getId());

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static Timestamp getVocabularyLastUploaded(Vocabulary vocabulary) throws SQLException {
        if (vocabulary == null) {
            return null;
        }
        String sql = "SELECT * FROM " + vocabTable + " WHERE id = ? ";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();

            return new QueryRunner().query(connection, sql, VocabularyManager.resultSetGetLastUploadedHandler, vocabulary.getId());

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void setVocabularyLastUploaded(Vocabulary vocabulary) throws SQLException {

        if (vocabulary == null) {
            return;
        }

        String updateSql = "UPDATE vocabulary SET lastUploaded = ? WHERE id = ?";
        Connection connection = null;

        try {
            Calendar calendar = Calendar.getInstance();
            Timestamp timeNow = new Timestamp(calendar.getTime().getTime());
            connection = MySQLHelper.getInstance().getConnection();

            new QueryRunner().update(connection, updateSql, timeNow, vocabulary.getId());

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Map<String, VocabRecord> getRecordMap(int vocabularyId, int definitionId) throws SQLException {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT r2.value as identifier, r1.* FROM vocabulary_record_data r1  ");
        sql.append("left join vocabulary_record_data r2 on r1.record_id = r2.record_id and r2.definition_id = ?  ");
        sql.append("WHERE r1.vocabulary_id = ? ");

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql.toString(), VocabularyManager.vocabularyRecordMapHandler, definitionId, vocabularyId);

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }
}
