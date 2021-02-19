package de.sub.goobi.metadaten;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;

import org.goobi.api.display.enums.DisplayType;

public interface SearchableMetadata {

    /**
     * defines the type of the current metadata field
     * 
     * @return {@link DisplayType}
     */
    public DisplayType getMetadataDisplaytype();

    /**
     * set the default search value, some implementations like viaf use own fields
     * 
     * @param value
     */

    public void setSearchValue(String value);

    /**
     * set the default field to perform the search in. Some implementations like geonames don't need this
     * 
     * @param value
     */

    public void setSearchOption(String value);

    /**
     * performs the search
     * 
     * @return
     */

    public String search();

    /**
     * clear results of older search requests
     */

    public void clearResults();

    /**
     * defines if the search should be performed in gnd or viaf
     */
    public void setSearchInViaf(boolean serachInViaf);


    /**
     * Create a new URL, escape problematic characters
     * 
     * @param string
     * @return
     */

    default URL convertToURLEscapingIllegalCharacters(String string) {
        try {
            String decodedURL = URLDecoder.decode(string, "UTF-8");
            URL url = new URL(decodedURL);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            return uri.toURL();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Remove problematic tilde characters from response string // fix for gnd non sort name parts
     * 
     * @param str input string
     * @return string without 0x98 and 0x9C
     */
    default String filter(String str) {
        StringBuilder filtered = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char current = str.charAt(i);
            if (current != 0x98 && current != 0x9C) {
                filtered.append(current);
            }
        }
        return filtered.toString();
    }
}
