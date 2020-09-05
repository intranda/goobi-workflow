package org.goobi.production.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PluginInstallConflict {
    private String path;
    private ResolveTactic resolveTactic;
    private String editedVersion;

    public static enum ResolveTactic {
        unknown,
        useMaintainer,
        useCurrent,
        editedVersion
    }
}
