package org.goobi.production.plugin;

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
 */
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FileCommandVisitorTest {

    @Test
    public void testConstructorKeepMode() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_KEEP);
        assertNotNull(visitor);
        assertNotNull(visitor.getDeltionSpanTags());
        assertNotNull(visitor.getInsertionSpanTags());
        assertTrue(visitor.getDeltionSpanTags().isEmpty());
        assertTrue(visitor.getInsertionSpanTags().isEmpty());
        assertEquals("", visitor.getCurrentDeletionText());
        assertEquals("", visitor.getCurrentInsertionText());
        assertEquals(SpanTag.TEXT_NORMAL, visitor.getCurrentDeletionMode());
        assertEquals(SpanTag.TEXT_NORMAL, visitor.getCurrentInsertionMode());
    }

    @Test
    public void testConstructorInsertionMode() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_INSERTION);
        assertNotNull(visitor);
    }

    @Test
    public void testConstructorDeletionMode() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_DELETION);
        assertNotNull(visitor);
    }

    @Test
    public void testVisitKeepCommandInKeepModeAccumulatesText() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_KEEP);
        visitor.visitKeepCommand('a');
        visitor.visitKeepCommand('b');
        assertEquals("ab", visitor.getCurrentInsertionText());
        assertEquals("ab", visitor.getCurrentDeletionText());
    }

    @Test
    public void testVisitKeepCommandInKeepModeSetsNormalMode() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_KEEP);
        visitor.visitKeepCommand('a');
        assertEquals(SpanTag.TEXT_NORMAL, visitor.getCurrentInsertionMode());
        assertEquals(SpanTag.TEXT_NORMAL, visitor.getCurrentDeletionMode());
    }

    @Test
    public void testVisitKeepCommandInInsertionModeSetsInsertedPassive() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_INSERTION);
        visitor.visitKeepCommand('a');
        assertEquals(SpanTag.TEXT_INSERTED_PASSIVE, visitor.getCurrentInsertionMode());
        assertEquals(SpanTag.TEXT_INSERTED_PASSIVE, visitor.getCurrentDeletionMode());
    }

    @Test
    public void testVisitKeepCommandInDeletionModeSetsDeletedPassive() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_DELETION);
        visitor.visitKeepCommand('a');
        assertEquals(SpanTag.TEXT_DELETED_PASSIVE, visitor.getCurrentInsertionMode());
        assertEquals(SpanTag.TEXT_DELETED_PASSIVE, visitor.getCurrentDeletionMode());
    }

    @Test
    public void testVisitInsertCommandAccumulatesInsertionText() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_KEEP);
        visitor.visitInsertCommand('x');
        visitor.visitInsertCommand('y');
        assertEquals("xy", visitor.getCurrentInsertionText());
    }

    @Test
    public void testVisitInsertCommandSetsInsertedActiveMode() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_KEEP);
        visitor.visitInsertCommand('x');
        assertEquals(SpanTag.TEXT_INSERTED_ACTIVE, visitor.getCurrentInsertionMode());
    }

    @Test
    public void testVisitInsertCommandDoesNotAffectDeletionText() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_KEEP);
        visitor.visitInsertCommand('x');
        assertEquals("", visitor.getCurrentDeletionText());
    }

    @Test
    public void testVisitDeleteCommandAccumulatesDeletionText() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_KEEP);
        visitor.visitDeleteCommand('z');
        visitor.visitDeleteCommand('w');
        assertEquals("zw", visitor.getCurrentDeletionText());
    }

    @Test
    public void testVisitDeleteCommandSetsDeletedActiveMode() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_KEEP);
        visitor.visitDeleteCommand('z');
        assertEquals(SpanTag.TEXT_DELETED_ACTIVE, visitor.getCurrentDeletionMode());
    }

    @Test
    public void testVisitDeleteCommandDoesNotAffectInsertionText() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_KEEP);
        visitor.visitDeleteCommand('z');
        assertEquals("", visitor.getCurrentInsertionText());
    }

    @Test
    public void testSpaceInKeepModeUsesSpaceNormalMode() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_KEEP);
        visitor.visitKeepCommand(' ');
        // Space character causes a mode flush – the space itself should be the current text
        assertEquals(" ", visitor.getCurrentInsertionText());
        assertEquals(SpanTag.SPACE_NORMAL, visitor.getCurrentInsertionMode());
    }

    @Test
    public void testSpaceInInsertCommandUsesSpaceInsertedActiveMode() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_KEEP);
        visitor.visitInsertCommand(' ');
        assertEquals(SpanTag.SPACE_INSERTED_ACTIVE, visitor.getCurrentInsertionMode());
    }

    @Test
    public void testSpaceInDeleteCommandUsesSpaceDeletedActiveMode() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_KEEP);
        visitor.visitDeleteCommand(' ');
        assertEquals(SpanTag.SPACE_DELETED_ACTIVE, visitor.getCurrentDeletionMode());
    }

    @Test
    public void testModeChangeFlushesPreviousTextToSpanTags() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_KEEP);
        visitor.visitKeepCommand('a');
        visitor.visitKeepCommand('b');
        // Force a mode change by switching to insert
        visitor.visitInsertCommand('c');
        // "ab" should have been flushed to the insertion span tags list
        assertEquals(1, visitor.getInsertionSpanTags().size());
        assertEquals("ab", visitor.getInsertionSpanTags().get(0).getText());
        assertEquals(SpanTag.TEXT_NORMAL, visitor.getInsertionSpanTags().get(0).getType());
    }

    @Test
    public void testResetDeletionSpanTags() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_KEEP);
        visitor.visitKeepCommand('a');
        visitor.visitInsertCommand('b'); // flush "a" to deletion span tags
        visitor.resetDeletionSpanTags();
        assertTrue(visitor.getDeltionSpanTags().isEmpty());
    }

    @Test
    public void testResetInsertionSpanTags() {
        FileCommandVisitor visitor = new FileCommandVisitor(FileCommandVisitor.MODE_KEEP);
        visitor.visitKeepCommand('a');
        visitor.visitInsertCommand('b'); // flush "a" to insertion span tags
        visitor.resetInsertionSpanTags();
        assertTrue(visitor.getInsertionSpanTags().isEmpty());
    }
}
