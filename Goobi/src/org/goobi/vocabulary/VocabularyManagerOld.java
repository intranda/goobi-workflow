package org.goobi.vocabulary;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.MySQLHelper;
import lombok.Data;
import lombok.extern.log4j.Log4j;

@Data
@Log4j
public class VocabularyManagerOld {
    private ArrayList<Definition> definitions;
    private Vocabulary vocabulary;
    private VocabRecord record;
    private Gson gson;

    public VocabularyManagerOld() throws SQLException {
        this.gson = new GsonBuilder().create();
    }

    /**
     * Read the entire vocabulary from the serialised file
     * 
     * @param title the title of the vocabulary to load
     * @throws SQLException
     */
    public void loadVocabulary(String title) throws SQLException {
        if (StringUtils.isBlank(title)) {
            return;
        }
        title = title.trim();
        if (vocabulary != null && title.equals(vocabulary.getTitle())) {
            return;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT v.vocabId, v.title vocabTitle, v.description, v.structure, r.recordId as recordId, r.attr ");
        sql.append("FROM vocabularies v LEFT JOIN vocabularyRecords r ON v.vocabId = r.vocabId WHERE v.title =?");

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();

            java.sql.PreparedStatement stmt = connection.prepareStatement(sql.toString());
            stmt.setString(1, title);

            //                        stmt.executeUpdate("DELETE FROM vocabularyRecords" );
            //                        stmt.executeUpdate("DELETE FROM vocabularies" );

            ResultSet rs = stmt.executeQuery();

            setupFromResultSet(rs, title);
            showFirstRecord();

        } catch (Exception e) {
            log.error(e);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    //setup: by design, only one vocabTitle.
    private void setupFromResultSet(ResultSet rs, String title) throws SQLException {

        Vocabulary vocab = null;

        try {
            while (rs.next()) {

                JsonParser jsonParser = new JsonParser();

                //only first time:
                if (vocab == null) {
                    vocab = getVocabularyFromResultSet(rs, jsonParser, title);
                    this.vocabulary = vocab;
                }
                // TODO
                //Now get the record:
                int iRecordId = rs.getInt("recordId");
                ArrayList<Field> lstFields = new ArrayList<>();

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

                        for (Field f : lstFields) {
                            f.setDefinition(getDefinitionForLabel(f.getLabel()));
                        }

                        vocab.getRecords().add(new VocabRecord(iRecordId, lstFields));
                    } catch (Exception e) {
                        log.error(e);
                    }

                    log.debug("load Vocabulary " + vocabulary.getTitle());
                }
            }
        } catch (JsonSyntaxException | SQLException e) {
            log.error(e);
        }

        //init:
        if (vocab == null) {
            generateSampleDef();
            generateSampleVocabulary("Playground");
        }

    }

    private Vocabulary getVocabularyFromResultSet(ResultSet rs, JsonParser jsonParser, String title) throws SQLException {

        Vocabulary vocab = null;
        try {
            int vocabId = rs.getInt("vocabId");
            String strVocabTitle = rs.getString("vocabTitle");
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

            this.definitions = lstDefs;

            vocab = new Vocabulary();
            vocab.setId(vocabId);
            vocab.setTitle(strVocabTitle);
            vocab.setDescription(strDescription);
            vocab.setStruct(lstDefs);
            vocab.setRecords(new ArrayList<VocabRecord>());

        } catch (Exception e) {
            log.error("Problem while loading the vocabulary " + title);
            Helper.setFehlerMeldung("Problem while loading the vocabulary " + title, e);
            vocab = new Vocabulary();
            vocab.setTitle(title);
            vocab.setDescription("No description given.");
            vocab.setRecords(new ArrayList<VocabRecord>());
        }

        return vocab;
    }

    /* Load first record if it is there. Otherwise create new empty one.
     */
    private void showFirstRecord() throws SQLException {
        if (vocabulary.getRecords().size() == 0) {
            addNewRecord();
        }
        record = vocabulary.getRecords().get(0);
    }

