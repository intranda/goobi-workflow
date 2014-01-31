package org.goobi.beans;

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
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.goobi.beans.Docket;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.goobi.beans.User;
import org.goobi.io.BackupFileRotation;
import org.goobi.io.FileListFilter;
import org.goobi.production.export.ExportDocket;

import ugh.dl.Fileformat;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;
//import ugh.fileformats.mets.MetsMods;
//import ugh.fileformats.mets.XStream;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.tasks.ProcessSwapInTask;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.metadaten.MetadatenSperrung;
import de.sub.goobi.persistence.managers.DocketManager;
import de.sub.goobi.persistence.managers.MasterpieceManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.TemplateManager;
import de.sub.goobi.persistence.managers.UserManager;

public class Process implements Serializable, DatabaseObject, Comparable<Process> {
    private static final Logger logger = Logger.getLogger(Process.class);
    private static final long serialVersionUID = -6503348094655786275L;
    private Integer id;
    private String titel;
    private String ausgabename;
    private Boolean istTemplate;
    private Boolean inAuswahllisteAnzeigen;
    private Project projekt;
    private Date erstellungsdatum;
    private List<Step> schritte;
//    private List<HistoryEvent> history;
    private List<Masterpiece> werkstuecke;
    private List<Template> vorlagen;
    private List<Processproperty> eigenschaften;
    private String sortHelperStatus;
    private Integer sortHelperImages;
    private Integer sortHelperArticles;
    private Integer sortHelperMetadata;
    private Integer sortHelperDocstructs;
    private Ruleset regelsatz;
    private Integer batchID;
    private Boolean swappedOut = false;
    private Boolean panelAusgeklappt = false;
    private Boolean selected = false;
    private Docket docket;

    // temporär
    private Integer projectId;
    private Integer MetadatenKonfigurationID;
    private Integer docketId;

    private final MetadatenSperrung msp = new MetadatenSperrung();
    Helper help = new Helper();

    public static String DIRECTORY_PREFIX = "orig";
    public static String DIRECTORY_SUFFIX = "images";

    private String wikifield = "";
    private static final String TEMPORARY_FILENAME_PREFIX = "temporary_";

    public Process() {
        this.swappedOut = false;
        this.titel = "";
        this.istTemplate = false;
        this.inAuswahllisteAnzeigen = false;
        this.eigenschaften = new ArrayList<Processproperty>();
        this.schritte = new ArrayList<Step>();
        this.erstellungsdatum = new Date();

    }

    /*
     * Getter und Setter
     */

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSortHelperStatus() {
        return this.sortHelperStatus;
    }

    public void setSortHelperStatus(String sortHelperStatus) {
        this.sortHelperStatus = sortHelperStatus;
    }

    public boolean isIstTemplate() {
        if (this.istTemplate == null) {
            this.istTemplate = Boolean.valueOf(false);
        }
        return this.istTemplate;
    }

    public void setIstTemplate(boolean istTemplate) {
        this.istTemplate = istTemplate;
    }

    public String getTitel() {
        return this.titel;
    }

    public void setTitel(String inTitel) {
        this.titel = inTitel.trim();
    }

    public List<Step> getSchritte() {
        if ((this.schritte == null || schritte.isEmpty()) && id != null) {
            schritte = StepManager.getStepsForProcess(id);
        }
        return this.schritte;
    }

    public void setSchritte(List<Step> schritte) {
        this.schritte = schritte;
    }

//    public List<HistoryEvent> getHistory() {

//        if (this.history == null && id != null) {
//            List<HistoryEvent> events = ProcessManager.getHistoryEvents(id);
//            for (HistoryEvent he : events) {
//                he.setProcess(this);
//            }
//            this.history = events;
//        }
//        return this.history;
//    }

//    public void setHistory(List<HistoryEvent> history) {
//
//        this.history = history;
//    }

    public List<Template> getVorlagen() {
        if ((vorlagen == null || vorlagen.isEmpty()) && id != null) {
            vorlagen = TemplateManager.getTemplatesForProcess(id);
        }
        return this.vorlagen;
    }

