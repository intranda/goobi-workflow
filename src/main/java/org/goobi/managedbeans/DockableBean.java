package org.goobi.managedbeans;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.production.plugin.barcode.BarcodeScannerPlugin;
import org.goobi.production.plugin.interfaces.IDockablePlugin;

import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("DockableBean")
@WindowScoped
@Data
@EqualsAndHashCode(callSuper = false)
@Log4j2
public class DockableBean implements Serializable {
    private List<IDockablePlugin> dockablePlugins;

    // TODO: Reflection loading
    public DockableBean() {
        dockablePlugins = List.of(new BarcodeScannerPlugin());
        dockablePlugins.forEach(p -> {
            try {
                p.initialize();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("An exception occurred during plugin initialization", e);
            }
        });
    }
}
