package org.goobi.production.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PluginInstallConflict {
    private String path;
    private ResolveTactic resolveTactic;
    private String editedVersion;
    private String localVersion;
    private String archiveVersion;

    public static enum ResolveTactic {
        unknown,
        useMaintainer,
        useCurrent,
        editedVersion
    }
}
