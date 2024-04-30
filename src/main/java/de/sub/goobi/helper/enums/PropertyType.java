package de.sub.goobi.helper.enums;

import lombok.Getter;

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

/**
 * This enum contains property types, which can be used for display and validation purpose Validation can be done by engaging validation classes,
 * which could be returned by the validation type Enum, contained in here
 * 
 * 
 * @author Wulf
 *
 */
public enum PropertyType {

    UNKNOWN(0, "unknown", false),

    /** general type */
    GENERAL(1, "general", false),

    /** normal message */
    MESSAGE_NORMAL(2, "messageNormal", false),

    /** important message */
    MESSAGE_IMPORTANT(3, "messageImportant", false),

    /** error message */
    MESSAGE_ERROR(4, "messageError", false),
    STRING(5, "String", true),
    BOOLEAN(6, "Boolean", true),
    LIST(7, "List", true),
    NUMBER(8, "Number", true),
    CONTAINER(9, "Container", true),
    DATE(10, "Date", true),
    INTEGER(11, "Integer", true),
    SPECIAL_VIEW(12, "SpecialView", false),
    TEXTAREA(13, "Textarea", true),
    LIST_MULTI_SELECT(14, "ListMultiSelect", true),
    WIKIFIELD(15, "WikiField", false),
    // special Properties
    HIDDEN(16, "Hidden", false),
    ERROR_MESSAGE(17, "ErrorMessage", true),
    COMMAND_LINK(18, "CommandLink", true),
    NO_EDIT(19, "NoEdit", true),
    FILTER(20, "Filter", false),

    YAML(21, "yaml", true),
    JSON(22, "json", true),
    XML(23, "xml", true);

    @Getter
    private int id;
    private String name;

    @Getter
    private Boolean showInDisplay;

    private PropertyType(int id, String inName, Boolean showInDisplay) {
        this.id = id;
        this.name = inName;

        this.showInDisplay = showInDisplay;
    }

    public String getName() {
        return this.name.toLowerCase();
    }

    @Override
    public java.lang.String toString() {
        return this.name().toLowerCase();
    }

    public static PropertyType getByName(String inName) {
        for (PropertyType p : PropertyType.values()) {
            if (p.getName().equals(inName.toLowerCase())) {
                return p;
            }
        }
        return STRING;
    }

    public static PropertyType getById(int id) {
        for (PropertyType p : PropertyType.values()) {
            if (p.getId() == (id)) {
                return p;
            }
        }
        return STRING;
    }

}
