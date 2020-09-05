package org.goobi.production.plugin;

import java.util.List;
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

    private PluginInstallInfo info;
    private List<PluginInstallConflict> conflicts;
    private String error;

    public PluginPreInstallCheck(PluginInstallInfo info) {
        super();
        this.info = info;
    }

}
