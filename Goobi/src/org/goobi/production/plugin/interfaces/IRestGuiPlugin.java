package org.goobi.production.plugin.interfaces;

import spark.Service;

public interface IRestGuiPlugin extends IPlugin {
    public void initRoutes(Service http);

    public String getJsPath();
}
