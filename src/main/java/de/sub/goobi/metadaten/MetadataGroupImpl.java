package de.sub.goobi.metadaten;

/***************************************************************
 * Copyright notice
 *
 * (c) 2013 Robert Sehr <robert.sehr@intranda.com>
 *
 * All rights reserved
 *
 * This file is part of the Goobi project. The Goobi project is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * The GNU General Public License can be found at
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * This script is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * This copyright notice MUST APPEAR in all copies of this file!
 ***************************************************************/

import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.Process;

import de.sub.goobi.helper.Helper;
import jakarta.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.AllowedMetadataGroupType;
import ugh.dl.Corporate;
import ugh.dl.DocStruct;
import ugh.dl.HoldingElement;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;

@Log4j2
public class MetadataGroupImpl {
    @Getter
    @Setter
    private List<MetadatumImpl> metadataList = new ArrayList<>();
    @Getter
    @Setter
    private List<MetaPerson> personList = new ArrayList<>();
    @Getter
    @Setter
    private List<MetaCorporate> corporateList = new ArrayList<>();
    @Getter
    @Setter
    private List<MetadataGroupImpl> groupList = new ArrayList<>();

    @Getter
    @Setter
    private Prefs myPrefs;
    @Getter
    @Setter
    private Process myProcess;
    @Getter
    @Setter
    private MetadataGroup metadataGroup;

    @Getter
    private List<SelectItem> addableMetadata = new ArrayList<>();
    @Getter
    private List<SelectItem> addableCorporations = new ArrayList<>();
    @Getter
    private List<SelectItem> addablePersons = new ArrayList<>();
    @Getter
    private List<SelectItem> addableGroupTypes = new ArrayList<>();

    @Getter
    private String metadataGroupId;

    @Getter
    private String parentGroupId;

    @Getter
    private int level;

    public MetadataGroupImpl(Prefs prefs, Process process, MetadataGroup metadataGroup, Metadaten bean,
            MetadataGroupIdRegistry idRegistry, String parentGroupId, int level) {
        this.myPrefs = prefs;
        this.myProcess = process;
        this.metadataGroup = metadataGroup;
        int counter = 0;
        this.metadataGroupId = idRegistry == null ? null : idRegistry.idFor(metadataGroup);
        this.parentGroupId = parentGroupId;
        this.level = level;
        metadataGroup.checkDefaultDisplayMetadata();
        addMandatoryMetadata(metadataGroup);

        for (Metadata md : metadataGroup.getMetadataList()) {
            MetadatumImpl mdum = new MetadatumImpl(md, counter++, myPrefs, myProcess, bean);
            mdum.setValidationErrorPresent(md.isValidationErrorPresent());
            mdum.setValidationMessage(md.getValidationMessage());
            metadataList.add(mdum);
        }
        for (Person p : metadataGroup.getPersonList()) {
            MetaPerson mp = new MetaPerson(p, counter++, myPrefs, metadataGroup.getParent(), myProcess, bean);
            personList.add(mp);
        }

        for (Corporate corporate : metadataGroup.getCorporateList()) {
            MetaCorporate mc = new MetaCorporate(corporate, myPrefs, metadataGroup.getParent(), bean);
            corporateList.add(mc);
        }
        for (MetadataGroup mg : metadataGroup.getAllMetadataGroups()) {
            MetadataGroupImpl mgi = new MetadataGroupImpl(myPrefs, process, mg, bean, idRegistry, metadataGroupId, level + 1);
            groupList.add(mgi);
        }
        // get addable metadata, person, corporates and sub groups
        List<MetadataType> allAddableTypes = metadataGroup.getAddableMetadataTypes(false);
        if (allAddableTypes != null) {
            for (MetadataType t : allAddableTypes) {
                SelectItem si = new SelectItem(t.getName(), getMetadatatypeLanguage(t));

                if (t.isCorporate()) {
                    addableCorporations.add(si);
                } else if (t.getIsPerson()) {
                    addablePersons.add(si);
                } else {
                    addableMetadata.add(si);
                }

            }
        }
        List<String> allAddableGroupTypeNames = metadataGroup.getAddableMetadataGroupTypes();
        if (allAddableGroupTypeNames != null) {
            for (String typeName : allAddableGroupTypeNames) {
                MetadataGroupType mgt = prefs.getMetadataGroupTypeByName(typeName);
                addableGroupTypes.add(new SelectItem(typeName, getMetadataGroupTypeLanguage(mgt)));
            }
        }
    }

