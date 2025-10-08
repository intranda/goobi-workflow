package de.sub.goobi.metadaten;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.helper.Helper;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.TypeNotAllowedAsChildException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Helper.class, MetadatenHelper.class })
public class VideoSectionManagerTest {

    private Prefs prefs;
    private DigitalDocument document;
    private VideoSectionManager manager;

    @Before
    public void setUp() {
        prefs = createMock(Prefs.class);
        document = createMock(DigitalDocument.class);
        manager = new VideoSectionManager(prefs, document);
    }

    @Test
    public void testResetAndHasNewPageArea() {
        DocStruct mockStruct = createMock(DocStruct.class);
        manager.setNewPageArea(mockStruct);
        assertTrue(manager.hasNewPageArea());

        manager.resetNewPageArea();
        assertFalse(manager.hasNewPageArea());
    }

    @Test
    public void testCreatePhysicalObject() {
        DocStruct page = createMock(DocStruct.class);
        DocStruct parent = createMock(DocStruct.class);

        Metadata md = createMock(Metadata.class);
        MetadataType mdType = createMock(MetadataType.class);
        List<Metadata> mdl = new ArrayList<>();
        mdl.add(md);

        expect(page.getParent()).andReturn(parent).anyTimes();
        expect(parent.getAllMetadata()).andReturn(Arrays.asList(md)).anyTimes();
        expect(page.getDocstructType()).andReturn("area").anyTimes();
        expect(page.getAllMetadata()).andReturn(mdl).anyTimes();
        expect(md.getType()).andReturn(mdType).anyTimes();
        expect(mdType.getName()).andReturn("physPageNumber").anyTimes();
        expect(md.getValue()).andReturn("5").anyTimes();
        expect(parent.getAllChildren()).andReturn(Collections.singletonList(page)).anyTimes();
        replay(page, parent, md, mdType);

        manager.setNewPageArea(page);
        PhysicalObject po = manager.createPhysicalObject(page);
        assertNotNull(po);
        assertEquals("5_1", po.getPhysicalPageNo());
        // logicalPageNo throws IllegalArgumentException (not "area")
        // handled inside method
        assertNotNull(po.getLogicalPageNo());
    }

    @Test
    public void testGetNewPageAreaLabel() {
        DocStruct area = createMock(DocStruct.class);
        manager.setNewPageArea(area);

        PowerMock.mockStatic(MetadatenHelper.class);
        PowerMock.mockStatic(Helper.class);

        expect(MetadatenHelper.getSingleMetadataValue(area, "logicalPageNumber"))
                .andReturn(Optional.of("X"))
                .once();
        expect(Helper.getTranslation(eq("mets_pageArea"), eq("X"))).andReturn("LABEL").once();

        PowerMock.replay(MetadatenHelper.class, Helper.class);

        String label = manager.getNewPageAreaLabel();
        assertEquals("LABEL", label);

        PowerMock.verify(MetadatenHelper.class, Helper.class);
    }

    //    @Test
    public void testCreateVideoSection() throws Exception {
        DocStruct page = createMock(DocStruct.class);
        DocStructType dst = createMock(DocStructType.class);
        DocStruct pageArea = createMock(DocStruct.class);

        MetadataType mtLogical = createMock(MetadataType.class);
        MetadataType mtPhysical = createMock(MetadataType.class);
        MetadataType mtBegin = createMock(MetadataType.class);
        MetadataType mtEnd = createMock(MetadataType.class);
        MetadataType mtBeType = createMock(MetadataType.class);

        expect(page.getAllChildren()).andReturn(null).anyTimes();
        expect(prefs.getDocStrctTypeByName("area")).andReturn(dst).once();
        expect(document.createDocStruct(dst)).andReturn(pageArea).once();
        expect(prefs.getMetadataTypeByName("logicalPageNumber")).andReturn(mtLogical).once();
        expect(prefs.getMetadataTypeByName("physPageNumber")).andReturn(mtPhysical).once();
        expect(prefs.getMetadataTypeByName("_BEGIN")).andReturn(mtBegin).once();
        expect(prefs.getMetadataTypeByName("_END")).andReturn(mtEnd).anyTimes();
        expect(prefs.getMetadataTypeByName("_BETYPE")).andReturn(mtBeType).once();
        // Metadata mocks: allow setValue + addMetadata calls
        Metadata m1 = createMock(Metadata.class);
        Metadata m2 = createMock(Metadata.class);
        Metadata m3 = createMock(Metadata.class);
        Metadata m4 = createMock(Metadata.class);
        Metadata m5 = createMock(Metadata.class);

        PowerMock.expectNew(Metadata.class, mtLogical).andReturn(m1);
        PowerMock.expectNew(Metadata.class, mtPhysical).andReturn(m2);
        PowerMock.expectNew(Metadata.class, mtBegin).andReturn(m3);
        PowerMock.expectNew(Metadata.class, mtEnd).andReturn(m4);
        PowerMock.expectNew(Metadata.class, mtBeType).andReturn(m5);
        expect(m1.getType()).andReturn(mtLogical).anyTimes();
        expect(m2.getType()).andReturn(mtPhysical).anyTimes();
        expect(m3.getType()).andReturn(mtBegin).anyTimes();
        expect(m4.getType()).andReturn(mtEnd).anyTimes();
        expect(m5.getType()).andReturn(mtBeType).anyTimes();

        expect(mtLogical.getName()).andReturn("logicalPageNumber").anyTimes();
        expect(mtPhysical.getName()).andReturn("physPageNumber").anyTimes();
        expect(mtBegin.getName()).andReturn("_BEGIN").anyTimes();
        expect(mtEnd.getName()).andReturn("_END").anyTimes();
        expect(mtBeType.getName()).andReturn("_BETYPE").anyTimes();

        expect(m1.getValue()).andReturn("1").anyTimes();
        expect(m2.getValue()).andReturn("1").anyTimes();

        m1.setValue(anyString());
        expectLastCall().anyTimes();
        m2.setValue(anyString());
        expectLastCall().anyTimes();
        m3.setValue("start");
        expectLastCall();
        m4.setValue("end");
        expectLastCall();
        m5.setValue("TIME");
        expectLastCall();
        List<Metadata> mdl = new ArrayList<>();
        mdl.add(m1);
        mdl.add(m2);
        mdl.add(m3);
        mdl.add(m4);
        mdl.add(m5);

        pageArea.addMetadata(anyObject(Metadata.class));
        expectLastCall().anyTimes();
        pageArea.setDocstructType("area");
        expectLastCall();
        pageArea.setIdentifier(anyString());
        expectLastCall();
        expect(page.getAllMetadata()).andReturn(mdl).anyTimes();
        replay(prefs, document, page, dst, pageArea,
                mtLogical, mtPhysical, mtBegin, mtEnd, mtBeType,
                m1, m2, m3, m4, m5);

        PowerMock.replay(Metadata.class);

        DocStruct result = manager.createVideoSection(page, "start", "end");
        assertNotNull(result);

        verify(prefs, document, pageArea);
        PowerMock.verify(Metadata.class);
    }

    @Test
    public void testAssignToPhysicalDocStructWithException() throws Exception {
        DocStruct page = createMock(DocStruct.class);
        DocStruct area = createMock(DocStruct.class);

        page.addChild(area);
        expectLastCall().andThrow(new TypeNotAllowedAsChildException("fail"));

        replay(page, area);

        // Should catch exception and not throw
        manager.assignToPhysicalDocStruct(area, page);

        verify(page, area);
    }

}
