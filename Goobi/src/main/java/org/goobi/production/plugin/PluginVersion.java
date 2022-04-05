package org.goobi.production.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PluginVersion {
    private String checksum;
    private String checksumType;
    private String goobiVersion;
    private String publicGoobiVersion;
    private String pluginVersion;
}
