package org.goobi.production.plugin;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This class stores the content of a span-Tag in the XHTML files. It contains a text and a type (an int constant).
 *
 * @author Maurice Mueller
 */
@AllArgsConstructor
public class SpanTag {
    public static final String LINE_NUMBER = "LINE_NUMBER";
    public static final String KEEP = "KEEP";
    public static final String INDENTION = "INDENTION";
    public static final String INSERTION = "INSERTION";
    public static final String DELETION = "DELETION";
    /**
     * This is the text content in a span block in the XHTML file.
     */
    @Getter
    private String text;
    /**
     * The type of a span block may be one of the above constants.
     */
    @Getter
    private String type;
}