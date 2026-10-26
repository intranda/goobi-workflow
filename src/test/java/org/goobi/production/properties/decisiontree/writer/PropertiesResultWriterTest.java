package org.goobi.production.properties.decisiontree.writer;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class PropertiesResultWriterTest {

    /** Fake for the properties of one process. */
    private static class FakeStore implements PropertyStore {
        private final Map<String, String> values = new LinkedHashMap<>();
        private int deletes;

        @Override
        public Map<String, String> read(Set<String> names) {
            Map<String, String> result = new LinkedHashMap<>();
            values.forEach((k, v) -> {
                if (names.contains(k)) {
                    result.put(k, v);
                }
            });
            return result;
        }

        @Override
        public void save(String name, String value) {
            values.put(name, value);
        }

        @Override
        public void delete(String name) {
            deletes++;
            values.remove(name);
        }
    }

    private static final Set<String> OWNED = Set.of("Scanner", "Bit Depth", "DPI");

    @Test
    public void writeSetsPresentFieldsAndDeletesTheOthers() {
        FakeStore store = new FakeStore();
        store.values.put("DPI", "old");
        store.values.put("Unrelated", "keep");
        new PropertiesResultWriter(store, Map.of()).write(Map.of("Scanner", "A", "Bit Depth", "16"), OWNED);
        assertEquals(Map.of("Scanner", "A", "Bit Depth", "16", "Unrelated", "keep"), store.values);
    }

    @Test
    public void valuesAreTrimmedAndBlankMeansDelete() {
        FakeStore store = new FakeStore();
        store.values.put("DPI", "old");
        new PropertiesResultWriter(store, Map.of()).write(Map.of("Scanner", "  A  ", "DPI", "   "), OWNED);
        assertEquals(Map.of("Scanner", "A"), store.values);
    }

    @Test
    public void mappedFieldsUseTheTargetNameInTheStoreAndTheLogicalNameOutside() {
        FakeStore store = new FakeStore();
        PropertiesResultWriter writer = new PropertiesResultWriter(store, Map.of("Bit Depth", "BitDepth"));
        writer.write(Map.of("Bit Depth", "16", "Scanner", "A"), OWNED);
        assertEquals(Map.of("BitDepth", "16", "Scanner", "A"), store.values);
        assertEquals(Map.of("Bit Depth", "16", "Scanner", "A"), writer.read(OWNED));
    }

    @Test
    public void readReturnsOnlyOwnedFieldsThatAreSet() {
        FakeStore store = new FakeStore();
        store.values.put("Scanner", "A");
        store.values.put("Unrelated", "x");
        assertEquals(Map.of("Scanner", "A"), new PropertiesResultWriter(store, Map.of()).read(OWNED));
    }

    @Test
    public void clearDeletesEveryOwnedFieldOnly() {
        FakeStore store = new FakeStore();
        store.values.putAll(Map.of("Scanner", "A", "DPI", "1", "Unrelated", "keep"));
        new PropertiesResultWriter(store, Map.of("DPI", "Dpi")).clear(OWNED);
        assertEquals(Map.of("DPI", "1", "Unrelated", "keep"), store.values, "DPI is stored as Dpi, so the unmapped name is not owned here");
        assertTrue(store.deletes >= 3);
    }

    @Test
    public void writeThenReadGivesTheSameFields() {
        FakeStore store = new FakeStore();
        PropertiesResultWriter writer = new PropertiesResultWriter(store, Map.of("Bit Depth", "BitDepth"));
        Map<String, String> fields = Map.of("Scanner", "A", "Bit Depth", "16", "DPI", "≥400 dpi");
        writer.write(fields, OWNED);
        assertEquals(fields, writer.read(OWNED));
    }
}
