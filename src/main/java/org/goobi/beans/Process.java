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
package org.goobi.beans;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.io.BackupFileManager;
import org.goobi.io.FileListFilter;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.enums.LogType;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.helper.tasks.ProcessSwapInTask;
import de.sub.goobi.metadaten.Image;
import de.sub.goobi.metadaten.ImageCommentPropertyHelper;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.metadaten.MetadatenSperrung;
import de.sub.goobi.persistence.managers.DocketManager;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UserManager;
import io.goobi.workflow.xslt.XsltPreparatorDocket;
import io.goobi.workflow.xslt.XsltPreparatorMetadata;
import io.goobi.workflow.xslt.XsltToPdf;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.UGHException;
import ugh.exceptions.WriteException;

@Log4j2
public class Process extends AbstractJournal implements Serializable, DatabaseObject, Comparable<Process>, IJournal, IPropertyHolder {
    private static final long serialVersionUID = -6503348094655786275L;

    private static final String META_FILE = "meta.xml";
    private static final String META_ANCHOR_FILE = "meta_anchor.xml";
    private static final String TEMP_FILE = "temp.xml";
    private static final String TEMP_ANCHOR_FILE = "temp_anchor.xml";
    private static final String TEMPLATE_FILE = "template.xml";
    private static final String FULLTEXT_FILE = "fulltext.xml";
    private static final String LOG_FILE_SUFFIX = "_log.xml";
    private static final String DOCKET_FILE = "docket.xsl";
    private static final String DOCKET_METADATA_FILE = "docket_metadata.xsl";

    private static final String MAIN_FOLDER = "main";
    private static final String MEDIA_FOLDER = "media";
    private static final String MASTER_FOLDER = "master";
    private static final String INTERN_FOLDER = "intern";

    @Getter
    @Setter
    private Integer id;
    @Getter
    @Setter
    private String titel;
    @Getter
    @Setter
    private String ausgabename;
    private Boolean istTemplate;
    private Boolean inAuswahllisteAnzeigen;
    @Setter
    private Project projekt;
    @Getter
    @Setter
    private Date erstellungsdatum;
    @Setter
    private List<Step> schritte;

    @Setter
    private List<GoobiProperty> eigenschaften;
    @Getter
    @Setter
    private String sortHelperStatus;
    @Setter
    private Integer sortHelperImages;
    @Setter
    private Integer sortHelperArticles;
    @Setter
    private Integer sortHelperMetadata;
    @Setter
    private Integer sortHelperDocstructs;
    @Getter
    @Setter
    private Date sortHelperLastStepCloseDate;
    @Getter
    @Setter
    private Ruleset regelsatz;
    @Getter
    @Setter
    private Batch batch;
    private Boolean swappedOut = false;
    private Boolean panelAusgeklappt = false;
    private Boolean selected = false;
    @Setter
    private Docket docket;
    @Setter
    private transient ExportValidator exportValidator = null;

    private String imagesTiffDirectory = null;
    private String imagesOrigDirectory = null;
    private String ocrAltoDirectory = null;
    private String ocrXmlDirectory = null;
    private String ocrTxtDirectory = null;
    private String ocrPdfDirectory = null;
    private String imagesSourceDirectory = null;
    private String importDirectory = null;
    private String exportDirectory = null;

    private BeanHelper bhelp = new BeanHelper();

    // temporär
    @Getter
    @Setter
    private Integer projectId;
    @Getter
    @Setter
    private Integer metadatenKonfigurationID;
    @Getter
    @Setter
    private Integer docketId;

    private final MetadatenSperrung msp = new MetadatenSperrung();
    Helper help = new Helper();

    private HashMap<String, String> tempVariableMap = new HashMap<>();

    @Getter
    @Setter
    private boolean mediaFolderExists = false;

    private transient List<StringPair> metadataList = new ArrayList<>();
    private String representativeImage = null;

    private List<SelectItem> folderList = new ArrayList<>();
    @Getter
    @Setter
    private String currentFolder;

    @Getter
    @Setter
    private String uploadFolder = INTERN_FOLDER;

    @Getter
    private boolean showFileDeletionButton;

    @Getter
    private boolean pauseAutomaticExecution;

    private static final Object xmlWriteLock = new Object();

    @Getter
    private EntryType entryType = EntryType.PROCESS;

    public Process() {
        this.swappedOut = false;
        this.titel = "";
        this.istTemplate = false;
        this.inAuswahllisteAnzeigen = false;
        this.eigenschaften = new ArrayList<>();
        this.schritte = new ArrayList<>();
        this.erstellungsdatum = new Date();

    }

    public void setIstTemplate(boolean istTemplate) {
        this.istTemplate = istTemplate;
    }

    public boolean isIstTemplate() {
        if (this.istTemplate == null) {
            this.istTemplate = Boolean.valueOf(false);
        }
        return this.istTemplate;
    }

    public List<Step> getSchritte() {
        if ((this.schritte == null || schritte.isEmpty()) && id != null) {
            schritte = StepManager.getStepsForProcess(id);
        }
        return this.schritte;
    }

    public boolean containsStepOfOrder(int order) {
        for (Step element : this.schritte) {
            if (element.getReihenfolge() == order) {
                return true;
            }
        }
        return false;
    }

    public boolean getContainsExportStep() {
        this.getSchritte();
        for (Step element : this.schritte) {
            if (element.isTypExportDMS() || element.isTypExportRus()) {
                return true;
            }
        }
        return false;
    }

    public List<GoobiProperty> getEigenschaften() {
        if ((eigenschaften == null || eigenschaften.isEmpty()) && id != null) {
            eigenschaften = PropertyManager.getPropertiesForObject(id, PropertyOwnerType.PROCESS);
        }
        return this.eigenschaften;
    }

    /*
     * Metadaten-Sperrungen zurückgeben
     */

    public User getBenutzerGesperrt() {
        User rueckgabe = null;
        if (MetadatenSperrung.isLocked(this.id.intValue())) {
            String benutzerID = this.msp.getLockBenutzer(this.id.intValue());
            try {
                rueckgabe = UserManager.getUserById(Integer.parseInt(benutzerID));
            } catch (Exception e) {
                Helper.setFehlerMeldung(Helper.getTranslation("userNotFound"), e);
            }
        }
        return rueckgabe;
    }

    public long getMinutenGesperrt() {
        return this.msp.getLockSekunden(this.id) / 60;
    }

    public long getSekundenGesperrt() {
        return this.msp.getLockSekunden(this.id) % 60;
    }

    public String getImagesTifDirectory(boolean useFallBack) throws IOException, SwapException {
        if (this.imagesTiffDirectory != null && StorageProvider.getInstance().isDirectory(Paths.get(this.imagesTiffDirectory))) {
            return this.imagesTiffDirectory;
        }
        Path dir = Paths.get(getImagesDirectory());

        /* nur die _tif-Ordner anzeigen, die nicht mir orig_ anfangen */

        String mediaFolder = VariableReplacer.simpleReplace(ConfigurationHelper.getInstance().getProcessImagesMainDirectoryName(), this);

        if (!StorageProvider.getInstance().isDirectory(Paths.get(dir.toString(), mediaFolder)) && useFallBack) {
            String configuredFallbackFolder = ConfigurationHelper.getInstance().getProcessImagesFallbackDirectoryName();
            if (StringUtils.isNotBlank(configuredFallbackFolder)) {
                String fallback = VariableReplacer.simpleReplace(configuredFallbackFolder, this);
                if (Files.exists(Paths.get(dir.toString(), fallback))) {
                    mediaFolder = fallback;
                }
            }
        }

        //if first fallback fails, fall back to largest thumbs folder if possible
        if (!StorageProvider.getInstance().isDirectory(Paths.get(dir.toString(), mediaFolder)) && useFallBack) {
            //fall back to largest thumbnail image
            java.nio.file.Path largestThumbnailDirectory = getThumbsDirectories(mediaFolder).entrySet()
                    .stream()
                    .sorted((entry1, entry2) -> entry2.getKey().compareTo(entry2.getKey()))
                    .map(Entry::getValue)
                    .map(Paths::get)
                    .filter(StorageProvider.getInstance()::isDirectory)
                    .findFirst()
                    .orElse(null);
            if (largestThumbnailDirectory != null) {
                return largestThumbnailDirectory.toString();
            }
        }

        String rueckgabe = getImagesDirectory() + mediaFolder;
        if (!rueckgabe.endsWith(FileSystems.getDefault().getSeparator())) {
            rueckgabe += FileSystems.getDefault().getSeparator();
        }
        if (!ConfigurationHelper.getInstance().isUseMasterDirectory() && ConfigurationHelper.getInstance().isCreateMasterDirectory()) {
            try {
                FilesystemHelper.createDirectory(rueckgabe);
            } catch (InterruptedException e) { //NOSONAR InterruptedException must not be re-thrown as it is not running in a separate thread
                log.error(e);
            }
        }
        this.imagesTiffDirectory = rueckgabe;
        return rueckgabe;
    }

