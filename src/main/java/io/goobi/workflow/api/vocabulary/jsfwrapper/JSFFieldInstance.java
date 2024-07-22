package io.goobi.workflow.api.vocabulary.jsfwrapper;

import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.FieldInstance;
import io.goobi.vocabulary.exchange.FieldType;
import io.goobi.vocabulary.exchange.FieldValue;
import io.goobi.vocabulary.exchange.TranslationInstance;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import lombok.Getter;
import lombok.Setter;

import javax.faces.model.SelectItem;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JSFFieldInstance extends FieldInstance {
    @Setter
    private String language;
    @Getter
    private List<SelectItem> allSelectableItems;
    @Getter
    private List<SelectItem> currentlySelectableItems;
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
        String label = allSelectableItems.stream()
                .filter(i -> i.getValue().equals(newValue))
                .findFirst()
                .orElseThrow()
                .getLabel();
        allSelectedValues.put(label, newValue);
        allSelectedValueKeys = allSelectedValues.keySet().stream()
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());
        currentlySelectableItems = allSelectableItems.stream()
                .filter(i -> !allSelectedValues.containsValue(i.getValue()))
                .collect(Collectors.toList());
        getValues().clear();
        getValues().addAll(allSelectedValues.values().stream()
                .map(v -> {
                    TranslationInstance ti = new TranslationInstance();
                    ti.setValue(v);
                    FieldValue fv = new FieldValue();
                    fv.setTranslations(new LinkedList<>(List.of(ti)));
                    return fv;
                })
                .collect(Collectors.toSet()));
    }

    @Getter
    private Map<String, String> allSelectedValues = new HashMap<>();

    @Getter
    private List<String> allSelectedValueKeys = Collections.emptyList();

    public void removeSelectedValue(String selection) {
        allSelectedValues.remove(selection);
        allSelectedValueKeys = allSelectedValues.keySet().stream()
                .collect(Collectors.toList());
        currentlySelectableItems = allSelectableItems.stream()
                .filter(i -> !allSelectedValues.containsValue(i.getValue()))
                .collect(Collectors.toList());
        getValues().clear();
        getValues().addAll(allSelectedValues.values().stream()
                .map(v -> {
                    TranslationInstance ti = new TranslationInstance();
                    ti.setValue(v);
                    FieldValue fv = new FieldValue();
                    fv.setTranslations(new LinkedList<>(List.of(ti)));
                    return fv;
                })
                .collect(Collectors.toSet()));
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
            allSelectableItems = type.getSelectableValues().stream()
                    .map(SelectItem::new)
                    .collect(Collectors.toList());
        } else if (definition.getReferenceVocabularyId() != null) {
            allSelectableItems = api.vocabularyRecords().all(definition.getReferenceVocabularyId()).stream()
                    .map(r -> {
                        // TODO: FIX
//                        r.setLanguage(language);
//                        r.load(null);
//                        return new SelectItem(String.valueOf(r.getId()), r.getMainValue());
                        return new SelectItem(null);
                    })
                    .collect(Collectors.toList());
            if (Boolean.TRUE.equals(definition.getMultiValued())) {
                for (String selectedValue : getValues().stream()
                        .flatMap(v -> v.getTranslations().stream())
                        .map(TranslationInstance::getValue)
                        .filter(v -> !v.isBlank())
                        .collect(Collectors.toList())) {
                    String label = allSelectableItems.stream()
                            .filter(i -> i.getValue().equals(selectedValue))
                            .findFirst()
                            .orElseThrow()
                            .getLabel();
                    allSelectedValues.put(label, selectedValue);
                }
                allSelectedValueKeys = allSelectedValues.keySet().stream()
                        .sorted(String::compareToIgnoreCase)
                        .collect(Collectors.toList());
            }
        }
        currentlySelectableItems = allSelectableItems.stream()
                .filter(i -> !allSelectedValues.containsValue(i.getValue()))
                .collect(Collectors.toList());
    }
}
