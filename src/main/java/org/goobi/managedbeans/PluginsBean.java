/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
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

    @Inject // NOSONAR needs to be a field injection, as the been constructor does not allow arguments
    HelperForm helperForm;

    @Getter
    private Map<String, List<PluginInfo>> plugins;

    @Getter
    @Setter
    private String mode = "installed";

    private static PluginsBean instance;

    public PluginsBean() {
        if (PluginsBean.instance == null) {
            PluginsBean.instance = this;
        }
        this.plugins = this.getPluginsFromFS();
    }

    public static PluginsBean getInstance() {
        return PluginsBean.instance;
    }

    public Map<String, List<PluginInfo>> getPluginsFromFS() {
        Map<String, List<PluginInfo>> pluginList = new LinkedHashMap<>();
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        Path pluginsFolder = Paths.get(config.getPluginFolder());
        Path libFolder = Paths.get(config.getLibFolder());
        pluginList.putAll(this.getPluginsFromPath(pluginsFolder, true));
        pluginList.putAll(this.getPluginsFromPath(libFolder, false));
        PluginsBean.moveGUIPluginsToBottom(pluginList);
        return pluginList;
    }

    /**
     * If the GUI plugins category exists and is not empty, it is moved to the end of the list by removing and putting it as the last element. Because
     * this is an ordered map, the order is kept.
     *
     * @param categories The map of plugin categories and plugin lists
     */
    private static void moveGUIPluginsToBottom(Map<String, List<PluginInfo>> categories) {
        List<PluginInfo> guiPlugins = categories.get("GUI");
        if (guiPlugins != null && !guiPlugins.isEmpty()) {
            categories.remove("GUI");
            categories.put("GUI", guiPlugins);
        }
    }

    //get plugins from any folder (including subfolders or not)
    public Map<String, List<PluginInfo>> getPluginsFromPath(Path pluginsFolder, boolean instantiate) {
        Set<String> stepPluginsInUse = StepManager.getDistinctStepPluginTitles();
        Map<String, List<PluginInfo>> pluginList = new TreeMap<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(pluginsFolder)) {
            // dirList collects plugins that are directly located in the plugins/ folder
            List<PluginInfo> dirList = new ArrayList<>();
            for (Path pluginDir : dirStream) {
                if (Files.isDirectory(pluginDir)) {
                    List<PluginInfo> subDirList = new ArrayList<>();
                    try (DirectoryStream<Path> pluginStream = Files.newDirectoryStream(pluginDir)) {
                        for (Path pluginP : pluginStream) {
                            if (pluginP.getFileName().toString().endsWith("jar")) {
                                subDirList.add(getPluginInfo(pluginP.toAbsolutePath(), stepPluginsInUse, instantiate));
                            }
                        }
                    }
                    String folder = pluginDir.getFileName().toString();
                    pluginList.put(folder, subDirList);
                } else if (pluginDir.getFileName().toString().endsWith("jar")) {
                    dirList.add(getPluginInfo(pluginDir.toAbsolutePath(), stepPluginsInUse, instantiate));
                }
            }
            // if there were plugins inside the directory dirList will not be empty
            if (!dirList.isEmpty()) {
                String folder = pluginsFolder.getFileName().toString();
                pluginList.put(folder, dirList); // add the plugins to the list
            }
        } catch (IOException e) {
            log.error(e);
        }
        return pluginList;
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
                    if (line.startsWith("Build-Date: ")) {
                        try {
                            info.setBuildDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(line.replace("Build-Date: ", "")));
                        } catch (ParseException e) {
                            log.error(e);
                        }
                    }
                    if (line.startsWith("Revision: ")) {
                        info.setGitHash(line.replace("Revision: ", ""));
                    }
                    if (line.startsWith("Version: ")) {
                        info.setGoobiVersion(line.replace("Version: ", ""));
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
        int[] runningVersionFields = Arrays.stream(runningVersion.split("\\.")).mapToInt(Integer::valueOf).toArray();
        int[] submittedVersionFields = Arrays.stream(goobiVersion.split("\\.")).mapToInt(Integer::valueOf).toArray();
        int majorDiff = runningVersionFields[0] - submittedVersionFields[0];
        if (majorDiff != 0) {
            return majorDiff;
        }
        int minorDiff = runningVersionFields[1] - submittedVersionFields[1];
        if (minorDiff != 0) {
            return minorDiff;
        }
        // ignore sub version when comparing version strings
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
