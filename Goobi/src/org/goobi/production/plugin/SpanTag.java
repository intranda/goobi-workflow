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
    public static final String TEXT_NORMAL = "TEXT_NORMAL";
    public static final String TEXT_INSERTED_PASSIVE = "TEXT_INSERTED_PASSIVE";
    public static final String TEXT_INSERTED_ACTIVE = "TEXT_INSERTED_ACTIVE";
    public static final String TEXT_DELETED_PASSIVE = "TEXT_DELETED_PASSIVE";
    public static final String TEXT_DELETED_ACTIVE = "TEXT_DELETED_ACTIVE";
    public static final String SPACE_NORMAL = "SPACE_NORMAL";
    public static final String SPACE_INSERTED_PASSIVE = "SPACE_INSERTED_PASSIVE";
    public static final String SPACE_INSERTED_ACTIVE = "SPACE_INSERTED_ACTIVE";
    public static final String SPACE_DELETED_PASSIVE = "SPACE_DELETED_PASSIVE";
    public static final String SPACE_DELETED_ACTIVE = "SPACE_DELETED_ACTIVE";
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