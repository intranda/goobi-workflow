package org.goobi.production.plugin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class PluginInstallInfo {
    private String name;
    private String type;
    private String sourcecodeUri;
    private String documentationUri;
    private List<PluginVersion> versions;
}
