package io.goobi.workflow.api.vocabulary.helper;

import io.goobi.vocabulary.exchange.VocabularyRecord;
import lombok.Getter;

import java.util.Random;

@Getter
public class ExtendedVocabularyRecord extends VocabularyRecord {
    private int level;

    public ExtendedVocabularyRecord(VocabularyRecord vocabularyRecord) {
        System.err.println(vocabularyRecord);
        this.level = new Random().nextInt(5);
    }
}
