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

package de.sub.goobi.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import org.goobi.beans.Process;
import org.jdom2.JDOMException;
import org.mozilla.universalchardet.UniversalDetector;

import de.intranda.digiverso.ocr.alto.model.structureclasses.logical.AltoDocument;
import de.intranda.digiverso.ocr.conversion.ConvertAbbyyToAltoStaX;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider.StorageType;
import de.sub.goobi.helper.exceptions.SwapException;
import lombok.extern.log4j.Log4j2;

/**
 * Helper class for file system operations.
 */
@Log4j2
public class FilesystemHelper {

    private static final String ALTO = ".alto";
    private static final String TXT = ".txt";
    private static final String XML = ".xml";

    private static final String LINEBREAK = "\n";
    private static final String XML_LINEBREAK = "<br />";

    /**
     * Creates a directory with a name given. Under Linux a script is used to set the file system permissions accordingly. This cannot be done from
     * within java code before version 1.7.
     * 
     * @param dirName Name of directory to create
     * @throws InterruptedException If the thread running the script is interrupted by another thread while it is waiting, then the wait is ended and
     *             an InterruptedException is thrown.
     * @throws IOException If an I/O error occurs.
     */

    public static void createDirectory(String dirName) throws IOException, InterruptedException {
        if (ConfigurationHelper.getInstance().useS3() && S3FileUtils.getPathStorageType(Paths.get(dirName)) == StorageType.S3) {
            return;
        }
        if (!StorageProvider.getInstance().isDirectory(Paths.get(dirName))) {
            if (ConfigurationHelper.getInstance().getScriptCreateDirMeta().isEmpty() || ConfigurationHelper.getInstance().useS3()) {
                StorageProvider.getInstance().createDirectories(Paths.get(dirName));
            } else {
                ShellScript createDirScript = new ShellScript(Paths.get(ConfigurationHelper.getInstance().getScriptCreateDirMeta()));
                createDirScript.run(Arrays.asList(dirName));
            }
        }
    }

    /**
     * Creates a directory with a name given and assigns permissions to the given user. Under Linux a script is used to set the file system
     * permissions accordingly. This cannot be done from within java code before version 1.7.
     * 
     * @param dirName Name of directory to create
     * @throws InterruptedException If the thread running the script is interrupted by another thread while it is waiting, then the wait is ended and
     *             an InterruptedException is thrown.
     * @throws IOException If an I/O error occurs.
     */

    public static boolean createDirectoryForUser(String dirName, String userName) throws IOException, InterruptedException {
        if (!StorageProvider.getInstance().isFileExists(Paths.get(dirName))) {
            if (ConfigurationHelper.getInstance().getScriptCreateDirUserHome().isEmpty()) {
                StorageProvider.getInstance().createDirectories(Paths.get(dirName));
            } else {
                ShellScript createDirScript = new ShellScript(Paths.get(ConfigurationHelper.getInstance().getScriptCreateDirUserHome()));
                createDirScript.run(Arrays.asList(userName, dirName));
            }
        }
        return StorageProvider.getInstance().isFileExists(Paths.get(dirName));
    }

    public static void deleteSymLink(String symLink) {
        String command = ConfigurationHelper.getInstance().getScriptDeleteSymLink();
        if (!command.isEmpty() && !ConfigurationHelper.getInstance().useS3()) {
            ShellScript deleteSymLinkScript;
            try {
                deleteSymLinkScript = new ShellScript(Paths.get(command));
                deleteSymLinkScript.run(Arrays.asList(symLink));
            } catch (FileNotFoundException e) {
                log.error("FileNotFoundException in deleteSymLink()", e);
                Helper.setFehlerMeldung("Couldn't find script file, error", e.getMessage());
            } catch (IOException e) {
                log.error("IOException in deleteSymLink()", e);
                Helper.setFehlerMeldung("Aborted deleteSymLink(), error", e.getMessage());
            } catch (InterruptedException e) { //NOSONAR InterruptedException must not be re-thrown as it is not running in a separate thread
                log.error("InterruptedException in deleteSymLink()", e);
                Helper.setFehlerMeldung("Command '" + command + "' is interrupted in deleteSymLink()!");
            }
        }
    }

