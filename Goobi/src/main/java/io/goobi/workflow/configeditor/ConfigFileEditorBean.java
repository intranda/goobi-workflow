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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationRuntimeException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import io.goobi.workflow.configeditor.xml.ReportErrorsErrorHandler;
import io.goobi.workflow.configeditor.xml.XMLError;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Named
@WindowScoped
@Log4j2
public class ConfigFileEditorBean implements Serializable {

    private static final long serialVersionUID = 4944542089317605274L;

    public static final String MESSAGE_KEY_PREFIX = "plugin_administration_config_file_editor";

    // The name of this file is needed to exclude it from the list that is shown in the GUI
    public static final String CONFIGURATION_FILE = "goobi_configeditor.xml";

    @Getter
    private String title = "intranda_administration_config_file_editor";

    private transient List<ConfigFile> configFiles;

    /**
     * -1 means that no config file is selected
     */
    @Getter
    private int currentConfigFileIndex = -1;

    private int configFileIndexAfterSaveOrIgnore = -1;

    @Getter
    private boolean configFileContentChanged = false;

    /**
     * null means that no config file is selected
     */
    @Getter
    private transient ConfigFile currentConfigFile = null;

    /**
     * null means that no config file is selected
     */
    @Getter
    @Setter
    private String currentConfigFileFileContent = null;

    @Getter
    private String currentConfigFileType;

    @Getter
    private boolean validationError;

    /**
     * Constructor
     */
    public ConfigFileEditorBean() {
        XMLConfiguration configuration = getConfig();
        ConfigFileUtils.init(configuration);
        getConfigFiles();
    }

    private XMLConfiguration getConfig() {
        XMLConfiguration config = new XMLConfiguration();
        config.setDelimiterParsingDisabled(true);
        try {
            config.load(new Helper().getGoobiConfigDirectory() + CONFIGURATION_FILE);
        } catch (ConfigurationException e) {
            log.error("Error while reading the configuration file " + CONFIGURATION_FILE, e);
        }
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
        return config;
    }

    public String getCurrentEditorTitle() {
        if (this.currentConfigFile != null) {
            return this.currentConfigFile.getFileName();
        } else {
            return "";
        }
    }

    private void initConfigFileDates() {
        StorageProviderInterface storageProvider = StorageProvider.getInstance();
        for (ConfigFile file : this.configFiles) {
            try {
                String pathName = file.getConfigDirectory().getDirectory() + file.getFileName();
                long lastModified = storageProvider.getLastModifiedDate(Paths.get(pathName));
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                file.setLastModified(formatter.format(lastModified));
            } catch (IOException ioException) {
                file.setLastModified("[no date available]");
            }
        }
    }

    public String getLastModifiedDateOfConfigurationFile(ConfigFile configFile) {
        return configFile.getLastModified();
    }

    public void setCurrentConfigFileFileContentBase64(String content) {
        if ("".equals(content)) {
            // content is not set up correctly, don't write into file!
            return;
        }
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decoded = decoder.decode(content);
        this.currentConfigFileFileContent = new String(decoded, StandardCharsets.UTF_8);
    }

    public String getCurrentConfigFileFileContentBase64() {
        // The return value must be empty to indicate that the text was not initialized until now.
        return "";
    }

    public List<String> getWarningMessages() {
        return ConfigFileUtils.getWarningMessages();
    }

    public boolean isWarningListNotEmpty() {
        if (ConfigFileUtils.getWarningMessages() == null) {
            return true;
        }
        return !ConfigFileUtils.getWarningMessages().isEmpty();
    }

    public List<ConfigFile> getConfigFiles() {
        if (this.configFiles == null) {
            this.configFiles = ConfigFileUtils.getAllConfigFiles();
            this.initConfigFileDates();
        }

        if (this.configFiles != null) {
            return this.configFiles;
        } else {
            return new ArrayList<>();
        }
    }

    public void setCurrentConfigFileIndex(int index) {
        this.setConfigFile(index);
    }

    public String getCurrentConfigFileFileName() {
        return this.currentConfigFile.getConfigDirectory().getDirectory() + this.currentConfigFile.getFileName();
    }

    public boolean isActiveConfigFile(ConfigFile configFile) {
        return this.findConfigFileIndex(configFile) == this.currentConfigFileIndex;
    }

    public boolean isWritable(ConfigFile configFile) {
        return configFile.isWritable();
    }

    public boolean isCurrentFileWritable() {
        return this.currentConfigFile.isWritable();
    }

    public void editConfigFile(ConfigFile configFile) {
        int index = this.findConfigFileIndex(configFile);
        if (this.hasFileContentChanged()) {
            this.configFileContentChanged = true;
            this.configFileIndexAfterSaveOrIgnore = index;
            this.validationError = false;
            return;
        }
        this.setConfigFile(index);
        if (!this.currentConfigFile.isWritable()) {
            String key = "plugin_administration_config_file_editor_file_not_writable_check_permissions";
            Helper.setMeldung("configFileEditor", Helper.getTranslation(key), "");
        }
    }

