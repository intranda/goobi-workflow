package org.goobi.production.plugin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PluginInstallInfo {
    private int id;
    private String name;
    private String type;
    private String sourcecodeUri;
    private String documentationUri;
    private List<PluginVersion> versions;
}
