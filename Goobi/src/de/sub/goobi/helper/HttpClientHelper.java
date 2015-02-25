package de.sub.goobi.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
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
 */
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import de.sub.goobi.config.ConfigurationHelper;

public class HttpClientHelper {

    private static final Logger logger = Logger.getLogger(HttpClientHelper.class);

    public static ResponseHandler<byte[]> byteArrayResponseHandler = new ResponseHandler<byte[]>() {
        @Override
        public byte[] handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("Wrong status code : " + response.getStatusLine().getStatusCode());
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toByteArray(entity);
            } else {
                return null;
            }
        }
    };

    public static ResponseHandler<String> stringResponseHandler = new ResponseHandler<String>() {
        @Override
        public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("Wrong status code : " + response.getStatusLine().getStatusCode());
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity);
            } else {
                return null;
            }
        }
    };

    public static ResponseHandler<InputStream> streamResponseHandler = new ResponseHandler<InputStream>() {
        @Override
        public InputStream handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("Wrong status code : " + response.getStatusLine().getStatusCode());
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return new ByteArrayInputStream(EntityUtils.toByteArray(entity));
            } else {
                return null;
            }
        }
    };

    public static String getStringFromUrl(String url) {
        String response = "";
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet method = new HttpGet(url);
        try {
            response = client.execute(method, HttpClientHelper.stringResponseHandler);
        } catch (IOException e) {
            logger.error("Cannot execute URL " + url, e);
        } finally {
            method.releaseConnection();

            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }
        return response;
    }

    public static OutputStream getStreamFromUrl(String url, OutputStream out) {
        CloseableHttpClient httpclient = null;
        HttpGet method = null;
        InputStream istr = null;
        try {
            httpclient = HttpClientBuilder.create().build();
            method = new HttpGet(url);
            Integer contentServerTimeOut = ConfigurationHelper.getInstance().getGoobiContentServerTimeOut();
            Builder builder = RequestConfig.custom();
            builder.setSocketTimeout(contentServerTimeOut);
            RequestConfig rc = builder.build();
            method.setConfig(rc);

            byte[] response = httpclient.execute(method, HttpClientHelper.byteArrayResponseHandler);
            if (response == null) {
                logger.error("Response stream is null");
            }
            istr = new ByteArrayInputStream(response);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = istr.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
            logger.error("Unable to connect to url " + url, e);

        } finally {
            method.releaseConnection();
            if (httpclient != null) {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
            if (istr != null) {
                try {
                    istr.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }

        return out;
    }

}
