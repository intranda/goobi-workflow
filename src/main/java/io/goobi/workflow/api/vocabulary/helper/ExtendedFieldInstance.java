package io.goobi.workflow.api.vocabulary.helper;

import de.sub.goobi.forms.SpracheForm;
import de.sub.goobi.helper.Helper;
import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.FieldInstance;
import io.goobi.vocabulary.exchange.FieldType;
import io.goobi.vocabulary.exchange.FieldValue;
import io.goobi.vocabulary.exchange.TranslationDefinition;
import io.goobi.vocabulary.exchange.TranslationInstance;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.goobi.managedbeans.FormInputMultiSelectBean;
import org.goobi.managedbeans.FormInputMultiSelectHelper;

import javax.faces.model.SelectItem;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.goobi.workflow.api.vocabulary.helper.ExtendedTranslationInstance.transformToThreeCharacterAbbreviation;

@Getter
@Log4j2
public class ExtendedFieldInstance extends FieldInstance {
    private Function<Long, ExtendedVocabularyRecord> recordResolver = VocabularyAPIManager.getInstance().vocabularyRecords()::get;
    private Function<Long, List<ExtendedVocabularyRecord>> recordsResolver = this::getAllRecords;
    private Function<Long, FieldDefinition> definitionResolver = VocabularyAPIManager.getInstance().vocabularySchemas()::getDefinition;
    private Function<Long, FieldType> typeResolver = VocabularyAPIManager.getInstance().fieldTypes()::get;
    private Supplier<String> languageSupplier = () -> Optional.ofNullable(Helper.getLanguageBean())
            .map(SpracheForm::getLocale)
            .map(Locale::getLanguage)
            .orElse(null);

    private FieldDefinition definition;
    private FieldType type;

    private FormInputMultiSelectBean selectionBean;
    private List<SelectItem> selectableItems;

    private List<ExtendedVocabularyRecord> getAllRecords(Long vocabularyId) {
        return VocabularyAPIManager.getInstance().vocabularyRecords()
                .list(vocabularyId)
                .all()
                .request()
                .getContent();
    }

    public ExtendedFieldInstance(FieldInstance orig) {
        setId(orig.getId());
        setRecordId(orig.getRecordId());
        setDefinitionId(orig.getDefinitionId());
        setValues(orig.getValues());

        postInit();
        prepareEmpty();
        sortTranslations();
    }

    private void postInit() {
        this.definition = definitionResolver.apply(getDefinitionId());
        if (this.definition.getTypeId() != null) {
            this.type = typeResolver.apply(this.definition.getTypeId());
        }
        if (this.type != null) {
            this.selectableItems = this.type.getSelectableValues().stream()
                    .map(v -> new SelectItem(v, v))
                    .collect(Collectors.toList());
        } else if (this.definition.getReferenceVocabularyId() != null) {
            this.selectableItems = recordsResolver.apply(this.definition.getReferenceVocabularyId()).stream()
                    .map(r -> new SelectItem(Long.toString(r.getId()), r.getMainValue()))
                    .collect(Collectors.toList());
        }
        this.selectionBean = new FormInputMultiSelectHelper(() -> this.selectableItems, this::getSelection, this::setSelection);
    }

    private void prepareEmpty() {
        if (getValues().isEmpty()) {
            getValues().add(new ExtendedFieldValue(new FieldValue(), definition.getTranslationDefinitions()));
        }
    }

    public FormInputMultiSelectBean getSelectionBean() {
        return this.selectionBean;
    }

    public List<SelectItem> getSelection() {
        if (this.selectableItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<SelectItem> selection = new LinkedList<>();
        for (String selectedValue : getValues().stream()
                .flatMap(v -> v.getTranslations().stream())
                .map(TranslationInstance::getValue)
                .filter(v -> !v.isBlank() && !v.equals("null"))
                .collect(Collectors.toList())) {
            // TODO: Single selects are directly bound to value translations and might set "null"
            SelectItem item = this.selectableItems.stream()
                    .filter(i -> i.getValue().equals(selectedValue))
                    .findFirst()
                    .orElseThrow();
            selection.add(item);
        }
        return selection;
    }

    public void setSelection(List<SelectItem> selection) {
        List<String> selectedValues = selection.stream()
                .map(s -> (String) s.getValue())
                .collect(Collectors.toList());
        getValues().clear();
        selectedValues.forEach(v -> {
            FieldValue fieldValue = addFieldValue();
            fieldValue.getTranslations().get(0).setValue(v);
        });
    }

    private void sortTranslations() {
        getValues().forEach(this::sortTranslations);
    }

    private void sortTranslations(FieldValue value) {
        Collections.sort(value.getTranslations(), Comparator.comparing(TranslationInstance::getLanguage));
    }

    public FieldValue addFieldValue() {
        FieldValue value = new ExtendedFieldValue(new FieldValue(), definition.getTranslationDefinitions());
        getValues().add(value);
        prepareEmpty();
        sortTranslations(value);
        return value;
    }

    public void deleteFieldValue(FieldValue value) {
        getValues().remove(value);
    }

    public String getFieldValue() {
        return getFieldValue(transformToThreeCharacterAbbreviation(this.languageSupplier.get()));
    }

    public String getFieldValue(String language) {
        String value = extractValue(this, language);
        if (!value.isBlank() && definition.getReferenceVocabularyId() != null) {
            // Process multi-valued referenced fields
            List<Long> ids = Arrays.stream(value.strip().split("\\|"))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            List<String> values = new LinkedList<>();
            for (long id : ids) {
                ExtendedVocabularyRecord result = new ExtendedVocabularyRecord(recordResolver.apply(id));
                values.add(result.getMainValue());
            }
            value = String.join("|", values);
        }
        return value;
    }

    public void setFieldValue(String value) {
        getValues().clear();
        FieldValue fieldValue = addFieldValue();
        fieldValue.getTranslations().forEach(t -> t.setValue(value));
    }

    private String extractValue(FieldInstance field, String language) {
        return field.getValues().stream()
                .map(
                        v -> {
                            Optional<String> preferredLanguage = v.getTranslations().stream()
                                    .filter(t -> language != null && language.equals(t.getLanguage()))
                                    .map(TranslationInstance::getValue)
                                    .findFirst();
                            if (preferredLanguage.isPresent() && !preferredLanguage.get().isBlank()) {
                                return preferredLanguage.get();
                            }
                            String fallbackLanguage = definition.getTranslationDefinitions().stream()
                                    .filter(t -> Boolean.TRUE.equals(t.getFallback()))
                                    .map(TranslationDefinition::getLanguage)
                                    .findFirst()
                                    .orElse(null);
                            if (fallbackLanguage == null) {
                                return v.getTranslations().stream()
                                        .filter(t -> t.getLanguage() == null)
                                        .map(TranslationInstance::getValue)
                                        .findFirst()
                                        .orElseThrow();
                            }
                            return v.getTranslations().stream()
                                    .filter(t -> fallbackLanguage.equals(t.getLanguage()))
                                    .map(TranslationInstance::getValue)
                                    .findFirst()
                                    .orElseThrow();
                        }
                ).collect(Collectors.joining("|"));
    }

    public List<ExtendedFieldValue> getExtendedValues() {
        return getValues().stream()
                .map(v -> new ExtendedFieldValue(v, definition.getTranslationDefinitions()))
                .collect(Collectors.toList());
    }
}
