package org.goobi.vocabulary;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class Vocabulary {
    
    private Integer id;
    private String title;
    private String description;
    private List<VocabRecord> records;
    private ArrayList<Definition> struct;
}
