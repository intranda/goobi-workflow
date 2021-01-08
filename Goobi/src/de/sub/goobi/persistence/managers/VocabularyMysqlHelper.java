package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
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

/**
 * @author steffen
 *
 */
class VocabularyMysqlHelper implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5141386688477409583L;

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
            List<Definition> ret = new QueryRunner().query(connection, sql.toString(), new BeanListHandler<>(Definition.class), vocabularyId);
            return ret;
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

    static int getVocabularyCount(String order, String filter) throws SQLException {
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
            int numberOfProcessesWithTitle =
                    new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, vocabulary.getTitle());
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
            sql.append(
                    "INSERT INTO vocabulary_structure (vocabulary_id, label,language, type,validation,required ,mainEntry,distinctive,selection, titleField) ");
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

    @Deprecated
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
                        new QueryRunner().query(connection, sql.toString(), VocabularyManager.vocabularyRecordListHandler, vocabulary.getId());
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

    @Deprecated
    static void loadRecordsForVocabulary(Vocabulary vocabulary) throws SQLException {
        if (vocabulary != null && vocabulary.getId() != null) {
            String sql = "SELECT * FROM vocabularyRecords WHERE vocabId = ?";
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                List<VocabRecord> records = new QueryRunner().query(connection, sql.toString(),
                        VocabularyManager.resultSetToVocabularyRecordListHandler, vocabulary.getId());
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

    @Deprecated
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

    static void deleteRecord(VocabRecord record) throws SQLException {
        if (record.getId() != null) {
            String deleteFields = "DELETE from vocabulary_record_data WHERE record_id = ?";
            String deleteRecord = "DELETE from vocabulary_record WHERE id = ?";
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                run.update(connection, deleteFields, record.getId());
                run.update(connection, deleteRecord, record.getId());
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
            for (VocabRecord record : vocabulary.getRecords()) {
                if (record.getId() == null) {
                    Integer id = run.insert(connection, insertRecord, MySQLHelper.resultSetToIntegerHandler, vocabulary.getId());
                    record.setId(id);
                }
                for (Field field : record.getFields()) {
                    if (field.getId() == null) {
                        Integer id = run.insert(connection, insertField, MySQLHelper.resultSetToIntegerHandler, record.getId(), vocabulary.getId(),
                                field.getDefinition().getId(), field.getLabel(), field.getLanguage(), field.getValue());
                        field.setId(id);
                    } else {
                        run.update(connection, updateField, record.getId(), vocabulary.getId(), field.getDefinition().getId(), field.getLabel(),
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

    static void saveRecord(Integer vocabularyId, VocabRecord record) throws SQLException {
        String insertRecord = "INSERT INTO vocabulary_record (vocabulary_id) VALUES (?)";
        String insertField =
                "INSERT INTO vocabulary_record_data (record_id,vocabulary_id, definition_id, label, language, value) VALUES (?,?,?,?,?,?)";
        String updateField =
                "UPDATE vocabulary_record_data set record_id=?,vocabulary_id=?, definition_id=?, label=?, language=?, value=? WHERE id = ?";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            if (record.getId() == null) {
                Integer id = run.insert(connection, insertRecord, MySQLHelper.resultSetToIntegerHandler, vocabularyId);
                record.setId(id);
            }
            for (Field field : record.getFields()) {
                if (field.getId() == null) {
                    Integer id = run.insert(connection, insertField, MySQLHelper.resultSetToIntegerHandler, record.getId(), vocabularyId,
                            field.getDefinition().getId(), field.getLabel(), field.getLanguage(), field.getValue());
                    field.setId(id);
                } else {
                    run.update(connection, updateField, record.getId(), vocabularyId, field.getDefinition().getId(), field.getLabel(),
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
        if (MySQLHelper.isUsingH2()) {
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
                subQuery.append("r.label ='" + fieldName + "'");
            }
            sb.append(subQuery.toString());
            sb.append(")");

        }

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            List<Integer> idList = runner.query(connection, sb.toString(), MySQLHelper.resultSetToIntegerListHandler, vocabularyName);

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
        for (Field field : rec.getFields()) {
            for (Definition def : vocabulary.getStruct()) {
                if (field.getDefinitionId().equals(def.getId())) {
                    field.setDefinition(def);
                    break;
                }
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

    @Deprecated
    static void getRecords(Vocabulary vocabulary) throws SQLException {
        String likeStr = "like";
        if (MySQLHelper.isUsingH2()) {
            likeStr = "ilike";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT vocabularyRecords.* FROM vocabularyRecords LEFT JOIN vocabularies ON vocabularyRecords.vocabId=vocabularies.vocabId ");
        sb.append("WHERE vocabularies.vocabId = ? ");
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
        if (MySQLHelper.isUsingH2()) {
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
            List<VocabRecord> records =
                    new QueryRunner().query(connection, sb.toString(), VocabularyManager.vocabularyRecordListHandler, vocabularyName);
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
                if (!MySQLHelper.isUsingH2()) {
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
                if (!MySQLHelper.isUsingH2()) {
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

    public static void batchUpdateRecords(List<VocabRecord> records, Integer vocabularyID) throws SQLException {
        //        1.) delete old fields;
        String sql = "DELETE from vocabulary_record_data WHERE record_id IN (?)";
        List<Integer> idList = new ArrayList<>(records.size());

        for (VocabRecord rec : records) {
            idList.add(rec.getId());
        }
        Connection connection = null;
        try {
            QueryRunner runner = new QueryRunner();
            connection = MySQLHelper.getInstance().getConnection();
            runner.execute(connection, sql, idList);
            //        2.) insert new fields;
            fieldsBatchInsertion(records, vocabularyID, connection, runner);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }
}
