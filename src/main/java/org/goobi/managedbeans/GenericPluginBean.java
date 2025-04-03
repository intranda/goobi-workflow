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
import org.goobi.production.plugin.interfaces.IGenericPlugin;

import java.io.Serializable;
import java.util.List;

@Named("GenericPluginBean")
@WindowScoped
@Data
@EqualsAndHashCode(callSuper = false)
@Log4j2
public class GenericPluginBean implements Serializable {
    private List<IGenericPlugin> genericPlugins;

    @PostConstruct
    public void initialize() {
        genericPlugins = PluginLoader.getPluginList(PluginType.Generic).stream()
                .filter(IGenericPlugin.class::isInstance)
                .map(p -> (IGenericPlugin) p)
                .toList();
        genericPlugins.forEach(p -> {
            try {
                p.initialize();
            } catch (Exception e) {
                Helper.setFehlerMeldung("An exception occurred during plugin initialization: " + p.getTitle());
                log.error("An exception occurred during plugin initialization: {}", p.getTitle(), e);
            }
        });
    }
}
