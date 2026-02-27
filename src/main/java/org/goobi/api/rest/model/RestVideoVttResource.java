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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.metadaten.Metadaten;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@jakarta.ws.rs.Path("/view/video")
@ApplicationScoped
@Log4j2
public class RestVideoVttResource {

    private static final Path METADATA_FOLDER = Paths.get(ConfigurationHelper.getInstance().getMetadataFolder());

    @Getter
    @Setter
    @Inject // NOSONAR constructor injection is not possible, as an empty constructor is needed
    private Metadaten metadataBean;

    @GET
    @jakarta.ws.rs.Path("/chapter")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getChapterVttFile() {
        String content = "";

        // jsf context is present, search for metadata bean
        try {
            if (metadataBean != null) {
                content = metadataBean.getChapterInformationAsVTT();
            }
        } catch (ContextNotActiveException e) {
            log.error(e);
        }
        return Response.ok(content, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"video.vtt\"")
                .build();
    }

    @GET
    @jakarta.ws.rs.Path("/{process}/{folder}/{filename}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getSubtitleVttFile(@PathParam("process") String processIdString, @PathParam("folder") String folder,
            @PathParam("filename") String inFileName) {

        // get metadata folder
        // processid + folder + filename
        Path file =
                METADATA_FOLDER.resolve(getFilename(processIdString)).resolve("ocr").resolve(getFilename(folder)).resolve(getFilename(inFileName));
        String content = "";
        if (StorageProvider.getInstance().isFileExists(file)) {
            // read content
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
