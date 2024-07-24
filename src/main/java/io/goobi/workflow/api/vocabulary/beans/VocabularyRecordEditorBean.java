package io.goobi.workflow.api.vocabulary.beans;

import io.goobi.vocabulary.exchange.FieldInstance;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.workflow.api.vocabulary.helper.ExtendedFieldInstance;

import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@FacesComponent("io.goobi.workflow.api.vocabulary.beans.VocabularyRecordEditorBean")
public class VocabularyRecordEditorBean extends UINamingContainer {
    private enum PropertyKeys {
        sortedExtendedFields
    }

    private Optional<List<ExtendedFieldInstance>> extendedVocabularyRecord = Optional.empty();

    public void init() {
        VocabularyRecord r = (VocabularyRecord) getAttributes().get("record");
        if (r != null && r.getFields() != null) {
            extendedVocabularyRecord =  Optional.of(r.getFields().stream()
                    .sorted(Comparator.comparingLong(FieldInstance::getDefinitionId))
                    .map(ExtendedFieldInstance::new)
                    .collect(Collectors.toList()));
        }
    }

    public List<ExtendedFieldInstance> getSortedExtendedFields() {
        return extendedVocabularyRecord.orElse(Collections.emptyList());
    }
}