    /**
     * Creates an empty instance for every metadata/person/corporate type of this group whose cardinality in the ruleset is mandatory ("1m" or "+")
     * and that isn't already present, so mandatory fields show up in the editor without requiring the user to add them manually first.
     */
    private void addMandatoryMetadata(MetadataGroup metadataGroup) {
        MetadataGroupType type = metadataGroup.getType();
        List<MetadataType> allTypes = type.getMetadataTypeList();
        if (allTypes == null) {
            return;
        }
        for (MetadataType mdt : allTypes) {
            String num = type.getNumberOfMetadataType(mdt);
            if (!("1m".equals(num) || "+".equals(num))) {
                continue;
            }
            if (mdt.getName().startsWith("_") || metadataGroup.countMDofthisType(mdt.getName()) > 0) {
                continue;
            }
            try {
                if (mdt.getIsPerson()) {
                    Person p = new Person(mdt);
                    p.setRole(mdt.getName());
                    metadataGroup.addPerson(p);
                } else if (mdt.isCorporate()) {
                    Corporate c = new Corporate(mdt);
                    c.setRole(mdt.getName());
                    metadataGroup.addCorporate(c);
                } else {
                    metadataGroup.addMetadata(new Metadata(mdt));
                }
            } catch (MetadataTypeNotAllowedException e) {
                log.error("Error creating mandatory field '{}' in group '{}'", mdt.getName(), type.getName(), e);
            }
        }
    }

    private String getMetadatatypeLanguage(MetadataType inMdt) {
        String label = inMdt.getLanguage(Helper.getMetadataLanguage());
        if (label == null) {
            label = inMdt.getName();
        }
        return label;
    }

    private String getMetadataGroupTypeLanguage(MetadataGroupType inMgt) {
        String label = inMgt.getLanguage(Helper.getMetadataLanguage());
        if (label == null) {
            label = inMgt.getName();
        }
        return label;
    }

    public String getName() {
        String label = this.metadataGroup.getType().getLanguage(Helper.getMetadataLanguage());
        if (label == null) {
            label = this.metadataGroup.getType().getName();
        }
        return label;
    }

    public boolean isMetadataAddable() {
        return !addableMetadata.isEmpty();
    }

    public boolean isCorporateAddable() {
        return !addableCorporations.isEmpty();
    }

    public boolean isPersonAddable() {
        return !addablePersons.isEmpty();
    }

    public boolean isGroupAddable() {
        return !addableGroupTypes.isEmpty();
    }

    public boolean isHasGroups() {
        return !groupList.isEmpty();
    }

    /**
     * Whether this group instance may be duplicated, based on the maximum cardinality configured for its type in the ruleset ("1m"/"1o" allow only
     * one instance, which already exists as this group).
     */
    public boolean isGroupDuplicatable() {
        String maxNumberAllowed = getMaxNumberOfSiblingGroupsAllowed();
        return "*".equals(maxNumberAllowed) || "+".equals(maxNumberAllowed);
    }

    /**
     * Whether this group instance may be deleted, based on the minimum cardinality configured for its type in the ruleset ("1m"/"+" require at
     * least one instance to remain).
     */
    public boolean isGroupDeletable() {
        String maxNumberAllowed = getMaxNumberOfSiblingGroupsAllowed();
        if (!("1m".equals(maxNumberAllowed) || "+".equals(maxNumberAllowed))) {
            return true;
        }
        return countSiblingGroupsOfSameType() > 1;
    }

    private String getMaxNumberOfSiblingGroupsAllowed() {
        HoldingElement parent = metadataGroup.getParent();
        MetadataGroupType type = metadataGroup.getType();
        if (parent instanceof DocStruct) {
            return ((DocStruct) parent).getType().getNumberOfMetadataGroups(type);
        }
        if (parent instanceof MetadataGroup) {
            AllowedMetadataGroupType allowedType = ((MetadataGroup) parent).getType().getAllowedMetadataGroupTypeByName(type.getName());
            return allowedType == null ? null : allowedType.getNumAllowed();
        }
        return null;
    }

    private int countSiblingGroupsOfSameType() {
        HoldingElement parent = metadataGroup.getParent();
        String typeName = metadataGroup.getType().getName();
        if (parent instanceof DocStruct) {
            return ((DocStruct) parent).countMDofthisType(typeName);
        }
        if (parent instanceof MetadataGroup) {
            return ((MetadataGroup) parent).countMDofthisType(typeName);
        }
        return 0;
    }

}
