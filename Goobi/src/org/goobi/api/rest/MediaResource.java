package org.goobi.api.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.message.internal.ReaderWriter;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.log4j.Log4j;

@Log4j
@javax.ws.rs.Path("/view/media")
public class MediaResource {

    private static final Path metadataFolderPath = Paths.get(ConfigurationHelper.getInstance().getMetadataFolder());

    @GET
    @javax.ws.rs.Path("{process}/{folder}/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary="Serves a media resource", description="Serves a media resource consisting of a process name, a directory name and a resource name")
    @ApiResponse(responseCode="200", description="OK")
    @ApiResponse(responseCode="500", description="Internal error")
    
    public Response serveMediaContent(@PathParam("process") String processIdString, @PathParam("folder") String folder,
            @PathParam("filename") String filename) {

        Path processFolder = metadataFolderPath.resolve(processIdString);

        Path mediaFolder = getImagesFolder(processFolder, folder);
        Path mediaResource = mediaFolder.resolve(filename);

        StreamingOutput entity = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try (InputStream in = StorageProvider.getInstance().newInputStream(mediaResource)) {
                    ReaderWriter.writeTo(in, output);
                }
            }
        };
        return Response.ok(entity).build();
    }

    private Path getImagesFolder(Path processFolder, String folder) {
        try {
            switch (folder.toLowerCase()) {
                case "master":
                case "orig":
                    return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getImagesOrigDirectory(false));
                case "media":
                case "tif":
                    return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getImagesTifDirectory(false));
                case "thumbnails_large":
                    return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getImagesDirectory(), "layoutWizzard-temp",
                            "thumbnails_large");
                case "thumbnails_small":
                    return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getImagesDirectory(), "layoutWizzard-temp",
                            "thumbnails_small");
                default:
                    if (!folder.contains("_")) {
                        return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getImagesDirectory(), folder);
                    } else {
                        return processFolder.resolve("images").resolve(folder);
                    }
            }
        } catch (IOException | InterruptedException | SwapException | DAOException e) {
            log.error(e);
        }
        return null;
    }

    /**
     * @param processIdString
     * @return
     */
    private synchronized org.goobi.beans.Process getGoobiProcess(String processIdString) {
        int processId = Integer.parseInt(processIdString);
        org.goobi.beans.Process process = ProcessManager.getProcessById(processId);
        return process;
    }

}
