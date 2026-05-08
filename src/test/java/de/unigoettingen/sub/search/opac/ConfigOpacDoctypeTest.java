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
package de.unigoettingen.sub.search.opac;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.FacesContextHelper;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
public class ConfigOpacDoctypeTest extends AbstractTest {

    private ConfigOpacDoctype doctype;
    private Map<String, String> labels;

    @BeforeEach
    public void setUp() {
        labels = new HashMap<>();
        labels.put("de", "Monographie");
        labels.put("en", "Monograph");

        List<String> mappings = Arrays.asList("Aa", "Oa", "Monograph");

        doctype = new ConfigOpacDoctype("monograph", "Monograph", "Monographie", false, false, false,
                labels, mappings, "Volume");

        UIViewRoot viewRoot = EasyMock.createMock(UIViewRoot.class);
        EasyMock.expect(viewRoot.getLocale()).andReturn(Locale.ENGLISH).anyTimes();
        EasyMock.replay(viewRoot);

        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        EasyMock.expect(facesContext.getViewRoot()).andReturn(viewRoot).anyTimes();
        EasyMock.replay(facesContext);

        FacesContextHelper.setFacesContext(facesContext);
    }

    @Test
    public void testConstructorAndGetters() {
        assertNotNull(doctype);
        assertEquals("monograph", doctype.getTitle());
        assertEquals("Monograph", doctype.getRulesetType());
        assertEquals("Monographie", doctype.getTifHeaderType());
        assertFalse(doctype.isPeriodical());
        assertFalse(doctype.isMultiVolume());
        assertFalse(doctype.isContainedWork());
        assertEquals(labels, doctype.getLabels());
        assertEquals(3, doctype.getMappings().size());
        assertTrue(doctype.getMappings().contains("Aa"));
        assertEquals("Volume", doctype.getRulesetChildType());
    }

    @Test
    public void testPeriodicalDoctype() {
        ConfigOpacDoctype periodical = new ConfigOpacDoctype("periodical", "Periodical", "Zeitschrift",
                true, false, false, labels, Arrays.asList("Ab"), "PeriodicalVolume");
        assertTrue(periodical.isPeriodical());
        assertFalse(periodical.isMultiVolume());
        assertEquals("PeriodicalVolume", periodical.getRulesetChildType());
    }

    @Test
    public void testMultiVolumeDoctype() {
        ConfigOpacDoctype mv = new ConfigOpacDoctype("multivolume", "MultiVolume", "Mehrbändig",
                false, true, false, labels, Arrays.asList("Af"), null);
        assertFalse(mv.isPeriodical());
        assertTrue(mv.isMultiVolume());
        assertNull(mv.getRulesetChildType());
    }

    @Test
    public void testSetMappings() {
        List<String> newMappings = Arrays.asList("X", "Y");
        doctype.setMappings(newMappings);
        assertEquals(newMappings, doctype.getMappings());
    }

    @Test
    public void testGetLocalizedLabelEnglish() {
        assertEquals("Monograph", doctype.getLocalizedLabel());
    }

}
