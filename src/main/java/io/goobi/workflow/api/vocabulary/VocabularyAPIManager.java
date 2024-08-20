package io.goobi.workflow.api.vocabulary;

import de.sub.goobi.config.ConfigurationHelper;

public class VocabularyAPIManager {
    private static VocabularyAPIManager instance;

    private FieldTypeAPI fieldTypeAPI;
    private LanguageAPI languageAPI;
    private VocabularySchemaAPI vocabularySchemaAPI;
    private VocabularyAPI vocabularyAPI;
    private VocabularyRecordAPI vocabularyRecordAPI;

    private VocabularyAPIManager() {
        final String host = ConfigurationHelper.getInstance().getVocabularyServerHost();
        final int port = ConfigurationHelper.getInstance().getVocabularyServerPort();
        this.fieldTypeAPI = new FieldTypeAPI(host, port);
        this.languageAPI = new LanguageAPI(host, port);
        this.vocabularySchemaAPI = new VocabularySchemaAPI(host, port);
        this.vocabularyAPI = new VocabularyAPI(host, port);
        this.vocabularyRecordAPI = new VocabularyRecordAPI(host, port);
    }

    public synchronized static VocabularyAPIManager getInstance() {
        if (instance == null) {
            instance = new VocabularyAPIManager();
        }
        return instance;
    }

    public FieldTypeAPI fieldTypes() {
        return fieldTypeAPI;
    }

    public LanguageAPI languages() {
        return languageAPI;
    }

    public VocabularySchemaAPI vocabularySchemas() {
        return vocabularySchemaAPI;
    }

    public VocabularyAPI vocabularies() {
        return vocabularyAPI;
    }

    public VocabularyRecordAPI vocabularyRecords() {
        return vocabularyRecordAPI;
    }
}
