package de.intranda.goobi.plugins;

import lombok.EqualsAndHashCode;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@EqualsAndHashCode(callSuper = false)
public class DanteEthniePlugin extends AbstractDantePlugin {

    // class name for goobi
    private static final String pluginTitle = "DanteEthnie";
    // name of database
    private static final String vocabulary = "ethnie";
    // name of main field containing the representative value 
    private static final String label = "NORM_LABEL_de";

    @Override
    public String getVocabulary() {
        return vocabulary;
    }

    @Override
    public String getTitle() {
        return pluginTitle;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
