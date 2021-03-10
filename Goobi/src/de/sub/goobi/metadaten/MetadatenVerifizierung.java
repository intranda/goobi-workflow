package de.sub.goobi.metadaten;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi-workflow
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;

import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import ugh.dl.Corporate;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.dl.Reference;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;

public class MetadatenVerifizierung {
    UghHelper ughhelp = new UghHelper();
    List<DocStruct> docStructsOhneSeiten;
    Process myProzess;
    boolean autoSave = false;
    private List<String> problems = new ArrayList<>();

    public boolean validate(Process inProzess) {
        Prefs myPrefs = inProzess.getRegelsatz().getPreferences();
        /*
         * -------------------------------- Fileformat einlesen --------------------------------
         */
        Fileformat gdzfile;
        try {
            gdzfile = inProzess.readMetadataFile();
        } catch (Exception e) {
            Helper.setFehlerMeldung(Helper.getTranslation("MetadataReadError") + inProzess.getTitel(), e.getMessage());
            problems.add(Helper.getTranslation("MetadataReadError") + ": " + e.getMessage());
            return false;
        }
        return validate(gdzfile, myPrefs, inProzess);
    }

    public boolean validate(Fileformat gdzfile, Prefs inPrefs, Process inProzess) {
        String metadataLanguage = Helper.getMetadataLanguage();
        if (metadataLanguage == null) {
            metadataLanguage = "en";
        }
        this.myProzess = inProzess;
        boolean ergebnis = true;

        DigitalDocument dd = null;
        try {
            dd = gdzfile.getDigitalDocument();
        } catch (Exception e) {
            Helper.setFehlerMeldung(Helper.getTranslation("MetadataDigitalDocumentError") + inProzess.getTitel(), e.getMessage());
            problems.add(Helper.getTranslation("MetadataDigitalDocumentError") + ": " + e.getMessage());
            ergebnis = false;
        }

        DocStruct logical = dd.getLogicalDocStruct();
        if (logical.getAllIdentifierMetadata() != null && logical.getAllIdentifierMetadata().size() > 0) {
            Metadata identifierTopStruct = logical.getAllIdentifierMetadata().get(0);
            try {
                if (!identifierTopStruct.getValue().replaceAll("[\\w|-]", "").equals("")) {
                    String[] parameter = { identifierTopStruct.getType().getNameByLanguage(metadataLanguage),
                            logical.getType().getNameByLanguage(metadataLanguage) };

                    Helper.setFehlerMeldung(Helper.getTranslation("InvalidIdentifierCharacter", parameter));
                    problems.add(Helper.getTranslation("InvalidIdentifierCharacter") + ": " + parameter);
                    ergebnis = false;
                }
                DocStruct firstChild = logical.getAllChildren().get(0);
                Metadata identifierFirstChild = firstChild.getAllIdentifierMetadata().get(0);
                if (identifierTopStruct.getValue() != null && identifierTopStruct.getValue() != ""
                        && identifierTopStruct.getValue().equals(identifierFirstChild.getValue())) {
                    String[] parameter = { identifierTopStruct.getType().getName(), logical.getType().getName(), firstChild.getType().getName() };
                    Helper.setFehlerMeldung(Helper.getTranslation("InvalidIdentifierSame", parameter));
                    problems.add(Helper.getTranslation("InvalidIdentifierSame") + ": " + parameter);
                    ergebnis = false;
                }
                if (!identifierFirstChild.getValue().replaceAll("[\\w|-]", "").equals("")) {
                    String[] parameter = { identifierTopStruct.getType().getNameByLanguage(metadataLanguage),
                            firstChild.getType().getNameByLanguage(metadataLanguage) };
                    Helper.setFehlerMeldung(Helper.getTranslation("InvalidIdentifierCharacter", parameter));
                    problems.add(Helper.getTranslation("InvalidIdentifierCharacter") + ": " + parameter);
                    ergebnis = false;
                }
            } catch (Exception e) {
                // no firstChild or no identifier
            }
        } else {
            Helper.setFehlerMeldung(Helper.getTranslation("MetadataMissingIdentifier"));
            problems.add(Helper.getTranslation("MetadataMissingIdentifier"));
            ergebnis = false;
        }
        /*
         * -------------------------------- PathImagesFiles prüfen --------------------------------
         */
        if (!this.isValidPathImageFiles(dd.getPhysicalDocStruct(), inPrefs)) {
            problems.add(Helper.getTranslation("InvalidImagePath"));
            ergebnis = false;
        }

        /*
         * -------------------------------- auf Docstructs ohne Seiten prüfen --------------------------------
         */
        DocStruct logicalTop = dd.getLogicalDocStruct();
        if (logicalTop == null) {
            Helper.setFehlerMeldung(inProzess.getTitel() + ": " + Helper.getTranslation("MetadataPaginationError"));
            problems.add(Helper.getTranslation("MetadataPaginationError"));
            ergebnis = false;
        }

        if (ConfigurationHelper.getInstance().isMetsEditorValidateImages()) {

            this.docStructsOhneSeiten = new ArrayList<>();
            this.checkDocStructsOhneSeiten(logicalTop);
            if (this.docStructsOhneSeiten.size() != 0) {
                for (Iterator<DocStruct> iter = this.docStructsOhneSeiten.iterator(); iter.hasNext();) {
                    DocStruct ds = iter.next();
                    Helper.setFehlerMeldung(inProzess.getTitel() + ": " + Helper.getTranslation("MetadataPaginationStructure")
                    + ds.getType().getNameByLanguage(metadataLanguage));
                    problems.add(Helper.getTranslation("MetadataPaginationStructure") + ds.getType().getNameByLanguage(metadataLanguage));
                }
                ergebnis = false;
            }

            /*
             * -------------------------------- auf Seiten ohne Docstructs prüfen --------------------------------
             */
            List<String> seitenOhneDocstructs = null;
            try {
                seitenOhneDocstructs = checkSeitenOhneDocstructs(gdzfile);
            } catch (PreferencesException e1) {
                Helper.setFehlerMeldung("[" + inProzess.getTitel() + "] Can not check pages without docstructs: ");
                problems.add(Helper.getTranslation("Can not check pages without docstructs"));
                ergebnis = false;
            }
            if (seitenOhneDocstructs != null && seitenOhneDocstructs.size() != 0) {
                for (Iterator<String> iter = seitenOhneDocstructs.iterator(); iter.hasNext();) {
                    String seite = iter.next();
                    Helper.setFehlerMeldung(inProzess.getTitel() + ": " + Helper.getTranslation("MetadataPaginationPages"), seite);
                    problems.add(Helper.getTranslation("MetadataPaginationPages") + ": " + seite);
                }
                ergebnis = false;
            }
        }
        /*
         * -------------------------------- auf mandatory Values der Metadaten prüfen --------------------------------
         */
        List<String> mandatoryList = checkMandatoryValues(dd.getLogicalDocStruct(), new ArrayList<String>(), metadataLanguage);
        if (mandatoryList.size() != 0) {
            for (Iterator<String> iter = mandatoryList.iterator(); iter.hasNext();) {
                String temp = iter.next();
                Helper.setFehlerMeldung(inProzess.getTitel() + ": " + Helper.getTranslation("MetadataMandatoryElement"), temp);
                problems.add(Helper.getTranslation("MetadataMandatoryElement") + ": " + temp);
            }
            ergebnis = false;
        }

        /*
         * -------------------------------- auf Details in den Metadaten prüfen, die in der Konfiguration angegeben wurden
         * --------------------------------
         */
        List<String> configuredList = checkConfiguredValidationValues(dd.getLogicalDocStruct(), new ArrayList<String>(), inPrefs, metadataLanguage);
        if (configuredList.size() != 0) {
            for (Iterator<String> iter = configuredList.iterator(); iter.hasNext();) {
                String temp = iter.next();
                Helper.setFehlerMeldung(inProzess.getTitel() + ": " + Helper.getTranslation("MetadataInvalidData"), temp);
                problems.add(Helper.getTranslation("MetadataInvalidData") + ": " + temp);
            }
            ergebnis = false;
        }

        List<String> expressionList = validateMetadatValues(dd.getLogicalDocStruct(), metadataLanguage);
        if (!expressionList.isEmpty()) {
            for (String text : expressionList) {
                Helper.setFehlerMeldung(text);
                problems.add(text);
            }
            ergebnis = false;
        }

        if (ConfigurationHelper.getInstance().isMetsEditorValidateImages()) {
            MetadatenImagesHelper mih = new MetadatenImagesHelper(inPrefs, dd);
            try {
                if (!mih.checkIfImagesValid(inProzess.getTitel(), inProzess.getImagesTifDirectory(true))) {
                    problems.add(Helper.getTranslation("ImagesNotValid"));
                    ergebnis = false;
                }
            } catch (Exception e) {
                Helper.setFehlerMeldung(inProzess.getTitel() + ": ", e);
                problems.add(Helper.getTranslation("Exception occurred") + ": " + e.getMessage());
                ergebnis = false;
            }

            try {

                List<String> images = mih.getDataFiles(myProzess, inProzess.getImagesTifDirectory(true));
                if (images != null && !images.isEmpty()) {
                    int sizeOfPagination = dd.getPhysicalDocStruct().getAllChildren().size();
                    int sizeOfImages = images.size();
                    if (sizeOfPagination != sizeOfImages) {
                        String[] parameter = { String.valueOf(sizeOfPagination), String.valueOf(sizeOfImages) };
                        Helper.setFehlerMeldung(Helper.getTranslation("imagePaginationError", parameter));
                        problems.add(Helper.getTranslation("imagePaginationError"));
                        return false;
                    }
                }
            } catch (InvalidImagesException | IOException | InterruptedException | SwapException | DAOException e1) {
                Helper.setFehlerMeldung(inProzess.getTitel() + ": ", e1);
                problems.add("InvalidImagesException: " + e1.getMessage());
                ergebnis = false;
            }
        }
        /*
         * -------------------------------- Metadaten ggf. zum Schluss speichern --------------------------------
         */
        try {
            if (this.autoSave) {
                inProzess.writeMetadataFile(gdzfile);
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("Error while writing metadata: " + inProzess.getTitel(), e);
            problems.add(Helper.getTranslation("Error while writing metadata") + ": " + e.getMessage());
        }
        return ergebnis;
    }

    private boolean isValidPathImageFiles(DocStruct phys, Prefs myPrefs) {
        try {
            MetadataType mdt = this.ughhelp.getMetadataType(myPrefs, "pathimagefiles");
            List<? extends Metadata> alleMetadaten = phys.getAllMetadataByType(mdt);
            if (alleMetadaten != null && alleMetadaten.size() > 0) {
                @SuppressWarnings("unused")
                Metadata mmm = alleMetadaten.get(0);

                return true;
            } else {
                Helper.setFehlerMeldung(this.myProzess.getTitel() + ": " + "Can not verify, image path is not set", "");
                return false;
            }
        } catch (UghHelperException e) {
            Helper.setFehlerMeldung(this.myProzess.getTitel() + ": " + "Verify aborted, error: ", e.getMessage());
            return false;
        }
    }

    private void checkDocStructsOhneSeiten(DocStruct inStruct) {
        if (inStruct.getAllToReferences().size() == 0 && !inStruct.getType().isAnchor()) {
            this.docStructsOhneSeiten.add(inStruct);
        }
        /* alle Kinder des aktuellen DocStructs durchlaufen */
        if (inStruct.getAllChildren() != null) {
            for (Iterator<DocStruct> iter = inStruct.getAllChildren().iterator(); iter.hasNext();) {
                DocStruct child = iter.next();
                checkDocStructsOhneSeiten(child);
            }
        }
    }

    private List<String> checkSeitenOhneDocstructs(Fileformat inRdf) throws PreferencesException {
        List<String> rueckgabe = new ArrayList<>();
        DocStruct boundbook = inRdf.getDigitalDocument().getPhysicalDocStruct();
        /* wenn boundbook null ist */
        if (boundbook == null || boundbook.getAllChildren() == null) {
            return rueckgabe;
        }

        /* alle Seiten durchlaufen und prüfen ob References existieren */
        for (Iterator<DocStruct> iter = boundbook.getAllChildren().iterator(); iter.hasNext();) {
            DocStruct ds = iter.next();
            List<Reference> refs = ds.getAllFromReferences();
            String physical = "";
            String logical = "";
            if (refs.size() == 0) {

                for (Iterator<Metadata> iter2 = ds.getAllMetadata().iterator(); iter2.hasNext();) {
                    Metadata md = iter2.next();
                    if (md.getType().getName().equals("logicalPageNumber")) {
                        logical = " (" + md.getValue() + ")";
                    }
                    if (md.getType().getName().equals("physPageNumber")) {
                        physical = md.getValue();
                    }
                }
                rueckgabe.add(physical + logical);
            }
        }
        return rueckgabe;
    }

    private List<String> checkMandatoryValues(DocStruct inStruct, ArrayList<String> inList, String language) {
        DocStructType dst = inStruct.getType();
        List<MetadataType> allMDTypes = dst.getAllMetadataTypes();
        for (MetadataType mdt : allMDTypes) {
            String number = dst.getNumberOfMetadataType(mdt);
            List<? extends Metadata> ll = null;
            //            if (!mdt.getIsPerson()) {
            ll = inStruct.getAllMetadataByType(mdt);
            //            } else {
            //                ll = inStruct.getAllPersonsByType(mdt);
            //            }
            int real = 0;
            // if (ll.size() > 0) {
            real = ll.size();

            if ((number.equals("1m") || number.equals("+")) && real == 1) {
                if (mdt.getIsPerson()) {
                    Person p = (Person) ll.get(0);
                    if (StringUtils.isEmpty(p.getFirstname()) && StringUtils.isEmpty(p.getLastname())) {
                        inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                + Helper.getTranslation("MetadataIsEmpty"));
                    }
                } else if (mdt.isCorporate()) {
                    Corporate c = (Corporate) ll.get(0);
                    if (StringUtils.isEmpty(c.getMainName())) {
                        inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                + Helper.getTranslation("MetadataIsEmpty"));
                    }
                } else {
                    Metadata md = ll.get(0);
                    if (md.getValue() == null || md.getValue().equals("")) {
                        inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                + Helper.getTranslation("MetadataIsEmpty"));

                    }
                }

            }
            /* jetzt die Typen prüfen */
            if (number.equals("1m") && real != 1) {
                inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation("MetadataNotOneElement") + " " + real + " " + Helper.getTranslation("MetadataTimes"));
            }
            if (number.equals("1o") && real > 1) {
                inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation("MetadataToManyElements") + " " + real + " " + Helper.getTranslation("MetadataTimes"));
            }
            if (number.equals("+") && real == 0) {
                inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation("MetadataNotEnoughElements"));
            }
            //            } else {
            //                List<Person> ll = inStruct.getAllPersonsByType(mdt);
            //                int real = 0;
            //                real = ll.size();
            //
            //            }
        }

        for (MetadataGroupType mgt : dst.getAllMetadataGroupTypes()) {
            String allowedNumber = dst.getNumberOfMetadataGroups(mgt);

            List<MetadataGroup> assignedGroups = inStruct.getAllMetadataGroupsByType(mgt);
            int realNumber = assignedGroups.size();

            if ((allowedNumber.equals("1m") || allowedNumber.equals("+")) && realNumber == 1) {
                // check if metadata has values
                MetadataGroup group = assignedGroups.get(0);
                boolean isEmpty = true;
                for (Metadata md : group.getMetadataList()) {
                    if (md.getValue() != null && md.getValue().length() > 0) {
                        isEmpty = false;
                        break;
                    }
                }
                if (isEmpty) {
                    inList.add(mgt.getLanguage(language) + " in " + dst.getNameByLanguage(language) + " " + Helper.getTranslation("MetadataIsEmpty"));
                }
            }
            if (allowedNumber.equals("1m") && realNumber != 1) {
                inList.add(mgt.getLanguage(language) + " in " + dst.getNameByLanguage(language) + " " + Helper.getTranslation("MetadataNotOneElement")
                + " " + realNumber + Helper.getTranslation("MetadataTimes"));
            }
            if (allowedNumber.equals("1o") && realNumber > 1) {
                inList.add(mgt.getLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation("MetadataToManyElements") + " " + realNumber + " " + Helper.getTranslation("MetadataTimes"));
            }
            if (allowedNumber.equals("+") && realNumber == 0) {
                inList.add(mgt.getLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation("MetadataNotEnoughElements"));
            }

        }
        // check fields of each metadata group
        if (inStruct.getAllMetadataGroups() != null) {
            for (MetadataGroup mg : inStruct.getAllMetadataGroups()) {
                for (MetadataType mdt : mg.getType().getMetadataTypeList()) {
                    int numberOfExistingFields = mg.countMDofthisType(mdt.getName());
                    String expected = mg.getType().getNumberOfMetadataType(mdt);
                    if (("1m".equals(expected) || "1o".equals(expected)) && numberOfExistingFields > 1) {
                        // to many fields
                        inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                + Helper.getTranslation("MetadataToManyElements") + " " + numberOfExistingFields + " "
                                + Helper.getTranslation("MetadataTimes"));
                    } else if (("1m".equals(expected) || "+".equals(expected)) && numberOfExistingFields == 0) {
                        // required field empty
                        inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                + Helper.getTranslation("MetadataNotEnoughElements"));
                    } else if ("1m".equals(expected) || "+".equals(expected)) {
                        // check if first field is filled
                        if (mdt.getIsPerson()) {
                            Person p = mg.getPersonByType(mdt.getName()).get(0);
                            if (StringUtils.isEmpty(p.getFirstname()) && StringUtils.isEmpty(p.getLastname())) {
                                inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                        + Helper.getTranslation("MetadataIsEmpty"));
                            }
                        } else if (mdt.isCorporate()) {
                            Corporate c = mg.getCorporateByType(mdt.getName()).get(0);
                            if (StringUtils.isEmpty(c.getMainName())) {
                                inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                        + Helper.getTranslation("MetadataIsEmpty"));
                            }
                        } else {
                            Metadata md = mg.getMetadataByType(mdt.getName()).get(0);
                            if (md.getValue() == null || md.getValue().equals("")) {
                                inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                        + Helper.getTranslation("MetadataIsEmpty"));
                            }
                        }
                    }
                }
            }
        }
        // }
        /* alle Kinder des aktuellen DocStructs durchlaufen */
        if (inStruct.getAllChildren() != null) {
            for (DocStruct child : inStruct.getAllChildren()) {
                checkMandatoryValues(child, inList, language);
            }
        }
        return inList;
    }

    /**
     * individuelle konfigurierbare projektspezifische Validierung der Metadaten ================================================================
     */
    private List<String> checkConfiguredValidationValues(DocStruct inStruct, ArrayList<String> inFehlerList, Prefs inPrefs, String language) {
        /*
         * -------------------------------- Konfiguration öffnen und die Validierungsdetails auslesen --------------------------------
         */
        ConfigProjects cp = null;
        try {
            cp = new ConfigProjects(this.myProzess.getProjekt().getTitel());
        } catch (IOException e) {
            Helper.setFehlerMeldung("[" + this.myProzess.getTitel() + "] " + "IOException", e.getMessage());
            return inFehlerList;
        }
        int count = cp.getParamList("validate.metadata").size();
        for (int i = 0; i < count; i++) {

            /* Attribute auswerten */
            String prop_metadatatype = cp.getParamString("validate.metadata(" + i + ")[@metadata]");
            String prop_doctype = cp.getParamString("validate.metadata(" + i + ")[@docstruct]");
            String prop_startswith = cp.getParamString("validate.metadata(" + i + ")[@startswith]");
            String prop_endswith = cp.getParamString("validate.metadata(" + i + ")[@endswith]");
            String prop_createElementFrom = cp.getParamString("validate.metadata(" + i + ")[@createelementfrom]");
            DocStruct myStruct = inStruct;
            MetadataType mdt = null;
            try {
                mdt = this.ughhelp.getMetadataType(inPrefs, prop_metadatatype);
            } catch (UghHelperException e) {
                Helper.setFehlerMeldung("[" + this.myProzess.getTitel() + "] " + "Metadatatype does not exist: ", prop_metadatatype);
            }
            /*
             * wenn das Metadatum des FirstChilds überprüfen werden soll, dann dieses jetzt (sofern vorhanden) übernehmen
             */
            if (prop_doctype != null && prop_doctype.equals("firstchild")) {
                if (myStruct.getAllChildren() != null && myStruct.getAllChildren().size() > 0) {
                    myStruct = myStruct.getAllChildren().get(0);
                } else {
                    continue;
                }
            }

            /*
             * wenn der MetadatenTyp existiert, dann jetzt die nötige Aktion überprüfen
             */
            if (mdt != null) {
                /* ein CreatorsAllOrigin soll erzeugt werden */
                if (prop_createElementFrom != null) {
                    ArrayList<MetadataType> listOfFromMdts = new ArrayList<>();
                    StringTokenizer tokenizer = new StringTokenizer(prop_createElementFrom, "|");
                    while (tokenizer.hasMoreTokens()) {
                        String tok = tokenizer.nextToken();
                        try {
                            MetadataType emdete = this.ughhelp.getMetadataType(inPrefs, tok);
                            listOfFromMdts.add(emdete);
                        } catch (UghHelperException e) {
                            /*
                             * wenn die zusammenzustellenden Personen für CreatorsAllOrigin als Metadatatyp nicht existieren, Exception abfangen und
                             * nicht weiter drauf eingehen
                             */
                        }
                    }
                    if (listOfFromMdts.size() > 0) {
                        checkCreateElementFrom(inFehlerList, listOfFromMdts, myStruct, mdt, language);
                    }
                } else {
                    checkStartsEndsWith(inFehlerList, prop_startswith, prop_endswith, myStruct, mdt, language);
                }
            }
        }
        return inFehlerList;
    }

    /**
     * Create Element From - für alle Strukturelemente ein bestimmtes Metadatum erzeugen, sofern dies an der jeweiligen Stelle erlaubt und noch nicht
     * vorhanden ================================================================
     */
    private void checkCreateElementFrom(ArrayList<String> inFehlerList, ArrayList<MetadataType> inListOfFromMdts, DocStruct myStruct,
            MetadataType mdt, String language) {

        /*
         * -------------------------------- existiert das zu erzeugende Metadatum schon, dann überspringen, ansonsten alle Daten zusammensammeln und
         * in das neue Element schreiben --------------------------------
         */
        List<? extends Metadata> createMetadaten = myStruct.getAllMetadataByType(mdt);
        if (createMetadaten == null || createMetadaten.size() == 0) {
            try {
                Metadata createdElement = new Metadata(mdt);
                StringBuffer myValue = new StringBuffer();
                /*
                 * alle anzufügenden Metadaten durchlaufen und an das Element anh�ngen
                 */
                for (MetadataType mdttemp : inListOfFromMdts) {

                    List<Person> fromElemente = myStruct.getAllPersons();
                    if (fromElemente != null && fromElemente.size() > 0) {
                        /*
                         * wenn Personen vorhanden sind (z.B. Illustrator), dann diese durchlaufen
                         */
                        for (Person p : fromElemente) {

                            if (p.getRole() == null) {
                                Helper.setFehlerMeldung("[" + this.myProzess.getTitel() + " " + myStruct.getType().getNameByLanguage(language) + "] "
                                        + Helper.getTranslation("MetadataPersonWithoutRole"));
                                break;
                            } else {
                                if (p.getRole().equals(mdttemp.getName())) {
                                    if (myValue.length() > 0) {
                                        myValue.append("; ");
                                    }
                                    if (StringUtils.isNotBlank(p.getLastname())) {
                                        myValue.append(p.getLastname());
                                    }
                                    if (StringUtils.isNotBlank(p.getFirstname())) {
                                        myValue.append(", ");
                                        myValue.append(p.getFirstname());
                                    }
                                }
                            }
                        }
                    }
                }

                if (myValue.length() > 0) {
                    createdElement.setValue(myValue.toString());

                    myStruct.addMetadata(createdElement);
                }
            } catch (DocStructHasNoTypeException e) {
            } catch (MetadataTypeNotAllowedException e) {
            }

        }

        /*
         * -------------------------------- alle Kinder durchlaufen --------------------------------
         */
        List<DocStruct> children = myStruct.getAllChildren();
        if (children != null && children.size() > 0) {
            for (Iterator<DocStruct> iter = children.iterator(); iter.hasNext();) {
                checkCreateElementFrom(inFehlerList, inListOfFromMdts, iter.next(), mdt, language);
            }
        }
    }

    /**
     * Metadatum soll mit bestimmten String beginnen oder enden ================================================================
     */
    private void checkStartsEndsWith(List<String> inFehlerList, String prop_startswith, String prop_endswith, DocStruct myStruct, MetadataType mdt,
            String language) {
        /* startswith oder endswith */
        List<? extends Metadata> alleMetadaten = myStruct.getAllMetadataByType(mdt);
        if (alleMetadaten != null && alleMetadaten.size() > 0) {
            for (Iterator<? extends Metadata> iter = alleMetadaten.iterator(); iter.hasNext();) {
                Metadata md = iter.next();

                /* prüfen, ob es mit korrekten Werten beginnt */
                if (prop_startswith != null) {
                    boolean isOk = false;
                    StringTokenizer tokenizer = new StringTokenizer(prop_startswith, "|");
                    while (tokenizer.hasMoreTokens()) {
                        String tok = tokenizer.nextToken();
                        if (md.getValue() != null && md.getValue().startsWith(tok)) {
                            isOk = true;
                        }
                    }
                    if (!isOk && !this.autoSave) {
                        inFehlerList.add(md.getType().getNameByLanguage(language) + " " + Helper.getTranslation("MetadataWithValue") + " "
                                + md.getValue() + " " + Helper.getTranslation("MetadataDoesNotStartWith") + " " + prop_startswith);
                    }
                    if (!isOk && this.autoSave) {
                        md.setValue(new StringTokenizer(prop_startswith, "|").nextToken() + md.getValue());
                    }
                }
                /* prüfen, ob es mit korrekten Werten endet */
                if (prop_endswith != null) {
                    boolean isOk = false;
                    StringTokenizer tokenizer = new StringTokenizer(prop_endswith, "|");
                    while (tokenizer.hasMoreTokens()) {
                        String tok = tokenizer.nextToken();
                        if (md.getValue() != null && md.getValue().endsWith(tok)) {
                            isOk = true;
                        }
                    }
                    if (!isOk && !this.autoSave) {
                        inFehlerList.add(md.getType().getNameByLanguage(language) + " " + Helper.getTranslation("MetadataWithValue") + " "
                                + md.getValue() + " " + Helper.getTranslation("MetadataDoesNotEndWith") + " " + prop_endswith);
                    }
                    if (!isOk && this.autoSave) {
                        md.setValue(md.getValue() + new StringTokenizer(prop_endswith, "|").nextToken());
                    }
                }
            }
        }
    }

    private List<String> validateMetadatValues(DocStruct inStruct, String lang) {
        List<String> errorList = new ArrayList<>();
        List<Metadata> metadataList = inStruct.getAllMetadata();
        if (metadataList != null) {
            for (Metadata md : metadataList) {
                if (StringUtils.isNotBlank(md.getType().getValidationExpression())) {
                    String regularExpression = md.getType().getValidationExpression();
                    if (md.getValue() == null || !md.getValue().matches(regularExpression)) {
                        String errorMessage = md.getType().getValidationErrorMessages().get(lang);
                        if (StringUtils.isNotBlank(errorMessage)) {
                            errorList.add(errorMessage.replace("{}", md.getValue()));
                        } else {
                            errorList.add(Helper.getTranslation("mets_ErrorRegularExpression", md.getType().getNameByLanguage(lang), md.getValue(),
                                    regularExpression));
                        }
                    }
                }
            }
        }
        List<MetadataGroup> groupList = inStruct.getAllMetadataGroups();
        if (groupList != null) {
            for (MetadataGroup mg : groupList) {
                for (Metadata md : mg.getMetadataList()) {
                    if (StringUtils.isNotBlank(md.getType().getValidationExpression())) {
                        String regularExpression = md.getType().getValidationExpression();
                        if (md.getValue() == null || !md.getValue().matches(regularExpression)) {
                            String errorMessage = md.getType().getValidationErrorMessages().get(lang);
                            if (StringUtils.isNotBlank(errorMessage)) {
                                errorList.add(errorMessage.replace("{}", md.getValue()));
                            } else {
                                errorList.add(Helper.getTranslation("mets_ErrorRegularExpression", md.getType().getNameByLanguage(lang), md.getValue(),
                                        regularExpression));
                            }
                        }
                    }
                }
            }
        }

        if (inStruct.getAllChildren() != null) {
            for (DocStruct child : inStruct.getAllChildren()) {
                errorList.addAll(validateMetadatValues(child, lang));
            }
        }

        return errorList;
    }

    /**
     * automatisch speichern lassen, wenn Änderungen nötig waren ================================================================
     */
    public boolean isAutoSave() {
        return this.autoSave;
    }

    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    public boolean validateIdentifier(DocStruct uppermostStruct) {

        if (uppermostStruct.getType().isAnchor()) {
            String language = Helper.getMetadataLanguage();
            if (language == null) {
                language = "en";
            }

            if (uppermostStruct.getAllIdentifierMetadata() != null && uppermostStruct.getAllIdentifierMetadata().size() > 0) {
                Metadata identifierTopStruct = uppermostStruct.getAllIdentifierMetadata().get(0);
                try {
                    if (identifierTopStruct.getValue() == null || identifierTopStruct.getValue().length() == 0) {
                        Helper.setFehlerMeldung(identifierTopStruct.getType().getNameByLanguage(language) + " in "
                                + uppermostStruct.getType().getNameByLanguage(language) + " " + Helper.getTranslation("MetadataIsEmpty"));
                        return false;
                    }
                    if (!identifierTopStruct.getValue().replaceAll("[\\w|-]", "").equals("")) {
                        Helper.setFehlerMeldung(Helper.getTranslation("MetadataIdentifierError")
                                + identifierTopStruct.getType().getNameByLanguage(language) + " in DocStruct "
                                + uppermostStruct.getType().getNameByLanguage(language) + Helper.getTranslation("MetadataInvalidCharacter"));
                        return false;
                    }
                    DocStruct firstChild = uppermostStruct.getAllChildren().get(0);
                    Metadata identifierFirstChild = firstChild.getAllIdentifierMetadata().get(0);
                    if (identifierFirstChild.getValue() == null || identifierFirstChild.getValue().length() == 0) {
                        return false;
                    }
                    if (!identifierFirstChild.getValue().replaceAll("[\\w|-]", "").equals("")) {
                        Helper.setFehlerMeldung(identifierTopStruct.getType().getNameByLanguage(language) + " in "
                                + uppermostStruct.getType().getNameByLanguage(language) + " " + Helper.getTranslation("MetadataIsEmpty"));
                        return false;
                    }
                    if (identifierTopStruct.getValue() != null && identifierTopStruct.getValue() != ""
                            && identifierTopStruct.getValue().equals(identifierFirstChild.getValue())) {
                        Helper.setFehlerMeldung(Helper.getTranslation("MetadataIdentifierError") + identifierTopStruct.getType().getName()
                                + Helper.getTranslation("MetadataIdentifierSame") + uppermostStruct.getType().getName() + " and "
                                + firstChild.getType().getName());
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }

        }
        return true;
    }

    public List<String> getProblems() {
        return problems;
    }
}
