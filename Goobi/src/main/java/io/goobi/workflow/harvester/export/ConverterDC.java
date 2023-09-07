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
import java.io.FileNotFoundException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sub.goobi.config.ConfigHarvester;
import io.goobi.workflow.harvester.export.ExportOutcome.ExportOutcomeStatus;
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

public class ConverterDC extends AbstractConverter {

    /** Loggers for this class. */
    private static final Logger logger = LoggerFactory.getLogger(ConverterDC.class);

    /**
     * Map that holds all physical DocStructs with their physical number IDs as keys (for sorted referencing from the top DocStruct).
     */
    private Map<Integer, DocStruct> docStructPhysPageNumberMap;

    /** Map that holds ContentFiles with DC identifiers as keys. */
    private Map<Integer, ContentFile> contentFileMap;

    private String rootDocstrctType;

    public ConverterDC(Document doc, ExportMode mode, String rootDocstrctType, String repositoryType) {
        super(doc, mode, repositoryType);
        this.rootDocstrctType = rootDocstrctType;
    }

    @Override
    public ExportOutcome createNewFileformat() {
        ExportOutcome outcome = new ExportOutcome();
        if (doc == null) {
            logger.error("XML doc is null.");
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = "XML doc is null.";
            return outcome;
        }

        Fileformat myRdf = null;
        docStructPhysPageNumberMap = new HashMap<>();
        contentFileMap = new HashMap<>();

        if (doc.getRootElement().getChild("error", null) != null) {
            logger.error("Error: {}", doc.getRootElement().getChild("error", null).getAttributeValue("code"));
            outcome.status = ExportOutcomeStatus.NOT_FOUND;
            return outcome;
        }
        Element eleListRecords = doc.getRootElement().getChild("GetRecord", null);
        if (eleListRecords == null) {
            logger.warn("No 'GetRecord' element found .");
            outcome.status = ExportOutcomeStatus.NOT_FOUND;
            return outcome;
        }
        List<Element> recordList = eleListRecords.getChildren("record", null);
        if (recordList.size() == 0) {
            logger.warn("No 'record' elements found.");
            outcome.status = ExportOutcomeStatus.NOT_FOUND;
            return outcome;
        }

        Element eleRecord = recordList.get(0);
        // for (Element eleRecord : recordList) {
        // Information for target file name construction
        try {
            // MetsMods fileFormat = new MetsMods(myPrefs);
            MetsMods fileFormat;
            if (mode.equals(ExportMode.VIEWER)) {
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
                logger.error("Unknown root docstrct type, cannot proceed.");
                outcome.status = ExportOutcomeStatus.ERROR;
                outcome.message = "Unknown root docstrct type, cannot proceed.";
                return outcome;
            }

            MetadataType mdTypeCollection = myPrefs.getMetadataTypeByName("singleDigCollection");
            if (mdTypeCollection == null) {
                logger.error("Metadata type 'singleDigCollection' not defined, cannot proceed.");
                outcome.status = ExportOutcomeStatus.ERROR;
                outcome.message = "Metadata type 'singleDigCollection' not defined, cannot proceed.";
                return outcome;
            }

            DocStruct dsAnchor = dd.createDocStruct(dsTypeAnchor);
            DocStruct dsPhysical = dd.getPhysicalDocStruct();
            // dsAnchor.addReferenceTo(dsPhysical, "logical_physical");
            dsAnchor.setIdentifier("root");
            dd.setLogicalDocStruct(dsAnchor);
            // docStructAgoraIdMap.put(agoraDoc.getAnchorElement().getAttributeValue("ID"), dsAnchor);

            // Set identifier
            Metadata metaId = new Metadata(myPrefs.getMetadataTypeByName("CatalogIDDigital"));
            metaId.setValue(piWithoutPrefix);
            dsAnchor.addMetadata(metaId);

            for (Object eleMetaObj : eleRecord.getChild("metadata", null).getChild("dc", nsOaiDc).getChildren()) {
                boolean isPerson = false;
                Element eleMeta = (Element) eleMetaObj;
                String metaName = MapperDC.getUGHTerm(eleMeta.getName().trim().replace("dc:", ""));
                if (metaName == null) {
                    continue;
                }

                if (metaName.startsWith("_PERSON_")) {
                    metaName = metaName.replace("_PERSON_", "");
                    isPerson = true;
                }

                if (metaName.equals("CatalogIDDigital")) {
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
                        if (metaName.equals("DocLanguage")) {
                            metadata.setValue(MapperDC.getUGHLanguageCode(eleMeta.getValue().trim()));
                        } else if (metaName.equals("shelfmarksource") && eleMeta.getValue().contains("_")) {
                            String value = eleMeta.getValue().trim();
                            value = value.substring(0, value.indexOf("_"));
                            metadata.setValue(value);
                        } else {
                            metadata.setValue(eleMeta.getValue().trim());
                        }
                    }
                } else {
                    logger.warn("Metadata type '" + metaName + "' is not allowed in the DocStrct type '" + dsAnchor.getType().getName() + "'.");
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
                    metaCollection.setValue(defaultCollectionName);
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
            if (eleRelations != null && eleRelations.size() > 0) {
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
                    if (suffix.equals("djvu")) {
                        imageNames = ParsingUtils.listImageFolderContents(imageFolderUrl, suffix);
                    } else {
                        imageNames.add(relationUrlSplit[relationUrlSplit.length - 1]);
                    }

                    // http://www.dhm.uni-greifswald.de/textband/band_42/directory_Band_42.djvu

                    // if (mode.equals(UGHMode.VIEWER)) {
                    // // Add URLs for the viewer
                    // for (String imageName : imageNames) {
                    // String imageUrl = imageFolderUrl + imageName;
                    // ContentFile cf = new ContentFile();
                    // cf.setMimetype("image/" + imageFormat);
                    // logger.debug(element.getText().trim());
                    // cf.setLocation(imageUrl);
                    // fs.addFile(cf);
                    // contentFileMap.put(counter, cf);
                    // File contentFile = new File(cf.getLocation());
                    // if (contentFile != null) {
                    // fileNames.add(contentFile.getName());
                    // logger.debug("contentFile: " + contentFile.getName());
                    // }
                    //
                    // if (suffix == null) {
                    // // Read the file suffix so that it can be used in file groups with the same capitalization
                    // int nameLength = cf.getLocation().length();
                    // suffix = cf.getLocation().substring(nameLength - 3, nameLength);
                    // logger.debug("File suffix: " + suffix);
                    // }
                    // }
                    // } else {
                    // Download images for Goobi
                    logger.info("Downloading {} images...",  imageNames.size());
                    int counter = 0;
                    for (String imageName : imageNames) {
                        if (counter >= 9) {
                            break;
                        }
                        // byte[] imgBytes = null;
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
                            logger.debug("Downloaded image file '{}' to '{}/{}'", imageName, folderName, newImageName);
                            // TODO convert to TIFF here?
                            counter++;
                        } catch (FileNotFoundException e) {
                            logger.error(e.getMessage(), e);
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }

                        ContentFile cf = new ContentFile();
                        cf.setMimetype("image/" + imageFormat);
                        // logger.debug(element.getText().trim());
                        cf.setLocation(imageUrlLocal + "/" + piWithoutPrefix + "/" + newImageName);
                        fs.addFile(cf);
                        contentFileMap.put(imageNames.indexOf(imageName), cf);
                        File contentFile = new File(cf.getLocation());
                        fileNames.add(contentFile.getName());
                        // logger.debug("contentFile: " + contentFile.getName());

                        if (suffix == null) {
                            // Read the file suffix so that it can be used in file groups with the same capitalization
                            int nameLength = cf.getLocation().length();
                            suffix = cf.getLocation().substring(nameLength - 3, nameLength);
                            // logger.debug("File suffix: " + suffix);
                        }
                    }
                    logger.info("Image download completed.");
                }
            } else {
                logger.warn("No images found for this record.");
            }

            if (suffix == null) {
                suffix = "jpg";
            }

            DocStruct physicaldocstruct = dd.getPhysicalDocStruct();
            MetadataType MDTypeForPath = myPrefs.getMetadataTypeByName("pathimagefiles");

            try {
                Metadata mdForPath = new Metadata(MDTypeForPath);

                // mdForPath.setType(MDTypeForPath);
                // TODO: add the possibility for using other image formats
                mdForPath.setValue("./" + piWithoutPrefix);
                physicaldocstruct.addMetadata(mdForPath);
            } catch (MetadataTypeNotAllowedException e1) {
                logger.error("MetadataTypeNotAllowedException while reading images", e1);
            } catch (DocStructHasNoTypeException e1) {
                logger.error("DocStructHasNoTypeException while reading images", e1);
            }
            dd.setPhysicalDocStruct(physicaldocstruct);

            if (mode.equals(ExportMode.VIEWER)) {
                addVirtualFileGroup(imageUrlLocal + piWithoutPrefix, dd, suffix, "image/" + imageFormat, "LOCAL");
                addVirtualFileGroup(imageUrlLocal + piWithoutPrefix, dd, suffix, "image/" + imageFormat, "PRESENTATION");
                addVirtualFileGroup(imageUrlRemote + piWithoutPrefix + "/800/0", dd, "jpg", "image/jpeg", "DEFAULT");
                addVirtualFileGroup(imageUrlRemote + piWithoutPrefix + "/600/0", dd, "jpg", "image/jpeg", "MIN");
                addVirtualFileGroup(imageUrlRemote + piWithoutPrefix + "/1000/0", dd, "jpg", "image/jpeg", "MAX");
                Collections.sort(fileNames, Collator.getInstance());
                dd.overrideContentFiles(fileNames);
            }

            // Pages
            {
                logger.debug("Adding " + contentFileMap.keySet().size() + " pages...");
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
            }

            // if (true)
            // return ExportOutcome.OK;

            myRdf = fileFormat;
            String outputFileName = piWithoutPrefix;
            writeFileformat(myRdf, outputFileName, folderName);
            return outcome;
        } catch (TypeNotAllowedForParentException e) {
            logger.error(e.getMessage(), e);
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = e.getMessage();
            return outcome;
        } catch (TypeNotAllowedAsChildException e) {
            logger.error(e.getMessage(), e);
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = e.getMessage();
            return outcome;
        } catch (PreferencesException e) {
            logger.error(e.getMessage(), e);
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = e.getMessage();
            return outcome;
        } catch (MetadataTypeNotAllowedException e) {
            logger.error(e.getMessage(), e);
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = e.getMessage();
            return outcome;
        } catch (WriteException e) {
            logger.error(e.getMessage(), e);
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = e.getMessage();
            return outcome;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
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
        // File outputDir = new File("output");
        File outputDir;
        if (mode.equals(ExportMode.VIEWER)) {
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
                // File newImageFile = new File(folderName + File.separator +
                // convertImageFileName(oldImageFile.getName()));
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
            logger.info(counter + " image files moved to '" + newImageFolder.getAbsolutePath() + "'");
        } else {
            logger.warn("'" + imageFolder.getAbsolutePath() + "' does not exist or is not a directory - no images copied.");
        }

        // Write METS file
        if (ff.write(fileName + ".xml")) {
            logger.info("Successfully written '" + fileName + ".xml'.");
            // MetsMods myRdf2 = new MetsMods(myPrefs);
            // myRdf2.read("MetsMods.xml");
            // loggerInfo.info("fileformat: " + myRdf2);
            // loggerInfo.info("digDoc: " + myRdf2.getDigitalDocument());
            // loggerInfo.info("logDocStrct: " + myRdf2.getDigitalDocument().getLogicalDocStruct());
        }
    }
}
