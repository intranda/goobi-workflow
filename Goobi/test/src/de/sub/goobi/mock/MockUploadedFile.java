package de.sub.goobi.mock;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
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
        return null;
    }

}
