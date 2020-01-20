package org.goobi.production.plugin;

import static spark.Service.ignite;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.goobi.api.rest.AuthorizationFilter;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IRestGuiPlugin;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import spark.Service;
import spark.route.ServletRoutes;
import spark.servlet.SparkApplication;

@Log4j2
public class SparkListener implements SparkApplication {
    private Path staticFilesLocation = Paths.get(ConfigurationHelper.getInstance().getGoobiFolder() + "/static_assets/");

    @Override
    public void init() {
        Service http = ignite();
        log.debug("spark static file location: " + staticFilesLocation);
        http.externalStaticFileLocation(staticFilesLocation.toString());
        declareRoutes(http);
    }

    private void declareRoutes(Service http) {
        List<IPlugin> plugins = PluginLoader.getPluginList(PluginType.Step);
        plugins.addAll(PluginLoader.getPluginList(PluginType.Dashboard));
        ServletRoutes.get().clear();
        http.path("/plugins", () -> {
            http.before("/*", (q, r) -> {
                HttpServletRequest hreq = q.raw();
                if (!AuthorizationFilter.hasJsfContext(hreq)) {
                    // user logged in check failed - now check for webapi token.
                    String token = q.headers("token");
                    if (StringUtils.isBlank(token)) {
                        token = q.params("token");
                    }

                    String pathInfo = q.pathInfo();
                    String method = q.requestMethod();
                    String ip = q.headers("x-forwarded-for");
                    if (ip == null) {
                        ip = q.ip();
                    }
                    if (!AuthorizationFilter.checkPermissions(ip, token, pathInfo, method)) {
                        http.halt(401);
                    }
                    q.attribute("tokenAuthorized", true);
                } else {
                    HttpSession session = hreq.getSession();
                    LoginBean userBean = (LoginBean) session.getAttribute("LoginForm");
                    q.attribute("user", userBean.getMyBenutzer());
                }

            });
            for (IPlugin p : plugins) {
                if (p instanceof IRestGuiPlugin) {
                    ((IRestGuiPlugin) p).extractAssets(staticFilesLocation);
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
