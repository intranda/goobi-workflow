package org.goobi.vocabulary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.goobi.beans.DatabaseObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlRootElement
public class Vocabulary implements Serializable, DatabaseObject {

    /**
     * 
     */
    private static final long serialVersionUID = -86569570995051824L;

    private Integer id;
    private String title;
    private String description;
    private List<VocabRecord> records = new ArrayList<>();
    private List<Definition> struct = new ArrayList<>();

    @JsonIgnore
    private String url;

    @Override
    public void lazyLoad() {
    }

    // TODO: paginator for records list?

}
