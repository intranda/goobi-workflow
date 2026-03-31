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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.glassfish.jersey.message.internal.ReaderWriter;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import lombok.extern.log4j.Log4j2;

@Log4j2
@jakarta.ws.rs.Path("/view/media")
public class MediaResource {

    private static final Path METADATA_FOLDER = Paths.get(ConfigurationHelper.getInstance().getMetadataFolder());

    @GET
    @jakarta.ws.rs.Path("{process}/{folder}/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Serves a media resource",
            description = "Serves a media resource consisting of a process name, a directory name and a resource name")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "206", description = "Partial Content")
    @ApiResponse(responseCode = "500", description = "Internal error")

    public Response serveMediaContent(@PathParam("process") String processIdString, @PathParam("folder") String folder,
            @PathParam("filename") String filename, @HeaderParam("Range") String rangeHeader) {

        Path processFolder = METADATA_FOLDER.resolve(processIdString);

        Path mediaFolder = getImagesFolder(processFolder, folder);
        Path mediaResource = mediaFolder.resolve(filename);

        long fileSize;
        try {
            fileSize = StorageProvider.getInstance().getFileSize(mediaResource);
        } catch (IOException e) {
            log.error(e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String mimeType = detectMimeType(filename);

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            return servePartialContent(mediaResource, rangeHeader, fileSize, mimeType);
        }

        StreamingOutput entity = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try (InputStream in = StorageProvider.getInstance().newInputStream(mediaResource)) {
                    ReaderWriter.writeTo(in, output);
                }
            }
        };
        return Response.ok(entity)
                .header("Accept-Ranges", "bytes")
                .header("Content-Length", fileSize)
                .header("Content-Type", mimeType)
                .build();
    }

    private Response servePartialContent(Path mediaResource, String rangeHeader, long fileSize, String mimeType) {
        String rangeValue = rangeHeader.substring("bytes=".length());
        String[] parts = rangeValue.split("-", 2);

        long start;
        long end;
        try {
            start = parts[0].isEmpty() ? fileSize - Long.parseLong(parts[1]) : Long.parseLong(parts[0]);
            end = (parts.length < 2 || parts[1].isEmpty()) ? fileSize - 1 : Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return Response.status(416)
                    .header("Content-Range", "bytes */" + fileSize)
                    .build();
        }

        if (start < 0 || start >= fileSize || end >= fileSize || start > end) {
            return Response.status(416)
                    .header("Content-Range", "bytes */" + fileSize)
                    .build();
        }

        long contentLength = end - start + 1;
        final long rangeStart = start;
        final long rangeEnd = end;

        StreamingOutput entity = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try (InputStream in = StorageProvider.getInstance().newInputStream(mediaResource)) {
                    long toSkip = rangeStart;
                    while (toSkip > 0) {
                        long skipped = in.skip(toSkip);
                        if (skipped <= 0) {
                            break;
                        }
                        toSkip -= skipped;
                    }
                    byte[] buffer = new byte[8192];
                    long remaining = rangeEnd - rangeStart + 1;
                    int read;
                    while (remaining > 0 && (read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                        output.write(buffer, 0, read);
                        remaining -= read;
                    }
                }
            }
        };

        return Response.status(206)
                .entity(entity)
                .header("Accept-Ranges", "bytes")
                .header("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileSize)
                .header("Content-Length", contentLength)
                .header("Content-Type", mimeType)
                .build();
    }

    private String detectMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lower.endsWith(".webm")) {
            return "video/webm";
        } else if (lower.endsWith(".ogv")) {
            return "video/ogg";
        } else if (lower.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (lower.endsWith(".ogg") || lower.endsWith(".oga")) {
            return "audio/ogg";
        } else if (lower.endsWith(".wav")) {
            return "audio/wav";
        } else if (lower.endsWith(".flac")) {
            return "audio/flac";
        }
        return MediaType.APPLICATION_OCTET_STREAM;
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
                case "ocr_txt":
                    return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getOcrTxtDirectory());
                case "ocr_alto":
                    return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getOcrAltoDirectory());
                case "ocr_xml":
                    return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getOcrXmlDirectory());
                case "ocr_pdf":
                    return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getOcrPdfDirectory());
                default:
                    if (!folder.contains("_")) {
                        return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getImagesDirectory(), folder);
                    } else {
                        return processFolder.resolve("images").resolve(folder);
                    }
            }
        } catch (IOException | SwapException | DAOException e) {
            log.error(e);
        }
        return processFolder.resolve("images").resolve(folder);
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
