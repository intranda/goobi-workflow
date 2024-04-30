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
package org.goobi.api.rest.object;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.ws.WebServiceException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.goobi.beans.Process;

import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;

/**
 * @author Florian Alpers
 *
 */

@Path("/view/object")
public class ObjectResource {

    private static final String FILE_NOT_FOUND = "The file could not be found in the file system: ";

    @GET
    @Path("/{processId}/{foldername}/{filename}/info.json")
    @Produces({ MediaType.APPLICATION_JSON })
    public ObjectInfo getInfo(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("processId") int processId,
            @PathParam("foldername") String foldername, @PathParam("filename") String filename) {

        response.addHeader("Access-Control-Allow-Origin", "*"); //NOSONAR, CORS is needed
        filename = Paths.get(filename).getFileName().toString();
        String objectURI = request.getRequestURL().toString().replace("/info.json", "");
        String baseURI = objectURI.replace(filename, "");
        String baseFilename = FilenameUtils.getBaseName(filename);
        Process process = ProcessManager.getProcessById(processId);
        if (process == null) {
            throw new NotFoundException("No process found with identifier " + processId);
        }

        try {
            foldername =
                    NIOFileUtils.sanitizePath(Paths.get(process.getImagesDirectory(), foldername).toString(),
                            process.getImagesDirectory());
            List<URI> resourceURIs = getResources(Paths.get(foldername).toString(), baseFilename, baseURI);
            ObjectInfo info = new ObjectInfo(objectURI);
            info.setResources(resourceURIs);
            return info;
        } catch (IOException | SwapException | URISyntaxException e) {
            throw new WebServiceException(e);
        }

    }

    /**
     * @param foldername
     * @param baseFilename
     * @param baseURI
     * @param process
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws SwapException
     * @throws DAOException
     * @throws URISyntaxException
     */
    private List<URI> getResources(String baseFolder, String baseFilename, String baseURI)
            throws IOException, URISyntaxException {
        List<URI> resourceURIs = new ArrayList<>();

        baseFilename = Paths.get(baseFilename).getFileName().toString();

        java.nio.file.Path mtlFilePath = Paths.get(baseFolder, baseFilename + ".mtl");
        if (mtlFilePath.toFile().isFile()) {
            resourceURIs.add(new URI(baseURI + Paths.get(baseFolder).relativize(mtlFilePath)));
        }

        java.nio.file.Path resourceFolderPath = Paths.get(baseFolder, baseFilename);
        if (resourceFolderPath.toFile().isDirectory()) {
            try (DirectoryStream<java.nio.file.Path> directoryStream = Files.newDirectoryStream(resourceFolderPath)) { //NOSONAR, input parameter are checked
                for (java.nio.file.Path path : directoryStream) {
                    java.nio.file.Path relPath = resourceFolderPath.getParent().relativize(path);
                    resourceURIs.add(new URI(baseURI + relPath.toString().replace(File.separator, "/")));
                }
            }
        }
        Collections.sort(resourceURIs);
        return resourceURIs;
    }

    @GET
    @Path("/{processId}/{foldername}/{filename}.js")
    @Produces({ "application/javascript" })
    public String getJS(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("processId") int processId,
            @PathParam("foldername") String foldername, @PathParam("filename") final String filenameBase)
            throws IOException, InterruptedException, SwapException, DAOException {

        String filename = filenameBase + ".js";
        Process process = ProcessManager.getProcessById(processId);

        foldername = Paths.get(foldername).getFileName().toString();
        filename = Paths.get(filename).getFileName().toString();

        java.nio.file.Path objectPath = Paths.get(process.getImagesDirectory(), foldername, filename);
        if (!objectPath.toFile().isFile()) {

            try (DirectoryStream<java.nio.file.Path> folders =
                    Files.newDirectoryStream(Paths.get(process.getImagesDirectory(), foldername))) {
                for (java.nio.file.Path folder : folders) {
                    java.nio.file.Path filePath = folder.resolve(filename);
                    if (Files.isRegularFile(filePath)) {
                        return Files.readAllLines(filePath).stream().collect(Collectors.joining("\n"));
                    }
                }
            }

            throw new FileNotFoundException(FILE_NOT_FOUND + objectPath);
        } else {
            return Files.readAllLines(objectPath).stream().collect(Collectors.joining("\n"));
        }
    }

