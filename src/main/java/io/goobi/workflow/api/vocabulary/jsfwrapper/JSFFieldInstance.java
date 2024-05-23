package io.goobi.workflow.api.vocabulary.jsfwrapper;

import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.FieldInstance;
import io.goobi.vocabulary.exchange.FieldType;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import lombok.Getter;

import javax.faces.model.SelectItem;
import java.util.List;
import java.util.stream.Collectors;

public class JSFFieldInstance extends FieldInstance {
    @Getter
    private List<SelectItem> selectableItems;
    @Getter
    private FieldDefinition definition;
    @Getter
    private FieldType type;

    private VocabularyAPIManager api = VocabularyAPIManager.getInstance();

    public JSFFieldInstance(FieldInstance field) {
        setId(field.getId());
        setDefinitionId(field.getDefinitionId());
        setRecordId(field.getRecordId());
        setValues(field.getValues());
        set_links(field.get_links());
    }

    public void load(FieldDefinition definition, FieldType type) {
        this.definition = definition;
        this.type = type;
        if (type != null) {
            selectableItems = type.getSelectableValues().stream()
                    .map(SelectItem::new)
                    .collect(Collectors.toList());
        } else if (definition.getReferenceVocabularyId() != null) {
            selectableItems = api.vocabularyRecords().all(definition.getReferenceVocabularyId()).stream()
                    .map(r -> new SelectItem(String.valueOf(r.getId()), r.getMainValue()))
                    .collect(Collectors.toList());
        }
    }
}
