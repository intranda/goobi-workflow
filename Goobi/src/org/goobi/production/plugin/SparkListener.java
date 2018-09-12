package org.goobi.production.plugin;

import static spark.Service.ignite;

import java.util.List;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IRestGuiPlugin;

import spark.Service;
import spark.route.ServletRoutes;
import spark.servlet.SparkApplication;

public class SparkListener implements SparkApplication {

    @Override
    public void init() {
        Service http = ignite();
        http.externalStaticFileLocation("/opt/digiverso/goobi/plugins/static_assets/");
        declareRoutes(http);
    }

    private void declareRoutes(Service http) {
        final List<IPlugin> plugins = PluginLoader.getPluginList(PluginType.Restgui);
        ServletRoutes.get().clear();
        http.path("/plugins", () -> {
            for (IPlugin p : plugins) {
                ((IRestGuiPlugin) p).initRoutes(http);
            }

            http.get("/healthcheck", (q, r) -> {
                return "it works!\n";
            });
            http.put("/reload", (req, resp) -> {
                this.declareRoutes(http);
                return "reloaded\n";
            });
        });
    }

}
