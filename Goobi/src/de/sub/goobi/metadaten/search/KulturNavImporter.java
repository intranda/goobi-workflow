package de.sub.goobi.metadaten.search;

import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.digiverso.normdataimporter.model.NormDataRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.*;


/**
 * Import authority data from KulturNav. The endpoint is queried and then
 * data are imported and mapped to the norm labels
 *
 * @author Hemed Ali Al Ruwehy
 * 2021-03-03
 */
public class KulturNavImporter extends JsonDataLoader {
    public static final String BASE_URL = "https://kulturnav.org/";
    public static final String SUMMARY_URL = BASE_URL + "api/summary/";
    private static final Logger logger = LoggerFactory.getLogger(KulturNavImporter.class);

    public KulturNavImporter() {
    }


    /**
     * Creates a norm data record based on the structure of the jsonMap
     *
     * @param jsonMap          a JSON map
     * @param defaultLabelList a default list of preferred values that will
     *                         be shown to the search box. Default are values
     *                         from getValueList()
     * @return a record of type NormDataRecord
     */
    private static NormDataRecord createNormDataRecord(Map<String, Object> jsonMap,
                                                       List<String> defaultLabelList) {
        NormDataRecord normDataRecord = new NormDataRecord();
        List<NormData> normDataValuesList = new ArrayList<>();

        normDataValuesList.addAll(
                NormDataUtils.createNormData("NORM_LABEL", jsonMap.get("name")));
        normDataValuesList.addAll(
                NormDataUtils.createNormData("NORM_ALTLABEL", jsonMap.get("caption")));
        normDataValuesList.addAll(NormDataUtils.createNormData(
                "URI",
                BASE_URL + jsonMap.get("uuid"),
                false,
                true));
        normDataRecord.setNormdataList(normDataValuesList);

        // TODO Set preferred values based on keys from defaultLabelList
        // Used as preferred values in the search box
        Set<String> valuesForLabel = NormDataUtils.getValuesForLabel(
                normDataValuesList, "NORM_LABEL", "NORM_ALTLABEL");
        normDataRecord.getValueList().addAll(valuesForLabel);
        return normDataRecord;
    }


    /**
     * Creates a Norm data records based on the structure of the jsonMap
     *
     * @param jsonMap a JSON map
     * @return a record of type NormDataRecord
     */
    public static NormDataRecord createNormDataRecord(Map<String, Object> jsonMap) {
        return createNormDataRecord(jsonMap, Collections.emptyList());
    }

    /**
     * KulturNav search string must be encoded and all white spaces must be
     * interpreted as "%20" so that the backend can understand.
     *
     * @param searchString a URL to be encoded
     * @return an encoded URL
     */
    public static String parseSearchString(String searchString) {
        try {
            if (StringUtils.isNotBlank(searchString)) {
                StringBuilder encodedString = new StringBuilder();
                String[] tokens = searchString.split("\\s+");
                for (String token : tokens) {
                    encodedString
                            .append(URLEncoder.encode(token.trim(), "UTF-8"))
                            .append(' ');
                }
                return encodedString.toString()
                        .trim()
                        .replaceAll("\\s+", "%20");
            }
        } catch (Exception e) {
            logger.error("Unable to parse search string {}, {}", searchString, e.toString());
        }
        return searchString;
    }


    /**
     * Source is a fragment which restricts the search to a specific category,
     * for example, an entity class or a dataset, e.g
     * <code>entityType:Person,entity.dataset:508197af-6e36-4e4f-927c-79f8f63654b2</code>
     * See more: http://kulturnav.org/info/api
     *
     * @param source a source to parse
     * @return a source which is ready to be appended to KulturNav API endpoint
     */
    public static String parseSource(String source) {
        if (StringUtils.isNotBlank(source)) {
            if (source.endsWith(",")) {
                return source;
            }
            return source + ",";
        }
        return "";
    }

    /**
     * Imports data from the given endpoint and return a list of norm data records.
     *
     * @param url a URL to import from.
     * @return a list of records of type NormDataRecord
     */
    public static List<NormDataRecord> importNormData(String url) {
        List<NormDataRecord> records = new ArrayList<>();
        List<Map<String, Object>> hits = loadJsonList(url);
        for (Map<String, Object> hit : hits) {
            /*System.out.println("---------------- Hit ---------------\n" +
                    new GsonBuilder()
                            .setPrettyPrinting()
                            .create()
                            .toJson(hit));*/
            records.add(createNormDataRecord(hit));
        }
        return records;
    }


    /**
     * Constructs summary Url to send it to KulturNav
     *
     * @param searchString a search string
     * @param source  a source @see {{@link #parseSource(String)}}
     * @return
     */
    public static String constructSummaryUrl(String searchString, String source) {
        StringBuilder knUrl = new StringBuilder();
        knUrl.append(KulturNavImporter.SUMMARY_URL);
        knUrl.append(KulturNavImporter.parseSource(source));
        knUrl.append("compoundName:");
        knUrl.append(KulturNavImporter.parseSearchString(searchString));

        return knUrl.toString();
    }

    // Main method for easy debugging
    public static void main(String[] args) throws Exception {
        String encoded = parseSearchString("Almaas OR Øyvind 1939");
        String url = SUMMARY_URL + "entityType:Person,compoundName:" + encoded;
        System.out.println("Full URL: " + url);

        // System.out.println(fetchJsonString(" http://api.dante.gbv.de/search?query=Adolf"));
        // System.out.println(fetchJsonString("https://jambo.uib.no/blackbox/suggest?q=Mar"));
        // System.out.println(fetchJsonString("https://arbeidsplassen.nav.no/stillinger/api/search"));

        // Print the content of record
        List<NormDataRecord> records = importNormData(url);
        for (NormDataRecord normDataRecord : records) {
            System.out.println("--------------------");
            NormDataUtils.printRecord(normDataRecord);
        }
    }

}
