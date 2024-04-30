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

    @Test
    public void testGetIconFileName() {
        assertEquals("chrome.png", Browser.getIconFileName(Browser.CHROME));
        assertEquals("firefox.png", Browser.getIconFileName(Browser.FIREFOX));
        assertEquals("ie.png", Browser.getIconFileName(Browser.INTERNET_EXPLORER));
        assertEquals("konqueror.png", Browser.getIconFileName(Browser.KONQUEROR));
        assertEquals("mozilla.png", Browser.getIconFileName(Browser.MOZILLA));
        assertEquals("netscape.png", Browser.getIconFileName(Browser.NETSCAPE));
        assertEquals("opera.png", Browser.getIconFileName(Browser.OPERA));
        assertEquals("safari.png", Browser.getIconFileName(Browser.SAFARI));
        // In difference to testEnumIcons(), a default image icon file is returned for 'null' in the static method Browser.getIconFileName(Browser):
        assertEquals("none.png", Browser.getIconFileName(null));
    }

}
