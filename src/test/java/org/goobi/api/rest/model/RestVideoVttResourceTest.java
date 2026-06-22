package org.goobi.api.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.goobi.beans.Process;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.persistence.managers.ProcessManager;
import jakarta.ws.rs.core.Response;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;

@ExtendWith(MockitoExtension.class)
public class RestVideoVttResourceTest {

    private RestVideoVttResource resource;
    private ConfigurationHelper confHelper;

    @BeforeEach
    public void setUp() {
        confHelper = Mockito.mock(ConfigurationHelper.class);
        Mockito.lenient().when(confHelper.getMetadataFolder()).thenReturn("/tmp/metadata");
        resource = new RestVideoVttResource();
    }

    @Test
    public void testGetChapterVttFileInvalidProcessId() {
        Response resp = resource.getChapterVttFile("notAnInt", "video.mp4");
        assertEquals(400, resp.getStatus());
    }

    @Test
    public void testGetChapterVttFileProcessNotFound() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(999)).thenReturn(null);
            Response resp = resource.getChapterVttFile("999", "video.mp4");
            assertEquals(404, resp.getStatus());
        }
    }

    @Test
    public void testGetChapterVttFileNoPages() throws Exception {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            Process process = Mockito.mock(Process.class);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            Fileformat ff = Mockito.mock(Fileformat.class);
            DigitalDocument dd = Mockito.mock(DigitalDocument.class);
            DocStruct physTopStruct = Mockito.mock(DocStruct.class);

            Mockito.when(process.readMetadataFile()).thenReturn(ff);
            Mockito.when(ff.getDigitalDocument()).thenReturn(dd);
            Mockito.when(dd.getPhysicalDocStruct()).thenReturn(physTopStruct);
            Mockito.when(physTopStruct.getAllChildren()).thenReturn(null);

            Response resp = resource.getChapterVttFile("1", "video.mp4");
            assertEquals(200, resp.getStatus());
            assertEquals("WEBVTT\n\n", resp.getEntity().toString());
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

            StorageProviderInterface storageProvider = Mockito.mock(StorageProviderInterface.class);

            try (MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class)) {
                mockedStorageProvider.when(() -> StorageProvider.getInstance()).thenReturn(storageProvider);
                Mockito.when(storageProvider.isFileExists(Mockito.any())).thenReturn(false);

                Response resp = resource.getSubtitleVttFile("999", "notfound", "missing.vtt");
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
