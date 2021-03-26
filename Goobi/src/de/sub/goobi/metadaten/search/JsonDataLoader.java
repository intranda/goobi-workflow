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
 * Loads JSON data from a given URL and do serialization
 *
 * @author Hemed Al Ruwehy
 * 2021-03-12
 */
public class JsonDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(JsonDataLoader.class);

    public JsonDataLoader() {

    }

    /**
     * Fetches data from the given JSON endpoint and return a list of Map representation
     * of such structure. The method expects that the response is JSON array
     *
     * @param url a URL to fetch from.
     * @return a list of JSON objects from the response (i.e list of Maps)
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> loadJsonList(String url) {
        logger.debug("Loading data from: {}", url);
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

