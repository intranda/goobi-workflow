package de.sub.goobi.export.download;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.goobi.beans.Process;
import org.goobi.beans.ProjectFileGroup;
import org.goobi.beans.User;
import org.goobi.managedbeans.LoginBean;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.tag.TagException;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.metadaten.MetadatenImagesHelper;
import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.ExportFileformat;
import ugh.dl.Fileformat;
import ugh.dl.Md;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.dl.VirtualFileGroup;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

public class ExportMets {
    protected Helper help = new Helper();
    protected Prefs myPrefs;
    protected List<String> problems = new ArrayList<>();

    protected static final Logger logger = LogManager.getLogger(ExportMets.class);

    private static final Pattern descriptionPattern = Pattern.compile("\\D");

    private static final String metsNamespace = "http://www.loc.gov/METS/";
    private static final String premisNamespace = "http://www.loc.gov/standards/premis/";

    /**
     * DMS-Export in das Benutzer-Homeverzeichnis
     * 
     * @param myProzess
     * @throws InterruptedException
     * @throws IOException
     * @throws DAOException
     * @throws SwapException
     * @throws ReadException
     * @throws UghHelperException
     * @throws ExportFileException
     * @throws MetadataTypeNotAllowedException
     * @throws WriteException
     * @throws PreferencesException
     * @throws DocStructHasNoTypeException
     * @throws TypeNotAllowedForParentException
     */
    public boolean startExport(Process myProzess) throws IOException, InterruptedException, DocStructHasNoTypeException, PreferencesException,
    WriteException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException, SwapException, DAOException,
    TypeNotAllowedForParentException {

        String benutzerHome = "";
        LoginBean login = Helper.getLoginBean();
        if (login != null) {
            benutzerHome = login.getMyBenutzer().getHomeDir();
        } else {
            benutzerHome = myProzess.getProjekt().getDmsImportImagesPath();
        }
        return startExport(myProzess, benutzerHome);
    }

    /**
     * DMS-Export an eine gewünschte Stelle
     * 
     * @param myProzess
     * @param zielVerzeichnis
     * @throws InterruptedException
     * @throws IOException
     * @throws PreferencesException
     * @throws WriteException
     * @throws UghHelperException
     * @throws ExportFileException
     * @throws MetadataTypeNotAllowedException
     * @throws DocStructHasNoTypeException
     * @throws DAOException
     * @throws SwapException
     * @throws ReadException
     * @throws TypeNotAllowedForParentException
     */
    public boolean startExport(Process myProzess, String inZielVerzeichnis) throws IOException, InterruptedException, PreferencesException,
    WriteException, DocStructHasNoTypeException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException,
    SwapException, DAOException, TypeNotAllowedForParentException {

        /*
         * -------------------------------- Read Document --------------------------------
         */
        this.myPrefs = myProzess.getRegelsatz().getPreferences();
        String atsPpnBand = myProzess.getTitel();
        Fileformat gdzfile = myProzess.readMetadataFile();

        String zielVerzeichnis = prepareUserDirectory(inZielVerzeichnis);

        String targetFileName = zielVerzeichnis + atsPpnBand + "_mets.xml";
        return writeMetsFile(myProzess, targetFileName, gdzfile, false);

    }
    
    public boolean startDownload(Process myProzess) throws PreferencesException, WriteException, TypeNotAllowedForParentException, IOException, InterruptedException, SwapException, DAOException, ReadException {
    	this.myPrefs = myProzess.getRegelsatz().getPreferences();
        Fileformat gdzfile = myProzess.readMetadataFile();
        String atsPpnBand = myProzess.getTitel();
        String targetFileName = atsPpnBand + "_mets.xml";
        return writeMetsFile(myProzess, targetFileName, gdzfile, false);
    }

    /**
     * prepare user directory
     * 
     * @param inTargetFolder the folder to proove and maybe create it
     */
    protected String prepareUserDirectory(String inTargetFolder) {
        String target = inTargetFolder;
        User myBenutzer = Helper.getCurrentUser();
        if (myBenutzer != null) {
            try {
                FilesystemHelper.createDirectoryForUser(target, myBenutzer.getLogin());
            } catch (Exception e) {
                Helper.setFehlerMeldung("Export canceled, could not create destination directory: " + inTargetFolder, e);
            }
        }
        return target;
    }

    /**
     * write MetsFile to given Path
     * 
     * @param myProzess the Process to use
     * @param targetFileName the filename where the metsfile should be written
     * @param gdzfile the FileFormat-Object to use for Mets-Writing
     * @throws DAOException
     * @throws SwapException
     * @throws InterruptedException
     * @throws IOException
     * @throws TypeNotAllowedForParentException
     */

