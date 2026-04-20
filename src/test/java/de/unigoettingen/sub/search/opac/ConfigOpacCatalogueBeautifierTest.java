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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ConfigOpacCatalogueBeautifierTest {

    @Test
    public void testConstructorAndGetters() {
        ConfigOpacCatalogueBeautifierElement changeEl = new ConfigOpacCatalogueBeautifierElement("100", "a", "newval");

        ConfigOpacCatalogueBeautifierElement proof1 = new ConfigOpacCatalogueBeautifierElement("200", "b", "trigger");
        ConfigOpacCatalogueBeautifierElement proof2 = new ConfigOpacCatalogueBeautifierElement("300", "c", "other");
        List<ConfigOpacCatalogueBeautifierElement> proofList = new ArrayList<>();
        proofList.add(proof1);
        proofList.add(proof2);

        ConfigOpacCatalogueBeautifier beautifier = new ConfigOpacCatalogueBeautifier(changeEl, proofList);
        assertNotNull(beautifier);
        assertEquals(changeEl, beautifier.getTagElementToChange());
        assertEquals(2, beautifier.getTagElementsToProof().size());
        assertTrue(beautifier.getTagElementsToProof().contains(proof1));
        assertTrue(beautifier.getTagElementsToProof().contains(proof2));
    }

    @Test
    public void testWithEmptyProofList() {
        ConfigOpacCatalogueBeautifierElement changeEl = new ConfigOpacCatalogueBeautifierElement("100", "a", "val");
        List<ConfigOpacCatalogueBeautifierElement> emptyList = new ArrayList<>();

        ConfigOpacCatalogueBeautifier beautifier = new ConfigOpacCatalogueBeautifier(changeEl, emptyList);
        assertNotNull(beautifier);
        assertTrue(beautifier.getTagElementsToProof().isEmpty());
    }

}
