package org.goobi.managedbeans;

import java.util.List;

import jakarta.faces.model.SelectItem;

public interface FormInputMultiSelectBean {
    List<SelectItem> getCurrentlySelectableItems();

    List<SelectItem> getSelection();

    void setSelection(List<SelectItem> selection);

    String getCurrentSelection();

    void setCurrentSelection(String item);

    void removeSelection(SelectItem selection);
}
