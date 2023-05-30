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
package org.goobi.production.plugin;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.text.diff.CommandVisitor;

import lombok.extern.log4j.Log4j2;

/**
 * This class stores two file contents and their result from comparison via CommandVisitor.
 *
 * @author Maurice Mueller
 */
@Log4j2
public class FileCommandVisitor implements CommandVisitor<Character> {

    protected static final String MODE_KEEP = "keep";
    protected static final String MODE_INSERTION = "insertion";
    protected static final String MODE_DELETION = "deletion";

    private List<SpanTag> deletionSpanTags;
    private List<SpanTag> insertionSpanTags;

    private String currentDeletionMode = SpanTag.TEXT_NORMAL;
    private String currentInsertionMode = SpanTag.TEXT_NORMAL;
    private String currentDeletionText = "";
    private String currentInsertionText = "";
    private String lineMode = MODE_KEEP;

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
        if (this.lineMode.equals(MODE_INSERTION)) {
            this.handleCharacter(SpanTag.TEXT_INSERTED_PASSIVE, character, MODE_KEEP);
        } else if (this.lineMode.equals(MODE_DELETION)) {
            this.handleCharacter(SpanTag.TEXT_DELETED_PASSIVE, character, MODE_KEEP);
        } else {// this.lineMode.equals(MODE_KEEP)
            this.handleCharacter(SpanTag.TEXT_NORMAL, character, MODE_KEEP);
        }
    }

    /**
     * Adds an inserted character to the result.
     *
     * @param character The character that was inserted
     */
    @Override
    public void visitInsertCommand(Character character) {
        this.handleCharacter(SpanTag.TEXT_INSERTED_ACTIVE, character, MODE_INSERTION);
    }

    /**
     * Adds a deleted character to the result.
     *
     * @param character The character that was deleted
     */
    @Override
    public void visitDeleteCommand(Character character) {
        this.handleCharacter(SpanTag.TEXT_DELETED_ACTIVE, character, MODE_DELETION);
    }

    /**
     * Handles a character. Adds a new SpanTag object and sets the current mode.
     *
     * @param mode The mode to use now
     * @param character The character to handle
     * @param which The mode "keep", "insertion" or "deletion" to know which text should be extended
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
                default:
                    log.trace("Unused span tag in FileCommandVisitor.handleCharacter(): " + mode);
            }
        }
        if (which.equals(MODE_KEEP) || which.equals(MODE_INSERTION)) {
            if (!this.currentInsertionMode.equals(mode) || spaceMode) {
                if (this.currentInsertionText.length() > 0) {
                    this.insertionSpanTags.add(new SpanTag(this.currentInsertionText, this.currentInsertionMode));
                }
                this.currentInsertionMode = mode;
                this.currentInsertionText = "";
            }
            this.currentInsertionText += this.escapeCharacter(character);
        }
        if (which.equals(MODE_KEEP) || which.equals(MODE_DELETION)) {
            if (!this.currentDeletionMode.equals(mode) || spaceMode) {
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