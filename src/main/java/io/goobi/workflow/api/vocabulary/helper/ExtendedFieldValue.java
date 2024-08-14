package io.goobi.workflow.api.vocabulary.helper;

import io.goobi.vocabulary.exchange.FieldValue;
import io.goobi.vocabulary.exchange.TranslationDefinition;
import io.goobi.vocabulary.exchange.TranslationInstance;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Log4j2
public class ExtendedFieldValue extends FieldValue {
    private List<ExtendedTranslationInstance> extendedTranslations;

    ExtendedFieldValue(FieldValue orig, Set<TranslationDefinition> definitions) {
        setId(orig.getId());
        setFieldId(orig.getFieldId());
        setTranslations(orig.getTranslations());
        set_links(orig.get_links());

        postInit(definitions);
    }

    private void postInit(Set<TranslationDefinition> definitions) {
        prepareEmpty(definitions);

        Map<String, TranslationDefinition> lookup = definitions.stream()
                .filter(d -> d.getLanguage() != null)
                .collect(Collectors.toMap(TranslationDefinition::getLanguage, Function.identity()));
        this.extendedTranslations = getTranslations().stream()
                .map(t -> new ExtendedTranslationInstance(t, lookup.getOrDefault(t.getLanguage(), null)))
                .collect(Collectors.toList());
    }

    private void prepareEmpty(Set<TranslationDefinition> definitions) {
        if (!definitions.isEmpty()) {
            definitions.stream()
                    .filter(t -> getTranslations().stream().noneMatch(t2 -> t2.getLanguage().equals(t.getLanguage())))
                    .forEach(t -> {
                        TranslationInstance translation = new TranslationInstance();
                        translation.setLanguage(t.getLanguage());
                        translation.setValue("");
                        getTranslations().add(translation);
                    });
        } else if (getTranslations().isEmpty()) {
            TranslationInstance translation = new TranslationInstance();
            translation.setValue("");
            getTranslations().add(translation);
        }
    }
}
