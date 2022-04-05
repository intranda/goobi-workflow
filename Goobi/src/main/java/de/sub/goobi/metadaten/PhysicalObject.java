package de.sub.goobi.metadaten;

import de.sub.goobi.helper.Helper;
import lombok.Data;
import ugh.dl.DocStruct;

@Data
public class PhysicalObject {

    private boolean selected;
    private DocStruct docStruct;
    private String physicalPageNo;
    private String logicalPageNo;
    private String type;
    private String imagename;
    private String coordinates;

    private boolean representative;

    private boolean doublePage;

    public String getLabel() {
        if ("div".equals(type)) {
            return physicalPageNo + ": " + logicalPageNo;
        } else {
            return Helper.getTranslation("mets_pageArea", logicalPageNo);
        }
    }
}
