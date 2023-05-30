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
 * @author Hemed Al Ruwehy 2021-03-12
 */
@Log4j2
public class JsonDataLoader {

    /**
     * Fetches data from the given JSON endpoint and return a list of Map representation of such structure. The method expects that the response is
     * JSON array
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
            hits = (new Gson()).fromJson(reader, ArrayList.class);
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
    public static XMLConfiguration loadXmlFile(String fileUrl) {
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
