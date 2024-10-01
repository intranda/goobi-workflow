package io.goobi.workflow.api.vocabulary.helper;

import io.goobi.vocabulary.exchange.Language;
import io.goobi.vocabulary.exchange.TranslationDefinition;
import io.goobi.vocabulary.exchange.TranslationInstance;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.function.Function;

@Getter
@Log4j2
public class ExtendedTranslationInstance extends TranslationInstance {
    private Function<String, Language> languageNameResolver = VocabularyAPIManager.getInstance().languages()::findByAbbreviation;

    private final TranslationInstance wrapped;
    private TranslationDefinition definition;
    private String languageName;

    ExtendedTranslationInstance(TranslationInstance orig, TranslationDefinition definition) {
        this.wrapped = orig;
        this.definition = definition;

        postInit();
    }

    @Override
    public String getLanguage() {
        return wrapped.getLanguage();
    }

    @Override
    public void setLanguage(String language) {
        wrapped.setLanguage(language);
    }

    @Override
    public String getValue() {
        return wrapped.getValue();
    }

    @Override
    public void setValue(String value) {
        wrapped.setValue(value);
    }

    private void postInit() {
        if (getLanguage() != null) {
            this.languageName = languageNameResolver.apply(getLanguage()).getName();
        }
    }

    static String transformToThreeCharacterAbbreviation(String language) {
        if (language == null) {
            return null;
        }
        switch (language) {
            case "en":
                return "eng";
            case "de":
                return "ger";
            case "fr":
                return "fre";
            default:
                log.warn("Unknown language \"{}\", falling back to \"eng\"", language);
                return "eng";
        }
    }
}
