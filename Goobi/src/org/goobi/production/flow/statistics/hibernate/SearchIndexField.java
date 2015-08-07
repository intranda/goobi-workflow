package org.goobi.production.flow.statistics.hibernate;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public @Data class SearchIndexField {
    
    
    private String indexName;
    
    private List<String> metadataList; 

}
