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
import lombok.Getter;
import lombok.Setter;
import ugh.dl.Corporate;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
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
    private Prefs myPrefs;
    @Getter
    @Setter
    private Process myProcess;
    @Getter
    @Setter
    private MetadataGroup metadataGroup;

    public MetadataGroupImpl(Prefs prefs, Process process, MetadataGroup metadataGroup, Metadaten bean) {
        this.myPrefs = prefs;
        this.myProcess = process;
        this.metadataGroup = metadataGroup;
        int counter = 0;
        for (Metadata md : metadataGroup.getMetadataList()) {
            MetadatumImpl mdum = new MetadatumImpl(md, counter++, myPrefs, myProcess, bean);
            metadataList.add(mdum);
        }
        for (Person p : metadataGroup.getPersonList()) {
            MetaPerson mp = new MetaPerson(p, counter++, myPrefs, metadataGroup.getDocStruct(), myProcess, bean);
            personList.add(mp);
        }

        for (Corporate corporate : metadataGroup.getCorporateList()) {
            MetaCorporate mc = new MetaCorporate(corporate, myPrefs, metadataGroup.getDocStruct(), myProcess, bean);
            corporateList.add(mc);
        }
    }

    public String getName() {
        String label = this.metadataGroup.getType().getLanguage(Helper.getMetadataLanguage());
        if (label == null) {
            label = this.metadataGroup.getType().getName();
        }
        return label;
    }

}
