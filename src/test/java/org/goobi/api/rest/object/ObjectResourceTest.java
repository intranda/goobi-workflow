package org.goobi.api.rest.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.goobi.beans.Process;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.StreamingOutput;

@ExtendWith(MockitoExtension.class)
public class ObjectResourceTest {

    private static final String FOLDERNAME_RESOURCES = "object";

    private static final String CONTENT_GLTF = "gltf-file";

    private static final String CONTENT_BIN = "data-file";

    private static final String CONTENT_SURFACE = "surface-file";

    private static final String FILENAME_GLTF = "object.gltf";

    private static final String FILENAME_BIN = "data.bin";

    private static final String FILENAME_SURFACE = "surface.jpg";

    private static final String FOLDERNAME_MASTER = "processname_master";

    private static final String FOLDERNAME_IMAGES = "images";

    private static final String FOLDERNAME_RESOURCES_IMAGES = "images";

    private static final int PROCESS_ID = 11;

    private ObjectResource objectResource = new ObjectResource();
    private Path tempProcessPath;

    @BeforeEach
    public void before() throws IOException {
        tempProcessPath = Files.createTempDirectory("Goobi_workflow_ObjectResourceTest_");
    }

    @AfterEach
    public void after() throws IOException {
        FileUtils.forceDelete(tempProcessPath.toFile());
    }

    @Test
    public void testGetObject() throws IOException, InterruptedException, SwapException, DAOException {

        Path objectFilePath = tempProcessPath.resolve(Path.of(FOLDERNAME_IMAGES, FOLDERNAME_MASTER, FILENAME_GLTF));
        Files.createDirectories(objectFilePath.getParent());
        Files.createFile(objectFilePath);
        Files.writeString(objectFilePath, CONTENT_GLTF);
        assertEquals(CONTENT_GLTF, Files.readString(objectFilePath));

        Process process = Mockito.mock(Process.class);
        Mockito.when(process.getImagesDirectory()).thenReturn(objectFilePath.getParent().getParent().toString());

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(PROCESS_ID)).thenReturn(process);

            HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
            HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
            StreamingOutput output = objectResource.getObject(request, response, PROCESS_ID, FOLDERNAME_MASTER, FILENAME_GLTF);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                output.write(out);
                assertEquals(CONTENT_GLTF, out.toString(Charset.defaultCharset()));
            }
        }
    }

    @Test
    public void testGetObjectResource() throws IOException, InterruptedException, SwapException, DAOException {

        Path objectFilePath = tempProcessPath.resolve(Path.of(FOLDERNAME_IMAGES, FOLDERNAME_MASTER, FOLDERNAME_RESOURCES, FILENAME_BIN));
        Files.createDirectories(objectFilePath.getParent());
        Files.createFile(objectFilePath);
        Files.writeString(objectFilePath, CONTENT_BIN);
        assertEquals(CONTENT_BIN, Files.readString(objectFilePath));

        Process process = Mockito.mock(Process.class);
        Mockito.when(process.getImagesDirectory()).thenReturn(objectFilePath.getParent().getParent().getParent().toString());

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(PROCESS_ID)).thenReturn(process);

            HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
            HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
            StreamingOutput output =
                    objectResource.getObjectResource(request, response, PROCESS_ID, FOLDERNAME_MASTER, FOLDERNAME_RESOURCES, FILENAME_BIN);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                output.write(out);
                assertEquals(CONTENT_BIN, out.toString(Charset.defaultCharset()));
            }
        }
    }

    @Test
    public void testGetSecondaryObjectResource() throws IOException, InterruptedException, SwapException, DAOException {

        Path objectFilePath = tempProcessPath
                .resolve(Path.of(FOLDERNAME_IMAGES, FOLDERNAME_MASTER, FOLDERNAME_RESOURCES, FOLDERNAME_RESOURCES_IMAGES, FILENAME_SURFACE));
        Files.createDirectories(objectFilePath.getParent());
        Files.createFile(objectFilePath);
        Files.writeString(objectFilePath, CONTENT_SURFACE);
        assertEquals(CONTENT_SURFACE, Files.readString(objectFilePath));

        Process process = Mockito.mock(Process.class);
        Mockito.when(process.getImagesDirectory()).thenReturn(objectFilePath.getParent().getParent().getParent().getParent().toString());

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(PROCESS_ID)).thenReturn(process);

            HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
            HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
            StreamingOutput output = objectResource.getObjectResource(request, response, PROCESS_ID, FOLDERNAME_MASTER, FOLDERNAME_RESOURCES,
                    FOLDERNAME_RESOURCES_IMAGES, FILENAME_SURFACE);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                output.write(out);
                assertEquals(CONTENT_SURFACE, out.toString(Charset.defaultCharset()));
            }
        }
    }

}
