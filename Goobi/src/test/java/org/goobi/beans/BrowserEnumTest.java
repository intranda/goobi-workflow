package org.goobi.beans;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class BrowserEnumTest extends AbstractTest {

    @Test
    public void testEnumNames() {
        assertEquals("Chrome", Browser.CHROME.getName());
        assertEquals("Firefox", Browser.FIREFOX.getName());
        assertEquals("MSIE", Browser.INTERNET_EXPLORER.getName());
        assertEquals("Konqueror", Browser.KONQUEROR.getName());
        assertEquals("Gecko", Browser.MOZILLA.getName());
        assertEquals("Netscape", Browser.NETSCAPE.getName());
        assertEquals("Opera", Browser.OPERA.getName());
        assertEquals("Safari", Browser.SAFARI.getName());
    }

    @Test
    public void testEnumIcons() {
        assertEquals("chrome.png", Browser.CHROME.getIconFileName());
        assertEquals("firefox.png", Browser.FIREFOX.getIconFileName());
        assertEquals("ie.png", Browser.INTERNET_EXPLORER.getIconFileName());
        assertEquals("konqueror.png", Browser.KONQUEROR.getIconFileName());
        assertEquals("mozilla.png", Browser.MOZILLA.getIconFileName());
        assertEquals("netscape.png", Browser.NETSCAPE.getIconFileName());
        assertEquals("opera.png", Browser.OPERA.getIconFileName());
        assertEquals("safari.png", Browser.SAFARI.getIconFileName());
    }

    @Test
    public void testParseBrowser() {
        assertEquals(Browser.FIREFOX, Browser.parseBrowser("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0"));
        assertEquals(Browser.FIREFOX, Browser.parseBrowser("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:102.0) Gecko/20100101 Firefox/102.0"));
        assertEquals(Browser.SAFARI,
                Browser.parseBrowser("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Safari/537.36"));
    }

}
