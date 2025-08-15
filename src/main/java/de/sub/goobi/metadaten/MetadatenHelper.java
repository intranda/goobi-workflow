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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.goobi.beans.Process;
import org.goobi.beans.Ruleset;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperComparator;
import jakarta.faces.model.SelectItem;
import lombok.extern.log4j.Log4j2;
import ugh.dl.Corporate;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.ExportFileformat;
import ugh.dl.Fileformat;
import ugh.dl.HoldingElement;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import ugh.dl.NamePart;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.dl.Reference;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.UGHException;

@Log4j2
public class MetadatenHelper {
    public static final int PAGENUMBER_FIRST = 0;
    public static final int PAGENUMBER_LAST = 1;

    private Prefs myPrefs;
    private DigitalDocument mydocument;

    /* =============================================================== */

    public MetadatenHelper(Prefs inPrefs, DigitalDocument inDocument) {
        this.myPrefs = inPrefs;
        this.mydocument = inDocument;
    }

    /* =============================================================== */
    public DocStruct ChangeCurrentDocstructType(DocStruct inOldDocstruct, String inNewType)
            throws DocStructHasNoTypeException, MetadataTypeNotAllowedException, TypeNotAllowedAsChildException, TypeNotAllowedForParentException {

        String isNotAllowedInNewElement = " is not allowed in new element ";

        DocStructType dst = this.myPrefs.getDocStrctTypeByName(inNewType);
        DocStruct newDocstruct = this.mydocument.createDocStruct(dst);
        /*
         * -------------------------------- alle Metadaten hinzufügen --------------------------------
         */
        if (inOldDocstruct.getAllMetadata() != null && !inOldDocstruct.getAllMetadata().isEmpty()) {
            for (Metadata old : inOldDocstruct.getAllMetadata()) {
                boolean match = false;
                String error = "Metadata " + old.getType().getName() + isNotAllowedInNewElement + newDocstruct.getType().getName();
                if (old.getValue() != null && !old.getValue().isEmpty()) {
                    if (newDocstruct.getAddableMetadataTypes(true) != null && !newDocstruct.getAddableMetadataTypes(true).isEmpty()) {
                        for (MetadataType mt : newDocstruct.getAddableMetadataTypes(true)) {
                            if (mt.getName().equals(old.getType().getName())) {
                                match = true;
                                break;
                            }
                        }
                        if (match) {
                            newDocstruct.addMetadata(old);
                        } else {
                            try {
                                newDocstruct.addMetadata(old);
                            } catch (UGHException e) {
                                Helper.setFehlerMeldung(error);
                                return inOldDocstruct;
                            }
                        }

                    } else {
                        Helper.setFehlerMeldung(error);
                        return inOldDocstruct;
                    }
                }
            }
        }
        /*
         * -------------------------------- alle Personen hinzufügen --------------------------------
         */
        if (inOldDocstruct.getAllPersons() != null && !inOldDocstruct.getAllPersons().isEmpty()) {
            for (Person old : inOldDocstruct.getAllPersons()) {
                boolean match = false;
                String error = "Person " + old.getType().getName() + isNotAllowedInNewElement + newDocstruct.getType().getName();
                if ((old.getFirstname() != null && !old.getFirstname().isEmpty()) || (old.getLastname() != null && !old.getLastname().isEmpty())) {

                    if (newDocstruct.getAddableMetadataTypes(true) != null && !newDocstruct.getAddableMetadataTypes(true).isEmpty()) {
                        for (MetadataType mt : newDocstruct.getAddableMetadataTypes(true)) {
                            if (mt.getName().equals(old.getType().getName())) {
                                match = true;
                                break;
                            }
                        }
                        if (match) {
                            newDocstruct.addPerson(old);
                        } else {
                            Helper.setFehlerMeldung(error);
                        }
                    } else {
                        Helper.setFehlerMeldung(error);
                        return inOldDocstruct;
                    }
                }
            }
        }
        if (inOldDocstruct.getAllMetadataGroups() != null && !inOldDocstruct.getAllMetadataGroups().isEmpty()) {
            for (MetadataGroup mg : inOldDocstruct.getAllMetadataGroups()) {
                boolean match = false;
                String error = "Person " + mg.getType().getName() + isNotAllowedInNewElement + newDocstruct.getType().getName();
                if (newDocstruct.getPossibleMetadataGroupTypes() != null && !newDocstruct.getPossibleMetadataGroupTypes().isEmpty()) {
                    for (MetadataGroupType mgt : newDocstruct.getPossibleMetadataGroupTypes()) {
                        if (mgt.getName().equals(mg.getType().getName())) {
                            match = true;
                            break;
                        }
                    }
                    if (match) {
                        newDocstruct.addMetadataGroup(mg);
                    } else {
                        Helper.setFehlerMeldung(error);
                    }
                } else {
                    Helper.setFehlerMeldung(error);
                    return inOldDocstruct;
                }

            }

        }

        /*
         * -------------------------------- alle Seiten hinzufügen --------------------------------
         */
        if (inOldDocstruct.getAllToReferences() != null) {
            for (Reference p : inOldDocstruct.getAllToReferences()) {
                newDocstruct.addReferenceTo(p.getTarget(), p.getType());
            }
        }

        /*
         * -------------------------------- alle Docstruct-Children hinzufügen --------------------------------
         */
        if (inOldDocstruct.getAllChildren() != null && !inOldDocstruct.getAllChildren().isEmpty()) {
            for (DocStruct old : inOldDocstruct.getAllChildren()) {
                String error = "Child element " + old.getType().getName() + isNotAllowedInNewElement + newDocstruct.getType().getName();
                if (newDocstruct.getType().getAllAllowedDocStructTypes() != null && !newDocstruct.getType().getAllAllowedDocStructTypes().isEmpty()) {

                    if (!newDocstruct.getType().getAllAllowedDocStructTypes().contains(old.getType().getName())) {
                        Helper.setFehlerMeldung(error);
                        return inOldDocstruct;
                    } else {
                        newDocstruct.addChild(old);
                    }
                } else {
                    Helper.setFehlerMeldung(error);
                    return inOldDocstruct;
                }
            }
        }
        /*
         * -------------------------------- neues Docstruct zum Parent hinzufügen und an die gleiche Stelle schieben, wie den Vorg?nger
         * --------------------------------
         */
        newDocstruct.setParent(inOldDocstruct.getParent());

        int index = inOldDocstruct.getParent().getAllChildren().indexOf(inOldDocstruct);
        inOldDocstruct.getParent().getAllChildren().add(index, newDocstruct);
        /*
         * -------------------------------- altes Docstruct vom Parent entfernen und neues als aktuelles nehmen --------------------------------
         */
        inOldDocstruct.getParent().removeChild(inOldDocstruct);
        return newDocstruct;
    }

