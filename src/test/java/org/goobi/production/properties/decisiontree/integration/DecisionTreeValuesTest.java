package org.goobi.production.properties.decisiontree.integration;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.production.properties.decisiontree.binding.BindingConfig;
import org.goobi.production.properties.decisiontree.binding.TreeRegistry;
import org.goobi.production.properties.decisiontree.engine.TreeEngine;
import org.goobi.production.properties.decisiontree.engine.TreeEngine.LeafPath;
import org.goobi.production.properties.decisiontree.model.TreeDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DecisionTreeValuesTest {

    private static final String SAMPLE = "/org/goobi/production/properties/decisiontree/goobi_decisionTrees.xml";

    @TempDir
    Path dir;

    private DecisionTreeValues values;
    private TreeDefinition tree;
    private final TreeEngine engine = new TreeEngine();

    @BeforeEach
    public void load() throws Exception {
        Path file = dir.resolve("goobi_decisionTrees.xml");
        try (InputStream in = getClass().getResourceAsStream(SAMPLE)) {
            Files.write(file, in.readAllBytes());
        }
        TreeRegistry registry = new TreeRegistry(file);
        values = new DecisionTreeValues(registry);
        tree = registry.tree("scan-profile").orElseThrow();
    }

    private static GoobiProperty property(String name, String value, String container) {
        GoobiProperty property = new GoobiProperty(PropertyOwnerType.PROCESS);
        property.setPropertyName(name);
        property.setPropertyValue(value);
        property.setContainer(container);
        return property;
    }

    private static List<GoobiProperty> stored(Map<String, String> fields, Map<String, String> fieldMap) {
        List<GoobiProperty> properties = new ArrayList<>();
        fields.forEach((name, value) -> properties.add(property(fieldMap.getOrDefault(name, name), value, "0")));
        return properties;
    }

    private static BindingConfig binding(String treeId, Map<String, String> fieldMap) {
        return new BindingConfig(treeId, null, null, fieldMap);
    }

    @Test
    public void nothingStoredGivesAnEmptyValue() {
        assertEquals("", values.derive(binding("scan-profile", Map.of()), List.of()));
    }

    @Test
    public void everyResultIsFoundFromItsStoredFields() {
        for (LeafPath leaf : engine.allPaths(tree)) {
            String value = values.derive(binding("scan-profile", Map.of()), stored(leaf.fields(), Map.of()));
            assertTrue(List.of(value.split(", ")).contains(leaf.result().id()), leaf.result().id() + " not in '" + value + "'");
        }
    }

    @Test
    public void mappedFieldNamesAreRead() {
        Map<String, String> map = Map.of("Bit Depth", "BitDepth");
        for (LeafPath leaf : engine.allPaths(tree)) {
            String value = values.derive(binding("scan-profile", map), stored(leaf.fields(), map));
            assertTrue(List.of(value.split(", ")).contains(leaf.result().id()), leaf.result().id());
        }
        // stored under the logical name although the binding maps it: the field is missing, so nothing fits
        LeafPath first = engine.allPaths(tree).get(0);
        assertEquals("", values.derive(binding("scan-profile", map), stored(first.fields(), Map.of())));
    }

    @Test
    public void severalResultsThatFitAreListed() {
        // BND-COLOUR and REF-COLOUR give identical fields
        LeafPath bound = engine.allPaths(tree).stream().filter(l -> "BND-COLOUR".equals(l.result().id())).findFirst().orElseThrow();
        String value = values.derive(binding("scan-profile", Map.of()), stored(bound.fields(), Map.of()));
        assertEquals("BND-COLOUR, REF-COLOUR", value);
    }

    @Test
    public void fieldsThatFitNoResultGiveAnEmptyValue() {
        Map<String, String> fields = new LinkedHashMap<>(engine.allPaths(tree).get(0).fields());
        fields.put("DPI", "by hand");
        assertEquals("", values.derive(binding("scan-profile", Map.of()), stored(fields, Map.of())));
    }

    @Test
    public void propertiesInOtherContainersAreIgnored() {
        List<GoobiProperty> properties = new ArrayList<>();
        engine.allPaths(tree).get(0).fields().forEach((name, value) -> properties.add(property(name, value, "1")));
        assertEquals("", values.derive(binding("scan-profile", Map.of()), properties));
    }

    @Test
    public void unknownTreeGivesAnEmptyValue() {
        LeafPath first = engine.allPaths(tree).get(0);
        assertEquals("", values.derive(binding("no-such-tree", Map.of()), stored(first.fields(), Map.of())));
    }

    @Test
    public void listStoreIsReadOnly() {
        ListPropertyStore store = new ListPropertyStore(List.of());
        assertThrows(UnsupportedOperationException.class, () -> store.save("a", "b"));
        assertThrows(UnsupportedOperationException.class, () -> store.delete("a"));
    }
}
