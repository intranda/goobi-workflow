package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Dimension;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class ImageLevelTest extends AbstractTest {

    private String url = "https://example.com/test/";
    private Dimension size = new Dimension(200, 200);

    @Test
    public void testImageLevelConstructorWithDimension() {
        ImageLevel fixture = new ImageLevel(url, size);
        assertNotNull(fixture);
    }

    @Test
    public void testImageLevelConstructorWithPx() {
        ImageLevel fixture = new ImageLevel(url, 200, 200);
        assertNotNull(fixture);
    }

    @Test
    public void testSize() {
        ImageLevel fixture = new ImageLevel(url, size);
        assertEquals(size, fixture.getSize());
    }

    @Test
    public void testWidth() {
        ImageLevel fixture = new ImageLevel(url, size);
        assertEquals(200, fixture.getWidth());
    }

    @Test
    public void testHeight() {
        ImageLevel fixture = new ImageLevel(url, size);
        assertEquals(200, fixture.getHeight());
    }

    @Test
    public void testToString() {
        ImageLevel fixture = new ImageLevel(url, size);
        assertEquals("{\"url\" : \"https://example.com/test/\",\"width\" : 200,\"height\" : 200}", fixture.toString());
    }

    @Test
    public void testCompareTo() {
        ImageLevel fixture = new ImageLevel(url, size);
        ImageLevel sameSize = new ImageLevel(url, 200, 200);
        ImageLevel smaller = new ImageLevel(url, 100, 100);
        ImageLevel larger = new ImageLevel(url, 300, 300);

        assertEquals(0, fixture.compareTo(sameSize));
        assertEquals(1, fixture.compareTo(smaller));
        assertEquals(-1, fixture.compareTo(larger));
    }
}
