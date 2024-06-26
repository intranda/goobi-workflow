package org.goobi.production.plugin.interfaces;

public interface IGuiPlugin {
    public default String[] getJsPaths() {
        return new String[0];
    }

    public default String[] getCssPaths() {
        return new String[0];
    }
}
