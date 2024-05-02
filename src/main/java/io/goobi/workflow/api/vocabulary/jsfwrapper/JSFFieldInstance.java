package io.goobi.workflow.api.vocabulary.jsfwrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.goobi.vocabulary.exchange.FieldInstance;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

public class JSFFieldInstance extends FieldInstance {
    @Getter
    @Setter
    private String language;
    @Getter
    @Setter
    private String value;

    @JsonIgnore
    public String getDisplayLanguageKey() {
        if (StringUtils.isBlank(language)) {
            return "";
        }
        return "vocabularyManager_language_abbreviation_" + language;
    }
}
