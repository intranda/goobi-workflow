package de.sub.goobi.helper;

import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.services.s3.model.S3Object;

public class S3ObjectCloserInputStream extends InputStream {

    private InputStream inputStream;
    private S3Object s3Object;

    public S3ObjectCloserInputStream(InputStream inputStream, S3Object s3Object) {
        this.inputStream = inputStream;
        this.s3Object = s3Object;
    }

    @Override
    public int read() throws IOException {

        return inputStream.read();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        s3Object.close();
    }
}
