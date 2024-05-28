package org.goobi.beans;

import lombok.Data;

@Data
public class NamedEntity {
    private String label;
    private String type;
    private String uri;
}
