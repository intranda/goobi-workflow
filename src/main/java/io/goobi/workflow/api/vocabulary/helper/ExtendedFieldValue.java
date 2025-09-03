package io.goobi.workflow.api.vocabulary.helper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.goobi.vocabulary.exchange.FieldValue;
import io.goobi.vocabulary.exchange.HateoasHref;
import io.goobi.vocabulary.exchange.TranslationDefinition;
import io.goobi.vocabulary.exchange.TranslationInstance;
import lombok.Getter;

@Getter
public class ExtendedFieldValue extends FieldValue {
    private final FieldValue wrapped;
    private List<ExtendedTranslationInstance> extendedTranslations;

    ExtendedFieldValue(FieldValue orig, Set<TranslationDefinition> definitions) {
        this.wrapped = orig;

        postInit(definitions);
    }

    @Override
    public Long getId() {
        return wrapped.getId();
    }

    @Override
    public void setId(Long id) {
        wrapped.setId(id);
    }

    @Override
    public Long getFieldId() {
        return wrapped.getFieldId();
    }

    @Override
    public void setFieldId(Long fieldId) {
        wrapped.setFieldId(fieldId);
    }

    @Override
    public List<TranslationInstance> getTranslations() {
        return wrapped.getTranslations();
    }

    @Override
    public void setTranslations(List<TranslationInstance> translations) {
        wrapped.setTranslations(translations);
    }

    @Override
    public Map<String, HateoasHref> get_links() {
        return wrapped.get_links();
    }

    @Override
    public void set_links(Map<String, HateoasHref> links) {
        wrapped.set_links(links);
    }

    private void postInit(Set<TranslationDefinition> definitions) {
        prepareEmpty(definitions);

        Map<String, TranslationDefinition> lookup = definitions.stream()
                .filter(d -> d.getLanguage() != null)
                .collect(Collectors.toMap(TranslationDefinition::getLanguage, Function.identity()));
        this.extendedTranslations = getTranslations().stream()
                .map(t -> new ExtendedTranslationInstance(t, lookup.getOrDefault(t.getLanguage(), null)))
                .sorted(Comparator.comparing(TranslationInstance::getLanguage))
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
