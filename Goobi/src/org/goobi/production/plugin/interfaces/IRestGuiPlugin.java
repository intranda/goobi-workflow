package org.goobi.production.plugin.interfaces;

import java.nio.file.Path;

import spark.Service;

public interface IRestGuiPlugin extends IStepPlugin {
    public void initRoutes(Service http);

    public String[] getJsPaths();

    public void extractAssets(Path assetsPath);
}
