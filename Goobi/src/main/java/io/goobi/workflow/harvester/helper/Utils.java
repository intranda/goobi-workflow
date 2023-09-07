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
package io.goobi.workflow.harvester.helper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sub.goobi.config.ConfigHarvester;
import de.sub.goobi.helper.ShellScript;
import io.goobi.workflow.harvester.DataManager;
import io.goobi.workflow.harvester.export.GoobiImportThread;

public class Utils {

    public static DateTimeFormatter formatterISO8601DateTimeMS = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
    public static DateTimeFormatter formatterISO8601DateTimeFullWithTimeZone = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /** Logger for this class. */
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * Runs ProcessImportModule as a command line tool in a separate thread (if not already running).
     */
    public static void runGoobiImport() {
        if (!ConfigHarvester.getInstance().isGoobiImportEnabled()) {
            return;
        }
        if (!DataManager.getInstance().isImportRunning()) {
            File file = new File(ConfigHarvester.getInstance().getImportModule());
            if (file.exists()) {
                new GoobiImportThread(file).start();
                logger.info("Goobi ProcessImportModule started.");
            } else {
                logger.error("Goobi ProcessImportModule not found at '" + file.getAbsolutePath() + "'.");
            }
        } else {
            logger.info("Goobi ProcessImportModule is already running.");
        }
    }

    /**
     * 
     * @param url
     * @param outputFolder
     * @param outputFilename
     * @param useProxy
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static File downloadFile(final String url, final File outputFolder, final String outputFilename, final boolean useProxy)
            throws ClientProtocolException, IOException {
        logger.trace("downloadFile: {}", url);
        String destFileName = outputFilename;

        HttpGet method = null;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            method = new HttpGet(url);
            if (useProxy) {
                HttpHost proxy = new HttpHost("PROXYURL", 0);
                httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            }
            HttpResponse response = httpclient.execute(method);
            switch (response.getStatusLine().getStatusCode()) {
                case 404:
                    logger.warn("404 returned for URL {}", url);
                    throw new IOException("404 returned for URL: " + url);
                case 500:
                    throw new IOException(response.getStatusLine().getReasonPhrase());
                default: // nothing
            }

            // If no destination file name was provioded, check for suggested file name in the response header
            if (destFileName == null) {
                destFileName = extractFileNameFromResponseHeader(response);
            }
            // Otherwise use a timestamp
            if (destFileName == null) {
                logger.warn("No file name found for download URL '{}', using timestamp...", url);
                destFileName = String.valueOf(System.currentTimeMillis());
            }

            HttpEntity entity = response.getEntity();
            byte[] data;
            if (entity != null) {
                data = EntityUtils.toByteArray(entity);
            } else {
                data = new byte[0];
            }
            File file = new File(outputFolder, destFileName);
            try (InputStream is = new ByteArrayInputStream(data); OutputStream os = new FileOutputStream(file)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) > 0) {
                    os.write(buf, 0, len);
                }
            }

            return file;
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    /**
     * 
     * @param response
     * @return
     * @should extract file name correctly
     */
    protected static String extractFileNameFromResponseHeader(HttpResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("response may not be null");
        }

        Header headerCD = response.getFirstHeader("Content-Disposition");
        if (headerCD != null) {
            for (HeaderElement headerElement : headerCD.getElements()) {
                if (headerElement.getName().equalsIgnoreCase("attachment")) {
                    NameValuePair nv = headerElement.getParameterByName("filename");
                    if (nv != null) {
                        return nv.getValue();
                    }
                }
            }
        }

        return null;
    }

    @Deprecated
    static ResponseHandler<byte[]> responseHandler = new ResponseHandler<byte[]>() {
        @Override
        public byte[] handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            if (response.getStatusLine().getStatusCode() == 404) {
                throw new IOException("404");
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toByteArray(entity);
            }

            return new byte[0];
        }
    };

    /**
     * 
     * @param scriptFilePath
     * @param pdfFile
     * @param page
     * @return Error message
     * @throws FileNotFoundException
     */
    public static String runScript(String scriptFilePath, String[] args) throws FileNotFoundException {
        logger.trace("runScript: {}, {}", scriptFilePath, args);
        Path toolPath = Paths.get(scriptFilePath.trim());
        ShellScript script = new ShellScript(toolPath);
        try {
            script.run(Arrays.asList(args));
            if (!script.getStdErr().isEmpty()) {
                StringBuilder sbErr = new StringBuilder();
                for (String error : script.getStdErr()) {
                    sbErr.append(error).append("\n");
                }
                logger.error(sbErr.toString());
                return sbErr.toString();
            } else if (!script.getStdOut().isEmpty()) {
                StringBuilder sbOut = new StringBuilder();
                for (String out : script.getStdOut()) {
                    sbOut.append(out).append("\n");
                }
                logger.info(sbOut.toString());
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }

        return "";
    }
}
