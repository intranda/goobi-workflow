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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;

/**
 * Reads the binding of a property with the type {@code decisiontree}. One helper for all places that read properties, so the lookup is not copied.
 * The configuration must use the XPath expression engine, like the one of the property parser.
 */
public final class BindingParser {

    private BindingParser() {
    }

    /**
     * @param config the configuration of {@code goobi_processProperties.xml}
     * @param property xpath of the property, for example {@code /property[3]}
     * @return the binding
     * @throws IllegalArgumentException if the binding is not valid; the message says why
     */
    public static BindingConfig parse(HierarchicalConfiguration config, String property) {
        String name = config.getString(property + "/@name", "");
        String treeId = StringUtils.trimToNull(config.getString(property + "/tree"));
        if (treeId == null) {
            throw new IllegalArgumentException("The property '" + name + "' has the type decisiontree but no <tree>");
        }
        String resultIdProperty = StringUtils.trimToNull(config.getString(property + "/resultIdProperty"));
        String pathProperty = StringUtils.trimToNull(config.getString(property + "/pathProperty"));
        Map<String, String> fieldMap = new LinkedHashMap<>();
        int count = config.getMaxIndex(property + "/map");
        for (int i = 0; i <= count; i++) {
            String entry = property + "/map[" + (i + 1) + "]";
            String field = StringUtils.trimToNull(config.getString(entry + "/@field"));
            String to = StringUtils.trimToNull(config.getString(entry + "/@to"));
            if (field == null || to == null) {
                throw new IllegalArgumentException("The property '" + name + "' has a <map> without field or to");
            }
            if (fieldMap.put(field, to) != null) {
                throw new IllegalArgumentException("The property '" + name + "' maps the field '" + field + "' more than once");
            }
        }
        checkTargets(name, fieldMap, resultIdProperty, pathProperty);
        return new BindingConfig(treeId, resultIdProperty, pathProperty, fieldMap);
    }

    private static void checkTargets(String name, Map<String, String> fieldMap, String resultIdProperty, String pathProperty) {
        Map<String, String> seen = new HashMap<>();
        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            String to = entry.getValue();
            if (to.contains(")") || to.contains("}")) {
                throw new IllegalArgumentException(
                        "The property '" + name + "' maps '" + entry.getKey() + "' to '" + to + "', which must not contain ')' or '}'");
            }
            String other = seen.put(to, entry.getKey());
            if (other != null) {
                throw new IllegalArgumentException(
                        "The property '" + name + "' maps '" + other + "' and '" + entry.getKey() + "' to the same name '" + to + "'");
            }
        }
        if (resultIdProperty != null && resultIdProperty.equals(pathProperty)) {
            throw new IllegalArgumentException("The property '" + name + "' uses the same property for resultIdProperty and pathProperty");
        }
    }
}
