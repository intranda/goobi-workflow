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

package org.goobi.api.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.goobi.beans.Process;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.persistence.managers.ProcessManager;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

public class ProcessFileArchiveTest extends AbstractTest {

    @Test
    public void testArchiveUnknownProcess() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(999999999)).thenReturn(null);

            ProcessFileResource r = new ProcessFileResource();
            Response resp = r.downloadFolderAsArchive("999999999", "master");
            assertEquals(404, resp.getStatus());
        }
    }

    @Test
    public void testArchiveReturnsZipWithRegularFiles() throws Exception {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class)) {

            Process process = Mockito.spy(new Process());
            process.setId(1);
            process.setTitel("testprocess");
            Mockito.doReturn("/some/folder/").when(process).getConfiguredImageFolder("master");

            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            StorageProviderInterface storageProvider = Mockito.mock(StorageProviderInterface.class);
            mockedStorageProvider.when(StorageProvider::getInstance).thenReturn(storageProvider);

            Mockito.when(storageProvider.isFileExists(Mockito.any())).thenReturn(true);

            Path file1 = Paths.get("/some/folder/file1.tif");
            Path file2 = Paths.get("/some/folder/file2.tif");
            List<Path> files = new ArrayList<>();
            files.add(file1);
            files.add(file2);
            Mockito.when(storageProvider.listFiles("/some/folder/")).thenReturn(files);

            Mockito.when(storageProvider.isDirectory(file1)).thenReturn(false);
            Mockito.when(storageProvider.isDirectory(file2)).thenReturn(false);

            byte[] content1 = "content-of-file1".getBytes(StandardCharsets.UTF_8);
            byte[] content2 = "content-of-file2".getBytes(StandardCharsets.UTF_8);
            Mockito.when(storageProvider.newInputStream(file1)).thenReturn(new ByteArrayInputStream(content1));
            Mockito.when(storageProvider.newInputStream(file2)).thenReturn(new ByteArrayInputStream(content2));

            ProcessFileResource r = new ProcessFileResource();
            Response resp = r.downloadFolderAsArchive("1", "master");
            assertEquals(200, resp.getStatus());

            StreamingOutput stream = (StreamingOutput) resp.getEntity();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            stream.write(baos);

            List<String> entryNames = new ArrayList<>();
            List<String> entryContents = new ArrayList<>();
            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    entryNames.add(entry.getName());
                    ByteArrayOutputStream entryContent = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = zis.read(buffer)) != -1) {
                        entryContent.write(buffer, 0, read);
                    }
                    entryContents.add(entryContent.toString(StandardCharsets.UTF_8));
                    zis.closeEntry();
                }
            }

            assertEquals(2, entryNames.size());
            assertEquals("file1.tif", entryNames.get(0));
            assertEquals("file2.tif", entryNames.get(1));
            assertEquals("content-of-file1", entryContents.get(0));
            assertEquals("content-of-file2", entryContents.get(1));
        }
    }

    @Test
    public void testArchiveReturnsNoContentWhenOnlyDirectoriesPresent() throws Exception {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class)) {

            Process process = Mockito.spy(new Process());
            process.setId(1);
            process.setTitel("testprocess");
            Mockito.doReturn("/some/folder/").when(process).getConfiguredImageFolder("master");

            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            StorageProviderInterface storageProvider = Mockito.mock(StorageProviderInterface.class);
            mockedStorageProvider.when(StorageProvider::getInstance).thenReturn(storageProvider);

            Mockito.when(storageProvider.isFileExists(Mockito.any())).thenReturn(true);

            Path subFolder = Paths.get("/some/folder/subfolder");
            List<Path> files = new ArrayList<>();
            files.add(subFolder);
            Mockito.when(storageProvider.listFiles("/some/folder/")).thenReturn(files);
            Mockito.when(storageProvider.isDirectory(subFolder)).thenReturn(true);

            ProcessFileResource r = new ProcessFileResource();
            Response resp = r.downloadFolderAsArchive("1", "master");
            assertEquals(204, resp.getStatus());
        }
    }
}