    public void editConfigFileIgnore() {
        this.configFileContentChanged = false;
        this.setConfigFile(this.configFileIndexAfterSaveOrIgnore);
    }

    public int findConfigFileIndex(ConfigFile configFile) {
        for (int index = 0; index < this.configFiles.size(); index++) {
            if (configFile == this.configFiles.get(index)) {
                return index;
            }
        }
        return -1;
    }

    public void save() throws ParserConfigurationException, SAXException, IOException {
        if (this.getCurrentConfigFileFileName().endsWith(".xml") && !checkXML()) {
            return;
        }
        if (this.getCurrentConfigFileFileName().endsWith(".properties") && !checkProperties()) {
            return;
        }
        // Only create a backup if the new file content differs from the existing file content
        if (this.hasFileContentChanged()) {
            ConfigFileUtils.createBackup(this.currentConfigFile);
            String directory = this.currentConfigFile.getConfigDirectory().getDirectory();
            ConfigFileUtils.writeFile(directory, this.getCurrentConfigFileFileName(), this.currentConfigFileFileContent);
        }

        if (this.configFileIndexAfterSaveOrIgnore != -1) {
            if (this.configFileIndexAfterSaveOrIgnore != this.currentConfigFileIndex) {
                this.setConfigFile(this.configFileIndexAfterSaveOrIgnore);
            }
            this.configFileIndexAfterSaveOrIgnore = -1;
        }
        this.configFileContentChanged = false;
    }

    private boolean checkProperties() throws IOException {
        PropertiesConfiguration apacheProp = new PropertiesConfiguration();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(this.currentConfigFileFileContent.getBytes(StandardCharsets.UTF_8))) {
            apacheProp.load(bais);
        } catch (ConfigurationException | ConfigurationRuntimeException e) {
            Helper.setFehlerMeldung("configFileEditor", e.getMessage(), "");
            Helper.setFehlerMeldung("configFileEditor", "File was not saved, because the properties format is not well-formed", "");
            return false;
        }
        return true;
    }

    private boolean checkXML() throws ParserConfigurationException, SAXException, IOException {
        boolean ok = true;
        List<XMLError> errors = checkXMLWellformed(this.currentConfigFileFileContent);
        if (!errors.isEmpty()) {
            for (XMLError error : errors) {
                Helper.setFehlerMeldung("configFileEditor",
                        String.format("Line %d column %d: %s", error.getLine(), error.getColumn(), error.getMessage()), "");
            }
            if (errors.stream().anyMatch(e -> "ERROR".equals(e.getSeverity()) || "FATAL".equals(e.getSeverity()))) {
                this.validationError = true;
                //this needs to be done, so the modal won't appear repeatedly and ask the user if he wants to save.
                this.configFileIndexAfterSaveOrIgnore = -1;
                this.configFileContentChanged = false;
                Helper.setFehlerMeldung("configFileEditor", "File was not saved, because the XML is not well-formed", "");
                ok = false;
            }
        } else {
            this.validationError = false;
        }
        return ok;
    }

    private boolean hasFileContentChanged() {
        if (this.currentConfigFile == null) {
            return false;
        }
        String fileContent = ConfigFileUtils.readFile(this.getCurrentConfigFileFileName());
        fileContent = fileContent.replace("\r\n", "\n");
        fileContent = fileContent.replace("\r", "\n");
        String editorContent = this.currentConfigFileFileContent;
        return !fileContent.equals(editorContent);
    }

    public void cancel() {
        this.setConfigFile(-1);
    }

    private void setConfigFile(int index) {
        // Change the (saved or unchanged) file
        if (index >= 0 && index < this.configFiles.size()) {
            this.currentConfigFileIndex = index;
            this.currentConfigFile = this.configFiles.get(index);
            this.currentConfigFileFileContent = ConfigFileUtils.readFile(this.getCurrentConfigFileFileName());
            this.currentConfigFileType = this.currentConfigFile.getType().toString();
        } else {
            // Close the file
            this.currentConfigFileIndex = -1;
            this.currentConfigFile = null;
            this.currentConfigFileFileContent = null;
        }
        this.validationError = false;
    }

    public String getExplanationTitle() {
        String key = MESSAGE_KEY_PREFIX + "_explanation_for";
        String translation = Helper.getTranslation(key);
        String fileName = this.currentConfigFile.getFileName();
        return translation + " " + fileName;
    }

    private List<XMLError> checkXMLWellformed(String xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        ReportErrorsErrorHandler eh = new ReportErrorsErrorHandler();
        builder.setErrorHandler(eh);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            builder.parse(bais);
        } catch (SAXParseException e) {
            //ignore this, because we collect the errors in the errorhandler and give them to the user.
        }

        return eh.getErrors();
    }
}