    /*
     * @return true if the Tif-Image-Directory exists, false if not
     */
    public Boolean getTifDirectoryExists() {
        return mediaFolderExists;
    }

    public Boolean getDisplayMETSButton() {
        if ((sortHelperImages == null || sortHelperImages == 0) && ConfigurationHelper.getInstance().isMetsEditorValidateImages()) {
            return getTifDirectoryExists();
        }
        return true;
    }

    public Boolean getDisplayPDFButton() {
        if (sortHelperImages == null || sortHelperImages == 0) {
            return getTifDirectoryExists();
        }
        return true;
    }

    public Boolean getDisplayDMSButton() {
        boolean zeroDocStructs = sortHelperDocstructs == null || sortHelperDocstructs == 0;
        if (zeroDocStructs && ConfigurationHelper.getInstance().isExportValidateImages() && (sortHelperImages == null || sortHelperImages == 0)) {
            return getTifDirectoryExists();
        }
        return true;
    }

    public String getImagesOrigDirectory(boolean useFallBack) throws IOException, SwapException, DAOException {
        if (this.imagesOrigDirectory != null) {
            return this.imagesOrigDirectory;
        }
        if (ConfigurationHelper.getInstance().isUseMasterDirectory()) {
            Path dir = Paths.get(getImagesDirectory());

            /* nur die _tif-Ordner anzeigen, die mit orig_ anfangen */

            String masterFolder = VariableReplacer.simpleReplace(ConfigurationHelper.getInstance().getProcessImagesMasterDirectoryName(), this);

            if (!StorageProvider.getInstance().isDirectory(Paths.get(dir.toString(), masterFolder)) && useFallBack) {
                String configuredFallbackFolder = ConfigurationHelper.getInstance().getProcessImagesFallbackDirectoryName();
                if (StringUtils.isNotBlank(configuredFallbackFolder)) {
                    String fallback = VariableReplacer.simpleReplace(configuredFallbackFolder, this);
                    if (Files.exists(Paths.get(dir.toString(), fallback))) {
                        masterFolder = fallback;
                    }
                }
            }

            //if first fallback fails, fall back to largest thumbs folder if possible
            if (!StorageProvider.getInstance().isDirectory(Paths.get(dir.toString(), masterFolder)) && useFallBack) {
                //fall back to largest thumbnail image
                java.nio.file.Path largestThumbnailDirectory = getThumbsDirectories(masterFolder).entrySet()
                        .stream()
                        .sorted(Comparator.comparing(Entry::getKey))
                        .map(Entry::getValue)
                        .map(Paths::get)
                        .filter(StorageProvider.getInstance()::isDirectory)
                        .findFirst()
                        .orElse(null);
                if (largestThumbnailDirectory != null) {
                    return largestThumbnailDirectory.toString();
                }
            }

            String rueckgabe;
            if (!masterFolder.contains(FileSystems.getDefault().getSeparator())) {
                rueckgabe = getImagesDirectory() + masterFolder + FileSystems.getDefault().getSeparator();
            } else {
                rueckgabe = masterFolder;
            }
            if (ConfigurationHelper.getInstance().isUseMasterDirectory() && this.getSortHelperStatus() != "100000000"
                    && ConfigurationHelper.getInstance().isCreateMasterDirectory()) {
                try {
                    FilesystemHelper.createDirectory(rueckgabe);
                } catch (IOException | InterruptedException e) { //NOSONAR InterruptedException must not be re-thrown as it is not running in a separate thread
                    log.error(e);
                }
            }
            this.imagesOrigDirectory = rueckgabe;
            return rueckgabe;
        } else {
            return getImagesTifDirectory(useFallBack);
        }
    }

    /**
     * Get the image directory name matching the given thumbnail directory name. If the name ends with a number preceded by an underscore character,
     * then that part is removed from the returned name Otherwise, the whole name is returned
     *
     * @param thumbDirName
     * @return
     */
    public String getMatchingImageDir(String thumbDirName) {
        if (thumbDirName.matches(".*_\\d+")) {
            return thumbDirName.substring(0, thumbDirName.lastIndexOf("_"));
        } else {
            return thumbDirName;
        }
    }

    public String getImagesDirectory() throws IOException, SwapException {
        return getDirectory("images");
    }

    public String getDirectory(String folder) throws IOException, SwapException {
        String pfad = getProcessDataDirectory() + folder + FileSystems.getDefault().getSeparator();
        try {
            FilesystemHelper.createDirectory(pfad);
        } catch (InterruptedException e) { //NOSONAR InterruptedException must not be re-thrown as it is not running in a separate thread
            log.error(e);
        }
        return pfad;
    }

    public String getSourceDirectory() throws IOException, SwapException {
        if (imagesSourceDirectory == null) {
            Path sourceFolder = Paths.get(getImagesDirectory(),
                    VariableReplacer.simpleReplace(ConfigurationHelper.getInstance().getProcessImagesSourceDirectoryName(), this));
            if (ConfigurationHelper.getInstance().isCreateSourceFolder() && !StorageProvider.getInstance().isDirectory(sourceFolder)) {
                StorageProvider.getInstance().createDirectories(sourceFolder);
            }
            imagesSourceDirectory = sourceFolder.toString();
        }
        return imagesSourceDirectory;
    }

    public String getProcessDataDirectory() throws IOException, SwapException {
        String pfad = getProcessDataDirectoryIgnoreSwapping();

        if (isSwappedOutGui()) {
            ProcessSwapInTask pst = new ProcessSwapInTask();
            pst.initialize(this);
            pst.execute();
            if (pst.getStatusProgress() == -1) {
                if (!StorageProvider.getInstance().isFileExists(Paths.get(pfad, "images"))
                        && !StorageProvider.getInstance().isFileExists(Paths.get(pfad, META_FILE))) {
                    throw new SwapException(pst.getStatusMessage());
                } else {
                    setSwappedOutGui(false);
                }
            }
        }
        return pfad;
    }

    public String getOcrDirectory() throws SwapException, IOException {
        return getProcessDataDirectory() + "ocr" + FileSystems.getDefault().getSeparator();
    }

    public String getOcrTxtDirectory() throws SwapException, IOException {
        if (ocrTxtDirectory == null) {
            ConfigurationHelper config = ConfigurationHelper.getInstance();
            String separator = FileSystems.getDefault().getSeparator();
            ocrTxtDirectory = getOcrDirectory() + VariableReplacer.simpleReplace(config.getProcessOcrTxtDirectoryName(), this) + separator;
        }
        return ocrTxtDirectory;
    }

