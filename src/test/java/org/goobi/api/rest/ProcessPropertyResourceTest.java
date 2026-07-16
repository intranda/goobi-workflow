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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import org.goobi.api.rest.model.RestPropertyResource;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Process;
import org.goobi.production.enums.LogType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
public class ProcessPropertyResourceTest extends AbstractProcessResourceTest {

    // ---- deleteProperty ----

    @Test
    public void testDeletePropertyReturnsOkAndDeletesPropertyAndLogsJournal() {
        Process process = buildProcess(3);
        GoobiProperty property = new GoobiProperty(PropertyOwnerType.PROCESS);
        property.setId(77);
        property.setObjectId(5);
        property.setPropertyName("myProp");

        RestPropertyResource body = new RestPropertyResource();
        body.setId(77);

        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<PropertyManager> propm = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            propm.when(() -> PropertyManager.getPropertById(77)).thenReturn(property);

            ProcessPropertyResource resource = new ProcessPropertyResource();
            resource.setRequest(mockRequestWithoutToken());

            Response response = resource.deleteProperty("5", body);
            assertEquals(200, response.getStatus());

            propm.verify(() -> PropertyManager.deleteProperty(property));
            helper.verify(() -> Helper.addMessageToProcessJournal(eq(5), eq(LogType.DEBUG), anyString()));
        }
    }

    @Test
    public void testDeletePropertyInvalidProcessIdReturnsBadRequest() {
        RestPropertyResource body = new RestPropertyResource();
        ProcessPropertyResource resource = new ProcessPropertyResource();

        assertEquals(400, resource.deleteProperty(null, body).getStatus());
        assertEquals(400, resource.deleteProperty("", body).getStatus());
        assertEquals(400, resource.deleteProperty("abc", body).getStatus());
    }

    @Test
    public void testDeletePropertyMissingResourceIdReturnsBadRequest() {
        RestPropertyResource body = new RestPropertyResource();
        ProcessPropertyResource resource = new ProcessPropertyResource();

        // id not set at all -> null
        assertEquals(400, resource.deleteProperty("5", body).getStatus());

        // id explicitly 0
        body.setId(0);
        assertEquals(400, resource.deleteProperty("5", body).getStatus());
    }

    @Test
    public void testDeletePropertyNotFoundReturnsNotFound() {
        RestPropertyResource body = new RestPropertyResource();
        body.setId(77);

        try (MockedStatic<PropertyManager> propm = Mockito.mockStatic(PropertyManager.class)) {
            propm.when(() -> PropertyManager.getPropertById(77)).thenReturn(null);

            ProcessPropertyResource resource = new ProcessPropertyResource();

            assertEquals(404, resource.deleteProperty("5", body).getStatus());
        }
    }

    @Test
    public void testDeletePropertyOtherProcessReturnsConflict() {
        Process otherProcess = buildProcess(9);
        RestPropertyResource body = new RestPropertyResource();
        body.setId(77);

        GoobiProperty property = new GoobiProperty(PropertyOwnerType.PROCESS);
        property.setId(77);
        property.setObjectId(999); // belongs to a different process than "5"

        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<PropertyManager> propm = Mockito.mockStatic(PropertyManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(999)).thenReturn(otherProcess);
            propm.when(() -> PropertyManager.getPropertById(77)).thenReturn(property);

            ProcessPropertyResource resource = new ProcessPropertyResource();
            resource.setRequest(mockRequestWithoutToken());

            assertEquals(409, resource.deleteProperty("5", body).getStatus());
        }
    }

    @Test
    public void testDeletePropertyNonMemberTokenReturnsForbidden() {
        Process process = buildProcess(3);
        RestPropertyResource body = new RestPropertyResource();
        body.setId(77);

        GoobiProperty property = new GoobiProperty(PropertyOwnerType.PROCESS);
        property.setId(77);
        property.setObjectId(5);

        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<PropertyManager> propm = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<ProjectManager> projm = Mockito.mockStatic(ProjectManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            propm.when(() -> PropertyManager.getPropertById(77)).thenReturn(property);
            projm.when(() -> ProjectManager.isUserMemberOfProject(7, 3)).thenReturn(false);

            ProcessPropertyResource resource = new ProcessPropertyResource();
            resource.setRequest(mockRequestWithToken(buildToken(7)));

            assertEquals(403, resource.deleteProperty("5", body).getStatus());
        }
    }

    // ---- getProperty ----

    @Test
    public void testGetPropertyNotFoundReturnsNotFound() {
        try (MockedStatic<PropertyManager> propm = Mockito.mockStatic(PropertyManager.class)) {
            propm.when(() -> PropertyManager.getPropertById(77)).thenReturn(null);

            ProcessPropertyResource resource = new ProcessPropertyResource();

            assertEquals(404, resource.getProperty("5", "77").getStatus());
        }
    }

    @Test
    public void testGetPropertyNonMemberTokenReturnsForbidden() {
        Process process = buildProcess(3);
        GoobiProperty property = new GoobiProperty(PropertyOwnerType.PROCESS);
        property.setId(77);
        property.setObjectId(5);

        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<PropertyManager> propm = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<ProjectManager> projm = Mockito.mockStatic(ProjectManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            propm.when(() -> PropertyManager.getPropertById(77)).thenReturn(property);
            projm.when(() -> ProjectManager.isUserMemberOfProject(7, 3)).thenReturn(false);

            ProcessPropertyResource resource = new ProcessPropertyResource();
            resource.setRequest(mockRequestWithToken(buildToken(7)));

            assertEquals(403, resource.getProperty("5", "77").getStatus());
        }
    }

    // ---- updateProperty ----

    @Test
    public void testUpdatePropertyNotFoundReturnsNotFound() {
        RestPropertyResource body = new RestPropertyResource();
        body.setId(77);

        try (MockedStatic<PropertyManager> propm = Mockito.mockStatic(PropertyManager.class)) {
            propm.when(() -> PropertyManager.getPropertById(77)).thenReturn(null);

            ProcessPropertyResource resource = new ProcessPropertyResource();

            assertEquals(404, resource.updateProperty("5", body).getStatus());
        }
    }

    @Test
    public void testUpdatePropertyNonMemberTokenReturnsForbidden() {
        Process process = buildProcess(3);
        RestPropertyResource body = new RestPropertyResource();
        body.setId(77);

        GoobiProperty property = new GoobiProperty(PropertyOwnerType.PROCESS);
        property.setId(77);
        property.setObjectId(5);

        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<PropertyManager> propm = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<ProjectManager> projm = Mockito.mockStatic(ProjectManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            propm.when(() -> PropertyManager.getPropertById(77)).thenReturn(property);
            projm.when(() -> ProjectManager.isUserMemberOfProject(7, 3)).thenReturn(false);

            ProcessPropertyResource resource = new ProcessPropertyResource();
            resource.setRequest(mockRequestWithToken(buildToken(7)));

            assertEquals(403, resource.updateProperty("5", body).getStatus());
        }
    }

    // ---- createProperty ----

    @Test
    public void testCreatePropertyProcessNotFoundReturnsNotFound() {
        RestPropertyResource body = new RestPropertyResource();
        body.setName("name");
        body.setValue("value");

        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(null);

            ProcessPropertyResource resource = new ProcessPropertyResource();

            assertEquals(404, resource.createProperty("5", body).getStatus());
        }
    }

    @Test
    public void testCreatePropertyNonMemberTokenReturnsForbidden() {
        Process process = buildProcess(3);
        RestPropertyResource body = new RestPropertyResource();
        body.setName("name");
        body.setValue("value");

        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> projm = Mockito.mockStatic(ProjectManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            projm.when(() -> ProjectManager.isUserMemberOfProject(7, 3)).thenReturn(false);

            ProcessPropertyResource resource = new ProcessPropertyResource();
            resource.setRequest(mockRequestWithToken(buildToken(7)));

            assertEquals(403, resource.createProperty("5", body).getStatus());
        }
    }
}