    /**
     * generate nice looking file sizes for files sizes given in bytes
     * 
     * @param size size of a file as long
     * @return String with nice looking file size
     */
    public static String getFileSizeShort(long size) {
        if (size < 1024) {
            return String.format("%d B", size);
        }
        String[] names = { "KB", "MB", "GB", "TB", "PB", "EB" };
        int count = 0;
        while (size / 1024 > 1024 && count < names.length - 1) {
            count++;
            size = size / 1024;
        }
        return String.format("%4.2f %s", ((double) size) / 1024, names[count]);
    }

    public static String getFileEncoding(Path file) throws IOException {
        byte[] buf = new byte[4096];
        String encoding = null;
        try (InputStream fis = StorageProvider.getInstance().newInputStream(file)) {
            UniversalDetector detector = new UniversalDetector(null);
            int nread;
            while (((nread = fis.read(buf)) > 0) && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();
            encoding = detector.getDetectedCharset();
            detector.reset();
        }
        if (encoding == null) {
            return "UTF-8";
        } else {
            return encoding;
        }
    }

    public static boolean isOcrFileExists(Process inProcess, String ocrFile) {
        // TODO are different checks for local and s3 needed?
        try {
            if (!ConfigurationHelper.getInstance().useS3()) {
                File txt = new File(inProcess.getOcrTxtDirectory(), ocrFile + TXT);
                File xml = new File(inProcess.getOcrXmlDirectory(), ocrFile + XML);
                File alto = new File(inProcess.getOcrAltoDirectory(), ocrFile + XML);
                return (txt.exists() && txt.canRead()) || (xml.exists() && xml.canRead()) || (alto.exists() && alto.canRead());
            } else {
                Path txt = Paths.get(inProcess.getOcrTxtDirectory(), ocrFile + TXT);
                Path xml = Paths.get(inProcess.getOcrXmlDirectory(), ocrFile + XML);
                Path alto = Paths.get(inProcess.getOcrAltoDirectory(), ocrFile + XML);
                StorageProviderInterface sp = StorageProvider.getInstance();
                return sp.isFileExists(xml) || sp.isFileExists(txt) || sp.isFileExists(alto);
            }
        } catch (SwapException | IOException e) {
            return false;
        }
    }

    public static String getOcrFileContent(Process inProcess, String ocrFile) {
        try {
            Path ocrfile = null;
            StorageProviderInterface sp = StorageProvider.getInstance();
            Path textFolder = Paths.get(inProcess.getOcrTxtDirectory());
            Path xmlFolder = Paths.get(inProcess.getOcrXmlDirectory());
            Path altoFolder = Paths.get(inProcess.getOcrAltoDirectory());
            if (sp.isFileExists(textFolder)) {
                // try to return content from txt folder
                ocrfile = textFolder.resolve(ocrFile + TXT);

                StringBuilder response = new StringBuilder();
                String buffer = null;
                String encoding = getFileEncoding(ocrfile);
                try (BufferedReader in = new BufferedReader(new InputStreamReader(sp.newInputStream(ocrfile), encoding))) {
                    while ((buffer = in.readLine()) != null) {
                        response.append(buffer.replaceAll("(\\s+)", " ")).append(XML_LINEBREAK);
                    }
                }
                return response.toString();
            } else if (sp.isFileExists(altoFolder)) {
                ocrfile = altoFolder.resolve(ocrFile + ALTO);
                if (!sp.isFileExists(ocrfile)) {
                    ocrfile = altoFolder.resolve(ocrFile + XML);
                }
                AltoDocument alto = AltoDocument.getDocumentFromFile(ocrfile.toFile());
                return alto.getContent().replace(LINEBREAK, XML_LINEBREAK);
            } else if (sp.isFileExists(xmlFolder)) {
                // try to return content from xml folder
                ocrfile = xmlFolder.resolve(ocrFile + XML);
                ConvertAbbyyToAltoStaX converter = new ConvertAbbyyToAltoStaX();
                try (InputStream input = sp.newInputStream(ocrfile)) {
                    AltoDocument alto = converter.convertToASM(input, ocrfile.getFileName().toString());
                    return alto.getContent().replace(LINEBREAK, XML_LINEBREAK);
                }
            }

        } catch (FileNotFoundException e) {
            try {
                log.debug("no OCR file found for image " + inProcess.getImagesDirectory() + ocrFile);
            } catch (IOException | SwapException e1) {
                log.error(e1);
            }
        } catch (IOException | SwapException | XMLStreamException | JDOMException e) {
            log.error("Problem reading the OCR file", e);

        }
        return "- no ocr content -";
    }

}