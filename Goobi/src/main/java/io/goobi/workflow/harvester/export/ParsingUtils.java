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
package io.goobi.workflow.harvester.export;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class.
 **/
public final class ParsingUtils {

    private static final Logger logger = LoggerFactory.getLogger(ParsingUtils.class);

    /** Private constructor. */
    private ParsingUtils() {
    }

    /**
     * Converts a <code>String</code> from one given encoding to the other.
     * 
     * @param string The string to convert.
     * @param from Source encoding.
     * @param to Destination encoding.
     * @return The converted string.
     */
    public static String convertStringEncoding(String string, String from, String to) {
        try {
            Charset charsetFrom = Charset.forName(from);
            Charset charsetTo = Charset.forName(to);
            CharsetEncoder encoder = charsetFrom.newEncoder();
            CharsetDecoder decoder = charsetTo.newDecoder();
            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(string));
            CharBuffer cbuf = decoder.decode(bbuf);
            return cbuf.toString();
        } catch (CharacterCodingException e) {
            logger.error(e.getMessage(), e);
        }

        return string;
    }

    /**
     * insert some chars in the time string
     * 
     * @param milliseconds to add to the current utc time if milliSecondsAdd = 0, no milli are added
     * @return the time in the format YYYY-MM-DDThh:mm:ssZ
     */
    public static String getCurrentUTCTime(long milliSecondsAdd) {
        String time = "";
        Date d = new Date();
        if (milliSecondsAdd > 0) {
            long newTime = d.getTime() + milliSecondsAdd;
            d.setTime(newTime);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");// ;YYYY-MM-DDThh:mm:ssZ
        SimpleDateFormat hours = new SimpleDateFormat("HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        hours.setTimeZone(TimeZone.getTimeZone("GMT"));
        String yearMonthDay = format.format(d);
        String hourMinuteSeconde = hours.format(d);

        time = yearMonthDay + "T" + hourMinuteSeconde + "Z";
        return time;
    }

    /**
     * Retrieves a binary object from the given URL and returns it as a byte array.
     * 
     * @param url The URL to query.
     * @return byte[].
     */
    public static byte[] fetchImage(String url) {
        byte[] resp = null;

        if (url != null && url.length() > 0) {
            CloseableHttpClient httpClient = null;
            HttpGet method = null;
            CloseableHttpResponse response = null;
            try {
                String[] urlSplit = url.split("[/]");
                String urlNew = null;
                for (int i = 0; i < urlSplit.length; ++i) {
                    if (i == 0) {
                        urlNew = urlSplit[i];
                    } else {
                        urlNew += "/" + URLEncoder.encode(urlSplit[i], "utf-8");
                    }
                }
                httpClient = HttpClients.createDefault();
                method = new HttpGet(urlNew);
                logger.debug(urlNew);
                response = httpClient.execute(method);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    resp = new byte[(int) response.getEntity().getContentLength()];
                    response.getEntity().getContent().read(resp);
                } else {
                    logger.error("Connection failed : " + response.getStatusLine().getReasonPhrase() + "\n URL: " + url);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (response != null) {
                    try {
                        response.close();
                    } catch (IOException e) {
                    }
                }
                if (method != null) {
                    method.releaseConnection();
                }
                if (httpClient != null) {
                    try {
                        httpClient.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        return resp;
    }

    /**
     * 
     * @param url
     * @param fileExtension
     * @return
     */
    public static List<String> listImageFolderContents(String url, String fileExtension) {
        List<String> ret = new ArrayList<>();

        if (StringUtils.isNotEmpty(url)) {
            String regex = "\"[\\w|%|(|)]+[.]";
            for (int i = 0; i < fileExtension.length(); ++i) {
                regex += "[" + fileExtension.charAt(i) + "]";
            }
            regex += "\"";

            CloseableHttpClient httpClient = null;
            HttpGet method = null;
            CloseableHttpResponse response = null;
            StringWriter sw = null;
            try {
                httpClient = HttpClients.createDefault();
                method = new HttpGet(url);
                response = httpClient.execute(method);
                sw = new StringWriter();
                IOUtils.copy(response.getEntity().getContent(), sw);
                Pattern pattern = Pattern.compile(regex);
                Matcher m = pattern.matcher(sw.toString());
                while (m.find()) {
                    String fileName = m.group().replaceAll("[\"]", "");
                    if (!fileName.contains("directory")) {
                        ret.add(fileName);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (sw != null) {
                    try {
                        sw.close();
                    } catch (IOException e) {
                    }
                }
                if (response != null) {
                    try {
                        response.close();
                    } catch (IOException e) {
                    }
                }
                if (method != null) {
                    method.releaseConnection();
                }
                if (httpClient != null) {
                    try {
                        httpClient.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        return ret;
    }

    /**
     * 
     * @param string
     * @return
     * @should convert correctly
     */
    public static String convertUmlaut(String string) {
        if (StringUtils.isNotBlank(string)) {
            string = string.replaceAll("[ä]", "ae");
            string = string.replaceAll("[ö]", "oe");
            string = string.replaceAll("[ü]", "ue");
            logger.debug("upper: " + string.toUpperCase());
            if (string.replaceAll("[ß]", "").toUpperCase().equals(string.replaceAll("[ß]", ""))) {
                string = string.replaceAll("[Ä]", "AE");
                string = string.replaceAll("[Ö]", "OE");
                string = string.replaceAll("[Ü]", "UE");
                string = string.replaceAll("[ß]", "SS");
            } else {
                string = string.replaceAll("[Ä]", "Ae");
                string = string.replaceAll("[Ö]", "Oe");
                string = string.replaceAll("[Ü]", "Ue");
                string = string.replaceAll("[ß]", "ss");
            }
        }

        return string;
    }

    public static void main(String[] args) {
        // String url = "http://www.dhm.uni-greifswald.de/textband/band_42/";
        // List<String> fileNames = listImageFolderContents(url, "djvu");
        // for (String s : fileNames) {
        // System.out.println(s);
        // }

        String url = "http://greif.uni-greifswald.de/geogreif/geogreif-content/upload/P27Saßnitz-Hafen.jpg";
        fetchImage(url);
    }
}
