package org.goobi.managedbeans;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FormInputMultiSelectHelper implements FormInputMultiSelectBean {
    private Supplier<List<SelectItem>> all;
    private Supplier<List<SelectItem>> getter;
    private Consumer<List<SelectItem>> setter;

    public FormInputMultiSelectHelper(Supplier<List<SelectItem>> all, Supplier<List<SelectItem>> getter, Consumer<List<SelectItem>> setter) {
        this.all = all;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public List<SelectItem> getCurrentlySelectableItems() {
        List<SelectItem> currentlySelectable = new ArrayList<>(all.get());
        getSelection().forEach(selected -> currentlySelectable.removeIf(i -> i.getValue().equals(selected.getValue())));
        return currentlySelectable;
    }

    @Override
    public List<SelectItem> getSelection() {
        return this.getter.get();
    }

    @Override
    public void setSelection(List<SelectItem> selection) {
        this.setter.accept(selection);
    }

    @Override
    public String getCurrentSelection() {
        return null;
    }

    @Override
    public void setCurrentSelection(String item) {
        if (item == null || item.isBlank()) {
            return;
        }

        Optional<SelectItem> selectedItem = this.all.get().stream()
                .filter(s -> s.getValue().equals(item))
                .findFirst();

        if (selectedItem.isEmpty()) {
            System.err.println("Unable to find selected item: " + item);
            return;
        }

        List<SelectItem> selectedItems = getSelection();
        selectedItems.add(selectedItem.get());
        setSelection(selectedItems);
    }

    @Override
    public void removeSelection(SelectItem selection) {
        List<SelectItem> selectedItems = getSelection();
        selectedItems.removeIf(i -> i.getValue().equals(selection.getValue()));
        setSelection(selectedItems);
    }
}
