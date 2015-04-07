package de.sub.goobi.forms;

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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.beans.Usergroup;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.flow.jobs.JobManager;
import org.quartz.SchedulerException;

import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;

import org.goobi.beans.Process;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.FileUtils;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.XmlArtikelZaehlen;
import de.sub.goobi.helper.XmlArtikelZaehlen.CountType;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UsergroupManager;
import dubious.sub.goobi.helper.encryption.MD5;

@ManagedBean(name = "AdministrationForm")
@ViewScoped
public class AdministrationForm implements Serializable {
    private static final long serialVersionUID = 5648439270064158243L;
    private static final Logger logger = Logger.getLogger(AdministrationForm.class);
    private String passwort;
    private boolean istPasswortRichtig = false;
    private boolean rusFullExport = false;

    public final static String DIRECTORY_SUFFIX = "_tif";

    /* =============================================================== */

    /**
     * Passwort eingeben
     */
    public String Weiter() {
        this.passwort = new MD5(this.passwort).getMD5();
        String adminMd5 = ConfigurationHelper.getInstance().getAdminPassword();
        this.istPasswortRichtig = (this.passwort.equals(adminMd5));
        if (!this.istPasswortRichtig) {
            Helper.setFehlerMeldung("wrong passworwd", "");
        }
        return "";
    }

    /* =============================================================== */

    public String getPasswort() {
        return this.passwort;
    }

    public void setPasswort(String passwort) {
        this.passwort = passwort;
    }

    /**
     * restart quartz timer for scheduled storage calculation, so it notices chanced start time configuration from configuration
     */
    public void restartStorageCalculationScheduler() {
        try {
            JobManager.restartTimedJobs();
            Helper.setMeldung("StorageHistoryManager scheduler restarted");
        } catch (SchedulerException e) {
            Helper.setFehlerMeldung("Error while restarting StorageHistoryManager scheduler", e);
        }
    }

    /**
     * run storage calculation for all processes now
     */
    public void startStorageCalculationForAllProcessesNow() {
        HistoryAnalyserJob job = new HistoryAnalyserJob();
        if (job.getIsRunning() == false) {
            job.execute();
            Helper.setMeldung("scheduler calculation executed");
        } else {
            Helper.setMeldung("Job is already running, try again in a few minutes");
        }
    }

    public boolean isIstPasswortRichtig() {
        return this.istPasswortRichtig;
    }

    public void createIndex() {
    }

    public void ProzesseDurchlaufen() throws DAOException {
        List<Process> auftraege = ProcessManager.getAllProcesses();
        //		List<Process> auftraege = dao.search("from Prozess");
        for (Process auf : auftraege) {
            ProcessManager.saveProcess(auf);
        }
        Helper.setMeldung(null, "", "Elements successful counted");
    }

    public void AnzahlenErmitteln() throws DAOException, IOException, InterruptedException, SwapException {
        XmlArtikelZaehlen zaehlen = new XmlArtikelZaehlen();
        List<Process> auftraege = ProcessManager.getAllProcesses();
        for (Process auf : auftraege) {

            try {
                auf.setSortHelperDocstructs(zaehlen.getNumberOfUghElements(auf, CountType.DOCSTRUCT));
                auf.setSortHelperMetadata(zaehlen.getNumberOfUghElements(auf, CountType.METADATA));
                auf.setSortHelperImages(FileUtils.getNumberOfFiles(new File(auf.getImagesOrigDirectory(true))));
                ProcessManager.saveProcess(auf);
            } catch (RuntimeException e) {
                logger.error("Fehler bei Band: " + auf.getTitel(), e);
            }

            ProcessManager.saveProcess(auf);
        }
        Helper.setMeldung(null, "", "Elements successful counted");
    }