    public void setVorlagen(List<Template> vorlagen) {
        this.vorlagen = vorlagen;
    }

    public List<Masterpiece> getWerkstuecke() {
        if ((werkstuecke == null || werkstuecke.isEmpty()) && id != null) {
            werkstuecke = MasterpieceManager.getMasterpiecesForProcess(id);
        }
        return this.werkstuecke;
    }

    public void setWerkstuecke(List<Masterpiece> werkstuecke) {
        this.werkstuecke = werkstuecke;
    }

    public String getAusgabename() {
        return this.ausgabename;
    }

    public void setAusgabename(String ausgabename) {
        this.ausgabename = ausgabename;
    }

    public List<Processproperty> getEigenschaften() {
        if ((eigenschaften == null || eigenschaften.isEmpty()) && id != null) {
            eigenschaften = PropertyManager.getProcessPropertiesForProcess(id);
        }
        return this.eigenschaften;
    }

    public void setEigenschaften(List<Processproperty> eigenschaften) {
        this.eigenschaften = eigenschaften;
    }

    /*
     * Metadaten-Sperrungen zurückgeben
     */

    public User getBenutzerGesperrt() {
        User rueckgabe = null;
        if (MetadatenSperrung.isLocked(this.id.intValue())) {
            String benutzerID = this.msp.getLockBenutzer(this.id.intValue());
            try {
                rueckgabe = UserManager.getUserById(new Integer(benutzerID));
            } catch (Exception e) {
                Helper.setFehlerMeldung(Helper.getTranslation("userNotFound"), e);
            }
        }
        return rueckgabe;
    }

    public long getMinutenGesperrt() {
        return this.msp.getLockSekunden(this.id.longValue()) / 60;
    }

    public long getSekundenGesperrt() {
        return this.msp.getLockSekunden(this.id.longValue()) % 60;
    }

    /*
     * Metadaten- und ImagePfad
     */

