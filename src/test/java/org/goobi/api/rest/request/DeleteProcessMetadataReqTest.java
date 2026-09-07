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

package org.goobi.api.rest.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.goobi.api.rest.response.UpdateMetadataResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.Prefs;

public class DeleteProcessMetadataReqTest extends AbstractTest {

    private Prefs prefs;
    private DigitalDocument document;
    private DocStruct logical;
    private MetadataGroup group;

    @BeforeEach
    public void setUp() throws Exception {
        prefs = new Prefs();
        prefs.loadPrefs("src/test/resources/rulesets/ruleset.xml");

        document = new DigitalDocument();
        logical = document.createDocStruct(prefs.getDocStrctTypeByName("Monograph"));
        document.setLogicalDocStruct(logical);

        group = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        logical.addMetadataGroup(group);
    }

    private Metadata junitMetadata(String value) throws Exception {
        Metadata metadata = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
        metadata.setValue(value);
        return metadata;
    }

    /**
     * The request works on a document that a process supplies, so the deletion is driven directly instead of through apply(Process).
     */
    private void deleteMetadata(String... names) throws Exception {
        DeleteProcessMetadataReq request = new DeleteProcessMetadataReq();

        Field payload = DeleteProcessMetadataReq.class.getDeclaredField("deleteMetadata");
        payload.setAccessible(true);
        payload.set(request, Arrays.asList(names));

        Method deletion =
                DeleteProcessMetadataReq.class.getDeclaredMethod("deleteMetadata", Prefs.class, DigitalDocument.class, UpdateMetadataResponse.class);
        deletion.setAccessible(true);
        deletion.invoke(request, prefs, document, new UpdateMetadataResponse());
    }

    /**
     * A metadata addressed as group/metadata lives in the group, so that is where it has to be deleted.
     */
    @Test
    public void testDeleteMetadataOfAGroupRemovesItFromTheGroup() throws Exception {
        group.addMetadata(junitMetadata("delete me"));

        deleteMetadata("junitgrp/junitMetadata");

        assertTrue(group.getMetadataByType("junitMetadata").isEmpty());
    }

    /**
     * Deleting inside a group must not touch metadata of the doc struct, not even one that happens to carry the same type and value.
     */
    @Test
    public void testDeleteMetadataOfAGroupKeepsMetadataOfTheDocStruct() throws Exception {
        group.addMetadata(junitMetadata("same value"));
        logical.addMetadata(junitMetadata("same value"));

        deleteMetadata("junitgrp/junitMetadata");

        assertEquals(1, logical.getAllMetadataByType(prefs.getMetadataTypeByName("junitMetadata")).size());
    }

    @Test
    public void testDeleteMetadataOfTheDocStruct() throws Exception {
        logical.addMetadata(junitMetadata("delete me"));

        deleteMetadata("junitMetadata");

        assertTrue(logical.getAllMetadataByType(prefs.getMetadataTypeByName("junitMetadata")).isEmpty());
    }
}