    /* =============================================================== */

    public void KnotenUp(DocStruct inStruct) throws TypeNotAllowedAsChildException {
        DocStruct parent = inStruct.getParent();
        if (parent == null) {
            return;
        }

        int index = parent.getAllChildren().indexOf(inStruct);
        if (index != 0) {
            parent.getAllChildren().remove(inStruct);
            parent.getAllChildren().add(index - 1, inStruct);

        }

    }

    /* =============================================================== */

    public void KnotenDown(DocStruct inStruct) throws TypeNotAllowedAsChildException {
        DocStruct parent = inStruct.getParent();
        if (parent == null) {
            return;
        }
        int max = parent.getAllChildren().size();
        int index = parent.getAllChildren().indexOf(inStruct);

        if (max - 1 > index) {
            parent.getAllChildren().remove(inStruct);
            parent.getAllChildren().add(index + 1, inStruct);
        }

    }

    /* =============================================================== */

    /**
     * die MetadatenTypen zurückgeben.
     *
     * @param inStruct
     * @param checkTypesFromParent
     * @return metadata list
     */
    public SelectItem[] getAddableDocStructTypen(DocStruct inStruct, boolean checkTypesFromParent) {
        /*
         * -------------------------------- zuerst mal die addierbaren Metadatentypen ermitteln --------------------------------
         */
        List<String> types;
        SelectItem[] myTypes = new SelectItem[0];

        try {
            if (!checkTypesFromParent) {
                types = inStruct.getType().getAllAllowedDocStructTypes();
            } else {
                types = inStruct.getParent().getType().getAllAllowedDocStructTypes();
            }
        } catch (NullPointerException e) {
            return myTypes;
        }

        if (types == null) {
            return myTypes;
        }

        List<DocStructType> newTypes = new ArrayList<>();
        for (String tempTitel : types) {
            DocStructType dst = this.myPrefs.getDocStrctTypeByName(tempTitel);
            if (dst != null) {
                newTypes.add(dst);
            } else {
                Helper.setMeldung(null, "Regelsatz-Fehler: ", " DocstructType " + tempTitel + " nicht definiert");
                log.error("getAddableDocStructTypen() - Regelsatz-Fehler: DocstructType " + tempTitel + " nicht definiert");
            }
        }

        /*
         * -------------------------------- die Metadatentypen sortieren --------------------------------
         */
        HelperComparator c = new HelperComparator();
        c.setSortierart("DocStructTypen");
        // TODO: Uses generics, if possible
        Collections.sort(newTypes, c);

        /*
         * -------------------------------- nun ein Array mit der richtigen Größe anlegen --------------------------------
         */
        int zaehler = newTypes.size();
        myTypes = new SelectItem[zaehler];

        /*
         * -------------------------------- und anschliessend alle Elemente in das Array packen --------------------------------
         */
        zaehler = 0;
        for (DocStructType dst : newTypes) {
            String label = dst.getNameByLanguage(Helper.getMetadataLanguage());
            if (label == null) {
                label = dst.getName();
            }
            myTypes[zaehler] = new SelectItem(dst, label);
            zaehler++;
        }
        return myTypes;
    }