    public String getImagesTifDirectory(boolean useFallBack) throws IOException, InterruptedException, SwapException, DAOException {
        File dir = new File(getImagesDirectory());
        DIRECTORY_SUFFIX = ConfigMain.getParameter("DIRECTORY_SUFFIX", "tif");
        DIRECTORY_PREFIX = ConfigMain.getParameter("DIRECTORY_PREFIX", "orig");
        /* nur die _tif-Ordner anzeigen, die nicht mir orig_ anfangen */
        FilenameFilter filterVerz = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith("_" + DIRECTORY_SUFFIX) && !name.startsWith(DIRECTORY_PREFIX + "_"));
            }
        };

        String tifOrdner = "";
        String[] verzeichnisse = dir.list(filterVerz);

        if (verzeichnisse != null) {
            for (int i = 0; i < verzeichnisse.length; i++) {
                tifOrdner = verzeichnisse[i];
            }
        }

        if (tifOrdner.equals("") && useFallBack) {
            String suffix = ConfigMain.getParameter("MetsEditorDefaultSuffix", "");
            if (!suffix.equals("")) {
                String[] folderList = dir.list();
                for (String folder : folderList) {
                    if (folder.endsWith(suffix)) {
                        tifOrdner = folder;
                        break;
                    }
                }
            }
        }

        if (!tifOrdner.equals("") && useFallBack) {
            String suffix = ConfigMain.getParameter("MetsEditorDefaultSuffix", "");
            if (!suffix.equals("")) {
                File tif = new File(tifOrdner);
                String[] files = tif.list();
                if (files == null || files.length == 0) {
                    String[] folderList = dir.list();
                    for (String folder : folderList) {
                        if (folder.endsWith(suffix)) {
                            tifOrdner = folder;
                            break;
                        }
                    }
                }
            }
        }

        if (tifOrdner.equals("")) {
            tifOrdner = this.titel + "_" + DIRECTORY_SUFFIX;
        }

        String rueckgabe = getImagesDirectory() + tifOrdner;

        if (!rueckgabe.endsWith(File.separator)) {
            rueckgabe += File.separator;
        }
        if (!ConfigMain.getBooleanParameter("useOrigFolder", true) && ConfigMain.getBooleanParameter("createOrigFolderIfNotExists", false)) {
            FilesystemHelper.createDirectory(rueckgabe);
        }
        return rueckgabe;
    }

    /*
     * @return true if the Tif-Image-Directory exists, false if not
     */
    public Boolean getTifDirectoryExists() {
        File testMe;
        try {
            testMe = new File(getImagesTifDirectory(true));
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        } catch (SwapException e) {
            return false;
        } catch (DAOException e) {
            return false;
        }
        if (testMe.list() == null) {
            return false;
        }
        if (testMe.exists() && testMe.list().length > 0) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean getDisplayMETSButton() {
        if (ConfigMain.getBooleanParameter("MetsEditorValidateImages", true)) {
            return getTifDirectoryExists();
        } else {
            return true;
        }
    }

    public Boolean getDisplayPDFButton() {
        return getTifDirectoryExists();
    }

    public Boolean getDisplayDMSButton() {
        if (ConfigMain.getBooleanParameter("ExportValidateImages", true)) {
            return getTifDirectoryExists();
        } else {
            return true;
        }
    }

    public String getImagesOrigDirectory(boolean useFallBack) throws IOException, InterruptedException, SwapException, DAOException {
        if (ConfigMain.getBooleanParameter("useOrigFolder", true)) {
            File dir = new File(getImagesDirectory());
            DIRECTORY_SUFFIX = ConfigMain.getParameter("DIRECTORY_SUFFIX", "tif");
            DIRECTORY_PREFIX = ConfigMain.getParameter("DIRECTORY_PREFIX", "orig");
            /* nur die _tif-Ordner anzeigen, die mit orig_ anfangen */
            FilenameFilter filterVerz = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return (name.endsWith("_" + DIRECTORY_SUFFIX) && name.startsWith(DIRECTORY_PREFIX + "_"));
                }
            };

            String origOrdner = "";
            String[] verzeichnisse = dir.list(filterVerz);
            for (int i = 0; i < verzeichnisse.length; i++) {
                origOrdner = verzeichnisse[i];
            }

            if (origOrdner.equals("") && useFallBack) {
                String suffix = ConfigMain.getParameter("MetsEditorDefaultSuffix", "");
                if (!suffix.equals("")) {
                    String[] folderList = dir.list();
                    for (String folder : folderList) {
                        if (folder.endsWith(suffix)) {
                            origOrdner = folder;
                            break;
                        }
                    }
                }
            }

            if (!origOrdner.equals("") && useFallBack) {
                String suffix = ConfigMain.getParameter("MetsEditorDefaultSuffix", "");
                if (!suffix.equals("")) {
                    File tif = new File(origOrdner);
                    String[] files = tif.list();
                    if (files == null || files.length == 0) {
                        String[] folderList = dir.list();
                        for (String folder : folderList) {
                            if (folder.endsWith(suffix)) {
                                origOrdner = folder;
                                break;
                            }
                        }
                    }
                }
            }

            if (origOrdner.equals("")) {
                origOrdner = DIRECTORY_PREFIX + "_" + this.titel + "_" + DIRECTORY_SUFFIX;
            }
            String rueckgabe = getImagesDirectory() + origOrdner + File.separator;
            if (ConfigMain.getBooleanParameter("createOrigFolderIfNotExists", false) && this.getSortHelperStatus() != "100000000") {
                FilesystemHelper.createDirectory(rueckgabe);
            }
            return rueckgabe;
        } else {
            return getImagesTifDirectory(useFallBack);
        }
    }

    public String getImagesDirectory() throws IOException, InterruptedException, SwapException, DAOException {
        String pfad = getProcessDataDirectory() + "images" + File.separator;
        FilesystemHelper.createDirectory(pfad);
        return pfad;
    }

    public String getSourceDirectory() throws IOException, InterruptedException, SwapException, DAOException {
        File dir = new File(getImagesDirectory());
        FilenameFilter filterVerz = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith("_" + "source"));
            }
        };
        File sourceFolder = null;
        String[] verzeichnisse = dir.list(filterVerz);
        if (verzeichnisse == null || verzeichnisse.length == 0) {
            sourceFolder = new File(dir, titel + "_source");
            if (ConfigMain.getBooleanParameter("createSourceFolder", false)) {
                sourceFolder.mkdir();
            }
        } else {
            sourceFolder = new File(dir, verzeichnisse[0]);
        }

        return sourceFolder.getAbsolutePath();
    }

    public String getProcessDataDirectory() throws IOException, InterruptedException, SwapException, DAOException {
        String pfad = getProcessDataDirectoryIgnoreSwapping();

        if (isSwappedOutGui()) {
            ProcessSwapInTask pst = new ProcessSwapInTask();
            pst.initialize(this);
            pst.execute();
            if (pst.getStatusProgress() == -1) {
                if (!new File(pfad, "images").exists() && !new File(pfad, "meta.xml").exists()) {
                    throw new SwapException(pst.getStatusMessage());
                } else {
                    setSwappedOutGui(false);
                }
                //				new ProzessDAO().save(this);
            }
        }
        return pfad;
    }

    public String getOcrDirectory() throws SwapException, DAOException, IOException, InterruptedException {
        return getProcessDataDirectory() + "ocr" + File.separator;
    }

    public String getTxtDirectory() throws SwapException, DAOException, IOException, InterruptedException {
        return getOcrDirectory() + this.titel + "_txt" + File.separator;
    }

    public String getWordDirectory() throws SwapException, DAOException, IOException, InterruptedException {
        return getOcrDirectory() + this.titel + "_wc" + File.separator;
    }

    public String getPdfDirectory() throws SwapException, DAOException, IOException, InterruptedException {
        return getOcrDirectory() + this.titel + "_pdf" + File.separator;
    }

    public String getAltoDirectory() throws SwapException, DAOException, IOException, InterruptedException {
        return getOcrDirectory() + this.titel + "_alto" + File.separator;
    }

    public String getImportDirectory() throws SwapException, DAOException, IOException, InterruptedException {
        return getProcessDataDirectory() + "import" + File.separator;
    }

    public String getExportDirectory() throws SwapException, DAOException, IOException, InterruptedException {
        return getProcessDataDirectory() + "export" + File.separator;
    }
    
    public String getProcessDataDirectoryIgnoreSwapping() throws IOException, InterruptedException, SwapException, DAOException {
        String pfad = this.help.getGoobiDataDirectory() + this.id.intValue() + File.separator;
        pfad = pfad.replaceAll(" ", "__");
        FilesystemHelper.createDirectory(pfad);
        return pfad;
    }

    /*
     * Helper
     */

    public Project getProjekt() {
        if (projekt == null && projectId != null) {
            try {
                projekt = ProjectManager.getProjectById(projectId);
            } catch (DAOException e) {
                logger.error(e);
            }
        }
        return this.projekt;
    }

    public void setProjekt(Project projekt) {
        this.projekt = projekt;
    }

    public Integer getBatchID() {
        return this.batchID;
    }

    public void setBatchID(Integer batch) {
        this.batchID = batch;
    }

    public Ruleset getRegelsatz() {
        return this.regelsatz;
    }

    public void setRegelsatz(Ruleset regelsatz) {
        this.regelsatz = regelsatz;
    }

    public int getSchritteSize() {

        return getSchritte().size();

    }

    public List<Step> getSchritteList() {

        return getSchritte();
    }

