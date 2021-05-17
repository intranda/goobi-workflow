package org.goobi.production.plugin;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import org.apache.commons.text.diff.CommandVisitor;

/**
 * This class stores two file contents and their result from comparison via CommandVisitor.
 *
 * @author Maurice Mueller
 */
public class FileCommandVisitor implements CommandVisitor<Character> {

    private List<SpanTag> spanTags;

    private String currentMode = SpanTag.KEEP;
    private String currentText = "";

    public FileCommandVisitor() {
        this.resetSpanTags();
    }

    /**
     * Adds a kept character to the result.
     *
     * @param character The character that was kept
     */
    @Override
    public void visitKeepCommand(Character character) {
        String string = this.escapeCharacter(character);
        this.spanTags.add(new SpanTag(string, SpanTag.KEEP));
        this.currentMode = SpanTag.KEEP;
    }

    /**
     * Adds an inserted character to the result.
     *
     * @param character The character that was inserted
     */
    @Override
    public void visitInsertCommand(Character character) {
        String string = this.escapeCharacter(character);
        this.spanTags.add(new SpanTag(string, SpanTag.INSERTION));
        this.currentMode = SpanTag.INSERTION;
    }

    /**
     * Adds a deleted character to the result.
     *
     * @param character The character that was deleted
     */
    @Override
    public void visitDeleteCommand(Character character) {
        String string = this.escapeCharacter(character);
        this.spanTags.add(new SpanTag(string, SpanTag.DELETION));
        this.currentMode = SpanTag.DELETION;
    }

    /**
     * Converts the character to a string. Special characters will be escaped.
     *
     * @param character The character to convert and replace
     * @return The string containing the (replaced) character
     */
    private String escapeCharacter(Character character) {
        if (character == null) {
            return "";
        }
        //if (character == '\t') {
            //return "    ";
        //}
        return String.valueOf(character);
    }

    /**
     * Returns the list of span tag elements containing the differences for a line
     *
     * @return The list of span tag elements
     */
    public List<SpanTag> getSpanTags() {
        return this.spanTags;
    }

    /**
     * Resets the list of span tags
     */
    public void resetSpanTags() {
        this.spanTags = new ArrayList<>();
    }
}