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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Process;
import org.goobi.production.cli.helper.OrderedKeyMap;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.TypeNotAllowedForParentException;

@Log4j2
public class FileManipulation {

    private static final String UNCOUNTED = "uncounted";
    private static final String NO_FILE_SELECTED = "noFileSelected";
    private static final String FILE_UPLOAD = "fileupload";

    private Metadaten metadataBean;

    public FileManipulation(Metadaten metadataBean) {
        this.metadataBean = metadataBean;
    }

    // insert new file after this page
    @Getter
    @Setter
    private String insertPage = "";

    @Getter
    @Setter
    private String imageSelection = "";

    // mode of insert (uncounted or into pagination sequence)
    @Getter
    @Setter
    private String insertMode = UNCOUNTED;

    @Getter
    @Setter
    private Part uploadedFile = null;

    @Getter
    @Setter
    private String uploadedFileName = null;

    @Getter
    @Setter
    private List<String> selectedFiles = new ArrayList<>();

    @Getter
    @Setter
    private boolean deleteFilesAfterMove = false;

    @Getter
    @Setter
    private boolean moveFilesInAllFolder = true;

    @Setter
    private List<String> allImportFolder = new ArrayList<>();

    @Getter
    @Setter
    private String currentFolder = "";

    /**
     * File upload with binary copying.
     */
    public void uploadFile() {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (this.uploadedFile == null) {
                Helper.setFehlerMeldung(NO_FILE_SELECTED);
                return;
            }

            String basename = getFileName(this.uploadedFile);
            if (basename.startsWith(".")) {
                basename = basename.substring(1);
            }
            if (basename.contains("/")) {
                basename = basename.substring(basename.lastIndexOf("/") + 1);
            }
            if (basename.contains("\\")) {
                basename = basename.substring(basename.lastIndexOf("\\") + 1);
            }

            if (StringUtils.isNotBlank(uploadedFileName)) {

                String fileExtension = Metadaten.getFileExtension(basename);
                if (!fileExtension.isEmpty() && !uploadedFileName.endsWith(fileExtension)) {
                    uploadedFileName = uploadedFileName + fileExtension;
                }
                basename = uploadedFileName;
            }
            basename = basename.replace("[^\\p{ASCII}]", "_");
            basename = basename.replace("[\\:\\*\\?\\|\\/]", "_").replace(" ", "_");
            log.trace("folder to import: " + currentFolder);
            String filename = metadataBean.getMyProzess().getImagesDirectory() + currentFolder + FileSystems.getDefault().getSeparator() + basename;
            filename = NIOFileUtils.sanitizePath(filename, metadataBean.getMyProzess().getImagesDirectory());
            log.trace("filename to import: " + filename);

            if (StorageProvider.getInstance().isFileExists(Paths.get(filename))) {
                Helper.setFehlerMeldung(Helper.getTranslation("fileExists", basename));
                return;
            }

            inputStream = this.uploadedFile.getInputStream();
            StorageProvider.getInstance().uploadFile(inputStream, Paths.get(filename));
            log.trace(filename + " was imported");
            // if file was uploaded into media folder, update pagination sequence
            if ((metadataBean.getMyProzess().getImagesDirectory() + currentFolder + FileSystems.getDefault().getSeparator())
                    .equals(metadataBean.getMyProzess()
                            .getImagesTifDirectory(false))) {
                log.trace("update pagination for " + metadataBean.getMyProzess().getTitel());
                updatePagination(filename);

            }

            Helper.setMeldung(Helper.getTranslation("metsEditorFileUploadSuccessful"));
        } catch (IOException | SwapException | DAOException | TypeNotAllowedForParentException | MetadataTypeNotAllowedException e) {
            log.error(e.getMessage(), e);
            Helper.setFehlerMeldung("uploadFailed", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        metadataBean.retrieveAllImages();

        metadataBean.changeFolder();
        // save current state
        metadataBean.Reload();
    }

    private String getFileName(final Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "";
    }

    private void updatePagination(String filename)
            throws TypeNotAllowedForParentException, IOException, SwapException, DAOException, MetadataTypeNotAllowedException {
        if (!matchesFileConfiguration(filename)) {
            return;
        }

        if ("lastPage".equals(insertPage)) {
            metadataBean.createPagination();
        } else {

            Prefs prefs = metadataBean.getMyProzess().getRegelsatz().getPreferences();
            DigitalDocument doc = metadataBean.getDocument();
            DocStruct physical = doc.getPhysicalDocStruct();

            List<DocStruct> pageList = physical.getAllChildren();

            int indexToImport = Math.max(Integer.parseInt(insertPage) - 1, 0);

            DocStructType newPageType = prefs.getDocStrctTypeByName("page");
            DocStruct newPage = doc.createDocStruct(newPageType);
            MetadataType physicalPageNoType = prefs.getMetadataTypeByName("physPageNumber");
            MetadataType logicalPageNoType = prefs.getMetadataTypeByName("logicalPageNumber");
            for (int index = 0; index < pageList.size(); index++) {

                if (index == indexToImport) {
                    DocStruct oldPage = pageList.get(index);

                    // physical page no for new page

                    Metadata mdTemp = new Metadata(physicalPageNoType);
                    mdTemp.setValue(String.valueOf(indexToImport + 1));
                    newPage.addMetadata(mdTemp);

                    // new physical page no for old page
                    oldPage.getAllMetadataByType(physicalPageNoType).get(0).setValue(String.valueOf(indexToImport + 2));

                    mdTemp = new Metadata(logicalPageNoType);

                    if (UNCOUNTED.equalsIgnoreCase(insertMode)) {
                        mdTemp.setValue(UNCOUNTED);
                    } else {
                        // set new logical no. for new and old page
                        Metadata oldPageNo = oldPage.getAllMetadataByType(logicalPageNoType).get(0);
                        mdTemp.setValue(oldPageNo.getValue());
                        if (index + 1 < pageList.size()) {
                            Metadata pageNoOfFollowingElement = pageList.get(index + 1).getAllMetadataByType(logicalPageNoType).get(0);
                            oldPageNo.setValue(pageNoOfFollowingElement.getValue());
                        } else {
                            oldPageNo.setValue(UNCOUNTED);
                        }
                    }

                    newPage.addMetadata(mdTemp);
                    doc.getLogicalDocStruct().addReferenceTo(newPage, "logical_physical");

                    ContentFile cf = new ContentFile();
                    cf.setLocation(filename);
                    newPage.addContentFile(cf);
                    doc.getFileSet().addFile(cf);

                }
                if (index > indexToImport) {
                    DocStruct currentPage = pageList.get(index);
                    // check if element is last element
                    currentPage.getAllMetadataByType(physicalPageNoType).get(0).setValue(String.valueOf(index + 2));
                    if (!UNCOUNTED.equalsIgnoreCase(insertMode)) {
                        if (index + 1 == pageList.size()) {
                            currentPage.getAllMetadataByType(logicalPageNoType).get(0).setValue(UNCOUNTED);
                        } else {
                            DocStruct followingPage = pageList.get(index + 1);
                            currentPage.getAllMetadataByType(logicalPageNoType)
                                    .get(0)
                                    .setValue(followingPage.getAllMetadataByType(logicalPageNoType).get(0).getValue());
                        }
                    }
                }
            }
            pageList.add(indexToImport, newPage);

        }
    }

    /**
     * download file
     */

    public void downloadFile() {
        Path downloadFile = null;

        int imageOrder = Integer.parseInt(imageSelection) - 1;
        DocStruct page = metadataBean.getDocument().getPhysicalDocStruct().getAllChildren().get(imageOrder);
        String imagename = page.getImageName();
        String filenamePrefix = imagename.substring(0, imagename.lastIndexOf("."));
        try {
            List<Path> filesInFolder = StorageProvider.getInstance().listFiles(metadataBean.getMyProzess().getImagesDirectory() + currentFolder);
            for (Path currentFile : filesInFolder) {
                String currentFileName = currentFile.getFileName().toString();
                String currentFileNamePrefix = currentFileName.substring(0, currentFileName.lastIndexOf("."));
                if (filenamePrefix.equals(currentFileNamePrefix)) {
                    downloadFile = currentFile;
                    break;
                }
            }
        } catch (SwapException | IOException e1) {
            log.error(e1);
        }

        if (downloadFile == null || !StorageProvider.getInstance().isFileExists(downloadFile)) {
            String[] parameter = { filenamePrefix, currentFolder };
            Helper.setFehlerMeldung(Helper.getTranslation("MetsEditorMissingFile", parameter));
            return;
        }

        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

            String fileName = downloadFile.getFileName().toString();
            ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
            String contentType = servletContext.getMimeType(fileName);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
            InputStream in = null;
            ServletOutputStream out = null;
            try {
                in = StorageProvider.getInstance().newInputStream(downloadFile);
                out = response.getOutputStream();
                byte[] buffer = new byte[4096];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                }
                out.flush();
            } catch (IOException e) {
                log.error("IOException while exporting run note", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.error(e);
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        log.error(e);
                    }
                }
            }

