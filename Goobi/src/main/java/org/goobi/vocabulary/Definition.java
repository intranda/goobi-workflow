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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@XmlRootElement
@NoArgsConstructor
public class Definition implements Serializable {

    private static final long serialVersionUID = 4869232901046909915L;

    private Integer id;

    // e.g. 'title'
    @NonNull
    private String label;

    // e.g. 'eng', 'ger'
    @NonNull
    private String language;

    // field type:  "input", "textarea", "select", "select1", "html"
    @NonNull
    private String type;

    // regular expression
    @NonNull
    private String validation;

    // define if the field is required

    private boolean required;

    // define if the field is the main entry

    private boolean mainEntry;

    // define if the field value must be unique within the vocabulary

    private boolean distinctive;

    private boolean titleField;

    // possible values to select
    private List<String> selecteableValues;

    public Definition(String label, String language, String type, String validation, boolean required, boolean mainEntry, boolean distinctive,
            boolean titleField) {
        this.label = label;
        this.language = language;
        this.type = type;
        this.validation = validation;
        this.required = required;
        this.mainEntry = mainEntry;
        this.distinctive = distinctive;
        this.titleField = titleField;

    }

    @JsonIgnore
    public String getSelection() {
        StringBuilder sb = new StringBuilder();
        if (selecteableValues != null && !selecteableValues.isEmpty()) {
            for (String string : selecteableValues) {
                if (sb.length() > 0) {
                    sb.append("|");
                }
                sb.append(string);
            }
        }
        return sb.toString();

    }

    @JsonIgnore
    public void setSelection(String value) {
        if (StringUtils.isNotBlank(value)) {
            selecteableValues = Arrays.asList(value.split("\\|"));
        } else {
            selecteableValues = null;
        }

    }

    @JsonIgnore
    public List<SelectItem> getSelectList() {
        List<SelectItem> list = new ArrayList<>();
        for (String s : selecteableValues) {
            list.add(new SelectItem(s, s, null));
        }
        return list;
    }

    @JsonIgnore
    public String getIdAsString() {
        if (id == null) {
            return "";
        }
        return String.valueOf(id);
    }

}
