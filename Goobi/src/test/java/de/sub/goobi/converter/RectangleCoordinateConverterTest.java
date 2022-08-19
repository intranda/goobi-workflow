package de.sub.goobi.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import org.junit.Test;

import com.thoughtworks.xstream.converters.ConversionException;

public class RectangleCoordinateConverterTest {


    @Test
    public void testConstructor() {
        RectangleCoordinateConverter conv = new RectangleCoordinateConverter();
        assertNotNull(conv);
    }

    @Test(expected = ConversionException.class)
    public void testGetAsObjectInvalid() {
        RectangleCoordinateConverter conv = new RectangleCoordinateConverter();
        String emptyTestString = "";
        conv.getAsObject(null, null, emptyTestString);
    }

    @Test
    public void testGetAsObject() {
        RectangleCoordinateConverter conv = new RectangleCoordinateConverter();
        String testString = "1.0,2.0,3.0,4.0";
        Rectangle2D rect = conv.getAsObject(null, null, testString);
        assertEquals(1.0, rect.getX(), 0);
        assertEquals(2.0, rect.getY(), 0);
        assertEquals(2.0, rect.getWidth(), 0);
        assertEquals(2.0, rect.getHeight(), 0);
    }

    @Test
    public void testGetAsString() {
        RectangleCoordinateConverter conv = new RectangleCoordinateConverter();
        Rectangle2D rect = new Rectangle(1, 2, 3, 4);
        String fixture = conv.getAsString(null, null, rect);
        assertEquals("1.0,2.0,4.0,6.0", fixture);
    }
}
