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
package org.goobi.api.rest.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.persistence.managers.ProcessManager;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("deprecation")
public class ProcessCommandAliasTest extends AbstractTest {

    @Test
    public void testGetStatusOfUnknownProcessReturnsProcessNotFound() {
        int unknownId = 999999999;

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(unknownId)).thenReturn(null);

            CommandProcessStatus command = new CommandProcessStatus();
            Response response = command.getStatusOfProcess(unknownId);

            assertEquals(Response.Status.PARTIAL_CONTENT.getStatusCode(), response.getStatus());
            assertEquals("Process not found", response.getEntity());
        }
    }

    @Test
    public void testImageDownloadWithUnknownProcessReturnsBadRequest() {
        int unknownId = 999999999;

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(unknownId)).thenReturn(null);

            CommandImageDownload command = new CommandImageDownload();
            Response response = command.download(unknownId);

            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testGetProcessStatusListWithInvalidDateFormatThrowsBadRequest() {
        CommandProcessStatus command = new CommandProcessStatus();

        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> command.getProcessStatusList("'; DROP TABLE prozesse; --", "2020-01-01"));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), exception.getResponse().getStatus());
    }
}
