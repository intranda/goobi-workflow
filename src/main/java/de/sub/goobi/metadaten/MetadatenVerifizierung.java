package de.sub.goobi.metadaten;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabulary;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabularyRecord;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.goobi.api.display.Item;
import org.goobi.api.display.enums.DisplayType;
import org.goobi.api.display.helper.ConfigDisplayRules;
import org.goobi.beans.Process;

import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
public class MetadatenVerifizierung {
    UghHelper ughhelp = new UghHelper();
    List<DocStruct> docStructsOhneSeiten;
    Process myProzess;
    boolean autoSave = false;

    private static final String IDENTIFIER_VALIDATION_REGEX = "[\\w|-]";

    private static final String INVALID_CHARACTER_ERROR = "InvalidIdentifierCharacter";
    private static final String METADATA_EMPTY_ERROR = "MetadataIsEmpty";
    private static final String METADATA_TO_MANY_ERROR = "MetadataToManyElements";
    private static final String METADATA_MISSING_ERROR = "MetadataNotOneElement";
    private static final String METADATA_NOT_ENOUGH_ERROR = "MetadataNotEnoughElements";
    private static final String METADATA_TIMES_ERROR = "MetadataTimes";
    private static final String METADATA_REGEX_ERROR = "mets_ErrorRegularExpression";

