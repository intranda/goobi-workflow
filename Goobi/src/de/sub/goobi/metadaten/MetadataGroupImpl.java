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
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.Person;
import ugh.dl.Prefs;

public class MetadataGroupImpl {

    private List<MetadatumImpl> metadataList = new ArrayList<>();
    private List<MetaPerson> personList = new ArrayList<>();
    private Prefs prefs;
    private Process process;
    private MetadataGroup metadataGroup;

    public MetadataGroupImpl(Prefs myPrefs, Process myProcess, MetadataGroup metadataGroup, Metadaten bean) {
        this.prefs = myPrefs;
        this.process = myProcess;
        this.metadataGroup = metadataGroup;
        int counter = 0;
        for (Metadata md : metadataGroup.getMetadataList()) {
            MetadatumImpl mdum = new MetadatumImpl(md, counter++, this.prefs, this.process, bean);
            metadataList.add(mdum);
        }
        for (Person p : metadataGroup.getPersonList()) {
            MetaPerson mp = new MetaPerson(p, counter++, this.prefs, metadataGroup.getDocStruct(), myProcess, bean);
            personList.add(mp);
        }
        // TODO corporates
    }

    public List<MetaPerson> getPersonList() {
        return personList;
    }

    public void setPersonList(List<MetaPerson> personList) {
        this.personList = personList;
    }

    public List<MetadatumImpl> getMetadataList() {
        return metadataList;
    }

    public void setMetadataList(List<MetadatumImpl> metadataList) {
        this.metadataList = metadataList;
    }

    public Prefs getMyPrefs() {
        return prefs;
    }

    public void setMyPrefs(Prefs myPrefs) {
        this.prefs = myPrefs;
    }

    public Process getMyProcess() {
        return process;
    }

    public void setMyProcess(Process myProcess) {
        this.process = myProcess;
    }

    public MetadataGroup getMetadataGroup() {
        return metadataGroup;
    }

    public void setMetadataGroup(MetadataGroup metadataGroup) {
        this.metadataGroup = metadataGroup;
    }

    public String getName() {
        String label = this.metadataGroup.getType().getLanguage(Helper.getMetadataLanguage());
        if (label == null) {
            label = this.metadataGroup.getType().getName();
        }
        return label;
    }

}
