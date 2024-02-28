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
package org.goobi.vocabulary;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@XmlRootElement
@NoArgsConstructor
public class Field implements Serializable {

    private static final long serialVersionUID = 4731164844446907463L;

    private Integer id;

    private String label;

    private String language;

    private String value;

    private Integer definitionId;

    private transient Definition definition;

    public Field(String label, String language, String value, Definition definition) {
        this.label = label;
        this.language = language;
        this.value = value;
        this.definition = definition;
    }

    @JsonIgnore
    private transient String validationMessage;

    public void setValue(String value) {
        if (this.definition != null) {
            String type = this.definition.getType();
            if (type.equals("input") || type.equals("textarea") || type.equals("html")) {
                this.value = value.trim();
                return;
            }
        }
        this.value = value;
    }

    /**
     * Simple getter to allow reading the current value as multi select field
     * 
     * @return
     */
    @JsonIgnore
    public String[] getValueMultiSelect() {
        return value.split("\\|");
    }

    /**
     * simple setter to allow writing information as multi select field
     * 
     * @param data
     */
    @JsonIgnore
    public void setValueMultiSelect(String[] data) {
        StringBuilder sb = new StringBuilder();
        if (data != null && data.length > 0) {
            for (int i = 0; i < data.length; i++) {
                if (sb.length() > 0) {
                    sb.append("|");
                }
                sb.append(data[i]);
            }
        }
        value = sb.toString();
    }

    @JsonIgnore
    public String getDisplayLanguageKey() {
        if (StringUtils.isBlank(language)) {
            return "";
        }
        return "vocabularyManager_language_abbreviation_" + language;
    }
}
