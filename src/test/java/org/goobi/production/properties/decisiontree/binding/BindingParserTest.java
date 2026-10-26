package org.goobi.production.properties.decisiontree.binding;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - https://goobi.io - https://www.intranda.com - https://github.com/intranda/goobi-workflow
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.junit.jupiter.api.Test;

public class BindingParserTest {

    private XMLConfiguration config(String property) throws Exception {
        XMLConfiguration config = new XMLConfiguration();
        config.setDelimiterParsingDisabled(true);
        config.load(new StringReader("<config_processProperties>" + property + "</config_processProperties>"));
        config.setExpressionEngine(new XPathExpressionEngine());
        return config;
    }

    private BindingConfig parse(String property) throws Exception {
        return BindingParser.parse(config(property), "/property[1]");
    }

    @Test
    public void minimalBinding() throws Exception {
        BindingConfig binding = parse("<property name=\"Scan Profile\"><type>decisiontree</type><tree>scan-profile</tree></property>");
        assertEquals("scan-profile", binding.treeId());
        assertNull(binding.resultIdProperty());
        assertNull(binding.pathProperty());
        assertTrue(binding.fieldMap().isEmpty());
        assertEquals("Bit Depth", binding.targetName("Bit Depth"));
    }

    @Test
    public void fullBinding() throws Exception {
        BindingConfig binding = parse("<property name=\"Scan Profile\"><type>decisiontree</type><tree> scan-profile </tree>"
                + "<resultIdProperty>Scan Profile Id</resultIdProperty><pathProperty>Scan Profile Path</pathProperty>"
                + "<map field=\"Bit Depth\" to=\"BitDepth\"/><map field=\"DPI\" to=\"Dpi\"/></property>");
        assertEquals("scan-profile", binding.treeId());
        assertEquals("Scan Profile Id", binding.resultIdProperty());
        assertEquals("Scan Profile Path", binding.pathProperty());
        assertEquals("BitDepth", binding.targetName("Bit Depth"));
        assertEquals("Scanner", binding.targetName("Scanner"));
        assertEquals(Set.of("BitDepth", "Dpi", "Scanner"), binding.targetNames(Set.of("Bit Depth", "DPI", "Scanner")));
    }

    @Test
    public void bindingIsReadFromTheRightProperty() throws Exception {
        XMLConfiguration config = config("<property name=\"A\"><type>text</type></property>"
                + "<property name=\"B\"><type>decisiontree</type><tree>second</tree></property>");
        assertEquals("second", BindingParser.parse(config, "/property[2]").treeId());
    }

    @Test
    public void missingTreeIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> parse("<property name=\"Scan Profile\"><type>decisiontree</type></property>"));
        assertTrue(e.getMessage().contains("Scan Profile"));
    }

    @Test
    public void incompleteMapIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> parse("<property name=\"P\"><tree>t</tree><map field=\"A\"/></property>"));
        assertThrows(IllegalArgumentException.class, () -> parse("<property name=\"P\"><tree>t</tree><map to=\"A\"/></property>"));
    }

    @Test
    public void doubleMappingIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> parse("<property name=\"P\"><tree>t</tree><map field=\"A\" to=\"X\"/><map field=\"A\" to=\"Y\"/></property>"));
        assertThrows(IllegalArgumentException.class,
                () -> parse("<property name=\"P\"><tree>t</tree><map field=\"A\" to=\"X\"/><map field=\"B\" to=\"X\"/></property>"));
    }

    @Test
    public void targetNamesWithBracketsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> parse("<property name=\"P\"><tree>t</tree><map field=\"A\" to=\"X)\"/></property>"));
        assertThrows(IllegalArgumentException.class, () -> parse("<property name=\"P\"><tree>t</tree><map field=\"A\" to=\"X}\"/></property>"));
    }

    @Test
    public void sameAuditPropertyTwiceIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> parse(
                "<property name=\"P\"><tree>t</tree><resultIdProperty>X</resultIdProperty><pathProperty>X</pathProperty></property>"));
    }

    @Test
    public void mapOfTheBindingIsUnmodifiable() throws Exception {
        BindingConfig binding = parse("<property name=\"P\"><tree>t</tree><map field=\"A\" to=\"B\"/></property>");
        assertThrows(UnsupportedOperationException.class, () -> binding.fieldMap().put("C", "D"));
        assertEquals(Map.of("A", "B"), binding.fieldMap());
    }
}
