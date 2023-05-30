/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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

import lombok.Getter;

/**
 * This enum represents different web browsers and provides a method to parse the browser by its description. For each browser, an icon image file is
 * provided. It can be used to display different icons in the GUI.
 * 
 * @author Maurice Mueller
 *
 */
public enum Browser {

    /**
     * The Chrome browser
     */
    CHROME("Chrome", "chrome.png"),

    /**
     * The firefox browser
     */
    FIREFOX("Firefox", "firefox.png"),

    /**
     * The Microsoft Internet Explorer or Edge
     */
    INTERNET_EXPLORER("MSIE", "ie.png"),

    /**
     * The Konqueror browser
     */
    KONQUEROR("Konqueror", "konqueror.png"),

    /**
     * The Mozilla / Gecko browser
     */
    MOZILLA("Gecko", "mozilla.png"),

    /**
     * The Netscape browser
     */
    NETSCAPE("Netscape", "netscape.png"),

    /**
     * The Opera browser
     */
    OPERA("Opera", "opera.png"),

    /**
     * The Safari browser
     */
    SAFARI("Safari", "safari.png");

    /**
     * The name of this browser
     */
    @Getter
    private final String name;

    /**
     * The icon file name of this browser
     */
    @Getter
    private final String iconFileName;

    /**
     * A constructor to get a browser enum item
     *
     * @param name The name of the browser
     * @param icon The name of the icon file
     */
    private Browser(String name, String icon) {
        this.name = name;
        this.iconFileName = icon;
    }

    /**
     * Searches in the given string for a browser name and returns the browser item. When no browser is detectable, null is returned.
     *
     * When there are multiple names, in dependence of the priority the most relevant browser is returned.
     *
     * @param text The text to search the name of the browser in
     * @return The browser when one of them is detectable, otherwise null
     */
    public static Browser parseBrowser(String text) {
        Browser[] priorizedBrowsers = {
                Browser.FIREFOX,
                Browser.CHROME,
                Browser.INTERNET_EXPLORER,
                Browser.OPERA,
                Browser.KONQUEROR,
                Browser.NETSCAPE,
                Browser.SAFARI,
                Browser.MOZILLA
        };
        for (int browserIndex = 0; browserIndex < priorizedBrowsers.length; browserIndex++) {
            if (text.contains(priorizedBrowsers[browserIndex].getName())) {
                return priorizedBrowsers[browserIndex];
            }
        }
        return null;
    }

    /**
     * Returns the browser icon file name of the given browser. If the browser is null, "none.png" is returned
     *
     * @param browser The browser to get the icon file name for
     * @return The icon filename, otherwise "none.png"
     */
    public static String getIconFileName(Browser browser) {
        if (browser != null) {
            return browser.getIconFileName();
        } else {
            return "none.png";
        }
    }
}