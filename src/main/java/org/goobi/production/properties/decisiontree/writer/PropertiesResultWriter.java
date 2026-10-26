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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Writes the result as plain properties, one per field. The tree knows the logical field names; the binding may map them to other property names.
 */
public class PropertiesResultWriter implements ResultWriter {

    private final PropertyStore store;
    private final Map<String, String> fieldMap;

    /**
     * @param store the properties of the object
     * @param fieldMap logical field name to property name; names that are not listed are used unchanged
     */
    public PropertiesResultWriter(PropertyStore store, Map<String, String> fieldMap) {
        this.store = store;
        this.fieldMap = Map.copyOf(fieldMap);
    }

    private String target(String field) {
        return fieldMap.getOrDefault(field, field);
    }

    @Override
    public Map<String, String> read(Set<String> owned) {
        Map<String, String> byTarget = new LinkedHashMap<>();
        owned.forEach(field -> byTarget.put(target(field), field));
        Map<String, String> stored = store.read(new LinkedHashSet<>(byTarget.keySet()));
        Map<String, String> result = new LinkedHashMap<>();
        for (String field : owned) {
            String value = stored.get(target(field));
            if (value != null) {
                result.put(field, value);
            }
        }
        return result;
    }

    @Override
    public void write(Map<String, String> fields, Set<String> owned) {
        for (String field : owned) {
            String value = fields.get(field);
            if (value == null || value.isBlank()) {
                store.delete(target(field));
            } else {
                store.save(target(field), value.trim());
            }
        }
    }

    @Override
    public void clear(Set<String> owned) {
        owned.forEach(field -> store.delete(target(field)));
    }
}
