package org.goobi.managedbeans;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IThemePlugin;

import java.io.Serializable;
import java.util.List;

@Named("ThemeBean")
@WindowScoped
@Data
@EqualsAndHashCode(callSuper = false)
@Log4j2
public class ThemeBean implements Serializable {
    private List<IThemePlugin> themePlugins;

    @PostConstruct
    public void initialize() {
        themePlugins = PluginLoader.getPluginList(PluginType.Theme).stream()
                .filter(IThemePlugin.class::isInstance)
                .map(p -> (IThemePlugin) p)
                .sorted((p1, p2) -> {
                    if ("IntrandaThemeCore".equals(p1.getId())) return -1;
                    if ("IntrandaThemeCore".equals(p2.getId())) return 1;
                    return 0;
                })
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
