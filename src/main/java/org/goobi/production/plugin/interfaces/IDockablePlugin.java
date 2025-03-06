package org.goobi.production.plugin.interfaces;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.DockAnchor;

public interface IDockablePlugin extends IPlugin {
    default String getId() {
        return getTitle().replaceAll("\\s+", "");
    }

    default String getIcon() {
        return null;
    }

    @Override
    default PluginType getType() {
        return PluginType.Dockable;
    }

    default boolean isMenuBarDockable() { return isDockable(DockAnchor.MENU_BAR); }
    default boolean isFooterDockable() { return isDockable(DockAnchor.FOOTER); }

    boolean isDockable(DockAnchor anchor);

    default void initialize() throws Exception {
        // Nothing to do per default
    }

    default void execute() throws Exception {
        // Nothing to do per default
    }

    default String getModalPath() {
        return null;
    }
}
