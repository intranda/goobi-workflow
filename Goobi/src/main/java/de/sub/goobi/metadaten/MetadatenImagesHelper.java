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
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package de.sub.goobi.metadaten;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.goobi.beans.Process;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.S3FileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManipulatorException;
import de.unigoettingen.sub.commons.contentlib.imagelib.ImageInterpreter;
import de.unigoettingen.sub.commons.contentlib.imagelib.ImageManager;
import de.unigoettingen.sub.commons.contentlib.imagelib.JpegInterpreter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.dl.Reference;
import ugh.dl.RomanNumeral;
import ugh.exceptions.ContentFileNotLinkedException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;

@Log4j2
public class MetadatenImagesHelper {

    private static final String DEFAULT_FILE_PROTOCOL = "file://";
    private static final String WINDOWS_FILE_PROTOCOL = "file:/";

    private static final String METADATA_PHYSICAL_PAGE_NUMBER = "physPageNumber";
    private static final String METADATA_LOGICAL_PAGE_NUMBER = "logicalPageNumber";

    private static DocStructType docStructPage;
    private static DocStructType docStructAudio;
    private static DocStructType docStructVideo;
    private static DocStructType docStructObject;

    private Prefs myPrefs;
    private DigitalDocument mydocument;
    private int myLastImage = 0;

    public MetadatenImagesHelper(Prefs inPrefs, DigitalDocument inDocument) {
        this.myPrefs = inPrefs;
        this.mydocument = inDocument;
    }

    public List<String> checkImageNames(Process myProzess, String directoryName)
            throws TypeNotAllowedForParentException, SwapException, DAOException, IOException {
        DocStruct physical = this.mydocument.getPhysicalDocStruct();

        if (physical == null) {
            createPagination(myProzess, directoryName);
            return Collections.emptyList();
        }
        // get image names in directory

        Path folder;
        if (directoryName == null) {
            folder = Paths.get(myProzess.getImagesTifDirectory(true));
        } else {
            folder = Paths.get(myProzess.getImagesDirectory() + directoryName);
        }

        List<String> imagenames = StorageProvider.getInstance().list(folder.toString(), NIOFileUtils.imageNameFilter);
        if (imagenames == null || imagenames.isEmpty()) {
            // no images found, return
            return Collections.emptyList();
        }

        // get image names in nets file

        Map<String, DocStruct> imageNamesInMetsFile = new HashMap<>();

        List<DocStruct> pages = physical.getAllChildren();
        if (pages != null && !pages.isEmpty()) {
            for (DocStruct page : pages) {
                String filename = page.getImageName();
                if (filename != null) {
                    if (filename.contains(FileSystems.getDefault().getSeparator())) {
                        filename = filename.substring(filename.lastIndexOf(FileSystems.getDefault().getSeparator()));
                    }
                    imageNamesInMetsFile.put(filename, page);
                } else {
                    log.error("cannot find image");
                }
            }
        }

        // if size differs, create new pagination
        if (imagenames.size() != imageNamesInMetsFile.size()) {
            createPagination(myProzess, directoryName);
            return Collections.emptyList();
        }

        List<String> imagesWithoutDocstruct = new LinkedList<>();
        List<DocStruct> pagesWithoutFiles = new LinkedList<>();

        // search for page objects with invalid image names
        for (Map.Entry<String, DocStruct> entry : imageNamesInMetsFile.entrySet()) {
            String currentImagePrefix = entry.getKey().replace(Metadaten.getFileExtension(entry.getKey()), "");
            boolean match = false;
            for (String imageNameInDirectory : imagenames) {
                if (currentImagePrefix.equals(imageNameInDirectory.replace(Metadaten.getFileExtension(imageNameInDirectory), ""))) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                if (log.isDebugEnabled()) {
                    log.debug("adding docstruct with missing file " + entry.getKey() + " to abandoned list.");
                }
                pagesWithoutFiles.add(entry.getValue());
            }
        }

        // find abandoned image names in directory
        if (!pagesWithoutFiles.isEmpty()) {

            Collections.sort(pagesWithoutFiles, (docstruct1, docstruct2) -> {

                MetadataType mdt = myPrefs.getMetadataTypeByName(METADATA_PHYSICAL_PAGE_NUMBER);
                String value1 = docstruct1.getAllMetadataByType(mdt).get(0).getValue();
                String value2 = docstruct2.getAllMetadataByType(mdt).get(0).getValue();
                Integer order1 = Integer.parseInt(value1);
                Integer order2 = Integer.parseInt(value2);
                return order1.compareTo(order2);
            });

            for (String imagename : imagenames) {
                String currentImagePrefix = imagename.replace(Metadaten.getFileExtension(imagename), "");
                boolean match = false;
                for (String key : imageNamesInMetsFile.keySet()) {
                    if (currentImagePrefix.equals(key.replace(Metadaten.getFileExtension(key), ""))) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    if (log.isDebugEnabled()) {
                        log.debug("adding " + imagename + " to list of images without docstructs");
                    }
                    imagesWithoutDocstruct.add(imagename);
                }
            }
        }

        // both lists should have the same size
        if (pagesWithoutFiles.size() != imagesWithoutDocstruct.size()) {
            return Collections.emptyList();
        }

        int counter = pagesWithoutFiles.size();
        for (int i = 0; i < counter; i++) {
            String currentFile = imagesWithoutDocstruct.get(i);
            DocStruct currentPage = pagesWithoutFiles.get(i);
            currentPage.setImageName(folder.toString() + FileSystems.getDefault().getSeparator() + currentFile);
            if (log.isDebugEnabled()) {
                log.debug("set image " + currentFile + " to docstruct "
                        + currentPage.getAllMetadataByType(myPrefs.getMetadataTypeByName(METADATA_PHYSICAL_PAGE_NUMBER)).get(0).getValue());
            }
        }
        return imagenames;
    }

