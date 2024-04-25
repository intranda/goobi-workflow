package io.goobi.workflow.api.vocabulary.objects;

import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import org.goobi.beans.DatabaseObject;

import java.util.List;
import java.util.stream.Collectors;

public class VocabularyRecordDO extends VocabularyRecord implements DatabaseObject {
    @Override
    public void lazyLoad() {
        // REST objects don't require lazy loading
    }

    // TODO: Not clean to perform this logic here!
    public List<String> getTitleValues() {
        long schemaId = VocabularyAPIManager.getInstance().vocabularies().get(this.getVocabularyId()).getSchemaId();
        List<Long> definitionIds = VocabularyAPIManager.getInstance().vocabularySchemas().get(schemaId).getDefinitions().stream()
                .filter(d -> Boolean.TRUE.equals(d.getMainEntry()))
                .map(FieldDefinition::getId)
                .collect(Collectors.toList());
        return this.getFields().stream()
                .filter(f -> definitionIds.contains(f.getDefinitionId()))
                // TODO: Correct language
                .flatMap(f -> f.getValues().stream())
                .flatMap(v -> v.getTranslations().values().stream())
                .collect(Collectors.toList());
    }
}
