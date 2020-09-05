package org.goobi.production.plugin;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class PluginPreInstallCheck {
    public final static Set<String> endingWhitelist = Sets.newHashSet(".js", ".css", ".jar");
    public final static Set<String> pathBlacklist = Sets.newHashSet("pom.xml");

    private Path pluginExtractedPath;
    private PluginInstallInfo info;
    private Map<String, PluginInstallConflict> conflicts;
    private String error;

}
