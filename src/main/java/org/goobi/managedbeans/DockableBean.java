package org.goobi.managedbeans;

import de.sub.goobi.helper.Helper;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IDockablePlugin;

import java.io.Serializable;
import java.util.List;

@Named("DockableBean")
@WindowScoped
@Data
@EqualsAndHashCode(callSuper = false)
@Log4j2
public class DockableBean implements Serializable {
    private List<IDockablePlugin> dockablePlugins;

    @PostConstruct
    public void initialize() {
        dockablePlugins = PluginLoader.getPluginList(PluginType.Dockable).stream()
                .filter(IDockablePlugin.class::isInstance)
                .map(p -> (IDockablePlugin) p)
                .toList();
        dockablePlugins.forEach(p -> {
            try {
                p.initialize();
            } catch (Exception e) {
                Helper.setFehlerMeldung("An exception occurred during plugin initialization: " + p.getTitle());
                log.error("An exception occurred during plugin initialization: {}", p.getTitle(), e);
            }
        });
    }
}