    protected boolean writeMetsFile(Process myProzess, String targetFileName, Fileformat gdzfile, boolean writeLocalFilegroup)
            throws PreferencesException, WriteException, IOException, InterruptedException, SwapException, DAOException,
            TypeNotAllowedForParentException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        ExportFileformat mm = MetadatenHelper.getExportFileformatByName(myProzess.getProjekt().getFileFormatDmsExport(), myProzess.getRegelsatz());
        mm.setWriteLocal(writeLocalFilegroup);
        mm.setCreateUUIDs(config.isExportCreateUUIDsAsFileIDs());

        String imageFolderPath = myProzess.getImagesTifDirectory(true);
        Path imageFolder = Paths.get(imageFolderPath);
        /*
         * before creating mets file, change relative path to absolute -
         */
        DigitalDocument dd = gdzfile.getDigitalDocument();

        MetadatenImagesHelper mih = new MetadatenImagesHelper(this.myPrefs, dd);

        if (dd.getFileSet() == null || dd.getFileSet().getAllFiles().isEmpty()) {
            Helper.setMeldung(myProzess.getTitel() + ": digital document does not contain images; temporarily adding them for mets file creation");
            mih.createPagination(myProzess, null);
        } else {
            mih.checkImageNames(myProzess, imageFolder.getFileName().toString());
        }

        /*
         * get the topstruct element of the digital document depending on anchor property
         */
        DocStruct topElement = dd.getLogicalDocStruct();
        if (topElement.getType().isAnchor()) {
            if (topElement.getAllChildren() == null || topElement.getAllChildren().size() == 0) {
                throw new PreferencesException(
                        myProzess.getTitel() + ": the topstruct element is marked as anchor, but does not have any children for physical docstrucs");
            } else {
                topElement = topElement.getAllChildren().get(0);
            }
        }

        /*
         * -------------------------------- if the top element does not have any image related, set them all --------------------------------
         */

        if (config.isExportValidateImages()) {

            if (topElement.getAllToReferences("logical_physical") == null || topElement.getAllToReferences("logical_physical").size() == 0) {
                if (dd.getPhysicalDocStruct() != null && dd.getPhysicalDocStruct().getAllChildren() != null) {
                    Helper.setMeldung(myProzess.getTitel()
                            + ": topstruct element does not have any referenced images yet; temporarily adding them for mets file creation");
                    for (DocStruct mySeitenDocStruct : dd.getPhysicalDocStruct().getAllChildren()) {
                        topElement.addReferenceTo(mySeitenDocStruct, "logical_physical");
                    }
                } else {
                    Helper.setFehlerMeldung(myProzess.getTitel() + ": could not find any referenced images, export aborted");
                    dd = null;
                    return false;
                }
            }

            for (ContentFile cf : dd.getFileSet().getAllFiles()) {
                String location = cf.getLocation();
                // If the file's location string shoes no sign of any protocol,
                // use the file protocol.
                if (!location.contains("://")) {
                    if (!location.matches("^[A-Z]:.*") && !location.matches("^\\/.*")) {
                        //is a relative path
                        Path f = Paths.get(imageFolder.toString(), location);
                        location = f.toString();
                    }
                    location = "file://" + location;
                }
                cf.setLocation(location);
            }
        }
        mm.setDigitalDocument(dd);

        // if configured, extract metadata from files and store them as techMd premis
        if (config.isExportCreateTechnicalMetadata()) {
            int counter = 1;
            for (DocStruct page : dd.getPhysicalDocStruct().getAllChildren()) {
                Path path = Paths.get(page.getImageName());
                if (!path.isAbsolute()) {
                    path = Paths.get(imageFolder.toString(), page.getImageName());
                }
                Element techMd = createTechMd(path);
                if (techMd != null) {
                    Md md = new Md(techMd);
                    md.setType("techMD");
                    md.setId(String.format("AMD_%04d", counter++));
                    dd.addTechMd(md);
                    page.setAdmId(md.getId());
                }
            }
        }

