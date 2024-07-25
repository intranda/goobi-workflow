package org.goobi.managedbeans;

import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@FacesComponent("org.goobi.managedbeans.FormInputDropdownBadgesBean")
public class FormInputDropdownBadgesBean extends UINamingContainer {
    public String getCurrentSelection() {
        return null;
    }

    public void setCurrentSelection(String item) {
        if (item == null || item.isBlank()) {
            return;
        }

        Optional<SelectItem> selectedItem = getSelectableItems().stream()
                .filter(s -> s.getValue().equals(item))
                .findFirst();

        if (selectedItem.isEmpty()) {
            System.err.println("Unable to find selected item: " + item);
            return;
        }

        List<SelectItem> selectedItems = getSelectedItems();
        selectedItems.add(selectedItem.get());
        System.err.println(selectedItems);
        setSelectedItems(selectedItems);
    }

    public void removeSelection(SelectItem selection) {
        List<SelectItem> selectedItems = getSelectedItems();
        selectedItems.removeIf(i -> i.getValue().equals(selection.getValue()));
//        setSelectedItems(selectedItems);
    }

    public List<SelectItem> getCurrentlySelectableItems() {
        List<SelectItem> currentlySelectable = new ArrayList<>(getSelectableItems());
        getSelectedItems().forEach(selected -> currentlySelectable.removeIf(i -> i.getValue().equals(selected.getValue())));
        return currentlySelectable;
    }

    private List<SelectItem> getSelectableItems() {
        return (List<SelectItem>) getAttributes().get("selectableItems");
    }

    private List<SelectItem> getSelectedItems() {
        return (List<SelectItem>) getAttributes().get("selectedItems");
    }

    private void setSelectedItems(List<SelectItem> items) {
        getAttributes().put("selectedItems", items);
    }
}
