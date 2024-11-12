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
 */

package io.goobi.workflow.configeditor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.goobi.io.BackupFileManager;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class ConfigFileUtils {

    private static Charset standardCharset;

    private static XMLConfiguration xmlConfiguration;

    private ConfigFileUtils() {
        // hide constructor
    }

    public static void init(XMLConfiguration configuration) {
        ConfigFileUtils.xmlConfiguration = configuration;
        ConfigFileUtils.standardCharset = StandardCharsets.UTF_8;
    }

    public static List<ConfigFile> getAllConfigFiles() {
        return ConfigFileUtils.getAllConfigFiles(ConfigFileUtils.xmlConfiguration);
    }

    // TODO: Later an own permission for super admins should solve the decision if configuration files are displayed or not
    // Until now the configuration file of this plugin is filtered out to avoid to be edited in the browser interface by "default" admins
    // For that the warnings can be used and displayed above the plugin in the GUI.
    public static List<ConfigFile> getAllConfigFiles(XMLConfiguration xml) {

        // If the default config directory was not included, it is included afterwards
        ConfigurationHelper configuration = ConfigurationHelper.getInstance();
        String defaultConfigDirectory = configuration.getConfigurationFolder();
        boolean foundDefaultConfigDirectory = false;

        List<ConfigFile> configFiles = new ArrayList<>();

        int index = 0;
        ConfigDirectory directory;
        do {
            directory = ConfigFileUtils.tryToParseConfigDirectory(xml, index);
            if (directory != null) {

                if (!ConfigFileUtils.containsHiddenDirectory(directory.getDirectory())) {
                    configFiles.addAll(ConfigFileUtils.getAllConfigFilesFromDirectory(directory));
                }

                if (defaultConfigDirectory.equals(directory.getDirectory())) {
                    foundDefaultConfigDirectory = true;
                }
            }
            index++;
        } while (directory != null);

        if (!foundDefaultConfigDirectory) {
            ConfigDirectory defaultDirectory = new ConfigDirectory(defaultConfigDirectory, "backup/", 8, "");
            configFiles.addAll(ConfigFileUtils.getAllConfigFilesFromDirectory(defaultDirectory));
        }

        return configFiles;
    }

    /**
     * Returns true if at least one of the path elements is a hidden directory (starts with a '.').
     *
     * @param path The path to check
     * @return true If the path contains a hidden directory
     */
    public static boolean containsHiddenDirectory(String path) {
        Path file = Paths.get(path);
        for (int index = 0; index < file.getNameCount(); index++) {
            Path subPath = file.getName(index);
            if (subPath.toString().startsWith(".")) {
                return true;
            }
        }
        return false;
    }

    // Until now this method only returns an empty list because warnings are not used.
    public static List<String> getWarningMessages() {
        return new ArrayList<>();
    }

    /**
     * This security check is needed to only allow directories that are inside the parent directory of the configured goobi directory. Especially
     * paths that contain "../" (parent directory) may not direct to directories outside of that directory.
     *
     * @param directory
     * @return
     */
    public static boolean isDirectoryAllowed(String directory) {
        String goobiDirectoryName = ConfigurationHelper.getInstance().getGoobiFolder();
        Path goobiDirectory = Paths.get(goobiDirectoryName).toAbsolutePath();
        Path goobiParentDirectory = goobiDirectory.getParent().toAbsolutePath();
        String absoluteGoobiParentDirectoryName = goobiParentDirectory.toString();

        String absoluteRequestedDirectoryName = Paths.get(directory).toAbsolutePath().toString();
        return absoluteRequestedDirectoryName.startsWith(absoluteGoobiParentDirectoryName);
    }

    private static ConfigDirectory tryToParseConfigDirectory(XMLConfiguration xml, int index) {
        String element = "configFileDirectories.directory(" + index + ")";

        // Get the directory name where the configuration files are (or null)
        String directory = null;
        try {
            directory = xml.getString(element);
        } catch (Exception exception) {
            log.error(exception);
        }
        // No directory with this index found, loop breaks to look for further directories
        if (directory == null || directory.length() == 0) {
            return null;
        }
        if (!directory.endsWith("/")) {
            directory += "/";
        }

        // Get the backup directory if it is defined in the parameter. If it is not defined, set it to "backup/"
        String backupDirectory = null;
        try {
            backupDirectory = xml.getString(element + "[@backupFolder]");
        } catch (Exception exception) {
            // No value is set, standard value should be used.
        }
        // The default backup directory is the subdirectory "backup/" in the directory where the configuration files are.
        // If the backupDirectory is an empty string (""), the configuration directory is used for the backups.
        if (backupDirectory == null) {
            backupDirectory = "backup/";
        }
        if (backupDirectory.length() > 0 && !backupDirectory.endsWith("/")) {
            backupDirectory += "/";
        }
        backupDirectory = directory + backupDirectory;

        // Get the number of backup files that should be rotated before old files are deleted. The default value is 8.
        int numberOfBackups = 8;
        try {
            numberOfBackups = xml.getInt(element + "[@backupFiles]");
        } catch (Exception exception) {
            // No value is set, standard value should be used.
        }
        if (numberOfBackups < 0) {
            numberOfBackups = 0;
        }

        // Get the file regex parameter. If the regex is not used, it is set to null and not used in the file selection algorithm.
        String fileRegex = null;
        try {
            fileRegex = xml.getString(element + "[@fileRegex]");
        } catch (Exception exception) {
            // No value is set, standard value should be used.
        }

        return new ConfigDirectory(directory, backupDirectory, numberOfBackups, fileRegex);
    }

    private static List<ConfigFile> getAllConfigFilesFromDirectory(ConfigDirectory configDirectory) {
        StorageProviderInterface storage = StorageProvider.getInstance();
        String regex = configDirectory.getFileRegex();

        // listFiles(String folder) already contains an empty try-catch-block without warning message
        // In case of an error an empty list is provided here.
        List<Path> files = storage.listFiles(configDirectory.getDirectory());
        List<ConfigFile> configFiles = new ArrayList<>();

        for (int index = 0; index < files.size(); index++) {
            Path file = files.get(index).toAbsolutePath();

            // The own configuration file is excluded because normal admins should not be able to add custom paths
            if (storage.isFileExists(file) && !ConfigFileUtils.isOwnConfigFile(files.get(index).getFileName().toString())) {
                String name = file.getFileName().toString();
                if (name.startsWith(".")) {
                    continue;
                }
                if (regex != null && regex.length() > 0 && !name.matches(regex)) {
                    continue;
                }
                ConfigFile configFile = new ConfigFile(file);
                configFile.setConfigDirectory(configDirectory);
                if (name.endsWith(".xml")) {
                    configFile.setType(ConfigFile.Type.XML);
                    configFiles.add(configFile);
                } else if (name.endsWith(".properties")) {
                    configFile.setType(ConfigFile.Type.PROPERTIES);
                    configFiles.add(configFile);
                }
            }
        }
        return configFiles;
    }

    private static boolean isOwnConfigFile(String fileName) {
        return ConfigFileEditorBean.CONFIGURATION_FILE.equals(fileName);
    }

    public static void createBackup(ConfigFile configFile) throws IOException {
        ConfigDirectory configDirectory = configFile.getConfigDirectory();
        String directory = configDirectory.getDirectory();
        String backupDirectory = configDirectory.getBackupDirectory();
        String fileName = configFile.getFileName();
        int numberOfBackups = configDirectory.getNumberOfBackups();
        try {
            BackupFileManager.createBackup(directory, backupDirectory, fileName, numberOfBackups, false);
        } catch (IOException ioException) {
            String message = "ConfigFileEditorAdministrationPlugin could not create the backup file.";
            message += " Please check the permissions in the configured backup directory.";
            log.error(message);
            String key = "plugin_administration_config_file_editor_backup_not_writable_check_permissions";
            StringBuilder buffer = new StringBuilder();
            buffer.append(Helper.getTranslation(key));
            buffer.append(" (");
            buffer.append(backupDirectory);
            buffer.append(fileName);
            buffer.append(")");
            Helper.setFehlerMeldung(buffer.toString(), "");
        }
    }

    public static String readFile(String fileName) {
        try {
            Charset charset = ConfigFileUtils.standardCharset;
            return FileUtils.readFileToString(new File(fileName), charset);
        } catch (IOException | IllegalArgumentException ioException) {
            String message = "ConfigFileEditorAdministrationPlugin could not read file " + fileName;
            log.error(message);
            Helper.setFehlerMeldung(message, "");
            return "";
        }
    }

    /**
     * The fileName parameter must already contain the backup directory. The backupDirectory parameter is used to check whether the directory exists.
     *
     * @param backupDirectory
     * @param fileName
     * @param content
     */
    public static void writeFile(String backupDirectory, String fileName, String content) {
        if (!Paths.get(backupDirectory).toFile().exists()) {
            ConfigFileUtils.createDirectory(backupDirectory);
        }

        try {
            Charset charset = ConfigFileUtils.standardCharset;
            FileUtils.write(new File(fileName), content, charset);
            String message = Helper.getTranslation("savedConfigFileSuccessfully");
            Helper.setMeldung(message, "");
        } catch (IOException | IllegalArgumentException ioException) {
            String message = "ConfigFileEditorAdministrationPlugin could not write file " + fileName;
            log.error(message);
            Helper.setFehlerMeldung(message, "");
        }
    }

    public static void createDirectory(String directoryName) {
        Path path = Paths.get(directoryName);
        try {
            StorageProvider.getInstance().createDirectories(path);
        } catch (IOException ioException) {
            log.error("ConfigFileEditorAdministrationPlugin could not create directory " + directoryName);
        }
    }

}