package org.goobi.vocabulary;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
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
import sun.security.ssl.Debug;

@Data
@Log4j
public class VocabularyManager {
    private List<Definition> definitions;
    private Vocabulary vocabulary;
    private XMLConfiguration config;
    private VocabRecord record;
    private Gson gson;

    private static final Logger logger = Logger.getLogger(VocabularyManager.class);

    public VocabularyManager(XMLConfiguration config) {
        this.config = config;
        this.gson = new GsonBuilder().create();

        //do the tables exist?
        setupDBs();
    }

    /**
     * Read the entire vocabulary from the serialised file
     * 
     * @param title the title of the vocabulary to load
     */
    public void loadVocabulary(String title) {

        if (title == null) {
            generateSampleDef();
            generateSampleVocabulary("test");
            showFirstRecord();
            return;
        }

        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT v.vocabId, v.title vocabTitle, v.description, v.structure, r.title recordTitle, r.attr ");
        sql.append("FROM vocabularies v LEFT JOIN vocabularyRecords r ON v.vocabId = r.vocabId WHERE v.title = \'" + title + "\'");

        try {
            connection = MySQLHelper.getInstance().getConnection();

            java.sql.Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql.toString());

            setupFromResultSet(rs, title);

            //  this.vocabulary = new QueryRunner().query(connection, sql.toString(), resultSetToVocabularyHandler, title);
            //  this.definitions = vocabulary.getStruct();

            showFirstRecord();

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    //setup: by design, only one vocabTitle.
    private void setupFromResultSet(ResultSet rs, String title) {

        Vocabulary vocab = null;

        try {
            while (rs.next()) {

                JsonParser jsonParser = new JsonParser();

                //only first time:
                if (vocab == null) {
                    vocab = getVocabularyFromResultSet(rs, jsonParser, title);
                    this.vocabulary = vocab;
                }

                //Now get the record:
                String strRecordTitle = rs.getString("recordTitle");
                ArrayList<Field> lstFields = new ArrayList<Field>();

                JsonArray attr = jsonParser.parse("attr").getAsJsonArray();

                if (attr != null) {
                    for (JsonElement jsonElt : attr) {

                        JsonObject jsonField = jsonElt.getAsJsonObject();
                        lstFields.add(gson.fromJson(jsonField, Field.class));
                    }
                }

                for (Field f : lstFields) {
                    f.setDefinition(getDefinitionForLabel(f.getLabel()));
                }

                vocab.getRecords().add(new VocabRecord(strRecordTitle, lstFields));

                log.debug("load Vocabulary " + vocabulary.getTitle());

            }
        } catch (JsonSyntaxException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //init:
        if (vocab == null) {
            generateSampleDef();
            generateSampleVocabulary("test");
        }

    }

    private Vocabulary getVocabularyFromResultSet(ResultSet rs, JsonParser jsonParser, String title) throws SQLException {

        Vocabulary vocab = null;
        try {
            int vocabId = rs.getInt("vocabId");
            String strVocabTitle = rs.getString("vocabTitle");
            String strDescription = rs.getString("description");
            String jsonStruct = rs.getString("structure");
            ArrayList<Definition> lstDefs = new ArrayList<Definition>();

            JsonArray struct = jsonParser.parse(jsonStruct).getAsJsonArray();

            if (struct != null) {
                for (JsonElement jsonElt : struct) {

                    JsonObject jsonObj = jsonElt.getAsJsonObject();

                    String strLabel = jsonObj.get("label").getAsString();
                    String strType = jsonObj.get("type").getAsString();
                    String strValidation = jsonObj.get("validation").getAsString();
                    String strSelect = jsonObj.get("select").getAsString();

                    if (strLabel != null)
                        lstDefs.add(new Definition(strLabel, strType, strValidation, strSelect));
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
    private void showFirstRecord() {
        if (vocabulary.getRecords().size() == 0) {
            addNewRecord();
        }
        record = vocabulary.getRecords().get(0);
    }

    /**
     * Save vocabulary in file system
     */
    public void saveVocabulary() {

        //        Connection connection = null;
        //        StringBuilder sql = new StringBuilder();
        //        sql.append("SELECT v.vocabId, v.title vocabTitle, v.description, v.structure, r.title recordTitle, r.attr ");
        //        sql.append("FROM vocabularies v LEFT JOIN vocabularyRecords r ON v.vocabId = r.vocabId WHERE vocabTitle = " + title);
        //
        //        try {
        //
        //            connection = MySQLHelper.getInstance().getConnection();
        //            java.sql.Statement stmt = connection.createStatement();
        //            ResultSet rs = stmt.executeQuery(sql.toString());
        //
        //            setupFromResultSet(rs, title);
        //
        //            //  this.vocabulary = new QueryRunner().query(connection, sql.toString(), resultSetToVocabularyHandler, title);
        //            //  this.definitions = vocabulary.getStruct();
        //
        //            showFirstRecord();
        //
        //        } finally {
        //            if (connection != null) {
        //                MySQLHelper.closeConnection(connection);
        //            }
        //        }
        //        
        //        
        //        
        //        
        //        
        //        String file = ConfigurationHelper.getInstance().getGoobiFolder()
        //                + config.configurationAt("vocabulary[@title='" + vocabulary.getTitle() + "']").getString("path");
        //        try (Writer writer = new FileWriter(file)) {
        //            Gson gson = new GsonBuilder().create();
        //            gson.toJson(vocabulary, writer);
        //            log.debug("Vocabulary " + vocabulary.getTitle() + " saved in file " + file);
        //        } catch (IOException e) {
        //            log.error("Problem while saving the vocabulary to file " + file, e);
        //            Helper.setFehlerMeldung("Problem while saving the vocabulary to file " + file, e);
        //        }
    }

    /**
     * add a new empty record to the vocabulary
     */
    public void addNewRecord() {
        List<Field> fields = new ArrayList<>();
        for (Definition d : definitions) {
            fields.add(new Field(d.getLabel(), "", d));
        }

        record = new VocabRecord(String.valueOf(System.currentTimeMillis()), fields);
        addNewRecord(record);
    }

    //Add new record
    public void addNewRecord(VocabRecord record) {

        vocabulary.getRecords().add(record);
        String strValues = record.getId() + ", " + record.getTitle() + ", " + this.vocabulary.getId() + ", " + getJsonRecord(record);

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO vocabularyRecords(recordId, title, vocabId, attr) ");
        sql.append("VALUES ( " + strValues + ")");

        Connection connection;
        try {

            connection = MySQLHelper.getInstance().getConnection();
            java.sql.Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql.toString());

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String getJsonRecord(VocabRecord record2) {

        return gson.toJson(record2);
    }

    /**
     * remove the given record from the vocabulary
     * 
     * @param record the record to remove from vocabulary
     */
    public void deleteRecord(VocabRecord record) {

        vocabulary.getRecords().remove(record);

        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM vocabularyRecords WHERE recordId = " + record.getId());

        Connection connection;
        try {
            connection = MySQLHelper.getInstance().getConnection();

            java.sql.Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql.toString());

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
        definitions = new ArrayList<Definition>();

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
     */
    private void generateSampleVocabulary(String title) {
        vocabulary = new Vocabulary();
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
        vocabulary.getRecords().add(new VocabRecord("r1", fields1));

        List<Field> fields2 = new ArrayList<>();
        fields2.add(new Field("Title", "Fax", getDefinitionForLabel("Title")));
        fields2.add(new Field("Translation", "fax", getDefinitionForLabel("Translation")));
        fields2.add(new Field("Use case", "Send documents", getDefinitionForLabel("Use case")));
        fields2.add(new Field("Description", "Thing that is used to send documents electronically", getDefinitionForLabel("Description")));
        fields2.add(new Field("Days", "Wednesday", getDefinitionForLabel("Days")));
        fields2.add(new Field("Keywords", "red", getDefinitionForLabel("Keywords")));
        vocabulary.getRecords().add(new VocabRecord("r2", fields2));

        List<Field> fields3 = new ArrayList<>();
        fields3.add(new Field("Title", "Modem", getDefinitionForLabel("Title")));
        fields3.add(new Field("Translation", "modem", getDefinitionForLabel("Translation")));
        fields3.add(new Field("Use case", "Connect to internet via telephone line", getDefinitionForLabel("Use case")));
        fields3.add(new Field("Description", "Connect computer or other device to the internet via telephone line",
                getDefinitionForLabel("Description")));
        fields3.add(new Field("Days", "Thursday", getDefinitionForLabel("Days")));
        fields3.add(new Field("Keywords", "green", getDefinitionForLabel("Keywords")));
        vocabulary.getRecords().add(new VocabRecord("r3", fields3));

        //Save to DB:
        String strValues = vocabulary.getId() + ", \'" + vocabulary.getTitle() + " \' ,  \'" + vocabulary.getDescription() + "\', \'"
                + gson.toJson(this.definitions) + "\'";

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO vocabularies(vocabId, title, description, structure) ");
        sql.append("VALUES ( " + strValues + ")");

        Connection connection;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            java.sql.Statement stmt = connection.createStatement();
            Integer rs = stmt.executeUpdate(sql.toString());

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static ResultSetHandler<Vocabulary> resultSetToVocabularyHandler = null;

    private void setupDBs() {

        StringBuilder sql = new StringBuilder();

        sql.append("CREATE TABLE IF NOT EXISTS `vocabularies` (");
        sql.append(" `vocabId` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        sql.append(" `title` varchar(255) DEFAULT NULL,");
        sql.append(" `description` varchar(255) DEFAULT NULL,");
        sql.append(" `structure` text DEFAULT NULL,");
        sql.append("  PRIMARY KEY (`vocabId`),");
        sql.append("  CHECK (structure IS NULL OR JSON_VALID(structure))");
        sql.append(" ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        StringBuilder sql2 = new StringBuilder();
        sql2.append(" CREATE TABLE IF NOT EXISTS `vocabularyRecords` (");
        sql2.append(" `recordId` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        sql2.append(" `title` varchar(255) DEFAULT NULL,");
        sql2.append(" `vocabId` int(10) unsigned NOT NULL,");
        sql2.append(" `attr` text DEFAULT NULL,");
        sql2.append("  PRIMARY KEY (`recordId`),");
        sql2.append("  FOREIGN KEY (`vocabId`) REFERENCES vocabularies(vocabId) ON UPDATE CASCADE,");
        sql2.append("  CHECK (attr IS NULL OR JSON_VALID(attr))");
        sql2.append(" ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        Connection connection;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            java.sql.Statement stmt = connection.createStatement();
            Integer rs1 = stmt.executeUpdate(sql.toString());

            Debug.println("create DB1 ", rs1.toString());

            Integer rs2 = stmt.executeUpdate(sql2.toString());
            Debug.println("create DB2 ", rs2.toString());

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            String strWTF = e.getMessage();
            e.printStackTrace();
        }
    }

}

//    = new ResultSetHandler<Vocabulary>({     
//        return null;
//    };);
//    

//        Vocabulary vocab = null;
//        
//        @Override
//        public Vocabulary handle(ResultSet rs) throws SQLException {
//            try {
//                if (rs.next()) {
//                    return convert(rs);
//                }
//            } finally {
//                if (rs != null) {
//                    rs.close();
//                }
//            }
//            return null;
//        }
//
//        private Vocabulary convert(ResultSet rs) {
//            
//          int vocabId =  rs.getInt("vocabId");
//          String strVocabTitle = rs.getString("vocabTitle");
//          String strDescription = rs.getString("description");
//          String jsonStruct = rs.getString("structure");
//          ArrayList<Definition> lstDefs = new ArrayList<Definition>();
//          
//          try {
//              JsonParser jsonParser = new JsonParser();
//              JsonArray struct = jsonParser.parse(jsonStruct).getAsJsonArray();
//              
//              if (struct != null) {
//                  for(int i=0; i<struct.length(); i++){
//                      JSONObject jsonObj = struct.getJSONObject(i);
//                      
//                      String strLabel =jsonObj.get("label");
//                      String strType =jsonObj.get("type");
//                      String strValidation =jsonObj.get("validation");
//                      String strSelect =jsonObj.get("select");
//                      
//                      if (strLabel != null)
//                          lstDefs.add(new Definition(strLabel, strType, strValidation, strSelect));
//                    }
//              }
//              
//              //only first time:
//              if (vocab == null) {
//                  vocab = new Vocabulary();
//                  vocab.setTitle(strVocabTitle);
//                  vocab.setDescription(strDescription);
//                  vocab.setStruct(lstDefs);
//                  vocab.setRecords(new ArrayList<VocabRecord>());
//              }
//              
//                  //Now get the record:
//                  String strRecordTitle = rs.getString("recordTitle");
//                  ArrayList<Field> lstFields = new ArrayList<Field>();
//                  
//                  JsonArray attr = jsonParser.parse(attr).getAsJsonArray();
//                  
//                  if (attr != null) {
//                      Gson gson = new GsonBuilder().create();
//                      for(int i=0; i<struct.length(); i++){
//                          JSONObject jsonField = struct.getJSONObject(i);
//                          
//                          lstFields.add(gson.fromJson(jsonField, Field.class));
//                         }
//                  }
//                  
//                  for (Field f : lstFields) {
//                      f.setDefinition(getDefinitionForLabel(f.getLabel()));
//                  }
//          
//                  vocab.getRecords().add(new VocabRecord(strRecordTitle, lstFields));
//          
//              return vocab;
//              
//              
//              log.debug("Vocabulary " + vocabulary.getTitle() + " was loaded from file " + file);
//          } catch (FileNotFoundException e) {
//              log.error("Problem while loading the vocabulary from file " + file, e);
//              Helper.setFehlerMeldung("Problem while loading the vocabulary from file " + file, e);
//              vocabulary = new Vocabulary();
//              vocabulary.setTitle(title);
//              vocabulary.setDescription("No description given.");
//              vocabulary.setRecords(new ArrayList<VocabRecord>());
//              showFirstRecord();
//          }
//          
//       
//          
//          
//          
//          
//          
//          
////          String label = field.getString("@label");
////          String type = field.getString("@type");
////          String validation = field.getString("@validation");
////          String select = field.getString("@select");
////          definitions.add(new Definition(label, type, validation, select));
////          
////          
////            SubnodeConfiguration subConfig = config.configurationAt("vocabulary[@title='" + title + "']");
////            List<HierarchicalConfiguration> fields = subConfig.configurationsAt("field");
////            for (HierarchicalConfiguration field : fields) {
////                String label = field.getString("@label");
////                String type = field.getString("@type");
////                String validation = field.getString("@validation");
////                String select = field.getString("@select");
////                definitions.add(new Definition(label, type, validation, select));
////            }
////            //      generateSampleVocabulary(title);
////
////            String file = ConfigurationHelper.getInstance().getGoobiFolder() + subConfig.getString("path");
////            try {
////                Gson gson = new Gson();
////                JsonReader reader = new JsonReader(new FileReader(file));
////                vocabulary = gson.fromJson(reader, Vocabulary.class);
////                for (VocabRecord r : vocabulary.getRecords()) {
////                    for (Field f : r.getFields()) {
////                        f.setDefinition(getDefinitionForLabel(f.getLabel()));
////                    }
////                }
////
////                showFirstRecord();
////
////                log.debug("Vocabulary " + vocabulary.getTitle() + " was loaded from file " + file);
////            } catch (FileNotFoundException e) {
////                log.error("Problem while loading the vocabulary from file " + file, e);
////                Helper.setFehlerMeldung("Problem while loading the vocabulary from file " + file, e);
////                vocabulary = new Vocabulary();
////                vocabulary.setTitle(title);
////                vocabulary.setDescription("No description given.");
////                vocabulary.setRecords(new ArrayList<VocabRecord>());
////                showFirstRecord();
////            }
////        }
//
//    };
//    }
//
//}