        Map<String, String> additionalMetadataMap = config.getExportWriteAdditionalMetadata();
        if (!additionalMetadataMap.isEmpty()) {
            String projectMetadataName = additionalMetadataMap.get("Project");
            String institutionMetadataName = additionalMetadataMap.get("Institution");
            Prefs prefs = myProzess.getRegelsatz().getPreferences();
            if (StringUtils.isNotBlank(projectMetadataName)) {
                MetadataType mdt = prefs.getMetadataTypeByName(projectMetadataName);
                if (mdt != null) {
                    try {
                        ugh.dl.Metadata md = new ugh.dl.Metadata(mdt);
                        md.setValue(myProzess.getProjekt().getTitel());
                        topElement.addMetadata(md);
                    } catch (MetadataTypeNotAllowedException e) {
                        logger.warn("Configured metadata for project name is unknown or not allowed.");
                    }
                    if (topElement.getParent() != null) {
                        try {
                            ugh.dl.Metadata md = new ugh.dl.Metadata(mdt);
                            md.setValue(myProzess.getProjekt().getTitel());
                            topElement.getParent().addMetadata(md);
                        } catch (MetadataTypeNotAllowedException e) {
                            logger.warn("Configured metadata for project name is unknown or not allowed.");
                        }
                    }
                }
            }
            if (StringUtils.isNotBlank(institutionMetadataName)) {
                MetadataType mdt = prefs.getMetadataTypeByName(institutionMetadataName);
                if (mdt != null) {
                    try {
                        ugh.dl.Metadata md = new ugh.dl.Metadata(mdt);
                        md.setValue(myProzess.getProjekt().getInstitution().getLongName());
                        topElement.addMetadata(md);
                    } catch (MetadataTypeNotAllowedException e) {
                        logger.warn("Configured metadata for institution name is unknown or not allowed.");
                    }
                    if (topElement.getParent() != null) {
                        try {
                            ugh.dl.Metadata md = new ugh.dl.Metadata(mdt);
                            md.setValue(myProzess.getProjekt().getInstitution().getLongName());
                            topElement.getParent().addMetadata(md);
                        } catch (MetadataTypeNotAllowedException e) {
                            logger.warn("Configured metadata for institution name is unknown or not allowed.");
                        }
                    }
                }
            }
        }

        /*
         * -------------------------------- wenn Filegroups definiert wurden, werden diese jetzt in die Metsstruktur übernommen
         * --------------------------------
         */
        // Replace all pathes with the given VariableReplacer, also the file
        // group pathes!
        VariableReplacer vp = new VariableReplacer(mm.getDigitalDocument(), this.myPrefs, myProzess, null);
        List<ProjectFileGroup> myFilegroups = myProzess.getProjekt().getFilegroups();

        if (myFilegroups != null && myFilegroups.size() > 0) {
            for (ProjectFileGroup pfg : myFilegroups) {

                // check if source files exists
                if (pfg.getFolder() != null && pfg.getFolder().length() > 0) {
                    String foldername = myProzess.getMethodFromName(pfg.getFolder());
                    if (foldername != null) {
                        Path folder = Paths.get(myProzess.getMethodFromName(pfg.getFolder()));
                        if (folder != null && StorageProvider.getInstance().isFileExists(folder)
                                && !StorageProvider.getInstance().list(folder.toString()).isEmpty()) {
                            VirtualFileGroup v = createFilegroup(vp, pfg);
                            mm.getDigitalDocument().getFileSet().addVirtualFileGroup(v);
                        }
                    }
                } else {
                    VirtualFileGroup v = createFilegroup(vp, pfg);
                    mm.getDigitalDocument().getFileSet().addVirtualFileGroup(v);
                }
            }
        }

        // Replace rights and digiprov entries.
        mm.setRightsOwner(vp.replace(myProzess.getProjekt().getMetsRightsOwner()));
        mm.setRightsOwnerLogo(vp.replace(myProzess.getProjekt().getMetsRightsOwnerLogo()));
        mm.setRightsOwnerSiteURL(vp.replace(myProzess.getProjekt().getMetsRightsOwnerSite()));
        mm.setRightsOwnerContact(vp.replace(myProzess.getProjekt().getMetsRightsOwnerMail()));
        mm.setDigiprovPresentation(vp.replace(myProzess.getProjekt().getMetsDigiprovPresentation()));
        mm.setDigiprovReference(vp.replace(myProzess.getProjekt().getMetsDigiprovReference()));
        mm.setDigiprovPresentationAnchor(vp.replace(myProzess.getProjekt().getMetsDigiprovPresentationAnchor()));
        mm.setDigiprovReferenceAnchor(vp.replace(myProzess.getProjekt().getMetsDigiprovReferenceAnchor()));

        mm.setMetsRightsLicense(vp.replace(myProzess.getProjekt().getMetsRightsLicense()));
        mm.setMetsRightsSponsor(vp.replace(myProzess.getProjekt().getMetsRightsSponsor()));
        mm.setMetsRightsSponsorLogo(vp.replace(myProzess.getProjekt().getMetsRightsSponsorLogo()));
        mm.setMetsRightsSponsorSiteURL(vp.replace(myProzess.getProjekt().getMetsRightsSponsorSiteURL()));

        mm.setPurlUrl(vp.replace(myProzess.getProjekt().getMetsPurl()));
        mm.setContentIDs(vp.replace(myProzess.getProjekt().getMetsContentIDs()));

        String pointer = myProzess.getProjekt().getMetsPointerPath();
        pointer = vp.replace(pointer);
        mm.setMptrUrl(pointer);

        String anchor = myProzess.getProjekt().getMetsPointerPathAnchor();
        pointer = vp.replace(anchor);
        mm.setMptrAnchorUrl(pointer);

        mm.setGoobiID(String.valueOf(myProzess.getId()));

