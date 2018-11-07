package org.goobi.production.plugin.interfaces;

import spark.Service;

public interface IRestGuiPlugin extends IStepPlugin {
    public void initRoutes(Service http);

    public String[] getJsPaths();
}