    //TODO: Remove this
    public void SiciKorr() throws DAOException {
        Usergroup gruppe = UsergroupManager.getUsergroupById(15);
        List<Usergroup> neueGruppen = new ArrayList<Usergroup>();
        neueGruppen.add(gruppe);

        List<Step> schritte = StepManager.getSteps("titel", "titel='Automatische Generierung der SICI'", 0, Integer.MAX_VALUE);
        for (Step auf : schritte) {
            auf.setBenutzergruppen(neueGruppen);
            StepManager.saveStep(auf);
        }
        Helper.setMeldung(null, "", "Sici erfolgreich korrigiert");
    }

    public void StandardRegelsatzSetzen() throws DAOException {
        Ruleset mk = RulesetManager.getRulesetById(Integer.valueOf(1));

        List<Process> auftraege = ProcessManager.getAllProcesses();

        int i = 0;
        for (Process auf : auftraege) {

            auf.setRegelsatz(mk);
            ProcessManager.saveProcess(auf);
            if (logger.isDebugEnabled()) {
                logger.debug(auf.getId() + " - " + i++ + "von" + auftraege.size());
            }
        }
        Helper.setMeldung(null, "", "Standard-ruleset successful set");
    }

    public void ProzesseDatumSetzen() throws DAOException {
        List<Process> auftraege = ProcessManager.getAllProcesses();
        for (Process auf : auftraege) {

            for (Step s : auf.getSchritteList()) {

                if (s.getBearbeitungsbeginn() != null) {
                    auf.setErstellungsdatum(s.getBearbeitungsbeginn());
                    break;
                }
            }
            ProcessManager.saveProcess(auf);
        }
        Helper.setMeldung(null, "", "created date");
    }