//    public int getHistorySize() {
//
//        if (this.history == null) {
//            return 0;
//        } else {
//            return this.history.size();
//        }
//    }
//
//    public List<HistoryEvent> getHistoryList() {
//
//        List<HistoryEvent> temp = new ArrayList<HistoryEvent>();
//        if (this.history != null) {
//            temp.addAll(this.history);
//        }
//        return temp;
//    }

    public int getEigenschaftenSize() {
        return getEigenschaften().size();
    }

    public List<Processproperty> getEigenschaftenList() {

        return new ArrayList<Processproperty>(getEigenschaften());

    }

    public int getWerkstueckeSize() {

        return getWerkstuecke().size();
    }

    public List<Masterpiece> getWerkstueckeList() {

        return getWerkstuecke();
    }

    public int getVorlagenSize() {

        //        if (this.getVorlagen == null) {
        //            this.vorlagen = new ArrayList<Vorlage>();
        //        }
        return this.getVorlagen().size();
    }

    public List<Template> getVorlagenList() {

        return getVorlagen();
    }

    public Integer getSortHelperArticles() {
        if (this.sortHelperArticles == null) {
            this.sortHelperArticles = 0;
        }
        return this.sortHelperArticles;
    }

    public void setSortHelperArticles(Integer sortHelperArticles) {
        this.sortHelperArticles = sortHelperArticles;
    }

    public Integer getSortHelperImages() {
        if (this.sortHelperImages == null) {
            this.sortHelperImages = 0;
        }
        return this.sortHelperImages;
    }

    public void setSortHelperImages(Integer sortHelperImages) {
        this.sortHelperImages = sortHelperImages;
    }

    public Integer getSortHelperMetadata() {
        if (this.sortHelperMetadata == null) {
            this.sortHelperMetadata = 0;
        }
        return this.sortHelperMetadata;
    }

    public void setSortHelperMetadata(Integer sortHelperMetadata) {
        this.sortHelperMetadata = sortHelperMetadata;
    }

    public Integer getSortHelperDocstructs() {
        if (this.sortHelperDocstructs == null) {
            this.sortHelperDocstructs = 0;
        }
        return this.sortHelperDocstructs;
    }

    public void setSortHelperDocstructs(Integer sortHelperDocstructs) {
        this.sortHelperDocstructs = sortHelperDocstructs;
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
            if (step.getBearbeitungsstatusEnum() == StepStatus.OPEN || step.getBearbeitungsstatusEnum() == StepStatus.INWORK) {
                return step;
            }
        }
        return null;
    }

    public boolean isSelected() {
        return (this.selected == null ? false : this.selected);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Date getErstellungsdatum() {
        return this.erstellungsdatum;
    }

    public void setErstellungsdatum(Date erstellungsdatum) {
        this.erstellungsdatum = erstellungsdatum;
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
            if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
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
        // (abgeschlossen * 100) / (offen + inBearbeitung + abgeschlossen);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#000");
        return df.format(abgeschlossen2) + df.format(inBearbeitung2) + df.format(offen2);
    }

    public int getFortschritt1() {
        int offen = 0;
        int inBearbeitung = 0;
        int abgeschlossen = 0;
        for (Step step : getSchritte()) {
            if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
                abgeschlossen++;
            } else if (step.getBearbeitungsstatusEnum() == StepStatus.LOCKED) {
                offen++;
            } else {
                inBearbeitung++;
            }
        }
        if ((offen + inBearbeitung + abgeschlossen) == 0) {
            offen = 1;
        }
        return (offen * 100) / (offen + inBearbeitung + abgeschlossen);
    }

    public int getFortschritt2() {
        int offen = 0;
        int inBearbeitung = 0;
        int abgeschlossen = 0;
        for (Step step : getSchritte()) {
            if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
                abgeschlossen++;
            } else if (step.getBearbeitungsstatusEnum() == StepStatus.LOCKED) {
                offen++;
            } else {
                inBearbeitung++;
            }
        }
        if ((offen + inBearbeitung + abgeschlossen) == 0) {
            offen = 1;
        }
        return (inBearbeitung * 100) / (offen + inBearbeitung + abgeschlossen);
    }

    public int getFortschritt3() {
        int offen = 0;
        int inBearbeitung = 0;
        int abgeschlossen = 0;

        for (Step step : getSchritte()) {
            if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
                abgeschlossen++;
            } else if (step.getBearbeitungsstatusEnum() == StepStatus.LOCKED) {
                offen++;
            } else {
                inBearbeitung++;
            }
        }
        if ((offen + inBearbeitung + abgeschlossen) == 0) {
            offen = 1;
        }
        double offen2 = 0;
        double inBearbeitung2 = 0;
        double abgeschlossen2 = 0;

        offen2 = (offen * 100) / (double) (offen + inBearbeitung + abgeschlossen);
        inBearbeitung2 = (inBearbeitung * 100) / (double) (offen + inBearbeitung + abgeschlossen);
        abgeschlossen2 = 100 - offen2 - inBearbeitung2;
        return (int) abgeschlossen2;
    }

    public String getMetadataFilePath() throws IOException, InterruptedException, SwapException, DAOException {
        return getProcessDataDirectory() + "meta.xml";
    }

    public String getTemplateFilePath() throws IOException, InterruptedException, SwapException, DAOException {
        return getProcessDataDirectory() + "template.xml";
    }

    public String getFulltextFilePath() throws IOException, InterruptedException, SwapException, DAOException {
        return getProcessDataDirectory() + "fulltext.xml";
    }

    public Fileformat readMetadataFile() throws ReadException, IOException, InterruptedException, PreferencesException, SwapException, DAOException,
            WriteException {
        if (!checkForMetadataFile()) {
            throw new IOException(Helper.getTranslation("metadataFileNotFound") + " " + getMetadataFilePath());
        }
        /* prüfen, welches Format die Metadaten haben (Mets, xstream oder rdf */
        String type = MetadatenHelper.getMetaFileType(getMetadataFilePath());
        logger.debug("current meta.xml file type for id " + getId() + ": " + type);
        Fileformat ff = MetadatenHelper.getFileformatByName(type, regelsatz);

        if (ff == null) {
            List<String> param = new ArrayList<String>();
            param.add(titel);
            param.add(type);
            Helper.setFehlerMeldung(Helper.getTranslation("MetadataFormatNotAvailable", param));
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

    // backup of meta.xml

   private void createBackupFile() throws IOException, InterruptedException, SwapException, DAOException {
        int numberOfBackups = 0;
        String FORMAT = "";
        if (ConfigMain.getIntParameter("numberOfMetaBackups") != 0) {
            numberOfBackups = ConfigMain.getIntParameter("numberOfMetaBackups");
            FORMAT = ConfigMain.getParameter("formatOfMetaBackups");
        }
        if (numberOfBackups != 0) {
            String typeOfBackup = ConfigMain.getParameter("typeOfBackup", "renameFile");
            if (typeOfBackup.equals("renameFile") && FORMAT != null) {
                createBackup(numberOfBackups, FORMAT);
            } else if (typeOfBackup.equals("")) {
                BackupFileRotation bfr = new BackupFileRotation();
                bfr.setNumberOfBackups(numberOfBackups);
                bfr.setFormat("meta.*\\.xml");
                bfr.setProcessDataDirectory(getProcessDataDirectory());
                bfr.performBackup();
            }
        } else {
            logger.debug("No backup configured for meta data files.");
        }
        //            format = ConfigMain.getParameter("formatOfMetaBackups");
        //        }
        //        if (format != null) {
        //            logger.info("Option 'formatOfMetaBackups' is deprecated and will be ignored.");
        //        }

    }

    private void createBackup(int numberOfBackups, String FORMAT) throws IOException, InterruptedException, SwapException, DAOException {
        FilenameFilter filter = new FileListFilter(FORMAT);
        File metaFilePath = new File(getProcessDataDirectory());
        File[] meta = metaFilePath.listFiles(filter);
        if (meta != null) {
            List<File> files = Arrays.asList(meta);
            Collections.reverse(files);

            int count;
            if (meta != null) {
                if (files.size() > numberOfBackups) {
                    count = numberOfBackups;
                } else {
                    count = meta.length;
                }
                while (count > 0) {
                    for (File data : files) {
                        if (data.length() != 0) {
                            if (data.getName().endsWith("xml." + (count - 1))) {
                                Long lastModified = data.lastModified();
                                File newFile = new File(data.toString().substring(0, data.toString().lastIndexOf(".")) + "." + (count));
                                data.renameTo(newFile);
                                if (lastModified > 0L) {
                                    newFile.setLastModified(lastModified);
                                }
                            }
                            if (data.getName().endsWith(".xml") && count == 1) {
                                Long lastModified = data.lastModified();
                                File newFile = new File(data.toString() + ".1");
                                data.renameTo(newFile);
                                if (lastModified > 0L) {
                                    newFile.setLastModified(lastModified);
                                }
                            }
                        }
                    }
                    count--;
                }
            }
        }
    }

    // private void renameMetadataFile(String oldFileName, String newFileName) {
    // File oldFile;
    // File newFile;
    // // Long lastModified;
    // if (oldFileName != null && newFileName != null) {
    // oldFile = new File(oldFileName);
    // // lastModified = oldFile.lastModified();
    // newFile = new File(newFileName);
    // oldFile.renameTo(newFile);
    // // newFile.setLastModified(lastModified);
    // }
    // }

    private boolean checkForMetadataFile() throws IOException, InterruptedException, SwapException, DAOException, WriteException,
            PreferencesException {
        boolean result = true;
        File f = new File(getMetadataFilePath());
        if (!f.exists()) {
            result = false;
        }

        return result;
    }

    private String getTemporaryMetadataFileName(String fileName) {

        File temporaryFile = new File(fileName);
        String directoryPath = temporaryFile.getParentFile().getPath();
        String temporaryFileName = TEMPORARY_FILENAME_PREFIX + temporaryFile.getName();

        return directoryPath + File.separator + temporaryFileName;
    }

    private void removePrefixFromRelatedMetsAnchorFileFor(String temporaryMetadataFilename) throws IOException {
        File temporaryFile = new File(temporaryMetadataFilename);
        File temporaryAnchorFile;

        String directoryPath = temporaryFile.getParentFile().getPath();
        String temporaryAnchorFileName = temporaryFile.getName().replace("meta.xml", "meta_anchor.xml");

        temporaryAnchorFile = new File(directoryPath + File.separator + temporaryAnchorFileName);

        if (temporaryAnchorFile.exists()) {
            String anchorFileName = temporaryAnchorFileName.replace(TEMPORARY_FILENAME_PREFIX, "");

            temporaryAnchorFileName = directoryPath + File.separator + temporaryAnchorFileName;
            anchorFileName = directoryPath + File.separator + anchorFileName;

            FilesystemHelper.renameFile(temporaryAnchorFileName, anchorFileName);
        }
    }

    public void writeMetadataFile(Fileformat gdzfile) throws IOException, InterruptedException, SwapException, DAOException, WriteException,
            PreferencesException {
        boolean backupCondition;
        boolean writeResult;
        File temporaryMetadataFile;

        Fileformat ff;
        String metadataFileName;
        String temporaryMetadataFileName;

        ff = MetadatenHelper.getFileformatByName(getProjekt().getFileFormatInternal(), this.regelsatz);

        //        switch (MetadataFormat.findFileFormatsHelperByName(this.projekt.getFileFormatInternal())) {
        //            case METS:
        //                ff = new MetsMods(this.regelsatz.getPreferences());
        //                break;
        //
        //            case RDF:
        //                ff = new RDFFile(this.regelsatz.getPreferences());
        //                break;
        //
        //            case LIDO:
        //                ff = new Lido(this.regelsatz.getPreferences());
        //                break;
        //            default:
        //                ff = new XStream(this.regelsatz.getPreferences());
        //                break;
        //        }
        // createBackupFile();
        metadataFileName = getMetadataFilePath();
        temporaryMetadataFileName = getTemporaryMetadataFileName(metadataFileName);

        ff.setDigitalDocument(gdzfile.getDigitalDocument());
        // ff.write(getMetadataFilePath());
        writeResult = ff.write(temporaryMetadataFileName);
        temporaryMetadataFile = new File(temporaryMetadataFileName);
        backupCondition = writeResult && temporaryMetadataFile.exists() && (temporaryMetadataFile.length() > 0);
        if (backupCondition) {
            createBackupFile();
            FilesystemHelper.renameFile(temporaryMetadataFileName, metadataFileName);
            removePrefixFromRelatedMetsAnchorFileFor(temporaryMetadataFileName);
        }
    }

    public void writeMetadataAsTemplateFile(Fileformat inFile) throws IOException, InterruptedException, SwapException, DAOException, WriteException,
            PreferencesException {
        inFile.write(getTemplateFilePath());
    }

    public Fileformat readMetadataAsTemplateFile() throws ReadException, IOException, InterruptedException, PreferencesException, SwapException,
            DAOException {
        if (new File(getTemplateFilePath()).exists()) {
            Fileformat ff = null;
            String type = MetadatenHelper.getMetaFileType(getTemplateFilePath());
            logger.debug("current template.xml file type: " + type);
            ff = MetadatenHelper.getFileformatByName(type, regelsatz);
            //            if (type.equals("mets")) {
            //                ff = new MetsMods(this.regelsatz.getPreferences());
            //            } else if (type.equals("xstream")) {
            //                ff = new XStream(this.regelsatz.getPreferences());
            //            } else {
            //                ff = new RDFFile(this.regelsatz.getPreferences());
            //            }
            ff.read(getTemplateFilePath());
            return ff;
        } else {
            throw new IOException("File does not exist: " + getTemplateFilePath());
        }
    }

    /**
     * prüfen, ob der Vorgang Schritte enthält, die keinem Benutzer und keiner Benutzergruppe zugewiesen ist
     * ================================================================
     */
    public boolean getContainsUnreachableSteps() {
        if (getSchritteList().size() == 0) {
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
     * here differet Getters and Setters for the same value, because Hibernate does not like bit-Fields with null Values (thats why Boolean) and
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

    public String getWikifield() {
        return this.wikifield;
    }

    public void setWikifield(String wikifield) {
        this.wikifield = wikifield;
    }

    public String downloadDocket() {

        logger.debug("generate docket for process " + this.id);
        String rootpath = ConfigMain.getParameter("xsltFolder");
        File xsltfile = new File(rootpath, "docket.xsl");
        if (docket != null) {
            xsltfile = new File(rootpath, docket.getFile());
            if (!xsltfile.exists()) {
                Helper.setFehlerMeldung("docketMissing");
                return "";
            }
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            String fileName = this.titel + ".pdf";
            ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
            String contentType = servletContext.getMimeType(fileName);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");

            // write run note to servlet output stream
            try {
                ServletOutputStream out = response.getOutputStream();
                ExportDocket ern = new ExportDocket();
                ern.startExport(this, out, xsltfile.getAbsolutePath());
                out.flush();
            } catch (IOException e) {
                logger.error("IOException while exporting run note", e);
            }

            facesContext.responseComplete();
        }
        return "";
    }

    public Step getFirstOpenStep() {

        for (Step s : getSchritteList()) {
            if (s.getBearbeitungsstatusEnum().equals(StepStatus.OPEN) || s.getBearbeitungsstatusEnum().equals(StepStatus.INWORK)) {
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
        } catch (SecurityException e) {

        } catch (NoSuchMethodException e) {

        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }

        try {
            String folder = this.getImagesTifDirectory(false);
            folder = folder.substring(0, folder.lastIndexOf("_"));
            folder = folder + "_" + methodName;
            if (new File(folder).exists()) {
                return folder;
            }

        } catch (SwapException e) {

        } catch (DAOException e) {

        } catch (IOException e) {

        } catch (InterruptedException e) {

        }

        return null;
    }

    public Docket getDocket() {
        if (docket == null && docketId != null) {
            try {
                docket = DocketManager.getDocketById(docketId);
            } catch (DAOException e) {
                logger.error(e);
            }
        }
        return docket;
    }

    public void setDocket(Docket docket) {
        this.docket = docket;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    @Override
    public int compareTo(Process arg0) {

        return id.compareTo(arg0.getId());
    }

    @Override
    public void lazyLoad() {
    }

    public Integer getMetadatenKonfigurationID() {
        return MetadatenKonfigurationID;
    }

    public void setMetadatenKonfigurationID(Integer metadatenKonfigurationID) {
        MetadatenKonfigurationID = metadatenKonfigurationID;
    }

    public Integer getDocketId() {
        return docketId;
    }

    public void setDocketId(Integer docketId) {
        this.docketId = docketId;
    }
}
