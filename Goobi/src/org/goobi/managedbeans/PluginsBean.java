package org.goobi.managedbeans;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.goobi.beans.PluginInfo;
import org.goobi.production.plugin.interfaces.IPlugin;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;

/**
 * This is the backing bean for the plugins-view that shows all plugins in the file system and their git revisions
 * 
 * @author Oliver Paetzel
 *
 */

@ViewScoped
@Log4j2
@Named
public class PluginsBean implements Serializable {

    private static final long serialVersionUID = 9152658727528258005L;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Getter
    private Map<String, List<PluginInfo>> plugins;

    @Getter
    @Setter
    private String mode = "installed";

    @Getter
    @Setter
    private String conflicts_mode = "edit_existing_file";

    public PluginsBean() {
        this.plugins = getPluginsFromFS();
    }

    public static Map<String, List<PluginInfo>> getPluginsFromFS() {
        Set<String> stepPluginsInUse = StepManager.getDistinctStepPluginTitles();
        Map<String, List<PluginInfo>> plugins = new TreeMap<>();
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        Path pluginsFolder = Paths.get(config.getPluginFolder());
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(pluginsFolder)) {
            for (Path pluginDir : dirStream) {
                if (Files.isDirectory(pluginDir)) {
                    List<PluginInfo> dirList = new ArrayList<>();
                    try (DirectoryStream<Path> pluginStream = Files.newDirectoryStream(pluginDir)) {
                        for (Path pluginP : pluginStream) {
                            if (pluginP.getFileName().toString().endsWith("jar")) {
                                dirList.add(getPluginInfo(pluginP.toAbsolutePath(), stepPluginsInUse));
                            }
                        }
                    }
                    plugins.put(pluginDir.getFileName().toString(), dirList);
                }
            }
        } catch (IOException e) {
            log.error(e);
        }
        return plugins;
    }

    private static PluginInfo getPluginInfo(Path pluginP, Set<String> stepPluginsInUse) throws ZipException, IOException {
        final PluginInfo info = new PluginInfo();
        info.setFilename(pluginP.getFileName().toString());
        PluginManager pm = PluginManagerFactory.createPluginManager();
        pm.addPluginsFrom(pluginP.toUri());
        Collection<IPlugin> plugins = new PluginManagerUtil(pm).getPlugins(IPlugin.class);
        for (IPlugin p : plugins) {
            info.addContainedPlugin(p.getTitle());
        }
        Set<String> pluginsInUse = new HashSet<>(info.getContainedPlugins());
        pluginsInUse.retainAll(stepPluginsInUse);
        info.setPluginsUsedInWorkflows(pluginsInUse);
        try (ZipFile zipFile = new ZipFile(pluginP.toFile())) {
            ZipEntry manifestEntry = zipFile.getEntry("META-INF/MANIFEST.MF");
            try (InputStream in = zipFile.getInputStream(manifestEntry); BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("Build-Time: ")) {
                        try {
                            info.setBuildDate(dateFormat.parse(line.replace("Build-Time: ", "")));
                        } catch (ParseException e) {
                            log.error(e);
                        }
                    }
                    if (line.startsWith("Implementation-SCM-Revision: ")) {
                        info.setGitHash(line.replace("Implementation-SCM-Revision: ", ""));
                    }
                }
            }
        }
        return info;
    }
}
