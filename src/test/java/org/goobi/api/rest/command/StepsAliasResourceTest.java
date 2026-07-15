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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;

import org.goobi.api.rest.model.RestReportProblemResponse;
import org.goobi.api.rest.request.ReportProblem;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.persistence.managers.StepManager;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.JAXBContext;

@SuppressWarnings("deprecation")
public class StepsAliasResourceTest extends AbstractTest {

    @Test
    public void testGetReportProblemForTaskWithUnknownStepReturnsNotFound() {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedStepManager.when(() -> StepManager.getStepById(4711)).thenReturn(null);

            StepsAliasResource resource = new StepsAliasResource();
            Response response = resource.getReportProblemForTask(4711, "Scanning", "Image damaged");

            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testGetReportProblemForTaskFromBodyWithUnknownStepReturnsNotFound() {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedStepManager.when(() -> StepManager.getStepById(4711)).thenReturn(null);

            StepsAliasResource resource = new StepsAliasResource();
            Response response = resource.getReportProblemForTaskFromBody(4711, "Scanning", "Image damaged");

            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testGetReportProblemForTaskWithXmlBodyAndUnknownStepReturnsNotFound() {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedStepManager.when(() -> StepManager.getStepById(4711)).thenReturn(null);

            ReportProblem problem = new ReportProblem();
            problem.setStepId("4711");
            problem.setDestinationStepName("Scanning");
            problem.setErrorText("Image damaged");

            StepsAliasResource resource = new StepsAliasResource();
            Response response = resource.getReportProblemForTask(problem);

            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testGetReportProblemForTaskWithXmlBodyAndNonNumericStepIdReturnsBadRequest() {
        ReportProblem problem = new ReportProblem();
        problem.setStepId("not-a-number");
        problem.setDestinationStepName("Scanning");
        problem.setErrorText("Image damaged");

        StepsAliasResource resource = new StepsAliasResource();
        Response response = resource.getReportProblemForTask(problem);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testReportProblemResponseIsXmlMarshalable() throws Exception {
        RestReportProblemResponse resp = new RestReportProblemResponse();
        resp.setStatus("ok");
        resp.setProcessId(1);

        JAXBContext ctx = JAXBContext.newInstance(RestReportProblemResponse.class);
        StringWriter sw = new StringWriter();
        ctx.createMarshaller().marshal(resp, sw);

        String xml = sw.toString().toLowerCase();
        assertTrue(xml.contains("restreportproblemresponse") || xml.contains("reportproblemresponse"));
    }
}
