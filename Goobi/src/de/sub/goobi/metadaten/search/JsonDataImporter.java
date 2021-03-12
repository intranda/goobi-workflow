package de.sub.goobi.metadaten.search;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Handle the import of JSON data and convert then into a Map of string value pairs
 *
 * @author Hemed Al Ruwehy
 * 2021-03-12
 */
public class JsonDataImporter {
    private static final Logger logger = LoggerFactory.getLogger(JsonDataImporter.class);

    public JsonDataImporter() {

    }

    /**
     * Imports data from the given JSON endpoint and return a Map representation
     * of such JSON structure.
     *
     * It is expected that the response is in JSON format
     *
     * @param url a URL to fetch from.
     * @return a list of JSON objects from the response (i.e list of Maps)
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> fetchJsonData(String url) {
        logger.debug("Fetching JSON data from: {}", url);
        List<Map<String, Object>> hits = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(new URL(url).openStream())) {
            // List of hits from the response
            hits = (List<Map<String, Object>>) (new Gson()).fromJson(reader, ArrayList.class);
        } catch (IOException ioException) {
            logger.error("Error while fetching data from: {} {}", url, ioException);
        }
        return hits;
    }
}

