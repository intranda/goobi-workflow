package org.goobi.managedbeans;

import java.io.Serializable;
import java.util.List;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IThemePlugin;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Named("ThemeBean")
@WindowScoped
@Data
@EqualsAndHashCode(callSuper = false)

public class ThemeBean implements Serializable {
    private static final long serialVersionUID = 1076070916093079757L;
    private List<IThemePlugin> themePlugins;

    @PostConstruct
    public void initialize() {
        themePlugins = PluginLoader.getPluginList(PluginType.Theme)
                .stream()
                .filter(IThemePlugin.class::isInstance)
                .map(p -> (IThemePlugin) p)
                .toList();
        //        themePlugins.forEach(p -> {
        //            try {
        //                p.initialize();
        //            } catch (Exception e) {
        //                Helper.setFehlerMeldung("An exception occurred during plugin initialization: " + p.getTitle());
        //                log.error("An exception occurred during plugin initialization: {}", p.getTitle(), e);
        //            }
        //        });
    }
}
