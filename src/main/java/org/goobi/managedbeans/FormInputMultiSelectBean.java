package org.goobi.managedbeans;

import javax.faces.model.SelectItem;
import java.util.List;

public interface FormInputMultiSelectBean {
    List<SelectItem> getCurrentlySelectableItems();
    List<SelectItem> getSelection();
    void setSelection(List<SelectItem> selection);
    String getCurrentSelection();
    void setCurrentSelection(String item);
    void removeSelection(SelectItem selection);
}
