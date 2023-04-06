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

    private static final String TABLE_PROJECTS = "projekte.";
    private static final String TABLE_PROCESSES = "prozesse.";
    private static final String TABLE_PROCESS_PROPERTIES = "prozesseeigenschaften.";
    private static final String TABLE_TEMPLATE_PROPERTIES = "vorlageneigenschaften.";
    private static final String TABLE_WORKPIECE_PROPERTIES = "werkstueckeeigenschaften.";
    private static final String TABLE_METADATA = "metadata.";
    private static final String TABLE_LOG = "log.";

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
            return "prozesseeigenschaften" + order;
        } else if (value.startsWith(TABLE_TEMPLATE_PROPERTIES)) {
            return "vorlageneigenschaften" + order;
        } else if (value.startsWith(TABLE_WORKPIECE_PROPERTIES)) {
            return "werkstueckeeigenschaften" + order;
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
            return "prozesseeigenschaften ";
        } else if (value.startsWith(TABLE_TEMPLATE_PROPERTIES)) {
            return "vorlageneigenschaften ";
        } else if (value.startsWith(TABLE_WORKPIECE_PROPERTIES)) {
            return "werkstueckeeigenschaften ";
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
        } else {
            return "Wert";
        }
    }

    public String getJoinClause() {

        if (getTableName().isEmpty()) {
            return "";
        }
        if (value.startsWith(TABLE_PROCESS_PROPERTIES)) {
            return " prozesseeigenschaften " + getTableName() + " ON prozesse.ProzesseID = " + getTableName() + ".prozesseID AND " + getTableName()
                    + ".Titel = \"" + value.substring(value.indexOf(".") + 1) + "\"";

        } else if (value.startsWith(TABLE_METADATA)) {
            return " metadata " + getTableName() + " ON prozesse.ProzesseID = " + getTableName() + ".processid AND " + getTableName() + ".name = \""
                    + value.substring(value.indexOf(".") + 1) + "\"";

        } else if (value.startsWith(TABLE_PROJECTS)) {
            return " projekte " + getTableName() + " ON prozesse.ProjekteID = " + getTableName() + ".ProjekteID";

        } else if (value.startsWith(TABLE_TEMPLATE_PROPERTIES)) {
            return "vorlagen vorlagen" + order + " ON prozesse.ProzesseID = vorlagen" + order + ".ProzesseID LEFT JOIN vorlageneigenschaften "
                    + getTableName() + " ON " + getTableName() + ".vorlagenID = vorlagen" + order + ".vorlagenID AND " + getTableName() + ".Titel =\""
                    + value.substring(value.indexOf(".") + 1) + "\"";

        } else if (value.startsWith(TABLE_WORKPIECE_PROPERTIES)) {
            return "werkstuecke werkstuecke" + order + " ON prozesse.ProzesseID = werkstuecke" + order
                    + ".ProzesseID LEFT JOIN werkstueckeeigenschaften " + getTableName() + " ON " + getTableName() + ".werkstueckeID = werkstuecke"
                    + order + ".WerkstueckeID AND " + getTableName() + ".Titel =\"" + value.substring(value.indexOf(".") + 1) + "\"";
        }

        return "";
    }

    public String getAdditionalTable() {
        if (value.startsWith(TABLE_WORKPIECE_PROPERTIES)) {
            return " werkstuecke werkstuecke" + order;
        } else if (value.startsWith(TABLE_TEMPLATE_PROPERTIES)) {
            return " vorlagen vorlagen" + order;
        }
        return "";
    }
}
