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
        this.handleCharacter(SpanTag.KEEP, character);
    }

    /**
     * Adds an inserted character to the result.
     *
     * @param character The character that was inserted
     */
    @Override
    public void visitInsertCommand(Character character) {
        this.handleCharacter(SpanTag.INSERTION, character);
    }

    /**
     * Adds a deleted character to the result.
     *
     * @param character The character that was deleted
     */
    @Override
    public void visitDeleteCommand(Character character) {
        this.handleCharacter(SpanTag.DELETION, character);
    }

    /**
     * Handles a character. Adds a new SpanTag object and sets the current mode.
     *
     * @param mode The mode to use now
     * @param character The character to handle
     */
    private void handleCharacter(String mode, Character character) {
        if (character == ' ') {
            mode = SpanTag.SPACE;
        }
        if (this.currentMode != mode || mode == SpanTag.SPACE) {
            if (this.currentText.length() > 0) {
                this.spanTags.add(new SpanTag(this.currentText, this.currentMode));
            }
            this.currentMode = mode;
            this.currentText = "";
        }
        this.currentText += this.escapeCharacter(character);
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

    /**
     * Returns the current text
     *
     * @return The current text
     */
    public String getCurrentText() {
        return this.currentText;
    }

    /**
     * Returns the current mode
     *
     * @return The current mode
     */
    public String getCurrentMode() {
        return this.currentMode;
    }
}