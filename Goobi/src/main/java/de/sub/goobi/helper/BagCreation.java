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

package de.sub.goobi.helper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import de.sub.goobi.config.ConfigurationHelper;
import gov.loc.repository.bagit.creator.BagCreator;
import gov.loc.repository.bagit.domain.Metadata;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;
import gov.loc.repository.bagit.hash.SupportedAlgorithm;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class BagCreation {

    @Getter
    private Path bagitRoot;
    @Getter
    private Path ieFolder;
    @Getter
    private Path metadataFolder;
    @Getter
    private Path objectsFolder;
    @Getter
    private Path documentationFolder;
    @Getter
    private Path otherFolder;
    @Getter
    private Metadata metadata = new Metadata();

    private Path updatedIeFolder;

    /**
     * 
     * @param rootFolderName name of the root folder. If missing, a new uuid folder is generated within the temp directory
     */

    public BagCreation(String rootFolderName) {
        if (StringUtils.isBlank(rootFolderName)) {
            bagitRoot = Paths.get(ConfigurationHelper.getInstance().getTemporaryFolder(), UUID.randomUUID().toString());
        } else {
            bagitRoot = Paths.get(rootFolderName);
        }
    }

    /**
     * Create a sub folder for the payload. All files of the intellectual entity are stored here
     *
     * @param folderName optional sub folder name below the data folder, can be empty
     * @param objectFolderName: name of the object folder, i.e. 'objects' or 'representations'
     * 
     */

    public void createIEFolder(String folderName, String objectFolderName) {
        if (StringUtils.isBlank(folderName)) {
            ieFolder = Paths.get(bagitRoot.toString());
            updatedIeFolder = Paths.get(bagitRoot.toString(), "data");
        } else {
            ieFolder = Paths.get(bagitRoot.toString(), folderName);
            updatedIeFolder = Paths.get(bagitRoot.toString(), "data", folderName);
        }
        metadataFolder = Paths.get(ieFolder.toString(), "metadata");
        objectsFolder = Paths.get(ieFolder.toString(), objectFolderName);
        documentationFolder = Paths.get(ieFolder.toString(), "documentation");
        otherFolder = Paths.get(ieFolder.toString(), "other");
        try {
            StorageProvider.getInstance().createDirectories(metadataFolder);
            StorageProvider.getInstance().createDirectories(objectsFolder);
        } catch (IOException e) {
            log.error(e);
        }
    }

    public void createBag() {
        StandardSupportedAlgorithms algorithm = StandardSupportedAlgorithms.SHA256;
        boolean includeHiddenFiles = true;
        Collection<SupportedAlgorithm> algos = new ArrayList<>();
        algos.add(algorithm);

        /* create bag */
        try {
            BagCreator.bagInPlace(bagitRoot, algos, includeHiddenFiles, metadata);
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error(e);
        }
        log.info("Files and Metadata aranged in Bag structure");

        // update folder
        ieFolder = updatedIeFolder;
        metadataFolder = Paths.get(ieFolder.toString(), "metadata");
        objectsFolder = Paths.get(ieFolder.toString(), objectsFolder.getFileName().toString());
        documentationFolder = Paths.get(ieFolder.toString(), "documentation");

    }

    public void addMetadata(String key, String value) {
        metadata.add(key, value);
    }

    /**
     * Remove all created files and folder
     */

    public void cleanUp() {
        StorageProvider.getInstance().deleteDir(bagitRoot);
    }
}