    public void ImagepfadKorrigieren() throws DAOException {
        UghHelper ughhelp = new UghHelper();

        List<Process> auftraege = ProcessManager.getAllProcesses();

        /* alle Prozesse durchlaufen */
        for (Process p : auftraege) {

            if (p.getBenutzerGesperrt() != null) {
                Helper.setFehlerMeldung("metadata locked: ", p.getTitel());
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Prozess: " + p.getTitel());
                }
                Prefs myPrefs = p.getRegelsatz().getPreferences();
                Fileformat gdzfile;
                try {
                    gdzfile = p.readMetadataFile();

                    MetadataType mdt = ughhelp.getMetadataType(myPrefs, "pathimagefiles");
                    List<? extends Metadata> alleMetadaten = gdzfile.getDigitalDocument().getPhysicalDocStruct().getAllMetadataByType(mdt);
                    if (alleMetadaten != null && alleMetadaten.size() > 0) {
                        Metadata md = alleMetadaten.get(0);
                        if (logger.isDebugEnabled()) {
                            logger.debug(md.getValue());
                        }
                        if (SystemUtils.IS_OS_WINDOWS) {
                            md.setValue("file:/" + p.getImagesDirectory() + p.getTitel().trim() + DIRECTORY_SUFFIX);
                        } else {
                            md.setValue("file://" + p.getImagesDirectory() + p.getTitel().trim() + DIRECTORY_SUFFIX);
                        }
                        p.writeMetadataFile(gdzfile);
                        Helper.setMeldung(null, "", "Image path set: " + p.getTitel() + ": ./" + p.getTitel() + DIRECTORY_SUFFIX);
                    } else {
                        Helper.setMeldung(null, "", "No Image path available: " + p.getTitel());
                    }
                } catch (ReadException e) {
                    Helper.setFehlerMeldung("", "ReadException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("ReadException: " + p.getTitel(), e);
                } catch (IOException e) {
                    Helper.setFehlerMeldung("", "IOException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("IOException: " + p.getTitel(), e);
                } catch (InterruptedException e) {
                    Helper.setFehlerMeldung("", "InterruptedException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("InterruptedException: " + p.getTitel(), e);
                } catch (PreferencesException e) {
                    Helper.setFehlerMeldung("", "PreferencesException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("PreferencesException: " + p.getTitel(), e);
                } catch (UghHelperException e) {
                    Helper.setFehlerMeldung("", "UghHelperException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("UghHelperException: " + p.getTitel(), e);
                } catch (Exception e) {
                    Helper.setFehlerMeldung("", "Exception: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("Exception: " + p.getTitel(), e);
                }
            }
        }
        Helper.setMeldung(null, "", "------------------------------------------------------------------");
        Helper.setMeldung(null, "", "Image paths set");
    }

    public void test(){
    	Helper.setFehlerMeldung("Fehlermeldung mit extrem langem Text, die sich über viele Zeilen erstreckt und so weiter geht bis ein Zeilenumbruch kommt der dann in einem Zeilenumbruch endet und damit die Zeile umgebrochen hat 1");
    	Helper.setFehlerMeldung("Fehlermeldung mit extrem langem Text, die sich über viele Zeilen erstreckt und so weiter geht bis ein Zeilenumbruch kommt der dann in einem Zeilenumbruch endet und damit die Zeile umgebrochen hat 2");
    	Helper.setFehlerMeldung("Fehlermeldung mit extrem langem Text, die sich über viele Zeilen erstreckt und so weiter geht bis ein Zeilenumbruch kommt der dann in einem Zeilenumbruch endet und damit die Zeile umgebrochen hat 3");
    	Helper.setFehlerMeldung("Fehlermeldung mit extrem langem Text, die sich über viele Zeilen erstreckt und so weiter geht bis ein Zeilenumbruch kommt der dann in einem Zeilenumbruch endet und damit die Zeile umgebrochen hat 4", new Exception("eine Exception die eine Exception ist und damit eine Exception geworfen hat."));

    	Helper.setMeldung("Meldung mit extrem langem Text, die sich über viele Zeilen erstreckt und so weiter geht bis ein Zeilenumbruch kommt der dann in einem Zeilenumbruch endet und damit die Zeile umgebrochen hat 1");
    	Helper.setMeldung("Meldung mit extrem langem Text, die sich über viele Zeilen erstreckt und so weiter geht bis ein Zeilenumbruch kommt der dann in einem Zeilenumbruch endet und damit die Zeile umgebrochen hat 2");
    	Helper.setMeldung("Meldung mit extrem langem Text, die sich über viele Zeilen erstreckt und so weiter geht bis ein Zeilenumbruch kommt der dann in einem Zeilenumbruch endet und damit die Zeile umgebrochen hat 3");
    }
    
    
    public void PPNsKorrigieren() throws DAOException {
        UghHelper ughhelp = new UghHelper();

        List<Process> auftraege =
                ProcessManager.getProcesses(null, "projekteId = (select ProjekteId from projekte where titel = 'DigiZeitschriften')", 0,
                        Integer.MAX_VALUE);

        /* alle Prozesse durchlaufen */
        for (Process p : auftraege) {

            if (p.getBenutzerGesperrt() != null) {
                Helper.setFehlerMeldung("metadata locked: ", p.getTitel());
            } else {
                String myBandnr = p.getTitel();
                StringTokenizer tokenizer = new StringTokenizer(p.getTitel(), "_");
                while (tokenizer.hasMoreTokens()) {
                    myBandnr = "_" + tokenizer.nextToken();
                }
                Prefs myPrefs = p.getRegelsatz().getPreferences();
                try {
                    Fileformat gdzfile = p.readMetadataFile();
                    DocStruct dsTop = gdzfile.getDigitalDocument().getLogicalDocStruct();
                    DocStruct dsFirst = null;
                    if (dsTop.getAllChildren() != null && dsTop.getAllChildren().size() > 0) {
                        dsFirst = dsTop.getAllChildren().get(0);
                    }

                    MetadataType mdtPpnDigital = ughhelp.getMetadataType(myPrefs, "CatalogIDDigital");
                    MetadataType mdtPpnAnalog = ughhelp.getMetadataType(myPrefs, "CatalogIDSource");
                    List<? extends Metadata> alleMetadaten;

                    /* digitale PPN korrigieren */
                    if (dsFirst != null) {
                        alleMetadaten = dsFirst.getAllMetadataByType(mdtPpnDigital);
                        if (alleMetadaten != null && alleMetadaten.size() > 0) {
                            Metadata md = alleMetadaten.get(0);
                            if (logger.isDebugEnabled()) {
                                logger.debug(md.getValue());
                            }
                            if (!md.getValue().endsWith(myBandnr)) {
                                md.setValue(md.getValue() + myBandnr);
                                Helper.setMeldung(null, "PPN digital adjusted: ", p.getTitel());
                            }
                        }

                        /* analoge PPN korrigieren */
                        alleMetadaten = dsFirst.getAllMetadataByType(mdtPpnAnalog);
                        if (alleMetadaten != null && alleMetadaten.size() > 0) {
                            Metadata md1 = alleMetadaten.get(0);
                            if (logger.isDebugEnabled()) {
                                logger.debug(md1.getValue());
                            }
                            if (!md1.getValue().endsWith(myBandnr)) {
                                md1.setValue(md1.getValue() + myBandnr);
                                Helper.setMeldung(null, "PPN analog adjusted: ", p.getTitel());
                            }
                        }
                    }

                    /* Collections korrigieren */
                    List<String> myKollektionenTitel = new ArrayList<String>();
                    MetadataType coltype = ughhelp.getMetadataType(myPrefs, "singleDigCollection");
                    ArrayList<Metadata> myCollections;
                    if (dsTop.getAllMetadataByType(coltype) != null && dsTop.getAllMetadataByType(coltype).size() != 0) {
                        myCollections = new ArrayList<Metadata>(dsTop.getAllMetadataByType(coltype));
                        if (myCollections != null && myCollections.size() > 0) {
                            for (Metadata md : myCollections) {

                                if (myKollektionenTitel.contains(md.getValue())) {
                                    dsTop.removeMetadata(md);
                                } else {
                                    myKollektionenTitel.add(md.getValue());
                                }
                            }
                        }
                    }
                    if (dsFirst != null && dsFirst.getAllMetadataByType(coltype) != null) {
                        myKollektionenTitel = new ArrayList<String>();
                        myCollections = new ArrayList<Metadata>(dsFirst.getAllMetadataByType(coltype));
                        if (myCollections != null && myCollections.size() > 0) {
                            for (Metadata md : myCollections) {
                                //								Metadata md = (Metadata) it.next();
                                if (myKollektionenTitel.contains(md.getValue())) {
                                    dsFirst.removeMetadata(md);
                                } else {
                                    myKollektionenTitel.add(md.getValue());
                                }
                            }
                        }
                    }

                    p.writeMetadataFile(gdzfile);

                } catch (ReadException e) {
                    Helper.setFehlerMeldung("", "ReadException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("ReadException: " + p.getTitel(), e);
                } catch (IOException e) {
                    Helper.setFehlerMeldung("", "IOException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("IOException: " + p.getTitel(), e);
                } catch (InterruptedException e) {
                    Helper.setFehlerMeldung("", "InterruptedException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("InterruptedException: " + p.getTitel(), e);
                } catch (PreferencesException e) {
                    Helper.setFehlerMeldung("", "PreferencesException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("PreferencesException: " + p.getTitel(), e);
                } catch (UghHelperException e) {
                    Helper.setFehlerMeldung("", "UghHelperException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("UghHelperException: " + p.getTitel(), e);
                } catch (Exception e) {
                    Helper.setFehlerMeldung("", "Exception: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("Exception: " + p.getTitel(), e);
                }
            }
        }
        Helper.setMeldung(null, "", "------------------------------------------------------------------");
        Helper.setMeldung(null, "", "PPNs adjusted");
    }

    //TODO: Remove this
    public static void PPNsFuerStatistischesJahrbuchKorrigieren2() {
        UghHelper ughhelp = new UghHelper();
        List<Process> pl = ProcessManager.getProcesses(null, " titel like 'statjafud%'", 0, Integer.MAX_VALUE);

        //        Session session = Helper.getHibernateSession();
        //        Criteria crit = session.createCriteria(Process.class);
        //        crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
        //        crit.add(Restrictions.like("titel", "statjafud%"));
        //        /* alle Prozesse durchlaufen */
        //        List<Process> pl = crit.list();
        for (Process p : pl) {

            if (p.getBenutzerGesperrt() != null) {
                Helper.setFehlerMeldung("metadata locked: " + p.getTitel());
            } else {
                Prefs myPrefs = p.getRegelsatz().getPreferences();
                try {
                    Fileformat gdzfile = p.readMetadataFile();
                    DocStruct dsTop = gdzfile.getDigitalDocument().getLogicalDocStruct();
                    MetadataType mdtPpnDigital = ughhelp.getMetadataType(myPrefs, "CatalogIDSource");

                    /* analoge PPN korrigieren */
                    if (dsTop != null) {
                        List<? extends Metadata> alleMetadaten = dsTop.getAllMetadataByType(mdtPpnDigital);
                        if (alleMetadaten != null && alleMetadaten.size() > 0) {
                            for (Iterator<? extends Metadata> it = alleMetadaten.iterator(); it.hasNext();) {
                                Metadata md = it.next();
                                if (!md.getValue().startsWith("PPN")) {
                                    md.setValue("PPN" + md.getValue());
                                    p.writeMetadataFile(gdzfile);
                                }
                            }
                        }
                    }
                } catch (ReadException e) {
                    Helper.setFehlerMeldung("", "ReadException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("ReadException: " + p.getTitel(), e);
                } catch (IOException e) {
                    Helper.setFehlerMeldung("", "IOException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("IOException: " + p.getTitel(), e);
                } catch (InterruptedException e) {
                    Helper.setFehlerMeldung("", "InterruptedException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("InterruptedException: " + p.getTitel(), e);
                } catch (PreferencesException e) {
                    Helper.setFehlerMeldung("", "PreferencesException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("PreferencesException: " + p.getTitel(), e);
                } catch (UghHelperException e) {
                    Helper.setFehlerMeldung("", "UghHelperException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("UghHelperException: " + p.getTitel(), e);
                } catch (Exception e) {
                    Helper.setFehlerMeldung("", "Exception: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("Exception: " + p.getTitel(), e);
                }
            }
        }
        Helper.setMeldung("------------------------------------------------------------------");
        Helper.setMeldung("PPNs adjusted");
    }

    public void PPNsFuerStatistischesJahrbuchKorrigieren() throws DAOException {
        UghHelper ughhelp = new UghHelper();
        BeanHelper bhelp = new BeanHelper();
        List<Process> auftraege =
                ProcessManager.getProcesses(null, "projekteId = (select ProjekteId from projekte where titel = 'UB-MannheimDigizeit')", 0,
                        Integer.MAX_VALUE);

        //        Session session = Helper.getHibernateSession();
        //        Criteria crit = session.createCriteria(Process.class);
        //        crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
        //        crit.createCriteria("projekt", "proj");
        //        crit.add(Restrictions.like("proj.titel", "UB-MannheimDigizeit"));

        /* alle Prozesse durchlaufen */
        for (Iterator<Process> iter = auftraege.iterator(); iter.hasNext();) {
            Process p = iter.next();
            if (p.getBenutzerGesperrt() != null) {
                Helper.setFehlerMeldung("metadata locked: ", p.getTitel());
            } else {
                String ppn = bhelp.WerkstueckEigenschaftErmitteln(p, "PPN digital").replace("PPN ", "").replace("PPN", "");
                String jahr = bhelp.ScanvorlagenEigenschaftErmitteln(p, "Bandnummer");
                String ppnAufBandebene = "PPN" + ppn + "_" + jahr;

                Prefs myPrefs = p.getRegelsatz().getPreferences();
                try {
                    Fileformat gdzfile = p.readMetadataFile();
                    DocStruct dsTop = gdzfile.getDigitalDocument().getLogicalDocStruct();
                    DocStruct dsFirst = null;
                    if (dsTop.getAllChildren() != null && dsTop.getAllChildren().size() > 0) {
                        dsFirst = dsTop.getAllChildren().get(0);
                    }

                    MetadataType mdtPpnDigital = ughhelp.getMetadataType(myPrefs, "CatalogIDDigital");

                    /* digitale PPN korrigieren */
                    if (dsFirst != null) {
                        List<? extends Metadata> alleMetadaten = dsFirst.getAllMetadataByType(mdtPpnDigital);
                        if (alleMetadaten == null || alleMetadaten.size() == 0) {
                            Metadata md = new Metadata(mdtPpnDigital);
                            md.setValue(ppnAufBandebene);
                            dsFirst.addMetadata(md);
                        }
                    }

                    /* Collections korrigieren */
                    List<String> myKollektionenTitel = new ArrayList<String>();
                    MetadataType coltype = ughhelp.getMetadataType(myPrefs, "singleDigCollection");
                    ArrayList<Metadata> myCollections;
                    if (dsTop.getAllMetadataByType(coltype) != null) {
                        myCollections = new ArrayList<Metadata>(dsTop.getAllMetadataByType(coltype));
                        if (myCollections != null && myCollections.size() > 0) {
                            for (Iterator<Metadata> it = myCollections.iterator(); it.hasNext();) {
                                Metadata md = it.next();
                                if (myKollektionenTitel.contains(md.getValue())) {
                                    dsTop.removeMetadata(md);
                                } else {
                                    myKollektionenTitel.add(md.getValue());
                                }
                            }
                        }
                    }
                    if (dsFirst != null && dsFirst.getAllMetadataByType(coltype).size() > 0) {
                        myKollektionenTitel = new ArrayList<String>();
                        myCollections = new ArrayList<Metadata>(dsFirst.getAllMetadataByType(coltype));
                        if (myCollections != null && myCollections.size() > 0) {
                            for (Iterator<Metadata> it = myCollections.iterator(); it.hasNext();) {
                                Metadata md = it.next();
                                if (myKollektionenTitel.contains(md.getValue())) {
                                    dsFirst.removeMetadata(md);
                                } else {
                                    myKollektionenTitel.add(md.getValue());
                                }
                            }
                        }
                    }

                    p.writeMetadataFile(gdzfile);

                } catch (ReadException e) {
                    Helper.setFehlerMeldung("", "ReadException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("ReadException: " + p.getTitel(), e);
                } catch (IOException e) {
                    Helper.setFehlerMeldung("", "IOException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("IOException: " + p.getTitel(), e);
                } catch (InterruptedException e) {
                    Helper.setFehlerMeldung("", "InterruptedException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("InterruptedException: " + p.getTitel(), e);
                } catch (PreferencesException e) {
                    Helper.setFehlerMeldung("", "PreferencesException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("PreferencesException: " + p.getTitel() + " - " + e.getMessage());
                } catch (UghHelperException e) {
                    Helper.setFehlerMeldung("", "UghHelperException: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("UghHelperException: " + p.getTitel(), e);
                } catch (Exception e) {
                    Helper.setFehlerMeldung("", "Exception: " + p.getTitel() + " - " + e.getMessage());
                    logger.error("Exception: " + p.getTitel(), e);
                }
            }
        }
        Helper.setMeldung(null, "", "------------------------------------------------------------------");
        Helper.setMeldung(null, "", "PPNs adjusted");
    }

    public boolean isRusFullExport() {
        return this.rusFullExport;
    }

    public void setRusFullExport(boolean rusFullExport) {
        this.rusFullExport = rusFullExport;
    }

}
