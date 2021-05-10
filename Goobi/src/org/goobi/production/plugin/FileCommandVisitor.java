package org.goobi.production.plugin;

import lombok.Getter;

import org.apache.commons.text.diff.CommandVisitor;

/**
 * This class stores two file contents and their result from comparison via CommandVisitor.
 *
 * @author Maurice Mueller
 */
public class FileCommandVisitor implements CommandVisitor<Character> {

    private static final String PATTERN = "${text}";
    private static final String DELETION = "<span style=\"background-color: #FB504B\">" + PATTERN + "</span>";
    private static final String INSERTION = "<span style=\"background-color: #45EA85\">" + PATTERN + "</span>";
    private static final String HTML_LINEBREAK = "<br />";
    private static final String JAVA_LINEBREAK = System.getProperty("line.separator");

    @Getter
    private String left = "";
    @Getter
    private String right = "";

    /**
     * Keeps the character in both strings. Replaces java line breaks with HTML line breaks.
     *
     * @param character The character that was kept
     */
    @Override
    public void visitKeepCommand(Character character) {
        if (character == null) {
            return;
        }
        String toAppend = this.replaceLineBreak(character);
        this.left += toAppend;
        this.right += toAppend;
    }

    /**
     * Adds an inserted character to the right string. Replaces java line breaks with HTML line breaks.
     *
     * @param character The character that was inserted
     */
    @Override
    public void visitInsertCommand(Character character) {
        if (character == null) {
            return;
        }
        String toAppend = this.replaceLineBreak(character);
        this.right += INSERTION.replace(PATTERN, "" + toAppend);
    }

    /**
     * Adds a deleted character to the left string. Replaces java line breaks with HTML line breaks.
     *
     * @param character The character that was deleted
     */
    @Override
    public void visitDeleteCommand(Character character) {
        if (character == null) {
            return;
        }
        String toAppend = this.replaceLineBreak(character);
        this.left += DELETION.replace(PATTERN, "" + toAppend);
    }

    /**
     * Converts the character to a string. When it is a line break, it is replaced by the HTML typical line break.
     * 
     * @param character The character to convert and replace
     * @return The string containing the character or a replaced HTML line break
     */
    private String replaceLineBreak(Character character) {
        String result = "" + character;
        if (result.equals(JAVA_LINEBREAK)) {
            result = HTML_LINEBREAK;
        }
        return result;
    }
}