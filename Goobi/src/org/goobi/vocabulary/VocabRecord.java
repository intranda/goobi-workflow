package org.goobi.vocabulary;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.goobi.beans.DatabaseObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement
public class VocabRecord implements DatabaseObject{
    private Integer id;
    private List<Field> fields;

    @JsonIgnore
    public String getTitle() {
        for (Field field : fields) {
            if (field.getLabel().toLowerCase().equals("title")) {
                return field.getValue();
            }
        }
        return "";
    }
    @JsonIgnore
    public String getKeywords() {
        for (Field field : fields) {
            if (field.getLabel().toLowerCase().equals("keywords")) {
                return field.getValue();
            }
        }
        return "";
    }
    @JsonIgnore
    public List<String> getAllKeywords() {
        for (Field field : fields) {
            if (field.getLabel().toLowerCase().equals("keywords")) {
                return Arrays.asList(field.getValue().split("\\r?\\n"));
            }
        }
        return Collections.emptyList();
    }

    @JsonIgnore
    public String getDescription() {
        for (Field field : fields) {
            if (field.getLabel().toLowerCase().equals("description")) {
                return field.getValue();
            }
        }
        return "";
    }

    @Override
    public void lazyLoad() {
    }

}
