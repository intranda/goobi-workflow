package de.sub.goobi.metadaten;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.TypeNotAllowedForParentException;

import de.schlichtherle.io.FileInputStream;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;

public class FileManipulation {
    private static final Logger logger = Logger.getLogger(FileManipulation.class);
    private Metadaten metadataBean;

    public FileManipulation(Metadaten metadataBean) {
        this.metadataBean = metadataBean;
    }

    // insert new file after this page
    private String insertPage = "";

    private String imageSelection = "";

    // mode of insert (uncounted or into pagination sequence)
    private String insertMode = "";

    private UploadedFile uploadedFile = null;

    /**
     * File upload with binary copying.
     */
    public void uploadFile() {
        ByteArrayInputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (this.uploadedFile == null) {
                Helper.setFehlerMeldung("noFileSelected");
                return;
            }

            String basename = this.uploadedFile.getName();
            if (basename.startsWith(".")) {
                basename = basename.substring(1);
            }
            if (basename.contains("/")) {
                basename = basename.substring(basename.lastIndexOf("/") + 1);
            }
            if (basename.contains("\\")) {
                basename = basename.substring(basename.lastIndexOf("\\") + 1);
            }

            String filename = metadataBean.getMyProzess().getImagesDirectory() + metadataBean.getCurrentTifFolder() + File.separator + basename;

            if (new File(filename).exists()) {
                List<String> parameterList = new ArrayList<String>();
                parameterList.add(filename);
                Helper.setFehlerMeldung(Helper.getTranslation("fileExists", parameterList));
                return;
            }

            inputStream = new ByteArrayInputStream(this.uploadedFile.getBytes());
            outputStream = new FileOutputStream(filename);

            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }

            // if file was uploaded into media folder, update pagination sequence
            if (metadataBean.getMyProzess().getImagesTifDirectory(false).equals(
                    metadataBean.getMyProzess().getImagesDirectory() + metadataBean.getCurrentTifFolder() + File.separator)) {
                if (insertPage.equals(Helper.getTranslation("lastPage"))) {
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
                            mdTemp.setValue(String.valueOf(indexToImport));
                            newPage.addMetadata(mdTemp);

                            // new physical page no for old page
                            oldPage.getAllMetadataByType(physicalPageNoType).get(0).setValue(String.valueOf(indexToImport + 1));

                            // logical page no
                            logicalPageNoType = prefs.getMetadataTypeByName("logicalPageNumber");
                            mdTemp = new Metadata(logicalPageNoType);

                            if (insertMode.equalsIgnoreCase("uncounted")) {
                                mdTemp.setValue("uncounted");
                            } else {
                                // set new logical no. for new and old page 
                                Metadata oldPageNo = oldPage.getAllMetadataByType(logicalPageNoType).get(0);
                                mdTemp.setValue(oldPageNo.getValue());
                                if (index < pageList.size()) {
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

                        }
                        if (index > indexToImport) {
                            DocStruct currentPage = pageList.get(index);
                            // check if element is last element
                            currentPage.getAllMetadataByType(physicalPageNoType).get(0).setValue(String.valueOf(index + 1));
                            if (index + 1 == pageList.size()) {
                                currentPage.getAllMetadataByType(logicalPageNoType).get(0).setValue("uncounted");
                            } else {
                                DocStruct followingPage = pageList.get(index + 1);
                                currentPage.getAllMetadataByType(logicalPageNoType).get(0).setValue(
                                        followingPage.getAllMetadataByType(logicalPageNoType).get(0).getValue());
                            }

                        }
                    }
                    pageList.add(indexToImport, newPage);

                }

            }

            Helper.setMeldung(Helper.getTranslation("metsEditorFileUploadSuccessful"));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Helper.setFehlerMeldung("uploadFailed", e);
        } catch (SwapException e) {
            logger.error(e.getMessage(), e);
            Helper.setFehlerMeldung("uploadFailed", e);
        } catch (DAOException e) {
            logger.error(e.getMessage(), e);
            Helper.setFehlerMeldung("uploadFailed", e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            Helper.setFehlerMeldung("uploadFailed", e);
        } catch (TypeNotAllowedForParentException e) {
            logger.error(e);
            Helper.setFehlerMeldung("uploadFailed", e);
        } catch (MetadataTypeNotAllowedException e) {
            logger.error(e);
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
    }

    public UploadedFile getUploadedFile() {
        return this.uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
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

    public String getImageSelection() {
        return imageSelection;
    }

    public void setImageSelection(String imageSelection) {
        this.imageSelection = imageSelection;
    }

    public void downloadFile() {
        File downloadFile = null;

        int imageOrder = Integer.parseInt(imageSelection);
        DocStruct page = metadataBean.getDocument().getPhysicalDocStruct().getAllChildren().get(imageOrder);
        String imagename = page.getImageName();
        String filenamePrefix = imagename.substring(0, imagename.lastIndexOf("."));
        try {
            File[] filesInFolder = new File(metadataBean.getMyProzess().getImagesDirectory() + metadataBean.getCurrentTifFolder()).listFiles();
            for (File currentFile : filesInFolder) {
                String currentFileName = currentFile.getName();
                String currentFileNamePrefix = currentFileName.substring(0, currentFileName.lastIndexOf("."));
                if (filenamePrefix.equals(currentFileNamePrefix)) {
                    downloadFile = currentFile;
                    break;
                }
            }
        } catch (SwapException e1) {
            logger.error(e1);
        } catch (DAOException e1) {
            logger.error(e1);
        } catch (IOException e1) {
            logger.error(e1);
        } catch (InterruptedException e1) {
            logger.error(e1);
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

            String fileName = downloadFile.getName();
            ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
            String contentType = servletContext.getMimeType(fileName);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
            InputStream in = null;
            ServletOutputStream out = null;
            try {
                in = new FileInputStream(downloadFile);
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
}
