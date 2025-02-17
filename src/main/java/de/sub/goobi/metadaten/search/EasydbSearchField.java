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
package de.sub.goobi.metadaten.search;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class EasydbSearchField {

    // search type match:
    //        search element “match”
    //
    //        This search element allows to match a given text. It can be used with all search types. Match ignores case and diacritical marks,
    //        and detects some writing variants. For instance, “fusse” will match “Füße”.
    //        Parameter   Value
    //        mode    search mode (string, optional): fulltext (default), token or wildcard
    //        string  text to match (string). Maximal length: 256 charcaters (*)
    //        fields  fields to match against (array of fully qualified field names, optional): defaults to all. See Field Names
    //        languages   languages to match against (array of strings, optional): defaults to all search languages of the user
    //        phrase  phrase search (boolean, optional): defaults to false

    // search type in:
    //        Search for specific values in one or more fields.
    //        Parameter   Value
    //        in  values (array of <type>): <type> depends on field type. For <type> text/string: maximal length: 256 charcaters (*)
    //        fields  fields to consider for the search (array of fully qualified field names). See Field Names
    //        objecttype  objecttype (string): name of a linked objecttype or _pool
    //        include_path    include all objects in the path (boolean, optional, defaults to false): only with objecttype (see below)
    //        eas_field   EAS field (string)
    //        languages   languages to use (array of strings, optional): defaults to all search languages of the user

    // search element “range”

    //        This search element allows to match numeric (Number, Id), date/time (Date, Timestamp) or string (String) fields using a range.
    //        Parameter   Value
    //        field   field to consider for the search (string): fully qualified field name
    //        from    lower end of the range (number/string, optional): inclusive
    //        to  upper end of the range (number/string, optional): inclusive

    // search element “nested”
    //        This search element allows to retreive documents that match against a query for their nested documents. It is like a complex search
    //        that is performed at a certain path inside the main object type and returns objects from the main object type, but is run against
    //        the nested object type.
    //        Parameter   Value
    //        path    path to a field of nested elements (string): the field must be of type Nested
    //        search  search elements for the sub-query (array of search elements)

    //  search element “complex”
    //        This search element allows to specify more complex search expressions by nesting them in the global search. The normal search already
    //        allows some combinations, like “A or B or C”:

    private String type; // "match", "in", "range"
    private String bool; //  must (default), must_not or should
    private String mode; // fulltext (default), token or wildcard

    private String string; // search value
    private List<String> fields; // "artefact.title"

    private boolean phrase = false; // phrase search (boolean, optional): defaults to false

    private List<Object> in; // list of values

    //    private List<EasydbSearchField> search; // list of search fields

    private String field;
    private String from;
    private String to;

    private List<String> overrideValueList = new ArrayList<>();

    private String fieldType = "numeric";

    @JsonIgnore
    public List<String> getOverrideValueList() {
        return overrideValueList;
    }

    @JsonIgnore
    public void setOverrideValueList(List<String> overrideValueList) {
        this.overrideValueList = overrideValueList;
    }

    @JsonIgnore
    public String getFieldType() {
        return fieldType;
    }

    @JsonIgnore
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

}
