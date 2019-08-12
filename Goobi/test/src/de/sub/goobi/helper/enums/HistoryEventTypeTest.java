package de.sub.goobi.helper.enums;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - https://goobi.io
 *          - https://www.intranda.com 
 *          - https://github.com/intranda/goobi
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
import static org.junit.Assert.*;

import org.junit.Test;

public class HistoryEventTypeTest {

    @Test
    public void testEnumValues() {
        assertEquals(new Integer(0), HistoryEventType.unknown.getValue());
        assertEquals(new Integer(1), HistoryEventType.storageDifference.getValue());
        assertEquals(new Integer(2), HistoryEventType.imagesWorkDiff.getValue());
        assertEquals(new Integer(3), HistoryEventType.imagesMasterDiff.getValue());
        assertEquals(new Integer(4), HistoryEventType.metadataDiff.getValue());
        assertEquals(new Integer(5), HistoryEventType.docstructDiff.getValue());
        assertEquals(new Integer(6), HistoryEventType.stepDone.getValue());
        assertEquals(new Integer(7), HistoryEventType.stepOpen.getValue());
        assertEquals(new Integer(8), HistoryEventType.stepInWork.getValue());
        assertEquals(new Integer(9), HistoryEventType.stepError.getValue());
        assertEquals(new Integer(10), HistoryEventType.stepLocked.getValue());
        assertEquals(new Integer(11), HistoryEventType.bitonal.getValue());
        assertEquals(new Integer(12), HistoryEventType.grayScale.getValue());
        assertEquals(new Integer(13), HistoryEventType.color.getValue());
    }

    @Test
    public void testEnumTitles() {
        assertEquals("unknown", HistoryEventType.unknown.getTitle());
        assertEquals("Storage Increase", HistoryEventType.storageDifference.getTitle());
        assertEquals("imagesWorkDiff", HistoryEventType.imagesWorkDiff.getTitle());
        assertEquals("imagesMasterDiff", HistoryEventType.imagesMasterDiff.getTitle());
        assertEquals("metadataDiff", HistoryEventType.metadataDiff.getTitle());
        assertEquals("docstructDiff", HistoryEventType.docstructDiff.getTitle());
        assertEquals("stepDone", HistoryEventType.stepDone.getTitle());
        assertEquals("stepOpen", HistoryEventType.stepOpen.getTitle());
        assertEquals("stepInWork", HistoryEventType.stepInWork.getTitle());
        assertEquals("stepError", HistoryEventType.stepError.getTitle());
        assertEquals("stepLocked", HistoryEventType.stepLocked.getTitle());
        assertEquals("imagesBitonalDiff", HistoryEventType.bitonal.getTitle());
        assertEquals("imagesGrayScaleDiff", HistoryEventType.grayScale.getTitle());
        assertEquals("imagesColorDiff", HistoryEventType.color.getTitle());
    }

    @Test
    public void testEnumIsNumeric() {
        assertEquals(false, HistoryEventType.unknown.isNumeric());
        assertEquals(true, HistoryEventType.storageDifference.isNumeric());
        assertEquals(true, HistoryEventType.imagesWorkDiff.isNumeric());
        assertEquals(true, HistoryEventType.imagesMasterDiff.isNumeric());
        assertEquals(true, HistoryEventType.metadataDiff.isNumeric());
        assertEquals(true, HistoryEventType.docstructDiff.isNumeric());
        assertEquals(true, HistoryEventType.stepDone.isNumeric());
        assertEquals(true, HistoryEventType.stepOpen.isNumeric());
        assertEquals(true, HistoryEventType.stepInWork.isNumeric());
        assertEquals(true, HistoryEventType.stepError.isNumeric());
        assertEquals(true, HistoryEventType.stepLocked.isNumeric());
        assertEquals(true, HistoryEventType.bitonal.isNumeric());
        assertEquals(true, HistoryEventType.grayScale.isNumeric());
        assertEquals(true, HistoryEventType.color.isNumeric());
    }

    @Test
    public void testEnumIsString() {
        assertEquals(false, HistoryEventType.unknown.isString());
        assertEquals(false, HistoryEventType.storageDifference.isString());
        assertEquals(false, HistoryEventType.imagesWorkDiff.isString());
        assertEquals(false, HistoryEventType.imagesMasterDiff.isString());
        assertEquals(false, HistoryEventType.metadataDiff.isString());
        assertEquals(false, HistoryEventType.docstructDiff.isString());
        assertEquals(true, HistoryEventType.stepDone.isString());
        assertEquals(true, HistoryEventType.stepOpen.isString());
        assertEquals(true, HistoryEventType.stepInWork.isString());
        assertEquals(true, HistoryEventType.stepError.isString());
        assertEquals(true, HistoryEventType.stepLocked.isString());
        assertEquals(false, HistoryEventType.bitonal.isString());
        assertEquals(false, HistoryEventType.grayScale.isString());
        assertEquals(false, HistoryEventType.color.isString());
    }

    @Test
    public void testEnumGetGrouping() {
        assertEquals(null, HistoryEventType.unknown.getGroupingFunction());
        assertEquals(null, HistoryEventType.storageDifference.getGroupingFunction());
        assertEquals(null, HistoryEventType.imagesWorkDiff.getGroupingFunction());
        assertEquals(null, HistoryEventType.imagesMasterDiff.getGroupingFunction());
        assertEquals(null, HistoryEventType.metadataDiff.getGroupingFunction());
        assertEquals(null, HistoryEventType.docstructDiff.getGroupingFunction());
        assertEquals("min", HistoryEventType.stepDone.getGroupingFunction());
        assertEquals("min", HistoryEventType.stepOpen.getGroupingFunction());
        assertEquals(null, HistoryEventType.stepInWork.getGroupingFunction());
        assertEquals(null, HistoryEventType.stepError.getGroupingFunction());
        assertEquals("max", HistoryEventType.stepLocked.getGroupingFunction());
        assertEquals(null, HistoryEventType.bitonal.getGroupingFunction());
        assertEquals(null, HistoryEventType.grayScale.getGroupingFunction());
        assertEquals(null, HistoryEventType.color.getGroupingFunction());
    }

    @Test
    public void testEnumTypeFromValue() {
        HistoryEventType type = HistoryEventType.getTypeFromValue(new Integer(1));
        assertEquals(HistoryEventType.storageDifference, type);
        type = HistoryEventType.getTypeFromValue(new Integer(999));
        assertEquals(HistoryEventType.unknown, type);

    }

}