    /**
     * alle unbenutzen Metadaten des Docstruct löschen, Unterelemente rekursiv aufrufen.
     * 
     * @param inStruct ================================================================
     */
    public void deleteAllUnusedElements(DocStruct inStruct) {
        inStruct.deleteUnusedPersonsAndMetadata();
        if (inStruct.getAllChildren() != null && !inStruct.getAllChildren().isEmpty()) {
            for (DocStruct ds : inStruct.getAllChildren()) {
                deleteAllUnusedElements(ds);
            }
        }
    }

    /**
     * die erste Imagenummer zurückgeben.
     * 
     * @param inStrukturelement
     * @param inPageNumber
     * 
     */
    // TODO: alphanumerisch

    public MutablePair<String, String> getImageNumber(DocStruct inStrukturelement, int inPageNumber) {
        String physical = "";
        String logical = "";

        if (inStrukturelement == null) {
            return null;
        }
        List<Reference> listReferenzen = inStrukturelement.getAllReferences("to");
        if (listReferenzen != null && !listReferenzen.isEmpty()) {
            /*
             * -------------------------------- Referenzen sortieren --------------------------------
             */
            Collections.sort(listReferenzen, (o1, o2) -> {
                final Reference r1 = o1;
                final Reference r2 = o2;
                Integer page1 = 0;
                Integer page2 = 0;

                final MetadataType mdt = MetadatenHelper.this.myPrefs.getMetadataTypeByName("physPageNumber");
                List<? extends Metadata> listMetadaten = r1.getTarget().getAllMetadataByType(mdt);
                if (listMetadaten != null && !listMetadaten.isEmpty()) {
                    final Metadata meineSeite = listMetadaten.get(0);
                    page1 = Integer.parseInt(meineSeite.getValue());
                }
                listMetadaten = r2.getTarget().getAllMetadataByType(mdt);
                if (listMetadaten != null && !listMetadaten.isEmpty()) {
                    final Metadata meineSeite = listMetadaten.get(0);
                    page2 = Integer.parseInt(meineSeite.getValue());
                }
                return page1.compareTo(page2);
            });

            MetadataType mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
            List<? extends Metadata> listSeiten = listReferenzen.get(0).getTarget().getAllMetadataByType(mdt);
            if (inPageNumber == PAGENUMBER_LAST) {
                listSeiten = listReferenzen.get(listReferenzen.size() - 1).getTarget().getAllMetadataByType(mdt);
            }
            if (listSeiten != null && !listSeiten.isEmpty()) {
                Metadata meineSeite = listSeiten.get(0);
                physical = meineSeite.getValue();
            }
            mdt = this.myPrefs.getMetadataTypeByName("logicalPageNumber");
            listSeiten = listReferenzen.get(0).getTarget().getAllMetadataByType(mdt);
            if (inPageNumber == PAGENUMBER_LAST) {
                listSeiten = listReferenzen.get(listReferenzen.size() - 1).getTarget().getAllMetadataByType(mdt);
            }
            if (listSeiten != null && !listSeiten.isEmpty()) {
                Metadata meineSeite = listSeiten.get(0);
                logical = meineSeite.getValue();
            }
        }
        if (physical.length() > 0) {
            return new MutablePair<>(physical, logical);
        } else {
            return null;
        }
    }

