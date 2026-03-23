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
package org.goobi.api.rest.process.pdf;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.weld.exceptions.IllegalArgumentException;

import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.servlet.controller.GetAction;
import de.unigoettingen.sub.commons.contentlib.servlet.controller.GetPdfAction;
import de.unigoettingen.sub.commons.contentlib.servlet.model.ContentServerConfiguration;
import de.unigoettingen.sub.commons.contentlib.servlet.model.SinglePdfRequest;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerBinding;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import lombok.extern.log4j.Log4j2;

@jakarta.ws.rs.Path("/process/{processId}/files/pdf")
@ContentServerBinding
@Log4j2
public class GoobiFilesPdfResource {

    @Context
    private ContainerRequestContext context;
    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;

    private final org.goobi.beans.Process process;

    public GoobiFilesPdfResource(
            @Context ContainerRequestContext context, @Context HttpServletRequest request, @Context HttpServletResponse response,
            @PathParam("processId") String processId)
            throws ContentLibException {
        this.process = getGoobiProcess(processId);
        if (this.process == null) {
            throw new IllegalArgumentException("No process " + processId + " found");
        }
    }

    @GET
    @jakarta.ws.rs.Path("/{pdfFilename}")
    @Produces({ "application/pdf" })
    public Response getPdfFromImages() throws ContentLibException, IOException, SwapException, URISyntaxException {
        String tiffDirectory = this.process.getImagesTifDirectory(false);
        List<String> imageFiles = StorageProvider.getInstance()
                .list(tiffDirectory.toString(), NIOFileUtils.imageOrPdfNameFilter)
                .stream()
                .map(img -> tiffDirectory + img)
                .toList();

        SinglePdfRequest pdfRequest = new SinglePdfRequest(StringUtils.join(imageFiles, "$"), GetAction.parseParameters(request, context));

        StreamingOutput so = out -> {
            try {
                new GetPdfAction().writePdf(pdfRequest, ContentServerConfiguration.getInstance(), out);
            } catch (URISyntaxException | ContentLibException e) {
                throw new WebApplicationException(e);
            }
        };
        return Response.ok(so).build();

    }

    /**
     * @param processIdString
     * @return
     */
    private synchronized org.goobi.beans.Process getGoobiProcess(String processIdString) {
        int processId = Integer.parseInt(processIdString);
        return ProcessManager.getProcessById(processId);
    }
}
