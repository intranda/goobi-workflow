/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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

package org.goobi.api.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.easymock.EasyMock;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.vocabulary.JskosRecord;
import org.goobi.vocabulary.VocabRecord;
import org.goobi.vocabulary.Vocabulary;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.VocabularyManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ VocabularyManager.class, Helper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*", "javax.crypto.*" })

public class VocabularyResourceTest {

    @Before
    public void setUp() throws Exception {
        PowerMock.mockStatic(Helper.class);
        EasyMock.expect(Helper.getCurrentUser()).andReturn(null).anyTimes();
        PowerMock.replay(Helper.class);

        PowerMock.mockStatic(VocabularyManager.class);

        List<VocabRecord> records = new ArrayList<>();

        VocabRecord vr = new VocabRecord();
        records.add(vr);

        Vocabulary vocab = new Vocabulary();

        EasyMock.expect(VocabularyManager.findRecords(EasyMock.anyString(), EasyMock.anyString())).andReturn(records).anyTimes();
        EasyMock.expect(VocabularyManager.findRecords(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString()))
                .andReturn(records)
                .anyTimes();
        EasyMock.expect(VocabularyManager.findRecords(EasyMock.anyString(), EasyMock.anyObject())).andReturn(records).anyTimes();

        EasyMock.expect(VocabularyManager.getRecord(EasyMock.anyInt(), EasyMock.anyInt())).andReturn(vr).anyTimes();

        VocabularyManager.getAllRecords(EasyMock.anyObject());
        EasyMock.expect(VocabularyManager.getVocabularyByTitle(EasyMock.anyString())).andReturn(vocab).anyTimes();
        EasyMock.expect(VocabularyManager.getVocabularyById(EasyMock.anyInt())).andReturn(vocab).anyTimes();
        PowerMock.replay(VocabularyManager.class);

    }

    @Test
    public void testResource() {
        VocabularyResource res = new VocabularyResource();
        assertNotNull(res);
    }

    @Test
    public void testVocabularySearchValue() {
        VocabularyResource res = new VocabularyResource();
        Response response = res.findRecords("vocabulary", "searchvalue");
        assertEquals(200, response.getStatus());
        @SuppressWarnings("unchecked")
        List<VocabRecord> rec = (List<VocabRecord>) response.getEntity();
        assertEquals(1, rec.size());
    }

    @Test
    public void testVocabularyFieldnameSearchValue() {
        VocabularyResource res = new VocabularyResource();
        Response response = res.findRecords("vocabulary", "fieldname", "searchvalue");
        assertEquals(200, response.getStatus());
        @SuppressWarnings("unchecked")
        List<VocabRecord> rec = (List<VocabRecord>) response.getEntity();
        assertEquals(1, rec.size());

    }

    @Test
    public void testVocabulary() {
        VocabularyResource res = new VocabularyResource();
        List<StringPair> list = new ArrayList<>();
        Response response = res.findRecords("vocabulary", list);
        assertEquals(200, response.getStatus());
        @SuppressWarnings("unchecked")
        List<VocabRecord> rec = (List<VocabRecord>) response.getEntity();
        assertEquals(1, rec.size());
    }

    @Test
    public void testGetVocabularyByName() {
        VocabularyResource res = new VocabularyResource();
        Response response = res.getVocabularyByName("name");
        assertEquals(200, response.getStatus());
        Vocabulary vocab = (Vocabulary) response.getEntity();
        assertNotNull(vocab);
    }

    @Test
    public void testGetRecord() {
        VocabularyResource res = new VocabularyResource();
        Response response = res.getRecord(1, 1);
        VocabRecord rec = (VocabRecord) response.getEntity();
        assertNotNull(rec);
    }

    @Test
    public void testGetRecordAsJskos() throws Exception {
        UriInfo uriInfo = EasyMock.createNiceMock(UriInfo.class);

        EasyMock.expect(uriInfo.getBaseUri()).andReturn(new URI("http://example.com")).anyTimes();

        EasyMock.replay(uriInfo);

        VocabularyResource res = new VocabularyResource();
        Response response = res.getRecordAsJskos(uriInfo, 1, 1);

        JskosRecord rec = (JskosRecord) response.getEntity();

        assertNotNull(rec);
        assertEquals("https://goobi.io/", rec.getContext());

    }

}
