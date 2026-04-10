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
 */
package io.goobi.workflow.harvester;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class HarvesterGoobiImportTest extends AbstractTest {

    @Test
    public void testAnnotationRetentionIsRuntime() {
        Retention retention = HarvesterGoobiImport.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    public void testAnnotationTargetIsType() {
        Target target = HarvesterGoobiImport.class.getAnnotation(Target.class);
        assertNotNull(target);
        ElementType[] values = target.value();
        assertEquals(1, values.length);
        assertEquals(ElementType.TYPE, values[0]);
    }

    @Test
    public void testAnnotationDefaultDescription() {
        @HarvesterGoobiImport
        class TestImplDefault {
        }

        HarvesterGoobiImport anno = TestImplDefault.class.getAnnotation(HarvesterGoobiImport.class);
        assertNotNull(anno);
        assertEquals("", anno.description());
    }

    @Test
    public void testAnnotationCustomDescription() {
        @HarvesterGoobiImport(description = "custom description")
        class TestImplCustom {
        }

        HarvesterGoobiImport anno = TestImplCustom.class.getAnnotation(HarvesterGoobiImport.class);
        assertNotNull(anno);
        assertEquals("custom description", anno.description());
    }

}
