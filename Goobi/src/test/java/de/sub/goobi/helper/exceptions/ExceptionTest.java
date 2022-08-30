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
package de.sub.goobi.helper.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class ExceptionTest extends AbstractTest {

    @Test
    public void testDAOException() {
        NullPointerException npe = new NullPointerException();
        DAOException exception = new DAOException(npe);
        assertNotNull(exception);
    }

    @Test
    public void testDAOExceptionString() {
        DAOException exception = new DAOException("test");
        assertEquals("test", exception.getMessage());
    }

    @Test
    public void testExportFileException() {
        ExportFileException exception = new ExportFileException();
        assertNotNull(exception);
    }

    @Test
    public void testExportFileExceptionString() {
        ExportFileException exception = new ExportFileException("test");
        assertEquals("test", exception.getMessage());
    }

    @Test
    public void testImportPluginException() {
        ImportPluginException exception = new ImportPluginException();
        assertNotNull(exception);
    }

    @Test
    public void testImportPluginExceptionString() {
        ImportPluginException exception = new ImportPluginException("test");
        assertEquals("test", exception.getMessage());
    }

    @Test
    public void testImportPluginExceptionThrowable() {
        Throwable throwable = new Throwable();
        ImportPluginException exception = new ImportPluginException(throwable);
        assertEquals(throwable, exception.getCause());
    }

    @Test
    public void testImportPluginExceptionStringThrowable() {
        Throwable throwable = new Throwable();
        ImportPluginException exception = new ImportPluginException("test", throwable);
        assertEquals("test", exception.getMessage());
        assertEquals(throwable, exception.getCause());
    }

    @Test
    public void testInvalidImagesExceptionString() {
        InvalidImagesException exception = new InvalidImagesException("test");
        assertEquals("test", exception.getMessage());
    }

    @Test
    public void testInvalidImagesExceptionThrowable() {
        Throwable throwable = new Throwable();
        Exception e = new ImportPluginException(throwable);
        InvalidImagesException exception = new InvalidImagesException(e);
        assertNotNull(exception);
    }

    @Test
    public void testSwapException() {
        SwapException exception = new SwapException();
        assertNotNull(exception);
    }

    @Test
    public void testSwapExceptionString() {
        SwapException exception = new SwapException("test");
        assertEquals("test", exception.getMessage());
    }

    @Test
    public void testUghHelperException() {
        UghHelperException exception = new UghHelperException();
        assertNotNull(exception);
    }

    @Test
    public void testUghHelperExceptionString() {
        UghHelperException exception = new UghHelperException("test");
        assertEquals("test", exception.getMessage());
    }
}
