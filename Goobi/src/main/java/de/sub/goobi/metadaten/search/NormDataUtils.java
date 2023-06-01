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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.digiverso.normdataimporter.model.NormDataValue;

/**
 * A class providing some utility methods for dealing with Norm data
 *
 * @author Hemed Al Ruwehy
 *         <p>
 *         2021-03-12
 */
public class NormDataUtils {

    // Prevent this class from being instantiated
    private NormDataUtils() {

    }

    /***
     * Creates norm data based on the given parameters.
     *
     * @param label a label
     * @param object a Java object of type List, Map or Primitive type
     * @param useAsId whether the text is ID
     * @param useAsUrl whether the text is Url
     * @return a list of norm data
     */
    @SuppressWarnings("unchecked")
    private static NormData getNormData(String label, Object object, boolean useAsId, boolean useAsUrl) {
        NormData normData = new NormData();
        if (Objects.nonNull(object)) {
            List<NormDataValue> normDataValues = new ArrayList<>();
            // if is of type list, then convert every item to NormDataValue
            if (object instanceof List) {
                for (Object item : (List<Object>) object) {
                    normDataValues.add(new NormDataValue(
                            item.toString(),
                            useAsId ? item.toString() : null,
                            useAsUrl ? item.toString() : null));
                }
            }
            // do the same for other types e.g primitive types
            else if (!(object instanceof Map)) {
                String stringValue = object.toString();
                normDataValues.add(
                        new NormDataValue(stringValue,
                                useAsId ? stringValue : null,
                                useAsUrl ? stringValue : null));
            }
            normData.setKey(label);
            normData.setValues(normDataValues);
        }
        return normData;
    }

    /**
     * A shortcut for creating norm data for texts where we are not interested with Id or Url.
     *
     * @param label a label
     * @param jsonContent a JSON string
     * @return a list of norm data
     */
    public static List<NormData> createNormData(String label, Object jsonContent) {
        return createNormData(label, jsonContent, false, false);
    }

    /***
     * Creates a norm data list. The values of JSON objects are retrieved based on three criteria, whether they are arrays, or primitive types or maps
     * (i.e nested objects).
     *
     * @param label a label
     * @param javaObject a Java Object of type List, Map or Primitive types
     * @param useAsId whether the text is ID
     * @param useAsUrl whether the text is Url
     * @return a list of norm data
     */
    @SuppressWarnings("unchecked")
    public static List<NormData> createNormData(String label, Object javaObject, boolean useAsId, boolean useAsUrl) {
        List<NormData> normDataList = new ArrayList<>();
        if (Objects.nonNull(javaObject)) {
            if (javaObject instanceof Map) {
                // if it is map (i.e nested JSON object), go further down and
                // fetch values recursively. Note however that, this goes
                // only one level deep in the tree
                Map<String, Object> contentMap = (Map<String, Object>) javaObject;
                for (Map.Entry<String, Object> entry : contentMap.entrySet()) {
                    List<NormData> internalNormData = createNormData(
                            label + "_" + entry.getKey(),
                            entry.getValue(),
                            false,
                            false);
                    normDataList.addAll(internalNormData);
                }
            } else { // handle array or primitive types
                NormData normData = getNormData(label, javaObject, useAsId, useAsUrl);
                normDataList.add(normData);
            }
        }
        return normDataList;
    }

    /**
     * Gets a list of values for a specific label in the norm data
     *
     * @param normDataList a norm data list to get vakues from
     * @param labels one or more labels
     * @return list of values for a specific label.
     */
    public static Set<String> getValuesForLabel(List<NormData> normDataList, String... labels) {
        Set<String> allValues = new TreeSet<>();
        if (labels != null && labels.length > 0) {
            for (String label : labels) {
                Set<String> labelValues = new HashSet<>();
                for (NormData normData : normDataList) {
                    if (normData.getKey().startsWith(label)) {
                        normData.getValues().forEach(value -> {
                            if (Objects.nonNull(value)) {
                                labelValues.add(value.getText());
                            }
                        });
                    }
                }
                allValues.addAll(labelValues);
            }
        }
        return allValues;
    }

}
