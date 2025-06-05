package de.sub.goobi.export.dms;

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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.goobi.beans.Process;
import org.goobi.beans.ProjectFileGroup;
import org.goobi.beans.User;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IExportPlugin;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.export.download.ExportMets;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.metadaten.MetadatenVerifizierung;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DocStruct;
import ugh.dl.ExportFileformat;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

@Log4j2
public class ExportDms extends ExportMets implements IExportPlugin {
    private static final long serialVersionUID = -8965539133582826845L;
    protected boolean exportWithImages = true;
    @Setter
    protected boolean exportFulltext = true;

    public static final String DIRECTORY_SUFFIX = "_media";

    private static final String EXPORT_ERROR_PREFIX = "Export cancelled: ";

    public ExportDms() {
    }

    public ExportDms(boolean exportImages) {
        this.exportWithImages = exportImages;
    }

    @Override
    public void setExportImages(boolean exportImages) {
        exportWithImages = exportImages;
    }

    /**
     * DMS-Export an eine gewünschte Stelle
     * 
     * @param myProzess
     * @param zielVerzeichnis
     * @throws InterruptedException
     * @throws IOException
     * @throws WriteException
     * @throws PreferencesException
     * @throws UghHelperException
     * @throws ExportFileException
     * @throws MetadataTypeNotAllowedException
     * @throws DocStructHasNoTypeException
     * @throws DAOException
     * @throws SwapException
     * @throws TypeNotAllowedForParentException
     */
    @Override
    public boolean startExport(Process myProzess, String inZielVerzeichnis) throws IOException, InterruptedException, WriteException,
            PreferencesException, DocStructHasNoTypeException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException,
            SwapException, DAOException, TypeNotAllowedForParentException {

        String errorMessageTitle = EXPORT_ERROR_PREFIX + "Process: " + myProzess.getTitel();

        this.myPrefs = myProzess.getRegelsatz().getPreferences();
        String atsPpnBand = myProzess.getTitel();

        /*
         * -------------------------------- Dokument einlesen --------------------------------
         */
        Fileformat gdzfile;
        Fileformat exportValidationFile;

        ExportFileformat exportValidationNewfile =
                MetadatenHelper.getExportFileformatByName(myProzess.getProjekt().getFileFormatDmsExport(), myProzess.getRegelsatz());

        try {
            gdzfile = myProzess.readMetadataFile();

            // Check for existing Export Validator, and if it exists, run the associated command
            if (myProzess.isConfiguredWithExportValidator()) {
                Helper.setMeldung(null, myProzess.getTitel() + ": ", "XML validation found");

                exportValidationNewfile.setDigitalDocument(gdzfile.getDigitalDocument());
                exportValidationFile = exportValidationNewfile;
                trimAllMetadata(exportValidationFile.getDigitalDocument().getLogicalDocStruct());
                Path temporaryFile = Paths.get(ConfigurationHelper.getInstance().getTemporaryFolder() + atsPpnBand + ".xml");
                writeMetsFile(myProzess, temporaryFile.toString(), exportValidationFile, false);

                String pathToGeneratedFile = ConfigurationHelper.getInstance().getTemporaryFolder() + atsPpnBand + ".xml";
                String command = myProzess.getExportValidator().getCommand();

                // replace {EXPORTFILE} keyword from configuration file
                final String exportTag = "{EXPORTFILE}";
                if (!command.contains(exportTag)) {
                    String details = "Export validation command does not contain required {EXPORTFILE} tag. Aborting export. Command: " + command;
                    Helper.setFehlerMeldung(errorMessageTitle, details);
                    Helper.addMessageToProcessJournal(myProzess.getId(), LogType.DEBUG, details);
                    log.warn(EXPORT_ERROR_PREFIX + details);
                    problems.add(EXPORT_ERROR_PREFIX + "Malformed export validation command.");
                    return false;
                }
                command = command.replace(exportTag, pathToGeneratedFile);

                Helper.addMessageToProcessJournal(myProzess.getId(), LogType.DEBUG, "Started export validation using command: " + command);

                if (!executeValidation(myProzess, temporaryFile, command)) {
                    return false;
                }
            }
            // throw away validation file and re-read the original data
            gdzfile = myProzess.readMetadataFile();

        } catch (Exception exception) { //NOSONAR InterruptedException must not be re-thrown as it is not running in a separate thread
            Helper.setFehlerMeldung(Helper.getTranslation("exportError") + myProzess.getTitel(), exception);
            log.error("Export abgebrochen, xml-LeseFehler", exception);
            problems.add(EXPORT_ERROR_PREFIX + exception.getMessage());
            return false;
        }

        trimAllMetadata(gdzfile.getDigitalDocument().getLogicalDocStruct());
        VariableReplacer replacer = new VariableReplacer(gdzfile.getDigitalDocument(), this.myPrefs, myProzess, null);

        /*
         * -------------------------------- Metadaten validieren --------------------------------
         */

        if (ConfigurationHelper.getInstance().isUseMetadataValidation()) {
            MetadatenVerifizierung mv = new MetadatenVerifizierung();
            if (!mv.validate(gdzfile, this.myPrefs, myProzess)) {
                String errorDetails = "The metadata could not be validated successfully.";
                Helper.setFehlerMeldung(errorMessageTitle, errorDetails);
                problems.add(EXPORT_ERROR_PREFIX + errorDetails);
                problems.addAll(mv.getProblems());
                return false;
            }
        }

        /*
         * -------------------------------- Speicherort vorbereiten und downloaden --------------------------------
         */
        String zielVerzeichnis;
        Path benutzerHome;
        if (myProzess.getProjekt().isUseDmsImport()) {
            zielVerzeichnis = myProzess.getProjekt().getDmsImportImagesPath();
            zielVerzeichnis = replacer.replace(zielVerzeichnis);
            benutzerHome = Paths.get(zielVerzeichnis);

            /* ggf. noch einen Vorgangsordner anlegen */
            if (myProzess.getProjekt().isDmsImportCreateProcessFolder()) {
                benutzerHome = Paths.get(benutzerHome.toString(), myProzess.getTitel());
                zielVerzeichnis = benutzerHome.toString();

                /* alte Import-Ordner löschen */
                if (!StorageProvider.getInstance().deleteDir(benutzerHome)) {
                    String errorDetails = "Import folder could not be cleared.";
                    Helper.setFehlerMeldung(errorMessageTitle, errorDetails);
                    problems.add(EXPORT_ERROR_PREFIX + errorDetails);
                    return false;
                }
                /* alte Success-Ordner löschen */
                String successPath = myProzess.getProjekt().getDmsImportSuccessPath();
                successPath = replacer.replace(successPath);
                Path successFile = Paths.get(successPath, myProzess.getTitel());
                if (!StorageProvider.getInstance().deleteDir(successFile)) {
                    String errorDetails = "Success folder could not be cleared.";
                    Helper.setFehlerMeldung(errorMessageTitle, errorDetails);
                    problems.add(EXPORT_ERROR_PREFIX + errorDetails);
                    return false;
                }
                /* alte Error-Ordner löschen */
                String importPath = myProzess.getProjekt().getDmsImportErrorPath();
                importPath = replacer.replace(importPath);
                Path errorfile = Paths.get(importPath, myProzess.getTitel());
                if (!StorageProvider.getInstance().deleteDir(errorfile)) {
                    String errorDetails = "Error folder could not be cleared.";
                    Helper.setFehlerMeldung(errorMessageTitle, errorDetails);
                    problems.add(EXPORT_ERROR_PREFIX + errorDetails);
                    return false;
                }

                if (!StorageProvider.getInstance().isFileExists(benutzerHome)) {
                    StorageProvider.getInstance().createDirectories(benutzerHome);
                }
            }

        } else {
            zielVerzeichnis = inZielVerzeichnis + atsPpnBand;
            zielVerzeichnis = replacer.replace(zielVerzeichnis) + FileSystems.getDefault().getSeparator();
            // wenn das Home existiert, erst löschen und dann neu anlegen
            benutzerHome = Paths.get(zielVerzeichnis);
            if (!StorageProvider.getInstance().deleteDir(benutzerHome)) {
                String errorDetails = "Could not delete home directory.";
                Helper.setFehlerMeldung(errorMessageTitle, errorDetails);
                problems.add(EXPORT_ERROR_PREFIX + errorDetails);
                return false;
            }
            prepareUserDirectory(zielVerzeichnis);
        }
        /*
         * -------------------------------- der eigentliche Download der Images --------------------------------
         */
        try {
            if (this.exportWithImages) {
                imageDownload(myProzess, benutzerHome, atsPpnBand, DIRECTORY_SUFFIX);
            }
            if (this.exportFulltext) {
                fulltextDownload(myProzess, benutzerHome, atsPpnBand, DIRECTORY_SUFFIX);
            }

            String ed = myProzess.getExportDirectory();
            Path exportFolder = Paths.get(ed);
            if (StorageProvider.getInstance().isFileExists(exportFolder) && StorageProvider.getInstance().isDirectory(exportFolder)) {
                List<Path> filesInExportFolder = StorageProvider.getInstance().listFiles(ed);

                for (Path exportFile : filesInExportFolder) {
                    if (StorageProvider.getInstance().isDirectory(exportFile)
                            && !StorageProvider.getInstance().list(exportFile.toString()).isEmpty()) {
                        if (!exportFile.getFileName().toString().matches(".+\\.\\d+")) {
                            String suffix = exportFile.getFileName().toString().substring(exportFile.getFileName().toString().lastIndexOf("_"));
                            Path destination = Paths.get(benutzerHome.toString(), atsPpnBand + suffix);
                            if (!StorageProvider.getInstance().isFileExists(destination)) {
                                StorageProvider.getInstance().createDirectories(destination);
                            }
                            List<Path> files = StorageProvider.getInstance().listFiles(exportFile.toString());
                            for (Path file : files) {
                                Path target = Paths.get(destination.toString(), file.getFileName().toString());
                                StorageProvider.getInstance().copyFile(file, target);
                            }
                        }
                    } else {
                        // if it is a regular file, export it to source folder
                        Path destination = Paths.get(benutzerHome.toString(), atsPpnBand + "_src");
                        if (!StorageProvider.getInstance().isFileExists(destination)) {
                            StorageProvider.getInstance().createDirectories(destination);
                        }
                        Path target = Paths.get(destination.toString(), exportFile.getFileName().toString());
                        StorageProvider.getInstance().copyFile(exportFile, target);
                    }

                }
            }
        } catch (AccessDeniedException exception) {
            String errorDetails = "Access to " + exception.getMessage() + " was denied.";
            Helper.setFehlerMeldung(errorMessageTitle, errorDetails);
            problems.add(EXPORT_ERROR_PREFIX + errorDetails);
            return false;
        } catch (Exception exception) { //NOSONAR InterruptedException must not be re-thrown as it is not running in a separate thread
            Helper.setFehlerMeldung(errorMessageTitle, exception);
            Helper.addMessageToProcessJournal(myProzess.getId(), LogType.ERROR, errorMessageTitle + "\n" + exception.getMessage());
            problems.add(EXPORT_ERROR_PREFIX + exception.getMessage());
            return false;
        }

        /*
         * -------------------------------- zum Schluss Datei an gewünschten Ort
         * exportieren entweder direkt in den Import-Ordner oder ins
         * Benutzerhome anschliessend den Import-Thread starten
         * --------------------------------
         */
        boolean externalExport =
                MetadatenHelper.getExportFileformatByName(myProzess.getProjekt().getFileFormatDmsExport(), myProzess.getRegelsatz()) != null;
        if (myProzess.getProjekt().isUseDmsImport()) {
            Helper.setMeldung(null, myProzess.getTitel() + ": ", "DMS-Export started");
            if (externalExport) {

                /* Wenn METS, dann per writeMetsFile schreiben... */
                writeMetsFile(myProzess, benutzerHome.toString() + "/" + atsPpnBand + ".xml", gdzfile, false);
            } else {
                /* ...wenn nicht, nur ein Fileformat schreiben. */
                gdzfile.write(benutzerHome.toString() + "/" + atsPpnBand + ".xml");
            }

            if (!ConfigurationHelper.getInstance().isExportWithoutTimeLimit()) {
                DmsImportThread agoraThread = new DmsImportThread(myProzess, atsPpnBand);
                agoraThread.start();
                try {
                    /* xx Sekunden auf den Thread warten, evtl. killen */
                    agoraThread.join(myProzess.getProjekt().getDmsImportTimeOut().longValue());
                    if (agoraThread.isAlive()) {
                        agoraThread.stopThread();
                    }
                } catch (InterruptedException e) {
                    Helper.setFehlerMeldung(myProzess.getTitel() + ": error on export - ", e.getMessage());
                    problems.add("Export problems: " + e.getMessage());
                    log.error(myProzess.getTitel() + ": error on export", e);
                    agoraThread.interrupt();
                }
                if (agoraThread.rueckgabe.length() > 0) {
                    Helper.setFehlerMeldung(myProzess.getTitel() + ": ", agoraThread.rueckgabe);
                } else {
                    Helper.setMeldung(null, myProzess.getTitel() + ": ", "ExportFinished");
                    /* Success-Ordner wieder löschen */
                    if (myProzess.getProjekt().isDmsImportCreateProcessFolder()) {
                        String sf = myProzess.getProjekt().getDmsImportSuccessPath();
                        sf = replacer.replace(sf);
                        Path successFile = Paths.get(sf, myProzess.getTitel());
                        StorageProvider.getInstance().deleteDir(successFile);
                    }
                }
            }
        } else {
            /* ohne Agora-Import die xml-Datei direkt ins Home schreiben */
            if (externalExport) {
                writeMetsFile(myProzess, zielVerzeichnis + atsPpnBand + ".xml", gdzfile, false);
            } else {
                gdzfile.write(zielVerzeichnis + atsPpnBand + ".xml");
            }

            Helper.setMeldung(null, myProzess.getTitel() + ": ", "ExportFinished");
        }
        return true;
    }

