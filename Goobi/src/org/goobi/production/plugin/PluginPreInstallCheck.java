package org.goobi.production.plugin;

import java.nio.file.Path;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class PluginPreInstallCheck {

    private Path pluginExtractedPath;
    private PluginInstallInfo info;
    private Map<String, PluginInstallConflict> conflicts;
    private String error;

}
