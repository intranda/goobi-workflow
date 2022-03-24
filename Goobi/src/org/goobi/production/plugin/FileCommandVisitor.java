package org.goobi.production.plugin;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.text.diff.CommandVisitor;

/**
 * This class stores two file contents and their result from comparison via CommandVisitor.
 *
 * @author Maurice Mueller
 */
public class FileCommandVisitor implements CommandVisitor<Character> {

    private List<SpanTag> deletionSpanTags;
    private List<SpanTag> insertionSpanTags;

    private String currentDeletionMode = SpanTag.TEXT_NORMAL;
    private String currentInsertionMode = SpanTag.TEXT_NORMAL;
    private String currentDeletionText = "";
    private String currentInsertionText = "";
    private String lineMode = "keep";

    /**
     * A constructor to get a FileCommandVisitor object and define the initial line mode
     *
     * @param mode The initial line mode (may be "insertion", "deletion" or "keep"
     */
    public FileCommandVisitor(String mode) {
        this.resetDeletionSpanTags();
        this.resetInsertionSpanTags();
        this.lineMode = mode;
    }

    /**
     * Adds a kept character to the result.
     *
     * @param character The character that was kept
     */
    @Override
    public void visitKeepCommand(Character character) {
        if (this.lineMode.equals("insertion")) {
            this.handleCharacter(SpanTag.TEXT_INSERTED_PASSIVE, character, "keep");
        } else if (this.lineMode.equals("deletion")) {
            this.handleCharacter(SpanTag.TEXT_DELETED_PASSIVE, character, "keep");
        } else {// this.lineMode.equals("keep")
            this.handleCharacter(SpanTag.TEXT_NORMAL, character, "keep");
        }
    }

    /**
     * Adds an inserted character to the result.
     *
     * @param character The character that was inserted
     */
    @Override
    public void visitInsertCommand(Character character) {
        this.handleCharacter(SpanTag.TEXT_INSERTED_ACTIVE, character, "insertion");
    }

    /**
     * Adds a deleted character to the result.
     *
     * @param character The character that was deleted
     */
    @Override
    public void visitDeleteCommand(Character character) {
        this.handleCharacter(SpanTag.TEXT_DELETED_ACTIVE, character, "deletion");
        }

    /**
     * Handles a character. Adds a new SpanTag object and sets the current mode.
     *
     * @param mode The mode to use now
     * @param character The character to handle
     * @mode The which "keep", "insertion" or "deletion" to know which text should be extended
     */
    private void handleCharacter(String mode, Character character, String which) {
        boolean spaceMode = (character == ' ');
        if (spaceMode) {
            switch (mode) {
                case SpanTag.TEXT_NORMAL:
                    mode = SpanTag.SPACE_NORMAL;
                    break;
                case SpanTag.TEXT_INSERTED_ACTIVE:
                    mode = SpanTag.SPACE_INSERTED_ACTIVE;
                    break;
                case SpanTag.TEXT_INSERTED_PASSIVE:
                    mode = SpanTag.SPACE_INSERTED_PASSIVE;
                    break;
                case SpanTag.TEXT_DELETED_ACTIVE:
                    mode = SpanTag.SPACE_DELETED_ACTIVE;
                    break;
                case SpanTag.TEXT_DELETED_PASSIVE:
                    mode = SpanTag.SPACE_DELETED_PASSIVE;
                    break;
            }
        }
        if (which.equals("keep") || which.equals("insertion")) {
            if (this.currentInsertionMode != mode || spaceMode) {
                if (this.currentInsertionText.length() > 0) {
                    this.insertionSpanTags.add(new SpanTag(this.currentInsertionText, this.currentInsertionMode));
                }
                this.currentInsertionMode = mode;
                this.currentInsertionText = "";
            }
            this.currentInsertionText += this.escapeCharacter(character);
        }
        if (which.equals("keep") || which.equals("deletion")) {
            if (this.currentDeletionMode != mode || spaceMode) {
                if (this.currentDeletionText.length() > 0) {
                    this.deletionSpanTags.add(new SpanTag(this.currentDeletionText, this.currentDeletionMode));
                }
                this.currentDeletionMode = mode;
                this.currentDeletionText = "";
            }
            this.currentDeletionText += this.escapeCharacter(character);
        }
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
     * Returns the list of deletion span tag elements containing the differences for a line
     *
     * @return The list of deletion span tag elements
     */
    public List<SpanTag> getDeltionSpanTags() {
        return this.deletionSpanTags;
    }

    /**
     * Resets the list of deletion span tags
     */
    public void resetDeletionSpanTags() {
        this.deletionSpanTags = new ArrayList<>();
    }

    /**
     * Returns the list of insertion span tag elements containing the differences for a line
     *
     * @return The list of insertion span tag elements
     */
    public List<SpanTag> getInsertionSpanTags() {
        return this.insertionSpanTags;
    }

    /**
     * Resets the list of insertion span tags
     */
    public void resetInsertionSpanTags() {
        this.insertionSpanTags = new ArrayList<>();
    }

    /**
     * Returns the current deletion text
     *
     * @return The current deletion text
     */
    public String getCurrentDeletionText() {
        return this.currentDeletionText;
    }

    /**
     * Returns the current insertion text
     *
     * @return The current insertion text
     */
    public String getCurrentInsertionText() {
        return this.currentInsertionText;
    }

    /**
     * Returns the current deletion mode
     *
     * @return The current deletion mode
     */
    public String getCurrentDeletionMode() {
        return this.currentDeletionMode;
    }

    /**
     * Returns the current insertion mode
     *
     * @return The current insertion mode
     */
    public String getCurrentInsertionMode() {
        return this.currentInsertionMode;
    }
}