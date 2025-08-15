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

package io.goobi.workflow.api.connection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class HttpUtils {

    private HttpUtils() {
        // hide implicit public constructor
    }

    private static final String WRONG_STATUS_CODE_PREFIX = "Wrong status code: ";

    public static final ResponseHandler<byte[]> byteArrayResponseHandler = new ResponseHandler<>() {
        @Override
        public byte[] handleResponse(HttpResponse response) throws IOException {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.error(WRONG_STATUS_CODE_PREFIX + response.getStatusLine().getStatusCode());
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

    public static final ResponseHandler<String> stringResponseHandler = new ResponseHandler<>() {
        @Override
        public String handleResponse(HttpResponse response) throws IOException {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.error(WRONG_STATUS_CODE_PREFIX + response.getStatusLine().getStatusCode());
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity, StandardCharsets.UTF_8);
            } else {
                return null;
            }
        }
    };

    public static final ResponseHandler<InputStream> streamResponseHandler = new ResponseHandler<>() {
        @Override
        public InputStream handleResponse(HttpResponse response) throws IOException {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.error(WRONG_STATUS_CODE_PREFIX + response.getStatusLine().getStatusCode());
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

    private static void setupProxy(String url, HttpGet method) {
        if (ConfigurationHelper.getInstance().isUseProxy()) {
            try {
                URL ipAsURL = new URI(url).toURL();
                if (!ConfigurationHelper.getInstance().isProxyWhitelisted(ipAsURL)) {
                    HttpHost proxy = new HttpHost(ConfigurationHelper.getInstance().getProxyUrl(), ConfigurationHelper.getInstance().getProxyPort());
                    log.debug("Using proxy " + proxy.getHostName() + ":" + proxy.getPort());

                    Builder builder = RequestConfig.custom();
                    builder.setProxy(proxy);

                    RequestConfig rc = builder.build();

                    method.setConfig(rc);
                } else {
                    log.debug("url was on proxy whitelist, no proxy used: " + url);
                }
            } catch (MalformedURLException | URISyntaxException e) {
                log.debug("could not convert into URL: ", url);
            }

        }
    }

    private static CloseableHttpClient getClientWithBasicAuthentication(String... parameter) {
        CloseableHttpClient client;
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(parameter[3], Integer.parseInt(parameter[4])),
                new UsernamePasswordCredentials(parameter[1], parameter[2]));
        client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        return client;
    }

    //  parameter:
    // * first: url
    // * second: username
    // * third: password
    // * forth: scope (e.g. "localhost")
    // * fifth: port
    public static String getStringFromUrl(String... parameter) {
        String response = "";
        if (parameter == null) {
            return response;
        }
        CloseableHttpClient client = null;
        String url = parameter[0];
        HttpGet method = new HttpGet(url);

        if (parameter.length > 4) {
            client = getClientWithBasicAuthentication(parameter);

        } else {
            client = HttpClientBuilder.create().build(); // client will never be null
        }

        setupProxy(url, method);

        try {
            response = client.execute(method, HttpUtils.stringResponseHandler); // also implies that client != null
        } catch (IOException e) {
            log.error("Cannot execute URL " + url, e);
        } finally {
            method.releaseConnection();

            try {
                client.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
        return response;
    }

    /**
     * @deprecated This method is replaced with getStringFromUrl(String...)
     *
     * @param url
     * @param parameter
     * @return response as string
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public static String getStringFromUrl(String url, String[] parameter) {
        String response = "";
        CloseableHttpClient client = null;
        HttpGet method = new HttpGet(url);

        if ((parameter != null) && (parameter.length > 3)) {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(parameter[2], Integer.parseInt(parameter[3])),
                    new UsernamePasswordCredentials(parameter[0], parameter[1]));
            client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        } else { // if parameter == null || parameter.length <= 3
            client = HttpClientBuilder.create().build(); // client will never be null
        }

        if (ConfigurationHelper.getInstance().isUseProxy()) {
            try {
                URL ipAsURL = new URL(url);
                if (!ConfigurationHelper.getInstance().isProxyWhitelisted(ipAsURL)) {
                    HttpHost proxy = new HttpHost(ConfigurationHelper.getInstance().getProxyUrl(), ConfigurationHelper.getInstance().getProxyPort());
                    log.debug("Using proxy " + proxy.getHostName() + ":" + proxy.getPort());

                    RequestConfig.Builder builder = RequestConfig.custom();
                    builder.setProxy(proxy);

                    RequestConfig rc = builder.build();

                    method.setConfig(rc);
                } else {
                    log.debug("url was on proxy whitelist, no proxy used: " + url);
                }
            } catch (MalformedURLException e) {
                log.debug("could not convert into URL: ", url);
            }
        }

        try {
            response = client.execute(method, stringResponseHandler); // also implies that client != null
        } catch (IOException e) {
            log.error("Cannot execute URL " + url, e);
        } finally {
            method.releaseConnection();

            try {
                client.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
        return response;
    }

    //  parameter:
    // * first: url
    // * second: username
    // * third: password
    // * forth: scope (e.g. "localhost")
    // * fifth: port

    public static OutputStream getStreamFromUrl(OutputStream out, String... parameter) {
        CloseableHttpClient httpclient = null;
        HttpGet method = null;
        InputStream istr = null;
        String url = parameter[0];
        try {

            method = new HttpGet(url);
            if (parameter.length > 4) {
                httpclient = getClientWithBasicAuthentication(parameter);

            } else {
                httpclient = HttpClientBuilder.create().build();

            }

            setupProxy(url, method);

            Integer contentServerTimeOut = ConfigurationHelper.getInstance().getGoobiContentServerTimeOut();
            Builder builder = RequestConfig.custom();
            builder.setSocketTimeout(contentServerTimeOut);
            RequestConfig rc = builder.build();
            method.setConfig(rc);

            byte[] response = httpclient.execute(method, HttpUtils.byteArrayResponseHandler);
            if (response == null) {
                log.error("Response stream is null");
            }
            istr = new ByteArrayInputStream(response);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = istr.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            log.error("Unable to connect to url " + url, e);

        } finally {
            if (method != null) {
                method.releaseConnection();
            }
            if (httpclient != null) {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
            if (istr != null) {
                try {
                    istr.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }

        return out;
    }

}