    /**
     * @deprecated This method is not used anymore
     *
     * @return
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public String getOcrWcDirectory() throws SwapException, IOException {
        return getOcrDirectory() + this.titel + "_wc" + FileSystems.getDefault().getSeparator();
    }

    public String getOcrPdfDirectory() throws SwapException, IOException {
        if (ocrPdfDirectory == null) {
            ConfigurationHelper config = ConfigurationHelper.getInstance();
            String separator = FileSystems.getDefault().getSeparator();
            ocrPdfDirectory = getOcrDirectory() + VariableReplacer.simpleReplace(config.getProcessOcrPdfDirectoryName(), this) + separator;
        }
        return ocrPdfDirectory;
    }

    public String getOcrAltoDirectory() throws SwapException, IOException {
        if (ocrAltoDirectory == null) {
            ConfigurationHelper config = ConfigurationHelper.getInstance();
            String separator = FileSystems.getDefault().getSeparator();
            ocrAltoDirectory = getOcrDirectory() + VariableReplacer.simpleReplace(config.getProcessOcrAltoDirectoryName(), this) + separator;
        }
        return ocrAltoDirectory;
    }

    public String getOcrXmlDirectory() throws SwapException, IOException {
        if (ocrXmlDirectory == null) {
            ConfigurationHelper config = ConfigurationHelper.getInstance();
            String separator = FileSystems.getDefault().getSeparator();
            ocrXmlDirectory = getOcrDirectory() + VariableReplacer.simpleReplace(config.getProcessOcrXmlDirectoryName(), this) + separator;
        }
        return ocrXmlDirectory;
    }

    public String getImportDirectory() throws SwapException, IOException {
        if (importDirectory == null) {
            ConfigurationHelper config = ConfigurationHelper.getInstance();
            String separator = FileSystems.getDefault().getSeparator();
            importDirectory = getProcessDataDirectory() + VariableReplacer.simpleReplace(config.getProcessImportDirectoryName(), this) + separator;
        }
        return importDirectory;
    }

    public String getExportDirectory() throws SwapException, IOException {
        if (exportDirectory == null) {
            ConfigurationHelper config = ConfigurationHelper.getInstance();
            String separator = FileSystems.getDefault().getSeparator();
            exportDirectory = getProcessDataDirectory() + VariableReplacer.simpleReplace(config.getProcessExportDirectoryName(), this) + separator;
        }
        return exportDirectory;
    }

    public String getProcessDataDirectoryIgnoreSwapping() throws IOException {
        String pfad = this.help.getGoobiDataDirectory() + this.id.intValue() + FileSystems.getDefault().getSeparator();
        if (!ConfigurationHelper.getInstance().isAllowWhitespacesInFolder()) {
            pfad = pfad.replace(" ", "__");
        }

        try {
            FilesystemHelper.createDirectory(pfad);
        } catch (IOException | InterruptedException e) { //NOSONAR InterruptedException must not be re-thrown as it is not running in a separate thread
            log.error(e);
        }
        return pfad;
    }

    /**
     * Get the process thumbnail directory which is located directly in the process directory and named 'thumbs'
     *
     * @return The full path to the thumbnail directory ending with a path separator
     * @throws IOException
     * @throws DAOException
     */
    public String getThumbsDirectory() throws IOException, SwapException {
        return getProcessDataDirectory() + "thumbs" + FileSystems.getDefault().getSeparator();
    }

    /**
     * Return all thumbnail directories containing thumbs for the images stored in the given images directory as a map hashed by the thumbnail size.
     * If no thumbnail directories exist, anfalse empty map is returned
     *
     * @param imageDirectory The path of an image directory, either only the name or the entire path.
     * @return A map of folders containing thumbnails for the given imageDirectory hashed by thumbnail size
     * @throws DAOException
     * @throws IOException
     */
    public Map<Integer, String> getThumbsDirectories(String imageDirectory) throws IOException, SwapException {
        final String thumbsDirectory = getThumbsDirectory();
        final String imageDirectoryFinal = Paths.get(imageDirectory).getFileName().toString(); //only use the directory name, not the entire path
        return StorageProvider.getInstance()
                .listDirNames(thumbsDirectory)
                .stream()
                .filter(dir -> dir.matches(imageDirectoryFinal + "_\\d{1,9}"))
                .collect(Collectors.toMap(dir -> Integer.parseInt(dir.substring(dir.lastIndexOf("_") + 1)), dir -> thumbsDirectory + dir));
    }

    /**
     * Return the thumbnail directory for the given imageDirectory containing images of the given size. If no directory exists for the given size then
     * the thumbnail directory with the closest larger image size is returned. If no such directory exists, null is returned
     *
     * @param imageDirectory The path of an image directory, either only the name or the entire path.
     * @param size The size of the desired thumbnails
     * @return The full path to thumbnail directory or null if no matching directory was found
     * @throws IOException
     * @throws InterruptedException
     * @throws SwapException
     * @throws DAOException
     */
    public String getThumbsDirectory(String imageDirectory, Integer size) throws IOException, InterruptedException, SwapException, DAOException {
        Map<Integer, String> dirMap = getThumbsDirectories(imageDirectory);
        Optional<Integer> bestSize = dirMap.keySet().stream().filter(dirSize -> dirSize >= size).sorted().findFirst();
        if (bestSize.isPresent()) {
            return dirMap.get(bestSize.get());
        } else {
            return null;
        }
    }

    /**
     * Return the thumbnail directory for the given imageDirectory containing images of the largest size. If no such directory exists, null is
     * returned
     *
     * @param imageDirectory The path of an image directory, either only the name or the entire path.
     * @return The full path to the largest thumbnail directory or null if no matching directory was found
     * @throws SwapException
     * @throws IOException
     */
    public String getLargestThumbsDirectory(String imageDirectory) throws IOException, SwapException {
        Map<Integer, String> dirMap = getThumbsDirectories(imageDirectory);
        Optional<Integer> bestSize = dirMap.keySet().stream().sorted((i1, i2) -> i2.compareTo(i1)).findFirst();
        if (bestSize.isPresent()) {
            return dirMap.get(bestSize.get());
        } else {
            return null;
        }
    }

