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
        } else if (value.startsWith("prozesse.")) {
            return "prozesse";
        } else if (value.startsWith("projekte.")) {
            return "projekte";
        } else if (value.startsWith("prozesseeigenschaften.")) {
            return "prozesseeigenschaften" + order;
        } else if (value.startsWith("vorlageneigenschaften.")) {
            return "vorlageneigenschaften" + order;
        } else if (value.startsWith("werkstueckeeigenschaften.")) {
            return "werkstueckeeigenschaften" + order;
        } else if (value.startsWith("metadata.")) {
            return "metadata" + order;
        } else if (value.startsWith("log.")) {
            return "log";
        }
        return "";
    }

    public String getTableType() {
        if (value == null || value.isEmpty() || value.startsWith("prozesse.")) {
            return "";
        } else if (value.startsWith("projekte.")) {
            return "projekte ";
        } else if (value.startsWith("prozesseeigenschaften.")) {
            return "prozesseeigenschaften ";
        } else if (value.startsWith("vorlageneigenschaften.")) {
            return "vorlageneigenschaften ";
        } else if (value.startsWith("werkstueckeeigenschaften.")) {
            return "werkstueckeeigenschaften ";
        } else if (value.startsWith("metadata.")) {
            return "metadata ";
        } else if (value.startsWith("log.")) {
            return "processlog ";
        }
        return "";
    }

    public String getColumnName() {
        if (value == null || value.isEmpty() || !value.contains(".")) {
            return "";
        } else if (value.startsWith("prozesse.") || value.startsWith("projekte.")) {
            return value.substring(value.indexOf(".") + 1);
        } else if (value.startsWith("metadata.")) {
            return "print";
        } else if (value.startsWith("log.")) {
            return "content";
        } else {
            return "Wert";
        }
    }

    public String getJoinClause() {

        if (getTableName().isEmpty()) {
            return "";
        }
        if (value.startsWith("prozesseeigenschaften.")) {
            return " prozesseeigenschaften " + getTableName() + " ON prozesse.ProzesseID = " + getTableName() + ".prozesseID AND " + getTableName()
                    + ".Titel = \"" + value.substring(value.indexOf(".") + 1) + "\"";

        } else if (value.startsWith("metadata.")) {
            return " metadata " + getTableName() + " ON prozesse.ProzesseID = " + getTableName() + ".processid AND " + getTableName() + ".name = \""
                    + value.substring(value.indexOf(".") + 1) + "\"";

        } else if (value.startsWith("projekte.")) {
            return " projekte " + getTableName() + " ON prozesse.ProjekteID = " + getTableName() + ".ProjekteID";

        } else if (value.startsWith("vorlageneigenschaften.")) {
            return "vorlagen vorlagen" + order + " ON prozesse.ProzesseID = vorlagen" + order + ".ProzesseID LEFT JOIN vorlageneigenschaften "
                    + getTableName() + " ON " + getTableName() + ".vorlagenID = vorlagen" + order + ".vorlagenID AND " + getTableName() + ".Titel =\""
                    + value.substring(value.indexOf(".") + 1) + "\"";

        } else if (value.startsWith("werkstueckeeigenschaften.")) {
            return "werkstuecke werkstuecke" + order + " ON prozesse.ProzesseID = werkstuecke" + order
                    + ".ProzesseID LEFT JOIN werkstueckeeigenschaften " + getTableName() + " ON " + getTableName() + ".werkstueckeID = werkstuecke"
                    + order + ".WerkstueckeID AND " + getTableName() + ".Titel =\"" + value.substring(value.indexOf(".") + 1) + "\"";
        }

        return "";
    }

    public String getAdditionalTable() {
        if (value.startsWith("werkstueckeeigenschaften.")) {
            return " werkstuecke werkstuecke" + order;
        } else if (value.startsWith("vorlageneigenschaften.")) {
            return " vorlagen vorlagen" + order;
        }
        return "";
    }
}
