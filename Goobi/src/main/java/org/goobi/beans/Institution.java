/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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
package org.goobi.beans;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;

import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.persistence.managers.InstitutionManager;
import de.sub.goobi.persistence.managers.JournalManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Institution implements Serializable, DatabaseObject, Comparable<Institution> {

    /**
     * 
     */
    private static final long serialVersionUID = -2608701994741239302L;

    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private String shortName; // TODO unique name

    @Getter
    @Setter
    private String longName;
    @Getter
    @Setter
    private boolean allowAllRulesets;
    @Getter
    @Setter
    private boolean allowAllDockets;
    @Getter
    @Setter
    private boolean allowAllAuthentications;

    @Getter
    @Setter
    private boolean allowAllPlugins;

    private List<InstitutionConfigurationObject> allowedRulesets = new ArrayList<>();

    private List<InstitutionConfigurationObject> allowedDockets = new ArrayList<>();

    private List<InstitutionConfigurationObject> allowedAuthentications = new ArrayList<>();

    private List<InstitutionConfigurationObject> allowedAdministrationPlugins = new ArrayList<>();
    private List<InstitutionConfigurationObject> allowedStatisticsPlugins = new ArrayList<>();
    private List<InstitutionConfigurationObject> allowedDashboardPlugins = new ArrayList<>();
    private List<InstitutionConfigurationObject> allowedWorkflowPlugins = new ArrayList<>();

    @Getter
    @Setter
    // any additional data is hold in a map and gets stored in an xml column, it is searchable using xpath
    // individual values can be extracted: 'select ExtractValue(additional_data, '/root/key') from institution'
    private Map<String, String> additionalData = new HashMap<>();

    @Getter
    @Setter
    private transient Part uploadedFile = null;
    private transient Path tempFileToImport;
    private String basename;

    private String importFolder = "/tmp/"; // TODO

    @Getter
    @Setter
    private String content = "";

    @Getter
    @Setter
    private List<JournalEntry> journal = new ArrayList<>();

    @Override
    public int compareTo(Institution o) {
        return this.shortName.compareTo(o.getShortName());
    }

    @Override
    public void lazyLoad() {

    }

    public List<InstitutionConfigurationObject> getAllowedRulesets() {
        if (allowedRulesets.isEmpty()) {
            allowedRulesets = InstitutionManager.getConfiguredRulesets(id);
        }
        return allowedRulesets;
    }

    public List<InstitutionConfigurationObject> getAllowedDockets() {
        if (allowedDockets.isEmpty()) {
            allowedDockets = InstitutionManager.getConfiguredDockets(id);
        }
        return allowedDockets;
    }

    public List<InstitutionConfigurationObject> getAllowedAuthentications() {
        if (allowedAuthentications.isEmpty()) {
            allowedAuthentications = InstitutionManager.getConfiguredAuthentications(id);
        }
        return allowedAuthentications;
    }

    public List<InstitutionConfigurationObject> getAllowedAdministrationPlugins() {
        if (allowedAdministrationPlugins.isEmpty()) {
            List<String> pluginNames = PluginLoader.getListOfPlugins(PluginType.Administration);
            if (!pluginNames.isEmpty()) {
                allowedAdministrationPlugins = InstitutionManager.getConfiguredAdministrationPlugins(id, pluginNames);
            }
        }
        return allowedAdministrationPlugins;
    }

    public List<InstitutionConfigurationObject> getAllowedStatisticsPlugins() {
        if (allowedStatisticsPlugins.isEmpty()) {
            List<String> pluginNames = PluginLoader.getListOfPlugins(PluginType.Statistics);
            if (!pluginNames.isEmpty()) {
                allowedStatisticsPlugins = InstitutionManager.getConfiguredStatisticsPlugins(id, pluginNames);
            }
        }
        return allowedStatisticsPlugins;
    }

    public List<InstitutionConfigurationObject> getAllowedDashboardPlugins() {
        if (allowedDashboardPlugins.isEmpty()) {
            List<String> pluginNames = PluginLoader.getListOfPlugins(PluginType.Dashboard);
            if (!pluginNames.isEmpty()) {
                allowedDashboardPlugins = InstitutionManager.getConfiguredDashboardPlugins(id, pluginNames);
            }
        }
        return allowedDashboardPlugins;
    }

    public List<InstitutionConfigurationObject> getAllowedWorkflowPlugins() {
        if (allowedWorkflowPlugins.isEmpty()) {
            List<String> pluginNames = PluginLoader.getListOfPlugins(PluginType.Workflow);
            if (!pluginNames.isEmpty()) {
                allowedWorkflowPlugins = InstitutionManager.getConfiguredWorkflowPlugins(id, pluginNames);
            }
        }
        return allowedWorkflowPlugins;
    }

    public boolean isAdministrationPluginAllowed(String pluginName) {
        if (isAllowAllPlugins()) {
            return true;
        }
        for (InstitutionConfigurationObject ico : getAllowedAdministrationPlugins()) {
            if (ico.getObject_name().equals(pluginName)) {
                return ico.isSelected();
            }
        }
        return false;
    }

    public boolean isWorkflowPluginAllowed(String pluginName) {
        if (isAllowAllPlugins()) {
            return true;
        }
        for (InstitutionConfigurationObject ico : getAllowedWorkflowPlugins()) {
            if (ico.getObject_name().equals(pluginName)) {
                return ico.isSelected();
            }
        }
        return false;
    }

    public boolean isStatisticsPluginAllowed(String pluginName) {
        if (isAllowAllPlugins()) {
            return true;
        }
        for (InstitutionConfigurationObject ico : getAllowedStatisticsPlugins()) {
            if (ico.getObject_name().equals(pluginName)) {
                return ico.isSelected();
            }
        }
        return false;
    }

    public boolean isDashboardPluginAllowed(String pluginName) {
        if (isAllowAllPlugins()) {
            return true;
        }
        for (InstitutionConfigurationObject ico : getAllowedDashboardPlugins()) {
            if (ico.getObject_name().equals(pluginName)) {
                return ico.isSelected();
            }
        }
        return false;
    }

    public void addLogEntry() {
        if (uploadedFile != null) {
            saveUploadedFile();
        } else {
            LoginBean loginForm = Helper.getLoginBean();

            JournalEntry entry =
                    new JournalEntry(id, new Date(), loginForm.getMyBenutzer().getNachVorname(), LogType.USER, content, EntryType.INSTITUTION);

            content = "";

            journal.add(entry);

            JournalManager.saveJournalEntry(entry);
        }
    }

    /**
     * List the files of a selected folder. If a LogEntry is used (because it was uploaded in the logfile area), it will be used. Otherwise a
     * temporary LogEntry is created.
     * 
     * @return
     */

    public List<JournalEntry> getFilesInSelectedFolder() {

        String currentFolder = importFolder + shortName;

        List<Path> files = StorageProvider.getInstance().listFiles(currentFolder);
        List<JournalEntry> answer = new ArrayList<>();
        // check if LogEntry exist
        for (Path file : files) {
            boolean matchFound = false;
            for (JournalEntry entry : journal) {
                if (entry.getType() == LogType.FILE && StringUtils.isNotBlank(entry.getFilename()) && entry.getFilename().equals(file.toString())) {
                    entry.setFile(file);
                    answer.add(entry);
                    matchFound = true;
                    break;
                }
            }
            // otherwise create one
            if (!matchFound) {
                JournalEntry entry = new JournalEntry(id, new Date(), "", LogType.USER, "", EntryType.INSTITUTION);
                entry.setFilename(file.toString()); // absolute path
                entry.setFile(file);
                answer.add(entry);
            }
        }

        return answer;
    }

    /**
     * Download a selected file
     * 
     * @param entry
     */

    public void downloadFile(JournalEntry entry) {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

        Path path = entry.getFile();
        if (path == null) {
            path = Paths.get(entry.getFilename());
        }
        String fileName = path.getFileName().toString();
        String contentType = facesContext.getExternalContext().getMimeType(fileName);
        try {
            int contentLength = (int) StorageProvider.getInstance().getFileSize(path);
            response.reset();
            response.setContentType(contentType);
            response.setContentLength(contentLength);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            OutputStream output = response.getOutputStream();
            try (InputStream inp = StorageProvider.getInstance().newInputStream(path)) {
                IOUtils.copy(inp, output);
            }
            facesContext.responseComplete();
        } catch (IOException e) {
            log.error(e);
        }
    }

    /**
     * Delete a LogEntry and the file belonging to it
     * 
     * @param entry
     */

    public void deleteFile(JournalEntry entry) {
        Path path = entry.getFile();
        if (path == null) {
            path = Paths.get(entry.getFilename());
        }
        // check if log entry has an id
        if (entry.getId() != null) {
            // if yes, delete entry
            String filename = entry.getBasename();

            journal.remove(entry);
            JournalManager.deleteJournalEntry(entry);

            // create a new entry to document the deletion

            JournalEntry deletionInfo = new JournalEntry(id, new Date(), Helper.getCurrentUser().getNachVorname(), LogType.INFO,
                    Helper.getTranslation("processlogFileDeleted", filename), EntryType.INSTITUTION);

            journal.add(deletionInfo);
            JournalManager.saveJournalEntry(deletionInfo);
        }
        // delete file
        try {
            StorageProvider.getInstance().deleteFile(path);
        } catch (IOException e) {
            log.error(e);
        }

    }

    /**
     * Save the previous uploaded file in the selected process directory and create a new LogEntry.
     * 
     */

    public void saveUploadedFile() {

        Path folder = Paths.get(importFolder + shortName);
        try {

            if (!StorageProvider.getInstance().isFileExists(folder)) {
                StorageProvider.getInstance().createDirectories(folder);
            }
            Path destination = Paths.get(folder.toString(), basename);
            StorageProvider.getInstance().move(tempFileToImport, destination);

            JournalEntry entry =
                    new JournalEntry(id, new Date(), Helper.getCurrentUser().getNachVorname(), LogType.FILE, content, EntryType.INSTITUTION);
            entry.setFilename(destination.toString());
            JournalManager.saveJournalEntry(entry);
            journal.add(entry);

        } catch (IOException e) {
            log.error(e);
        }
        uploadedFile = null;
        content = "";
    }

    /**
     * Upload a file and save it as a temporary file
     * 
     */
    public void uploadFile() {
        if (this.uploadedFile == null) {
            Helper.setFehlerMeldung("noFileSelected");
            return;
        }

        basename = getFileName(this.uploadedFile);
        if (basename.startsWith(".")) {
            basename = basename.substring(1);
        }
        if (basename.contains("/")) {
            basename = basename.substring(basename.lastIndexOf("/") + 1);
        }
        if (basename.contains("\\")) {
            basename = basename.substring(basename.lastIndexOf("\\") + 1);
        }
        basename = Paths.get(basename).getFileName().toString();

        try {
            tempFileToImport = Files.createTempFile(basename, ""); //NOSONAR, using temporary file is save here

            try (InputStream inputStream = this.uploadedFile.getInputStream();
                    OutputStream outputStream = new FileOutputStream(tempFileToImport.toString())) {

                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            Helper.setFehlerMeldung("uploadFailed");
        }
    }

    /**
     * extract the filename for the uploaded file
     * 
     * @param part
     * @return
     */

    private String getFileName(final Part part) {
        for (String contentDisposition : part.getHeader("content-disposition").split(";")) {
            if (contentDisposition.trim().startsWith("filename")) {
                return contentDisposition.substring(contentDisposition.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "";
    }
}