    /**
     * vom übergebenen DocStruct alle Metadaten ermitteln und um die fehlenden DefaultDisplay-Metadaten ergänzen.
     * ================================================================
     */
    public List<? extends Metadata> getMetadataInclDefaultDisplay(DocStruct inStruct, String inLanguage, Metadaten.MetadataTypes metadataType,
            Process inProzess, boolean displayInternalMetadata) {
        List<MetadataType> allowedMetadataTypes = inStruct.getType().getAllMetadataTypes();

        List<MetadataType> displayMetadataTypes = inStruct.getType().getAllDefaultDisplayMetadataTypes();

        List<Metadata> allMetadata = new LinkedList<>();
        List<Corporate> allCorporates = new LinkedList<>();
        List<Person> allPersons = new LinkedList<>();
        /* sofern Default-Metadaten vorhanden sind, diese ggf. ergänzen */
        if (allowedMetadataTypes != null) {
            for (MetadataType mdt : allowedMetadataTypes) {
                // check if data exists
                List<? extends Metadata> existingData = inStruct.getAllMetadataByType(mdt);
                try {
                    // check if metadata exists and contains data
                    if (existingData != null && !existingData.isEmpty()) {
                        if (mdt.getIsPerson()) {
                            for (int i = 0; i < existingData.size(); i++) {
                                Person p = (Person) existingData.get(i);
                                allPersons.add(p);
                            }
                        } else if (mdt.isCorporate()) {
                            for (int i = 0; i < existingData.size(); i++) {
                                Corporate corporate = (Corporate) existingData.get(i);
                                allCorporates.add(corporate);
                            }
                        } else {
                            allMetadata.addAll(existingData);
                        }
                    } else // check if it is in the default list
                    if (displayMetadataTypes.contains(mdt)) {
                        if (mdt.getIsPerson()) {
                            Person p = new Person(mdt);
                            p.setRole(mdt.getName());
                            p.setParent(inStruct);
                            allPersons.add(p);
                        } else if (mdt.isCorporate()) {
                            Corporate corporate = new Corporate(mdt);
                            corporate.setRole(mdt.getName());
                            corporate.setParent(inStruct);
                            allCorporates.add(corporate);
                        } else {
                            Metadata md = new Metadata(mdt);
                            md.setParent(inStruct);
                            allMetadata.add(md); // add this new metadata
                        }
                    }
                } catch (DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
                    // do nothing
                }
            }
            inStruct.setAllMetadata(allMetadata);
            inStruct.setAllCorporates(allCorporates);
            inStruct.setAllPersons(allPersons);

        }

        /*
         * wenn keine Sortierung nach Regelsatz erfolgen soll, hier alphabetisch sortieren
         */
        if (metadataType == Metadaten.MetadataTypes.PERSON) {
            List<Person> persons = inStruct.getAllPersons();
            if (persons != null && !inProzess.getRegelsatz().isOrderMetadataByRuleset()) {
                Collections.sort(persons, new MetadataComparator(inLanguage));
            }
            return persons;
        } else if (metadataType == Metadaten.MetadataTypes.CORPORATE) {
            List<Corporate> corpList = inStruct.getAllCorporates();
            if (corpList != null && !inProzess.getRegelsatz().isOrderMetadataByRuleset()) {
                Collections.sort(corpList, new MetadataComparator(inLanguage));
            }
            return corpList;
        } else {
            List<Metadata> metadata = inStruct.getAllMetadata();
            if (metadata != null && !inProzess.getRegelsatz().isOrderMetadataByRuleset()) {
                Collections.sort(metadata, new MetadataComparator(inLanguage));
            }
            if (displayInternalMetadata) {
                return metadata;
            } else {
                return getAllVisibleMetadataHack(inStruct);
            }

        }
    }

