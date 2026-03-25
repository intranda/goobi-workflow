package de.sub.goobi.media.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Process;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.SwapException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.servlet.controller.GetPdfAction;
import de.unigoettingen.sub.commons.contentlib.servlet.model.ContentServerConfiguration;
import de.unigoettingen.sub.commons.contentlib.servlet.model.SinglePdfRequest;
import de.unigoettingen.sub.commons.util.PathConverter;

public class PDFGenerator {

    private final String imagesDirectory;
    private final String altoDirectory;
    private final String processId;

    public PDFGenerator(Process process) throws IOException, SwapException {
        this.processId = Integer.toString(process.getId());
        this.imagesDirectory = process.getImagesDirectory();
        this.altoDirectory = process.getOcrAltoDirectory();
    }

    public String getImagesDirectory() {
        return imagesDirectory;
    }

    public String getProcessId() {
        return processId;
    }

    public String getAltoDirectory() {
        return altoDirectory;
    }

    /**
     * Creates a pdf from all image and pdf files from the images folder of the given name and writes it into the given OutputStream
     * 
     * @param folderName The name of a folder within the processes "images" directory
     * @param out An output stream to write the pdf to
     * @throws IOException
     * @throws ContentLibException
     * @throws URISyntaxException
     * @throws SwapException
     */
    public void writePDFFromFolder(String folderName, OutputStream out) throws URISyntaxException, ContentLibException, IOException, SwapException {

        List<String> imageFiles = StorageProvider.getInstance()
                .list(getImageFolderPath(folderName).toString(), NIOFileUtils.imageOrPdfNameFilter)
                .stream()
                .map(img -> getImageFolderURI(folderName).resolve(img).toString())
                .toList();

        SinglePdfRequest pdfRequest = new SinglePdfRequest(StringUtils.join(imageFiles, "$"), Collections.emptyMap());
        pdfRequest.setAltoSource(getAltoFolderURI());
        pdfRequest.setImageSource(getImageFolderURI(folderName));

        new GetPdfAction().writePdf(pdfRequest, ContentServerConfiguration.getInstance(), out);
    }

    private Path getImageFolderPath(String folderName) {
        return Path.of(this.getImagesDirectory()).resolve(Path.of(folderName).getFileName());
    }

    private URI getImageFolderURI(String folderName) {
        if (ConfigurationHelper.getInstance().useS3()) {
            return URI.create("s3://" + ConfigurationHelper.getInstance().getS3Bucket() + "/" + this.getProcessId() + "/images/"
                    + Paths.get(folderName).getFileName().toString() + "/");
        } else {
            return PathConverter.toURI(Path.of(this.getImagesDirectory()).resolve(Path.of(folderName).getFileName()));
        }
    }

    private URI getAltoFolderURI() {
        if (ConfigurationHelper.getInstance().useS3()) {
            return URI.create("s3://" + ConfigurationHelper.getInstance().getS3Bucket() + "/" + this.getProcessId() + "/ocr/"
                    + Paths.get(getAltoDirectory()).getFileName().toString() + "/");
        } else {
            return PathConverter.toURI(Path.of(getAltoDirectory()));
        }
    }

}
