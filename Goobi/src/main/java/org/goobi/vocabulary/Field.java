package org.goobi.vocabulary;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@XmlRootElement
@NoArgsConstructor
public class Field {

    private Integer id;

    private String label;

    private String language;

    private String value;

    private Integer definitionId;

    private transient Definition definition;

    public Field(String label, String language, String value, Definition definition) {
        this.label = label;
        this.language = language;
        this.value = value;
        this.definition = definition;
    }

    @JsonIgnore
    private transient String validationMessage;

    /**
     * Simple getter to allow reading the current value as multi select field
     * 
     * @return
     */
    @JsonIgnore
    public String[] getValueMultiSelect() {
        return value.split("\\|");
    }

    /**
     * simple setter to allow writing information as multi select field
     * 
     * @param data
     */
    @JsonIgnore
    public void setValueMultiSelect(String[] data) {
        StringBuilder sb = new StringBuilder();
        if (data != null && data.length > 0) {
            for (int i = 0; i < data.length; i++) {
                if (sb.length() > 0) {
                    sb.append("|");
                }
                sb.append(data[i]);
            }
        }
        value = sb.toString();
    }

    @JsonIgnore
    public String getDisplayLanguageKey() {
        if (StringUtils.isBlank(language)) {
            return "";
        }
        return "vocabularyManager_language_abbreviation_" + language;
    }
}
