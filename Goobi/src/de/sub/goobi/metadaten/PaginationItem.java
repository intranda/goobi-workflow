package de.sub.goobi.metadaten;

import lombok.Data;
import ugh.dl.DocStruct;

@Data
public class PaginationItem {

    private boolean selected;
    private DocStruct docstruct;
    private String physicalPageNo;
    private String logicalPageNo;
    private String type;
    private String imagename;
    private String coordinates;
}