    @GET
    @Path("/{processId}/{foldername}/{filename}.xml")
    @Produces({ MediaType.TEXT_XML })
    public String getXml(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("processId") int processId,
            @PathParam("foldername") String foldername, @PathParam("filename") final String filenameBase)
            throws IOException, InterruptedException, SwapException, DAOException {

        String filename = filenameBase + ".xml";

        foldername = Paths.get(foldername).getFileName().toString();
        filename = Paths.get(filename).getFileName().toString();

        Process process = ProcessManager.getProcessById(processId);
        java.nio.file.Path objectPath = Paths.get(process.getImagesDirectory(), foldername, filename);
        if (!objectPath.toFile().isFile()) {

            try (DirectoryStream<java.nio.file.Path> folders =
                    Files.newDirectoryStream(Paths.get(process.getImagesDirectory(), foldername))) {
                for (java.nio.file.Path folder : folders) {
                    java.nio.file.Path filePath = folder.resolve(filename);
                    if (Files.isRegularFile(filePath)) {
                        return Files.readAllLines(filePath).stream().collect(Collectors.joining("\n"));
                    }
                }
            }

            throw new FileNotFoundException(FILE_NOT_FOUND + objectPath);
        } else {
            return Files.readAllLines(objectPath).stream().collect(Collectors.joining("\n"));
        }
    }

