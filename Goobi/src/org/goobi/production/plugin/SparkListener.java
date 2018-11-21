package org.goobi.production.plugin;

import static spark.Service.ignite;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.goobi.managedbeans.LoginBean;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IRestGuiPlugin;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j;
import spark.Service;
import spark.route.ServletRoutes;
import spark.servlet.SparkApplication;

@Log4j
public class SparkListener implements SparkApplication {

    @Override
    public void init() {
        Service http = ignite();
        String staticFileLocation = ConfigurationHelper.getInstance().getPluginFolder() + "/static_assets/";
        http.externalStaticFileLocation(staticFileLocation);
        declareRoutes(http);
    }

    private void declareRoutes(Service http) {
        final List<IPlugin> plugins = PluginLoader.getPluginList(PluginType.Step);
        ServletRoutes.get().clear();
        http.path("/plugins", () -> {
            http.before((q, r) -> {
                HttpServletRequest hreq = q.raw();
                LoginBean userBean = (LoginBean) hreq.getSession().getAttribute("LoginForm");
                if (((userBean == null || userBean.getMyBenutzer() == null))) {
                    // TODO: user logged in check failed - now check for webapi token.
                    http.halt(401);
                }

            });
            for (IPlugin p : plugins) {
                if (p instanceof IRestGuiPlugin) {
                    ((IRestGuiPlugin) p).initRoutes(http);
                }
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