    /**
     * Save vocabulary in file system TODO: not performant: change to batch update.
     * 
     * @throws SQLException
     */
    public void saveVocabulary() throws SQLException {

        Connection connection = null;

        try {
            connection = MySQLHelper.getInstance().getConnection();

            //is the vocab itself new?
            List<String> lstAllVocabs = getAllVocabulariesFromDB();
            if (!lstAllVocabs.contains(this.vocabulary.getTitle())) {
                saveNewVocabularyToDB(connection);
            } else {
                updateVocabularyToDB(connection);
            }

            //check all fields are present:
            for (Definition def : definitions) {
                addDefToRecords(def);
            }

            //and save all the records
            for (VocabRecord rec : vocabulary.getRecords()) {

                String strAttr = getJsonRecord(rec);

                StringBuilder sql = new StringBuilder();
                sql.append("UPDATE vocabularyRecords ");
                sql.append("SET title =  ?, attr  = ? ");
                sql.append("WHERE vocabId = " + vocabulary.getId() + " AND recordId = " + rec.getId());

                java.sql.PreparedStatement stmt = connection.prepareStatement(sql.toString());
                stmt.setString(1, rec.getTitle());
                stmt.setString(2, strAttr);

                int iResult = stmt.executeUpdate();

            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Problem while saving the vocabulary " + vocabulary.getTitle());
            Helper.setFehlerMeldung("Problem while saving the vocabulary " + vocabulary.getTitle(), e);

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

        //                String file = ConfigurationHelper.getInstance().getGoobiFolder()
        //                        + config.configurationAt("vocabulary[@title='" + vocabulary.getTitle() + "']").getString("path");
        //                try (Writer writer = new FileWriter(file)) {
        //                    Gson gson = new GsonBuilder().create();
        //                    gson.toJson(vocabulary, writer);
        //                    log.debug("Vocabulary " + vocabulary.getTitle() + " saved in file " + file);
        //                } catch (IOException e) {
        //                    log.error("Problem while saving the vocabulary to file " + file, e);
        //                    Helper.setFehlerMeldung("Problem while saving the vocabulary to file " + file, e);
        //                }
    }

    public List<String> getAllVocabulariesFromDB() throws SQLException {

        ArrayList<String> vocabs = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT title FROM vocabularies ORDER BY title");

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();

            java.sql.Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql.toString());

            while (rs.next()) {
                vocabs.add(rs.getString("title"));
            }

        } catch (SQLException e) {
            log.error(e);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

        return vocabs;
    }

    private void updateVocabularyToDB(Connection connection) throws SQLException {
        //vocabulary:
        //id is auto-incremented
        StringBuilder sqlVocab = new StringBuilder();
        sqlVocab.append("UPDATE vocabularies ");
        sqlVocab.append("SET title =  ?, description = ?, structure  = ? ");
        sqlVocab.append("WHERE vocabId = " + vocabulary.getId());

        java.sql.PreparedStatement stmtVocab = connection.prepareStatement(sqlVocab.toString());

        stmtVocab.setString(1, vocabulary.getTitle());
        stmtVocab.setString(2, vocabulary.getDescription());
        stmtVocab.setString(3, gson.toJson(this.definitions));
        Integer rsVocab = stmtVocab.executeUpdate();
    }

    private void saveNewVocabularyToDB(Connection connection) throws SQLException {
        //vocabulary:
        //id is auto-incremented
        StringBuilder sqlVocab = new StringBuilder();
        sqlVocab.append("INSERT INTO vocabularies(title, description, structure) ");
        sqlVocab.append("VALUES (?,?,?)");

        java.sql.PreparedStatement stmtVocab = connection.prepareStatement(sqlVocab.toString());

        stmtVocab.setString(1, vocabulary.getTitle());
        stmtVocab.setString(2, vocabulary.getDescription());
        stmtVocab.setString(3, gson.toJson(this.definitions));
        Integer rsVocab = stmtVocab.executeUpdate();
    }

    /**
     * add a new empty record to the vocabulary
     * 
     * @throws SQLException
     */
    public void addNewRecord() throws SQLException {
        List<Field> fields = new ArrayList<>();
        for (Definition d : definitions) {
            fields.add(new Field(d.getLabel(), "", d));
        }

        //        int iLast = getLargestRecordId();

        record = new VocabRecord(null, fields);
        addNewRecord(record);
    }

    //Add new record
    public void addNewRecord(VocabRecord record) throws SQLException {

        vocabulary.getRecords().add(record);
        String strTitle = "NULL";
        if (!record.getTitle().isEmpty()) {
            strTitle = record.getTitle();
        }

        //   String strValues = record.getId() + ", " + strTitle + ", " + this.vocabulary.getId() + ",  \'" + getJsonRecord(record) + "\'";

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO vocabularyRecords(title, vocabId, attr) ");
        sql.append("VALUES (?,?,?)");

        Connection connection = null;
        try {

            connection = MySQLHelper.getInstance().getConnection();

            java.sql.PreparedStatement stmt = connection.prepareStatement(sql.toString());
            stmt.setString(1, strTitle);
            stmt.setInt(2, this.vocabulary.getId());
            stmt.setString(3, getJsonRecord(record));

            if (stmt.execute()) {
                ResultSet rs = stmt.getResultSet();
                rs.next();
                record.setId(rs.getInt("recordId"));
            }

        } catch (SQLException e) {
            log.error(e);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private String getJsonRecord(VocabRecord record2) {

        return gson.toJson(record2.getFields());
    }

    /**
     * remove the given record from the vocabulary
     * 
     * @param record the record to remove from vocabulary
     * @throws SQLException
     */
    public void deleteRecord(VocabRecord record) throws SQLException {

        vocabulary.getRecords().remove(record);

        //if not yet saved, then the id will be null:
        if (record.getId() == null) {
            return;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM vocabularyRecords WHERE recordId = ? AND vocabId = ?");
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();

            java.sql.PreparedStatement stmt = connection.prepareStatement(sql.toString());
            stmt.setInt(1, record.getId());
            stmt.setInt(2, this.vocabulary.getId());

            int iResult = stmt.executeUpdate();

        } catch (SQLException e) {
            log.error(e);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

        showFirstRecord();
    }

    /**
     * find correct definition for a given field
     * 
     * @param label
     * @return the correct Definition that was found or a default
     */
    private Definition getDefinitionForLabel(String label) {
        for (Definition d : definitions) {
            if (d.getLabel().equals(label)) {
                return d;
            }
        }
        return new Definition("none", "input", null, null);
    }

    private void generateSampleDef() {
        definitions = new ArrayList<>();

        definitions.add(new Definition("Title", "input", "", ""));
        definitions.add(new Definition("Translation", "input", "", ""));
        definitions.add(new Definition("Use case", "textarea", "", ""));
        definitions.add(new Definition("Description", "textarea", "", ""));
        definitions.add(new Definition("Days", "select", "", "Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday"));
        definitions.add(new Definition("Keywords", "textarea", "", ""));
    }

    /**
     * Generate some simple demo data to see if the GUI is working
     * 
     * @param title
     * @throws SQLException
     */
    private void generateSampleVocabulary(String title) throws SQLException {
        vocabulary = new Vocabulary();
        vocabulary.setId(1003);
        vocabulary.setTitle(title);
        vocabulary.setDescription("This is the description for vocabulary " + title);
        vocabulary.setRecords(new ArrayList<VocabRecord>());

        List<Field> fields1 = new ArrayList<>();
        fields1.add(new Field("Title", "Telefon", getDefinitionForLabel("Title")));
        fields1.add(new Field("Translation", "telephone", getDefinitionForLabel("Translation")));
        fields1.add(new Field("Use case", "Speaking", getDefinitionForLabel("Use case")));
        fields1.add(new Field("Description", "Thing that rings to speak on the phone", getDefinitionForLabel("Description")));
        fields1.add(new Field("Days", "Tuesday", getDefinitionForLabel("Days")));
        fields1.add(new Field("Keywords", "blue", getDefinitionForLabel("Keywords")));
        vocabulary.getRecords().add(new VocabRecord(100, fields1));

        List<Field> fields2 = new ArrayList<>();
        fields2.add(new Field("Title", "Fax", getDefinitionForLabel("Title")));
        fields2.add(new Field("Translation", "fax", getDefinitionForLabel("Translation")));
        fields2.add(new Field("Use case", "Send documents", getDefinitionForLabel("Use case")));
        fields2.add(new Field("Description", "Thing that is used to send documents electronically", getDefinitionForLabel("Description")));
        fields2.add(new Field("Days", "Wednesday", getDefinitionForLabel("Days")));
        fields2.add(new Field("Keywords", "red", getDefinitionForLabel("Keywords")));
        vocabulary.getRecords().add(new VocabRecord(101, fields2));

        List<Field> fields3 = new ArrayList<>();
        fields3.add(new Field("Title", "Modem", getDefinitionForLabel("Title")));
        fields3.add(new Field("Translation", "modem", getDefinitionForLabel("Translation")));
        fields3.add(new Field("Use case", "Connect to internet via telephone line", getDefinitionForLabel("Use case")));
        fields3.add(new Field("Description", "Connect computer or other device to the internet via telephone line",
                getDefinitionForLabel("Description")));
        fields3.add(new Field("Days", "Thursday", getDefinitionForLabel("Days")));
        fields3.add(new Field("Keywords", "green", getDefinitionForLabel("Keywords")));
        vocabulary.getRecords().add(new VocabRecord(102, fields3));

        //        //Save to DB:
        //        String strValues = vocabulary.getId() + ", \'" + vocabulary.getTitle() + "\' ,  \'" + vocabulary.getDescription() + "\', \'"
        //                + gson.toJson(this.definitions) + "\'";

        //vocabulary:
        StringBuilder sqlVocab = new StringBuilder();
        sqlVocab.append("INSERT INTO vocabularies(vocabId, title, description, structure) ");
        sqlVocab.append("VALUES ( ?,?,?,?)");

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();

            //first vocabulary:
            java.sql.PreparedStatement stmtVocab = connection.prepareStatement(sqlVocab.toString());

            stmtVocab.setInt(1, vocabulary.getId());
            stmtVocab.setString(2, vocabulary.getTitle());
            stmtVocab.setString(3, vocabulary.getDescription());
            stmtVocab.setString(4, gson.toJson(this.definitions));
            Integer rsVocab = stmtVocab.executeUpdate();

            //then records:
            StringBuilder sqlRec = new StringBuilder();
            sqlRec.append("INSERT INTO vocabularyRecords(recordId, title, vocabId, attr) ");
            sqlRec.append("VALUES ( ?,?,?,?)");
            java.sql.PreparedStatement stmt = connection.prepareStatement(sqlRec.toString());

            for (VocabRecord record : vocabulary.getRecords()) {

                //vocabularyRecords:
                stmt.setInt(1, record.getId());
                stmt.setString(2, record.getTitle());
                stmt.setInt(3, this.vocabulary.getId());
                stmt.setString(4, getJsonRecord(record));

                //                String strRecord =
                //                        record.getId() + ", \'" + record.getTitle() + "\' ,  \'" + vocabulary.getId() + "\', \'" + getJsonRecord(record) + "\'";
                //
                //                StringBuilder sqlRec = new StringBuilder();
                //                sqlRec.append("INSERT INTO vocabularyRecords(recordId, title, vocabId, attr) ");
                //                sqlRec.append("VALUES ( " + strRecord + ")");

                Integer rsRecord = stmt.executeUpdate();
            }

        } catch (SQLException e) {
            log.error(e);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    //assuming that the definitions have been set, make a new vocab
    public void generateNewVocabulary(String title) {

        this.vocabulary = new Vocabulary();
        //        vocabulary.setId();
        vocabulary.setTitle(title);
        vocabulary.setDescription("This is the description for " + title);
        vocabulary.setRecords(new ArrayList<VocabRecord>());
        vocabulary.setStruct(this.definitions);
    }

    public static ResultSetHandler<Vocabulary> resultSetToVocabularyHandler = null;

    //If a new definition has been made, add it to all the records:
    public void addDefToRecords(Definition def) {

        //adjust the records appropriately:
        for (VocabRecord record : vocabulary.getRecords()) {

            Boolean boExists = false;
            for (Field field : record.getFields()) {
                if (field.getLabel().contentEquals(def.getLabel())) {
                    boExists = true;
                    break;
                }
            }

            if (!boExists) {
                record.getFields().add(new Field(def.getLabel(), "", def));
            }
        }
    }

    //If a new definition has been made, add it to all the records:
    public void removeDefFromRecords(Definition def) {

        definitions.remove(def);

        if (vocabulary.getRecords().size() == 0) {
            return;
        }

        //adjust the records appropriately:
        for (VocabRecord record : vocabulary.getRecords()) {

            record.getFields().removeIf(f -> f.getLabel() == def.getLabel());
        }
    }

    //If a new definition has been made, add it to all the records:
    public void removeUndefinedDefsFromRecords() {

        ArrayList<String> lstDefs = new ArrayList<>();

        for (Definition def : definitions) {
            lstDefs.add(def.getLabel());
        }

        if (vocabulary.getRecords().size() == 0) {
            return;
        }

        //adjust the records appropriately:
        for (VocabRecord record : vocabulary.getRecords()) {

            record.getFields().removeIf(f -> !lstDefs.contains(f.getLabel()));
        }
    }

}