    /**
     * vom übergebenen DocStruct alle Metadaten ermitteln und um die fehlenden DefaultDisplay-Metadaten ergänzen.
     * ================================================================
     */
    public List<MetadataGroup> getMetadataGroupsInclDefaultDisplay(DocStruct inStruct, String inLanguage, Process inProzess) {
        List<MetadataGroupType> displayMetadataTypes = inStruct.getDefaultDisplayMetadataGroupTypes();
        /* sofern Default-Metadaten vorhanden sind, diese ggf. ergänzen */
        if (displayMetadataTypes != null) {
            for (MetadataGroupType mdt : displayMetadataTypes) {
                // check, if mdt is already in the allMDs Metadata list, if not
                // - add it
                if (!(inStruct.getAllMetadataGroupsByType(mdt) != null && !inStruct.getAllMetadataGroupsByType(mdt).isEmpty())) {
                    try {

                        MetadataGroup md = new MetadataGroup(mdt);
                        inStruct.addMetadataGroup(md); // add this new metadata
                        // element

                    } catch (DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
                        // do nothing
                    }
                }

            }
        }

        /*
         * wenn keine Sortierung nach Regelsatz erfolgen soll, hier alphabetisch sortieren
         */

        List<MetadataGroup> answer = inStruct.getAllMetadataGroups();
        if (answer != null && !inProzess.getRegelsatz().isOrderMetadataByRuleset()) {
            // TODO order groups

            // order metadata within each group
            for (MetadataGroup mg : answer) {
                Collections.sort(mg.getMetadataList(), new MetadataComparator(inLanguage));
                Collections.sort(mg.getPersonList(), new MetadataComparator(inLanguage));
                Collections.sort(mg.getCorporateList(), new MetadataComparator(inLanguage));
            }

        }
        return answer;
    }

    /** TODO: Replace it, after Maven is kicked :) */
    private List<Metadata> getAllVisibleMetadataHack(DocStruct inStruct) {

        // Start with the list of all metadata.
        List<Metadata> result = new LinkedList<>();

        // Iterate over all metadata.
        if (inStruct.getAllMetadata() != null) {
            for (Metadata md : inStruct.getAllMetadata()) {
                // If the metadata has some value and it does not start with the
                // HIDDEN_METADATA_CHAR, add it to the result list.
                if (!md.getType().getName().startsWith("_")) {
                    result.add(md);
                }
            }
        }
        if (result.isEmpty()) {
            result = null;
        }
        return result;
    }

    /**
     * prüfen, ob es sich hier um eine rdf- oder um eine mets-Datei handelt.
     */
    public static String getMetaFileType(String file) throws IOException {
        /*
         * --------------------- Typen und Suchbegriffe festlegen -------------------
         */
        HashMap<String, String> types = new HashMap<>();
        types.put("metsmods", "ugh.fileformats.mets.MetsModsImportExport".toLowerCase());
        types.put("Mets", "www.loc.gov/METS/".toLowerCase());
        types.put("Rdf", "<RDF:RDF ".toLowerCase());
        types.put("XStream", "<ugh.dl.DigitalDocument>".toLowerCase());
        types.put("Lido", "lido:lido");

        try (FileReader input = new FileReader(file);
                BufferedReader bufRead = new BufferedReader(input)) {
            char[] buffer = new char[200];
            while ((bufRead.read(buffer)) >= 0) {

                String temp = new String(buffer).toLowerCase();
                for (Entry<String, String> entry : types.entrySet()) {
                    if (temp.contains(entry.getValue())) {
                        return entry.getKey();
                    }
                }
            }
        }

        return "-";
    }

    /**
     * @param inMdt
     * @return localized Title of metadata type ================================================================
     */
    public String getMetadatatypeLanguage(MetadataType inMdt) {
        String label = inMdt.getLanguage(Helper.getMetadataLanguage());
        if (label == null) {
            label = inMdt.getName();
        }
        return label;
    }

    public String getMetadataGroupTypeLanguage(MetadataGroupType inMdt) {
        String label = inMdt.getLanguage(Helper.getMetadataLanguage());
        if (label == null) {
            label = inMdt.getName();
        }
        return label;
    }

    /**
     * Comparator für die Metadaten.
     */
    // TODO: Uses generics, if possible
    public static class MetadataComparator implements Comparator<Object> {
        private String language = "de";

