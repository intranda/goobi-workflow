package de.intranda.goobi.plugins;

import lombok.EqualsAndHashCode;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@EqualsAndHashCode(callSuper = false)
public class DanteCitizenshipIso3166Plugin extends AbstractDantePlugin {

    // class name for goobi
    private static final String pluginTitle = "DanteCitizenshipIso3166";
    // name of database
    private static final String vocabulary = "citizenship_iso_3166";
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
