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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.PluginInfo;
import org.goobi.production.plugin.interfaces.IPlugin;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.HelperForm;
import de.sub.goobi.helper.Helper;
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

    @Inject
    HelperForm helperForm;

    @Getter
    private Map<String, List<PluginInfo>> plugins;
    private List<String> translations;

    @Getter
    @Setter
    private String mode = "installed";

    public PluginsBean() {
        this.translations = new ArrayList<>();
        this.plugins = this.getPluginsFromFS();
    }

    public Map<String, List<PluginInfo>> getPluginsFromFS() {
        Map<String, List<PluginInfo>> plugins = new LinkedHashMap<>();
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        Path pluginsFolder = Paths.get(config.getPluginFolder());
        Path libFolder = Paths.get(config.getLibFolder());
        plugins.putAll(this.getPluginsFromPath(pluginsFolder, true));
        plugins.putAll(this.getPluginsFromPath(libFolder, false));
        PluginsBean.moveGUIPluginsToBottom(plugins);
        return plugins;
    }

    /**
     * If the GUI plugins category exists and is not empty, it is moved to the end of the list by removing and putting it as the last element. Because
     * this is an ordered map, the order is kept.
     *
     * @param categories The map of plugin categories and plugin lists
     */
    private static void moveGUIPluginsToBottom(Map<String, List<PluginInfo>> categories) {
        List<PluginInfo> guiPlugins = categories.get("GUI");
        if (guiPlugins != null && guiPlugins.size() > 0) {
            categories.remove("GUI");
            categories.put("GUI", guiPlugins);
        }
    }

    //get plugins from any folder (including subfolders or not)
    public Map<String, List<PluginInfo>> getPluginsFromPath(Path pluginsFolder, boolean instantiate) {
        Set<String> stepPluginsInUse = StepManager.getDistinctStepPluginTitles();
        Map<String, List<PluginInfo>> plugins = new TreeMap<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(pluginsFolder)) {
            List<PluginInfo> dirList = new ArrayList<>();
            for (Path pluginDir : dirStream) {
                if (Files.isDirectory(pluginDir)) {
                    dirList = new ArrayList<>();
                    try (DirectoryStream<Path> pluginStream = Files.newDirectoryStream(pluginDir)) {
                        for (Path pluginP : pluginStream) {
                            if (pluginP.getFileName().toString().endsWith("jar")) {
                                dirList.add(getPluginInfo(pluginP.toAbsolutePath(), stepPluginsInUse, instantiate));
                            }
                        }
                    }
                    String folder = pluginDir.getFileName().toString();
                    plugins.put(folder, dirList);
                    translations.add(this.getTranslatedFolderName(folder));
                    System.out.println("Added " + this.getTranslatedFolderName(folder));
                } else { //if plugin is directly inside directory
                    if (pluginDir.getFileName().toString().endsWith("jar")) {
                        dirList.add(getPluginInfo(pluginDir.toAbsolutePath(), stepPluginsInUse, instantiate));
                    }
                }
            }
            if (!dirList.isEmpty()) { //if there were plugins inside the directory dirList will not be empty
                String folder = pluginsFolder.getFileName().toString();
                plugins.put(folder, dirList); // add the plugins to the list
                translations.add(this.getTranslatedFolderName(folder));
                System.out.println("Added " + this.getTranslatedFolderName(folder));
            }
        } catch (IOException e) {
            log.error(e);
        }
        return plugins;
    }

    public String getTranslatedFolderName(String folder) {
        String prefix = "plugin_list_title_";
        String translated = Helper.getTranslation(prefix + folder);
        if (translated.startsWith(prefix)) {
            return folder;
        } else {
            return translated;
        }
    }

    private static PluginInfo getPluginInfo(Path pluginP, Set<String> stepPluginsInUse, boolean instantiate) throws ZipException, IOException {
        final PluginInfo info = new PluginInfo();
        info.setFilename(pluginP.getFileName().toString());
        if (instantiate) {
            PluginManager pm = PluginManagerFactory.createPluginManager();
            pm.addPluginsFrom(pluginP.toUri());
            Collection<IPlugin> plugins = new PluginManagerUtil(pm).getPlugins(IPlugin.class);
            for (IPlugin p : plugins) {
                info.addContainedPlugin(p.getTitle());
            }
            Set<String> pluginsInUse = new HashSet<>(info.getContainedPlugins());
            pluginsInUse.retainAll(stepPluginsInUse);
            info.setPluginsUsedInWorkflows(pluginsInUse);
        }
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
                    if (line.startsWith("Goobi-Version: ")) {
                        info.setGoobiVersion(line.replace("Goobi-Version: ", ""));
                    }
                }
            }
        }
        return info;
    }

    public int compareGoobiVersionToRunningVersion(String goobiVersion) {
        if (StringUtils.isBlank(goobiVersion) || "N/A".equals(goobiVersion)) {
            return 1;
        }
        if (goobiVersion.endsWith("-SNAPSHOT")) {
            //SNAPSHOT versions are always a bad idea and should be marked as bad at all times
            return 1;
        }
        String runningVersion = helperForm.getVersion().replace("-dev", "").replace("-SNAPSHOT", "");
        return compareGoobiVersions(goobiVersion, runningVersion);
    }

    public static int compareGoobiVersions(String goobiVersion, String runningVersion) {
        int[] runningVersionFields = Arrays.stream(runningVersion.split("\\."))
                .mapToInt(Integer::valueOf)
                .toArray();
        int[] submittedVersionFields = Arrays.stream(goobiVersion.split("\\."))
                .mapToInt(Integer::valueOf)
                .toArray();
        int majorDiff = runningVersionFields[0] - submittedVersionFields[0];
        if (majorDiff != 0) {
            return majorDiff;
        }
        int minorDiff = runningVersionFields[1] - submittedVersionFields[1];
        if (minorDiff != 0) {
            return minorDiff;
        }
        if (runningVersionFields.length == 3 && submittedVersionFields.length == 3) {
            return runningVersionFields[2] - submittedVersionFields[2];
        }
        if (runningVersionFields.length == 3) {
            return 1;
        }
        if (submittedVersionFields.length == 3) {
            return -1;
        }
        return minorDiff;
    }

    public String getBadgeClassForGoobiVersion(String goobiVersion) {
        int compared = compareGoobiVersionToRunningVersion(goobiVersion);
        if (compared < 0) {
            return "badge-intranda-orange";
        }
        if (compared > 0) {
            return "badge-intranda-red";
        }
        //compared == 0, plugin Goobi version matches running version 
        return "badge-intranda-green";
    }

    public String getPluginFolder() {
        return ConfigurationHelper.getInstance().getPluginFolder();
    }
}