    /**
     * Markus baut eine Seitenstruktur aus den vorhandenen Images ---------------- Steps - ---------------- Validation of images compare existing
     * number images with existing number of page DocStructs if it is the same don't do anything if DocStructs are less add new pages to
     * physicalDocStruct if images are less delete pages from the end of pyhsicalDocStruct --------------------------------
     * 
     * @return null
     * @throws TypeNotAllowedForParentException
     * @throws IOException
     * @throws DAOException
     * @throws SwapException
     */
    public void createPagination(Process inProzess, String directory)
            throws TypeNotAllowedForParentException, IOException, SwapException, DAOException {
        String mediaFolder = inProzess.getImagesTifDirectory(false);
        String mediaFolderWithFallback = inProzess.getImagesTifDirectory(true);

        DocStruct physicaldocstruct = this.mydocument.getPhysicalDocStruct();

        DocStruct logical = this.mydocument.getLogicalDocStruct();
        if (logical.getType().isAnchor() && logical.getAllChildren() != null && !logical.getAllChildren().isEmpty()) {
            logical = logical.getAllChildren().get(0);
        }
        MetadataType metadataTypeForPath = this.myPrefs.getMetadataTypeByName("pathimagefiles");

        /*--------------------------------
         * der physische Baum wird nur
         * angelegt, wenn er noch nicht existierte
         * --------------------------------*/
        if (physicaldocstruct == null) {
            DocStructType dst = this.myPrefs.getDocStrctTypeByName("BoundBook");
            physicaldocstruct = this.mydocument.createDocStruct(dst);
            this.mydocument.setPhysicalDocStruct(physicaldocstruct);
        }

        // check for valid filepath
        try {
            List<? extends Metadata> filepath = physicaldocstruct.getAllMetadataByType(metadataTypeForPath);
            if (filepath == null || filepath.isEmpty()) {
                Metadata mdForPath = new Metadata(metadataTypeForPath);
                if (SystemUtils.IS_OS_WINDOWS) {
                    mdForPath.setValue(WINDOWS_FILE_PROTOCOL + mediaFolder);
                } else {
                    mdForPath.setValue(DEFAULT_FILE_PROTOCOL + mediaFolder);
                }
                physicaldocstruct.addMetadata(mdForPath);
            }
        } catch (Exception e) {
            log.error(e);
        }

        Path folderToCheck = null;

        if (directory == null) {
            folderToCheck = Paths.get(mediaFolderWithFallback);
            checkIfImagesValid(inProzess.getTitel(), mediaFolderWithFallback);
        } else {
            folderToCheck = Paths.get(directory);
            if (!folderToCheck.isAbsolute()) {
                folderToCheck = Paths.get(inProzess.getImagesDirectory(), directory);
            }
            checkIfImagesValid(inProzess.getTitel(), folderToCheck.toString());
        }

        docStructPage = this.myPrefs.getDocStrctTypeByName("page");
        docStructAudio = this.myPrefs.getDocStrctTypeByName("audio");
        docStructVideo = this.myPrefs.getDocStrctTypeByName("video");
        docStructObject = this.myPrefs.getDocStrctTypeByName("object");

        // use fallback to 'page', if additional types are not configured in ruleset
        if (docStructAudio == null) {
            docStructAudio = docStructPage;
        }
        if (docStructVideo == null) {
            docStructVideo = docStructPage;
        }
        if (docStructObject == null) {
            docStructObject = docStructPage;
        }

        /*-------------------------------
         * retrieve existing pages/images
         * -------------------------------*/
        List<DocStruct> oldPages = physicaldocstruct.getAllChildren();
        if (oldPages == null) {
            oldPages = new ArrayList<>();
        }

        /*--------------------------------
         * add new page/images if necessary
         * --------------------------------*/

        if (oldPages.size() == this.myLastImage) {
            return;
        }

        String defaultPagination = ConfigurationHelper.getInstance().getMetsEditorDefaultPagination();
        Map<String, DocStruct> assignedImages = new HashMap<>();
        List<DocStruct> pageElementsWithoutImages = new ArrayList<>();
        List<String> imagesWithoutPageElements = new ArrayList<>();

        if (physicaldocstruct.getAllChildren() != null && !physicaldocstruct.getAllChildren().isEmpty()) {
            List<String> imageFileList = null;
            imageFileList = StorageProvider.getInstance().list(folderToCheck.toString());
            Set<String> imageFileSet = new HashSet<>(imageFileList);
            for (DocStruct page : physicaldocstruct.getAllChildren()) {
                if (page.getImageName() != null) {
                    if (imageFileSet.contains(page.getImageName())) {
                        assignedImages.put(page.getImageName(), page);
                    } else {
                        try {
                            page.removeContentFile(page.getAllContentFiles().get(0));
                            pageElementsWithoutImages.add(page);
                        } catch (ContentFileNotLinkedException e) {
                            log.error(e);
                        }
                    }
                } else {
                    pageElementsWithoutImages.add(page);

                }
            }

        }
        try {

            List<String> imageNamesInMediaFolder = getDataFiles(inProzess, directory);
            if (imageNamesInMediaFolder != null && !imageNamesInMediaFolder.isEmpty()) {
                for (String imageName : imageNamesInMediaFolder) {
                    if (!assignedImages.containsKey(imageName)) {
                        imagesWithoutPageElements.add(imageName);
                    }
                }
            }
        } catch (InvalidImagesException e1) {
            log.error(e1);
        }

        // handle possible cases

        // case 1: existing pages but no images (some images are removed)
        if (!pageElementsWithoutImages.isEmpty() && imagesWithoutPageElements.isEmpty()) {
            for (DocStruct pageToRemove : pageElementsWithoutImages) {
                physicaldocstruct.removeChild(pageToRemove);
                List<Reference> refs = new ArrayList<>(pageToRemove.getAllFromReferences());
                for (ugh.dl.Reference ref : refs) {
                    DocStruct source = ref.getSource();
                    for (Reference reference : source.getAllToReferences()) {
                        if (reference.getTarget().equals(pageToRemove)) {
                            source.getAllToReferences().remove(reference);
                            break;
                        }
                    }
                }
            }
        }

        // case 2: no page docs but images (some images are added)
        else if (pageElementsWithoutImages.isEmpty() && !imagesWithoutPageElements.isEmpty()) {
            int currentPhysicalOrder = assignedImages.size();
            for (String newImage : imagesWithoutPageElements) {

                String mimetype = NIOFileUtils.getMimeTypeFromFile(Paths.get(newImage));
                DocStruct dsPage = this.createDocStruct(mimetype);

                try {
                    // physical page no
                    physicaldocstruct.addChild(dsPage);
                    MetadataType mdt = this.myPrefs.getMetadataTypeByName(METADATA_PHYSICAL_PAGE_NUMBER);
                    Metadata mdTemp = new Metadata(mdt);
                    mdTemp.setValue(String.valueOf(++currentPhysicalOrder));
                    dsPage.addMetadata(mdTemp);

                    // logical page no
                    mdt = this.myPrefs.getMetadataTypeByName(METADATA_LOGICAL_PAGE_NUMBER);
                    mdTemp = new Metadata(mdt);

                    if ("arabic".equalsIgnoreCase(defaultPagination)) {
                        mdTemp.setValue(String.valueOf(currentPhysicalOrder));
                    } else if ("roman".equalsIgnoreCase(defaultPagination)) {
                        RomanNumeral roman = new RomanNumeral();
                        roman.setValue(currentPhysicalOrder);
                        mdTemp.setValue(roman.getNumber());
                    } else {
                        mdTemp.setValue("uncounted");
                    }

                    dsPage.addMetadata(mdTemp);
                    logical.addReferenceTo(dsPage, "logical_physical");

                    // image name
                    ContentFile cf = new ContentFile();
                    cf.setMimetype(mimetype);
                    if (SystemUtils.IS_OS_WINDOWS) {
                        cf.setLocation(WINDOWS_FILE_PROTOCOL + mediaFolder + newImage);
                    } else {
                        cf.setLocation(DEFAULT_FILE_PROTOCOL + mediaFolder + newImage);
                    }
                    dsPage.addContentFile(cf);

                } catch (TypeNotAllowedAsChildException | MetadataTypeNotAllowedException e) {
                    log.error(e);
                }
            }
        }

        // case 3: empty page docs and unassinged images
        else {
            for (DocStruct page : pageElementsWithoutImages) {
                if (!imagesWithoutPageElements.isEmpty()) {
                    // assign new image name to page
                    String newImageName = imagesWithoutPageElements.get(0);
                    imagesWithoutPageElements.remove(0);
                    ContentFile cf = new ContentFile();
                    if (SystemUtils.IS_OS_WINDOWS) {
                        cf.setLocation(WINDOWS_FILE_PROTOCOL + mediaFolder + newImageName);
                    } else {
                        cf.setLocation(DEFAULT_FILE_PROTOCOL + mediaFolder + newImageName);
                    }
                    page.addContentFile(cf);
                } else {
                    // remove page
                    physicaldocstruct.removeChild(page);
                    List<Reference> refs = new ArrayList<>(page.getAllFromReferences());
                    for (ugh.dl.Reference ref : refs) {
                        ref.getSource().removeReferenceTo(page);
                    }
                }
            }
            if (!imagesWithoutPageElements.isEmpty()) {
                // create new page elements

                int currentPhysicalOrder = physicaldocstruct.getAllChildren().size();
                for (String newImage : imagesWithoutPageElements) {

                    String mimetype = NIOFileUtils.getMimeTypeFromFile(Paths.get(newImage));
                    DocStruct dsPage = this.createDocStruct(mimetype);

                    try {
                        // physical page no
                        physicaldocstruct.addChild(dsPage);
                        MetadataType mdt = this.myPrefs.getMetadataTypeByName(METADATA_PHYSICAL_PAGE_NUMBER);
                        Metadata mdTemp = new Metadata(mdt);
                        mdTemp.setValue(String.valueOf(++currentPhysicalOrder));
                        dsPage.addMetadata(mdTemp);

                        // logical page no
                        mdt = this.myPrefs.getMetadataTypeByName(METADATA_LOGICAL_PAGE_NUMBER);
                        mdTemp = new Metadata(mdt);

                        if ("arabic".equalsIgnoreCase(defaultPagination)) {
                            mdTemp.setValue(String.valueOf(currentPhysicalOrder));
                        } else if ("roman".equalsIgnoreCase(defaultPagination)) {
                            RomanNumeral roman = new RomanNumeral();
                            roman.setValue(currentPhysicalOrder);
                            mdTemp.setValue(roman.getNumber());
                        } else {
                            mdTemp.setValue("uncounted");
                        }

                        dsPage.addMetadata(mdTemp);
                        logical.addReferenceTo(dsPage, "logical_physical");

                        // image name
                        ContentFile cf = new ContentFile();
                        cf.setMimetype(mimetype);
                        if (SystemUtils.IS_OS_WINDOWS) {
                            cf.setLocation(WINDOWS_FILE_PROTOCOL + mediaFolder + newImage);
                        } else {
                            cf.setLocation(DEFAULT_FILE_PROTOCOL + mediaFolder + newImage);
                        }
                        dsPage.addContentFile(cf);

                    } catch (TypeNotAllowedAsChildException | MetadataTypeNotAllowedException e) {
                        log.error(e);
                    }
                }

            }
        }
        int currentPhysicalOrder = 1;
        MetadataType mdt = this.myPrefs.getMetadataTypeByName(METADATA_PHYSICAL_PAGE_NUMBER);
        if (physicaldocstruct.getAllChildren() != null) {
            for (DocStruct page : physicaldocstruct.getAllChildren()) {
                List<? extends Metadata> pageNoMetadata = page.getAllMetadataByType(mdt);
                if (pageNoMetadata == null || pageNoMetadata.isEmpty()) {
                    break;
                }
                for (Metadata pageNo : pageNoMetadata) {
                    pageNo.setValue(String.valueOf(currentPhysicalOrder));
                }
                currentPhysicalOrder++;
            }
        }
    }

