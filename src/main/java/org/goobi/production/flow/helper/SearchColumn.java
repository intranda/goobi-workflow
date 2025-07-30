package org.goobi.production.flow.helper;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - https://goobi.io - https://www.intranda.com - https://github.com/intranda/goobi-workflow
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
public class SearchColumn implements Serializable {

    private static final long serialVersionUID = -3474943392910282588L;

    public static final String TABLE_PROJECTS = "projekte.";
    public static final String TABLE_PROCESSES = "prozesse.";
    public static final String TABLE_PROCESS_PROPERTIES = "property.";
    public static final String TABLE_METADATA = "metadata.";
    public static final String TABLE_LOG = "log.";

    @Getter
    @Setter
    private String value = "";

    @Getter
    private int order;

    public SearchColumn(int order) {
        this.order = order;
    }

    public String getTableName() {
        if (value == null || value.isEmpty()) {
            return "";
        } else if (value.startsWith(TABLE_PROCESSES)) {
            return "prozesse";
        } else if (value.startsWith(TABLE_PROJECTS)) {
            return "projekte";
        } else if (value.startsWith(TABLE_PROCESS_PROPERTIES)) {
            return "properties" + order;
        } else if (value.startsWith(TABLE_METADATA)) {
            return "metadata" + order;
        } else if (value.startsWith(TABLE_LOG)) {
            return "log";
        }
        return "";
    }

    public String getTableType() {
        if (value == null || value.isEmpty() || value.startsWith(TABLE_PROCESSES)) {
            return "";
        } else if (value.startsWith(TABLE_PROJECTS)) {
            return "projekte ";
        } else if (value.startsWith(TABLE_PROCESS_PROPERTIES)) {
            return "properties ";
        } else if (value.startsWith(TABLE_METADATA)) {
            return "metadata ";
        } else if (value.startsWith(TABLE_LOG)) {
            return "journal ";
        }
        return "";
    }

    public String getColumnName() {
        if (value == null || value.isEmpty() || !value.contains(".")) {
            return "";
        } else if (value.startsWith(TABLE_PROCESSES) || value.startsWith(TABLE_PROJECTS)) {
            return value.substring(value.indexOf(".") + 1);
        } else if (value.startsWith(TABLE_METADATA)) {
            return "print";
        } else if (value.startsWith(TABLE_LOG)) {
            return "content";
        } else if (value.startsWith(TABLE_PROCESS_PROPERTIES)) {
            return "property_value";
        } else {
            return "Wert";
        }
    }

    public String getJoinClause() {

        if (getTableName().isEmpty()) {
            return "";
        }
        if (value.startsWith(TABLE_PROCESS_PROPERTIES)) {
            return " properties " + getTableName() + " ON prozesse.ProzesseID = " + getTableName() + ".object_id AND "
                    + getTableName() + ".object_type = 'process' AND " + getTableName() + ".property_name = \"" + value.substring(value.indexOf(".") + 1)
                    + "\"";

        } else if (value.startsWith(TABLE_METADATA)) {
            return " metadata " + getTableName() + " ON prozesse.ProzesseID = " + getTableName() + ".processid AND " + getTableName() + ".name = \""
                    + value.substring(value.indexOf(".") + 1) + "\"";

        } else if (value.startsWith(TABLE_PROJECTS)) {
            return " projekte " + getTableName() + " ON prozesse.ProjekteID = " + getTableName() + ".ProjekteID";

        }

        return "";
    }

}