    @GET
    @Path("/{processId}/{foldername}/images/{filename}.jpg")
    @Produces({ "image/jpeg" })
    public void getJpeg(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("processId") int processId,
            @PathParam("foldername") String foldername, @PathParam("filename") final String filenameBase)
            throws IOException, InterruptedException, SwapException, DAOException {

        String filename = filenameBase + ".jpg";

        foldername = Paths.get(foldername).getFileName().toString();
        filename = Paths.get(filename).getFileName().toString();

        Process process = ProcessManager.getProcessById(processId);
        java.nio.file.Path objectPath = Paths.get(process.getImagesDirectory(), foldername, "images", filename);
        if (!objectPath.toFile().isFile()) {

            try (DirectoryStream<java.nio.file.Path> folders =
                    Files.newDirectoryStream(Paths.get(process.getImagesDirectory(), foldername))) {
                for (java.nio.file.Path folder : folders) {
                    java.nio.file.Path filePath = folder.resolve(filename);
                    if (Files.isRegularFile(filePath)) {
                        try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
                            IOUtils.copy(fis, response.getOutputStream());
                        }
                    }
                }
            }

            throw new FileNotFoundException(FILE_NOT_FOUND + objectPath);
        } else {
            try (FileInputStream fis = new FileInputStream(objectPath.toFile())) {
                IOUtils.copy(fis, response.getOutputStream());
            }
        }
    }

    @GET
    @Path("/{processId}/{foldername}/{filename}")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public StreamingOutput getObject(@Context HttpServletRequest request, @Context HttpServletResponse response,
            @PathParam("processId") int processId, @PathParam("foldername") String foldername, @PathParam("filename") final String filename)
            throws IOException, InterruptedException, SwapException, DAOException {

        foldername = Paths.get(foldername).getFileName().toString();
        Process process = ProcessManager.getProcessById(processId);
        java.nio.file.Path objectPath =
                Paths.get(NIOFileUtils.sanitizePath(Paths.get(process.getImagesDirectory(), foldername, filename).toString(),
                        process.getImagesDirectory()));

        if (!objectPath.toFile().isFile()) {
            //try subfolders
            DirectoryStream.Filter<? super java.nio.file.Path> filter = new DirectoryStream.Filter<java.nio.file.Path>() {

                @Override
                public boolean accept(java.nio.file.Path entry) throws IOException {
                    return entry.endsWith(FilenameUtils.getBaseName(filename));
                }

            };

            try (DirectoryStream<java.nio.file.Path> folders =
                    Files.newDirectoryStream(Paths.get(process.getImagesDirectory(), foldername), filter)) {
                for (java.nio.file.Path folder : folders) {
                    java.nio.file.Path filePath = folder.resolve(filename);
                    if (Files.isRegularFile(filePath)) {
                        return new ObjectStreamingOutput(filePath);
                    }
                }
            }

            throw new FileNotFoundException(FILE_NOT_FOUND + objectPath);
        } else {
            return new ObjectStreamingOutput(objectPath);
        }
    }

    @GET
    @Path("/{processId}/{foldername}/{subfolder}/{filename}")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public StreamingOutput getObjectResource(@Context HttpServletRequest request, @Context HttpServletResponse response,
            @PathParam("processId") int processId, @PathParam("foldername") String foldername, @PathParam("subfolder") String subfolder,
            @PathParam("filename") final String filename) throws IOException, InterruptedException, SwapException, DAOException {

        Process process = ProcessManager.getProcessById(processId);
        java.nio.file.Path objectPath =
                Paths.get(NIOFileUtils.sanitizePath(Paths.get(process.getImagesDirectory(), foldername, subfolder, filename).toString(),
                        process.getImagesDirectory()));
        if (!objectPath.toFile().isFile()) {
            throw new FileNotFoundException(FILE_NOT_FOUND + objectPath);
        } else {
            return new ObjectStreamingOutput(objectPath);
        }
    }

    @GET
    @Path("/{processId}/{foldername}//{subfolder}/{filename}")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public StreamingOutput getObjectResource2(@Context HttpServletRequest request, @Context HttpServletResponse response,
            @PathParam("processId") int processId, @PathParam("foldername") String foldername, @PathParam("subfolder") String subfolder,
            @PathParam("filename") final String filename) throws IOException, InterruptedException, SwapException, DAOException {
        return getObjectResource(request, response, processId, foldername, subfolder, filename);
    }

    @GET
    @Path("/{processId}/{foldername}/{subfolder1}/{subfolder2}/{filename}")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public StreamingOutput getObjectResource(@Context HttpServletRequest request, @Context HttpServletResponse response,
            @PathParam("processId") int processId, @PathParam("foldername") String foldername, @PathParam("subfolder1") String subfolder1,
            @PathParam("subfolder2") String subfolder2, @PathParam("filename") String filename)
            throws IOException, InterruptedException, SwapException, DAOException {

        Process process = ProcessManager.getProcessById(processId);
        java.nio.file.Path objectPath =
                Paths.get(NIOFileUtils.sanitizePath(Paths.get(process.getImagesDirectory(), foldername, subfolder1, subfolder2, filename).toString(),
                        process.getImagesDirectory()));
        if (!objectPath.toFile().isFile()) {
            throw new FileNotFoundException(FILE_NOT_FOUND + objectPath);
        } else {
            return new ObjectStreamingOutput(objectPath);
        }
    }

    @GET
    @Path("/{processId}/{foldername}//{subfolder1}/{subfolder2}/{filename}")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public StreamingOutput getObjectResource2(@Context HttpServletRequest request, @Context HttpServletResponse response,
            @PathParam("processId") int processId, @PathParam("foldername") String foldername, @PathParam("subfolder1") String subfolder1,
            @PathParam("subfolder2") String subfolder2, @PathParam("filename") String filename)
            throws IOException, InterruptedException, SwapException, DAOException {
        return getObjectResource(request, response, processId, foldername, subfolder1, subfolder2, filename);
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
                    IOUtils.copy(inputStream, output);
                }
            } catch (Throwable e) {
                throw new WebApplicationException(e);
            }

        }
    }
}
