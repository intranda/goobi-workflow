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
package io.goobi.workflow.harvester.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

import de.sub.goobi.config.ConfigHarvester;
import io.goobi.workflow.harvester.export.ExportOutcome.ExportOutcomeStatus;
import lombok.extern.log4j.Log4j2;
import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.FileSet;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.MetsModsImportExport;

@Log4j2
public class ConverterDC extends AbstractConverter {

    private String rootDocstrctType;

    public ConverterDC(Document doc, ExportMode mode, String rootDocstrctType, String repositoryType) {
        super(doc, mode, repositoryType);
        this.rootDocstrctType = rootDocstrctType;
    }

    @Override
    public ExportOutcome createNewFileformat() {
        ExportOutcome outcome = new ExportOutcome();
        if (doc == null) {
            log.error("XML doc is null.");
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = "XML doc is null.";
            return outcome;
        }

        Fileformat myRdf = null;
        Map<Integer, DocStruct> docStructPhysPageNumberMap = new HashMap<>();
        Map<Integer, ContentFile> contentFileMap = new HashMap<>();

        if (doc.getRootElement().getChild("error", null) != null) {
            log.error("Error: {}", doc.getRootElement().getChild("error", null).getAttributeValue("code"));
            outcome.status = ExportOutcomeStatus.NOT_FOUND;
            return outcome;
        }
        Element eleListRecords = doc.getRootElement().getChild("GetRecord", null);
        if (eleListRecords == null) {
            log.warn("No 'GetRecord' element found .");
            outcome.status = ExportOutcomeStatus.NOT_FOUND;
            return outcome;
        }
        List<Element> recordList = eleListRecords.getChildren("record", null);
        if (recordList.isEmpty()) {
            log.warn("No 'record' elements found.");
            outcome.status = ExportOutcomeStatus.NOT_FOUND;
            return outcome;
        }

        Element eleRecord = recordList.get(0);
        // Information for target file name construction
        try {
            MetsMods fileFormat;
            if (ExportMode.VIEWER.equals(mode)) {
                fileFormat = new MetsModsImportExport(myPrefs);
                ((MetsModsImportExport) fileFormat).setRightsOwner(rightsOwner);
                ((MetsModsImportExport) fileFormat).setRightsOwnerLogo(rightsOwnerLogo);
                ((MetsModsImportExport) fileFormat).setRightsOwnerSiteURL(rightsOwnerSiteUrl);
            } else {
                fileFormat = new MetsMods(myPrefs);
            }

            String pi = eleRecord.getChild("header", null).getChildText("identifier", null).trim();
            if (pi.isEmpty()) {
                pi = "missing";
            }
            String piWithoutPrefix = pi.replace(oaiPrefix + ":", "");

            if (piWithoutPrefix.endsWith("map_sla")) {
                rootDocstrctType = "Map";
            } else if (piWithoutPrefix.endsWith("doc_sla")) {
                rootDocstrctType = "Monograph";
            }

            DigitalDocument dd = new DigitalDocument();
            fileFormat.setDigitalDocument(dd);

            /* BoundBook hinzufügen */
            DocStructType dst = myPrefs.getDocStrctTypeByName("BoundBook");
            DocStruct dsBoundBook = dd.createDocStruct(dst);
            dd.setPhysicalDocStruct(dsBoundBook);

            DocStructType dsTypeAnchor = myPrefs.getDocStrctTypeByName(rootDocstrctType);
            if (dsTypeAnchor == null) {
                log.error("Unknown root docstrct type, cannot proceed.");
                outcome.status = ExportOutcomeStatus.ERROR;
                outcome.message = "Unknown root docstrct type, cannot proceed.";
                return outcome;
            }

            MetadataType mdTypeCollection = myPrefs.getMetadataTypeByName("singleDigCollection");
            if (mdTypeCollection == null) {
                log.error("Metadata type 'singleDigCollection' not defined, cannot proceed.");
                outcome.status = ExportOutcomeStatus.ERROR;
                outcome.message = "Metadata type 'singleDigCollection' not defined, cannot proceed.";
                return outcome;
            }

            DocStruct dsAnchor = dd.createDocStruct(dsTypeAnchor);
            DocStruct dsPhysical = dd.getPhysicalDocStruct();
            dsAnchor.setIdentifier("root");
            dd.setLogicalDocStruct(dsAnchor);

            // Set identifier
            Metadata metaId = new Metadata(myPrefs.getMetadataTypeByName("CatalogIDDigital"));
            metaId.setValue(piWithoutPrefix);
            dsAnchor.addMetadata(metaId);

            for (Element eleMeta : eleRecord.getChild("metadata", null).getChild("dc", nsOaiDc).getChildren()) {
                boolean isPerson = false;
                String metaName = MapperDC.getUGHTerm(eleMeta.getName().trim().replace("dc:", ""));
                if (metaName == null) {
                    continue;
                }

                if (metaName.startsWith("_PERSON_")) {
                    metaName = metaName.replace("_PERSON_", "");
                    isPerson = true;
                }

                if ("CatalogIDDigital".equals(metaName)) {
                    if (dsAnchor.hasMetadataType(myPrefs.getMetadataTypeByName(metaName))) {
                        metaName = "CatalogIDSource";
                    }
                }

                MetadataType mdType = myPrefs.getMetadataTypeByName(metaName);
                if (isMetadataTypeAllowed(dsAnchor, mdType)) {
                    if (isPerson) {
                        Person person = new Person(mdType);
                        dsAnchor.addPerson(person);
                        person.setLastname(eleMeta.getValue().trim());
                        person.setFirstname("");
                        person.setRole(myPrefs.getMetadataTypeByName(metaName).getName());
                    } else {
                        Metadata metadata = new Metadata(mdType);
                        dsAnchor.addMetadata(metadata);
                        if ("DocLanguage".equals(metaName)) {
                            metadata.setValue(MapperDC.getUGHLanguageCode(eleMeta.getValue().trim()));
                        } else if ("shelfmarksource".equals(metaName) && eleMeta.getValue().contains("_")) {
                            String value = eleMeta.getValue().trim();
                            value = value.substring(0, value.indexOf("_"));
                            metadata.setValue(value);
                        } else {
                            metadata.setValue(eleMeta.getValue().trim());
                        }
                    }
                } else {
                    log.warn("Metadata type '" + metaName + "' is not allowed in the DocStrct type '" + dsAnchor.getType().getName() + "'.");
                }
            }

            // Add collection name to the volume from the method parameter, or set to defaultCollectionName if none was
            // given
            if (dsAnchor.getAllMetadataByType(mdTypeCollection).isEmpty()) {
                Metadata metaCollection = new Metadata(mdTypeCollection);
                dsAnchor.addMetadata(metaCollection);
                String collectionName1 = ParsingUtils.convertUmlaut(ConfigHarvester.getInstance().getCollectionNameDC1());
                String collectionName2 = ParsingUtils.convertUmlaut(ConfigHarvester.getInstance().getCollectionNameDC2());
                if (collectionName1 != null && collectionName1.length() > 0) {
                    metaCollection.setValue(collectionName1);
                } else {
                    metaCollection.setValue(DEFAULT_COLLECTION);
                }
                if (collectionName2 != null && collectionName2.length() > 0) {
                    Metadata metaCollection2 = new Metadata(mdTypeCollection);
                    dsAnchor.addMetadata(metaCollection2);
                    metaCollection2.setValue(collectionName2);
                }
            }

            // Files
            String folderName = imageTempFolder + File.separator + piWithoutPrefix;
            List<Element> eleRelations = eleRecord.getChild("metadata", null).getChild("dc", nsOaiDc).getChildren("relation", nsDc);
            List<String> fileNames = new ArrayList<>();
            String suffix = null;
            String imageFormat = "tiff";
            if (eleRelations != null && !eleRelations.isEmpty()) {
                FileSet fs = new FileSet();
                dd.setFileSet(fs);
                for (Element element : eleRelations) {
                    if (element.getText().isEmpty()) {
                        continue;
                    }

                    String[] relationUrlSplit = element.getText().trim().split("[/]");
                    String imageFolderUrl = "";
                    for (int i = 0; i < relationUrlSplit.length - 1; ++i) {
                        imageFolderUrl += relationUrlSplit[i] + "/";
                    }
                    // Determine file suffix
                    String[] baseFileNameSplit = relationUrlSplit[relationUrlSplit.length - 1].split("[.]");
                    suffix = baseFileNameSplit[baseFileNameSplit.length - 1];

                    List<String> imageNames = new ArrayList<>();
                    if ("djvu".equals(suffix)) {
                        imageNames = ParsingUtils.listImageFolderContents(imageFolderUrl, suffix);
                    } else {
                        imageNames.add(relationUrlSplit[relationUrlSplit.length - 1]);
                    }

                    // Download images for Goobi
                    log.info("Downloading {} images...", imageNames.size());
                    int counter = 0;
                    for (String imageName : imageNames) {
                        if (counter >= 9) {
                            break;
                        }
                        String newImageName = "";
                        byte[] imgBytes = ParsingUtils.fetchImage(imageFolderUrl + imageName);
                        if (imgBytes == null) {
                            continue;
                        }
                        File folder = new File(folderName);
                        if (!folder.exists()) {
                            folder.mkdirs();
                        }
                        newImageName = imageNameFormat.format(counter + 1);
                        newImageName += "." + suffix;
                        try (OutputStream out = new FileOutputStream(folderName + File.separator + newImageName)) {
                            out.write(imgBytes);
                            out.close();
                            log.debug("Downloaded image file '{}' to '{}/{}'", imageName, folderName, newImageName);
                            counter++;
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }

                        ContentFile cf = new ContentFile();
                        cf.setMimetype("image/" + imageFormat);
                        cf.setLocation(imageUrlLocal + "/" + piWithoutPrefix + "/" + newImageName);
                        fs.addFile(cf);
                        contentFileMap.put(imageNames.indexOf(imageName), cf);
                        File contentFile = new File(cf.getLocation());
                        fileNames.add(contentFile.getName());

                        if (suffix == null) {
                            // Read the file suffix so that it can be used in file groups with the same capitalization
                            int nameLength = cf.getLocation().length();
                            suffix = cf.getLocation().substring(nameLength - 3, nameLength);
                        }
                    }
                    log.info("Image download completed.");
                }
            } else {
                log.warn("No images found for this record.");
            }

            if (suffix == null) {
                suffix = "jpg";
            }

            DocStruct physicaldocstruct = dd.getPhysicalDocStruct();
            MetadataType MDTypeForPath = myPrefs.getMetadataTypeByName("pathimagefiles");

            try {
                Metadata mdForPath = new Metadata(MDTypeForPath);

                mdForPath.setValue("./" + piWithoutPrefix);
                physicaldocstruct.addMetadata(mdForPath);
            } catch (MetadataTypeNotAllowedException e1) {
                log.error("MetadataTypeNotAllowedException while reading images", e1);
            } catch (DocStructHasNoTypeException e1) {
                log.error("DocStructHasNoTypeException while reading images", e1);
            }
            dd.setPhysicalDocStruct(physicaldocstruct);

            if (ExportMode.VIEWER.equals(mode)) {
                addVirtualFileGroup(imageUrlLocal + piWithoutPrefix, dd, suffix, "image/" + imageFormat, "LOCAL");
                addVirtualFileGroup(imageUrlLocal + piWithoutPrefix, dd, suffix, "image/" + imageFormat, "PRESENTATION");
                addVirtualFileGroup(imageUrlRemote + piWithoutPrefix + "/800/0", dd, "jpg", "image/jpeg", "DEFAULT");
                addVirtualFileGroup(imageUrlRemote + piWithoutPrefix + "/600/0", dd, "jpg", "image/jpeg", "MIN");
                addVirtualFileGroup(imageUrlRemote + piWithoutPrefix + "/1000/0", dd, "jpg", "image/jpeg", "MAX");
                Collections.sort(fileNames, Collator.getInstance());
                dd.overrideContentFiles(fileNames);
            }

            // Pages

            log.debug("Adding " + contentFileMap.keySet().size() + " pages...");
            for (Integer i : contentFileMap.keySet()) {
                DocStruct dsPage = dd.createDocStruct(myPrefs.getDocStrctTypeByName("page"));
                dsPhysical.addChild(dsPage);
                Metadata metaPhysPageNumber = new Metadata(myPrefs.getMetadataTypeByName("physPageNumber"));
                dsPage.addMetadata(metaPhysPageNumber);
                metaPhysPageNumber.setValue(String.valueOf(i + 1));
                Metadata metaLogPageNumber = new Metadata(myPrefs.getMetadataTypeByName("logicalPageNumber"));
                dsPage.addMetadata(metaLogPageNumber);
                metaLogPageNumber.setValue(String.valueOf(i + 1));
                docStructPhysPageNumberMap.put(Integer.valueOf(metaPhysPageNumber.getValue()), dsPage);
                ContentFile cf = contentFileMap.get(i);
                if (cf != null) {
                    dsPage.addContentFile(cf);
                }
                dsAnchor.addReferenceTo(dsPage, "logical_physical");
            }

            myRdf = fileFormat;
            String outputFileName = piWithoutPrefix;
            writeFileformat(myRdf, outputFileName, folderName);
            return outcome;
        } catch (TypeNotAllowedForParentException | TypeNotAllowedAsChildException | PreferencesException | MetadataTypeNotAllowedException
                | WriteException | IOException e) {
            log.error(e.getMessage(), e);
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = e.getMessage();
            return outcome;
        }
    }

