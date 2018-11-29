package de.intranda.goobi.plugins;

import org.goobi.production.plugin.interfaces.AbstractMetadataPlugin;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.apache.commons.lang.StringUtils;
import org.goobi.production.plugin.interfaces.IMetadataPlugin;

import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@EqualsAndHashCode(callSuper = false)
public @Data class SelectPlugin extends AbstractMetadataPlugin implements IMetadataPlugin {

    @Override
    public String getTitle() {
        return "SelectPlugin";
    }

    public String getPagePath() {
        return "/uii/includes/metseditor/extension/formMetsMultiselect.xhtml";
    }

    public void setSelectedItems(List<String> selectedItems) {

        String val = "";
        for (String sel : selectedItems) {
            val += sel + ";";
        }

        metadata.setValue(val);
    }

    public List<String> getSelectedItems() {
        String values = metadata.getValue();
        if (StringUtils.isNotBlank(values)) {
            List<String> answer = new ArrayList<String>();
            while (StringUtils.isNotBlank(values)) {
                int semicolon = values.indexOf(";");
                if (semicolon != -1) {
                    String value = values.substring(0, semicolon);
                    answer.add(value);

                    int length = values.length();
                    values = values.substring(semicolon + 1, length);
                } else {
                    answer.add(values);
                    values = "";
                }
            }
            return answer;
        } else {
            return defaultItems;
        }
    }

}
