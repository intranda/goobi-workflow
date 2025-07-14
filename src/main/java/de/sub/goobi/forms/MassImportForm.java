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
package de.sub.goobi.forms;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.apache.jena.base.Sys;
import org.goobi.beans.Batch;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;
import org.goobi.goobiScript.GoobiScriptImport;
import org.goobi.goobiScript.GoobiScriptManager;
import org.goobi.goobiScript.GoobiScriptResult;
import org.goobi.production.enums.ImportFormat;
import org.goobi.production.enums.ImportReturnValue;
import org.goobi.production.enums.ImportType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.helper.JobCreation;
import org.goobi.production.importer.DocstructElement;
import org.goobi.production.importer.ImportObject;
import org.goobi.production.importer.Record;
import org.goobi.production.plugin.ImportPluginLoader;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.goobi.production.plugin.interfaces.IImportPluginVersion2;
import org.goobi.production.plugin.interfaces.IImportPluginVersion3;
import org.goobi.production.properties.DisplayProperty;
import org.goobi.production.properties.ImportProperty;
import org.goobi.production.properties.PropertyParser;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.XmlTools;
import de.sub.goobi.persistence.managers.ProcessManager;
import io.goobi.workflow.xslt.XsltToPdf;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.Prefs;

@Named("MassImportForm")
@WindowScoped
@Log4j2
public class MassImportForm implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 4780655212251185461L;

    private static final String docStructsGetter = "getCurrentDocStructs";

    @Inject // NOSONAR needs to be a field injection, as the been constructor does not allow arguments
    private GoobiScriptManager goobiScriptManager;
    private ImportFormat format = null;
    private final ImportPluginLoader ipl = new ImportPluginLoader();

    @Getter
    private String currentPlugin = "";

    private transient Path importFile = null;
    private final Helper help = new Helper();

    // progress bar
    @Setter
    private Integer progress = 0;
    private Integer currentProcessNo = 0;
    @Setter
    private Integer totalProcessNo = 0;

    @Getter
    private IImportPlugin plugin;
    @Getter
    @Setter
    private Process template;
    @Getter
    @Setter
    private List<Process> process;
    @Getter
    @Setter
    private List<Process> processList;
    @Getter
    @Setter
    private List<String> digitalCollections;
    @Getter
    @Setter
    private List<String> possibleDigitalCollection;
    @Getter
    @Setter
    private List<String> ids = new ArrayList<>();
    @Getter
    @Setter
    private List<String> usablePluginsForRecords = new ArrayList<>();
    @Getter
    @Setter
    private List<String> usablePluginsForIDs = new ArrayList<>();
    @Getter
    @Setter
    private List<String> usablePluginsForFiles = new ArrayList<>();
    @Getter
    @Setter
    private List<String> usablePluginsForFolder = new ArrayList<>();
    @Getter
    @Setter
    private String records = "";
    @Getter
    @Setter
    private String idList = "";
    @Getter
    @Setter
    private transient Part uploadedFile = null;
    @Getter
    @Setter
    private List<String> allFilenames = new ArrayList<>();
    @Getter
    @Setter
    private List<String> selectedFilenames = new ArrayList<>();

    @Getter
    private Batch batch;

    @Inject // NOSONAR needs to be a field injection, as the been constructor does not allow arguments
    private NavigationForm bean;

    @Getter
    private List<DisplayProperty> configuredProperties = new ArrayList<>();

    @PostConstruct
    public void init() {
        this.usablePluginsForRecords = this.ipl.getPluginsForType(ImportType.Record);
        this.usablePluginsForIDs = this.ipl.getPluginsForType(ImportType.ID);
        this.usablePluginsForFiles = this.ipl.getPluginsForType(ImportType.FILE);
        this.usablePluginsForFolder = this.ipl.getPluginsForType(ImportType.FOLDER);

    }

    public String prepare() {
        if (this.template.getContainsUnreachableSteps()) {
            if (this.template.getSchritteList().isEmpty()) {
                Helper.setFehlerMeldung("noStepsInWorkflow");
            }
            for (Step s : this.template.getSchritteList()) {
                if (s.getBenutzergruppenSize() == 0 && s.getBenutzerSize() == 0) {
                    Helper.setFehlerMeldung(Helper.getTranslation("noUserInStep", s.getTitel()));
                }
            }
            return "";
        }
        if (Boolean.TRUE.equals(this.template.getProjekt().getProjectIsArchived())) {
            Helper.setFehlerMeldung("projectIsArchived");
            return "";
        }
        configuredProperties = PropertyParser.getInstance().getProcessCreationProperties(template, template.getTitel());
        uploadedFile = null;

        initializePossibleDigitalCollections();
        // get navigationBean to set current tab and load the first selected plugin
        // The plugin will only be set if there is exactly one plugin in the concerning list.
        // Otherwise the user should select one in the GUI

        if (!this.usablePluginsForRecords.isEmpty()) {
            if (this.usablePluginsForRecords.size() == 1) {
                this.setCurrentPlugin(this.usablePluginsForRecords.get(0));
            }
            if (bean != null) {
                bean.setActiveImportTab("recordImport");
            }

        } else if (!usablePluginsForIDs.isEmpty()) {
            if (this.usablePluginsForIDs.size() == 1) {
                setCurrentPlugin(usablePluginsForIDs.get(0));
            }
            if (bean != null) {
                bean.setActiveImportTab("idImport");
            }

        } else if (!usablePluginsForFiles.isEmpty()) {
            if (this.usablePluginsForFiles.size() == 1) {
                setCurrentPlugin(usablePluginsForFiles.get(0));
            }
            if (bean != null) {
                bean.setActiveImportTab("uploadImport");
            }

        } else if (!usablePluginsForFolder.isEmpty()) {
            if (this.usablePluginsForFolder.size() == 1) {
                setCurrentPlugin(usablePluginsForFolder.get(0));
            }
            if (bean != null) {
                bean.setActiveImportTab("folder");
            }
        }

        return "process_import_1";
    }

    /**
     * generate a list with all possible collections for given project
     */

    private void initializePossibleDigitalCollections() {
        this.possibleDigitalCollection = new ArrayList<>();
        ArrayList<String> defaultCollections = new ArrayList<>();
        String filename = this.help.getGoobiConfigDirectory() + "goobi_digitalCollections.xml";
        if (!StorageProvider.getInstance().isFileExists(Paths.get(filename))) {
            Helper.setFehlerMeldung("File not found: ", filename);
            return;
        }
        this.digitalCollections = new ArrayList<>();
        try {
            /* Datei einlesen und Root ermitteln */
            SAXBuilder builder = XmlTools.getSAXBuilder();
            Document doc = builder.build(filename);
            Element root = doc.getRootElement();
            /* alle Projekte durchlaufen */
            String defaultValue = "default";
            List<Element> projekte = root.getChildren();
            for (Element projekt : projekte) {
                // collect default collections
                if (defaultValue.equals(projekt.getName())) {
                    List<Element> myCols = projekt.getChildren("DigitalCollection");
                    for (Element col : myCols) {
                        if (col.getAttribute(defaultValue) != null && "true".equalsIgnoreCase(col.getAttributeValue(defaultValue))) {
                            digitalCollections.add(col.getText());
                        }

                        defaultCollections.add(col.getText());
                    }
                } else {
                    // run through the projects
                    List<Element> projektnamen = projekt.getChildren("name");
                    for (Element projektname : projektnamen) {
                        // all all collections to list
                        if (projektname.getText().equalsIgnoreCase(this.template.getProjekt().getTitel())) {
                            List<Element> myCols = projekt.getChildren("DigitalCollection");
                            for (Element col : myCols) {
                                if (col.getAttribute(defaultValue) != null && "true".equalsIgnoreCase(col.getAttributeValue(defaultValue))) {
                                    digitalCollections.add(col.getText());
                                }

                                this.possibleDigitalCollection.add(col.getText());
                            }
                        }
                    }
                }
            }
        } catch (JDOMException | IOException e1) {
            log.error("error while parsing digital collections", e1);
            Helper.setFehlerMeldung("Error while parsing digital collections", e1);
        }

        if (this.possibleDigitalCollection.isEmpty()) {
            this.possibleDigitalCollection = defaultCollections;
        }
    }

    public String convertData() {
        this.processList = new ArrayList<>();
        if (StringUtils.isEmpty(currentPlugin)) {
            Helper.setFehlerMeldung("missingPlugin");
            return "";
        }

        if (testForData()) {
            // if the mass import plugin can be run as GoobiScript do it
            if (plugin instanceof IImportPluginVersion3) {
                IImportPluginVersion3 plugin3 = (IImportPluginVersion3) this.plugin;
                plugin3.setWorkflowName(template.getTitel());
            }
            if (this.plugin instanceof IImportPluginVersion2) {
                IImportPluginVersion2 plugin2 = (IImportPluginVersion2) this.plugin;
                if (plugin2.isRunnableAsGoobiScript()) {
                    GoobiScriptImport igs = new GoobiScriptImport();
                    igs.setMi(this);
                    igs.setAdditionalProperties(configuredProperties);
                    StringBuilder bld = new StringBuilder();
                    if (StringUtils.isNotEmpty(this.idList)) {
                        List<String> idsList = this.plugin.splitIds(this.idList);
                        for (String id : idsList) {
                            bld.append(id);
                            bld.append(",");
                        }
                    } else if (this.importFile != null) {
                        this.plugin.setFile(this.importFile.toFile());
                        List<Record> recordList = this.plugin.generateRecordsFromFile();
                        for (Record r : recordList) {
                            bld.append(r.getId());
                            bld.append(",");
                        }
                        igs.setRecords(recordList);
                    } else if (StringUtils.isNotEmpty(this.records)) {
                        List<Record> recordList = this.plugin.splitRecords(this.records);
                        for (Record r : recordList) {
                            bld.append(r.getId());
                            bld.append(",");
                        }
                        igs.setRecords(recordList);
                    } else if (!this.selectedFilenames.isEmpty()) {
                        List<Record> recordList = this.plugin.generateRecordsFromFilenames(this.selectedFilenames);
                        for (Record r : recordList) {
                            bld.append(r.getId());
                            bld.append(",");
                        }
                        igs.setRecords(recordList);
                    }
                    String myIdentifiers = bld.toString();
                    if (myIdentifiers.endsWith(",")) {
                        myIdentifiers = myIdentifiers.substring(0, myIdentifiers.lastIndexOf(","));
                    }

                    if (myIdentifiers.contains(",")) {
                        batch = new Batch();
                        ProcessManager.saveBatch(batch);
                    } else {
                        batch = null;
                    }

                    HashMap<String, String> myParameters = new HashMap<>();
                    myParameters.put("template", String.valueOf(this.template.getId()));
                    myParameters.put("identifiers", myIdentifiers);
                    myParameters.put("action", "import");
                    myParameters.put("plugin", plugin2.getTitle());
                    myParameters.put("projectId", String.valueOf(this.template.getProjectId()));

                    List<GoobiScriptResult> newScripts = igs.prepare(new ArrayList<>(),
                            "action:import plugin:" + plugin2.getTitle() + " template:" + this.template.getId() + " identifiers:" + myIdentifiers,
                            myParameters);
                    for (GoobiScriptResult gsr : newScripts) {
                        gsr.setCustomGoobiScriptImpl(igs);
                    }
                    if (!newScripts.isEmpty()) {
                        Helper.setMeldung("Import has started");
                        goobiScriptManager.enqueueScripts(newScripts);
                        goobiScriptManager.startWork();
                    }
                    return "";
                }
            }

            // if not runnable as GoobiScript run it in the regular MassImport GUI
            List<ImportObject> answer = new ArrayList<>();
            Batch localBatch = null; // I modified this variable's name so that it won't hide the field declared at line 164 anymore.
            // But I've no idea WTH we would need this, given that the field itself was also used at line 364. - Zehong
            // found list with ids
            Prefs prefs = this.template.getRegelsatz().getPreferences();
            String tempfolder = ConfigurationHelper.getInstance().getTemporaryFolder();
            this.plugin.setImportFolder(tempfolder);
            this.plugin.setPrefs(prefs);

            if (StringUtils.isNotEmpty(this.idList)) {
                List<String> idsList = this.plugin.splitIds(this.idList);
                List<Record> recordList = new ArrayList<>();
                for (String id : idsList) {
                    Record r = new Record();
                    r.setData(id);
                    r.setId(id);
                    r.setCollections(this.digitalCollections);
                    recordList.add(r);
                }
                totalProcessNo = recordList.size() * 2;
                answer = this.plugin.generateFiles(recordList);
            } else if (this.importFile != null) {
                // uploaded file
                this.plugin.setFile(this.importFile.toFile());
                List<Record> recordList = this.plugin.generateRecordsFromFile();
                for (Record r : recordList) {
                    r.setCollections(this.digitalCollections);
                }
                totalProcessNo = recordList.size() * 2;
                answer = this.plugin.generateFiles(recordList);
            } else if (StringUtils.isNotEmpty(this.records)) {
                // found list with records
                List<Record> recordList = this.plugin.splitRecords(this.records);
                for (Record r : recordList) {
                    r.setCollections(this.digitalCollections);
                }
                totalProcessNo = recordList.size() * 2;
                answer = this.plugin.generateFiles(recordList);
            } else if (!this.selectedFilenames.isEmpty()) {
                List<Record> recordList = this.plugin.generateRecordsFromFilenames(this.selectedFilenames);
                for (Record r : recordList) {
                    r.setCollections(this.digitalCollections);
                }
                totalProcessNo = recordList.size() * 2;
                answer = this.plugin.generateFiles(recordList);

            }

            if (answer.size() > 1) {
                localBatch = new Batch();
                ProcessManager.saveBatch(localBatch);
            }
            for (ImportObject io : answer) {

                if (localBatch != null && localBatch.getBatchId() != null) {
                    io.setBatch(localBatch);
                }
                if (ImportReturnValue.ExportFinished.equals(io.getImportReturnValue())) {
                    // if exist, add process properties
                    for (DisplayProperty prop : configuredProperties) {
                        @SuppressWarnings("deprecation")
                        Processproperty pe = new Processproperty();
                        pe.setPropertyName(prop.getValue());
                        pe.setPropertyValue(prop.getName());
                        pe.setContainer(prop.getContainer());
                        io.getProcessProperties().add(pe);
                    }

                    Process p = JobCreation.generateProcess(io, this.template);
                    if (p == null) {
                        boolean validImportFileName = StringUtils.isNotBlank(io.getImportFileName());
                        boolean validSelectedFileNames = selectedFilenames != null && !selectedFilenames.isEmpty();
                        if (validImportFileName && validSelectedFileNames && selectedFilenames.contains(io.getImportFileName())) {
                            selectedFilenames.remove(io.getImportFileName());
                        }
                        Helper.setFehlerMeldung("Import failed for " + io.getProcessTitle() + ", process generation failed");

                    } else {
                        Helper.setMeldung(ImportReturnValue.ExportFinished.getValue() + " for " + io.getProcessTitle());
                        this.processList.add(p);
                    }
                } else {
                    String[] parameter = { io.getProcessTitle(), io.getErrorMessage() };
                    Helper.setFehlerMeldung(Helper.getTranslation("importFailedError", parameter));
                    boolean validImportFileName = StringUtils.isNotBlank(io.getImportFileName());
                    boolean validSelectedFileNames = selectedFilenames != null && !selectedFilenames.isEmpty();
                    if (validImportFileName && validSelectedFileNames && selectedFilenames.contains(io.getImportFileName())) {
                        selectedFilenames.remove(io.getImportFileName());
                    }
                }
                currentProcessNo = currentProcessNo + 1;
            }
            if (answer.size() != this.processList.size()) {
                // some error on process generation, don't go to next page
                return "";
            }
        } else {
            Helper.setFehlerMeldung("missingData");
            return "";
        }
        currentProcessNo = 0;
        this.idList = null;
        if (this.importFile != null) {
            StorageProvider.getInstance().deleteDir(this.importFile);

            this.importFile = null;
        }
        if (selectedFilenames != null && !selectedFilenames.isEmpty()) {
            this.plugin.deleteFiles(this.selectedFilenames);
        }
        this.records = "";
        return "process_import_3";
    }

    /**
     * File upload with binary copying.
     */
    public void uploadFile() {

        if (this.uploadedFile == null) {
            Helper.setFehlerMeldung("noFileSelected");
            return;
        }

        String filename = this.createUploadFileName();

        try (InputStream inputStream = this.uploadedFile.getInputStream();
                OutputStream outputStream = new FileOutputStream(filename)) { // NOSONAR
            // filename is safe here, any prefix folder name from user input is removed from it (see basename above)

            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }

            this.importFile = Paths.get(filename);
            Helper.setMeldung("uploadSuccessful");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            Helper.setFehlerMeldung("uploadFailed");
        }
    }

    private String createUploadFileName() {
        String basename = getFileName(this.uploadedFile);
        if (basename.startsWith(".")) {
            basename = basename.substring(1);
        }
        if (basename.contains("/")) {
            basename = basename.substring(basename.lastIndexOf("/") + 1);
        }
        if (basename.contains("\\")) {
            basename = basename.substring(basename.lastIndexOf("\\") + 1);
        }
        return ConfigurationHelper.getInstance().getTemporaryFolder() + basename;
    }

    private String getFileName(final Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "";
    }

    /**
     * tests input fields for correct data
     *
     * @return true if data is valid or false otherwise
     */

    private boolean testForData() {
        return StringUtils.isNotBlank(this.idList) || StringUtils.isNotBlank(this.records) || this.importFile != null
                || !this.selectedFilenames.isEmpty();
    }

    /**
     *
     * @return list with all import formats
     */
    public List<String> getFormats() {
        List<String> l = new ArrayList<>();
        for (ImportFormat input : ImportFormat.values()) {
            l.add(input.getTitle());
        }
        return l;
    }

    /**
     * @deprecated This method is replaced by getFormat()
     *
     * @return The current format
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public String getCurrentFormat() {
        if (this.format != null) {
            return this.format.getTitle();
        } else {
            return "";
        }
    }

    /**
     * @deprecated This method is replaced by setFormat(String)
     *
     * @param formatTitle current format
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public void setCurrentFormat(String formatTitle) {
        this.format = ImportFormat.getTypeFromTitle(formatTitle);
    }

    /**
     * @deprecated This method is not used anymore
     *
     * @param processes The list of processes
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public void setProcesses(List<Process> processes) {
        this.process = processes;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format) {
        this.format = ImportFormat.getTypeFromTitle(format);
    }

    /**
     * @return the format
     */
    public String getFormat() {
        if (this.format == null) {
            return "";
        }
        return this.format.getTitle();
    }

    /**
     * @param currentPlugin the currentPlugin to set
     */
    public void setCurrentPlugin(String currentPlugin) {
        this.currentPlugin = currentPlugin;
        if (currentPlugin != null && currentPlugin.length() > 0) {
            // check if currentPlugin name contains a sub
            if (currentPlugin.contains(" -- ")) {
                String[] parts = currentPlugin.split(" -- ");
                // first part: plugin name
                plugin = (IImportPlugin) PluginLoader.getPluginByTitle(PluginType.Import, parts[0]);
                // second part: config name
                plugin.setConfigurationName(parts[1]);

            } else {
                this.plugin = (IImportPlugin) PluginLoader.getPluginByTitle(PluginType.Import, this.currentPlugin);
            }
            plugin.setForm(this);
            if (this.plugin.getImportTypes().contains(ImportType.FOLDER)) {
                this.allFilenames = this.plugin.getAllFilenames();
            }
            plugin.setPrefs(template.getRegelsatz().getPreferences());
        }
    }

    public boolean getHasNextPage() {
        java.lang.reflect.Method method;
        try {
            method = this.plugin.getClass().getMethod(docStructsGetter);
            Object o = method.invoke(this.plugin);
            @SuppressWarnings("unchecked")
            List<? extends DocstructElement> list = (List<? extends DocstructElement>) o;
            if (list != null) {
                return true;
            }
        } catch (Exception e) {
            // the case this.plugin == null will end up here
            // no need to do anything
        }
        try {
            method = this.plugin.getClass().getMethod("getProperties");
            Object o = method.invoke(this.plugin);
            @SuppressWarnings("unchecked")
            List<ImportProperty> list = (List<ImportProperty>) o;
            if (!list.isEmpty()) {
                return true;
            }
        } catch (Exception e) {
            // the case this.plugin == null will end up here
            // no need to do anything
        }
        return false;
    }

    public String nextPage() {
        if (!testForData()) {
            Helper.setFehlerMeldung("missingData");
            return "";
        }
        java.lang.reflect.Method method;
        try {
            method = this.plugin.getClass().getMethod(docStructsGetter);
            Object o = method.invoke(this.plugin);
            @SuppressWarnings("unchecked")
            List<? extends DocstructElement> list = (List<? extends DocstructElement>) o;
            if (list != null) {
                return "process_import_2_mass";
            }
        } catch (Exception e) {
            // the case this.plugin == null will end up here
            // no need to do anything
        }
        return "process_import_2";
    }

    public List<ImportProperty> getProperties() {

        if (this.plugin != null) {
            return this.plugin.getProperties();
        }
        return new ArrayList<>();
    }

    public String downloadDocket() {
        if (log.isDebugEnabled()) {
            log.debug("generate docket for process list");
        }
        String rootpath = ConfigurationHelper.getInstance().getXsltFolder();
        Path xsltfile = Paths.get(rootpath, "docket_multipage.xsl");
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            String fileName = "batch_" + processList.get(0).getBatch().getBatchId() + ".pdf";
            ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
            String contentType = servletContext.getMimeType(fileName);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");

            // write docket to servlet output stream
            try {
                ServletOutputStream out = response.getOutputStream();
                XsltToPdf ern = new XsltToPdf();
                ern.startExport(this.processList, out, xsltfile.toString());
                out.flush();
            } catch (IOException e) {
                log.error("IOException while exporting run note", e);
            }

            facesContext.responseComplete();
        }
        return "";
    }

    public List<? extends DocstructElement> getDocstructs() {
        java.lang.reflect.Method method;
        try {
            method = this.plugin.getClass().getMethod(docStructsGetter);
            Object o = method.invoke(this.plugin);
            @SuppressWarnings("unchecked")
            List<? extends DocstructElement> list = (List<? extends DocstructElement>) o;
            if (list != null) {
                return list;
            }
        } catch (Exception e) {
            // the case this.plugin == null will end up here
            // no need to do anything
        }
        return new ArrayList<>();
    }

    public String getPagePath() {
        java.lang.reflect.Method method;
        try {
            method = this.plugin.getClass().getMethod("getPagePath");
            Object o = method.invoke(this.plugin);
            if (o != null) {
                return (String) o; // path
            }

        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    public int getDocstructssize() {
        return getDocstructs().size();
    }

    /**
     * @deprecated This method is not used anymore
     *
     * @return The include path for a plugin page
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public String getInclude() {
        return "plugins/" + plugin.getTitle() + ".jsp";
    }

    public Integer getProgress() {
        if (progress == null) {
            progress = 0;
        } else if (totalProcessNo == 0) {
            progress = 100;
        } else {
            progress = (currentProcessNo * 100 / totalProcessNo);

            if (progress > 100) {
                progress = 100;
            }
        }
        return progress;
    }

    public void setCurrentProcess(Integer number) {
        currentProcessNo = number;
    }

    public void onComplete() {
        progress = null;
    }

    public boolean isShowProgressBar() {
        return progress != null && progress != 100 && progress != 0;
    }

    public void addProcessToProgressBar() {
        currentProcessNo = currentProcessNo + 1;
    }

    public Process cloneTemplate() {
        Process p = new Process();

        p.setIstTemplate(false);
        p.setInAuswahllisteAnzeigen(false);
        p.setProjekt(template.getProjekt());
        p.setRegelsatz(template.getRegelsatz());
        p.setDocket(template.getDocket());
        p.setExportValidator(template.getExportValidator());

        BeanHelper bHelper = new BeanHelper();
        bHelper.SchritteKopieren(template, p);
        bHelper.EigenschaftenKopieren(template, p);

        return p;
    }

    public boolean isHasUsablePluginsForRecords() {
        return !usablePluginsForRecords.isEmpty();
    }

    public boolean isHasUsablePluginsForIDs() {
        return !usablePluginsForIDs.isEmpty();
    }

    public boolean isHasUsablePluginsForFiles() {
        return !usablePluginsForFiles.isEmpty();
    }

    public boolean isHasUsablePluginsForFolder() {
        return !usablePluginsForFolder.isEmpty();
    }

}
