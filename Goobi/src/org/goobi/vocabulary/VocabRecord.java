package org.goobi.vocabulary;

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
public class VocabRecord implements DatabaseObject {
    private Integer id;
    private Integer vocabularyId;
    private List<Field> fields;

    @JsonIgnore
    public String getTitle() {
        for (Field field : fields) {
            if (field.getDefinition().getMainEntry()) {
                return field.getValue();
            }
        }
        return "";
    }

    @Override
    public void lazyLoad() {
    }

}
