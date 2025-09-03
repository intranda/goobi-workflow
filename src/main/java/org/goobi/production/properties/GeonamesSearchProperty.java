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

import java.util.List;

import org.geonames.Toponym;

public interface GeonamesSearchProperty {

    /**
     * get the current search value.
     * 
     * @return searcg value
     */
    String getSearchValue();

    /**
     * sets a new search value.
     * 
     * @param value search value
     */
    void setSearchValue(String value);

    String getValue();

    void setValue(String option);

    String getGeonamesNumber();

    void setGeonamesNumber(String option);

    void searchGeonames();

    Toponym getCurrentToponym();

    void setCurrentToponym(Toponym toponym);

    List<Toponym> getResultList();

    void setResultList(List<Toponym> results);

    int getTotalResults();

    void setTotalResults(int results);

    boolean isShowNoHits();

    void setShowNoHits(boolean showNotHits);

    void importGeonamesData();

}