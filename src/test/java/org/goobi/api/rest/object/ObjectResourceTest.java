package org.goobi.api.rest.object;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.h2.store.fs.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.StreamingOutput;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ProcessManager.class)
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
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

    @Before
    public void before() throws IOException {
        tempProcessPath = Files.createTempDirectory("Goobi_workflow_ObjectResourceTest_");
    }

    @After
    public void after() {
        FileUtils.deleteRecursive(tempProcessPath.toString(), false);
    }

    @Test
    public void testGetObject() throws IOException, InterruptedException, SwapException, DAOException {

        Path objectFilePath = tempProcessPath.resolve(Path.of(FOLDERNAME_IMAGES, FOLDERNAME_MASTER, FILENAME_GLTF));
        Files.createDirectories(objectFilePath.getParent());
        Files.createFile(objectFilePath);
        Files.writeString(objectFilePath, CONTENT_GLTF);
        assertEquals(CONTENT_GLTF, Files.readString(objectFilePath));

        Process process = PowerMock.createMock(Process.class);
        EasyMock.expect(process.getImagesDirectory()).andReturn(objectFilePath.getParent().getParent().toString()).anyTimes();

        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(PROCESS_ID)).andReturn(process);
        PowerMock.replayAll();

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        StreamingOutput output = objectResource.getObject(request, response, PROCESS_ID, FOLDERNAME_MASTER, FILENAME_GLTF);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            output.write(out);
            assertEquals(CONTENT_GLTF, out.toString(Charset.defaultCharset()));
        }
    }

    @Test
    public void testGetObjectResource() throws IOException, InterruptedException, SwapException, DAOException {

        Path objectFilePath = tempProcessPath.resolve(Path.of(FOLDERNAME_IMAGES, FOLDERNAME_MASTER, FOLDERNAME_RESOURCES, FILENAME_BIN));
        Files.createDirectories(objectFilePath.getParent());
        Files.createFile(objectFilePath);
        Files.writeString(objectFilePath, CONTENT_BIN);
        assertEquals(CONTENT_BIN, Files.readString(objectFilePath));

        Process process = PowerMock.createMock(Process.class);
        EasyMock.expect(process.getImagesDirectory()).andReturn(objectFilePath.getParent().getParent().getParent().toString()).anyTimes();

        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(PROCESS_ID)).andReturn(process);
        PowerMock.replayAll();

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        StreamingOutput output =
                objectResource.getObjectResource(request, response, PROCESS_ID, FOLDERNAME_MASTER, FOLDERNAME_RESOURCES, FILENAME_BIN);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            output.write(out);
            assertEquals(CONTENT_BIN, out.toString(Charset.defaultCharset()));
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

        Process process = PowerMock.createMock(Process.class);
        EasyMock.expect(process.getImagesDirectory()).andReturn(objectFilePath.getParent().getParent().getParent().getParent().toString()).anyTimes();

        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(PROCESS_ID)).andReturn(process);
        PowerMock.replayAll();

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
