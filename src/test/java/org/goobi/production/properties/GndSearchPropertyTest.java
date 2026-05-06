package org.goobi.production.properties;

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
 */
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.intranda.digiverso.normdataimporter.model.NormData;

public class GndSearchPropertyTest {

    private GndSearchProperty gndProperty;

    @BeforeEach
    public void setUp() {
        gndProperty = new GndSearchProperty() {
            @Override
            public String getSearchValue() {
                return null;
            }

            @Override
            public void setSearchValue(String value) {
            }

            @Override
            public String getSearchOption() {
                return null;
            }

            @Override
            public void setSearchOption(String option) {
            }

            @Override
            public String getValue() {
                return null;
            }

            @Override
            public void setValue(String option) {
            }

            @Override
            public String getGndNumber() {
                return null;
            }

            @Override
            public void setGndNumber(String option) {
            }

            @Override
            public void searchGnd() {
            }

            @Override
            public List<List<NormData>> getDataList() {
                return null;
            }

            @Override
            public void setDataList(List<List<NormData>> data) {
            }

            @Override
            public void setCurrentData(List<NormData> data) {
            }

            @Override
            public List<NormData> getCurrentData() {
                return null;
            }

            @Override
            public void importGndData() {
            }

            @Override
            public boolean isShowNoHits() {
                return false;
            }

            @Override
            public void setShowNoHits(boolean showNotHits) {
            }
        };
    }

    @Test
    public void testConvertValidHttpUrl() {
        URL url = gndProperty.convertToURLEscapingIllegalCharacters("http://d-nb.info/gnd/118540238");
        assertNotNull(url);
        assertEquals("http", url.getProtocol());
        assertEquals("d-nb.info", url.getHost());
    }

    @Test
    public void testConvertValidHttpsUrl() {
        URL url = gndProperty.convertToURLEscapingIllegalCharacters("https://d-nb.info/gnd/118540238");
        assertNotNull(url);
        assertEquals("https", url.getProtocol());
    }

    @Test
    public void testConvertInvalidUrlReturnsNull() {
        URL url = gndProperty.convertToURLEscapingIllegalCharacters("not a valid url");
        assertNull(url);
    }

    @Test
    public void testConvertUrlWithSpecialCharacters() {
        URL url = gndProperty.convertToURLEscapingIllegalCharacters("http://example.com/path?query=value&other=123");
        assertNotNull(url);
    }
}