    /**
     * Creates a METS file from the given <code>Fileformat</code>, then copies all images from the given original image folder name to a new folder
     * named after the METS file.
     * 
     * @param ff
     * @param fileName
     * @param imagePath
     * @throws PreferencesException
     * @throws WriteException
     * @throws IOException
     */
    public void writeFileformat(Fileformat ff, String fileName, String imagePath) throws WriteException, PreferencesException, IOException {
        File outputDir;
        if (ExportMode.VIEWER.equals(mode)) {
            outputDir = new File(hotfolderViewer);
        } else {
            outputDir = new File(hotfolderGoobi);
        }
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        fileName = outputDir.getAbsolutePath() + File.separator + fileName;

        // Move images
        File imageFolder = new File(imagePath);
        if (imageFolder.isDirectory()) {
            File[] imageFiles = imageFolder.listFiles();
            String folderName = fileName;
            File newImageFolder = new File(folderName);
            newImageFolder.mkdirs();
            int counter = 0;
            for (File oldImageFile : imageFiles) {
                File newImageFile = new File(folderName + File.separator + oldImageFile.getName());
                FileInputStream fis = new FileInputStream(oldImageFile);
                FileOutputStream fos = new FileOutputStream(newImageFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                fis.close();
                fos.close();
                oldImageFile.delete();
                counter++;
            }
            imageFolder.delete();
            log.info(counter + " image files moved to '" + newImageFolder.getAbsolutePath() + "'");
        } else {
            log.warn("'" + imageFolder.getAbsolutePath() + "' does not exist or is not a directory - no images copied.");
        }

        // Write METS file
        if (ff.write(fileName + ".xml")) {
            log.info("Successfully written '" + fileName + ".xml'.");
        }
    }
}
