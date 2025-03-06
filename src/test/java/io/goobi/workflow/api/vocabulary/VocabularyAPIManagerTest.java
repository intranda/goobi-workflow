package io.goobi.workflow.api.vocabulary;

import org.junit.Test;

public class VocabularyAPIManagerTest {
    @Test
    public void retrieveAllAPIs() {
        VocabularyAPIManager.getInstance().fieldTypes();
        VocabularyAPIManager.getInstance().vocabularies();
        VocabularyAPIManager.getInstance().vocabularySchemas();
        VocabularyAPIManager.getInstance().vocabularyRecords();
        VocabularyAPIManager.getInstance().languages();
    }
}