        // if (!ConfigMain.getParameter("ImagePrefix", "\\d{8}").equals("\\d{8}")) {
        List<String> images = new ArrayList<>();
        if (config.isExportValidateImages()) {
            try {
                images = new MetadatenImagesHelper(this.myPrefs, dd).getDataFiles(myProzess, imageFolderPath);

                int sizeOfPagination = dd.getPhysicalDocStruct().getAllChildren().size();
                if (images != null) {
                    int sizeOfImages = images.size();
                    if (sizeOfPagination == sizeOfImages) {
                        dd.overrideContentFiles(images);
                    } else {
                        String[] param = { String.valueOf(sizeOfPagination), String.valueOf(sizeOfImages) };
                        Helper.setFehlerMeldung(Helper.getTranslation("imagePaginationError", param));
                        return false;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                logger.error(e);
                return false;
            } catch (InvalidImagesException e) {
                logger.error(e);
                return false;
            }
        } else {
            // create pagination out of virtual file names
            dd.addAllContentFiles();
        }
        if (config.isExportInTemporaryFile()) {
            Path tempFile = StorageProvider.getInstance().createTemporaryFile(myProzess.getTitel(), ".xml");
            String filename = tempFile.toString();
            mm.write(filename);
            StorageProvider.getInstance().copyFile(tempFile, Paths.get(targetFileName));

            Path anchorFile = Paths.get(filename.replace(".xml", "_anchor.xml"));
            if (StorageProvider.getInstance().isFileExists(anchorFile)) {
                StorageProvider.getInstance().copyFile(anchorFile, Paths.get(targetFileName.replace(".xml", "_anchor.xml")));
                StorageProvider.getInstance().deleteDir(anchorFile);
            }
            StorageProvider.getInstance().deleteDir(tempFile);
        } else {
            mm.write(targetFileName);
        }
		try (InputStream in = StorageProvider.getInstance().newInputStream(Paths.get(targetFileName))) {
			FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
			ExternalContext ec = facesContext.getExternalContext();
			ec.responseReset();
			ec.setResponseHeader("Content-Disposition", "attachment; filename=" + targetFileName);
			ec.setResponseContentLength((int) StorageProvider.getInstance().getFileSize(Paths.get(targetFileName)));
			
			IOUtils.copy(in, ec.getResponseOutputStream());  
			
			facesContext.responseComplete();
		}catch (IOException e) {
			logger.error(e);
		}
        Helper.setMeldung(null, myProzess.getTitel() + ": ", "ExportFinished");
        return true;
    }

    public List<String> getProblems() {
        return problems;
    }

    private VirtualFileGroup createFilegroup(VariableReplacer variableRplacer, ProjectFileGroup projectFileGroup) {
        VirtualFileGroup v = new VirtualFileGroup();
        v.setName(projectFileGroup.getName());
        v.setPathToFiles(variableRplacer.replace(projectFileGroup.getPath()));
        v.setMimetype(projectFileGroup.getMimetype());
        v.setFileSuffix(projectFileGroup.getSuffix());
        v.setFileExtensionsToIgnore(projectFileGroup.getIgnoreMimetypes());
        v.setIgnoreConfiguredMimetypeAndSuffix(projectFileGroup.isUseOriginalFiles());
        if (projectFileGroup.getName().equals("PRESENTATION")) {
            v.setMainGroup(true);
        }
        return v;
    }

    /**
     * Extract metadata from file and create techMD element for it. The content is stored as premis xml
     * 
     */

    private Element createTechMd(Path file) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Element techMd = doc.createElementNS(metsNamespace, "techMD");
            Element mdWrap = doc.createElementNS(metsNamespace, "mdWrap");
            techMd.appendChild(mdWrap);
            mdWrap.setAttribute("MDTYPE", "OTHER");
            mdWrap.setAttribute("MIMETYPE", "text/xml");
            Element xmlData = doc.createElementNS(metsNamespace, "xmlData");
            mdWrap.appendChild(xmlData);

            Element object = doc.createElementNS(premisNamespace, "object");
            object.setAttribute("version", "3.0");
            object.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "type", "premis:file");
            xmlData.appendChild(object);

            String mimeType = NIOFileUtils.getMimeTypeFromFile(file);

            if (mimeType.startsWith("image")) {
                buildImageMetadata(doc, file, object);
            } else if (mimeType.equals("video/mpeg")) {
                buildMPEGMetadata(doc, file, object);
            } else if (mimeType.equals("application/pdf")) {
                buildPDFMetadata(doc, file, object);
            } else if (mimeType.equals("audio/x-wav")) {
                buildAudioMetadata(doc, file, object, false);
            } else if (mimeType.startsWith("audio") || mimeType.equals("video/x-mpeg")) {
                buildAudioMetadata(doc, file, object, true);
            } else if (mimeType.startsWith("video") || mimeType.equals("application/mxf")) {
                buildMPEGMetadata(doc, file, object);
            } else {
                String message = "Data is of type not covered by the premis creation: " + mimeType;
                logger.warn(message);
                problems.add(message);
            }

