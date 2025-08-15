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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.goobi.api.display.Item;
import org.goobi.api.display.enums.DisplayType;
import org.goobi.api.display.helper.ConfigDisplayRules;

import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.digiverso.normdataimporter.model.NormDataRecord;
import lombok.extern.log4j.Log4j2;

/**
 * Import authority data from KulturNav (https://kulturnav.org/). The endpoint is queried and then data are imported and mapped to the norm labels
 * which Goobi understands
 *
 * @author Hemed Ali Al Ruwehy 2021-03-03
 */
@Log4j2
public class KulturNavImporter extends JsonDataLoader {
    public static final String BASE_URL = "https://kulturnav.org/";
    public static final String SUMMARY_URL = BASE_URL + "api/summary/";

    /**
     * Extracts UUID from KulturNav Url, which is in the form: BASE_URL + "uuid".
     *
     * @param url an Url to extract from
     * @return UUID or the same input url if extraction failed
     */
    public static String getUuidFromUrl(String url) {
        if (StringUtils.isNotBlank(url)) {
            int lastIndex = url.lastIndexOf('/');
            if (lastIndex != -1) {
                return url.substring(lastIndex + 1);
            }
        }
        return url;
    }

    /**
     * Creates a Norm data records based on the structure of the jsonMap.
     *
     * @param jsonMap a JSON map
     * @return a record of type NormDataRecord
     */
    public static NormDataRecord createNormDataRecord(Map<String, Object> jsonMap) {
        NormDataRecord normDataRecord = new NormDataRecord();
        List<NormData> normDataValuesList = new ArrayList<>();

        normDataValuesList.addAll(NormDataUtils.createNormData("NORM_LABEL", jsonMap.get("name")));
        normDataValuesList.addAll(NormDataUtils.createNormData("NORM_ALTLABEL", jsonMap.get("caption")));
        normDataValuesList.addAll(NormDataUtils.createNormData("URI", BASE_URL + jsonMap.get("uuid"), false, true));
        normDataRecord.setNormdataList(normDataValuesList);

        // Used as preferred values in the search box
        Set<String> valuesForLabel = NormDataUtils.getValuesForLabel(normDataValuesList, "NORM_LABEL", "NORM_ALTLABEL");
        normDataRecord.getValueList().addAll(valuesForLabel);
        return normDataRecord;
    }

    /**
     * KulturNav search string must be encoded and all white spaces must be interpreted as "%20" so that the backend can understand.
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
        } catch (UnsupportedEncodingException e) {
            log.error("Unable to parse search string {}, {}", searchString, e.toString());
        }
        return searchString;
    }

    /**
     * Source is a fragment which restricts the search to a specific category, for example, an entity class or a dataset, e.g
     * <code>entityType:Person,entity.dataset:508197af-6e36-4e4f-927c-79f8f63654b2</code> . See more: http://kulturnav.org/info/api
     *
     * @param source a source to parse
     * @return a source which is ready to be appended to KulturNav API endpoint
     * @see {@link #readSourceFromDisplayRulesConfig(String)}
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
            records.add(createNormDataRecord(hit));
        }
        return records;
    }

    /**
     * Constructs summary Url to send it to KulturNav.
     *
     * @param searchString a search string
     * @param source a source @see {{@link #parseSource(String)}}
     * @return url
     */
    public static String constructSearchUrl(String searchString, String source) {
        StringBuilder knUrl = new StringBuilder();
        knUrl.append(KulturNavImporter.SUMMARY_URL);
        knUrl.append(KulturNavImporter.parseSource(source));
        knUrl.append("compoundName:");
        knUrl.append(KulturNavImporter.parseSearchString(searchString));
        return knUrl.toString();
    }

    /**
     * Gets source for metadata name "person" from config file.
     *
     * @return source
     */
    public static String getSourceForPerson() {
        String sourceForPerson = readSourceFromDisplayRulesConfig("person");
        if (sourceForPerson.isEmpty()) { // default
            return "entityType:Agent";
        }
        return sourceForPerson;
    }

    /**
     * Reads source from config file <code>goobi_metadataDisplayRules.xml</code> where the display type is {@link DisplayType#kulturnav} for a given
     * metadata ref (element name).
     * <p>
     * The structure could look like this: <code>
     * &lt;kulturnav ref="person"&gt;
     * &lt;source>entityType:Person, entity.dataset:d519f76b-5ce5-4876-906e-7d31a76eb609&lt;/source&gt;
     * &lt;/kulturnav&gt;
     * </code>
     *
     * @param metadataRef a metadata name
     * @return a source if found, otherwise empty string
     */
    public static String readSourceFromDisplayRulesConfig(String metadataRef) {
        String source = "";
        List<Item> items = ConfigDisplayRules.getInstance()
                .getItemsByNameAndType("*", metadataRef, DisplayType.kulturnav);

        if (!items.isEmpty() && !items.get(0).getSource().isEmpty()) {
            source = items.get(0).getSource();
            if (log.isDebugEnabled()) {
                log.debug("Found source with value {} for metadata {} and display type {}",
                        source, metadataRef, DisplayType.kulturnav.name());
            }
        }
        return source;
    }

}
