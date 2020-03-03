package de.sub.goobi.metadaten;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger; import org.apache.logging.log4j.LogManager;
import org.goobi.beans.Process;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.TypeNotAllowedForParentException;

public class FileManipulation {
    private static final Logger logger = LogManager.getLogger(FileManipulation.class);
    private Metadaten metadataBean;

    public FileManipulation(Metadaten metadataBean) {
        this.metadataBean = metadataBean;
    }

    // insert new file after this page
    private String insertPage = "";

    private String imageSelection = "";

    // mode of insert (uncounted or into pagination sequence)
    private String insertMode = "uncounted";

    private Part uploadedFile = null;

    private String uploadedFileName = null;

    private List<String> selectedFiles = new ArrayList<>();

    private boolean deleteFilesAfterMove = false;

    private boolean moveFilesInAllFolder = true;

    private List<String> allImportFolder = new ArrayList<>();

    private String currentFolder = "";

    /**
     * File upload with binary copying.
     */
    public void uploadFile() {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (this.uploadedFile == null) {
                Helper.setFehlerMeldung("noFileSelected");
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
            logger.trace("folder to import: " + currentFolder);
            String filename = metadataBean.getMyProzess().getImagesDirectory() + currentFolder + FileSystems.getDefault().getSeparator() + basename;

            logger.trace("filename to import: " + filename);

            if (StorageProvider.getInstance().isFileExists(Paths.get(filename))) {
                Helper.setFehlerMeldung(Helper.getTranslation("fileExists", basename));
                return;
            }

            inputStream = this.uploadedFile.getInputStream();
            StorageProvider.getInstance().uploadFile(inputStream, Paths.get(filename));
            logger.trace(filename + " was imported");
            // if file was uploaded into media folder, update pagination sequence
            if (metadataBean.getMyProzess()
                    .getImagesTifDirectory(false)
                    .equals(metadataBean.getMyProzess().getImagesDirectory() + currentFolder + FileSystems.getDefault().getSeparator())) {
                logger.trace("update pagination for " + metadataBean.getMyProzess().getTitel());
                updatePagination(filename);

            }

            Helper.setMeldung(Helper.getTranslation("metsEditorFileUploadSuccessful"));
        } catch (IOException | SwapException | DAOException | InterruptedException | TypeNotAllowedForParentException
                | MetadataTypeNotAllowedException e) {
            logger.error(e.getMessage(), e);
            Helper.setFehlerMeldung("uploadFailed", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
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
        return null;
    }

    public String getUploadedFileName() {
        return uploadedFileName;
    }

    public void setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
    }

    private void updatePagination(String filename)
            throws TypeNotAllowedForParentException, IOException, InterruptedException, SwapException, DAOException, MetadataTypeNotAllowedException {
        if (!matchesFileConfiguration(filename)) {
            return;
        }

        if (insertPage.equals("lastPage")) {
            metadataBean.createPagination();
        } else {

            Prefs prefs = metadataBean.getMyProzess().getRegelsatz().getPreferences();
            DigitalDocument doc = metadataBean.getDocument();
            DocStruct physical = doc.getPhysicalDocStruct();

            List<DocStruct> pageList = physical.getAllChildren();

            int indexToImport = Integer.parseInt(insertPage);
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

                    // logical page no
                    // logicalPageNoType = prefs.getMetadataTypeByName("logicalPageNumber");
                    mdTemp = new Metadata(logicalPageNoType);

                    if (insertMode.equalsIgnoreCase("uncounted")) {
                        mdTemp.setValue("uncounted");
                    } else {
                        // set new logical no. for new and old page
                        Metadata oldPageNo = oldPage.getAllMetadataByType(logicalPageNoType).get(0);
                        mdTemp.setValue(oldPageNo.getValue());
                        if (index + 1 < pageList.size()) {
                            Metadata pageNoOfFollowingElement = pageList.get(index + 1).getAllMetadataByType(logicalPageNoType).get(0);
                            oldPageNo.setValue(pageNoOfFollowingElement.getValue());
                        } else {
                            oldPageNo.setValue("uncounted");
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
                    if (!insertMode.equalsIgnoreCase("uncounted")) {
                        if (index + 1 == pageList.size()) {
                            currentPage.getAllMetadataByType(logicalPageNoType).get(0).setValue("uncounted");
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

    public Part getUploadedFile() {
        return this.uploadedFile;
    }

    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getInsertPage() {
        return insertPage;
    }

    public void setInsertPage(String insertPage) {
        this.insertPage = insertPage;
    }

    public String getInsertMode() {
        return insertMode;
    }

    public void setInsertMode(String insertMode) {
        this.insertMode = insertMode;
    }

    /**
     * download file
     */

    public String getImageSelection() {
        return imageSelection;
    }

    public void setImageSelection(String imageSelection) {
        this.imageSelection = imageSelection;
    }

    public void downloadFile() {
        Path downloadFile = null;

        int imageOrder = Integer.parseInt(imageSelection);
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
        } catch (SwapException | DAOException | IOException | InterruptedException e1) {
            logger.error(e1);
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
                logger.error("IOException while exporting run note", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        logger.error(e);
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        logger.error(e);
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
            Helper.setFehlerMeldung("noFileSelected");
            return;
        }
        List<DocStruct> allPages = metadataBean.getDocument().getPhysicalDocStruct().getAllChildren();
        List<String> filenamesToMove = new ArrayList<>();

        for (String fileIndex : selectedFiles) {
            try {
                int index = Integer.parseInt(fileIndex);
                filenamesToMove.add(allPages.get(index).getImageName());
            } catch (NumberFormatException e) {

            }
        }
        String tempDirectory = ConfigurationHelper.getInstance().getTemporaryFolder();
        Path fileuploadFolder = Paths.get(tempDirectory + "fileupload");
        if (!StorageProvider.getInstance().isFileExists(fileuploadFolder)) {
            try {
                StorageProvider.getInstance().createDirectories(fileuploadFolder);
            } catch (IOException e) {
                logger.error(e);
            }
        }
        Path destination = Paths.get(fileuploadFolder.toString() + FileSystems.getDefault().getSeparator() + metadataBean.getMyProzess().getTitel());
        if (!StorageProvider.getInstance().isFileExists(destination)) {
            try {
                StorageProvider.getInstance().createDirectories(destination);
            } catch (IOException e) {
                logger.error(e);
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

                } catch (SwapException e) {
                    logger.error(e);
                } catch (DAOException e) {
                    logger.error(e);
                } catch (IOException e) {
                    logger.error(e);
                } catch (InterruptedException e) {
                    logger.error(e);
                }
            }
        }
        if (deleteFilesAfterMove) {
            String[] pagesArray = new String[selectedFiles.size()];
            selectedFiles.toArray(pagesArray);
            metadataBean.setAlleSeitenAuswahl(pagesArray);
            metadataBean.deleteSeltectedPages();
            selectedFiles = new ArrayList<>();
            deleteFilesAfterMove = false;
        }

        metadataBean.retrieveAllImages();
    }

    public List<String> getSelectedFiles() {
        return selectedFiles;
    }

    public void setSelectedFiles(List<String> selectedFiles) {
        this.selectedFiles = selectedFiles;
    }

    public boolean isDeleteFilesAfterMove() {
        return deleteFilesAfterMove;
    }

    public void setDeleteFilesAfterMove(boolean deleteFilesAfterMove) {
        this.deleteFilesAfterMove = deleteFilesAfterMove;
    }

    public boolean isMoveFilesInAllFolder() {
        return moveFilesInAllFolder;
    }

    public void setMoveFilesInAllFolder(boolean moveFilesInAllFolder) {
        this.moveFilesInAllFolder = moveFilesInAllFolder;
    }

    /**
     * import files from folder
     * 
     */

    public List<String> getAllImportFolder() {

        String tempDirectory = ConfigurationHelper.getInstance().getTemporaryFolder();
        Path fileuploadFolder = Paths.get(tempDirectory + "fileupload");

        allImportFolder = new ArrayList<>();

        if (StorageProvider.getInstance().isDirectory(fileuploadFolder)) {
            allImportFolder.addAll(StorageProvider.getInstance().list(fileuploadFolder.toString(), NIOFileUtils.folderFilter));
        }
        return allImportFolder;
    }

    public void setAllImportFolder(List<String> allImportFolder) {
        this.allImportFolder = allImportFolder;
    }

    public void importFiles() {

        if (selectedFiles == null || selectedFiles.isEmpty()) {
            Helper.setFehlerMeldung("noFileSelected");
            return;
        }
        String tempDirectory = ConfigurationHelper.getInstance().getTemporaryFolder();

        String masterPrefix = "";
        boolean useMasterFolder = false;
        if (ConfigurationHelper.getInstance().isUseMasterDirectory()) {
            useMasterFolder = true;
            masterPrefix = ConfigurationHelper.getInstance().getMasterDirectoryPrefix();
        }
        Process currentProcess = metadataBean.getMyProzess();
        List<String> importedFilenames = new ArrayList<>();
        for (String importName : selectedFiles) {
            List<Path> subfolderList =
                    StorageProvider.getInstance().listFiles(tempDirectory + "fileupload" + FileSystems.getDefault().getSeparator() + importName);
            for (Path subfolder : subfolderList) {

                if (useMasterFolder) {
                    // check if current import folder is master folder
                    if (subfolder.getFileName().toString().startsWith(masterPrefix)) {
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
                        } catch (SwapException | DAOException | IOException | InterruptedException e) {
                            logger.error(e);
                        }
                    } else {
                        if (subfolder.getFileName().toString().contains("_")) {
                            String folderSuffix =
                                    subfolder.getFileName().toString().substring(subfolder.getFileName().toString().lastIndexOf("_") + 1);
                            String folderName = currentProcess.getMethodFromName(folderSuffix);
                            if (folderName != null) {
                                try {
                                    Path directory = Paths.get(folderName);
                                    List<Path> objectInFolder = StorageProvider.getInstance().listFiles(subfolder.toString());
                                    for (Path object : objectInFolder) {
                                        if (currentProcess.getImagesTifDirectory(false)
                                                .equals(folderName + FileSystems.getDefault().getSeparator())) {
                                            importedFilenames.add(object.getFileName().toString());
                                        }
                                        Path dest = Paths.get(directory.toString(), object.getFileName().toString());
                                        StorageProvider.getInstance().copyFile(object, dest);
                                    }

                                } catch (IOException | SwapException | DAOException | InterruptedException e) {
                                    logger.error(e);
                                }

                            }
                        }
                    }

                } else {
                    if (subfolder.getFileName().toString().contains("_")) {
                        String folderSuffix = subfolder.getFileName().toString().substring(subfolder.getFileName().toString().lastIndexOf("_") + 1);
                        String folderName = currentProcess.getMethodFromName(folderSuffix);
                        if (folderName != null) {
                            Path directory = Paths.get(folderName);
                            List<Path> objectInFolder = StorageProvider.getInstance().listFiles(subfolder.toString());
                            for (Path object : objectInFolder) {
                                try {
                                    if (currentProcess.getImagesTifDirectory(false).equals(folderName + FileSystems.getDefault().getSeparator())) {
                                        importedFilenames.add(object.getFileName().toString());
                                    }
                                    Path dest = Paths.get(directory.toString(), object.getFileName().toString());
                                    StorageProvider.getInstance().copyFile(object, dest);
                                } catch (IOException | SwapException | DAOException | InterruptedException e) {
                                    logger.error(e);
                                }
                            }
                        }
                    }
                }
            }
        }
        // update pagination
        try {
            if (insertPage == null || insertPage.isEmpty() || insertPage.equals("lastPage")) {
                metadataBean.createPagination();
            } else {
                int indexToImport = Integer.parseInt(insertPage);
                for (String filename : importedFilenames) {
                    updatePagination(filename);
                    insertPage = String.valueOf(++indexToImport);
                }
            }
        } catch (TypeNotAllowedForParentException | SwapException | DAOException | MetadataTypeNotAllowedException | IOException
                | InterruptedException e) {
            logger.error(e);
        }

        // delete folder

        for (String importName : selectedFiles) {
            Path importfolder = Paths.get(tempDirectory + "fileupload" + FileSystems.getDefault().getSeparator() + importName);
            StorageProvider.getInstance().deleteDir(importfolder);
        }
        metadataBean.retrieveAllImages();

        metadataBean.changeFolder();
        // save current state
        metadataBean.Reload();
    }

    public String getCurrentFolder() {
        return currentFolder;
    }

    public void setCurrentFolder(String currentFolder) {
        this.currentFolder = currentFolder;
    }

    private static boolean matchesFileConfiguration(String filename) {

        if (filename == null) {
            return false;
        }

        String afterLastSlash = filename.substring(filename.lastIndexOf('/') + 1);
        String afterLastBackslash = afterLastSlash.substring(afterLastSlash.lastIndexOf('\\') + 1);

        String prefix = ConfigurationHelper.getInstance().getImagePrefix();
        if (!afterLastBackslash.matches(prefix + "\\..+")) {
            return false;
        }

        return true;
    }

}