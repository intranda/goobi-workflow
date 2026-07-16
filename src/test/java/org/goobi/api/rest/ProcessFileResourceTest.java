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

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.goobi.beans.Process;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;

@ExtendWith(MockitoExtension.class)
public class ProcessFileResourceTest extends AbstractProcessResourceTest {

    @Test
    public void testGetFileListUnknownProcessReturnsNotFound() {
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(999)).thenReturn(null);

            ProcessFileResource resource = new ProcessFileResource();

            assertEquals(404, resource.getFileList("999", "media").getStatus());
        }
    }

    @Test
    public void testDownloadUnknownProcessReturnsNotFound() {
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(999)).thenReturn(null);

            ProcessFileResource resource = new ProcessFileResource();

            assertEquals(404, resource.downloadFle("999", "media", "img.tif").getStatus());
        }
    }

    @Test
    public void testDeleteUnknownProcessReturnsNotFound() {
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(999)).thenReturn(null);

            ProcessFileResource resource = new ProcessFileResource();

            assertEquals(404, resource.deleteFile("999", "media", "img.tif").getStatus());
        }
    }

    @Test
    public void testUploadUnknownProcessReturnsNotFound() {
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(999)).thenReturn(null);

            ProcessFileResource resource = new ProcessFileResource();

            // the process-not-found guard fires before the multipart parameters are touched,
            // so nulls are safe here
            InputStream fileInputStream = null;
            FormDataContentDisposition fileMetaData = null;
            String filename = null;
            assertEquals(404, resource.uploadFile("999", "media", fileInputStream, fileMetaData, filename).getStatus());
        }
    }

    @Test
    public void testDownloadBlankFolderReturnsBadRequest() {
        Process process = buildProcess(42); // id = 5
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);

            ProcessFileResource resource = new ProcessFileResource();
            resource.setRequest(mockRequestWithoutToken());

            assertEquals(400, resource.downloadFle("5", " ", "img.tif").getStatus());
        }
    }

    @Test
    public void testDeleteBlankFolderReturnsBadRequest() {
        Process process = buildProcess(42); // id = 5
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);

            ProcessFileResource resource = new ProcessFileResource();
            resource.setRequest(mockRequestWithoutToken());

            assertEquals(400, resource.deleteFile("5", " ", "img.tif").getStatus());
        }
    }

    @Test
    public void testGetFileListBlankFolderReturnsBadRequest() {
        Process process = buildProcess(42); // id = 5
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);

            ProcessFileResource resource = new ProcessFileResource();
            resource.setRequest(mockRequestWithoutToken());

            assertEquals(400, resource.getFileList("5", " ").getStatus());
        }
    }

    @Test
    public void testDownloadBlankFileReturnsBadRequest() {
        Process process = buildProcess(42); // id = 5
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);

            ProcessFileResource resource = new ProcessFileResource();
            resource.setRequest(mockRequestWithoutToken());

            assertEquals(400, resource.downloadFle("5", "media", " ").getStatus());
        }
    }

    @Test
    public void testDeleteBlankFileReturnsBadRequest() {
        Process process = buildProcess(42); // id = 5
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);

            ProcessFileResource resource = new ProcessFileResource();
            resource.setRequest(mockRequestWithoutToken());

            assertEquals(400, resource.deleteFile("5", "media", " ").getStatus());
        }
    }

    @Test
    public void testGetFileListNonMemberTokenReturnsForbidden() {
        Process process = buildProcess(42); // id = 5
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> projm = Mockito.mockStatic(ProjectManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            projm.when(() -> ProjectManager.isUserMemberOfProject(7, 42)).thenReturn(false);

            ProcessFileResource resource = new ProcessFileResource();
            resource.setRequest(mockRequestWithToken(buildToken(7)));

            assertEquals(403, resource.getFileList("5", "media").getStatus());
        }
    }
}
