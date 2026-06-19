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

package org.goobi.api.rest.model;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Process;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.metadaten.Metadaten;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.persistence.managers.ProcessManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.log4j.Log4j2;
import ugh.dl.ContentFile;
import ugh.dl.DocStruct;
import ugh.dl.Reference;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;

@jakarta.ws.rs.Path("/view/video")
@ApplicationScoped
@Log4j2
public class RestVideoVttResource {

    private static final Path METADATA_FOLDER = Paths.get(ConfigurationHelper.getInstance().getMetadataFolder());

    @GET
    @jakarta.ws.rs.Path("/chapter/{processId}/{filename:.+}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getChapterVttFile(@PathParam("processId") String processIdString, @PathParam("filename") String filename) {
        int processId;
        try {
            processId = Integer.parseInt(processIdString);
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        Process process = ProcessManager.getProcessById(processId);
        if (process == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            List<DocStruct> pages = process.readMetadataFile().getDigitalDocument().getPhysicalDocStruct().getAllChildren();
            DocStruct page = pages != null ? findPageByFilename(pages, getFilename(filename)) : null;
            if (page == null) {
                return Response.ok("WEBVTT\n\n").build();
            }
            String vtt = buildChapterVtt(page, getVideoPath(page));
            return Response.ok(vtt, MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"chapters.vtt\"")
                    .build();
        } catch (ReadException | PreferencesException | IOException | SwapException e) {
            log.error("Error generating chapter VTT for process {} file {}", processId, filename, e);
            return Response.serverError().build();
        }
    }

    private DocStruct findPageByFilename(List<DocStruct> pages, String targetFilename) {
        for (DocStruct page : pages) {
            String imageName = page.getImageName();
            if (imageName == null) continue;
            if (extractFilename(imageName).equals(targetFilename)) {
                return page;
            }
        }
        return null;
    }

    private Path getVideoPath(DocStruct page) {
        List<ContentFile> files = page.getAllContentFiles();
        if (files == null || files.isEmpty()) return null;
        String location = files.get(0).getLocation();
        if (location == null) return null;
        if (location.startsWith("file:")) {
            try {
                return Path.of(new URI(location));
            } catch (Exception e) {
                // fall through
            }
        }
        return Path.of(location);
    }

    private static String extractFilename(String location) {
        if (location.startsWith("file:")) {
            try {
                return Path.of(new URI(location)).getFileName().toString();
            } catch (Exception e) {
                // fall through
            }
        }
        int sep = Math.max(location.lastIndexOf('/'), location.lastIndexOf(File.separatorChar));
        return sep >= 0 ? location.substring(sep + 1) : location;
    }

    private String buildChapterVtt(DocStruct page, Path videoPath) {
        List<DocStruct> areas = page.getAllChildren();
        if (areas == null || areas.isEmpty()) {
            return "WEBVTT\n\n";
        }

        record Chapter(String start, String end, String label) {
        }

        List<Chapter> chapters = new ArrayList<>();
        for (DocStruct area : areas) {
            String start = MetadatenHelper.getSingleMetadataValue(area, "_BEGIN").orElse("");
            String end   = MetadatenHelper.getSingleMetadataValue(area, "_END").orElse("");
            String label = area.getAllFromReferences().stream()
                    .map(Reference::getSource)
                    .filter(Objects::nonNull)
                    .reduce((a, b) -> b)
                    .flatMap(last -> MetadatenHelper.getSingleMetadataValue(last, "TitleDocMain"))
                    .orElse("");
            chapters.add(new Chapter(start, end, label));
        }

        StringBuilder vtt = new StringBuilder("WEBVTT\n\n");
        for (int i = 0; i < chapters.size(); i++) {
            Chapter c = chapters.get(i);
            String endTime = StringUtils.isBlank(c.end())
                    ? (i + 1 < chapters.size() ? chapters.get(i + 1).start()
                            : (videoPath != null ? Metadaten.getVideoDuration(videoPath) : ""))
                    : c.end();
            vtt.append(i + 1).append("\n");
            vtt.append(normalizeTimestamp(c.start())).append(" --> ").append(normalizeTimestamp(endTime)).append("\n");
            vtt.append(c.label()).append("\n\n");
        }
        return vtt.toString();
    }

    private String normalizeTimestamp(String ts) {
        return ts.matches("\\d{2}:\\d{2}:\\d{2}\\.\\d{3}") ? ts : ts + ".000";
    }

    @GET
    @jakarta.ws.rs.Path("/{process}/{folder}/{filename}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getSubtitleVttFile(@PathParam("process") String processIdString, @PathParam("folder") String folder,
            @PathParam("filename") String inFileName) {
        Path file = METADATA_FOLDER.resolve(getFilename(processIdString)).resolve("ocr").resolve(getFilename(folder)).resolve(getFilename(inFileName));
        String content = "";
        if (StorageProvider.getInstance().isFileExists(file)) {
            try {
                // TODO move this to StorageProvider implementations
                content = Files.readString(file);
            } catch (IOException e) {
                log.error(e);
            }
        }
        return Response.ok(content, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + inFileName + "\"")
                .build();
    }

    public String getFilename(String folder) {
        return Path.of(folder).getFileName().toString();
    }
}
