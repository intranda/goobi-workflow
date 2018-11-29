package de.intranda.goobi.plugins;

import lombok.EqualsAndHashCode;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@EqualsAndHashCode(callSuper = false)
public class DanteBiographieChronicleEventPlugin extends AbstractDantePlugin {

    // class name for goobi
    private static final String pluginTitle = "DanteBiographieChronicleEvent";
    // name of database
    private static final String vocabulary = "biographie_chronicle_event";
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