    @Getter
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
            Helper.setFehlerMeldung(this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): " + Helper.getTranslation("MetadataReadError"),
                    e.getMessage());
            problems.add(this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): " + Helper.getTranslation("MetadataReadError") + ": "
                    + e.getMessage());
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
            Helper.setFehlerMeldung(this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): "
                    + Helper.getTranslation("MetadataDigitalDocumentError") + inProzess.getTitel(), e.getMessage());
            problems.add(this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): " + Helper.getTranslation("MetadataDigitalDocumentError")
                    + ": " + e.getMessage());
            ergebnis = false;
        }

        DocStruct logical = dd.getLogicalDocStruct();
        if (logical == null) {
            Helper.setFehlerMeldung(
                    this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): " + Helper.getTranslation("MetadataPaginationError"));
            problems.add(this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): " + Helper.getTranslation("MetadataPaginationError"));
            ergebnis = false;
        }
        // reset old errors
        if (logical != null) {
            resetValidationErrors(logical);
            // run through all metadata, reset validation messages
            List<DocStruct> children = logical.getAllChildrenAsFlatList();
            if (children != null) {
                for (DocStruct child : children) {
                    resetValidationErrors(child);
                }
            }

            if (logical.getAllIdentifierMetadata() != null && !logical.getAllIdentifierMetadata().isEmpty()) {
                Metadata identifierTopStruct = logical.getAllIdentifierMetadata().get(0);
                try {
                    if (!checkIdentifier(metadataLanguage, logical, identifierTopStruct)) {
                        ergebnis = false;
                    }
                } catch (Exception e) {
                    // no firstChild or no identifier
                }
            }
        } else {
            Helper.setFehlerMeldung(
                    this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): " + Helper.getTranslation("MetadataMissingIdentifier"));
            problems.add(this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): " + Helper.getTranslation("MetadataMissingIdentifier"));
            ergebnis = false;
        }

        /*
         * -------------------------------- auf Docstructs ohne Seiten prüfen --------------------------------
         */

        if (ConfigurationHelper.getInstance().isMetsEditorValidateImages() && logical != null) {

            this.docStructsOhneSeiten = new ArrayList<>();
            this.checkDocStructsOhneSeiten(logical);
            if (!this.docStructsOhneSeiten.isEmpty()) {
                for (DocStruct ds : this.docStructsOhneSeiten) {
                    Helper.setFehlerMeldung(this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): "
                            + Helper.getTranslation("MetadataPaginationStructure") + ds.getType().getNameByLanguage(metadataLanguage));
                    problems.add(this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): "
                            + Helper.getTranslation("MetadataPaginationStructure") + ds.getType().getNameByLanguage(metadataLanguage));
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
            if (seitenOhneDocstructs != null && !seitenOhneDocstructs.isEmpty()) {
                for (String seite : seitenOhneDocstructs) {
                    Helper.setFehlerMeldung(
                            this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): " + Helper.getTranslation("MetadataPaginationPages"),
                            seite);
                    problems.add(this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): " + Helper.getTranslation("MetadataPaginationPages")
                            + ": " + seite);
                }
                ergebnis = false;
            }
        }

        /*
         * -------------------------------- check selec1 menus configured in metadataDisplayRules.xml --------------------------------
         */

        List<String> select1List = checkSelectOneMenus(myProzess, dd.getLogicalDocStruct(), new ArrayList<>(), metadataLanguage);
        if (!select1List.isEmpty()) {
            for (String temp : select1List) {
                Helper.setFehlerMeldung(
                        this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): " + Helper.getTranslation("MetadataSelectOneInvalidElement"),
                        temp);
                problems.add(this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): "
                        + Helper.getTranslation("MetadataSelectOneInvalidElement") + ": " + temp);
            }
            ergebnis = false;
        }

        // check whether vocabulary values in metadata are in the configured vocabulary data lists
        List<String> vocabularyErrors = checkSelectFromVocabularyList(myProzess, dd.getLogicalDocStruct(), new ArrayList<>(), metadataLanguage);
        if (!vocabularyErrors.isEmpty()) {
            for (String error : vocabularyErrors) {
                Helper.setFehlerMeldung(this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): " + error);
                problems.add(this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): " + error);
            }
            ergebnis = false;
        }

        /*
         * -------------------------------- auf mandatory Values der Metadaten prüfen --------------------------------
         */
        List<String> mandatoryList = checkMandatoryValues(dd.getLogicalDocStruct(), new ArrayList<>(), metadataLanguage);
        if (!mandatoryList.isEmpty()) {
            for (String temp : mandatoryList) {
                Helper.setFehlerMeldung(
                        this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): " + Helper.getTranslation("MetadataMandatoryElement"), temp);
                problems.add(this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): " + Helper.getTranslation("MetadataMandatoryElement")
                        + ": " + temp);
            }
            ergebnis = false;
        }

        /*
         * -------------------------------- auf Details in den Metadaten prüfen, die in der Konfiguration angegeben wurden
         * --------------------------------
         */
        List<String> configuredList = checkConfiguredValidationValues(dd.getLogicalDocStruct(), new ArrayList<>(), inPrefs, metadataLanguage);
        if (!configuredList.isEmpty()) {
            for (String temp : configuredList) {
                Helper.setFehlerMeldung(
                        this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): " + Helper.getTranslation("MetadataInvalidData"), temp);
                problems.add(this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): " + Helper.getTranslation("MetadataInvalidData") + ": "
                        + temp);
            }
            ergebnis = false;
        }

        List<String> expressionList = validateMetadataValues(dd.getLogicalDocStruct(), metadataLanguage);
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
            } catch (InvalidImagesException | IOException | SwapException e1) {
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

    private boolean checkIdentifier(String metadataLanguage, DocStruct logical, Metadata identifierTopStruct) {
        boolean ergebnis = true;
        if (!"".equals(identifierTopStruct.getValue().replaceAll(IDENTIFIER_VALIDATION_REGEX, ""))) {
            String[] parameter = { identifierTopStruct.getType().getNameByLanguage(metadataLanguage),
                    logical.getType().getNameByLanguage(metadataLanguage) };
            String errorText = Helper.getTranslation(INVALID_CHARACTER_ERROR, parameter);
            Helper.setFehlerMeldung(this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): "
                    + Helper.getTranslation(INVALID_CHARACTER_ERROR, parameter));
            problems.add(errorText);
            ergebnis = false;
            addErrorToDocStructAndMetadata(logical, identifierTopStruct, errorText);
        }
        DocStruct firstChild = logical.getAllChildren().get(0);
        Metadata identifierFirstChild = firstChild.getAllIdentifierMetadata().get(0);
        if (StringUtils.isNotBlank(identifierTopStruct.getValue())
                && identifierTopStruct.getValue().equals(identifierFirstChild.getValue())) {
            String[] parameter = { identifierTopStruct.getType().getName(), logical.getType().getName(), firstChild.getType().getName() };

            String errorText = Helper.getTranslation("InvalidIdentifierSame", parameter);
            Helper.setFehlerMeldung(this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): "
                    + Helper.getTranslation("InvalidIdentifierSame", parameter));
            problems.add(errorText);
            ergebnis = false;
            addErrorToDocStructAndMetadata(logical, identifierTopStruct, errorText);
            addErrorToDocStructAndMetadata(firstChild, identifierFirstChild, errorText);

        }
        if (!"".equals(identifierFirstChild.getValue().replaceAll(IDENTIFIER_VALIDATION_REGEX, ""))) {
            String[] parameter = { identifierTopStruct.getType().getNameByLanguage(metadataLanguage),
                    firstChild.getType().getNameByLanguage(metadataLanguage) };
            Helper.setFehlerMeldung(this.myProzess.getTitel() + " (" + this.myProzess.getId() + "): "
                    + Helper.getTranslation(INVALID_CHARACTER_ERROR, parameter));
            String errorText = Helper.getTranslation(INVALID_CHARACTER_ERROR, parameter);
            problems.add(errorText);
            ergebnis = false;
            addErrorToDocStructAndMetadata(firstChild, identifierFirstChild, errorText);
        }
        return ergebnis;
    }

    private void resetValidationErrors(DocStruct ds) {
        if (ds.getAllMetadata() != null) {
            for (Metadata md : ds.getAllMetadata()) {
                md.setValidationErrorPresent(false);
                md.setValidationMessage(null);
            }
        }
        if (ds.getAllCorporates() != null) {
            for (Corporate md : ds.getAllCorporates()) {
                md.setValidationErrorPresent(false);
                md.setValidationMessage(null);
            }

        }
        if (ds.getAllPersons() != null) {
            for (Person md : ds.getAllPersons()) {
                md.setValidationErrorPresent(false);
                md.setValidationMessage(null);
            }
        }
        if (ds.getAllMetadataGroups() != null) {
            for (MetadataGroup mg : ds.getAllMetadataGroups()) {
                resetMetadataGroupValidationErrors(mg);
            }
        }
    }

    private void resetMetadataGroupValidationErrors(MetadataGroup mg) {
        if (mg.getMetadataList() != null) {
            for (Metadata md : mg.getMetadataList()) {
                md.setValidationErrorPresent(false);
                md.setValidationMessage(null);
            }
        }
        if (mg.getCorporateList() != null) {
            for (Corporate md : mg.getCorporateList()) {
                md.setValidationErrorPresent(false);
                md.setValidationMessage(null);
            }

        }
        if (mg.getPersonList() != null) {
            for (Person md : mg.getPersonList()) {
                md.setValidationErrorPresent(false);
                md.setValidationMessage(null);
            }
        }
        if (mg.getAllMetadataGroups() != null) {
            for (MetadataGroup sub : mg.getAllMetadataGroups()) {
                resetMetadataGroupValidationErrors(sub);
            }
        }

    }

    private void checkDocStructsOhneSeiten(DocStruct inStruct) {
        if (inStruct.getAllToReferences().isEmpty() && !inStruct.getType().isAnchor()) {
            this.docStructsOhneSeiten.add(inStruct);
        }
        /* alle Kinder des aktuellen DocStructs durchlaufen */
        if (inStruct.getAllChildren() != null) {
            for (DocStruct child : inStruct.getAllChildren()) {
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
        for (DocStruct ds : boundbook.getAllChildren()) {
            List<Reference> refs = ds.getAllFromReferences();
            String physical = "";
            String logical = "";
            if (refs.isEmpty()) {

                for (Metadata md : ds.getAllMetadata()) {
                    if ("logicalPageNumber".equals(md.getType().getName())) {
                        logical = " (" + md.getValue() + ")";
                    }
                    if ("physPageNumber".equals(md.getType().getName())) {
                        physical = md.getValue();
                    }
                }
                rueckgabe.add(physical + logical);
            }
        }
        return rueckgabe;
    }

    private List<String> checkSelectOneMenus(Process inProcess, DocStruct inStruct, ArrayList<String> inList, String language) {
        String projectTitle = inProcess.getProjekt().getTitel();
        ConfigDisplayRules displayRules = ConfigDisplayRules.getInstance();
        DocStructType dst = inStruct.getType();
        List<MetadataType> allMDTypes = dst.getAllMetadataTypes();
        for (MetadataType mdt : allMDTypes) {
            DisplayType displayType = displayRules.getElementTypeByName(projectTitle, mdt.getName());
            if (displayType == DisplayType.select1) {
                List<Item> allowedItems = displayRules.getItemsByNameAndType(projectTitle, mdt.getName(), displayType);
                List<String> allowedValues = allowedItems.stream().map(Item::getValue).collect(Collectors.toList());
                List<? extends Metadata> ll = null;
                ll = inStruct.getAllMetadataByType(mdt);
                for (Metadata md : ll) {
                    String actualValue = md.getValue();
                    if (!allowedValues.contains(actualValue)) {
                        String errorMessage = mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                + Helper.getTranslation("MetadataNotConfiguredInDisplayRules", actualValue);
                        inList.add(errorMessage);
                        addErrorToDocStructAndMetadata(inStruct, md, errorMessage);
                    }
                }
            }
        }

        if (inStruct.getAllChildren() != null) {
            for (DocStruct child : inStruct.getAllChildren()) {
                checkSelectOneMenus(inProcess, child, inList, language);
            }
        }

        return inList;
    }

    private List<String> checkSelectFromVocabularyList(Process inProcess, DocStruct inStruct, ArrayList<String> inList, String language) {
        String projectTitle = inProcess.getProjekt().getTitel();
        ConfigDisplayRules displayRules = ConfigDisplayRules.getInstance();
        DocStructType dst = inStruct.getType();
        List<MetadataType> allMDTypes = dst.getAllMetadataTypes();
        for (MetadataType mdt : allMDTypes) {
            DisplayType displayType = displayRules.getElementTypeByName(projectTitle, mdt.getName());
            if (displayType == DisplayType.vocabularyList) {
                List<Item> allowedItems = displayRules.getItemsByNameAndType(projectTitle, mdt.getName(), displayType);
                if (allowedItems.get(0) == null) {
                    break;
                }
                ExtendedVocabulary vocabulary = VocabularyAPIManager.getInstance().vocabularies().findByName(allowedItems.get(0).getSource());
                List<ExtendedVocabularyRecord> records = VocabularyAPIManager.getInstance().vocabularyRecords()
                        .list(vocabulary.getId())
                        .all()
                        .request()
                        .getContent();
                List<String> allowedValues = records.stream()
                        .map(ExtendedVocabularyRecord::getMainValue)
                        .collect(Collectors.toList());

                List<? extends Metadata> ll = null;
                ll = inStruct.getAllMetadataByType(mdt);
                for (Metadata md : ll) {
                    String actualValue = md.getValue();
                    if (!allowedValues.contains(actualValue)) {
                        String errorMessage = mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + ": "
                                + Helper.getTranslation("VocabularySelectionInvalid", actualValue, allowedItems.get(0).getSource());
                        inList.add(errorMessage);
                        addErrorToDocStructAndMetadata(inStruct, md, errorMessage);
                    }
                }
            }
        }

        // TODO: Is this necessary?
        if (inStruct.getAllChildren() != null) {
            for (DocStruct child : inStruct.getAllChildren()) {
                checkSelectFromVocabularyList(inProcess, child, inList, language);
            }
        }

        return inList;
    }

    private List<String> checkMandatoryValues(DocStruct inStruct, ArrayList<String> inList, String language) {
        DocStructType dst = inStruct.getType();
        List<MetadataType> allMDTypes = dst.getAllMetadataTypes();
        String validationErrorMessage = "";
        for (MetadataType mdt : allMDTypes) {
            String number = dst.getNumberOfMetadataType(mdt);
            List<? extends Metadata> ll = null;
            ll = inStruct.getAllMetadataByType(mdt);

            int real = 0;
            real = ll.size();

            if (("1m".equals(number) || "+".equals(number)) && real == 1) {
                if (mdt.getIsPerson()) {
                    Person p = (Person) ll.get(0);
                    if (StringUtils.isEmpty(p.getFirstname()) && StringUtils.isEmpty(p.getLastname())) {
                        inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                + Helper.getTranslation(METADATA_EMPTY_ERROR));
                        addErrorToDocStructAndMetadata(inStruct, p, language);
                    }
                } else if (mdt.isCorporate()) {
                    Corporate c = (Corporate) ll.get(0);
                    if (StringUtils.isEmpty(c.getMainName())) {
                        inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                + Helper.getTranslation(METADATA_EMPTY_ERROR));
                        addErrorToDocStructAndMetadata(inStruct, c, language);
                    }
                } else {
                    Metadata md = ll.get(0);
                    if (md.getValue() == null || "".equals(md.getValue())) {
                        inList.add(mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                + Helper.getTranslation(METADATA_EMPTY_ERROR));
                        addErrorToDocStructAndMetadata(inStruct, md,
                                mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                        + Helper.getTranslation(METADATA_EMPTY_ERROR));
                    }
                }

            }
            /* jetzt die Typen prüfen */
            if ("1m".equals(number) && real != 1) {
                validationErrorMessage = mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation(METADATA_MISSING_ERROR) + " " + real + " " + Helper.getTranslation(METADATA_TIMES_ERROR);
                inList.add(validationErrorMessage);
                addMessageToMetadatumByMetadataType(inStruct, mdt, validationErrorMessage);
            }
            if ("1o".equals(number) && real > 1) {
                validationErrorMessage = mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation(METADATA_TO_MANY_ERROR) + " " + real + " " + Helper.getTranslation(METADATA_TIMES_ERROR);
                inList.add(validationErrorMessage);
                addMessageToMetadatumByMetadataType(inStruct, mdt, validationErrorMessage);
            }
            if ("+".equals(number) && real == 0) {
                validationErrorMessage = mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation(METADATA_NOT_ENOUGH_ERROR);
                inList.add(validationErrorMessage);
                addMessageToMetadatumByMetadataType(inStruct, mdt, validationErrorMessage);
            }
        }

        for (MetadataGroupType mgt : dst.getAllMetadataGroupTypes()) {
            String allowedNumber = dst.getNumberOfMetadataGroups(mgt);

            List<MetadataGroup> assignedGroups = inStruct.getAllMetadataGroupsByType(mgt);
            int realNumber = assignedGroups.size();

            if (("1m".equals(allowedNumber) || "+".equals(allowedNumber)) && realNumber == 1) {
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
                    inList.add(
                            mgt.getLanguage(language) + " in " + dst.getNameByLanguage(language) + " " + Helper.getTranslation(METADATA_EMPTY_ERROR));
                }
            }
            if ("1m".equals(allowedNumber) && realNumber != 1) {
                inList.add(mgt.getLanguage(language) + " in " + dst.getNameByLanguage(language) + " " + Helper.getTranslation(METADATA_MISSING_ERROR)
                        + " " + realNumber + Helper.getTranslation(METADATA_TIMES_ERROR));

            }
            if ("1o".equals(allowedNumber) && realNumber > 1) {
                inList.add(mgt.getLanguage(language) + " in " + dst.getNameByLanguage(language) + " " + Helper.getTranslation(METADATA_TO_MANY_ERROR)
                        + " " + realNumber + " " + Helper.getTranslation(METADATA_TIMES_ERROR));
            }
            if ("+".equals(allowedNumber) && realNumber == 0) {
                inList.add(mgt.getLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                        + Helper.getTranslation(METADATA_NOT_ENOUGH_ERROR));
            }

        }
        // check fields of each metadata group
        if (inStruct.getAllMetadataGroups() != null) {
            for (MetadataGroup mg : inStruct.getAllMetadataGroups()) {
                for (MetadataType mdt : mg.getType().getMetadataTypeList()) {
                    int numberOfExistingFields = mg.countMDofthisType(mdt.getName());
                    String expected = mg.getType().getNumberOfMetadataType(mdt);
                    if (("1m".equals(expected) || "1o".equals(expected)) && numberOfExistingFields > 1) {
                        // too many fields
                        validationErrorMessage = mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                + Helper.getTranslation(METADATA_TO_MANY_ERROR) + " " + numberOfExistingFields + " "
                                + Helper.getTranslation(METADATA_TIMES_ERROR);
                        inList.add(validationErrorMessage);
                        addMessageToMetadatumByMetadataType(inStruct, mdt, validationErrorMessage);
                    } else if (("1m".equals(expected) || "+".equals(expected)) && numberOfExistingFields == 0) {
                        // required field empty
                        validationErrorMessage = mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                + Helper.getTranslation(METADATA_NOT_ENOUGH_ERROR);
                        inList.add(validationErrorMessage);
                        addMessageToMetadatumByMetadataType(inStruct, mdt, validationErrorMessage);
                    } else if ("1m".equals(expected) || "+".equals(expected)) {
                        // check if first field is filled
                        if (mdt.getIsPerson()) {
                            Person p = mg.getPersonByType(mdt.getName()).get(0);
                            if (StringUtils.isEmpty(p.getFirstname()) && StringUtils.isEmpty(p.getLastname())) {
                                // required field empty
                                validationErrorMessage = mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                        + Helper.getTranslation(METADATA_EMPTY_ERROR);
                                inList.add(validationErrorMessage);
                                addMessageToMetadatumByMetadataType(inStruct, mdt, validationErrorMessage);
                            }
                        } else if (mdt.isCorporate()) {
                            Corporate c = mg.getCorporateByType(mdt.getName()).get(0);
                            if (StringUtils.isEmpty(c.getMainName())) {
                                validationErrorMessage = mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                        + Helper.getTranslation(METADATA_EMPTY_ERROR);
                                inList.add(validationErrorMessage);
                                addMessageToMetadatumByMetadataType(inStruct, mdt, validationErrorMessage);
                            }
                        } else {
                            Metadata md = mg.getMetadataByType(mdt.getName()).get(0);
                            if (md.getValue() == null || "".equals(md.getValue())) {
                                validationErrorMessage = mdt.getNameByLanguage(language) + " in " + dst.getNameByLanguage(language) + " "
                                        + Helper.getTranslation(METADATA_EMPTY_ERROR);
                                inList.add(validationErrorMessage);
                                addMessageToMetadatumByMetadataType(inStruct, mdt, validationErrorMessage);
                            }
                        }
                    }
                }
            }
        }
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
    private List<String> checkConfiguredValidationValues(DocStruct inStruct, ArrayList<String> inErrorList, Prefs inPrefs, String language) {
        /*
         * -------------------------------- Konfiguration öffnen und die Validierungsdetails auslesen --------------------------------
         */
        ConfigProjects cp = null;
        try {
            cp = new ConfigProjects(this.myProzess.getProjekt().getTitel());
        } catch (IOException e) {
            Helper.setFehlerMeldung("[" + this.myProzess.getTitel() + "] " + "IOException", e.getMessage());
            return inErrorList;
        } catch (PatternSyntaxException exception) {
            String message = Helper.getTranslation("projectErrorConfigurationHasInvalidTitle");
            Helper.setFehlerMeldung("[" + this.myProzess.getTitel() + "] " + message + ": " + exception.getPattern());
            return inErrorList;
        }
        List<HierarchicalConfiguration> validations = cp.getList("/validate/metadata");

        for (HierarchicalConfiguration val : validations) {

            /* Attribute auswerten */
            String propMetadatatype = val.getString("@metadata");
            String propDoctype = val.getString("@docstruct");
            String propStartswith = val.getString("@startswith");
            String propEndswith = val.getString("@endswith");
            String propCreateElementFrom = val.getString("@createelementfrom");
            DocStruct myStruct = inStruct;
            MetadataType mdt = null;
            try {
                mdt = this.ughhelp.getMetadataType(inPrefs, propMetadatatype);
            } catch (UghHelperException e) {
                Helper.setFehlerMeldung("[" + this.myProzess.getTitel() + "] " + "Metadatatype does not exist: ", propMetadatatype);
            }
            /*
             * wenn das Metadatum des FirstChilds überprüfen werden soll, dann dieses jetzt (sofern vorhanden) übernehmen
             */
            if (propDoctype != null && "firstchild".equals(propDoctype)) {
                if (myStruct.getAllChildren() != null && !myStruct.getAllChildren().isEmpty()) {
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
                if (propCreateElementFrom != null) {
                    ArrayList<MetadataType> listOfFromMdts = new ArrayList<>();
                    StringTokenizer tokenizer = new StringTokenizer(propCreateElementFrom, "|");
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
                    if (!listOfFromMdts.isEmpty()) {
                        checkCreateElementFrom(inErrorList, listOfFromMdts, myStruct, mdt, language);
                    }
                } else {
                    checkStartsEndsWith(inErrorList, propStartswith, propEndswith, myStruct, mdt, language);
                }
            }
        }
        return inErrorList;
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
        if (createMetadaten == null || createMetadaten.isEmpty()) {
            try {
                Metadata createdElement = new Metadata(mdt);
                StringBuilder myValue = new StringBuilder();
                /*
                 * alle anzufügenden Metadaten durchlaufen und an das Element anhaengen
                 */
                for (MetadataType mdttemp : inListOfFromMdts) {

                    List<Person> fromElemente = myStruct.getAllPersons();
                    if (fromElemente != null && !fromElemente.isEmpty()) {
                        /*
                         * wenn Personen vorhanden sind (z.B. Illustrator), dann diese durchlaufen
                         */
                        for (Person p : fromElemente) {

                            if (p.getRole() == null) {
                                Helper.setFehlerMeldung("[" + this.myProzess.getTitel() + " " + myStruct.getType().getNameByLanguage(language) + "] "
                                        + Helper.getTranslation("MetadataPersonWithoutRole"));
                                break;
                            } else if (p.getRole().equals(mdttemp.getName())) {
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

                if (myValue.length() > 0) {
                    createdElement.setValue(myValue.toString());

                    myStruct.addMetadata(createdElement);
                }
            } catch (DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
                // do nothing
            }

        }

        /*
         * -------------------------------- alle Kinder durchlaufen --------------------------------
         */
        List<DocStruct> children = myStruct.getAllChildren();
        if (children != null && !children.isEmpty()) {
            for (DocStruct child : children) {
                checkCreateElementFrom(inFehlerList, inListOfFromMdts, child, mdt, language);
            }
        }
    }

    /**
     * Metadatum soll mit bestimmten String beginnen oder enden ================================================================
     */
    private void checkStartsEndsWith(List<String> inFehlerList, String propStartswith, String propEndswith, DocStruct myStruct, MetadataType mdt,
            String language) {
        /* startswith oder endswith */
        List<? extends Metadata> alleMetadaten = myStruct.getAllMetadataByType(mdt);
        if (alleMetadaten != null && !alleMetadaten.isEmpty()) {
            for (Metadata md : alleMetadaten) {
                /* prüfen, ob es mit korrekten Werten beginnt */
                if (propStartswith != null) {
                    boolean isOk = false;
                    StringTokenizer tokenizer = new StringTokenizer(propStartswith, "|");
                    while (tokenizer.hasMoreTokens()) {
                        String tok = tokenizer.nextToken();
                        if (md.getValue() != null && md.getValue().startsWith(tok)) {
                            isOk = true;
                        }
                    }
                    if (!isOk && !this.autoSave) {
                        String errorMessage = md.getType().getNameByLanguage(language) + " " + Helper.getTranslation("MetadataWithValue") + " "
                                + md.getValue() + " " + Helper.getTranslation("MetadataDoesNotStartWith") + " " + propStartswith;
                        inFehlerList.add(errorMessage);
                        addErrorToDocStructAndMetadata(myStruct, md, errorMessage);
                    }
                    if (!isOk && this.autoSave) {
                        md.setValue(new StringTokenizer(propStartswith, "|").nextToken() + md.getValue());
                    }
                }
                /* prüfen, ob es mit korrekten Werten endet */
                if (propEndswith != null) {
                    boolean isOk = false;
                    StringTokenizer tokenizer = new StringTokenizer(propEndswith, "|");
                    while (tokenizer.hasMoreTokens()) {
                        String tok = tokenizer.nextToken();
                        if (md.getValue() != null && md.getValue().endsWith(tok)) {
                            isOk = true;
                        }
                    }
                    if (!isOk && !this.autoSave) {
                        String errorMessage = md.getType().getNameByLanguage(language) + " " + Helper.getTranslation("MetadataWithValue") + " "
                                + md.getValue() + " " + Helper.getTranslation("MetadataDoesNotEndWith") + " " + propEndswith;
                        inFehlerList.add(errorMessage);
                        addErrorToDocStructAndMetadata(myStruct, md, errorMessage);
                    }
                    if (!isOk && this.autoSave) {
                        md.setValue(md.getValue() + new StringTokenizer(propEndswith, "|").nextToken());
                    }
                }
            }
        }
    }

    private void addErrorToDocStructAndMetadata(DocStruct myStruct, Metadata md, String errorMessage) {
        myStruct.setValidationErrorPresent(true);
        if (myStruct.getValidationMessage() != null && !myStruct.getValidationMessage().contains(errorMessage)) {
            myStruct.setValidationMessage(
                    myStruct.getValidationMessage() + " & " + errorMessage);
        } else {
            myStruct.setValidationMessage(Helper.getTranslation(errorMessage));
        }
        md.setValidationErrorPresent(true);
        if (md.getValidationMessage() != null && !md.getValidationMessage().contains(errorMessage)) {
            md.setValidationMessage(md.getValidationMessage() + " & " + errorMessage);
        } else {
            md.setValidationMessage(errorMessage);
        }
    }

    private List<String> validateMetadataValues(DocStruct inStruct, String lang) {
        List<String> errorList = new ArrayList<>();
        List<Metadata> metadataList = inStruct.getAllMetadata();
        if (metadataList != null) {
            for (Metadata md : metadataList) {
                if (StringUtils.isNotBlank(md.getType().getValidationExpression())) {
                    checkValidationExpression(inStruct, lang, errorList, md);
                }
            }
        }
        List<MetadataGroup> groupList = inStruct.getAllMetadataGroups();
        if (groupList != null) {
            for (MetadataGroup mg : groupList) {
                for (Metadata md : mg.getMetadataList()) {
                    if (StringUtils.isNotBlank(md.getType().getValidationExpression())) {
                        checkValidationExpression(inStruct, lang, errorList, md);
                    }
                }
            }
        }

        if (inStruct.getAllChildren() != null) {
            for (DocStruct child : inStruct.getAllChildren()) {
                errorList.addAll(validateMetadataValues(child, lang));
            }
        }

        return errorList;
    }

    private void checkValidationExpression(DocStruct inStruct, String lang, List<String> errorList, Metadata md) {
        String regularExpression = md.getType().getValidationExpression();
        if (StringUtils.isNotBlank(md.getValue()) && !md.getValue().matches(regularExpression)) {
            String errorMessage = md.getType().getValidationErrorMessages().get(lang);
            if (StringUtils.isNotBlank(errorMessage)) {
                errorList.add(errorMessage.replace("{}", md.getValue()));
                addErrorToDocStructAndMetadata(inStruct, md, errorMessage.replace("{}", md.getValue()));
            } else {
                errorList.add(Helper.getTranslation(METADATA_REGEX_ERROR, md.getType().getNameByLanguage(lang), md.getValue(),
                        regularExpression));
                addErrorToDocStructAndMetadata(inStruct, md, Helper.getTranslation(METADATA_REGEX_ERROR,
                        md.getType().getNameByLanguage(lang), md.getValue(), regularExpression));
            }
        }
    }

    private void addMessageToMetadatumByMetadataType(DocStruct struct, MetadataType mdt, String message) {
        try {
            Metadata md = new Metadata(mdt);
            addErrorToDocStructAndMetadata(struct, md, message);
        } catch (MetadataTypeNotAllowedException e) {
            log.error(e);
        }
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
            if (uppermostStruct.getAllIdentifierMetadata() != null && !uppermostStruct.getAllIdentifierMetadata().isEmpty()) {
                Metadata identifierTopStruct = uppermostStruct.getAllIdentifierMetadata().get(0);

                try {
                    if (identifierTopStruct.getValue() == null || identifierTopStruct.getValue().length() == 0) {
                        Helper.setFehlerMeldung(identifierTopStruct.getType().getNameByLanguage(language) + " in "
                                + uppermostStruct.getType().getNameByLanguage(language) + " " + Helper.getTranslation(METADATA_EMPTY_ERROR));
                        return false;
                    }
                    if (!"".equals(identifierTopStruct.getValue().replaceAll(IDENTIFIER_VALIDATION_REGEX, ""))) {
                        Helper.setFehlerMeldung(Helper.getTranslation("MetadataIdentifierError")
                                + identifierTopStruct.getType().getNameByLanguage(language) + " in DocStruct "
                                + uppermostStruct.getType().getNameByLanguage(language) + " " + Helper.getTranslation("MetadataInvalidCharacter"));
                        return false;
                    }
                    DocStruct firstChild = uppermostStruct.getAllChildren().get(0);
                    Metadata identifierFirstChild = firstChild.getAllIdentifierMetadata().get(0);
                    if (identifierFirstChild.getValue() == null || identifierFirstChild.getValue().length() == 0) {
                        return false;
                    }
                    if (!"".equals(identifierFirstChild.getValue().replaceAll(IDENTIFIER_VALIDATION_REGEX, ""))) {
                        Helper.setFehlerMeldung(identifierTopStruct.getType().getNameByLanguage(language) + " in "
                                + uppermostStruct.getType().getNameByLanguage(language) + " " + Helper.getTranslation(METADATA_EMPTY_ERROR));
                        return false;
                    }
                    if (StringUtils.isNotBlank(identifierTopStruct.getValue())
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
}
