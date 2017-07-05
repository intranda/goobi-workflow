/**
 * This file is part of the Goobi viewer - a content presentation and management application for digitized objects.
 *
 * Visit these websites for more information.
 *          - http://www.intranda.com
 *          - http://digiverso.com
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
package de.sub.goobi.metadaten.object;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.ws.WebServiceException;

import org.apache.log4j.lf5.util.StreamUtils;
import org.goobi.beans.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;

/**
 * @author Florian Alpers
 *
 */

@Path("/view/object")
public class ObjectResource {
    
    private static final Logger logger = LoggerFactory.getLogger(ObjectResource.class);

    @GET
    @Path("/{processId}/{foldername}/{filename}/info.json")
    @Produces({ MediaType.APPLICATION_JSON })
    public ObjectInfo getInfo(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("processId") String processId,
            @PathParam("foldername") String foldername, @PathParam("filename") String filename) {
        
        response.addHeader("Access-Control-Allow-Origin", "*");

        String objectURI = request.getRequestURL().toString().replace("/info.json", "");
                
        try {
            ObjectInfo info = new ObjectInfo(objectURI);
            return info;
        } catch (URISyntaxException e) {
           throw new WebServiceException(e);
        }
        
        
    }
    
    @GET
    @Path("/{processId}/{foldername}/{filename}")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public StreamingOutput getObject(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("processId") int processId,
            @PathParam("foldername") String foldername, @PathParam("filename") String filename) throws IOException, InterruptedException, SwapException, DAOException {

//        response.addHeader("Access-Control-Allow-Origin", "*");
        
        Process process = ProcessManager.getProcessById(processId);
        java.nio.file.Path objectPath = Paths.get(process.getImagesDirectory(), foldername, filename);
        if(!objectPath.toFile().isFile()) {
            throw new FileNotFoundException("File " + objectPath + " not found in file system");
        } else {
            return new ObjectStreamingOutput(objectPath);
        }
    }
    
    @GET
    @Path("/{processId}/{foldername}/{subfolder}/{filename}")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public StreamingOutput getObjectResource(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("processId") int processId,
            @PathParam("foldername") String foldername ,@PathParam("subfolder") String subfolder, @PathParam("filename") String filename) throws IOException, InterruptedException, SwapException, DAOException {

//        response.addHeader("Access-Control-Allow-Origin", "*");
        
        Process process = ProcessManager.getProcessById(processId);
        java.nio.file.Path objectPath = Paths.get(process.getImagesDirectory(), foldername, subfolder, filename);
        if(!objectPath.toFile().isFile()) {
            throw new FileNotFoundException("File " + objectPath + " not found in file system");
        } else {
            return new ObjectStreamingOutput(objectPath);
        }
    }

    
    public static class ObjectStreamingOutput implements StreamingOutput {

        private java.nio.file.Path filePath;

        public ObjectStreamingOutput(java.nio.file.Path filePath) {
            this.filePath = filePath;
        }

        @Override
        public void write(OutputStream output) throws IOException, WebApplicationException {
            try {
                try (InputStream inputStream = new java.io.FileInputStream(this.filePath.toString())) {
                    StreamUtils.copy(inputStream, output);
                    return;
                }
//            } catch (LostConnectionException e) {
//                logger.trace("aborted writing 3d object from  " + this.filePath);
            } catch (Throwable e) {
                throw new WebApplicationException(e);
            }

        }
    }
}
