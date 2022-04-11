package org.goobi.vocabulary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@XmlRootElement
@NoArgsConstructor
public class Definition {

    private Integer id;

    // e.g. 'title'
    @NonNull
    private String label;

    // e.g. 'eng', 'ger'
    @NonNull
    private String language;

    // field type:  "input", "textarea", "select", "html"
    @NonNull
    private String type;

    // regular expression
    @NonNull
    private String validation;

    // define if the field is required

    private boolean required;

    // define if the field is the main entry

    private boolean mainEntry;

    // define if the field value must be unique within the vocabulary

    private boolean distinctive;

    private boolean titleField;

    // possible values to select
    private List<String> selecteableValues;

    public Definition(String label, String language, String type, String validation, boolean required, boolean mainEntry, boolean distinctive,
            boolean titleField) {
        this.label = label;
        this.language = language;
        this.type = type;
        this.validation = validation;
        this.required = required;
        this.mainEntry = mainEntry;
        this.distinctive = distinctive;
        this.titleField = titleField;

    }

    @JsonIgnore
    public String getSelection() {
        StringBuilder sb = new StringBuilder();
        if (selecteableValues != null && !selecteableValues.isEmpty()) {
            for (String string : selecteableValues) {
                if (sb.length() > 0) {
                    sb.append("|");
                }
                sb.append(string);
            }
        }
        return sb.toString();

    }

    @JsonIgnore
    public void setSelection(String value) {
        if (StringUtils.isNotBlank(value)) {
            selecteableValues = Arrays.asList(value.split("\\|"));
        } else {
            selecteableValues = null;
        }

    }

    @JsonIgnore
    public List<SelectItem> getSelectList() {
        List<SelectItem> list = new ArrayList<>();
        for (String s : selecteableValues) {
            list.add(new SelectItem(s, s, null));
        }
        return list;
    }

    @JsonIgnore
    public String getIdAsString() {
        if (id == null) {
            return "";
        }
        return String.valueOf(id);
    }

}
