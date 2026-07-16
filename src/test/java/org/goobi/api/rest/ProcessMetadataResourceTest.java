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

import java.util.ArrayList;
import java.util.List;

import org.goobi.api.rest.model.RestMetadataResource;
import org.goobi.beans.Process;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import jakarta.ws.rs.core.Response;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;

@ExtendWith(MockitoExtension.class)
public class ProcessMetadataResourceTest extends AbstractProcessResourceTest {

    @Test
    public void testGetMetadataUnknownProcessReturnsNotFound() {
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(999)).thenReturn(null);

            ProcessMetadataResource resource = new ProcessMetadataResource();

            assertEquals(404, resource.getMetadata("999").getStatus());
        }
    }

    @Test
    public void testGetMetadataNonMemberTokenReturnsForbidden() {
        Process process = buildProcess(42);
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> projm = Mockito.mockStatic(ProjectManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            projm.when(() -> ProjectManager.isUserMemberOfProject(7, 42)).thenReturn(false);

            ProcessMetadataResource resource = new ProcessMetadataResource();
            resource.setRequest(mockRequestWithToken(buildToken(7)));

            assertEquals(403, resource.getMetadata("5").getStatus());
        }
    }

    @Test
    public void testUpdateMetadataUnknownProcessReturnsNotFound() {
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(999)).thenReturn(null);

            RestMetadataResource body = new RestMetadataResource();
            body.setName("TitleDocMain");
            body.setMetadataLevel("topstruct");

            ProcessMetadataResource resource = new ProcessMetadataResource();

            assertEquals(404, resource.updateMetadata("999", body).getStatus());
        }
    }

    @Test
    public void testUpdateMetadataNonMemberTokenReturnsForbidden() {
        Process process = buildProcess(42);
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> projm = Mockito.mockStatic(ProjectManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            projm.when(() -> ProjectManager.isUserMemberOfProject(7, 42)).thenReturn(false);

            RestMetadataResource body = new RestMetadataResource();
            body.setName("TitleDocMain");
            body.setMetadataLevel("topstruct");

            ProcessMetadataResource resource = new ProcessMetadataResource();
            resource.setRequest(mockRequestWithToken(buildToken(7)));

            assertEquals(403, resource.updateMetadata("5", body).getStatus());
        }
    }

    @Test
    public void testCreateMetadataUnknownProcessReturnsNotFound() {
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(999)).thenReturn(null);

            RestMetadataResource body = new RestMetadataResource();
            body.setName("TitleDocMain");
            body.setMetadataLevel("topstruct");

            ProcessMetadataResource resource = new ProcessMetadataResource();

            assertEquals(404, resource.createMetadata("999", body).getStatus());
        }
    }

    @Test
    public void testCreateMetadataNonMemberTokenReturnsForbidden() {
        Process process = buildProcess(42);
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> projm = Mockito.mockStatic(ProjectManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            projm.when(() -> ProjectManager.isUserMemberOfProject(7, 42)).thenReturn(false);

            RestMetadataResource body = new RestMetadataResource();
            body.setName("TitleDocMain");
            body.setMetadataLevel("topstruct");

            ProcessMetadataResource resource = new ProcessMetadataResource();
            resource.setRequest(mockRequestWithToken(buildToken(7)));

            assertEquals(403, resource.createMetadata("5", body).getStatus());
        }
    }

    @Test
    public void testDeleteMetadataMissingNameReturnsBadRequest() {
        RestMetadataResource body = new RestMetadataResource();
        body.setMetadataLevel("topstruct");
        Response response = new ProcessMetadataResource().deleteMetadata("5", body);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testDeleteMetadataUnknownProcessReturnsNotFound() {
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(999)).thenReturn(null);

            RestMetadataResource body = new RestMetadataResource();
            body.setName("TitleDocMain");
            body.setMetadataLevel("topstruct");

            ProcessMetadataResource resource = new ProcessMetadataResource();

            assertEquals(404, resource.deleteMetadata("999", body).getStatus());
        }
    }

    @Test
    public void testDeleteMetadataNonMemberTokenReturnsForbidden() {
        Process process = buildProcess(42);
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> projm = Mockito.mockStatic(ProjectManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            projm.when(() -> ProjectManager.isUserMemberOfProject(7, 42)).thenReturn(false);

            RestMetadataResource body = new RestMetadataResource();
            body.setName("TitleDocMain");
            body.setMetadataLevel("topstruct");

            ProcessMetadataResource resource = new ProcessMetadataResource();
            resource.setRequest(mockRequestWithToken(buildToken(7)));

            assertEquals(403, resource.deleteMetadata("5", body).getStatus());
        }
    }

    @Test
    public void testDeleteMetadataAnchorUsesFirstChildNotProcessIdAsIndex() throws Exception {
        Process process = Mockito.spy(buildProcess(42));

        MetadataType metadataType = Mockito.mock(MetadataType.class);
        Mockito.when(metadataType.getName()).thenReturn("TitleDocMain");

        Metadata metadata = Mockito.mock(Metadata.class);
        Mockito.when(metadata.getType()).thenReturn(metadataType);

        List<Metadata> childMetadata = new ArrayList<>();
        childMetadata.add(metadata);

        DocStruct anchorChild = Mockito.mock(DocStruct.class);
        Mockito.when(anchorChild.getAllMetadata()).thenReturn(childMetadata);
        Mockito.when(anchorChild.getAllPersons()).thenReturn(null);
        Mockito.when(anchorChild.getAllCorporates()).thenReturn(null);

        List<DocStruct> children = new ArrayList<>();
        children.add(anchorChild);

        DocStructType anchorType = Mockito.mock(DocStructType.class);
        Mockito.when(anchorType.isAnchor()).thenReturn(true);

        DocStruct logical = Mockito.mock(DocStruct.class);
        Mockito.when(logical.getType()).thenReturn(anchorType);
        Mockito.when(logical.getAllChildren()).thenReturn(children);

        DigitalDocument digitalDocument = Mockito.mock(DigitalDocument.class);
        Mockito.when(digitalDocument.getLogicalDocStruct()).thenReturn(logical);

        Fileformat fileformat = Mockito.mock(Fileformat.class);
        Mockito.when(fileformat.getDigitalDocument()).thenReturn(digitalDocument);

        Mockito.doReturn(fileformat).when(process).readMetadataFile();
        Mockito.doReturn(true).when(process).writeMetadataFile(fileformat);

        RestMetadataResource body = new RestMetadataResource();
        body.setName("TitleDocMain");
        body.setMetadataLevel("topstruct");

        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);

            ProcessMetadataResource resource = new ProcessMetadataResource();
            resource.setRequest(mockRequestWithoutToken());

            Response response = resource.deleteMetadata("5", body);
            assertEquals(200, response.getStatus());
        }
    }
}
