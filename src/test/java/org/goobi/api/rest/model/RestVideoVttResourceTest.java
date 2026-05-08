package org.goobi.api.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.metadaten.Metadaten;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
public class RestVideoVttResourceTest {

    private RestVideoVttResource resource;
    private Metadaten metadataBean;

    private ConfigurationHelper confHelper;

    @BeforeEach
    public void setUp() {
        confHelper = Mockito.mock(ConfigurationHelper.class);
        Mockito.lenient().when(confHelper.getMetadataFolder()).thenReturn("/tmp/metadata");

        resource = new RestVideoVttResource();
        metadataBean = Mockito.mock(Metadaten.class);
        resource.setMetadataBean(metadataBean);
    }

    @Test
    public void testGetChapterVttFileWithMetadataBean() {
        try (MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class)) {
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(confHelper);

            Mockito.when(metadataBean.getChapterInformationAsVTT()).thenReturn("WEBVTT CONTENT");

            Response resp = resource.getChapterVttFile();
            assertEquals(200, resp.getStatus());
            assertTrue(resp.getEntity().toString().contains("WEBVTT CONTENT"));
            assertTrue(resp.getHeaders().getFirst("Content-Disposition").toString().contains("video.vtt"));

            Mockito.verify(metadataBean).getChapterInformationAsVTT();
        }
    }

    @Test
    public void testGetChapterVttFileWithoutMetadataBean() {
        try (MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class)) {
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(confHelper);

            resource.setMetadataBean(null);

            Response resp = resource.getChapterVttFile();
            assertEquals(200, resp.getStatus());
            assertEquals("", resp.getEntity().toString());
        }
    }

    @Test
    public void testGetSubtitleVttFileFileExists() throws Exception {
        try (MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class)) {
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(confHelper);

            StorageProviderInterface storageProvider = Mockito.mock(StorageProviderInterface.class);

            try (MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class)) {
                mockedStorageProvider.when(() -> StorageProvider.getInstance()).thenReturn(storageProvider);

                Mockito.when(storageProvider.isFileExists(Mockito.any())).thenReturn(true);

                Response resp = resource.getSubtitleVttFile("123", "folder", "file.vtt");

                assertEquals(200, resp.getStatus());
                assertTrue(resp.getHeaders().getFirst("Content-Disposition").toString().contains("file.vtt"));
            }
        }
    }

    @Test
    public void testGetSubtitleVttFileFileDoesNotExist() {
        try (MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class)) {
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(confHelper);

            String process = "999";
            String folder = "notfound";
            String filename = "missing.vtt";

            StorageProviderInterface storageProvider = Mockito.mock(StorageProviderInterface.class);

            try (MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class)) {
                mockedStorageProvider.when(() -> StorageProvider.getInstance()).thenReturn(storageProvider);
                Mockito.when(storageProvider.isFileExists(Mockito.any())).thenReturn(false);

                Response resp = resource.getSubtitleVttFile(process, folder, filename);
                assertEquals(200, resp.getStatus());
                assertEquals("", resp.getEntity().toString());
            }
        }
    }

    @Test
    public void testGetFilename() {
        try (MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class)) {
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(confHelper);

            assertEquals("file.txt", resource.getFilename("folder/file.txt"));
            assertEquals("justname", resource.getFilename("justname"));
        }
    }
}
