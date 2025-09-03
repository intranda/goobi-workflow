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
 */

package org.goobi.production.properties;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

import de.intranda.digiverso.normdataimporter.model.NormData;

public interface GndSearchProperty {

    /**
     * get the current search value.
     * 
     * @return search value
     */
    String getSearchValue();

    /**
     * sets a new search value.
     * 
     * @param value search value
     */
    void setSearchValue(String value);

    /**
     * @return the selected search option.
     */
    String getSearchOption();

    /**
     * sets a new search option.
     * 
     * @param option
     */
    void setSearchOption(String option);

    String getValue();

    void setValue(String option);

    String getGndNumber();

    void setGndNumber(String option);

    /**
     * Execute gnd search.
     */
    void searchGnd();

    /**
     * @return a list of all found records.
     */
    List<List<NormData>> getDataList();

    void setDataList(List<List<NormData>> data);

    /**
     * selected record.
     * 
     * @param data
     */
    void setCurrentData(List<NormData> data);

    List<NormData> getCurrentData();

    /**
     * Import selected record.
     */
    void importGndData();

    boolean isShowNoHits();

    void setShowNoHits(boolean showNotHits);

    default URL convertToURLEscapingIllegalCharacters(String string) {
        try {
            String decodedURL = URLDecoder.decode(string, "UTF-8");
            URL url = new URI(decodedURL).toURL();
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            return uri.toURL();
        } catch (URISyntaxException | IOException ex) {
            return null;
        }
    }
}
