package org.goobi.beans;

import lombok.Getter;

/**
 * This enum represents a browser of a user.
 * 
 * It contains a list of possible browsers and supports a parsing routine to detect which browser the user has.
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
     * The name of the browser
     */
    @Getter
    private final String name;

    /**
     * The icon file name for this browser
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
     * Returns the browser icon file name of the given browser. When the browser is null, "none.png" is returned
     *
     * @param browser The browser to get the icon file name for
     * @return The icon filename, otherwise null
     */
    public static String getIconFileName(Browser browser) {
        if (browser != null) {
            return browser.getIconFileName();
        } else {
            return "none.png";
        }
    }
}