package org.goobi.api.rest.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class RestProcess {
    private int id;
    private String name;
    private String ruleset; 
    private Map<String, List<RestMetadata>> metadata;

    public RestProcess(int id) {
        this.id = id;
        this.metadata = new TreeMap<>();
    }

    public void addMetadata(String name, RestMetadata value) {
        List<RestMetadata> values = this.metadata.get(name);
        if (values == null) {
            values = new ArrayList<>();
            this.metadata.put(name, values);
        }
        values.add(value);
    }
}
