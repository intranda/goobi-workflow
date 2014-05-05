package de.sub.goobi.mock;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.myfaces.custom.fileupload.StorageStrategy;
import org.apache.myfaces.custom.fileupload.UploadedFile;

public class MockUploadedFile implements UploadedFile{

 
    private static final long serialVersionUID = -1271567035180962097L;

    private InputStream stream;
    private String name;
    
    public MockUploadedFile (InputStream stream, String name) {
        super();
        this.stream = stream;
        this.name = name;
    }
    
    @Override
    public byte[] getBytes() throws IOException {
        return IOUtils.toByteArray(stream);
    }

    @Override
    public String getContentType() {
        return "application/xml";
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return stream;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public StorageStrategy getStorageStrategy() {
        // TODO Auto-generated method stub
        return null;
    }

}
