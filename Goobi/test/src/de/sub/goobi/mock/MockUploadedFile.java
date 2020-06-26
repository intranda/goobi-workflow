package de.sub.goobi.mock;

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
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;

import javax.servlet.http.Part;

public class MockUploadedFile implements Part, Serializable {

    private static final long serialVersionUID = -1271567035180962097L;

    private InputStream stream;
    private String name;
    private String header;

    public MockUploadedFile(InputStream stream, String name) {
        super();
        this.stream = stream;
        this.name = name;
        header = "filename=" + name;
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
    public void delete() throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getHeader(String arg0) {

        return header;
    }

    @Override
    public Collection<String> getHeaderNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> getHeaders(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void write(String arg0) throws IOException {
        // TODO Auto-generated method stub

    }

    public String getSubmittedFileName() {
        // TODO Auto-generated method stub
        return null;
    }

}
