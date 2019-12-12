package org.goobi.vocabulary;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import lombok.Data;
import lombok.extern.log4j.Log4j;

@Data
@Log4j
public class VocabularyManager {
	private List<Definition> definitions;
	private Vocabulary vocabulary;
	private XMLConfiguration config;
	private VocabRecord record;
	
	public VocabularyManager(XMLConfiguration config) {
		this.config = config;
	}
	
	/**
	 * Read the entire vocabulary from the serialised file 
	 * 
	 * @param title the title of the vovabulary to load
	 */
	public void loadVocabulary(String title) {
		definitions = new ArrayList<Definition>();

		SubnodeConfiguration subConfig = config.configurationAt("vocabulary[@title='" + title + "']");
    		List<HierarchicalConfiguration> fields = subConfig.configurationsAt("field");
		for (HierarchicalConfiguration field : fields) {
			String label = field.getString("@label");
			String type = field.getString("@type");
			String validation = field.getString("@validation");
			String select = field.getString("@select");
			definitions.add(new Definition(label, type, validation, select));
		}
//		generateSampleVocabulary(title);
		
		String file = ConfigurationHelper.getInstance().getGoobiFolder() + subConfig.getString("path");
		try {
			Gson gson = new Gson();
			JsonReader reader = new JsonReader(new FileReader(file));
			vocabulary = gson.fromJson(reader, Vocabulary.class); 
			for (VocabRecord r : vocabulary.getRecords()) {
				for (Field f : r.getFields()) {
					f.setDefinition(getDefinitionForLabel(f.getLabel()));
				}
			}
			
			showFirstRecord();
			
			log.debug("Vocabulary " + vocabulary.getTitle() + " was loaded from file " + file);
		} catch (FileNotFoundException e) {
			log.error("Problem while loading the vocabulary from file " + file, e);
			Helper.setFehlerMeldung("Problem while loading the vocabulary from file " + file, e);
			vocabulary = new Vocabulary();
			vocabulary.setTitle(title);
			vocabulary.setDescription("No description given.");
			vocabulary.setRecords(new ArrayList<VocabRecord>());
			showFirstRecord();
		}
	}
	
	/**
	 * Load first record if it is there. Otherwise create new empty one.
	 */
	private void showFirstRecord() {
		if (vocabulary.getRecords().size()==0) {
			addNewRecord();
		}
		record = vocabulary.getRecords().get(0);
	}

	/**
	 * Save vocabulary in file system
	 */
	public void saveVocabulary() {
		String file = ConfigurationHelper.getInstance().getGoobiFolder() + config.configurationAt("vocabulary[@title='" + vocabulary.getTitle() + "']").getString("path");
		try (Writer writer = new FileWriter(file)) {
		    Gson gson = new GsonBuilder().create();
		    gson.toJson(vocabulary, writer);
		    log.debug("Vocabulary " + vocabulary.getTitle() + " saved in file " + file);
		} catch (IOException e) {
			log.error("Problem while saving the vocabulary to file " + file, e);
			Helper.setFehlerMeldung("Problem while saving the vocabulary to file " + file, e);
		}
	}
	
	/**
	 * add a new empty record to the vocabulary
	 */
	public void addNewRecord() {
		List <Field> fields = new ArrayList<>();
		for (Definition d : definitions) {
			fields.add(new Field(d.getLabel(), "",d));
		}
		
		record = new VocabRecord(String.valueOf(System.currentTimeMillis()), fields);
		vocabulary.getRecords().add(record);
	}
	
	/**
	 * remove the given record from the vocabulary
	 * @param record the record to remove from vocabulary
	 */
	public void deleteRecord (VocabRecord record) {
		vocabulary.getRecords().remove(record);
		showFirstRecord();
	}
	
	/**
	 * find correct definition for a given field
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
		
		List <Field> fields1 = new ArrayList<>();
		fields1.add(new Field("Title", "Telefon",getDefinitionForLabel("Title")));
		fields1.add(new Field("Translation", "telephone",getDefinitionForLabel("Translation")));
		fields1.add(new Field("Use case", "Speaking",getDefinitionForLabel("Use case")));
		fields1.add(new Field("Description", "Thing that rings to speak on the phone",getDefinitionForLabel("Description")));
		fields1.add(new Field("Days", "Tuesday",getDefinitionForLabel("Days")));
		fields1.add(new Field("Keywords", "blue",getDefinitionForLabel("Keywords")));
		vocabulary.getRecords().add(new VocabRecord("r1", fields1));
		
		List <Field> fields2 = new ArrayList<>();
		fields2.add(new Field("Title", "Fax",getDefinitionForLabel("Title")));
		fields2.add(new Field("Translation", "fax",getDefinitionForLabel("Translation")));
		fields2.add(new Field("Use case", "Send documents",getDefinitionForLabel("Use case")));
		fields2.add(new Field("Description", "Thing that is used to send documents electronically",getDefinitionForLabel("Description")));
		fields2.add(new Field("Days", "Wednesday",getDefinitionForLabel("Days")));
		fields2.add(new Field("Keywords", "red",getDefinitionForLabel("Keywords")));
		vocabulary.getRecords().add(new VocabRecord("r2", fields2));
		
		List <Field> fields3 = new ArrayList<>();
		fields3.add(new Field("Title", "Modem",getDefinitionForLabel("Title")));
		fields3.add(new Field("Translation", "modem",getDefinitionForLabel("Translation")));
		fields3.add(new Field("Use case", "Connect to internet via telephone line",getDefinitionForLabel("Use case")));
		fields3.add(new Field("Description", "Connect computer or other device to the internet via telephone line",getDefinitionForLabel("Description")));
		fields3.add(new Field("Days", "Thursday",getDefinitionForLabel("Days")));
		fields3.add(new Field("Keywords", "green",getDefinitionForLabel("Keywords")));
		vocabulary.getRecords().add(new VocabRecord("r3", fields3));
		
	}
}
