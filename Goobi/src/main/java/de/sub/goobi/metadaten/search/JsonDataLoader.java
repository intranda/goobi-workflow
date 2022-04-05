package de.sub.goobi.metadaten.search;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

import com.google.gson.Gson;

import lombok.extern.log4j.Log4j2;


/**
 * Loads data from a given URL and do serialization
 *
 * @author Hemed Al Ruwehy
 * 2021-03-12
 */
@Log4j2
public class JsonDataLoader {

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
        log.debug("Fetching json data from: {}", url);
        List<Map<String, Object>> hits = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(new URL(url).openStream())) {
            // List of hits from the response
            hits = (List<Map<String, Object>>) (new Gson()).fromJson(reader, ArrayList.class);
        } catch (IOException ioException) {
            log.error("Error while fetching data from: {} {}", url, ioException);
        }
        return hits;
    }


    /***
     * Loads XML file and return the instance of XMLConfiguration
     *
     * @param fileUrl url of XML file
     * @return an XMLConfiguration
     */
    public static XMLConfiguration loadXmlFile(String fileUrl ) {
        log.debug("Loading XML data from: {}", fileUrl);
        XMLConfiguration config = new XMLConfiguration();
        config.setDelimiterParsingDisabled(true);
        try {
            config.load(fileUrl);
        } catch (ConfigurationException e) {
            log.error("Error while loading config file " + e.getMessage());
        }
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
        return config;
    }
}

