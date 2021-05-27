package org.goobi.production.plugin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PluginInstallConflict {
    private String path;
    private ResolveTactic resolveTactic;
    private String editedVersion;
    private String existingVersion;
    private String uploadedVersion;
    private List<List<SpanTag>> spanTags;
    private List<String> lineNumbers;
    private List<String> lineTypes;

    public static enum ResolveTactic {
        unknown,
        useMaintainer,
        useCurrent,
        editedVersion
    }
}