            return techMd;
        } catch (ParserConfigurationException | IOException | UnsupportedAudioFileException | DataFormatException e) {
            logger.error(e);
        }

        return null;
    }

    /**
     * Generates premis metadata for (mpeg) file and adds them to object in the metsfile
     *
     */

    private void buildMPEGMetadata(Document doc, Path file, Element object) throws DataFormatException, IOException {

        addObjectIdentifier(doc, object, "local", file.getFileName().normalize().toString());
        String duration = null;
        String bitrate = null;
        String width = null;
        String height = null;
        // obtain the duration and bitrate of the file by running exif tool on the shell and parsing its output
        String[] command = { ConfigurationHelper.getInstance().getPathToExiftool(), file.toString() };
        java.lang.Process exiftool = Runtime.getRuntime().exec(command);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exiftool.getInputStream()))) {
            String s;
            while ((s = reader.readLine()) != null) {
                if (duration == null) {
                    duration = getDuration(s);
                }
                if (bitrate == null) {
                    bitrate = getBitrate(s);
                }
                if (width == null) {
                    width = getVideoWidth(s);
                }
                if (height == null) {
                    height = getVideoHeight(s);
                }
            }
        }

        if (duration != null) {
            addSignificantProperty(doc, object, "Duration", duration);
        }
        if (bitrate != null) {
            addSignificantProperty(doc, object, "Bitrate", bitrate);
        }
        if (width != null) {
            addSignificantProperty(doc, object, "ImageWidth", width);
        }
        if (height != null) {
            addSignificantProperty(doc, object, "ImageHeight", height);
        }

        Element objectCharacteristics = doc.createElementNS(premisNamespace, "objectCharacteristics");
        object.appendChild(objectCharacteristics);
        {
            Element fixity = doc.createElementNS(premisNamespace, "fixity");
            objectCharacteristics.appendChild(fixity);
            {
                addHash(doc, file, fixity, "SHA-1");
            }
            addSize(doc, file, objectCharacteristics);

        }

    }

    private void addSize(Document doc, Path file, Element objectCharacteristics) {
        Element size = doc.createElementNS(premisNamespace, "size");
        objectCharacteristics.appendChild(size);

        try {
            size.setTextContent(String.valueOf(Files.size(file)));
        } catch (DOMException | IOException e) {
            logger.error(e);
        }

    }

    /**
     * Generates premis metadata for image file and adds them to object
     * 
     * @param doc
     * @param file the file Object
     * @param object
     */
    private void buildImageMetadata(Document doc, Path file, Element object) throws FileNotFoundException, IOException {

        // obtain Dimensions of passed image
        Optional<Dimension> maybeImageDimension = getSize(file);
        Dimension imageDimension = null;
        if (maybeImageDimension.isPresent()) {
            imageDimension = maybeImageDimension.get();
        } else {
            // abort if Dimensions of image cannot be read
            String message = "Unable to read imagesize for image " + file.getFileName();
            logger.error(message);
            throw new IOException(message);
        }
        // create remaining structure for the premis block and add information
        addObjectIdentifier(doc, object, "local", file.getFileName().normalize().toString());
        addSignificantProperty(doc, object, "ImageHeight", Integer.toString((int) imageDimension.getHeight()));
        addSignificantProperty(doc, object, "ImageWidth", Integer.toString((int) imageDimension.getWidth()));

        Element objectCharacteristics = doc.createElementNS(premisNamespace, "objectCharacteristics");
        object.appendChild(objectCharacteristics);

        Element fixity = doc.createElementNS(premisNamespace, "fixity");
        objectCharacteristics.appendChild(fixity);

        addHash(doc, file, fixity, "SHA-256");

        addSize(doc, file, objectCharacteristics);

    }

    /**
     * Retrieves the image size (width/height) for the image referenced in the given page document The image sizes are retrieved from image metadata.
     * if this doesn't work, no image sizes are set
     *
     */
    static Optional<Dimension> getSize(Path filepath) {

        File imageFile = filepath.toFile();
        if (!imageFile.isFile()) {
            return Optional.empty();
        }
        Dimension imageSize = new Dimension(0, 0);
        try {
            Metadata imageMetadata = ImageMetadataReader.readMetadata(imageFile);
            Directory jpegDirectory = imageMetadata.getFirstDirectoryOfType(JpegDirectory.class);
            Directory exifDirectory = imageMetadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            Directory pngDirectory = imageMetadata.getFirstDirectoryOfType(PngDirectory.class);

            imageSize.width = getDirectoryDescription(pngDirectory, 1);
            imageSize.height = getDirectoryDescription(pngDirectory, 2);

            if (imageSize.height == 0 || imageSize.width == 0) {
                imageSize.width = getDirectoryDescription(exifDirectory, 256);
                imageSize.height = getDirectoryDescription(exifDirectory, 257);
            }
            if (imageSize.height == 0 || imageSize.width == 0) {
                imageSize.width = getDirectoryDescription(jpegDirectory, 3);
                imageSize.height = getDirectoryDescription(jpegDirectory, 1);
            }

            if (imageSize.getHeight() * imageSize.getHeight() > 0) {
                return Optional.of(imageSize);
            }
        } catch (ImageProcessingException |

                IOException e) {
            try {
                imageSize = getSizeForJp2(imageFile.toPath());
                return Optional.ofNullable(imageSize);
            } catch (IOException e2) {
                try {
                    BufferedImage image = ImageIO.read(imageFile);
                    if (image != null) {
                        return Optional.of(new Dimension(image.getWidth(), image.getHeight()));
                    }
                } catch (NullPointerException | IOException e1) {
                    logger.error("Unable to read image size for " + filepath.getFileName().toString(), e);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Reads Description intType from provided Directory, returns 0 in case anything goes wrong
     *
     * @param directory
     * @param tagType
     * @return
     */
    private static int getDirectoryDescription(Directory directory, int tagType) {
        if (directory != null) {
            String description = directory.getDescription(tagType);
            if (description != null) {
                return Integer.parseInt(descriptionPattern.matcher(description).replaceAll(""));
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    /**
     * Assumes provided Path leads to an Image in JP2000 format and gets Information on height and width of it
     *
     * @param image the file Object
     * @return
     * @throws IOException
     */
    private static Dimension getSizeForJp2(Path image) throws IOException {
        if (!image.getFileName().toString().endsWith(".jp2")) {
            throw new IOException("No valid image reader found for 'jpeg2000'");
        }
        //            if (image.getFileName().toString().matches("(?i).*\\.jp(2|x|2000)")) {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("jpeg2000");

        while (readers.hasNext()) {
            ImageReader reader = readers.next();
            if (reader != null) {
                Dimension dim = readDimension(image, reader);
                if (dim != null) {
                    return dim;
                }
            }
        }
        ImageReader reader = getOpenJpegReader();
        if (reader != null) {
            Dimension dim = readDimension(image, reader);
            if (dim != null) {
                return dim;
            }
        } else {
            logger.debug("Not openjpeg image reader found");
        }

        throw new IOException("No valid image reader found for 'jpeg2000'");

    }

    /**
     * Reads information on provided image with provided reader and returns it as a Dimension OBject with width and height
     *
     * @param image
     * @param reader
     * @return
     */
    private static Dimension readDimension(Path image, ImageReader reader) {
        try (InputStream inStream = Files.newInputStream(image); ImageInputStream iis = ImageIO.createImageInputStream(inStream)) {
            reader.setInput(iis);
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);
            if (width * height > 0) {
                return new Dimension(width, height);
            }
            logger.error("Error reading image dimensions of " + image + " with image reader " + reader.getClass().getSimpleName());
        } catch (IOException e) {
            logger.error("Error reading " + image + " with image reader " + reader.getClass().getSimpleName());
        }
        return null;
    }

    /**
     * Provides an OpenJp2ImageReader object or returns null if that is not possible
     *
     * @return
     */
    private static ImageReader getOpenJpegReader() {
        ImageReader reader;
        try {
            Object readerSpi = Class.forName("de.digitalcollections.openjpeg.imageio.OpenJp2ImageReaderSpi").newInstance();
            reader = ((ImageReaderSpi) readerSpi).createReaderInstance();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (NoClassDefFoundError | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            logger.warn("No openjpeg reader");
            return null;
        }
        return reader;
    }

    /**
     * Checks if passed String contains "Duration" if so, it parses it and reformats the time
     *
     * @param s
     * @return
     */
    private static String getDuration(String s) {
        String duration = null;
        if (s.startsWith("Duration")) {
            String[] splitString = s.split(" ");
            for (String str : splitString) {
                if (str.matches("\\d:\\d\\d:\\d\\d")) {
                    PeriodFormatter formatter = new PeriodFormatterBuilder().appendHours()
                            .appendSeparator(":")
                            .appendMinutes()
                            .appendSeparator(":")
                            .appendSeconds()
                            .toFormatter();
                    Period dur = formatter.parsePeriod(str);
                    PeriodFormatter newFormatter = new PeriodFormatterBuilder().appendHours()
                            .appendSuffix("h ")
                            .appendMinutes()
                            .appendSuffix("mn ")
                            .appendSeconds()
                            .appendSuffix("s")
                            .toFormatter();
                    duration = newFormatter.print(dur);
                }
            }
        }
        return duration;
    }

    /**
     * Parse passed String for "Bitrate" and return it
     *
     * @param s
     * @return
     */
    private static String getBitrate(String s) {
        String bitrate = null;
        if (s.contains("Bitrate") || s.contains("Video Frame Rate")) {
            String[] splitString = s.split(":");
            bitrate = splitString[1].trim();
        }
        return bitrate;
    }

    /**
     * Parse passed String for "Image Height" and return it
     * 
     * @param s
     * @return
     */
    private static String getVideoHeight(String s) {
        String bitrate = null;
        if (s.contains("Image Height")) {
            String[] splitString = s.split(":");
            bitrate = splitString[1].trim();
        }
        return bitrate;
    }

    /**
     * Parse passed String for "Image Width" and return it
     *
     * @param s
     * @return
     */
    private static String getVideoWidth(String s) {
        String bitrate = null;
        if (s.contains("Image Width")) {
            String[] splitString = s.split(":");
            bitrate = splitString[1].trim();
        }
        return bitrate;
    }

    /**
     * Generates sub elements on object containing identifierName and identifierValue
     *
     * @param document the premis document
     * @param object parent Element to content generated in this Method
     * @param identifierName to be written in subElement called objectIdentifierType
     * @param identifierValue to be written in subElement called objectIdentifierValue
     */
    private void addObjectIdentifier(Document document, Element object, String identifierName, String identifierValue) {
        Element objectIdentifier = document.createElementNS(premisNamespace, "objectIdentifier");

        object.appendChild(objectIdentifier);

        Element objectIdentifierType = document.createElementNS(premisNamespace, "objectIdentifierType");
        objectIdentifier.appendChild(objectIdentifierType);

        objectIdentifierType.setTextContent(identifierName);

        Element objectIdentifierValue = document.createElementNS(premisNamespace, "objectIdentifierValue");
        objectIdentifier.appendChild(objectIdentifierValue);

        objectIdentifierValue.setTextContent(identifierValue);
    }

    /**
     * Generates sub elements to object for significantPropertiesType and significantPropertiesValue which contain propertyType and propertyValue
     *
     * @param document the premis document
     * @param object parent Element to content generated in this Method
     * @param propertyType
     * @param propertyValue
     */
    private void addSignificantProperty(Document document, Element object, String propertyType, String propertyValue) {
        Element significantProperties2 = document.createElementNS(premisNamespace, "significantProperties");
        object.appendChild(significantProperties2);
        Element significantPropertiesType = document.createElementNS(premisNamespace, "significantPropertiesType");
        significantProperties2.appendChild(significantPropertiesType);

        significantPropertiesType.setTextContent(propertyType);
        Element significantPropertiesValue = document.createElementNS(premisNamespace, "significantPropertiesValue");
        significantProperties2.appendChild(significantPropertiesValue);
        significantPropertiesValue.setTextContent(propertyValue);

    }

    /**
     * Generate hash of file with passed algorithm, add to passed Element
     *
     * @param document the premis document
     * @param file PremisFile object
     * @param fixity Jdom Element associated with file
     * @param algorithmName hashing algorithm to use
     * @throws IOException
     */
    private void addHash(Document document, Path file, Element fixity, String algorithmName) throws IOException {
        Element messageDigestAlgorithm = document.createElementNS(premisNamespace, "messageDigestAlgorithm");
        fixity.appendChild(messageDigestAlgorithm);
        messageDigestAlgorithm.setTextContent(algorithmName);

        Element messageDigest = document.createElementNS(premisNamespace, "messageDigest");
        fixity.appendChild(messageDigest);

        String hash;
        try (InputStream is = Files.newInputStream(file)) {
            hash = calculateHash(is, algorithmName);
        }
        messageDigest.setTextContent(hash);
    }

    /**
     * Reads passed Inputstream and returns the passed algorightName hash of it, returns null if algorithm is not supported
     *
     * @param is
     * @param algorithmName
     * @return
     * @throws IOException
     */
    private static String calculateHash(InputStream is, String algorithmName) throws IOException {
        MessageDigest shamd = null;
        try {
            shamd = MessageDigest.getInstance(algorithmName);

            try (DigestInputStream dis = new DigestInputStream(is, shamd)) {
                int n = 0;
                byte[] buffer = new byte[8092];
                while (n != -1) {
                    n = dis.read(buffer);
                }
                is.close();
            }
            String shaString;
            shaString = getShaString(shamd, algorithmName);

            return shaString;
        } catch (NoSuchAlgorithmException e1) {
            logger.error(algorithmName + " not supported for hash");
            return null;
        }
    }

    /**
     * Extracts and padds (if needed) the hash from passed message digest assuming algorithmName was used to compute it
     *
     * @param messageDigest
     * @param algorithmName only supports SHA-1 and SHA-256
     * @return
     * @throws NoSuchAlgorithmException only supports SHA-1 and SHA-256, throws this if neither is passed
     */
    private static String getShaString(MessageDigest messageDigest, String algorithmName) throws NoSuchAlgorithmException {
        BigInteger bigInt = new BigInteger(1, messageDigest.digest());
        //        String sha256 = bigInt.toString(16).toLowerCase();
        StringBuilder sha256 = new StringBuilder(bigInt.toString(16).toLowerCase());
        if (algorithmName.equals("SHA-1")) {
            while (sha256.length() < 40) {
                sha256.insert(0, "0");
                //                sha256 = "0" + sha256;
            }
        } else if (algorithmName.equals("SHA-256")) {
            while (sha256.length() < 64) {
                sha256.insert(0, "0");

                //                sha256 = "0" + sha256;
            }
        } else {
            throw new NoSuchAlgorithmException("Only SHA-1 and SHA-256 supported");
        }
        return sha256.toString();
    }

    /**
     * Generates premis Metadata for a pdf file and adds them to object in the metsfile
     *
     * @param document the premis document
     * @param file the file Object
     * @param object
     * @throws IOException
     * @throws CommandExecutionException
     */
    private void buildPDFMetadata(Document document, Path file, Element object) throws IOException {

        addObjectIdentifier(document, object, "local", file.getFileName().normalize().toString());
        PDDocument doc = PDDocument.load(file.toFile());
        addSignificantProperty(document, object, "PageNumber", String.valueOf(doc.getNumberOfPages()));
        Element objectCharacteristics = document.createElementNS(premisNamespace, "objectCharacteristics");
        object.appendChild(objectCharacteristics);

        Element fixity = document.createElementNS(premisNamespace, "fixity");
        objectCharacteristics.appendChild(fixity);

        addHash(document, file, fixity, "SHA-1");

        addSize(document, file, objectCharacteristics);

    }

    /**
     * Builds premis metadata for a audio file and attaches them to object in the metsfile
     *
     * @param document the premis document
     * @param file the file Object
     * @param object xml Element, information created in this method will be added to this
     * @param isMp3 whether file is an mp3 file or a different audio format
     * @throws IOException
     * @throws CommandExecutionException
     * @throws DataFormatException
     * @throws CannotReadException
     * @throws TagException
     * @throws ReadOnlyFileException
     * @throws InvalidAudioFrameException
     * @throws UnsupportedAudioFileException
     */
    private void buildAudioMetadata(Document doc, Path file, Element object, boolean isMp3)
            throws IOException, DataFormatException, UnsupportedAudioFileException {

        addObjectIdentifier(doc, object, "local", file.getFileName().normalize().toString());
        //  buildMPEGMetadata(doc, file, object);

        double duration = 0;
        String bitrate = null;
        AudioFile audioFile = null;
        // get bitrate and duration of audio file, differs for mp3, because for it size and duration are not related
        if (isMp3) {
            try {
                audioFile = AudioFileIO.read(file.toFile());
            } catch (CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
                logger.error(e);
            }
            AudioHeader audioHeader = audioFile.getAudioHeader();
            MP3AudioHeader mp3Header = (MP3AudioHeader) audioHeader;
            duration = mp3Header.getPreciseTrackLength();
            bitrate = mp3Header.getBitRate();
        } else {
            try (InputStream audioSrc = Files.newInputStream(file); InputStream bufferedIn = new BufferedInputStream(audioSrc);
                    AudioInputStream ais = AudioSystem.getAudioInputStream(bufferedIn)) {
                long audioFileLength = Files.size(file);
                AudioFormat format = ais.getFormat();
                int frameSize = format.getFrameSize();
                float frameRate = format.getFrameRate();
                duration = (audioFileLength / (frameSize * frameRate));
                bitrate = String.valueOf(frameSize * frameRate * 8);
            }
        }

        DecimalFormatSymbols separator = new DecimalFormatSymbols(Locale.UK);
        DecimalFormat numberFormat = new DecimalFormat("#.######", separator);
        // check if duration and bitrate were successfully read, else throw exception
        if (duration != 0) {
            addSignificantProperty(doc, object, "Duration", numberFormat.format(duration));
        } else {
            throw new DataFormatException("Unable to determine duration of medium " + file);
        }
        if (bitrate != null) {
            addSignificantProperty(doc, object, "Bitrate", bitrate);
        } else {
            throw new DataFormatException("Unable to determine bitrate of medium " + file);
        }
        Element objectCharacteristics = doc.createElementNS(premisNamespace, "objectCharacteristics");
        object.appendChild(objectCharacteristics);

        Element fixity = doc.createElementNS(premisNamespace, "fixity");
        objectCharacteristics.appendChild(fixity);

        addHash(doc, file, fixity, "SHA-1");

        addSize(doc, file, objectCharacteristics);

    }

}
