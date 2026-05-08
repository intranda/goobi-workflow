package de.sub.goobi.metadaten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.helper.Helper;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.TypeNotAllowedAsChildException;

@ExtendWith(MockitoExtension.class)
public class VideoSectionManagerTest {

    private Prefs prefs;
    private DigitalDocument document;
    private VideoSectionManager manager;

    @BeforeEach
    public void setUp() {
        prefs = Mockito.mock(Prefs.class);
        document = Mockito.mock(DigitalDocument.class);
        manager = new VideoSectionManager(prefs, document);
    }

    @Test
    public void testResetAndHasNewPageArea() {
        DocStruct mockStruct = Mockito.mock(DocStruct.class);
        manager.setNewPageArea(mockStruct);
        assertTrue(manager.hasNewPageArea());

        manager.resetNewPageArea();
        assertFalse(manager.hasNewPageArea());
    }

    @Test
    public void testCreatePhysicalObject() {
        DocStruct page = Mockito.mock(DocStruct.class);
        DocStruct parent = Mockito.mock(DocStruct.class);

        Metadata md = Mockito.mock(Metadata.class);
        MetadataType mdType = Mockito.mock(MetadataType.class);
        List<Metadata> mdl = new ArrayList<>();
        mdl.add(md);

        Mockito.when(page.getParent()).thenReturn(parent);
        Mockito.when(parent.getAllMetadata()).thenReturn(Arrays.asList(md));
        Mockito.when(page.getDocstructType()).thenReturn("area");
        Mockito.when(page.getAllMetadata()).thenReturn(mdl);
        Mockito.when(md.getType()).thenReturn(mdType);
        Mockito.when(mdType.getName()).thenReturn("physPageNumber");
        Mockito.when(md.getValue()).thenReturn("5");
        Mockito.when(parent.getAllChildren()).thenReturn(Collections.singletonList(page));

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

        DocStruct area = Mockito.mock(DocStruct.class);
        manager.setNewPageArea(area);

        try (MockedStatic<MetadatenHelper> mockedMetadatenHelper = Mockito.mockStatic(MetadatenHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {

            mockedMetadatenHelper.when(() -> MetadatenHelper.getSingleMetadataValue(area, "logicalPageNumber"))
                    .thenReturn(Optional.of("X"));
            mockedHelper.when(() -> Helper.getTranslation("mets_pageArea", "X")).thenReturn("LABEL");

            String label = manager.getNewPageAreaLabel();
            assertEquals("LABEL", label);
        }
    }

    //    @Test
    public void testCreateVideoSection() throws Exception {
        DocStruct page = Mockito.mock(DocStruct.class);
        DocStructType dst = Mockito.mock(DocStructType.class);
        DocStruct pageArea = Mockito.mock(DocStruct.class);

        MetadataType mtLogical = Mockito.mock(MetadataType.class);
        MetadataType mtPhysical = Mockito.mock(MetadataType.class);
        MetadataType mtBegin = Mockito.mock(MetadataType.class);
        MetadataType mtEnd = Mockito.mock(MetadataType.class);
        MetadataType mtBeType = Mockito.mock(MetadataType.class);

        Mockito.when(page.getAllChildren()).thenReturn(null);
        Mockito.when(prefs.getDocStrctTypeByName("area")).thenReturn(dst);
        Mockito.when(document.createDocStruct(dst)).thenReturn(pageArea);
        Mockito.when(prefs.getMetadataTypeByName("logicalPageNumber")).thenReturn(mtLogical);
        Mockito.when(prefs.getMetadataTypeByName("physPageNumber")).thenReturn(mtPhysical);
        Mockito.when(prefs.getMetadataTypeByName("_BEGIN")).thenReturn(mtBegin);
        Mockito.when(prefs.getMetadataTypeByName("_END")).thenReturn(mtEnd);
        Mockito.when(prefs.getMetadataTypeByName("_BETYPE")).thenReturn(mtBeType);
        // Metadata mocks: allow setValue + addMetadata calls
        Metadata m1 = Mockito.mock(Metadata.class);
        Metadata m2 = Mockito.mock(Metadata.class);
        Metadata m3 = Mockito.mock(Metadata.class);
        Metadata m4 = Mockito.mock(Metadata.class);
        Metadata m5 = Mockito.mock(Metadata.class);

        // TODO: PowerMock.expectNew needs manual migration to Mockito.mockConstruction
        // PowerMock.expectNew(Metadata.class, mtLogical).andReturn(m1);
        // TODO: PowerMock.expectNew needs manual migration to Mockito.mockConstruction
        // PowerMock.expectNew(Metadata.class, mtPhysical).andReturn(m2);
        // TODO: PowerMock.expectNew needs manual migration to Mockito.mockConstruction
        // PowerMock.expectNew(Metadata.class, mtBegin).andReturn(m3);
        // TODO: PowerMock.expectNew needs manual migration to Mockito.mockConstruction
        // PowerMock.expectNew(Metadata.class, mtEnd).andReturn(m4);
        // TODO: PowerMock.expectNew needs manual migration to Mockito.mockConstruction
        // PowerMock.expectNew(Metadata.class, mtBeType).andReturn(m5);
        Mockito.when(m1.getType()).thenReturn(mtLogical);
        Mockito.when(m2.getType()).thenReturn(mtPhysical);
        Mockito.when(m3.getType()).thenReturn(mtBegin);
        Mockito.when(m4.getType()).thenReturn(mtEnd);
        Mockito.when(m5.getType()).thenReturn(mtBeType);

        Mockito.when(mtLogical.getName()).thenReturn("logicalPageNumber");
        Mockito.when(mtPhysical.getName()).thenReturn("physPageNumber");
        Mockito.when(mtBegin.getName()).thenReturn("_BEGIN");
        Mockito.when(mtEnd.getName()).thenReturn("_END");
        Mockito.when(mtBeType.getName()).thenReturn("_BETYPE");

        Mockito.when(m1.getValue()).thenReturn("1");
        Mockito.when(m2.getValue()).thenReturn("1");

        m1.setValue(Mockito.anyString());
        m2.setValue(Mockito.anyString());
        m3.setValue("start");
        m4.setValue("end");
        m5.setValue("TIME");
        List<Metadata> mdl = new ArrayList<>();
        mdl.add(m1);
        mdl.add(m2);
        mdl.add(m3);
        mdl.add(m4);
        mdl.add(m5);

        pageArea.addMetadata(Mockito.any());
        pageArea.setDocstructType("area");
        pageArea.setIdentifier(Mockito.anyString());
        Mockito.when(page.getAllMetadata()).thenReturn(mdl);

        DocStruct result = manager.createVideoSection(page, "start", "end");
        assertNotNull(result);

    }

    @Test
    public void testAssignToPhysicalDocStructWithException() throws Exception {
        DocStruct page = Mockito.mock(DocStruct.class);
        DocStruct area = Mockito.mock(DocStruct.class);

        Mockito.doThrow(new TypeNotAllowedAsChildException("test")).when(page).addChild(area);

        // Should catch exception and not throw
        manager.assignToPhysicalDocStruct(area, page);

    }

}
