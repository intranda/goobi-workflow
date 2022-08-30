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
package de.sub.goobi.mock;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class MockHttpServletResponse implements HttpServletResponse {

    @Override
    public void flushBuffer() throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public int getBufferSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getCharacterEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getContentType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Locale getLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isCommitted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resetBuffer() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBufferSize(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setCharacterEncoding(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setContentLength(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setContentType(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLocale(Locale arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addCookie(Cookie arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addDateHeader(String arg0, long arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addHeader(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addIntHeader(String arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean containsHeader(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String encodeRedirectURL(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String encodeRedirectUrl(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String encodeURL(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String encodeUrl(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getHeader(String arg0) {
        // TODO Auto-generated method stub
        return null;
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
    public int getStatus() {
        return status;
    }

    @Override
    public void sendError(int arg0) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendError(int arg0, String arg1) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendRedirect(String arg0) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDateHeader(String arg0, long arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHeader(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setIntHeader(String arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    private int status;

    @Override
    public void setStatus(int arg0) {
        status = arg0;
    }

    @Override
    public void setStatus(int arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setContentLengthLong(long arg0) {
        // TODO Auto-generated method stub

    }

}
