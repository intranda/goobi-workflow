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
import java.util.LinkedList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.goobi.beans.Process;

import de.sub.goobi.helper.Helper;
import lombok.Getter;
import lombok.Setter;
import ugh.dl.Corporate;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;

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
    private String metadataGroupId;

    @Getter
    private String parentGroupId;

    @Getter
    private int level;

    public MetadataGroupImpl(Prefs prefs, Process process, MetadataGroup metadataGroup, Metadaten bean, String metadataGroupId, String parentGroupId,
            int level) {
        this.myPrefs = prefs;
        this.myProcess = process;
        this.metadataGroup = metadataGroup;
        int counter = 0;
        this.metadataGroupId = metadataGroupId;
        this.parentGroupId = parentGroupId;
        this.level = level;
        metadataGroup.checkDefaultDisplayMetadata();

        for (Metadata md : metadataGroup.getMetadataList()) {
            MetadatumImpl mdum = new MetadatumImpl(md, counter++, myPrefs, myProcess, bean);
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
            MetadataGroupImpl mgi = new MetadataGroupImpl(myPrefs, process, mg, bean, metadataGroupId + "-" + counter++, metadataGroupId, level + 1);
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
    }

    private String getMetadatatypeLanguage(MetadataType inMdt) {
        String label = inMdt.getLanguage(Helper.getMetadataLanguage());
        if (label == null) {
            label = inMdt.getName();
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
        return metadataGroup.getAddableMetadataGroupTypes() != null;
    }

    public boolean isHasGroups() {
        return !groupList.isEmpty();
    }

    public List<MetadataGroupImpl> getAsFlatList() {
        List<MetadataGroupImpl> list = new LinkedList<>();
        list.add(this);
        for (MetadataGroupImpl child : groupList) {
            list.addAll(child.getAsFlatList());
        }

        return list;
    }

}