    private DocStruct createDocStruct(String mimetype) throws TypeNotAllowedForParentException {
        // TODO check mimetypes of all 3d object files
        if (mimetype.startsWith("image")) {
            return this.mydocument.createDocStruct(docStructPage);
        } else if (mimetype.startsWith("video") || "application/mxf".equals(mimetype)) {
            return this.mydocument.createDocStruct(docStructVideo);
        } else if (mimetype.startsWith("audio")) {
            return this.mydocument.createDocStruct(docStructAudio);
        } else if (mimetype.startsWith("object")) {
            return this.mydocument.createDocStruct(docStructObject);
        } else if (mimetype.startsWith("model")) {
            return this.mydocument.createDocStruct(docStructObject);
        } else {
            // use old implementation as default
            return this.mydocument.createDocStruct(docStructPage);
        }
    }

    /**
     * scale given image file to png using internal embedded content server
     * 
     * @throws ContentLibException
     * @throws IOException
     * @throws ImageManipulatorException
     */
    public void scaleFile(String inFileName, String outFileName, int inSize, int intRotation)
            throws ContentLibException, IOException, ImageManipulatorException {
        ConfigurationHelper conf = ConfigurationHelper.getInstance();
        Path inPath = Paths.get(inFileName);
        URI s3URI = null;
        try {
            s3URI = new URI("s3://" + conf.getS3Bucket() + "/" + S3FileUtils.path2Key(inPath));
        } catch (URISyntaxException e) {
            log.error(e);
        }
        log.trace("start scaleFile");

        int tmpSize = inSize;
        if (tmpSize < 1) {
            tmpSize = 1;
        }
        log.trace("Scale to " + tmpSize + "%");

        log.trace("api");
        ImageManager im = null;
        JpegInterpreter pi = null;
        try {
            im = conf.useS3() && s3URI != null ? new ImageManager(s3URI) : new ImageManager(inPath.toUri());
            log.trace("im");
            ImageInterpreter ii = im.getMyInterpreter();
            Dimension inputResolution = new Dimension((int) ii.getXResolution(), (int) ii.getYResolution());
            log.trace("input resolution: " + inputResolution.width + "x" + inputResolution.height + "dpi");
            Dimension outputResolution = new Dimension(144, 144);
            log.trace("output resolution: " + outputResolution.width + "x" + outputResolution.height + "dpi");
            Dimension dim = new Dimension(tmpSize * outputResolution.width / inputResolution.width,
                    tmpSize * outputResolution.height / inputResolution.height);
            log.trace("Absolute scale: " + dim.width + "x" + dim.height + "%");
            RenderedImage ri = im.scaleImageByPixel(dim, ImageManager.SCALE_BY_PERCENT, intRotation);
            log.trace("ri");
            pi = new JpegInterpreter(ri);
            log.trace("pi");
            pi.setXResolution(outputResolution.width);
            log.trace("xres = " + pi.getXResolution());
            pi.setYResolution(outputResolution.height);
            log.trace("yres = " + pi.getYResolution());
            FileOutputStream outputFileStream = new FileOutputStream(outFileName);
            log.trace("output");
            pi.writeToStream(null, outputFileStream);
            log.trace("write stream");
            outputFileStream.close();
            log.trace("close stream");
        } finally {
            if (im != null) {
                im.close();
            }
            if (pi != null) {
                pi.close();
            }
        }
    }

