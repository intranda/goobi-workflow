package de.sub.goobi.helper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class HttpClientHelper {

    private static final Logger logger = Logger.getLogger(HttpClientHelper.class);
    
  public  static ResponseHandler<byte[]> byteArrayResponseHandler = new ResponseHandler<byte[]>() {
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
}
