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
package de.sub.goobi.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ShellScriptReturnValueTest {

    @Test
    public void testConstructorAndGetters() {
        ShellScriptReturnValue value = new ShellScriptReturnValue(0, "output text", "error text");
        assertNotNull(value);
        assertEquals(0, value.getReturnCode());
        assertEquals("output text", value.getOutputText());
        assertEquals("error text", value.getErrorText());
    }

    @Test
    public void testSetReturnCode() {
        ShellScriptReturnValue value = new ShellScriptReturnValue(0, null, null);
        value.setReturnCode(42);
        assertEquals(42, value.getReturnCode());
    }

    @Test
    public void testSetOutputText() {
        ShellScriptReturnValue value = new ShellScriptReturnValue(0, null, null);
        value.setOutputText("new output");
        assertEquals("new output", value.getOutputText());
    }

    @Test
    public void testSetErrorText() {
        ShellScriptReturnValue value = new ShellScriptReturnValue(0, null, null);
        value.setErrorText("new error");
        assertEquals("new error", value.getErrorText());
    }

    @Test
    public void testEqualsAndHashCode() {
        ShellScriptReturnValue v1 = new ShellScriptReturnValue(1, "out", "err");
        ShellScriptReturnValue v2 = new ShellScriptReturnValue(1, "out", "err");
        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    public void testNotEquals() {
        ShellScriptReturnValue v1 = new ShellScriptReturnValue(0, "out", "err");
        ShellScriptReturnValue v2 = new ShellScriptReturnValue(1, "out", "err");
        assertNotEquals(v1, v2);
    }

    @Test
    public void testToString() {
        ShellScriptReturnValue value = new ShellScriptReturnValue(0, "out", "err");
        assertNotNull(value.toString());
    }
}
