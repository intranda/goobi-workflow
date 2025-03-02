package de.sub.goobi.helper.tasks;

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
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.goobi.beans.Process;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.HelperForm;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.metadaten.MetadatenVerifizierung;
import io.goobi.workflow.api.connection.HttpUtils;
import jakarta.ws.rs.core.UriBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/*************************************************************************************
 * Creation of PDF-Files as long running task for GoobiContentServerServlet First of all the variables have to be set via the setters after that you
 * can initialize and run it
 * 
 * @author Steffen Hankiewicz
 * @version 12.02.2009
 *************************************************************************************/
@Log4j2
public class CreatePdfFromServletThread extends LongRunningTask {
    @Setter
    private Path targetFolder;
    @Setter
    private String internalServletPath;
    @Getter
    @Setter
    private URL metsURL;
    @Getter
    @Setter
    private Path imagePath = null;
    @Getter
    @Setter
    private Path pdfPath = null;
    @Getter
    @Setter
    private Path altoPath = null;

    @Override
    public void initialize(Process inProzess) {
        super.initialize(inProzess);
        setTitle("Create PDF: " + inProzess.getTitel());
    }

    /**
     * Aufruf als Thread ================================================================
     */
    @Override
    public void run() {
        setStatusProgress(30);
        if (this.getProzess() == null || this.targetFolder == null || this.internalServletPath == null) {
            setStatusMessage("parameters for temporary and final folder and internal servlet path not defined");
            setStatusProgress(-1);
            return;
        }
        CloseableHttpClient httpclient = null;
        HttpGet method = null;
        try {
            httpclient = HttpClientBuilder.create().build();
            /* --------------------------------
             * define path for mets and pdfs
             * --------------------------------*/
            URL goobiContentServerUrl = null;
            Path tempPdf = StorageProvider.getInstance().createTemporaryFile(this.getProzess().getTitel(), ".pdf");
            Path finalPdf = Paths.get(this.targetFolder.toString(), this.getProzess().getTitel() + ".pdf");
            Integer contentServerTimeOut = ConfigurationHelper.getInstance().getGoobiContentServerTimeOut();

            String imageSource = "";
            String pdfSource = "";
            String altoSource = "";

            if (StringUtils.isNotBlank(imageSource)) {
                imageSource = "&imageSource=" + getImagePath().toUri();
            }
            if (StringUtils.isNotBlank(pdfSource)) {
                pdfSource = "&pdfSource=" + getPdfPath().toUri();
            }
            if (StringUtils.isNotBlank(altoSource)) {
                altoSource = "&altoSource=" + getAltoPath().toUri();
            }

            /* --------------------------------
             * using mets file
             * --------------------------------*/

            if (new MetadatenVerifizierung().validate(this.getProzess()) && this.metsURL != null) {

                goobiContentServerUrl = UriBuilder.fromUri(new HelperForm().getServletPathWithHostAsUrl())
                        .path("api")
                        .path("process")
                        .path("pdf")
                        .path(Integer.toString(this.getProzess().getId()))
                        .path(this.getProzess().getTitel() + ".pdf")
                        .queryParam("metsFile", this.metsURL)
                        .queryParam("imageSource", getImagePath().toUri())
                        .queryParam("pdfSource", getPdfPath().toUri())
                        .queryParam("altoSource", getAltoPath().toUri())
                        .queryParam("goobiMetsFile", "true")
                        .build()
                        .toURL();

                /* --------------------------------
                 * mets data does not exist or is invalid
                 * --------------------------------*/

            } else {
                goobiContentServerUrl = UriBuilder.fromUri(new HelperForm().getServletPathWithHostAsUrl())
                        .path("api")
                        .path("process")
                        .path("image")
                        .path(Integer.toString(getProzess().getId()))
                        .path("media") //dummy, replaced by images query param
                        .path("00000001.tif") //dummy, replaced by images query param
                        .path(getProzess().getTitel() + ".pdf")
                        .queryParam("imageSource", getImagePath().toUri())
                        .queryParam("pdfSource", getPdfPath().toUri())
                        .queryParam("altoSource", getAltoPath().toUri())
                        .queryParam("images", createImagesParameter(getProzess()))
                        .build()
                        .toURL();
            }

            /* --------------------------------
             * get pdf from servlet and forward response to file
             * --------------------------------*/

            if (log.isDebugEnabled()) {
                log.debug("Retrieving: " + goobiContentServerUrl.toString());
            }
            method = new HttpGet(goobiContentServerUrl.toString());

            RequestConfig.Builder builder = RequestConfig.custom();
            builder.setSocketTimeout(contentServerTimeOut);
            RequestConfig rc = builder.build();
            method.setConfig(rc);

            try {
                byte[] response = httpclient.execute(method, HttpUtils.byteArrayResponseHandler);
                try (InputStream istr = new ByteArrayInputStream(response);
                        OutputStream ostr = new FileOutputStream(tempPdf.toFile())) {

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = istr.read(buf)) > 0) {
                        ostr.write(buf, 0, len);
                    }
                }
            } catch (IOException e) {
                log.error(e);
            } finally {
                method.releaseConnection();
                if (httpclient != null) {
                    httpclient.close();
                }
            }

            /* --------------------------------
             * copy pdf from temp to final destination
             * --------------------------------*/
            if (log.isDebugEnabled()) {
                log.debug("pdf file created: " + tempPdf.toString() + "; now copy it to " + finalPdf.toString());
            }
            StorageProvider.getInstance().copyFile(tempPdf, finalPdf);
            if (log.isDebugEnabled()) {
                log.debug("pdf copied to " + finalPdf.toString() + "; now start cleaning up");
            }
            StorageProvider.getInstance().deleteDir(tempPdf);
            if (this.metsURL != null) {
                Path tempMets = Paths.get(this.metsURL.toString());
                StorageProvider.getInstance().deleteDir(tempMets);
            }
        } catch (Exception e) {
            log.error("Error while creating pdf for " + this.getProzess().getTitel(), e);
            setStatusMessage("error " + e.getClass().getSimpleName() + " while pdf creation: " + e.getMessage());
            setStatusProgress(-1);

            /* --------------------------------
             * report Error to User as Error-Log
             * --------------------------------*/
            String text = "error while pdf creation: " + e.getMessage();
            Path file = Paths.get(this.targetFolder.toString(), this.getProzess().getTitel() + ".PDF-ERROR.log");
            try {
                try (Writer output = new BufferedWriter(new FileWriter(file.toFile()))) {
                    output.write(text);
                }
            } catch (IOException e1) {
                log.error("Error while reporting error to user in file " + file.toString(), e);
            }
            return;
        } finally {
            if (method != null) {
                method.releaseConnection();
            }

        }
        setStatusMessage("done");
        setStatusProgress(100);
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