            facesContext.responseComplete();
        }

    }

    /**
     * move files on server folder
     */

    public void exportFiles() {
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            Helper.setFehlerMeldung(NO_FILE_SELECTED);
            return;
        }
        List<DocStruct> allPages = metadataBean.getDocument().getPhysicalDocStruct().getAllChildren();
        List<String> filenamesToMove = new ArrayList<>();

        for (String fileIndex : selectedFiles) {
            try {
                int index = Integer.parseInt(fileIndex);
                filenamesToMove.add(allPages.get(index - 1).getImageName());
            } catch (NumberFormatException exception) {
                log.error(exception);
            }
        }
        String tempDirectory = ConfigurationHelper.getInstance().getTemporaryFolder();
        Path fileuploadFolder = Paths.get(tempDirectory + FILE_UPLOAD);
        if (!StorageProvider.getInstance().isFileExists(fileuploadFolder)) {
            try {
                StorageProvider.getInstance().createDirectories(fileuploadFolder);
            } catch (IOException exception) {
                log.error(exception);
            }
        }
        Path destination = Paths.get(fileuploadFolder.toString() + FileSystems.getDefault().getSeparator() + metadataBean.getMyProzess().getTitel());
        if (!StorageProvider.getInstance().isFileExists(destination)) {
            try {
                StorageProvider.getInstance().createDirectories(destination);
            } catch (IOException exception) {
                log.error(exception);
            }
        }

        for (String filename : filenamesToMove) {
            String prefix = filename.replace(Metadaten.getFileExtension(filename), "");
            String processTitle = metadataBean.getMyProzess().getTitel();
            for (String folder : metadataBean.getAllTifFolders()) {
                try {
                    List<Path> filesInFolder = StorageProvider.getInstance().listFiles(metadataBean.getMyProzess().getImagesDirectory() + folder);
                    for (Path currentFile : filesInFolder) {

                        String filenameInFolder = currentFile.getFileName().toString();
                        String filenamePrefix = filenameInFolder.replace(Metadaten.getFileExtension(filenameInFolder), "");
                        if (filenamePrefix.equals(prefix)) {
                            Path tempFolder = Paths.get(destination.toString() + FileSystems.getDefault().getSeparator() + folder);
                            if (!StorageProvider.getInstance().isFileExists(tempFolder)) {
                                StorageProvider.getInstance().createDirectories(tempFolder);
                            }

                            Path destinationFile = Paths.get(tempFolder.toString(), processTitle + "_" + currentFile.getFileName().toString());
                            StorageProvider.getInstance().copyFile(currentFile, destinationFile);
                            break;

                        }

                    }

                } catch (SwapException | IOException e) {
                    log.error(e);
                }
            }
        }
        if (deleteFilesAfterMove) {
            OrderedKeyMap<String, PhysicalObject> pageMap = metadataBean.getPageMap();
            for (Map.Entry<String, PhysicalObject> entry : pageMap.entrySet()) {
                PhysicalObject physivalObject = entry.getValue();
                physivalObject.setSelected(selectedFiles.contains(entry.getKey()));
            }

            metadataBean.deleteSeltectedPages();
            selectedFiles = new ArrayList<>();
            deleteFilesAfterMove = false;
        }

        metadataBean.retrieveAllImages();
    }

    /**
     * import files from folder
     * 
     */

    public List<String> getAllImportFolder() {

        String tempDirectory = ConfigurationHelper.getInstance().getTemporaryFolder();
        Path fileuploadFolder = Paths.get(tempDirectory + FILE_UPLOAD);

        allImportFolder = new ArrayList<>();

        if (StorageProvider.getInstance().isDirectory(fileuploadFolder)) {
            allImportFolder.addAll(StorageProvider.getInstance().list(fileuploadFolder.toString(), NIOFileUtils.folderFilter));
        }
        return allImportFolder;
    }

    public void importFiles() {

        if (selectedFiles == null || selectedFiles.isEmpty()) {
            Helper.setFehlerMeldung(NO_FILE_SELECTED);
            return;
        }
        String tempDirectory = ConfigurationHelper.getInstance().getTemporaryFolder();

        boolean useMasterFolder = false;
        if (ConfigurationHelper.getInstance().isUseMasterDirectory()) {
            useMasterFolder = true;
        }
        Process currentProcess = metadataBean.getMyProzess();
        List<String> importedFilenames = new ArrayList<>();
        for (String importName : selectedFiles) {
            List<Path> subfolderList =
                    StorageProvider.getInstance().listFiles(tempDirectory + FILE_UPLOAD + FileSystems.getDefault().getSeparator() + importName);
            for (Path subfolder : subfolderList) {

                if (useMasterFolder) {
                    // check if current import folder is master folder
                    if (subfolder.getFileName()
                            .toString()
                            .equals(VariableReplacer.simpleReplace(ConfigurationHelper.getInstance().getProcessImagesSourceDirectoryName(),
                                    metadataBean.getMyProzess()))) {
                        try {
                            String masterFolderName = currentProcess.getImagesOrigDirectory(false);
                            Path masterDirectory = Paths.get(masterFolderName);
                            if (!StorageProvider.getInstance().isFileExists(masterDirectory)) {
                                StorageProvider.getInstance().createDirectories(masterDirectory);
                            }
                            List<Path> objectInFolder = StorageProvider.getInstance().listFiles(subfolder.toString());
                            for (Path object : objectInFolder) {
                                Path dest = Paths.get(masterDirectory.toString(), object.getFileName().toString());
                                StorageProvider.getInstance().copyFile(object, dest);
                            }
                        } catch (SwapException | DAOException | IOException e) {
                            log.error(e);
                        }
                    } else if (subfolder.getFileName().toString().contains("_")) {
                        String folderSuffix =
                                subfolder.getFileName().toString().substring(subfolder.getFileName().toString().lastIndexOf("_") + 1);
                        String folderName = currentProcess.getMethodFromName(folderSuffix);
                        if (folderName != null) {
                            try {
                                Path directory = Paths.get(folderName);
                                List<Path> objectInFolder = StorageProvider.getInstance().listFiles(subfolder.toString());
                                for (Path object : objectInFolder) {
                                    if ((folderName + FileSystems.getDefault().getSeparator())
                                            .equals(currentProcess.getImagesTifDirectory(false))) {
                                        importedFilenames.add(object.getFileName().toString());
                                    }
                                    Path dest = Paths.get(directory.toString(), object.getFileName().toString());
                                    StorageProvider.getInstance().copyFile(object, dest);
                                }

                            } catch (IOException | SwapException e) {
                                log.error(e);
                            }

                        }
                    }

                } else if (subfolder.getFileName().toString().contains("_")) {
                    String folderSuffix = subfolder.getFileName().toString().substring(subfolder.getFileName().toString().lastIndexOf("_") + 1);
                    String folderName = currentProcess.getMethodFromName(folderSuffix);
                    if (folderName != null) {
                        Path directory = Paths.get(folderName);
                        List<Path> objectInFolder = StorageProvider.getInstance().listFiles(subfolder.toString());
                        for (Path object : objectInFolder) {
                            try {
                                if ((folderName + FileSystems.getDefault().getSeparator()).equals(currentProcess.getImagesTifDirectory(false))) {
                                    importedFilenames.add(object.getFileName().toString());
                                }
                                Path dest = Paths.get(directory.toString(), object.getFileName().toString());
                                StorageProvider.getInstance().copyFile(object, dest);
                            } catch (IOException | SwapException e) {
                                log.error(e);
                            }
                        }
                    }
                }
            }
        }
        // update pagination
        try {
            if (insertPage == null || insertPage.isEmpty() || "lastPage".equals(insertPage)) {
                metadataBean.createPagination();
            } else {
                int indexToImport = Integer.parseInt(insertPage);
                for (String filename : importedFilenames) {
                    updatePagination(filename);
                    insertPage = String.valueOf(++indexToImport);
                }
            }
        } catch (TypeNotAllowedForParentException | SwapException | DAOException | MetadataTypeNotAllowedException | IOException e) {
            log.error(e);
        }

        // delete folder

        for (String importName : selectedFiles) {
            Path importfolder = Paths.get(tempDirectory + FILE_UPLOAD + FileSystems.getDefault().getSeparator() + importName);
            StorageProvider.getInstance().deleteDir(importfolder);
        }
        metadataBean.retrieveAllImages();

        metadataBean.changeFolder();
        // save current state
        metadataBean.Reload();
    }

    private static boolean matchesFileConfiguration(String filename) {

        if (filename == null) {
            return false;
        }

        String afterLastSlash = filename.substring(filename.lastIndexOf('/') + 1);
        String afterLastBackslash = afterLastSlash.substring(afterLastSlash.lastIndexOf('\\') + 1);

        String prefix = ConfigurationHelper.getInstance().getImagePrefix();

        return afterLastBackslash.matches(prefix + "\\..+");
    }

}