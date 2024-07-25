package io.goobi.workflow.api.vocabulary.helper;

import de.sub.goobi.helper.Helper;
import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.FieldInstance;
import io.goobi.vocabulary.exchange.FieldType;
import io.goobi.vocabulary.exchange.FieldValue;
import io.goobi.vocabulary.exchange.TranslationDefinition;
import io.goobi.vocabulary.exchange.TranslationInstance;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import lombok.Getter;

import javax.faces.model.SelectItem;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
public class ExtendedFieldInstance extends FieldInstance {
    private Function<Long, VocabularyRecord> recordResolver = VocabularyAPIManager.getInstance().vocabularyRecords()::get;
    private Function<Long, FieldDefinition> definitionResolver = VocabularyAPIManager.getInstance().vocabularySchemas()::getDefinition;
    private Function<Long, FieldType> typeResolver = VocabularyAPIManager.getInstance().fieldTypes()::get;
    private Supplier<String> languageSupplier = Helper.getLanguageBean().getLocale()::getLanguage;

    private FieldDefinition definition;
    private FieldType type;
    private List<SelectItem> allSelectableItems = Collections.emptyList();
    private List<SelectItem> currentlySelectableItems;
    private List<String> allSelectedValueKeys = Collections.emptyList();

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
    }

    private void prepareEmpty() {
        if (getValues().isEmpty()) {
            getValues().add(new FieldValue());
        }
        getValues().forEach(v -> {
            if (!definition.getTranslationDefinitions().isEmpty()) {
                definition.getTranslationDefinitions().stream()
                        .filter(t -> v.getTranslations().stream().noneMatch(t2 -> t2.getLanguage().equals(t.getLanguage())))
                        .forEach(t -> {
                            TranslationInstance translation = new TranslationInstance();
                            translation.setLanguage(t.getLanguage());
                            translation.setValue("");
                            v.getTranslations().add(translation);
                        });
            } else if (v.getTranslations().isEmpty()) {
                TranslationInstance translation = new TranslationInstance();
                translation.setValue("");
                v.getTranslations().add(translation);
            }
        });
    }

    private void sortTranslations() {
        getValues().forEach(v -> Collections.sort(v.getTranslations(), Comparator.comparing(TranslationInstance::getLanguage)));
    }

    public void addFieldValue() {
        getValues().add(new FieldValue());
        prepareEmpty();
    }

    public void deleteFieldValue(FieldValue value) {
        getValues().remove(value);
    }

    public String getFieldValue() {
        // TODO: Decide which language to show
        String value = extractValue(this);
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

    private String extractValue(FieldInstance field) {
        return field.getValues().stream()
                .map(
                        v -> {
                            Optional<String> preferredLanguage = v.getTranslations().stream()
                                    .filter(t -> languageSupplier.get() != null && transformToThreeCharacterAbbreviation(languageSupplier.get()).equals(t.getLanguage()))
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

    private static String transformToThreeCharacterAbbreviation(String language) {
        switch (language) {
            case "en":
                return "eng";
            case "de":
                return "ger";
            case "fr":
                return "fre";
            default:
                throw new IllegalArgumentException("Unknown language: \"" + language + "\"");
        }
    }
}
