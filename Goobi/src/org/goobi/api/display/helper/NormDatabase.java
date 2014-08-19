package org.goobi.api.display.helper;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
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
 */
import java.util.ArrayList;
import java.util.List;

import de.sub.goobi.config.ConfigNormdata;

public class NormDatabase {

    private String path;

    private String abbreviation;

    private static List<NormDatabase> allNormdatabases = new ArrayList<NormDatabase>();
    
    public NormDatabase(String path, String abbreviation) {
        this.path = path;
        this.abbreviation = abbreviation;
    }

    public static List<NormDatabase> getAllDatabases() {
        if (allNormdatabases == null || allNormdatabases.isEmpty()) {
            allNormdatabases = ConfigNormdata.getConfiguredNormdatabases();
        }
        return allNormdatabases;
    }

    public static void setAllNormdatabases(List<NormDatabase> allNormdatabases) {
        NormDatabase.allNormdatabases = allNormdatabases;
    }
    
    public String getAbbreviation() {
        return abbreviation;
    }

    public String getPath() {
        return path;
    }
//
    public static NormDatabase getByAbbreviation(String abbreviation) {
        for (NormDatabase ndb : allNormdatabases) {
            if (ndb.getAbbreviation().equals(abbreviation)) {
                return ndb;
            }
        }
        return getAllDatabases().get(0);
    }

}
