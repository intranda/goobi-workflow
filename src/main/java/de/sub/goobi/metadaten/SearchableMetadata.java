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

    public long getCurrentVocabularySearchField();

    public void setCurrentVocabularySearchField(long field);

    public String getVocabularySearchQuery();

    public void setVocabularySearchQuery(String query);
}
