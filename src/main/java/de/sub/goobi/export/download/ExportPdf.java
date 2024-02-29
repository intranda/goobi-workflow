package de.sub.goobi.export.download;

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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;

import org.goobi.beans.Process;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.HelperForm;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.helper.tasks.CreatePdfFromServletThread;
import lombok.extern.log4j.Log4j2;
import ugh.dl.Fileformat;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

@Log4j2
public class ExportPdf extends ExportMets {

    @Override
    public boolean startExport(Process myProzess, String inZielVerzeichnis) throws IOException, InterruptedException, PreferencesException,
            WriteException, DocStructHasNoTypeException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException,
            SwapException, DAOException, TypeNotAllowedForParentException {

        /*
         * -------------------------------- Read Document --------------------------------
         */
        Fileformat gdzfile = myProzess.readMetadataFile();
        String zielVerzeichnis = prepareUserDirectory(inZielVerzeichnis);
        this.myPrefs = myProzess.getRegelsatz().getPreferences();

        /*
         * -------------------------------- first of all write mets-file in images-Folder of process --------------------------------
         */

        Path metsTempFile = StorageProvider.getInstance().createTemporaryFile(myProzess.getTitel(), ".xml");
        writeMetsFile(myProzess, metsTempFile.toString(), gdzfile, true);
        Helper.setMeldung(null, myProzess.getTitel() + ": ", "mets file created");
        Helper.setMeldung(null, myProzess.getTitel() + ": ", "start pdf generation now");
        log.debug("METS file created: " + metsTempFile);
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
        String fullpath = req.getRequestURL().toString();
        String servletpath = context.getExternalContext().getRequestServletPath();
        String myBasisUrl = fullpath.substring(0, fullpath.indexOf(servletpath));

        Path imagesPath = Paths.get(myProzess.getImagesTifDirectory(true));
        if (!StorageProvider.getInstance().isFileExists(imagesPath)
                || StorageProvider.getInstance().list(imagesPath.toString(), NIOFileUtils.imageNameFilter).isEmpty()) {
            imagesPath = Paths.get(myProzess.getImagesOrigDirectory(true));
        }
        Path pdfPath = Paths.get(myProzess.getOcrPdfDirectory());
        Path altoPath = Paths.get(myProzess.getOcrAltoDirectory());

        if (!ConfigurationHelper.getInstance().isPdfAsDownload()) {
            /*
             * -------------------------------- use contentserver api for creation of pdf-file --------------------------------
             */
            CreatePdfFromServletThread pdf = new CreatePdfFromServletThread();
            pdf.setMetsURL(metsTempFile.toUri().toURL());
            pdf.setTargetFolder(Paths.get(zielVerzeichnis));
            pdf.setInternalServletPath(myBasisUrl);
            pdf.setImagePath(imagesPath);
            pdf.setPdfPath(pdfPath);
            pdf.setAltoPath(altoPath);
            log.debug("Target directory: " + zielVerzeichnis);
            log.debug("Using ContentServer2 base URL: " + myBasisUrl);
            pdf.initialize(myProzess);
            pdf.start();
        } else {

            try {
                /*
                 * -------------------------------- define path for mets and pdfs --------------------------------
                 */
                URL goobiContentServerUrl = null;

                /*
                 * -------------------------------- using mets file --------------------------------
                 */

                if (StorageProvider.getInstance().isFileExists(metsTempFile)) {

                    goobiContentServerUrl = UriBuilder.fromUri(new HelperForm().getServletPathWithHostAsUrl())
                            .path("api")
                            .path("process")
                            .path("pdf")
                            .path(Integer.toString(myProzess.getId()))
                            .path(myProzess.getTitel() + ".pdf")
                            .queryParam("metsFile", metsTempFile)
                            .queryParam("imageSource", imagesPath.toUri())
                            .queryParam("pdfSource", pdfPath.toUri())
                            .queryParam("altoSource", altoPath.toUri())
                            .build()
                            .toURL();

                    /*
                     * -------------------------------- mets data does not exist or is invalid --------------------------------
                     */

                } else {

                    goobiContentServerUrl = UriBuilder.fromUri(new HelperForm().getServletPathWithHostAsUrl())
                            .path("api")
                            .path("process")
                            .path("image")
                            .path(Integer.toString(myProzess.getId()))
                            .path("media") //dummy, replaced by images query param
                            .path("00000001.tif") //dummy, replaced by images query param
                            .path(myProzess.getTitel() + ".pdf")
                            .queryParam("imageSource", imagesPath.toUri())
                            .queryParam("pdfSource", pdfPath.toUri())
                            .queryParam("altoSource", altoPath.toUri())
                            .queryParam("images", createImagesParameter(myProzess))
                            .build()
                            .toURL();
                }

                /*
                 * -------------------------------- get pdf from servlet and forward response to file --------------------------------
                 */

                if (!context.getResponseComplete()) {
                    log.debug("Redirecting pdf request to " + goobiContentServerUrl.toString());
                    HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
                    String fileName = myProzess.getTitel() + ".pdf";
                    ServletContext servletContext = (ServletContext) context.getExternalContext().getContext();
                    String contentType = servletContext.getMimeType(fileName);
                    response.setContentType(contentType);
                    response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
                    response.sendRedirect(goobiContentServerUrl.toString());
                    context.responseComplete();
                }

            } catch (Exception e) { //NOSONAR InterruptedException must not be re-thrown as it is not running in a separate thread

                /*
                 * -------------------------------- report Error to User as Error-Log --------------------------------
                 */
                String text = "error while pdf creation: " + e.getMessage();
                Path file = Paths.get(zielVerzeichnis, myProzess.getTitel() + ".PDF-ERROR.log");
                try {
                    log.error(e);
                    try (Writer output = new BufferedWriter(new FileWriter(file.toFile()))) {
                        output.write(text);
                    }
                } catch (IOException e1) {
                    log.error(e1);
                }
                return false;
            }
        }
        return true;
    }

    private String createImagesParameter(Process myProzess) throws IOException, SwapException, MalformedURLException {
        StringBuilder images = new StringBuilder();
        List<Path> meta = StorageProvider.getInstance().listFiles(myProzess.getImagesTifDirectory(true), NIOFileUtils.imageNameFilter);
        ArrayList<String> filenames = new ArrayList<>();
        for (Path data : meta) {
            String file = "";
            file += data.toUri().toURL();
            filenames.add(file);
        }
        for (String f : filenames) {
            images.append(f).append("$");
        }
        images = images.deleteCharAt(images.length() - 1);
        return images.toString();
    }
}
