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

import org.goobi.api.rest.response.DeletionResponse;
import org.goobi.beans.Process;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.persistence.managers.ProcessManager;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
public class CommandProcessDeleteTest extends AbstractTest {

    @Test
    public void testDeleteUnknownProcessReturnsNotFound() {
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(999)).thenReturn(null);

            @SuppressWarnings("deprecation")
            Response response = new CommandProcessDelete().deleteProcess(999);

            assertEquals(404, response.getStatus());
            DeletionResponse entity = (DeletionResponse) response.getEntity();
            assertEquals("error", entity.getResult());
        }
    }

    @Test
    public void testDeleteExistingProcessReturnsSuccess() throws Exception {
        Process process = Mockito.spy(new Process());
        process.setId(5);
        process.setTitel("testProcess");
        Mockito.doReturn("/tmp/none").when(process).getProcessDataDirectory();
        Mockito.doReturn("/tmp/none/ocr").when(process).getOcrDirectory();

        StorageProviderInterface storage = Mockito.mock(StorageProviderInterface.class);

        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<StorageProvider> sp = Mockito.mockStatic(StorageProvider.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            sp.when(StorageProvider::getInstance).thenReturn(storage);

            @SuppressWarnings("deprecation")
            Response response = new CommandProcessDelete().deleteProcess(5);

            assertEquals(200, response.getStatus());
            DeletionResponse entity = (DeletionResponse) response.getEntity();
            assertEquals("success", entity.getResult());
            pm.verify(() -> ProcessManager.deleteProcess(process));
        }
    }
}