        public MetadataComparator(String inLanguage) {
            this.language = inLanguage;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        @Override
        public int compare(Object o1, Object o2) {
            Metadata s1 = (Metadata) o1;
            Metadata s2 = (Metadata) o2;
            if (s1 == null) {
                return -1;
            }
            if (s2 == null) {
                return 1;
            }
            String name1 = "";
            String name2 = "";
            try {
                MetadataType mdt1 = s1.getType();
                MetadataType mdt2 = s2.getType();
                name1 = mdt1.getNameByLanguage(this.language);
                name2 = mdt2.getNameByLanguage(this.language);
            } catch (java.lang.NullPointerException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Language " + language + " for metadata " + s1.getType() + " or " + s2.getType() + " is missing in ruleset");
                }
                return 0;
            }
            if (name1 == null || name1.length() == 0) {
                name1 = s1.getType().getName();
                if (name1 == null) {
                    return -1;
                }
            }
            if (name2 == null || name2.length() == 0) {
                name2 = s2.getType().getName();
                if (name2 == null) {
                    return 1;
                }
            }

            return name1.compareToIgnoreCase(name2);
        }
    }

    /**
     * Alle Rollen ermitteln, die für das übergebene Strukturelement erlaubt sind.
     * 
     * @param myDocStruct Strukturtyp
     * @param inRoleName Rollenname der aktuellen Person, damit diese ggf. in die Liste mit übernommen wird
     * @return role list ================================================ ================
     */
    public List<SelectItem> getAddablePersonRoles(HoldingElement myDocStruct, String inRoleName) {
        ArrayList<SelectItem> myList = new ArrayList<>();
        /*
         * -------------------------------- zuerst mal alle addierbaren Metadatentypen ermitteln --------------------------------
         */
        List<MetadataType> types = myDocStruct.getAddableMetadataTypes(false);
        if (types == null) {
            types = new ArrayList<>();
        }
        if (inRoleName != null && inRoleName.length() > 0) {
            boolean addRole = true;
            for (MetadataType mdt : types) {
                if (mdt.getName().equals(inRoleName)) {
                    addRole = false;
                }
            }

            if (addRole) {
                types.add(this.myPrefs.getMetadataTypeByName(inRoleName));
            }
        }
        /*
         * --------------------- alle Metadatentypen, die keine Person sind, oder mit einem Unterstrich anfangen rausnehmen -------------------
         */
        for (MetadataType mdt : new ArrayList<>(types)) {
            if (!mdt.getIsPerson()) {
                types.remove(mdt);
            }
        }

        /*
         * -------------------------------- die Metadatentypen sortieren --------------------------------
         */
        HelperComparator c = new HelperComparator();
        c.setSortierart("MetadatenTypen");
        Collections.sort(types, c);

        for (MetadataType mdt : types) {
            myList.add(new SelectItem(mdt.getName(), getMetadatatypeLanguage(mdt)));
        }
        return myList;
    }

    public List<SelectItem> getAddableCorporateRoles(HoldingElement myDocStruct, String inRoleName) {
        List<SelectItem> myList = new ArrayList<>();

        List<MetadataType> types = myDocStruct.getAddableMetadataTypes(false);
        if (types == null) {
            types = new ArrayList<>();
        }
        if (inRoleName != null && inRoleName.length() > 0) {
            boolean addRole = true;
            for (MetadataType mdt : types) {
                if (mdt.getName().equals(inRoleName)) {
                    addRole = false;
                }
            }

            if (addRole) {
                types.add(this.myPrefs.getMetadataTypeByName(inRoleName));
            }
        }
        /*
         * --------------------- alle Metadatentypen, die keine Person sind, oder mit einem Unterstrich anfangen rausnehmen -------------------
         */
        for (MetadataType mdt : new ArrayList<>(types)) {
            if (!mdt.isCorporate()) {
                types.remove(mdt);
            }
        }

        /*
         * -------------------------------- die Metadatentypen sortieren --------------------------------
         */
        HelperComparator c = new HelperComparator();
        c.setSortierart("MetadatenTypen");
        Collections.sort(types, c);

        for (MetadataType mdt : types) {
            myList.add(new SelectItem(mdt.getName(), getMetadatatypeLanguage(mdt)));
        }
        return myList;
    }

    public static Fileformat getFileformatByName(String name, Ruleset ruleset) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addUrls(ClasspathHelper.forPackage("ugh.fileformats"));
        Reflections reflections = new Reflections(builder);

