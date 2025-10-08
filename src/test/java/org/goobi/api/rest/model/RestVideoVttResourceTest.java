package org.goobi.api.rest.model;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.metadaten.Metadaten;
import jakarta.ws.rs.core.Response;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ConfigurationHelper.class, StorageProvider.class })
public class RestVideoVttResourceTest {

    private RestVideoVttResource resource;
    private Metadaten metadataBean;

    @Before
    public void setUp() {
        // Mock ConfigurationHelper static call
        ConfigurationHelper confHelper = createMock(ConfigurationHelper.class);
        PowerMock.mockStatic(ConfigurationHelper.class);
        EasyMock.expect(ConfigurationHelper.getInstance()).andReturn(confHelper).anyTimes();
        expect(confHelper.getMetadataFolder()).andReturn("/tmp/metadata").anyTimes();
        PowerMock.replay(ConfigurationHelper.class, confHelper);

        resource = new RestVideoVttResource();
        metadataBean = createMock(Metadaten.class);
        resource.setMetadataBean(metadataBean);
    }

    @Test
    public void testGetChapterVttFileWithMetadataBean() {
        EasyMock.expect(metadataBean.getChapterInformationAsVTT()).andReturn("WEBVTT CONTENT");
        replay(metadataBean);

        Response resp = resource.getChapterVttFile();
        assertEquals(200, resp.getStatus());
        assertTrue(resp.getEntity().toString().contains("WEBVTT CONTENT"));
        assertTrue(resp.getHeaders().getFirst("Content-Disposition").toString().contains("video.vtt"));

        verify(metadataBean);
    }

    @Test
    public void testGetChapterVttFileWithoutMetadataBean() {
        resource.setMetadataBean(null);

        Response resp = resource.getChapterVttFile();
        assertEquals(200, resp.getStatus());
        assertEquals("", resp.getEntity().toString());
    }

    @Test
    public void testGetSubtitleVttFileFileExists() throws Exception {
        // Arrange

        StorageProviderInterface storageProvider = createMock(StorageProviderInterface.class);
        PowerMock.mockStatic(StorageProvider.class);
        EasyMock.expect(StorageProvider.getInstance()).andReturn(storageProvider).anyTimes();

        EasyMock.expect(storageProvider.isFileExists(EasyMock.anyObject())).andReturn(true).once();

        PowerMock.replay(StorageProvider.class, storageProvider);

        // Act
        Response resp = resource.getSubtitleVttFile("123", "folder", "file.vtt");

        // Assert
        assertEquals(200, resp.getStatus());
        assertTrue(resp.getHeaders().getFirst("Content-Disposition").toString().contains("file.vtt"));

        PowerMock.verify(StorageProvider.class, storageProvider);
    }

    @Test
    public void testGetSubtitleVttFileFileDoesNotExist() {
        String process = "999";
        String folder = "notfound";
        String filename = "missing.vtt";
        Path expectedPath = Paths.get("/tmp/metadata/999/ocr/notfound/missing.vtt");

        StorageProviderInterface storageProvider = createMock(StorageProviderInterface.class);
        PowerMock.mockStatic(StorageProvider.class);
        EasyMock.expect(StorageProvider.getInstance()).andReturn(storageProvider).anyTimes();
        EasyMock.expect(storageProvider.isFileExists(expectedPath)).andReturn(false).once();
        PowerMock.replay(StorageProvider.class, storageProvider);

        Response resp = resource.getSubtitleVttFile(process, folder, filename);
        assertEquals(200, resp.getStatus());
        assertEquals("", resp.getEntity().toString());

        PowerMock.verify(StorageProvider.class, storageProvider);
    }

    @Test
    public void testGetFilename() {
        assertEquals("file.txt", resource.getFilename("folder/file.txt"));
        assertEquals("justname", resource.getFilename("justname"));
    }
}
