package org.goobi.managedbeans;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.production.plugin.barcode.BarcodeScannerPlugin;
import org.goobi.production.plugin.interfaces.IFooterPlugin;

import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("FooterBean")
@WindowScoped
@Data
@EqualsAndHashCode(callSuper = false)
@Log4j2
public class FooterBean implements Serializable {
    private List<IFooterPlugin> footerPlugins;

    // TODO: Reflection loading
    public FooterBean() {
        footerPlugins = List.of(new BarcodeScannerPlugin());
        footerPlugins.forEach(p -> {
            try {
                p.initialize();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("An exception occurred during plugin initialization", e);
            }
        });
    }
}
