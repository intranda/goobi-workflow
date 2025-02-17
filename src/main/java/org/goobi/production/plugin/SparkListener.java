///**
// * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
// *
// * Visit the websites for more information.
// *          - https://goobi.io
// *          - https://www.intranda.com
// *          - https://github.com/intranda/goobi-workflow
// *
// * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
// * Software Foundation; either version 2 of the License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
// * Temple Place, Suite 330, Boston, MA 02111-1307 USA
// *
// * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
// * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
// * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
// * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
// * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
// * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
// * exception statement from your version.
// */
//package org.goobi.production.plugin;
//
//import static spark.Service.ignite;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.List;
//
//import org.apache.commons.lang3.StringUtils;
//import org.goobi.api.rest.AuthorizationFilter;
//import org.goobi.managedbeans.LoginBean;
//import org.goobi.production.enums.PluginType;
//import org.goobi.production.plugin.interfaces.IPlugin;
//import org.goobi.production.plugin.interfaces.IRestGuiPlugin;
//import org.goobi.production.plugin.interfaces.IRestPlugin;
//
//import de.sub.goobi.config.ConfigurationHelper;
//import de.sub.goobi.helper.Helper;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.extern.log4j.Log4j2;
//import spark.Service;
//import spark.route.ServletRoutes;
//import spark.servlet.SparkApplication;
//
//@SuppressWarnings("deprecation")
//@Log4j2
//public class SparkListener implements SparkApplication {
//    private Path staticFilesLocation = Paths.get(ConfigurationHelper.getInstance().getGoobiFolder() + "/static_assets/");
//
//    @Override
//    public void init() {
//        Service http = ignite();
//        log.debug("spark static file location: " + staticFilesLocation);
//        http.externalStaticFileLocation(staticFilesLocation.toString());
//        declareRoutes(http);
//    }
//
//    private void declareRoutes(Service http) {
//        List<IPlugin> plugins = PluginLoader.getPluginList(PluginType.Step);
//        plugins.addAll(PluginLoader.getPluginList(PluginType.Dashboard));
//        plugins.addAll(PluginLoader.getPluginList(PluginType.Workflow));
//        ServletRoutes.get().clear();
//        http.path("/plugins", () -> {
//            http.before("/*", (q, r) -> {
//                HttpServletRequest hreq = q.raw();
//                if (!AuthorizationFilter.hasJsfContext(hreq)) {
//                    // user logged in check failed - now check for webapi token.
//                    String token = q.headers("token");
//                    if (StringUtils.isBlank(token)) {
//                        token = q.params("token");
//                    }
//
//                    String pathInfo = q.pathInfo();
//                    String method = q.requestMethod();
//                    String ip = q.headers("x-forwarded-for");
//                    if (ip == null) {
//                        ip = q.ip();
//                    }
//                    if (!AuthorizationFilter.checkPermissions(ip, token, pathInfo, method)) {
//                        http.halt(401);
//                    }
//                    q.attribute("tokenAuthorized", true);
//                } else {
//                    LoginBean userBean = Helper.getLoginBeanFromSession(hreq.getSession());
//
//                    q.attribute("user", userBean.getMyBenutzer());
//                }
//
//            });
//            for (IPlugin p : plugins) {
//                if (p instanceof IRestGuiPlugin) {
//                    ((IRestGuiPlugin) p).extractAssets(staticFilesLocation);
//                    ((IRestGuiPlugin) p).initRoutes(http);
//                }
//                if (p instanceof IRestPlugin) {
//                    ((IRestPlugin) p).initRoutes(http);
//                }
//            }
//
//            http.get("/healthcheck", (q, r) -> "it works!\n");
//            http.put("/reload", (req, resp) -> {
//                this.declareRoutes(http);
//                return "reloaded\n";
//            });
//        });
//    }
//
//}
