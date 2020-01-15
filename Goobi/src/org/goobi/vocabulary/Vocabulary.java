package org.goobi.vocabulary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.DatabaseObject;

import lombok.Data;

@Data
public class Vocabulary implements Serializable, DatabaseObject {

    /**
     * 
     */
    private static final long serialVersionUID = -86569570995051824L;

    private Integer id;
    private String title;
    private String description;
    private List<VocabRecord> records;
    private ArrayList<Definition> struct;

    @Override
    public void lazyLoad() {
        // TODO load records
    }
}
