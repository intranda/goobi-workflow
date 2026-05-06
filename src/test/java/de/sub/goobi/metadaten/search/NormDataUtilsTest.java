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
package de.sub.goobi.metadaten.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.digiverso.normdataimporter.model.NormDataValue;

public class NormDataUtilsTest {

    @Test
    public void testCreateNormDataFromString() {
        List<NormData> result = NormDataUtils.createNormData("LABEL", "Hello World");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("LABEL", result.get(0).getKey());
        assertEquals(1, result.get(0).getValues().size());
        assertEquals("Hello World", result.get(0).getValues().get(0).getText());
    }

    @Test
    public void testCreateNormDataFromInteger() {
        List<NormData> result = NormDataUtils.createNormData("COUNT", 42);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("COUNT", result.get(0).getKey());
        assertEquals("42", result.get(0).getValues().get(0).getText());
    }

    @Test
    public void testCreateNormDataFromList() {
        List<Object> values = Arrays.asList("val1", "val2", "val3");
        List<NormData> result = NormDataUtils.createNormData("ITEMS", values);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ITEMS", result.get(0).getKey());
        assertEquals(3, result.get(0).getValues().size());
        assertEquals("val1", result.get(0).getValues().get(0).getText());
        assertEquals("val3", result.get(0).getValues().get(2).getText());
    }

    @Test
    public void testCreateNormDataFromMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("first", "Alice");
        map.put("last", "Smith");
        List<NormData> result = NormDataUtils.createNormData("NAME", map);
        assertNotNull(result);
        assertEquals(2, result.size());
        // each entry becomes a separate NormData with label_key
        assertTrue(result.stream().anyMatch(nd -> "NAME_first".equals(nd.getKey())));
        assertTrue(result.stream().anyMatch(nd -> "NAME_last".equals(nd.getKey())));
    }

    @Test
    public void testCreateNormDataWithNullReturnsEmpty() {
        List<NormData> result = NormDataUtils.createNormData("LABEL", null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCreateNormDataWithIdAndUrl() {
        List<NormData> result = NormDataUtils.createNormData("URI", "https://example.com/123", true, true);
        assertNotNull(result);
        assertEquals(1, result.size());
        NormDataValue value = result.get(0).getValues().get(0);
        assertEquals("https://example.com/123", value.getText());
        assertEquals("https://example.com/123", value.getIdentifier());
        assertEquals("https://example.com/123", value.getUrl());
    }

    @Test
    public void testGetValuesForLabel() {
        NormData nd1 = new NormData();
        nd1.setKey("NORM_LABEL");
        NormDataValue v1 = new NormDataValue("Alice", null, null);
        nd1.setValues(Arrays.asList(v1));

        NormData nd2 = new NormData();
        nd2.setKey("NORM_ALTLABEL");
        NormDataValue v2 = new NormDataValue("Alicia", null, null);
        nd2.setValues(Arrays.asList(v2));

        List<NormData> normDataList = Arrays.asList(nd1, nd2);

        Set<String> result = NormDataUtils.getValuesForLabel(normDataList, "NORM_LABEL");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains("Alice"));
    }

    @Test
    public void testGetValuesForMultipleLabels() {
        NormData nd1 = new NormData();
        nd1.setKey("NORM_LABEL");
        nd1.setValues(Arrays.asList(new NormDataValue("Alice", null, null)));

        NormData nd2 = new NormData();
        nd2.setKey("NORM_ALTLABEL");
        nd2.setValues(Arrays.asList(new NormDataValue("Alicia", null, null)));

        List<NormData> normDataList = Arrays.asList(nd1, nd2);

        Set<String> result = NormDataUtils.getValuesForLabel(normDataList, "NORM_LABEL", "NORM_ALTLABEL");
        assertEquals(2, result.size());
        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("Alicia"));
    }

    @Test
    public void testGetValuesForLabelWithNullLabelsReturnsEmpty() {
        List<NormData> normDataList = new ArrayList<>();
        Set<String> result = NormDataUtils.getValuesForLabel(normDataList, (String[]) null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetValuesForLabelNoMatch() {
        NormData nd = new NormData();
        nd.setKey("OTHER_KEY");
        nd.setValues(Arrays.asList(new NormDataValue("Value", null, null)));

        Set<String> result = NormDataUtils.getValuesForLabel(Arrays.asList(nd), "NORM_LABEL");
        assertTrue(result.isEmpty());
    }
}