    /**
     * Return the thumbnail directory for the given imageDirectory containing images of the given size. If no directory exists for the given size then
     * the thumbnail directory with the closest larger image size is returned. If no such directory exists, the thumbnail directory for the given
     * image directory with the largest thumbnails is returned
     *
     * @param imageDirectory The path of an image directory, either only the name or the entire path.
     * @param size The size of the desired thumbnails
     * @return The full path to thumbnail directory or null if no matching directory was found
     * @throws DAOException
     * @throws SwapException
     * @throws InterruptedException
     * @throws IOException
     */
    public String getThumbsDirectoryOrSmaller(String imageDirectory, Integer size)
            throws IOException, InterruptedException, SwapException, DAOException {
        String bestDir = getThumbsDirectory(imageDirectory, size);
        if (bestDir != null) {
            return bestDir;
        } else {
            return getThumbsDirectories(imageDirectory).entrySet()
                    .stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getKey(), e1.getKey()))
                    .findFirst()
                    .map(Entry::getValue)
                    .orElse(null);
        }
    }

    /**
     * Return the thumbnail directory for the given imageDirectory containing images of the given size. If no directory exists for the given size then
     * the thumbnail directory with the closest larger image size is returned. If no such directory exists, null is returned
     *
     * @param imageDirectory The path of an image directory, either only the name or the entire path.
     * @param size The size of the desired thumbnails
     * @return The full path to thumbnail directory or the full path to the given imageDirectory if no matching thumbnail directory was found
     * @throws DAOException
     * @throws SwapException
     * @throws InterruptedException
     * @throws IOException
     */
    public String getThumbsOrImageDirectory(String imageDirectory, Integer size)
            throws IOException, InterruptedException, SwapException, DAOException {
        String bestDir = getThumbsDirectory(imageDirectory, size);
        if (bestDir != null) {
            return bestDir;
        } else if (imageDirectory.startsWith(getImagesDirectory())) {
            return imageDirectory;
        } else {
            return getImagesDirectory() + imageDirectory;
        }
    }

    /**
     * Get the image sizes of all thumbnail folders for the given image Folder
     *
     * @param imageDirectory
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws SwapException
     * @throws DAOException
     */
    public List<Integer> getThumbsSizes(String imageDirectory) throws IOException, InterruptedException, SwapException, DAOException {
        return new ArrayList<>(getThumbsDirectories(imageDirectory).keySet());
    }

    /*
     * Helper
     */

    public Project getProjekt() {
        if (projekt == null && projectId != null) {
            try {
                projekt = ProjectManager.getProjectById(projectId);
            } catch (DAOException e) {
                log.error(e);
            }
        }
        return this.projekt;
    }

    public int getSchritteSize() {

        return getSchritte().size();

    }

    public List<Step> getSchritteList() {

        return getSchritte();
    }

    public int getEigenschaftenSize() {
        return getEigenschaften().size();
    }

    public List<GoobiProperty> getEigenschaftenList() {
        return getEigenschaften();
    }

    public Integer getSortHelperArticles() {
        if (this.sortHelperArticles == null) {
            this.sortHelperArticles = 0;
        }
        return this.sortHelperArticles;
    }

    public Integer getSortHelperImages() {
        if (this.sortHelperImages == null) {
            this.sortHelperImages = 0;
        }
        return this.sortHelperImages;
    }

    public Integer getSortHelperMetadata() {
        if (this.sortHelperMetadata == null) {
            this.sortHelperMetadata = 0;
        }
        return this.sortHelperMetadata;
    }

    public Integer getSortHelperDocstructs() {
        if (this.sortHelperDocstructs == null) {
            this.sortHelperDocstructs = 0;
        }
        return this.sortHelperDocstructs;
    }

    public boolean isInAuswahllisteAnzeigen() {
        return this.inAuswahllisteAnzeigen;
    }

    public void setInAuswahllisteAnzeigen(boolean inAuswahllisteAnzeigen) {
        this.inAuswahllisteAnzeigen = inAuswahllisteAnzeigen;
    }

    public boolean isPanelAusgeklappt() {
        return this.panelAusgeklappt;
    }

    public void setPanelAusgeklappt(boolean panelAusgeklappt) {
        this.panelAusgeklappt = panelAusgeklappt;
    }

    public Step getAktuellerSchritt() {
        for (Step step : getSchritteList()) {
            if (step.getBearbeitungsstatusEnum() == StepStatus.OPEN || step.getBearbeitungsstatusEnum() == StepStatus.INWORK
                    || step.getBearbeitungsstatusEnum() == StepStatus.INFLIGHT) {
                return step;
            }
        }
        return null;
    }

    public boolean isSelected() {
        return Boolean.TRUE.equals(this.selected); // returns true if and only if this.selected != null and this.selected == true
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getErstellungsdatumAsString() {
        return Helper.getDateAsFormattedString(this.erstellungsdatum);
    }

    /*
     * Auswertung des Fortschritts
     */

    public String getFortschritt() {
        int offen = 0;
        int inBearbeitung = 0;
        int abgeschlossen = 0;
        for (Step step : getSchritte()) {
            if (step.getBearbeitungsstatusEnum() == StepStatus.DONE || step.getBearbeitungsstatusEnum() == StepStatus.DEACTIVATED) {
                abgeschlossen++;
            } else if (step.getBearbeitungsstatusEnum() == StepStatus.LOCKED) {
                offen++;
            } else {
                inBearbeitung++;
            }
        }
        double offen2 = 0;
        double inBearbeitung2 = 0;
        double abgeschlossen2 = 0;

        if ((offen + inBearbeitung + abgeschlossen) == 0) {
            offen = 1;
        }
        offen2 = (offen * 100) / (double) (offen + inBearbeitung + abgeschlossen);
        inBearbeitung2 = (inBearbeitung * 100) / (double) (offen + inBearbeitung + abgeschlossen);
        abgeschlossen2 = 100 - offen2 - inBearbeitung2;
        java.text.DecimalFormat df = new java.text.DecimalFormat("#000");
        return df.format(abgeschlossen2) + df.format(inBearbeitung2) + df.format(offen2);

    }

    public double getFortschritt1() {
        return this.calculateProgressInPercent(StepStatus.LOCKED);
    }

    public double getFortschritt2() {
        return this.calculateProgressInPercent(StepStatus.INWORK);
    }

    public double getFortschrittError() {
        return this.calculateProgressInPercent(StepStatus.ERROR);
    }

    public double getFortschritt3() {
        return this.calculateProgressInPercent(StepStatus.DONE);
    }

    private double calculateProgressInPercent(StepStatus returnStatus) {
        double open = 0;
        double inWork = 0;
        double error = 0;
        double done = 0;
        for (Step step : getSchritte()) {
            switch (step.getBearbeitungsstatusEnum()) {
                case DONE:
                    done++;
                    break;
                case LOCKED:
                    open++;
                    break;
                case ERROR:
                    error++;
                    break;
                case DEACTIVATED:
                    // nothing
                    break;
                default:
                    inWork++;
            }
        }
        double sum = open + inWork + error + done;
        // This is to protect division by zero and to return OPEN - 100% in case of 0 processes
        if (sum == 0) {
            open = 1;
            sum = 1;
        }
        switch (returnStatus) {
            case DONE:
                return done * 100 / sum;
            case LOCKED:
                return open * 100 / sum;
            case ERROR:
                return error * 100 / sum;
            case DEACTIVATED:
                // nothing
                return 0;
            default:
                return inWork * 100 / sum;
        }
    }

    public String getMetadataFilePath() throws IOException, SwapException {
        return getProcessDataDirectory() + META_FILE;
    }

    public String getTemplateFilePath() throws IOException, SwapException {
        return getProcessDataDirectory() + TEMPLATE_FILE;
    }

    public String getFulltextFilePath() throws IOException, InterruptedException, SwapException, DAOException {
        return getProcessDataDirectory() + FULLTEXT_FILE;
    }

    public Fileformat readMetadataFile() throws ReadException, IOException, SwapException {
        if (!checkForMetadataFile()) {
            throw new IOException(Helper.getTranslation("metadataFileNotFound") + " " + getMetadataFilePath());
        }
        // TODO check for younger temp files

        /* prüfen, welches Format die Metadaten haben (Mets, xstream oder rdf */
        String type = MetadatenHelper.getMetaFileType(getMetadataFilePath());
        Fileformat ff = MetadatenHelper.getFileformatByName(type, regelsatz);
        if (ff == null) {
            String[] parameter = { titel, type };
            Helper.setFehlerMeldung(Helper.getTranslation("MetadataFormatNotAvailable", parameter));
            return null;
        }
        try {
            ff.read(getMetadataFilePath());
        } catch (ReadException e) {
            if (e.getMessage().startsWith("Parse error at line -1")) {
                Helper.setFehlerMeldung("metadataCorrupt");
            } else {
                throw e;
            }
        }
        return ff;
    }

    private boolean checkForMetadataFile() throws IOException, SwapException {
        boolean result = true;
        Path f = Paths.get(getMetadataFilePath());
        if (!StorageProvider.getInstance().isFileExists(f)) {
            result = false;
        }

        return result;
    }

    public synchronized boolean writeMetadataFile(Fileformat gdzfile) throws IOException, SwapException, WriteException, PreferencesException {

        String path = this.getProcessDataDirectory();
        int maximumNumberOfBackups = ConfigurationHelper.getInstance().getNumberOfMetaBackups();

        // Backup meta.xml
        Path metaFile = Paths.get(path, META_FILE);

        // cancel if less than 10 mb free storage is available
        if (Files.getFileStore(Paths.get(path)).getUsableSpace() < 10485760l) {
            Helper.setFehlerMeldung(Helper.getTranslation("process_writeErrorNoSpace"));
            log.error("Saving metadata for {} was cancelled because there is not enough storage available.", id);
            return false;
        }

        String backupMetaFileName = Process.createBackup(path, META_FILE, maximumNumberOfBackups);
        Path backupMetaFile = Paths.get(path + backupMetaFileName);

        // Backup meta_anchor.xml
        Path metaAnchorFile = Paths.get(path + META_ANCHOR_FILE);
        String backupMetaAnchorFileName = Process.createBackup(path, META_ANCHOR_FILE, maximumNumberOfBackups);
        Path backupMetaAnchorFile = Paths.get(path + backupMetaAnchorFileName);

        Fileformat ff = MetadatenHelper.getFileformatByName(getProjekt().getFileFormatInternal(), this.regelsatz);

        synchronized (xmlWriteLock) {
            ff.setDigitalDocument(gdzfile.getDigitalDocument());
            try {
                ff.write(path + META_FILE);
            } catch (UGHException ughException) {
                // Error while writing meta.xml or meta_anchor.xml. Restore backups and rethrow error

                // Restore meta.xml
                if ((!Files.exists(metaFile) || Files.size(metaFile) == 0) && Files.exists(backupMetaFile)) {
                    Files.copy(backupMetaFile, metaFile, StandardCopyOption.REPLACE_EXISTING);
                }

                // Restore meta_anchor.xml
                if ((!Files.exists(metaAnchorFile) || Files.size(metaAnchorFile) == 0) && Files.exists(backupMetaAnchorFile)) {
                    Files.copy(backupMetaAnchorFile, metaAnchorFile, StandardCopyOption.REPLACE_EXISTING);
                }

                throw ughException;
            }
        }
        Map<String, List<String>> metadata = MetadatenHelper.getMetadataOfFileformat(gdzfile, false);

        MetadataManager.updateMetadata(id, metadata);

        Map<String, List<String>> jsonMetadata = MetadatenHelper.getMetadataOfFileformat(gdzfile, true);

        MetadataManager.updateJSONMetadata(id, jsonMetadata);
        return true;
    }

    private static String createBackup(String path, String fileName, int maximumNumberOfBackups) {
        String backupFileName;
        try {
            backupFileName = BackupFileManager.createBackup(path, path, fileName, maximumNumberOfBackups, true);
            // Output in the GUI is already done in BackupFileManager.createBackup()
        } catch (IOException ioException) {
            backupFileName = null;
            // Output in the GUI is already done in BackupFileManager.createBackup()
        }
        return backupFileName;
    }

    public void saveTemporaryMetsFile(Fileformat gdzfile) throws SwapException, IOException, PreferencesException, WriteException {

        int maximumNumberOfBackups = ConfigurationHelper.getInstance().getNumberOfMetaBackups();
        Process.createBackup(this.getProcessDataDirectory(), TEMP_FILE, maximumNumberOfBackups);

        Fileformat ff = MetadatenHelper.getFileformatByName(getProjekt().getFileFormatInternal(), this.regelsatz);
        ff.setDigitalDocument(gdzfile.getDigitalDocument());
        ff.write(getProcessDataDirectory() + TEMP_FILE);
    }

    public void writeMetadataAsTemplateFile(Fileformat inFile) throws IOException, SwapException, WriteException, PreferencesException {
        inFile.write(getTemplateFilePath());
    }

    public Fileformat readMetadataAsTemplateFile() throws ReadException, IOException, PreferencesException, SwapException {
        if (StorageProvider.getInstance().isFileExists(Paths.get(getTemplateFilePath()))) {
            Fileformat ff = null;
            String type = MetadatenHelper.getMetaFileType(getTemplateFilePath());
            if (log.isDebugEnabled()) {
                log.debug("current template.xml file type: " + type);
            }
            ff = MetadatenHelper.getFileformatByName(type, regelsatz);
            ff.read(getTemplateFilePath());
            return ff;
        } else {
            throw new IOException("File does not exist: " + getTemplateFilePath());
        }
    }

    public void removeTemporaryMetadataFiles() {
        DirectoryStream.Filter<Path> filter = new FileListFilter("temp\\.?(_anchor)?.xml.*+");

        try {
            List<Path> temporaryFiles = StorageProvider.getInstance().listFiles(getProcessDataDirectory(), filter);
            if (!temporaryFiles.isEmpty()) {
                for (Path file : temporaryFiles) {
                    StorageProvider.getInstance().deleteDir(file);
                }
            }
        } catch (SwapException | IOException e) {
            log.error(e);
        }
    }

    public boolean getTemporaryMetadataFiles() {
        return checkForNewerTemporaryMetadataFiles();
    }

    public void setTemporaryMetadataFiles(boolean value) {
    }

    public void overwriteMetadata() {
        try {
            String path = this.getProcessDataDirectory();
            Path temporaryFile = Paths.get(path, TEMP_FILE);
            Path temporaryAnchorFile = Paths.get(path, TEMP_ANCHOR_FILE);

            int maximumNumberOfBackups = ConfigurationHelper.getInstance().getNumberOfMetaBackups();

            if (StorageProvider.getInstance().isFileExists(temporaryFile)) {
                // backup meta.xml
                Process.createBackup(path, META_FILE, maximumNumberOfBackups);

                // copy temp.xml to meta.xml
                Path meta = Paths.get(path, META_FILE);
                StorageProvider.getInstance().copyFile(temporaryFile, meta);
            }
            if (StorageProvider.getInstance().isFileExists(temporaryAnchorFile)) {
                // backup meta_anchor.xml
                Process.createBackup(path, META_ANCHOR_FILE, maximumNumberOfBackups);

                // copy temp_anchor.xml to meta_anchor.xml
                Path metaAnchor = Paths.get(path, META_ANCHOR_FILE);
                StorageProvider.getInstance().copyFile(temporaryAnchorFile, metaAnchor);
            }

        } catch (SwapException | IOException e) {
            log.error(e);
        }
    }

    public boolean checkForNewerTemporaryMetadataFiles() {
        try {
            Path temporaryFile = Paths.get(getProcessDataDirectory(), TEMP_FILE);
            if (StorageProvider.getInstance().isFileExists(temporaryFile)) {
                Path metadataFile = Paths.get(getMetadataFilePath());
                long tempTime = StorageProvider.getInstance().getLastModifiedDate(temporaryFile);
                long metaTime = StorageProvider.getInstance().getLastModifiedDate(metadataFile);
                return tempTime > metaTime;

            }
        } catch (SwapException | IOException e) {
            log.error(e);
        }

        return false;
    }

    /**
     * prüfen, ob der Vorgang Schritte enthält, die keinem Benutzer und keiner Benutzergruppe zugewiesen ist
     * ================================================================
     */
    public boolean getContainsUnreachableSteps() {
        if (getSchritteList().isEmpty()) {
            return true;
        }
        for (Step s : getSchritteList()) {
            if (s.getBenutzergruppenSize() == 0 && s.getBenutzerSize() == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * check if there is one task in edit mode, where the user has the rights to write to image folder
     * ================================================================
     */
    public boolean isImageFolderInUse() {
        for (Step s : getSchritteList()) {
            if (s.getBearbeitungsstatusEnum() == StepStatus.INWORK && s.isTypImagesSchreiben()) {
                return true;
            }
        }
        return false;
    }

    /**
     * get user of task in edit mode with rights to write to image folder ================================================================
     */
    public User getImageFolderInUseUser() {
        for (Step s : getSchritteList()) {
            if (s.getBearbeitungsstatusEnum() == StepStatus.INWORK && s.isTypImagesSchreiben()) {
                return s.getBearbeitungsbenutzer();
            }
        }
        return null;
    }

    /**
     * here different Getters and Setters for the same value, because Hibernate does not like bit-Fields with null Values (thats why Boolean) and
     * MyFaces seams not to like Boolean (thats why boolean for the GUI) ================================================================
     */
    public Boolean isSwappedOutHibernate() {
        return this.swappedOut;
    }

    public void setSwappedOutHibernate(Boolean inSwappedOut) {
        this.swappedOut = inSwappedOut;
    }

    public boolean isSwappedOutGui() {
        if (this.swappedOut == null) {
            this.swappedOut = false;
        }
        return this.swappedOut;
    }

    public void setSwappedOutGui(boolean inSwappedOut) {
        this.swappedOut = inSwappedOut;
    }

    public void downloadXML() {
        XsltPreparatorDocket xmlExport = new XsltPreparatorDocket();
        try {
            String ziel = Helper.getCurrentUser().getHomeDir() + getTitel() + LOG_FILE_SUFFIX;
            xmlExport.startExport(this, ziel);
        } catch (IOException e) {
            Helper.setFehlerMeldung("could not write logfile to home directory: ", e);
        } catch (InterruptedException e) { //NOSONAR InterruptedException must not be re-thrown as it is not running in a separate thread
            Helper.setFehlerMeldung("could not execute command to write logfile to home directory", e);
        }
    }

    /**
     * download xml logfile for current process
     */
    public void downloadLogFile() {

        XsltPreparatorDocket xmlExport = new XsltPreparatorDocket();
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {

            // write to servlet output stream
            try {
                ServletOutputStream out = Process.createOutputStream(getTitel() + LOG_FILE_SUFFIX);
                xmlExport.startExport(this, out);
                out.flush();
            } catch (IOException e) {
                log.error("IOException while exporting run note", e);
            }

            facesContext.responseComplete();
        }
    }

    public String downloadDocket() {

        if (log.isDebugEnabled()) {
            log.debug("generate docket for process " + this.id);
        }
        String rootpath = ConfigurationHelper.getInstance().getXsltFolder();
        Path xsltfile = Paths.get(rootpath, DOCKET_FILE);

        if (docket != null) {
            xsltfile = Paths.get(rootpath, docket.getFile());
            if (!StorageProvider.getInstance().isFileExists(xsltfile)) {
                Helper.setFehlerMeldung("docketMissing");
                return "";
            }
        }
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {

            // write run note to servlet output stream
            try {
                ServletOutputStream out = Process.createOutputStream(this.titel + ".pdf");
                XsltToPdf ern = new XsltToPdf();
                ern.startExport(this, out, xsltfile.toString(), new XsltPreparatorDocket());
                out.flush();
            } catch (IOException e) {
                log.error("IOException while exporting run note", e);
            }

            facesContext.responseComplete();
        }
        return "";
    }

    /**
     * generate simplified set of structure and metadata to provide a PDF generation for printing
     */
    public void downloadSimplifiedMetadataAsPDF() {
        log.debug("generate simplified metadata xml for process " + this.id);
        String rootpath = ConfigurationHelper.getInstance().getXsltFolder();
        Path xsltfile = Paths.get(rootpath, DOCKET_METADATA_FILE);

        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {

            // write simplified metadata to servlet output stream
            try {
                ServletOutputStream out = Process.createOutputStream(this.titel + ".pdf");
                XsltToPdf ern = new XsltToPdf();
                ern.startExport(this, out, xsltfile.toString(), new XsltPreparatorMetadata());
                out.flush();
            } catch (IOException e) {
                log.error("IOException while exporting simplified metadata", e);
            }

            facesContext.responseComplete();
        }
    }

    /**
     * Creates and returns an output stream for the current faces context. The file name parameter in the HTTP header is set to the given file name.
     * The file name parameter is then recommended in the save-dialog on the client's side.
     *
     * @param fileName The file name that should be used as default file name on the client side
     * @return The output stream of the current faces context
     * @throws IOException If the output stream could not be created
     */
    private static ServletOutputStream createOutputStream(String fileName) throws IOException {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
        String contentType = servletContext.getMimeType(fileName);
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
        return response.getOutputStream();
    }

    public Step getFirstOpenStep() {

        for (Step s : getSchritteList()) {
            if (StepStatus.OPEN.equals(s.getBearbeitungsstatusEnum()) || StepStatus.INWORK.equals(s.getBearbeitungsstatusEnum())
                    || s.getBearbeitungsstatusEnum() == StepStatus.INFLIGHT) {
                return s;
            }
        }
        return null;
    }

    public String getMethodFromName(String methodName) {
        java.lang.reflect.Method method;
        try {
            method = this.getClass().getMethod(methodName);
            Object o = method.invoke(this);
            return (String) o;
        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException
                | InvocationTargetException exception) {
            // this exception is expected
        }

        try {
            String imagefolder = this.getImagesDirectory();

            String foldername = VariableReplacer.simpleReplace(ConfigurationHelper.getInstance().getAdditionalProcessFolderName(methodName), this);
            if (StringUtils.isNotBlank(foldername)) {
                String folder = imagefolder + foldername;
                if (StorageProvider.getInstance().isFileExists(Paths.get(folder))) {
                    return folder;
                }
            }
        } catch (SwapException | IOException exception) {
            log.error(exception);
        }

        return null;
    }

    public Docket getDocket() {
        if (docket == null && docketId != null) {
            try {
                docket = DocketManager.getDocketById(docketId);
            } catch (DAOException e) {
                log.error(e);
            }
        }
        return docket;
    }

    public ExportValidator getExportValidator() {
        return exportValidator;
    }

    @Override
    public int compareTo(Process arg0) {

        return id.compareTo(arg0.getId());
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object.getClass().equals(this.getClass()))) {
            return false;
        } else if (object == this) {
            return true;
        }
        Process process = (Process) (object);
        return process.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
        // TODO: Implement this method fitting to all member variables
    }

    @Override
    public void lazyLoad() {
    }

    public Process(Process source) {
        swappedOut = false;
        istTemplate = false;
        inAuswahllisteAnzeigen = false;
        eigenschaften = new ArrayList<>();
        schritte = new ArrayList<>();
        erstellungsdatum = new Date();

        setDocket(source.getDocket());
        setExportValidator(source.getExportValidator());
        setInAuswahllisteAnzeigen(false);
        setIstTemplate(source.isIstTemplate());
        setProjectId(source.getProjectId());
        setProjekt(source.getProjekt());
        setRegelsatz(source.getRegelsatz());
        setSortHelperStatus(source.getSortHelperStatus());
        setTitel(source.getTitel() + "_copy");

        this.bhelp.SchritteKopieren(source, this);
        this.bhelp.EigenschaftenKopieren(source, this);
        LoginBean loginForm = Helper.getLoginBean();

        for (Step step : getSchritteList()) {

            step.setBearbeitungszeitpunkt(getErstellungsdatum());
            step.setEditTypeEnum(StepEditType.AUTOMATIC);
            if (loginForm != null) {
                step.setBearbeitungsbenutzer(loginForm.getMyBenutzer());
            }

            if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
                step.setBearbeitungsbeginn(getErstellungsdatum());
                // this concerns steps, which are set as done right on creation
                // bearbeitungsbeginn is set to creation timestamp of process
                // because the creation of it is basically begin of work
                Date myDate = new Date();
                step.setBearbeitungszeitpunkt(myDate);
                step.setBearbeitungsende(myDate);
            }

        }

        try {

            ProcessManager.saveProcess(this);
        } catch (DAOException e) {
            log.error("error on save: ", e);
        }
    }

    public List<StringPair> getMetadataList() {
        if (metadataList.isEmpty()) {
            metadataList = MetadataManager.getMetadata(id);
        }
        return metadataList;
    }

    public String getMetadataValue(String metadataName) {
        for (StringPair sp : getMetadataList()) {
            if (sp.getOne().equals(metadataName)) {
                return sp.getTwo();
            }
        }
        return "";
    }

    /**
     * getter for the representative as IIIF URL of the configured thumbnail size
     *
     * @return IIIF URL for the representative thumbnail image
     */
    public String getRepresentativeImage() {
        int thumbnailWidth = ConfigurationHelper.getInstance().getMetsEditorThumbnailSize();
        return getRepresentativeImage(thumbnailWidth);
    }

    /**
     * convert the path of the representative into a IIIF URL of the given size
     *
     * @param thumbnailWidth max width of the image
     * @return IIIF URL for the representative image
     */
    public String getRepresentativeImage(int thumbnailWidth) {
        try {
            String thumbnail = getRepresentativeImageAsString();
            Path imagePath = Paths.get(thumbnail);
            if (StorageProvider.getInstance().isFileExists(imagePath)) {
                Image image = new Image(this, imagePath.getParent().getFileName().toString(), imagePath.getFileName().toString(), 0, thumbnailWidth);
                return image.getThumbnailUrl();
            } else {
                FacesContext context = FacesContextHelper.getCurrentFacesContext();
                HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
                String scheme = request.getScheme(); // http
                String serverName = request.getServerName(); // hostname.com
                int serverPort = request.getServerPort(); // 80
                String contextPath = request.getContextPath(); // /mywebapp
                StringBuilder sb = new StringBuilder();
                sb.append(scheme);
                sb.append("://");
                sb.append(serverName);
                sb.append(":");
                sb.append(serverPort);
                sb.append(contextPath);
                sb.append("/");
                sb.append(thumbnail);
                sb.append("&amp;width=");
                sb.append(thumbnailWidth);
                sb.append("&amp;height=");
                sb.append(thumbnailWidth);
                return sb.toString();
            }
        } catch (IOException | SwapException | DAOException e) {
            log.error("Error creating representative image url for process " + this.getId());
            String rootpath = "cs?action=image&format=jpg&sourcepath=file:///";
            return rootpath + representativeImage.replace("\\\\", "/");
        }
    }

    /**
     * get the path of the representative as string from the filesystem
     *
     * @return path of representative image
     */
    public String getRepresentativeImageAsString() {
        if (StringUtils.isBlank(representativeImage)) {
            int imageNo = 0;
            if (!getMetadataList().isEmpty()) {
                for (StringPair sp : getMetadataList()) {
                    if ("_representative".equals(sp.getOne())) {
                        imageNo = NumberUtils.toInt(sp.getTwo()) - 1;
                    }
                }
            }
            try {
                List<Path> images = StorageProvider.getInstance().listFiles(getImagesTifDirectory(true), NIOFileUtils.imageOrPdfNameFilter);
                if (images == null || images.isEmpty()) {
                    images = StorageProvider.getInstance().listFiles(getImagesOrigDirectory(true), NIOFileUtils.imageOrPdfNameFilter);
                }
                if (images != null && !images.isEmpty()) {
                    representativeImage = images.get(imageNo).toString();
                } else {
                    images = StorageProvider.getInstance().listFiles(getImagesTifDirectory(true), NIOFileUtils.objectNameFilter);
                    if (images == null || images.isEmpty()) {
                        images = StorageProvider.getInstance().listFiles(getImagesOrigDirectory(true), NIOFileUtils.objectNameFilter);
                    }
                    if (images != null && !images.isEmpty()) {
                        return "uii/template/img/goobi_3d_object_placeholder.png?version=1";
                    }
                }

                if (StringUtils.isBlank(representativeImage)) {
                    return "uii/template/img/thumbnail-placeholder.png?version=1";
                }
            } catch (IOException | SwapException | DAOException e) {
                log.error(e);
            }
        }
        return representativeImage;
    }

    public Map<Path, List<Path>> getAllFolderAndFiles() {
        Map<Path, List<Path>> folderAndFileMap = new HashMap<>();
        try {
            List<Path> imageFolders = StorageProvider.getInstance().listFiles(getImagesDirectory(), NIOFileUtils.folderFilter);
            List<Path> thumbFolders = StorageProvider.getInstance().listFiles(getThumbsDirectory(), NIOFileUtils.folderFilter);
            List<Path> ocrFolders = StorageProvider.getInstance().listFiles(getOcrDirectory(), NIOFileUtils.folderFilter);

            for (Path folder : imageFolders) {
                folderAndFileMap.put(folder, StorageProvider.getInstance().listFiles(folder.toString(), NIOFileUtils.fileFilter));
                // add subfolders to the list as well (e.g. for LayoutWizzard)
                for (Path subfolder : StorageProvider.getInstance().listFiles(folder.toString(), NIOFileUtils.folderFilter)) {
                    folderAndFileMap.put(subfolder, StorageProvider.getInstance().listFiles(subfolder.toString(), NIOFileUtils.fileFilter));
                }
            }
            for (Path folder : thumbFolders) {
                folderAndFileMap.put(folder, StorageProvider.getInstance().listFiles(folder.toString(), NIOFileUtils.fileFilter));
            }
            for (Path folder : ocrFolders) {
                folderAndFileMap.put(folder, StorageProvider.getInstance().listFiles(folder.toString(), NIOFileUtils.fileFilter));
            }
        } catch (IOException | SwapException e) {

            log.error(e);
        }

        return folderAndFileMap;
    }

    // this method is needed for ajaxPlusMinusButton.xhtml
    public String getTitelLokalisiert() {
        return titel;
    }

    /**
     * return specific variable for this process adapted by the central VariableReplacer
     *
     * @param inVariable
     * @return adapted result with replaced value
     */
    public String getReplacedVariable(String inVariable) {
        // if replaced value is not stored already then do it now
        if (!tempVariableMap.containsKey(inVariable)) {
            DigitalDocument dd = null;
            Prefs myPrefs = null;
            // just load the mets file if needed
            if (inVariable.startsWith("{meta")) {
                myPrefs = getRegelsatz().getPreferences();
                try {
                    Fileformat gdzfile = readMetadataFile();
                    dd = gdzfile.getDigitalDocument();
                } catch (Exception e) {
                    log.error("error reading METS file for process " + id, e);
                }
            }
            VariableReplacer replacer = new VariableReplacer(dd, myPrefs, this, null);
            // put replaced value into temporary store
            String replacedValue = replacer.replace(inVariable);
            //  return empty string, if value could not be found
            if (replacedValue.equals(inVariable)) {
                replacedValue = "";
            }
            tempVariableMap.put(inVariable, replacedValue);
        }

        return tempVariableMap.get(inVariable);
    }

    /**
     * Get all Step titles for steps of a given status as String with a given separator
     *
     * @param status long value for the status (0=Locked, 1=Open, 2=InWork, 3=Done, 4=Error, 5=Deactivated)
     * @param separator String value to use as separator
     * @return status as String with a given separator
     */
    public String getStepsAsString(long status, String separator) {
        StringBuilder result = new StringBuilder();

        if (schritte != null && !schritte.isEmpty()) {
            for (Step s : schritte) {
                if (s.getBearbeitungsstatusEnum().getValue() == status) {
                    if (result.length() > 0) {
                        result.append(separator);
                    }
                    result.append(s.getTitelLokalisiert());
                }
            }
        }
        return result.toString();
    }

    /**
     * Get a list of folder names to select from. Not all folder can be selected.
     *
     * @return
     */

    public List<SelectItem> getVisibleFolder() {
        if (folderList.isEmpty()) {
            try {
                folderList.add(new SelectItem(getExportDirectory(), Helper.getTranslation("process_log_file_exportFolder")));
                folderList.add(new SelectItem(getImportDirectory(), Helper.getTranslation("process_log_file_importFolder")));
                folderList.add(new SelectItem(getImagesTifDirectory(false), Helper.getTranslation("process_log_file_mediaFolder")));
                if (ConfigurationHelper.getInstance().isUseMasterDirectory()) {
                    folderList.add(new SelectItem(getImagesOrigDirectory(false), Helper.getTranslation("process_log_file_masterFolder")));
                }

                Iterator<String> configuredImageFolder = ConfigurationHelper.getInstance().getLocalKeys("process.folder.images");
                while (configuredImageFolder.hasNext()) {
                    String keyName = configuredImageFolder.next();
                    String folderName = keyName.replace("process.folder.images.", "");
                    if (!MASTER_FOLDER.equals(folderName) && !MAIN_FOLDER.equals(folderName)) {
                        String folder = getConfiguredImageFolder(folderName);
                        if (StringUtils.isNotBlank(folder) && StorageProvider.getInstance().isFileExists(Paths.get(folder))) {
                            folderList.add(new SelectItem(folder, Helper.getTranslation(folderName)));
                        }
                    }
                }

            } catch (SwapException | DAOException | IOException e) {
                log.error(e);
            }
        }
        return folderList;
    }

    /**
     * List the files of a selected folder. If a LogEntry is used (because it was uploaded in the logfile area), it will be used. Otherwise a
     * temporary LogEntry is created.
     *
     * @return
     */

    @Override
    public Path getDownloadFolder() {
        return Paths.get(currentFolder);
    }

    @Override
    public List<JournalEntry> getFilesInSelectedFolder() {
        if (StringUtils.isBlank(currentFolder)) {
            return Collections.emptyList();
        }
        // enable/disable option to delete a file
        if (currentFolder.endsWith("/import/") || currentFolder.endsWith("/export/") || currentFolder.endsWith("_source")) {
            showFileDeletionButton = true;
        } else {
            showFileDeletionButton = false;
        }

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
                JournalEntry entry = new JournalEntry(id, new Date(), "", LogType.USER, "", EntryType.PROCESS);
                entry.setFilename(file.toString()); // absolute path
                entry.setFile(file);
                answer.add(entry);
            }
        }

        return answer;
    }

    /**
     * Save the previous uploaded file in the selected process directory and create a new LogEntry.
     *
     */

    @Override
    public void saveUploadedFile() {

        Path folder = null;
        try {
            if (INTERN_FOLDER.equals(uploadFolder)) {
                folder = Paths.get(getProcessDataDirectory(), ConfigurationHelper.getInstance().getFolderForInternalJournalFiles());
            } else {
                folder = Paths.get(getExportDirectory());
            }
            if (!StorageProvider.getInstance().isFileExists(folder)) {
                StorageProvider.getInstance().createDirectories(folder);
            }
            Path destination = Paths.get(folder.toString(), basename);
            StorageProvider.getInstance().move(tempFileToImport, destination);

            JournalEntry entry = new JournalEntry(id, new Date(), Helper.getCurrentUser().getNachVorname(), LogType.FILE, content, EntryType.PROCESS);
            entry.setFilename(destination.toString());
            JournalManager.saveJournalEntry(entry);
            journal.add(entry);

        } catch (SwapException | IOException e) {
            log.error(e);
        }
        uploadedFile = null;
        content = "";
    }

    /**
     * Change the process title and rename folders, property values ezc
     */

    public boolean changeProcessTitle(String newTitle) {

        /* Prozesseigenschaften */
        if (getEigenschaftenList() != null && !getEigenschaftenList().isEmpty()) {
            for (GoobiProperty pe : this.getEigenschaftenList()) {
                if (pe != null && pe.getPropertyValue() != null && pe.getPropertyValue().contains(this.getTitel())) {
                    pe.setPropertyValue(pe.getPropertyValue().replaceAll(this.getTitel(), newTitle));
                }
            }
        }

        try {
            // renaming image directories
            String imageDirectoryName = getImagesDirectory();
            Path imageDirectory = Paths.get(imageDirectoryName);
            if (StorageProvider.getInstance().isFileExists(imageDirectory) && StorageProvider.getInstance().isDirectory(imageDirectory)) {
                List<Path> subdirs = StorageProvider.getInstance().listFiles(imageDirectoryName);
                for (Path imagedir : subdirs) {
                    if (StorageProvider.getInstance().isDirectory(imagedir) || StorageProvider.getInstance().isSymbolicLink(imagedir)) {
                        StorageProvider.getInstance().move(imagedir, Paths.get(imagedir.toString().replace(getTitel(), newTitle)));
                    }
                }
            }
            // renaming ocr directories
            String ocrDirectoryName = getOcrDirectory();
            Path ocrDirectory = Paths.get(ocrDirectoryName);
            if (StorageProvider.getInstance().isFileExists(ocrDirectory) && StorageProvider.getInstance().isDirectory(ocrDirectory)) {
                List<Path> subdirs = StorageProvider.getInstance().listFiles(ocrDirectoryName);
                for (Path imagedir : subdirs) {
                    if (StorageProvider.getInstance().isDirectory(imagedir) || StorageProvider.getInstance().isSymbolicLink(imagedir)) {
                        StorageProvider.getInstance().move(imagedir, Paths.get(imagedir.toString().replace(getTitel(), newTitle)));
                    }
                }
            }
        } catch (Exception e) {
            log.trace("could not rename folder", e);
        }

        if (!this.isIstTemplate()) {
            /* delete old tiffwriter file, if it exists */
            try {
                Path tiffheaderfile = Paths.get(getImagesDirectory() + "tiffwriter.conf");
                if (StorageProvider.getInstance().isFileExists(tiffheaderfile)) {
                    StorageProvider.getInstance().deleteDir(tiffheaderfile);
                }
            } catch (IOException | SwapException e) {
                log.error(e);
            }

            // update paths in metadata file
            try {
                Fileformat fileFormat = readMetadataFile();

                UghHelper ughhelp = new UghHelper();
                MetadataType mdt = ughhelp.getMetadataType(this, "pathimagefiles");
                DocStruct physical = fileFormat.getDigitalDocument().getPhysicalDocStruct();
                List<? extends ugh.dl.Metadata> alleImagepfade = physical.getAllMetadataByType(mdt);
                if (!alleImagepfade.isEmpty()) {
                    for (Metadata md : alleImagepfade) {
                        fileFormat.getDigitalDocument().getPhysicalDocStruct().getAllMetadata().remove(md);
                    }
                }
                Metadata newmd = new Metadata(mdt);
                String newFolder = getImagesTifDirectory(false).replace(getTitel(), newTitle);
                if (SystemUtils.IS_OS_WINDOWS) {
                    newmd.setValue("file:/" + newFolder);
                } else {
                    newmd.setValue("file://" + newFolder);
                }
                fileFormat.getDigitalDocument().getPhysicalDocStruct().addMetadata(newmd);

                if (physical.getAllChildren() != null) {
                    for (DocStruct page : physical.getAllChildren()) {
                        List<ContentFile> contentFileList = page.getAllContentFiles();
                        if (contentFileList != null) {
                            for (ContentFile cf : contentFileList) {
                                cf.setLocation(cf.getLocation().replace(getTitel(), newTitle));
                            }
                        }
                    }
                }

                writeMetadataFile(fileFormat);

            } catch (IOException | SwapException | UghHelperException | UGHException e) {
                log.info("Could not rename paths in metadata file", e);
            }
        }
        /* Vorgangstitel */
        this.setTitel(newTitle);

        return true;
    }

    /**
     * get the complete path to a folder as string, or null if the folder name is unknown
     *
     * @param folderName
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws SwapException
     * @throws DAOException
     */

    public String getConfiguredImageFolder(String folderName) throws IOException, SwapException, DAOException {
        if (MASTER_FOLDER.equals(folderName)) {
            return getImagesOrigDirectory(false);
        } else if (MAIN_FOLDER.equals(folderName) || MEDIA_FOLDER.equals(folderName)) {
            return getImagesTifDirectory(false);
        }

        String folder = this.getImagesDirectory();
        String folderPath;
        if (folderName.contains(".")) {
            String[] split = folderName.split("\\.");
            if (split.length != 2) {
                throw new IllegalArgumentException("Hierarchy is not allowed for configured folders: " + folderName);
            }
            folder = this.getDirectory(split[0]);
            folderPath = VariableReplacer.simpleReplace(ConfigurationHelper.getInstance().getAdditionalProcessFolderName(split[0], split[1]), this);
        } else {
            folderPath = VariableReplacer.simpleReplace(ConfigurationHelper.getInstance().getAdditionalProcessFolderName(folderName), this);
        }
        if (StringUtils.isNotBlank(folderPath)) {
            return folder + folderPath;
        }
        // TODO: fix this NPE
        return null;
    }

    /**
     * Get the date of the last finished step
     */

    public String getLastStatusChangeDate() {
        Date date = null;
        if (ConfigurationHelper.getInstance().isProcesslistShowEditionData() && schritte != null) {
            for (Step step : schritte) {
                Date end = step.getBearbeitungsende();
                if (step.getBearbeitungsstatusEnum() == StepStatus.DONE && end != null && (date == null || date.before(end))) {
                    date = end;
                }
            }
        }
        return date == null ? "" : Helper.getDateAsFormattedString(date);
    }

    public String getDisplayLastStepCloseDate() {
        return sortHelperLastStepCloseDate == null ? "" : Helper.getDateAsFormattedString(sortHelperLastStepCloseDate);
    }

    /**
     * Get the user name of the last finished step
     */

    public String getLastStatusChangeUser() {
        String username = "";
        if (ConfigurationHelper.getInstance().isProcesslistShowEditionData()) {
            Step task = null;
            if (schritte != null) {
                for (Step step : schritte) {
                    boolean isDone = step.getBearbeitungsstatusEnum() == StepStatus.DONE;
                    Date stepEnd = step.getBearbeitungsende();
                    if (isDone && stepEnd != null && (task == null || task.getBearbeitungsende().before(stepEnd))) {
                        task = step;
                    }
                }
            }
            if (task != null) {
                if (task.getEditTypeEnum() == StepEditType.AUTOMATIC || task.getEditTypeEnum() == StepEditType.ADMIN) {
                    username = Helper.getTranslation(task.getEditTypeEnum().getTitle());
                } else if (task.getBearbeitungsbenutzer() != null) {
                    username = task.getBearbeitungsbenutzer().getNachVorname();
                }
            }
        }
        return username;
    }

    /**
     * Get the name of the last finished task
     *
     */

    public String getLastStatusChangeTask() {
        String taskname = "";
        if (ConfigurationHelper.getInstance().isProcesslistShowEditionData()) {
            Step task = null;
            if (schritte != null) {
                for (Step step : schritte) {
                    boolean isDone = step.getBearbeitungsstatusEnum() == StepStatus.DONE;
                    Date stepEnd = step.getBearbeitungsende();
                    if (isDone && stepEnd != null && (task == null || task.getBearbeitungsende().before(stepEnd))) {
                        task = step;
                    }
                }
            }
            if (task != null) {
                taskname = task.getNormalizedTitle();
            }
        }
        return taskname;
    }

    public void setPauseAutomaticExecution(boolean pauseAutomaticExecution) {
        List<Step> automaticTasks = new ArrayList<>();

        if (this.pauseAutomaticExecution && !pauseAutomaticExecution) {
            // search any open tasks; check if they are automatic tasks; start them
            for (Step step : schritte) {
                if (step.isTypAutomatisch()) {
                    switch (step.getBearbeitungsstatusEnum()) {
                        case DEACTIVATED:
                        case DONE:
                        case ERROR:
                        case LOCKED:
                            break;
                        case INFLIGHT:
                        case INWORK:
                        case OPEN:
                            automaticTasks.add(step);
                    }
                }
            }
        }
        this.pauseAutomaticExecution = pauseAutomaticExecution;
        if (!automaticTasks.isEmpty()) {
            for (Step step : automaticTasks) {
                //We need to set the process in the step to this process, so the step doesn't fetch the process from
                //the DB when it checks if automatic execution is paused
                step.setProzess(this);
                ScriptThreadWithoutHibernate script = new ScriptThreadWithoutHibernate(step);
                script.startOrPutToQueue();
            }
        }
    }

    // TODO: Think about order
    public List<ImageComment> getImageComments() throws IOException, InterruptedException, SwapException, DAOException {
        return new ImageCommentPropertyHelper(this).getAllComments();
    }

    public List<String> getArchivedImageFolders() throws IOException, InterruptedException, SwapException, DAOException {
        if (this.id == null || ConfigurationHelper.getInstance().useS3()) {
            return new ArrayList<>();
        }
        List<String> filesInImages = StorageProvider.getInstance().list(this.getImagesDirectory());
        return filesInImages.stream()
                .filter(p -> p.endsWith(".xml"))
                .map(p -> Paths.get(p).getFileName().toString().replace(".xml", ""))
                .collect(Collectors.toList());
    }

    /**
     * Tiny check to see whether a process is currently setup to perform XML Export Validation before the actual export.
     *
     * @return true if the process is set up to perform a pre-export validation, false if not
     */
    public boolean isConfiguredWithExportValidator() {
        return getExportValidator() != null && getExportValidator().getLabel() != null && getExportValidator().getCommand() != null;
    }

    /**
     *
     * @deprecated use getJournal() instead
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public List<JournalEntry> getProcessLog() {
        return journal;
    }

    /**
     *
     * @deprecated use setJournal() instead
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public void setProcessLog(List<JournalEntry> journal) {
        this.journal = journal;
    }

    @Override
    public void addJournalEntryForAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<GoobiProperty> getProperties() {
        return getEigenschaften();
    }

    @Override
    public void setProperties(List<GoobiProperty> properties) {
        setEigenschaften(properties);
    }
}