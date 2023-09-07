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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sub.goobi.config.ConfigHarvester;
import io.goobi.workflow.harvester.export.ExportOutcome.ExportOutcomeStatus;
import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.MetsModsImportExport;

@SuppressWarnings("deprecation")
public class ConverterFindbuchEAD extends AbstractConverter {

    /** Loggers for this class. */
    private static final Logger logger = LoggerFactory.getLogger(ConverterFindbuchEAD.class);

    private static final String COLLECTION_LEVEL_0 = "Archive";

    /**
     * Map that holds all physical DocStructs with their physical number IDs as keys (for sorted referencing from the top DocStruct).
     */
    private Map<Integer, DocStruct> docStructPhysPageNumberMap;

    /** Map that holds ContentFiles with DC identifiers as keys. */
    private Map<Integer, ContentFile> contentFileMap;

    private String collectionLevel1;

    public ConverterFindbuchEAD(Document doc, ExportMode mode, String rootDocstrctType, String repositoryType, String collectionLevel1) {
        super(doc, mode, repositoryType);
        this.collectionLevel1 = collectionLevel1;
    }

    @Override
    public ExportOutcome createNewFileformat() {
        ExportOutcome outcome = new ExportOutcome();

        List<String> collectionLevelList = new ArrayList<>();
        collectionLevelList.add(COLLECTION_LEVEL_0);
        collectionLevelList.add(MapperFindbuchEAD.getUGHTerm(collectionLevel1.toLowerCase()));

        if (doc == null) {
            logger.error("XML doc is null.");
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = "XML doc is null.";
            return outcome;
        }
        if (!doc.hasRootElement()) {
            logger.error("XML doc has no root element.");
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = "XML doc has no root element.";
            return outcome;
        }

        Fileformat myRdf = null;
        docStructPhysPageNumberMap = new HashMap<>();
        contentFileMap = new HashMap<>();

        Element eleEadHeader = doc.getRootElement().getChild("eadheader", null);
        if (eleEadHeader == null) {
            logger.error("EAD document has no <eadheader> element.");
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = "EAD document has no <eadheader> element.";
            return outcome;
        }

        // Level 2 collection name (Bestand)
        String collectionLevel2 = eleEadHeader.getChild("filedesc", null).getChild("titlestmt", null).getChildText("titleproper", null);
        if (StringUtils.isNotEmpty(collectionLevel2)) {
            collectionLevel2 = collectionLevel2.replace("Tektonik:", "");
            collectionLevel2 = collectionLevel2.replace("Signatur:", "");
            collectionLevelList.add(collectionLevel2.trim());
        }

        Element eleArchDesc = doc.getRootElement().getChild("archdesc", null);
        if (eleArchDesc == null) {
            logger.error("EAD document has no <archdesc> element.");
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = "EAD document has no <archdesc> element.";
            return outcome;
        }

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

            DigitalDocument dd = new DigitalDocument();
            fileFormat.setDigitalDocument(dd);

            DocStructType dst = myPrefs.getDocStrctTypeByName("BoundBook");
            DocStruct dsBoundBook = dd.createDocStruct(dst);
            dd.setPhysicalDocStruct(dsBoundBook);

            MetadataType mdTypeCollection = myPrefs.getMetadataTypeByName("singleDigCollection");
            if (mdTypeCollection == null) {
                logger.error("Metadata type 'singleDigCollection' not defined, cannot proceed.");
                outcome.status = ExportOutcomeStatus.ERROR;
                outcome.message = "Metadata type 'singleDigCollection' not defined, cannot proceed.";
                return outcome;
            }

            String identifier = eleArchDesc.getChild("did", null).getChild("unitid", null).getAttributeValue("identifier");

            // C01
            Element eleC01 = (Element) XPath.selectSingleNode(eleArchDesc, "//c01");
            if (eleC01 == null) {
                logger.error("No c01 element found.");
                outcome.status = ExportOutcomeStatus.ERROR;
                outcome.message = "No c01 element found.";
                return outcome;
            }

            identifier += "_" + eleC01.getChild("did", null).getChild("unitid", null).getAttributeValue("identifier");

            // Level 3 collection name (Klassifikation)
            String collectionLevel3 = eleC01.getChild("did", null).getChildText("unittitle", null);
            // String collectionNumber = null;
            if (StringUtils.isNotEmpty(collectionLevel3)) {
                // String collectionLevel3Split[] = collectionLevel3.split("[-]");
                // if (collectionLevel3Split.length > 1) {
                // collectionNumber = collectionLevel3Split[0].trim();
                // collectionLevel3 = collectionLevel3Split[collectionLevel3Split.length - 1].trim().toLowerCase();
                // }
                collectionLevelList.add(collectionLevel3.trim());
            }

            Element eleC02 = eleC01.getChild("c02");
            if (eleC02 == null) {
                logger.error("No c02 element found.");
                outcome.status = ExportOutcomeStatus.ERROR;
                outcome.message = "No c02 element found.";
                return outcome;
            }
            Element eleVolume = eleC02;

            identifier += "_" + eleC02.getChild("did", null).getChild("unitid", null).getAttributeValue("identifier");

            DocStruct dsPhysical = dd.getPhysicalDocStruct();
            DocStructType dsTypeAnchor = myPrefs.getDocStrctTypeByName("VolumeRun");
            DocStructType dsTypeVolume;
            DocStruct dsAnchor = null;
            DocStruct dsVolume = null;

            // Multivolume
            if (StringUtils.isEmpty(eleC02.getAttributeValue("level"))) {
                dsAnchor = dd.createDocStruct(dsTypeAnchor);
                dsAnchor.addReferenceTo(dsPhysical, "logical_physical");
                dd.setLogicalDocStruct(dsAnchor);

                Element eleC03 = eleC02.getChild("c03");
                if (eleC03 == null) {
                    logger.error("No c03 element found.");
                    outcome.status = ExportOutcomeStatus.ERROR;
                    outcome.message = "No c03 element found.";
                    return outcome;
                }
                eleVolume = eleC03;

                // Idenfifier
                Metadata metaId = new Metadata(myPrefs.getMetadataTypeByName("CatalogIDDigital"));
                if (StringUtils.isEmpty(identifier)) {
                    identifier = "missing";
                }
                metaId.setValue(identifier);
                dsAnchor.addMetadata(metaId);

                // DID metadata
                for (Object objDid : eleC02.getChild("did", null).getChildren()) {
                    Element eleDid = (Element) objDid;
                    addMetadata(dd, dsAnchor, eleDid);
                }
            }

            // Volume
            {
                String volumeType = MapperFindbuchEAD.getUGHTerm(eleVolume.getAttributeValue("encodinganalog"));
                if (volumeType == null) {
                    logger.error("No docstrct type mapped for '" + eleVolume.getAttributeValue("encodinganalog") + "'.");
                    outcome.status = ExportOutcomeStatus.ERROR;
                    outcome.message = "No docstrct type mapped for '" + eleVolume.getAttributeValue("encodinganalog") + "'.";
                    return outcome;
                }
                logger.debug("Volume docstrct type is '" + volumeType + "'.");
                dsTypeVolume = myPrefs.getDocStrctTypeByName(volumeType);
                if (dsTypeVolume == null) {
                    logger.error("Unknown root docstrct type, cannot proceed.");
                    outcome.status = ExportOutcomeStatus.ERROR;
                    outcome.message = "Unknown root docstrct type, cannot proceed.";
                    return outcome;
                }
                dsVolume = dd.createDocStruct(dsTypeVolume);

                if (dsAnchor == null) {
                    dsVolume.addReferenceTo(dsPhysical, "logical_physical");
                    dd.setLogicalDocStruct(dsVolume);
                } else {
                    identifier += "_" + eleVolume.getChild("did", null).getChild("unitid").getAttributeValue("identifier");
                }

                // Identifier
                Metadata metaVolumeIdentifier = new Metadata(myPrefs.getMetadataTypeByName("CatalogIDDigital"));
                if (identifier.isEmpty()) {
                    identifier = "missing";
                }
                String piWithoutColon = identifier.replaceAll("[:]", ".");
                metaVolumeIdentifier.setValue(piWithoutColon);
                dsVolume.addMetadata(metaVolumeIdentifier);

                // did metadata
                if (eleVolume.getChild("did", null) != null) {
                    for (Object objDid : eleVolume.getChild("did", null).getChildren()) {
                        Element eleDid = (Element) objDid;
                        if (eleDid.getAttributeValue("encodinganalog") == null) {
                            if (eleDid.getChildren() != null || eleDid.getChildren().size() > 0) {
                                for (Object objDidLower : eleDid.getChildren()) {
                                    Element eleDidLower = (Element) objDidLower;
                                    addMetadata(dd, dsVolume, eleDidLower);
                                }
                                continue;
                            }
                        }
                        addMetadata(dd, dsVolume, eleDid);
                    }
                }

                // scopecontent metadata
                if (eleVolume.getChild("scopecontent", null) != null) {
                    for (Object objScopecontent : eleVolume.getChild("scopecontent", null).getChildren()) {
                        Element eleScopecontent = (Element) objScopecontent;
                        if (eleScopecontent.getAttributeValue("encodinganalog") == null) {
                            continue;
                        }
                        addMetadata(dd, dsVolume, eleScopecontent);
                    }
                }

                // odd metadata
                if (eleVolume.getChild("odd", null) != null) {
                    for (Object objList : eleVolume.getChild("odd", null).getChildren()) {
                        Element eleList = (Element) objList;
                        for (Object objItem : eleList.getChildren("item", null)) {
                            Element eleItem = (Element) objItem;
                            Element eleName = eleItem.getChild("name", null);
                            if (eleName.getAttributeValue("encodinganalog") == null) {
                                continue;
                            }
                            addMetadata(dd, dsVolume, eleName);
                        }
                    }
                }

                // Add collection name to the volume from the method parameter, or set to defaultCollectionName if none was
                // given
                if (dsVolume.getAllMetadataByType(mdTypeCollection).isEmpty()) {
                    Metadata metaCollection = new Metadata(mdTypeCollection);
                    dsVolume.addMetadata(metaCollection);
                    String collectionName1 = ConfigHarvester.getInstance().getCollectionNameDC1();
                    // String collectionName2 = DataManager.getInstance().getConfiguration().getCollectionNameDC2();
                    String separator = ConfigHarvester.getInstance().getCollectionSeparator();
                    String collectionName = "";
                    if (collectionLevelList.size() > 0) {
                        for (String s : collectionLevelList) {
                            collectionName += s + separator;
                            // if (collectionLevelList.indexOf(collectionName) > 0) {
                            // collectionName = collectionNumber + " " + collectionName;
                            // }
                        }
                        collectionName = collectionName.substring(0, collectionName.length() - 1);
                    } else if (StringUtils.isNotEmpty(collectionName1)) {
                        collectionName = collectionName1;
                    } else {
                        collectionName = defaultCollectionName;
                    }
                    collectionName = ParsingUtils.convertUmlaut(collectionName);
                    metaCollection.setValue(collectionName);
                }

                // Files
                String folderName = imageTempFolder + File.separator + piWithoutColon + "_tif";

                // Pages
                {
                    for (Integer i : contentFileMap.keySet()) {
                        DocStruct dsPage = dd.createDocStruct(myPrefs.getDocStrctTypeByName("page"));
                        dsPhysical.addChild(dsPage);
                        Metadata meta = new Metadata(myPrefs.getMetadataTypeByName("physPageNumber"));
                        dsPage.addMetadata(meta);
                        meta.setValue(String.valueOf(i));
                        docStructPhysPageNumberMap.put(Integer.valueOf(meta.getValue()), dsPage);
                        ContentFile cf = contentFileMap.get(i);
                        if (cf != null) {
                            dsPage.addContentFile(cf);
                        }
                        dsVolume.addReferenceTo(dsPage, "logical_physical");
                    }
                }
                // if (true)
                // return outcome;

                // Add 'pathimagefiles'
                Metadata mdForPath = new Metadata(myPrefs.getMetadataTypeByName("pathimagefiles"));
                mdForPath.setValue("./" + piWithoutColon + "_tif");
                dsBoundBook.addMetadata(mdForPath);

                myRdf = fileFormat;
                String outputFileName = piWithoutColon;
                writeFileformat(myRdf, outputFileName, folderName);
            }

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
        } catch (JDOMException e) {
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
     */
    public void writeFileformat(Fileformat ff, String fileName, String imagePath) {
        // File outputDir = new File("output");
        File outputDir = new File(hotfolderGoobi);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        fileName = outputDir.getAbsolutePath() + File.separator + fileName;

        // Move images
        try {
            File imageFolder = new File(imagePath);
            if (imageFolder.isDirectory()) {
                File[] imageFiles = imageFolder.listFiles();
                String folderName = fileName + "_tif";
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
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        // Write METS file
        try {
            if (ff.write(fileName + ".xml")) {
                logger.info("Successfully written '" + fileName + ".xml'.");
                // MetsMods myRdf2 = new MetsMods(myPrefs);
                // myRdf2.read("MetsMods.xml");
                // loggerInfo.info("fileformat: " + myRdf2);
                // loggerInfo.info("digDoc: " + myRdf2.getDigitalDocument());
                // loggerInfo.info("logDocStrct: " + myRdf2.getDigitalDocument().getLogicalDocStruct());
            }
        } catch (WriteException e) {
            logger.error(e.getMessage(), e);
        } catch (PreferencesException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 
     * @param dsLogical
     * @param eleDid
     * @return
     * @throws MetadataTypeNotAllowedException
     * @throws TypeNotAllowedAsChildException
     * @throws TypeNotAllowedForParentException
     */
    private boolean addMetadata(DigitalDocument dd, DocStruct dsLogical, Element eleDid) throws MetadataTypeNotAllowedException,
    TypeNotAllowedAsChildException, TypeNotAllowedForParentException {
        boolean isPerson = false;
        String metaName = eleDid.getAttributeValue("encodinganalog");
        String value = null;
        if (eleDid.getChild("p", null) != null) {
            value = eleDid.getChildText("p", null).trim();
            if (eleDid.getChild("head", null) != null) {
                metaName = eleDid.getChildText("head", null);
            }
        } else {
            value = eleDid.getValue().trim();
        }
        if (metaName == null) {
            return false;
        }

        String metaNameUGH = MapperFindbuchEAD.getUGHTerm(metaName.trim());
        if (metaNameUGH != null && metaNameUGH.startsWith("_PERSON_")) {
            metaNameUGH = metaNameUGH.replace("_PERSON_", "");
            isPerson = true;
            if (metaNameUGH.equals("Creator") && dsLogical.getType().getName().equals("Picture")) {
                metaNameUGH = "Photographer";
            }
        }

        if (metaNameUGH != null && metaNameUGH.equals("Dating") && dsLogical.getType().getName().equals("Record")) {
            metaNameUGH = "Period";
        }
        MetadataType mdType = myPrefs.getMetadataTypeByName(metaNameUGH);
        if (mdType != null) {
            if (mdType.getName().equals("SealDescription")) {
                // For seal metadata, add a new child docstruct
                DocStructType dsTypeSeal = myPrefs.getDocStrctTypeByName("Seal");
                DocStruct dsSeal = null;
                if (dsLogical.getAllChildren() != null) {
                    for (DocStruct ds : dsLogical.getAllChildren()) {
                        if (ds.getType().equals(dsTypeSeal)) {
                            dsSeal = ds;
                            break;
                        }
                    }
                }
                if (dsSeal == null) {
                    dsSeal = dd.createDocStruct(dsTypeSeal);
                    dsLogical.addChild(dsSeal);
                }
                Metadata metadata = new Metadata(mdType);
                dsSeal.addMetadata(metadata);
                metadata.setValue(value);
                return true;
            } else if (isMetadataTypeAllowed(dsLogical, mdType)) {
                if (isPerson) {
                    Person person = new Person(mdType);
                    dsLogical.addPerson(person);
                    person.setLastname(value);
                    person.setFirstname("");
                    person.setRole(mdType.getName());
                } else {
                    Metadata metadata = new Metadata(mdType);
                    if (metaNameUGH.equals("DocLanguage")) {
                        dsLogical.addMetadata(metadata);
                        metadata.setValue(MapperFindbuchEAD.getUGHLanguageCode(value));
                    } else if (metaNameUGH.equals("shelfmarksource")) {
                        // dd.getPhysicalDocStruct().addMetadata(metadata);
                        dsLogical.addMetadata(metadata);
                        metadata.setValue(value);
                    } else {
                        dsLogical.addMetadata(metadata);
                        metadata.setValue(value);
                    }
                }
                return true;
            } else {
                logger.warn("Metadata '" + metaNameUGH + "' is not allowed in docstrct '" + dsLogical.getType().getName() + "'.");
                return false;
            }
        }

        logger.warn("Metadata type '" + metaName + "' is not mapped.");
        return false;
    }
}
