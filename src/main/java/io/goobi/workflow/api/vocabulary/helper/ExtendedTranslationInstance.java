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

    private TranslationDefinition definition;
    private String languageName;

    ExtendedTranslationInstance(TranslationInstance orig, TranslationDefinition definition) {
        setValue(orig.getValue());
        setLanguage(orig.getLanguage());
        this.definition = definition;

        postInit();
    }

    private void postInit() {
        if (getLanguage() != null) {
            this.languageName = languageNameResolver.apply(getLanguage()).getName();
        }
    }

    static String transformToThreeCharacterAbbreviation(String language) {
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
