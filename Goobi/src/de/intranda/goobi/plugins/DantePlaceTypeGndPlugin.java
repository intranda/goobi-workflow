package de.intranda.goobi.plugins;

import lombok.EqualsAndHashCode;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@EqualsAndHashCode(callSuper = false)
public class DantePlaceTypeGndPlugin extends AbstractDantePlugin {

    // class name for goobi
    private static final String pluginTitle = "DantePlaceTypeGnd";
    // name of database
    private static final String vocabulary = "place_type_gnd";
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
