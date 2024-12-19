package org.goobi.production.plugin.interfaces;

import org.goobi.production.enums.PluginType;

import javax.faces.application.Application;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import java.io.IOException;

public interface IFooterPlugin extends IPlugin {
    default String getId() {
        return getTitle().replaceAll("\\s+", "");
    }

    String getIcon();

    @Override
    default PluginType getType() {
        return PluginType.Footer;
    }

    void initialize() throws Exception;

    void execute() throws Exception;

    default String getModalPath() {
        return null;
    }
}
