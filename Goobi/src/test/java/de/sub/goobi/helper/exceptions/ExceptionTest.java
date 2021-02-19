package de.sub.goobi.helper.exceptions;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExceptionTest {

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
