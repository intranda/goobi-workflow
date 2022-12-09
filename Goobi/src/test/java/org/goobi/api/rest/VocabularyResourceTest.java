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
        EasyMock.expect(VocabularyManager.findRecords(EasyMock.anyString(), EasyMock.anyObject(List.class))).andReturn(records).anyTimes();

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
        List<VocabRecord> rec = (List<VocabRecord>) response.getEntity();
        assertEquals(1, rec.size());
    }

    @Test
    public void testVocabularyFieldnameSearchValue() {
        VocabularyResource res = new VocabularyResource();
        Response response = res.findRecords("vocabulary", "fieldname", "searchvalue");
        assertEquals(200, response.getStatus());
        List<VocabRecord> rec = (List<VocabRecord>) response.getEntity();
        assertEquals(1, rec.size());

    }

    @Test
    public void testVocabulary() {
        VocabularyResource res = new VocabularyResource();
        List<StringPair> list = new ArrayList<>();
        Response response = res.findRecords("vocabulary", list);
        assertEquals(200, response.getStatus());
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