        Set<Class<? extends Fileformat>> formatSet = reflections.getSubTypesOf(Fileformat.class);
        for (Class<? extends Fileformat> cl : formatSet) {
            try {
                Fileformat ff = cl.getDeclaredConstructor().newInstance();
                if (ff.isWritable() && ff.getDisplayName().equals(name)) {
                    ff.setPrefs(ruleset.getPreferences());
                    return ff;
                }
            } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException | NoSuchMethodException
                    | PreferencesException | SecurityException e) {
                log.error(e);
            }

        }
        return null;
    }

    public static ExportFileformat getExportFileformatByName(String name, Ruleset ruleset) {
        Set<Class<? extends ExportFileformat>> formatSet = new Reflections("ugh.fileformats.*").getSubTypesOf(ExportFileformat.class);
        for (Class<? extends ExportFileformat> cl : formatSet) {

            try {
                ExportFileformat ff = cl.getDeclaredConstructor().newInstance();
                if (ff.isExportable() && ff.getDisplayName().equals(name)) {
                    ff.setPrefs(ruleset.getPreferences());
                    return ff;
                }
            } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException | NoSuchMethodException
                    | PreferencesException | SecurityException e) {
                log.error(e);
            }

        }
        return null;
    }

    public static Map<String, List<String>> getMetadataOfFileformat(Fileformat gdzfile, boolean includeAuthority) {

        Map<String, List<String>> metadataList = new HashMap<>();

        try {
            DocStruct ds = gdzfile.getDigitalDocument().getLogicalDocStruct();
            metadataList.put("DocStruct", Collections.singletonList(ds.getType().getName()));
            if (ds.getType().isAnchor() && ds.getAllChildren() != null) {
                DocStruct volume = ds.getAllChildren().get(0);
                getMetadataFromDocstruct(includeAuthority, metadataList, volume);
            }
            getMetadataFromDocstruct(includeAuthority, metadataList, ds);

            ds = gdzfile.getDigitalDocument().getPhysicalDocStruct();
            getMetadataFromDocstruct(includeAuthority, metadataList, ds);
        } catch (PreferencesException e) {
            log.error(e);
        }
        return metadataList;
    }

    public static Optional<Metadata> getSingleMetadata(DocStruct docStruct, String metadataType) {
        return docStruct.getAllMetadata()
                .stream()
                .filter(md -> md.getType().getName().equalsIgnoreCase(metadataType))
                .findAny();
    }

    public static Optional<String> getSingleMetadataValue(DocStruct docStruct, String metadataType) {
        return getSingleMetadata(docStruct, metadataType).map(Metadata::getValue);
    }

    private static void getMetadataFromDocstruct(boolean includeAuthority, Map<String, List<String>> metadataList, DocStruct ds) {
        if (ds.getAllMetadataGroups() != null) {
            for (MetadataGroup mg : ds.getAllMetadataGroups()) {
                if (mg.getPersonList() != null) {
                    for (Person p : mg.getPersonList()) {
                        if (includeAuthority) {
                            addAuthorityFromPerson(metadataList, p);
                        }
                        if (StringUtils.isNotBlank(p.getFirstname()) || StringUtils.isNotBlank(p.getLastname())) {
                            if (metadataList.containsKey(p.getType().getName())) {
                                List<String> oldValue = metadataList.get(p.getType().getName());
                                oldValue.add(p.getFirstname() + " " + p.getLastname());
                                metadataList.put(p.getType().getName(), oldValue);
                            } else {
                                List<String> list = new ArrayList<>();
                                list.add(p.getFirstname() + " " + p.getLastname());
                                metadataList.put(p.getType().getName(), list);
                            }
                        }
                    }
                }
                if (mg.getMetadataList() != null) {
                    for (Metadata md : mg.getMetadataList()) {
                        if (includeAuthority) {
                            addAuthorityFromMeta(metadataList, md);
                        }
                        if (StringUtils.isNotBlank(md.getValue())) {
                            if (metadataList.containsKey(md.getType().getName())) {
                                List<String> oldValue = metadataList.get(md.getType().getName());
                                oldValue.add(md.getValue());
                                metadataList.put(md.getType().getName(), oldValue);
                            } else {
                                List<String> list = new ArrayList<>();
                                list.add(md.getValue());
                                metadataList.put(md.getType().getName(), list);
                            }
                        }
                    }
                }
                if (mg.getCorporateList() != null) {
                    for (Corporate c : mg.getCorporateList()) {
                        if (includeAuthority) {
                            addAuthorityFromMeta(metadataList, c);
                        }
                        StringBuilder corporate = new StringBuilder();
                        if (StringUtils.isNotBlank(c.getMainName())) {
                            corporate.append(c.getMainName());
                            corporate.append(" ");
                        }
                        for (NamePart namePart : c.getSubNames()) {
                            if (StringUtils.isNotBlank(namePart.getValue())) {
                                corporate.append(namePart.getValue());
                                corporate.append(" ");
                            }
                        }
                        if (StringUtils.isNotBlank(c.getPartName())) {
                            corporate.append(c.getPartName());
                        }
                        String val = corporate.toString().trim();
                        if (StringUtils.isNotBlank(val)) {
                            if (metadataList.containsKey(c.getType().getName())) {
                                List<String> oldValue = metadataList.get(c.getType().getName());
                                oldValue.add(val);
                                metadataList.put(c.getType().getName(), oldValue);
                            } else {
                                List<String> list = new ArrayList<>();
                                list.add(val);
                                metadataList.put(c.getType().getName(), list);
                            }
                        }
                    }
                }
            }
        }

        if (ds.getAllCorporates() != null) {
            for (Corporate c : ds.getAllCorporates()) {
                if (includeAuthority) {
                    addAuthorityFromMeta(metadataList, c);
                }
                StringBuilder corporate = new StringBuilder();
                if (StringUtils.isNotBlank(c.getMainName())) {
                    corporate.append(c.getMainName());
                    corporate.append(" ");
                }
                for (NamePart namePart : c.getSubNames()) {
                    if (StringUtils.isNotBlank(namePart.getValue())) {

                        corporate.append(namePart.getValue());
                        corporate.append(" ");

                    }
                }
                if (StringUtils.isNotBlank(c.getPartName())) {
                    corporate.append(c.getPartName());
                }
                String val = corporate.toString().trim();
                if (StringUtils.isNotBlank(val)) {

                    if (metadataList.containsKey(c.getType().getName())) {
                        List<String> oldValue = metadataList.get(c.getType().getName());
                        oldValue.add(val);
                        metadataList.put(c.getType().getName(), oldValue);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(val);
                        metadataList.put(c.getType().getName(), list);
                    }
                }
            }
        }

        if (ds.getAllMetadata() != null) {
            for (Metadata md : ds.getAllMetadata()) {
                if (includeAuthority) {
                    addAuthorityFromMeta(metadataList, md);
                }
                if (StringUtils.isNotBlank(md.getValue())) {
                    if (metadataList.containsKey(md.getType().getName())) {
                        List<String> oldValue = metadataList.get(md.getType().getName());
                        oldValue.add(md.getValue());
                        metadataList.put(md.getType().getName(), oldValue);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(md.getValue());
                        metadataList.put(md.getType().getName(), list);
                    }
                }
            }
        }
        if (ds.getAllPersons() != null) {
            for (Person p : ds.getAllPersons()) {
                if (includeAuthority) {
                    addAuthorityFromPerson(metadataList, p);
                }
                if (StringUtils.isNotBlank(p.getFirstname()) || StringUtils.isNotBlank(p.getLastname())) {
                    if (metadataList.containsKey(p.getType().getName())) {
                        List<String> oldValue = metadataList.get(p.getType().getName());
                        oldValue.add(p.getFirstname() + " " + p.getLastname());
                        metadataList.put(p.getType().getName(), oldValue);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(p.getFirstname() + " " + p.getLastname());
                        metadataList.put(p.getType().getName(), list);
                    }
                }
            }
        }
    }

    private static void addAuthorityFromPerson(Map<String, List<String>> metadataList, Person p) {

        if (StringUtils.isNotBlank(p.getAuthorityID())) {

            String key = p.getType().getName() + "_authority";
            List<String> value = metadataList.get(key);
            if (value == null) {
                value = new ArrayList<>();
                metadataList.put(key, value);
            }
            value.add(p.getAuthorityID());
        }
    }

    private static void addAuthorityFromMeta(Map<String, List<String>> metadataList, Metadata md) {

        if (StringUtils.isNotBlank(md.getAuthorityID())) {

            String key = md.getType().getName() + "_authority";
            List<String> value = metadataList.get(key);
            if (value == null) {
                value = new ArrayList<>();
                metadataList.put(key, value);
            }
            value.add(md.getAuthorityID());
        }
    }

}
