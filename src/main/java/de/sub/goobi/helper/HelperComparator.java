package de.sub.goobi.helper;

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
import java.io.Serializable;
import java.util.Comparator;

import lombok.Setter;
import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;

public class HelperComparator implements Comparator<Object>, Serializable {

    private static final long serialVersionUID = -1124724462982810327L;
    @Setter
    private String sortierart;

    @Override
    public int compare(Object o1, Object o2) {

        int rueckgabe = 0;
        if (this.sortierart.equals("MetadatenTypen")) {
            rueckgabe = compareMetadatenTypen(o1, o2);
        }
        if (this.sortierart.equals("Metadata")) {
            rueckgabe = compareMetadata(o1, o2);
        }
        if (this.sortierart.equals("DocStructTypen")) {
            rueckgabe = compareDocStructTypen(o1, o2);
        }
        if (this.sortierart.equals("MetadatenGroupTypes")) {
            rueckgabe = compareMetadataGroupTypes(o1, o2);
        }

        return rueckgabe;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    private int compareMetadatenTypen(Object o1, Object o2) {
        MetadataType s1 = (MetadataType) o1;
        MetadataType s2 = (MetadataType) o2;
        String name1 = s1.getLanguage(Helper.getMetadataLanguage());
        String name2 = s2.getLanguage(Helper.getMetadataLanguage());
        if (name1 == null) {
            name1 = "";
        }
        if (name2 == null) {
            name2 = "";
        }
        return name1.compareToIgnoreCase(name2);
    }

    private int compareMetadata(Object o1, Object o2) {
        Metadata s1 = (Metadata) o1;
        Metadata s2 = (Metadata) o2;
        String name1 = s1.getType().getNameByLanguage(Helper.getMetadataLanguage());
        String name2 = s2.getType().getNameByLanguage(Helper.getMetadataLanguage());
        if (name1 == null) {
            name1 = s1.getType().getName();
        }
        if (name2 == null) {
            name2 = s2.getType().getName();
        }
        return name1.compareToIgnoreCase(name2);
    }

    private int compareDocStructTypen(Object o1, Object o2) {
        DocStructType s1 = (DocStructType) o1;
        DocStructType s2 = (DocStructType) o2;
        String name1 = s1.getNameByLanguage(Helper.getMetadataLanguage());
        String name2 = s2.getNameByLanguage(Helper.getMetadataLanguage());
        if (name1 == null) {
            name1 = "";
        }
        if (name2 == null) {
            name2 = "";
        }
        return name1.compareToIgnoreCase(name2);
    }

    private int compareMetadataGroupTypes(Object o1, Object o2) {
        MetadataGroupType s1 = (MetadataGroupType) o1;
        MetadataGroupType s2 = (MetadataGroupType) o2;
        String name1 = s1.getLanguage(Helper.getMetadataLanguage());
        String name2 = s2.getLanguage(Helper.getMetadataLanguage());
        if (name1 == null) {
            name1 = "";
        }
        if (name2 == null) {
            name2 = "";
        }
        return name1.compareToIgnoreCase(name2);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sortierart == null) ? 0 : sortierart.hashCode());
        return result;
    }
}
