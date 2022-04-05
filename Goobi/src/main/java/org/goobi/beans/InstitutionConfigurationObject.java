package org.goobi.beans;

import lombok.Data;

@Data
public class InstitutionConfigurationObject {
    private Integer id;
    private Integer institution_id;
    private Integer object_id;
    private String object_type;
    private String object_name;
    private boolean selected;
}