    private boolean executeValidation(Process myProzess, Path temporaryFile, String command) throws InterruptedException, IOException {

        Stream<String> errorStream = null;
        InputStreamReader errorStreamReader = null;
        BufferedReader reader = null;
        try {
            String[] com = { command };
            java.lang.Process exportValidationProcess = Runtime.getRuntime().exec(com);
            Integer exitVal = exportValidationProcess.waitFor();

            InputStream errorInputStream = exportValidationProcess.getErrorStream();
            errorStreamReader = new InputStreamReader(errorInputStream);
            reader = new BufferedReader(errorStreamReader);
            errorStream = reader.lines();
            String errorStreamAsString = errorStream.collect(Collectors.joining());

            // exitVal 0 indicates success, 1 indicates errors in the XML
            // errorStreamAsString represents STDERR. It should be completely empty, or else the command failed
            if (exitVal == 0 && errorStreamAsString.isBlank()) {
                Helper.setMeldung(null, myProzess.getTitel() + ": ", "XML validation completed successfully");
            } else {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("XML validation error for process \"");
                errorMessage.append(myProzess.getTitel());
                errorMessage.append("\" with validation command: \"");
                errorMessage.append(command);
                errorMessage.append("\", exit code was: ");
                errorMessage.append(exitVal.toString());
                String errorDetails = errorMessage.toString();
                Helper.setFehlerMeldung(EXPORT_ERROR_PREFIX + errorDetails, exitVal.toString());
                Helper.addMessageToProcessJournal(myProzess.getId(), LogType.DEBUG, errorDetails);
                log.error(EXPORT_ERROR_PREFIX + errorDetails);
                problems.add(EXPORT_ERROR_PREFIX + errorDetails);
                return false;
            }
        } catch (java.io.IOException e) {
            String errorDetails = "XML validation command could not be found. Command: " + command;
            Helper.setFehlerMeldung(EXPORT_ERROR_PREFIX + errorDetails);
            Helper.addMessageToProcessJournal(myProzess.getId(), LogType.DEBUG, errorDetails);
            log.error(EXPORT_ERROR_PREFIX + errorDetails);
            problems.add(EXPORT_ERROR_PREFIX + errorDetails);
            return false;
        } finally {
            // delete the now no longer required generated .xml
            if (StorageProvider.getInstance().isFileExists(temporaryFile)) {
                StorageProvider.getInstance().deleteFile(temporaryFile);
            }
            if (errorStream != null) {
                errorStream.close();
            }
            if (errorStreamReader != null) {
                errorStreamReader.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
        return true;
    }

    /**
     * run through all metadata and children of given docstruct to trim the strings calls itself recursively
     */
    protected void trimAllMetadata(DocStruct inStruct) {
        /* trimm all metadata values */
        if (inStruct.getAllMetadata() != null) {
            for (Metadata md : inStruct.getAllMetadata()) {
                if (md.getValue() != null) {
                    md.setValue(md.getValue().trim());
                }
            }
        }

        /* run through all children of docstruct */
        if (inStruct.getAllChildren() != null) {
            for (DocStruct child : inStruct.getAllChildren()) {
                trimAllMetadata(child);
            }
        }
    }

    public void fulltextDownload(Process myProzess, Path benutzerHome, String atsPpnBand, final String ordnerEndung)
            throws IOException, InterruptedException, SwapException, DAOException {

        // download sources
        Path sources = Paths.get(myProzess.getSourceDirectory());
        if (StorageProvider.getInstance().isFileExists(sources) && !StorageProvider.getInstance().list(sources.toString()).isEmpty()) {
            Path destination = Paths.get(benutzerHome.toString(), atsPpnBand + "_src");
            if (!StorageProvider.getInstance().isFileExists(destination)) {
                StorageProvider.getInstance().createDirectories(destination);
            }
            List<Path> dateien = StorageProvider.getInstance().listFiles(myProzess.getSourceDirectory());
            for (Path dir : dateien) {
                Path meinZiel = Paths.get(destination.toString(), dir.getFileName().toString());
                StorageProvider.getInstance().copyFile(dir, meinZiel);
            }
        }

        Path ocr = Paths.get(myProzess.getOcrDirectory());
        if (StorageProvider.getInstance().isFileExists(ocr)) {
            List<Path> folder = StorageProvider.getInstance().listFiles(myProzess.getOcrDirectory());
            for (Path dir : folder) {
                if (StorageProvider.getInstance().isDirectory(dir) && !StorageProvider.getInstance().list(dir.toString()).isEmpty()) {
                    String suffix = dir.getFileName().toString().substring(dir.getFileName().toString().lastIndexOf("_"));
                    Path destination = Paths.get(benutzerHome.toString(), atsPpnBand + suffix);
                    if (!StorageProvider.getInstance().isFileExists(destination)) {
                        StorageProvider.getInstance().createDirectories(destination);
                    }
                    List<Path> files = StorageProvider.getInstance().listFiles(dir.toString());
                    for (Path file : files) {
                        Path target = Paths.get(destination.toString(), file.getFileName().toString());
                        StorageProvider.getInstance().copyFile(file, target);
                    }
                }
            }
        }
    }

    public void imageDownload(Process myProzess, Path benutzerHome, String atsPpnBand, final String ordnerEndung)
            throws IOException, InterruptedException, SwapException, DAOException {

        /*
         * -------------------------------- dann den Ausgangspfad ermitteln --------------------------------
         */
        Path tifOrdner = Paths.get(myProzess.getImagesTifDirectory(false));

        /*
         * -------------------------------- jetzt die Ausgangsordner in die Zielordner kopieren --------------------------------
         */
        Path zielTif = Paths.get(benutzerHome.toString(), atsPpnBand + ordnerEndung);
        if (StorageProvider.getInstance().isFileExists(tifOrdner) && !StorageProvider.getInstance().list(tifOrdner.toString()).isEmpty()) {

            /* bei Agora-Import einfach den Ordner anlegen */
            if (myProzess.getProjekt().isUseDmsImport()) {
                if (!StorageProvider.getInstance().isFileExists(zielTif)) {
                    StorageProvider.getInstance().createDirectories(zielTif);
                }
            } else {
                /*
                 * wenn kein Agora-Import, dann den Ordner mit Benutzerberechtigung neu anlegen
                 */
                User myBenutzer = Helper.getCurrentUser();
                try {
                    if (myBenutzer == null) {
                        StorageProvider.getInstance().createDirectories(zielTif);
                    } else {
                        FilesystemHelper.createDirectoryForUser(zielTif.toString(), myBenutzer.getLogin());
                    }
                } catch (Exception exception) { //NOSONAR InterruptedException must not be re-thrown as it is not running in a separate thread
                    String errorDetails = "Could not create destination directory.";
                    Helper.setFehlerMeldung(EXPORT_ERROR_PREFIX + "Error", errorDetails);
                    log.error(errorDetails, exception);
                }
            }

            /* jetzt den eigentlichen Kopiervorgang */
            List<Path> files = StorageProvider.getInstance().listFiles(myProzess.getImagesTifDirectory(false), NIOFileUtils.DATA_FILTER);
            for (Path file : files) {
                Path target = Paths.get(zielTif.toString(), file.getFileName().toString());
                StorageProvider.getInstance().copyFile(file, target);

                //for 3d object files look for "helper files" with the same base name and copy them as well
                if (NIOFileUtils.objectNameFilter.accept(file)) {
                    copy3DObjectHelperFiles(myProzess, zielTif, file);
                }
            }
        }

        if (ConfigurationHelper.getInstance().isExportFilesFromOptionalMetsFileGroups()) {

            List<ProjectFileGroup> myFilegroups = myProzess.getProjekt().getFilegroups();
            if (myFilegroups != null && !myFilegroups.isEmpty()) {
                for (ProjectFileGroup pfg : myFilegroups) {
                    // check if source files exists
                    if (pfg.getFolder() != null && pfg.getFolder().length() > 0) {
                        Path folder = Paths.get(myProzess.getMethodFromName(pfg.getFolder()));
                        if (folder != null && StorageProvider.getInstance().isFileExists(folder)
                                && !StorageProvider.getInstance().list(folder.toString()).isEmpty()) {
                            List<Path> files = StorageProvider.getInstance().listFiles(folder.toString());
                            for (Path file : files) {
                                Path target = Paths.get(zielTif.toString(), file.getFileName().toString());
                                StorageProvider.getInstance().copyFile(file, target);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @param myProzess
     * @param zielTif
     * @param file
     * @throws IOException
     * @throws InterruptedException
     * @throws SwapException
     * @throws DAOException
     */
    public void copy3DObjectHelperFiles(Process myProzess, Path zielTif, Path file)
            throws IOException, InterruptedException, SwapException, DAOException {
        Path tiffDirectory = Paths.get(myProzess.getImagesTifDirectory(true));
        String baseName = FilenameUtils.getBaseName(file.getFileName().toString());
        List<Path> helperFiles = StorageProvider.getInstance()
                .listDirNames(tiffDirectory.toString())
                .stream()
                .filter(dirName -> dirName.equals(baseName))
                .map(tiffDirectory::resolve)
                .collect(Collectors.toList());
        for (Path helperFile : helperFiles) {
            Path helperTarget = Paths.get(zielTif.toString(), helperFile.getFileName().toString());
            if (StorageProvider.getInstance().isDirectory(helperFile)) {
                StorageProvider.getInstance().copyDirectory(helperFile, helperTarget);
            } else {
                StorageProvider.getInstance().copyFile(helperFile, helperTarget);
            }
        }
    }

    @Override
    public PluginType getType() {
        return PluginType.Export;
    }

    @Override
    public String getTitle() {
        return "ExportDms";
    }

    public String getDescription() {
        return getTitle();
    }
}
