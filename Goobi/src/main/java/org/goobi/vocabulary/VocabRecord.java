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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.goobi.beans.DatabaseObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data

@NoArgsConstructor
@XmlRootElement
public class VocabRecord implements DatabaseObject, Comparable<VocabRecord> {

    private static final long serialVersionUID = -1363358845851354446L;

    private Integer id;
    private Integer vocabularyId;
    private List<Field> fields = new ArrayList<>();

    @JsonIgnore
    private boolean valid = true;

    public VocabRecord(Integer id, Integer vocabularyId, List<Field> fields) {
        this.id = id;
        this.vocabularyId = vocabularyId;
        this.fields = fields;
    }

    @JsonIgnore
    public String getTitle() {
        for (Field field : fields) {
            if (field.getDefinition() != null && field.getDefinition().isMainEntry()) {
                if (field.getValue() != null) {
                    return field.getValue();
                }
            }
        }
        return "";
    }

    @Override
    public void lazyLoad() {
    }

    public List<Field> getMainFields() {
        List<Field> answer = new ArrayList<>();
        for (Field field : fields) {
            if (field.getDefinition() != null && field.getDefinition().isTitleField()) {
                answer.add(field);
            }
        }
        return answer;
    }

    public String getFieldValue(Definition definition) {
        for (Field field : fields) {
            if (field.getDefinition() != null && field.getDefinition().equals(definition)) {
                return field.getValue();
            }
        }
        return "";
    }

    public Field getFieldByLabel(String label) {
        for (Field field : this.fields) {
            if (field.getLabel().equals(label)) {
                return field;
            }
        }
        return null;
    }

    @Override
    public int compareTo(VocabRecord o) {
        return getTitle().compareToIgnoreCase(o.getTitle());
    }

}
