package io.goobi.workflow.api.vocabulary;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*"})
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
