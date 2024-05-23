package io.goobi.workflow.api.vocabulary.jsfwrapper;

import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.FieldInstance;
import io.goobi.vocabulary.exchange.FieldType;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import lombok.Getter;
import lombok.Setter;

import javax.faces.model.SelectItem;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JSFFieldInstance extends FieldInstance {
    @Setter
    private String language;
    @Getter
    private List<SelectItem> selectableItems;
    @Getter
    private FieldDefinition definition;
    @Getter
    private FieldType type;
    @Getter
    private String currentValue;
    public void setCurrentValue(String newValue) {
        if (newValue.isBlank() || allSelectedValues.containsKey(newValue)) {
            return;
        }
        String label = selectableItems.stream()
                .filter(i -> i.getValue().equals(newValue))
                .findFirst()
                .orElseThrow()
                .getLabel();
        allSelectedValues.put(label, newValue);
        allSelectedValueKeys = allSelectedValues.keySet().stream()
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());
    }

    private Map<String, String> allSelectedValues = new HashMap<>();

    @Getter
    private List<String> allSelectedValueKeys = Collections.emptyList();

    public void removeSelectedValue(String selection) {
        allSelectedValues.remove(selection);
        allSelectedValueKeys = allSelectedValues.keySet().stream()
                .collect(Collectors.toList());
    }

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
                    .map(r -> {
                        r.setLanguage(language);
                        r.load(null);
                        return new SelectItem(String.valueOf(r.getId()), r.getMainValue());
                    })
                    .collect(Collectors.toList());
        }
    }
}
