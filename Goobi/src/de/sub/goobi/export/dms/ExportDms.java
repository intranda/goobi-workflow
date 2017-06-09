package de.sub.goobi.export.dms;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.ProjectFileGroup;
import org.goobi.beans.User;

import ugh.dl.DocStruct;
import ugh.dl.ExportFileformat;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

import org.goobi.beans.Process;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IExportPlugin;

import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.export.download.ExportMets;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.metadaten.MetadatenVerifizierung;

public class ExportDms extends ExportMets implements IExportPlugin {
    private static final Logger logger = Logger.getLogger(ExportDms.class);
    ConfigProjects cp;
    private boolean exportWithImages = true;
    private boolean exportFulltext = true;
    private List<String> problems = new ArrayList<>();
    public final static String DIRECTORY_SUFFIX = "_tif";
    
    
    public ExportDms() {
    }

    public ExportDms(boolean exportImages) {
        this.exportWithImages = exportImages;
    }

    public void setExportFulltext(boolean exportFulltext) {
        this.exportFulltext = exportFulltext;
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

        this.myPrefs = myProzess.getRegelsatz().getPreferences();
        this.cp = new ConfigProjects(myProzess.getProjekt().getTitel());
        String atsPpnBand = myProzess.getTitel();
        
        /*
         * -------------------------------- Dokument einlesen --------------------------------
         */
        Fileformat gdzfile;
        //      Fileformat newfile;
        ExportFileformat newfile = MetadatenHelper.getExportFileformatByName(myProzess.getProjekt().getFileFormatDmsExport(), myProzess
                .getRegelsatz());
        try {
            gdzfile = myProzess.readMetadataFile();

            newfile.setDigitalDocument(gdzfile.getDigitalDocument());
            gdzfile = newfile;

        } catch (Exception e) {
            Helper.setFehlerMeldung(Helper.getTranslation("exportError") + myProzess.getTitel(), e);
            logger.error("Export abgebrochen, xml-LeseFehler", e);
            problems.add("Export cancelled: " + e.getMessage());
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
            	problems.add("Export cancelled because of validation errors");
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
                if (!NIOFileUtils.deleteDir(benutzerHome)) {
                    Helper.setFehlerMeldung("Export canceled, Process: " + myProzess.getTitel(), "Import folder could not be cleared");
                    problems.add("Export cancelled: Import folder could not be cleared.");
                    return false;
                }
                /* alte Success-Ordner löschen */
                String successPath = myProzess.getProjekt().getDmsImportSuccessPath();
                successPath = replacer.replace(successPath);
                Path successFile = Paths.get(successPath, myProzess.getTitel());
                if (!NIOFileUtils.deleteDir(successFile)) {
                    Helper.setFehlerMeldung("Export canceled, Process: " + myProzess.getTitel(), "Success folder could not be cleared");
                    problems.add("Export cancelled: Success folder could not be cleared.");
                    return false;
                }
                /* alte Error-Ordner löschen */
                String importPath = myProzess.getProjekt().getDmsImportErrorPath();
                importPath = replacer.replace(importPath);
                Path errorfile = Paths.get(importPath, myProzess.getTitel());
                if (!NIOFileUtils.deleteDir(errorfile)) {
                    Helper.setFehlerMeldung("Export canceled, Process: " + myProzess.getTitel(), "Error folder could not be cleared");
                    problems.add("Export cancelled: Error folder could not be cleared.");
                    return false;
                }

                if (!Files.exists(benutzerHome)) {
                    Files.createDirectories(benutzerHome);
                }
            }

        } else {
            zielVerzeichnis = inZielVerzeichnis + atsPpnBand;
            zielVerzeichnis = replacer.replace(zielVerzeichnis);
            // wenn das Home existiert, erst löschen und dann neu anlegen
            benutzerHome = Paths.get(zielVerzeichnis);
            if (!NIOFileUtils.deleteDir(benutzerHome)) {
                Helper.setFehlerMeldung("Export canceled: " + myProzess.getTitel(), "Could not delete home directory");
                problems.add("Export cancelled: Could not delete home directory.");
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
                fulltextDownload(myProzess, benutzerHome, atsPpnBand, DIRECTORY_SUFFIX);
            } else if (this.exportFulltext) {
                fulltextDownload(myProzess, benutzerHome, atsPpnBand, DIRECTORY_SUFFIX);
            }
            
           
            String ed = myProzess.getExportDirectory();
            ed = replacer.replace(ed);
            Path exportFolder = Paths.get(ed);
            if (Files.exists(exportFolder) && Files.isDirectory(exportFolder)) {
                List<Path> subdir = NIOFileUtils.listFiles(ed);

                for (Path dir : subdir) {
                    if (Files.isDirectory(dir) && !NIOFileUtils.list(dir.toString()).isEmpty()) {
                        if (!dir.getFileName().toString().matches(".+\\.\\d+")) {
                            String suffix = dir.getFileName().toString().substring(dir.getFileName().toString().lastIndexOf("_"));
                            Path destination = Paths.get(benutzerHome.toString(), atsPpnBand + suffix);
                            if (!Files.exists(destination)) {
                                Files.createDirectories(destination);
                            }
                            List<Path> files = NIOFileUtils.listFiles(dir.toString());
                            for (Path file : files) {
                                Path target = Paths.get(destination.toString(), file.getFileName().toString());
                                Files.copy(file, target, NIOFileUtils.STANDARD_COPY_OPTIONS);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("Export canceled, Process: " + myProzess.getTitel(), e);
            problems.add("Export cancelled: " + e.getMessage());
            return false;
        }

        /*
         * -------------------------------- zum Schluss Datei an gewünschten Ort
         * exportieren entweder direkt in den Import-Ordner oder ins
         * Benutzerhome anschliessend den Import-Thread starten
         * --------------------------------
         */
        boolean externalExport = MetadatenHelper.getExportFileformatByName(myProzess.getProjekt().getFileFormatDmsExport(), myProzess
                .getRegelsatz()) != null;
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
                    logger.error(myProzess.getTitel() + ": error on export", e);
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
                        NIOFileUtils.deleteDir(successFile);
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

    /**
     * run through all metadata and children of given docstruct to trim the strings calls itself recursively
     */
    private void trimAllMetadata(DocStruct inStruct) {
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

    public void fulltextDownload(Process myProzess, Path benutzerHome, String atsPpnBand, final String ordnerEndung) throws IOException,
            InterruptedException, SwapException, DAOException {

        // download sources
        Path sources = Paths.get(myProzess.getSourceDirectory());
        if (Files.exists(sources) && !NIOFileUtils.list(sources.toString()).isEmpty()) {
            Path destination = Paths.get(benutzerHome.toString(), atsPpnBand + "_src");
            if (!Files.exists(destination)) {
                Files.createDirectories(destination);
            }
            List<Path> dateien = NIOFileUtils.listFiles(myProzess.getSourceDirectory());
            for (Path dir : dateien) {
                Path meinZiel = Paths.get(destination.toString(), dir.getFileName().toString());
                Files.copy(dir, meinZiel, NIOFileUtils.STANDARD_COPY_OPTIONS);
            }
        }

        Path ocr = Paths.get(myProzess.getOcrDirectory());
        if (Files.exists(ocr)) {
            List<Path> folder = NIOFileUtils.listFiles(myProzess.getOcrDirectory());
            for (Path dir : folder) {
                if (Files.isDirectory(dir) && !NIOFileUtils.list(dir.toString()).isEmpty()) {
                    String suffix = dir.getFileName().toString().substring(dir.getFileName().toString().lastIndexOf("_"));
                    Path destination = Paths.get(benutzerHome.toString(), atsPpnBand + suffix);
                    if (!Files.exists(destination)) {
                        Files.createDirectories(destination);
                    }
                    List<Path> files = NIOFileUtils.listFiles(dir.toString());
                    for (Path file : files) {
                        Path target = Paths.get(destination.toString(), file.getFileName().toString());
                        Files.copy(file, target, NIOFileUtils.STANDARD_COPY_OPTIONS);
                    }
                }
            }
        }
    }

    public void imageDownload(Process myProzess, Path benutzerHome, String atsPpnBand, final String ordnerEndung) throws IOException,
            InterruptedException, SwapException, DAOException {

        /*
         * -------------------------------- dann den Ausgangspfad ermitteln --------------------------------
         */
        Path tifOrdner = Paths.get(myProzess.getImagesTifDirectory(true));

        /*
         * -------------------------------- jetzt die Ausgangsordner in die Zielordner kopieren --------------------------------
         */
        Path zielTif = Paths.get(benutzerHome.toString(), atsPpnBand + ordnerEndung);
        if (Files.exists(tifOrdner) && !NIOFileUtils.list(tifOrdner.toString()).isEmpty()) {

            /* bei Agora-Import einfach den Ordner anlegen */
            if (myProzess.getProjekt().isUseDmsImport()) {
                if (!Files.exists(zielTif)) {
                    Files.createDirectories(zielTif);
                }
            } else {
                /*
                 * wenn kein Agora-Import, dann den Ordner mit Benutzerberechtigung neu anlegen
                 */
                User myBenutzer = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
                try {
                	if (myBenutzer == null){
                		Files.createDirectories(zielTif);
                	}else{
                		FilesystemHelper.createDirectoryForUser(zielTif.toString(), myBenutzer.getLogin());
                	}
                } catch (Exception e) {
                    Helper.setFehlerMeldung("Export canceled, error", "could not create destination directory");
                    logger.error("could not create destination directory", e);
                }
            }

            /* jetzt den eigentlichen Kopiervorgang */
            List<Path> files = NIOFileUtils.listFiles(myProzess.getImagesTifDirectory(true), NIOFileUtils.DATA_FILTER);
            for (Path file : files) {
                Path target = Paths.get(zielTif.toString(), file.getFileName().toString());
                Files.copy(file, target, NIOFileUtils.STANDARD_COPY_OPTIONS);
            }
        }

        if (ConfigurationHelper.getInstance().isExportFilesFromOptionalMetsFileGroups()) {

            List<ProjectFileGroup> myFilegroups = myProzess.getProjekt().getFilegroups();
            if (myFilegroups != null && myFilegroups.size() > 0) {
                for (ProjectFileGroup pfg : myFilegroups) {
                    // check if source files exists
                    if (pfg.getFolder() != null && pfg.getFolder().length() > 0) {
                        Path folder = Paths.get(myProzess.getMethodFromName(pfg.getFolder()));
                        if (folder != null && Files.exists(folder) && !NIOFileUtils.list(folder.toString()).isEmpty()) {
                            List<Path> files = NIOFileUtils.listFiles(folder.toString());
                            for (Path file : files) {
                                Path target = Paths.get(zielTif.toString(), file.getFileName().toString());
                                Files.copy(file, target, NIOFileUtils.STANDARD_COPY_OPTIONS);
                            }
                        }
                    }
                }
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
    
    @Override
    public List<String> getProblems() {
        return problems;
    }
}
