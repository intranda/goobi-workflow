package org.goobi.production.plugin.interfaces;

import spark.Service;

public interface IRestPlugin extends IPlugin {
    public void initRoutes(Service http);

    public String[] getJsPaths();
}
