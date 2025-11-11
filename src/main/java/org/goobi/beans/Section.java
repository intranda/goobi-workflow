package org.goobi.beans;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Named
@SessionScoped
public class Section implements Serializable {
    private static final long serialVersionUID = 650242038L;

    private Map<String, Boolean> sections = new HashMap<>();

    public Boolean isCollapsed(String sectionId) {
        return sections.get(sectionId);
    }

    public boolean isCollapsed(String sectionId, boolean defaultValue) {
        Boolean collapsed = sections.get(sectionId);
        return collapsed != null ? collapsed : defaultValue;
    }

    public void toggle(String sectionId) {
        sections.put(sectionId, !isCollapsed(sectionId));
    }

    public void toggle(String sectionId, boolean defaultValue) {
        Boolean currentState = isCollapsed(sectionId);
        if (currentState == null) {
            sections.put(sectionId, !defaultValue);
        } else {
            sections.put(sectionId, !currentState);
        }
    }
}
