package de.sub.goobi.metadaten.search;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.digiverso.normdataimporter.model.NormDataRecord;
import de.intranda.digiverso.normdataimporter.model.NormDataValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;


/**
 * Class to import data from KulturNav. The endpoint is queried, data are imported
 * and then mapped to the norm labels in which Goobi understands
 *
 * @author Hemed Al Ruwehy
 * 2021-03-03
 */
public class KulturNavImporter implements NormDataImporter {
    private static final Logger logger = LoggerFactory.getLogger(KulturNavImporter.class);
    public static final String KULTURNAV_BASE_URL = "https://kulturnav.org/";

    public KulturNavImporter() {
    }

    // TODO Simply and remove boilerplate code and use recursion
    // For string
    // For Arrays
    // For Map
    private static void addToRecord(List<NormData> record, Object content, String label, boolean useAsIdentifier, boolean useAsUrl) {
        if (content != null) {
            NormData data = new NormData();
            data.setKey(label);
            List<NormDataValue> values = new ArrayList();
            Iterator var8;
            Object o;
            if (content instanceof ArrayList) {
                ArrayList contentList = (ArrayList) content;
                var8 = contentList.iterator();

                while (var8.hasNext()) {
                    o = var8.next();
                    values.add(new NormDataValue(o.toString(), useAsIdentifier ? o.toString() : null, useAsUrl ? o.toString() : null));
                }
            } else if (!(content instanceof LinkedTreeMap)) {
                values.add(new NormDataValue(content.toString(), useAsIdentifier ? content.toString() : null, useAsUrl ? content.toString() : null));
            } else {
                LinkedTreeMap contentMap = (LinkedTreeMap) content;
                var8 = contentMap.entrySet().iterator();

                while (true) {
                    if (!var8.hasNext()) {
                        data = null;
                        break;
                    }
                    o = var8.next();
                    NormData internal_data = new NormData();
                    List<NormDataValue> internal_values = new ArrayList();
                    Map.Entry e = (Map.Entry) o;
                    internal_data.setKey(label + "_" + e.getKey());
                    if (e.getValue() instanceof ArrayList) {
                        ArrayList v1 = (ArrayList) e.getValue();
                        Iterator var14 = v1.iterator();

                        while (var14.hasNext()) {
                            Object o1 = var14.next();
                            internal_values.add(new NormDataValue(o1.toString(), null, null));
                        }
                    } else {
                        internal_values.add(new NormDataValue(e.getValue().toString(), null, null));
                    }

                    internal_data.setValues(internal_values);
                    record.add(internal_data);
                }
            }
            if (data != null) {
                data.setValues(values);
                record.add(data);
            }
        }

    }


    // TODO Simplify the code
    private static NormDataRecord createRecord(LinkedTreeMap<String, Object> map, List<String> defaultLabelList) {
        NormDataRecord normdataRecord = new NormDataRecord();
        List<NormData> normdataValueList = new ArrayList<>();
        //addToRecord(normdataValueList, map.get("prefLabel"), "NORM_LABEL", false, false);
        addToRecord(normdataValueList, map.get("name"), "NORM_LABEL", false, false);
        addToRecord(normdataValueList, map.get("caption"), "NORM_ALTLABEL", false, false);
        addToRecord(normdataValueList, map.get("entityTypeHierarchy"), "NORM_NOTATION", false, false);
        addToRecord(normdataValueList, map.get("status"), "NORM_EXPLANATORYTEXT", false, false);
        addToRecord(normdataValueList, KULTURNAV_BASE_URL + map.get("uuid"), "URI", false, true);
        addToRecord(normdataValueList, map.get("identifier"), "NORM_EXTERNALURL", false, true);
        if (map.containsKey("inScheme")) {
            List<LinkedTreeMap> schema = (ArrayList) map.get("inScheme");
            Iterator var5 = schema.iterator();

            while (var5.hasNext()) {
                LinkedTreeMap data = (LinkedTreeMap) var5.next();
                if (data.get("uri") != null) {
                    addToRecord(normdataValueList, data.get("uri"), "NORM_AUTHORITY_URI", false, false);
                }

                if (data.get("prefLabel") != null) {
                    addToRecord(normdataValueList, data.get("prefLabel"), "NORM_AUTHORITY_ID", false, false);
                }
            }
        }
        Iterator var9;
        Iterator var12;
        if (defaultLabelList != null && !defaultLabelList.isEmpty()) {
            var9 = defaultLabelList.iterator();
            label61:
            while (var9.hasNext()) {
                String fieldName = (String) var9.next();
                var12 = normdataValueList.iterator();

                while (var12.hasNext()) {
                    NormData normdata = (NormData) var12.next();
                    if (normdata.getKey().equals(fieldName)) {
                        String value = normdata.getValues().get(0).getText();
                        normdataRecord.setPreferredValue(value);
                        break label61;
                    }
                }
            }
        }
        normdataRecord.setNormdataList(normdataValueList);
        var9 = normdataValueList.iterator();
        while (true) {
            NormData nd;
            do {
                if (!var9.hasNext()) {
                    return normdataRecord;
                }
                nd = (NormData) var9.next();
            } while (!nd.getKey().startsWith("NORM_LABEL") &&
                    !nd.getKey().startsWith("NORM_ALTLABEL") &&
                    !nd.getKey().startsWith("NORM_NOTATION"));

            var12 = nd.getValues().iterator();
            while (var12.hasNext()) {
                NormDataValue value = (NormDataValue) var12.next();
                normdataRecord.getValueList().add(value.getText());
            }
        }
    }





    /**
     * Prints the content of records of type NormDataRecord
     *
     * @param records a list of records
     */
    public static void printRecord(List<NormDataRecord> records) {
        for (NormDataRecord normDataRecord : records) {
            System.out.println("--------------------------" + normDataRecord.getPreferredValue());
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

    // Main method for easy debugging
    public static void main(String[] args) throws Exception {
        String url = "https://kulturnav.org/api/summary/entityType:Person,compoundName:Marcus";
        // String url = "http://api.dante.gbv.de/search?properties=*&query=*&voc=gender";
        List<NormDataRecord> records = new KulturNavImporter().importNormData(url);
        /*for (NormDataRecord ndr : records) {
            System.out.println("Value list: " + ndr);
        }*/
         printRecord(records);
    }

    /**
     * Imports data from the given endpoint and return a list of records
     *
     * @param url a URL to import from. The output are expected to be in JSON format
     * @return a list of records of type NormDataRecord
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<NormDataRecord> importNormData(String url) {
        logger.debug("Fetching norm data from: {}", url);
        List<NormDataRecord> records = new ArrayList<>();
        List<LinkedTreeMap<String, Object>> hits;
        try (InputStreamReader reader = new InputStreamReader(new URL(url).openStream())) {
            // List of hits from the response
            hits = (List<LinkedTreeMap<String, Object>>) (new Gson()).fromJson(reader, ArrayList.class);

            // For every hit, create a record
            for (LinkedTreeMap<String, Object> hit : hits) {

                System.out.println("---------------- Hit ------------------\n" +
                        new GsonBuilder()
                                .setPrettyPrinting()
                                .create()
                                .toJson(hit));

                records.add(createRecord(hit, Collections.emptyList()));
            }
        } catch (IOException ioException) {
            logger.error("Error while loading data from: {} {}", url, ioException);
        }
        return records;
    }
}