    // Add a method to validate the image files

    /**
     * die Images eines Prozesses auf Vollständigkeit prüfen ================================================================
     * 
     * @throws DAOException
     * @throws SwapException
     */
    public boolean checkIfImagesValid(String title, String folder) {
        boolean isValid = true;
        this.myLastImage = 0;

        /*--------------------------------
         * alle Bilder durchlaufen und dafür
         * die Seiten anlegen
         * --------------------------------*/
        Path dir = Paths.get(folder);

        if (StorageProvider.getInstance().isDirectory(dir)) {
            List<String> dateien = StorageProvider.getInstance().list(dir.toString(), NIOFileUtils.DATA_FILTER);
            List<String> dateien2 = StorageProvider.getInstance().list(dir.toString());
            //checks for fileName errors / empty folder
            if (dateien.isEmpty()) {
                String[] parameters = { String.valueOf(dateien2.size()), dir.toString() };

                //true if list is truly empty
                if (dateien.size() == dateien2.size()) {
                    Helper.setFehlerMeldung(Helper.getTranslation("imagesFolderEmpty", parameters));
                } else {
                    Helper.setFehlerMeldung(Helper.getTranslation("fileNameValidationError", parameters));
                }
                return false;
            }

            this.myLastImage = dateien.size();
            if ("\\d{8}".equals(ConfigurationHelper.getInstance().getImagePrefix())) {
                int counter = 1;
                int myDiff = 0;
                String curFile = null;
                try {
                    for (Iterator<String> iterator = dateien.iterator(); iterator.hasNext(); counter++) {
                        curFile = iterator.next();
                        int curFileNumber = Integer.parseInt(curFile.substring(0, curFile.indexOf(".")));
                        if (curFileNumber != counter + myDiff) {
                            String[] parameter = { title, String.valueOf(counter + myDiff), curFile };
                            String value = Helper.getTranslation("wrongImageNumber", parameter);
                            Helper.setFehlerMeldung(value);
                            myDiff = curFileNumber - counter;
                            isValid = false;
                        }
                    }
                } catch (NumberFormatException e1) {
                    isValid = false;
                    String[] parameter = { title, curFile };
                    String value = Helper.getTranslation("wrongFileName", parameter);
                    Helper.setFehlerMeldung(value);
                }
                return isValid;
            }
            return true;
        }
        Helper.setFehlerMeldung(Helper.getTranslation("noImageFolderFound", title));
        return false;
    }

