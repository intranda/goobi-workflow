package org.goobi.production.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PluginInstallConflict {
    private String path;
    private ResolveTactic resolveTactic;

    public static enum ResolveTactic {
        unknown,
        useMaintainer,
        useCurrent,
        diff
    }
}
