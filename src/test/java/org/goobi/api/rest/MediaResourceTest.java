/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package org.goobi.api.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;

import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

public class MediaResourceTest extends AbstractTest {

    private static final int PROCESS_ID = 42;
    private static final int PROJECT_ID = 7;
    private static final String FILENAME = "00000001.mp4";

    @TempDir
    private Path mediaFolder;

    private Process process;

    @BeforeEach
    public void setUp() throws Exception {
        Files.writeString(mediaFolder.resolve(FILENAME), "video");

        Project project = Mockito.mock(Project.class);
        Mockito.when(project.getId()).thenReturn(PROJECT_ID);
        process = Mockito.mock(Process.class);
        Mockito.when(process.getProjekt()).thenReturn(project);
        Mockito.when(process.getImagesTifDirectory(false)).thenReturn(mediaFolder.toString());
    }

    @Test
    public void testConstructor() {
        MediaResource res = new MediaResource();
        assertNotNull(res);
    }

    @Test
    public void testServeMediaContentForProjectMember() {
        Response response = serve(1, true);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testServeMediaContentDeniedForNonMember() {
        Response response = serve(1, false);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testServeMediaContentDeniedWithoutUser() {
        Response response = serve(null, false);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testServeMediaContentUnknownProcess() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(PROCESS_ID)).thenReturn(null);
            MediaResource res = new MediaResource();
            res.setRequest(Mockito.mock(HttpServletRequest.class));
            assertEquals(404, res.serveMediaContent(String.valueOf(PROCESS_ID), "media", FILENAME, null).getStatus());
        }
    }

    private Response serve(Integer userId, boolean member) {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(PROCESS_ID)).thenReturn(process);
            mockedProjectManager.when(() -> ProjectManager.isUserMemberOfProject(Mockito.anyInt(), Mockito.eq(PROJECT_ID))).thenReturn(member);

            HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
            Mockito.when(request.getAttribute("userid")).thenReturn(userId);
            MediaResource res = new MediaResource();
            res.setRequest(request);
            return res.serveMediaContent(String.valueOf(PROCESS_ID), "media", FILENAME, null);
        }
    }

}
