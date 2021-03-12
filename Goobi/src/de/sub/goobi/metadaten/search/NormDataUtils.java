package de.sub.goobi.metadaten.search;

import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.digiverso.normdataimporter.model.NormDataRecord;
import de.intranda.digiverso.normdataimporter.model.NormDataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A class providing some utility methods for dealing with Norm data
 *
 * @author Hemed Al Ruwehy
 * <p>
 * 2021-03-12
 */
public class NormDataUtils {

    private NormDataUtils() {

    }

    /***
     * Creates norm data based on the given parameters.
     *
     * @param label a label
     * @param object a Java object of type List, Map or Primitive type
     * @param useAsId  whether the text is ID
     * @param useAsUrl whether the text is Url
     * @return a list of norm data
     */
    @SuppressWarnings("unchecked")
    private static NormData getNormData(String label, Object object,
                                        boolean useAsId, boolean useAsUrl) {
        NormData normData = new NormData();
        if (Objects.nonNull(object)) {
            List<NormDataValue> normDataValues = new ArrayList<>();
            // if is of type list, then convert every item to NormDataValue
            if (object instanceof List) {
                for (Object item : (List<Object>) object) {
                    normDataValues.add(new NormDataValue(
                            item.toString(),
                            useAsId ? item.toString() : null,
                            useAsUrl ? item.toString() : null
                    ));
                }
            }
            // do the same for other types e.g primitive types
            else if (!(object instanceof Map)) {
                String stringValue = object.toString();
                normDataValues.add(
                        new NormDataValue(stringValue,
                                useAsId ? stringValue : null,
                                useAsUrl ? stringValue : null
                        ));
            }
            normData.setKey(label);
            normData.setValues(normDataValues);
        }
        return normData;
    }


    /**
     * A shortcut for creating norm data for texts where we are
     * not interested with Id or Url.
     *
     * @param label       a label
     * @param jsonContent a JSON string
     * @return a list of norm data
     */
    public static List<NormData> createNormData(String label, Object jsonContent) {
        return createNormData(label, jsonContent, false, false);
    }


    /***
     *  Creates a norm data list. The values of JSON objects are retrieved
     *  based on three criteria, whether they are arrays, or primitive types
     *  or maps (i.e nested objects)
     *
     * @param label a label
     * @param javaObject a Java Object of type List, Map or Primitive types
     * @param useAsId  whether the text is ID
     * @param useAsUrl whether the text is Url
     * @return a list of norm data
     */
    @SuppressWarnings("unchecked")
    public static List<NormData> createNormData(String label, Object javaObject,
                                                boolean useAsId, boolean useAsUrl) {
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
     * @param labels        one or more labels
     * @return list of values for a specific label.
     */
    public static List<String> getValuesForLabel(List<NormData> normDataList, String... labels) {
        List<String> allValues = new ArrayList<>();
        if(labels != null && labels.length > 0) {
            for (String label : labels) {
                List<String> labelValues = new ArrayList<>();
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

    /**
     * Prints the content of records of type NormDataRecord
     *
     * @param records a list of records
     */
    public static void printRecords(List<NormDataRecord> records) {
        for (NormDataRecord normDataRecord : records) {
            System.out.println("-----------------------");
            for (NormData normData : normDataRecord.getNormdataList()) {
                StringBuilder sb = new StringBuilder();
                sb.append(normData.getKey()).append(" : ");
                for (NormDataValue value : normData.getValues()) {
                    sb.append(value.getText()).append(", ");
                }
                System.out.println(sb.toString());
            }
        }
    }
}