    public static class GoobiImageFileComparator implements Comparator<String> {

        @Override
        public int compare(String s1, String s2) {
            String imageSorting = ConfigurationHelper.getInstance().getImageSorting();
            s1 = s1.substring(0, s1.lastIndexOf("."));
            s2 = s2.substring(0, s2.lastIndexOf("."));

            if ("number".equalsIgnoreCase(imageSorting)) {
                try {
                    Integer i1 = Integer.valueOf(s1);
                    Integer i2 = Integer.valueOf(s2);
                    return i1.compareTo(i2);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot compare \"" + s1 + "\" and \"" + s2
                            + "\". The comparison is configured as a number comparison, but at least one of them is not a number!");
                }
            } else if ("alphanumeric".equalsIgnoreCase(imageSorting)) {
                return s1.compareToIgnoreCase(s2);
            } else {
                return s1.compareToIgnoreCase(s2);
            }
        }

    }

    /**
     * 
     * @param myProzess current process
     * @return sorted list with strings representing images of proces
     * @throws InvalidImagesException
     */

    public List<String> getImageFiles(Process myProzess) throws InvalidImagesException {
        Path dir;
        try {
            dir = Paths.get(myProzess.getImagesTifDirectory(true));
        } catch (Exception e) {
            throw new InvalidImagesException(e);
        }
        /* Verzeichnis einlesen */

        List<String> dateien = StorageProvider.getInstance().list(dir.toString(), NIOFileUtils.imageNameFilter);

        /* alle Dateien durchlaufen */
        if (dateien != null && !dateien.isEmpty()) {
            Collections.sort(dateien, new GoobiImageFileComparator());
        }
        return dateien;

    }

    public List<String> getDataFiles(Process myProzess, String directory) throws InvalidImagesException {
        Path dir;
        try {
            if (directory == null) {
                dir = Paths.get(myProzess.getImagesTifDirectory(true));
            } else {
                // check if directy exists, otherwise use relative path
                dir = Paths.get(directory);
                if (!dir.isAbsolute()) {
                    dir = Paths.get(myProzess.getImagesDirectory() + directory);
                }
            }
        } catch (Exception e) {
            throw new InvalidImagesException(e);
        }
        /* Verzeichnis einlesen */

        List<String> dateien = StorageProvider.getInstance().list(dir.toString(), NIOFileUtils.DATA_FILTER);

        /* alle Dateien durchlaufen */
        if (dateien != null && !dateien.isEmpty()) {
            Collections.sort(dateien, new GoobiImageFileComparator());
        }
        return dateien;

    }

    /**
     * 
     * @param myProzess current process
     * @param directory current folder
     * @return sorted list with strings representing images of proces
     * @throws InvalidImagesException
     */

    public List<String> getImageFiles(Process myProzess, String directory, boolean useThumsbDir) throws InvalidImagesException {
        Path dir;
        try {
            dir = Paths.get(myProzess.getImagesDirectory() + directory);
        } catch (Exception e) {
            throw new InvalidImagesException(e);
        }

        if (useThumsbDir && !StorageProvider.getInstance().isDirectory(dir) && StringUtils.isNotBlank(directory)) {
            String thumbsFolder;
            try {
                thumbsFolder = myProzess.getLargestThumbsDirectory(directory);
                if (StringUtils.isNotBlank(thumbsFolder)) {
                    dir = Paths.get(thumbsFolder);
                }
            } catch (IOException | SwapException e) {
                log.error("Error reading thumbs folder for " + dir, e);
            }
        }

        /* Verzeichnis einlesen */
        List<String> dateien = StorageProvider.getInstance().list(dir.toString(), NIOFileUtils.imageOrObjectNameFilter);

        List<String> orderedFilenameList = new ArrayList<>();
        if (dateien != null && !dateien.isEmpty()) {
            List<DocStruct> pagesList = mydocument.getPhysicalDocStruct().getAllChildren();
            if (pagesList != null) {
                int pagessize = pagesList.size();
                int datasize = dateien.size();
                for (int i = 0; i < pagessize; i++) {
                    DocStruct page = pagesList.get(i);
                    // try to find media object based on complete name before trying to get it from filename prefix
                    String filename = page.getImageName();
                    boolean imageFound = false;
                    for (int j = 0; j < datasize; j++) {
                        String currentImage = dateien.get(j);
                        if (currentImage.equals(filename)) {
                            orderedFilenameList.add(currentImage);
                            imageFound = true;
                            break;
                        }
                    }
                    // if the file could not be found, try to find it based on the prefix. Maybe we are in a derivate folder with different file types
                    if (!imageFound) {
                        String filenamePrefix = filename.replace(Metadaten.getFileExtension(filename), "");

                        for (int j = 0; j < datasize; j++) {
                            String currentImage = dateien.get(j);
                            String currentImagePrefix = currentImage.replace(Metadaten.getFileExtension(currentImage), "");
                            if (currentImagePrefix.equals(filenamePrefix)) {
                                orderedFilenameList.add(currentImage);
                                break;
                            }
                        }
                    }
                }
            }

            if (orderedFilenameList.size() == dateien.size()) {
                return orderedFilenameList;
            } else {
                Collections.sort(dateien, new GoobiImageFileComparator());
                return dateien;
            }
        } else {
            return null;
        }
    }

    public List<String> getImageFiles(DocStruct physical) {
        List<String> orderedFileList = new ArrayList<>();
        List<DocStruct> pages = physical.getAllChildren();
        if (pages != null && !pages.isEmpty()) {
            for (DocStruct page : pages) {
                String filename = page.getImageName();
                if (filename != null) {
                    orderedFileList.add(filename);
                } else {
                    log.error("cannot find image");
                }
            }
        }
        return orderedFileList;
    }
}
