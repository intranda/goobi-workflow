package org.goobi.managedbeans;

import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean(name = "ThemeBean")
@ApplicationScoped
public class ThemeBean {

    private List<String> themes;

    private String currentTheme = "aristo";

    public ThemeBean() {
        themes = new ArrayList<>();
        themes.add("aristo");
        themes.add("afterdark");
    }

    public List<String> getThemes() {
        return themes;
    }

    public String getCurrentTheme() {
        return currentTheme;
    }
}