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

package io.goobi.workflow.harvester;

import java.io.InputStream;
import java.nio.file.Path;

import io.goobi.workflow.harvester.repository.Repository;
import jakarta.ws.rs.core.Response;

public interface MetadataParser {

    /**
     * 
     * Create a new process, get metadata from the input stream.
     * 
     * @param projectName
     * @param templateName
     * @param processTitle
     * @param inputStream
     * @return response
     */

    Response createNewProcess(String projectName, String templateName,
            String processTitle, InputStream inputStream);

    /**
     * Replace existing metadata of a process with the content of the input stream.
     *
     * @param processid
     * @param inputStream
     * @return response
     */

    Response replaceMetadata(Integer processid, InputStream inputStream);

    /**
     * Use this method to enhance metadata, e.g. add additional metadata or check for the document type and add an anchor record.
     *
     * @param repository
     * @param file
     */
    void extendMetadata(Repository repository, Path file);
}
