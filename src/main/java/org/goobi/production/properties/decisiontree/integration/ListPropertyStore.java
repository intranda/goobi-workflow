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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.goobi.beans.GoobiProperty;
import org.goobi.production.properties.decisiontree.writer.PropertyStore;

/**
 * A read-only view on a list of properties that is already loaded (for example the properties of a process), in container 0. Writing is not
 * supported: the result is only read to derive the state of a tree.
 */
public class ListPropertyStore implements PropertyStore {

    private static final String CONTAINER = "0";

    private final List<GoobiProperty> properties;

    public ListPropertyStore(List<GoobiProperty> properties) {
        this.properties = properties;
    }

    @Override
    public Map<String, String> read(Set<String> names) {
        Map<String, String> result = new LinkedHashMap<>();
        for (GoobiProperty property : properties) {
            if (property.getPropertyName() != null && names.contains(property.getPropertyName()) && CONTAINER.equals(property.getContainer())) {
                result.putIfAbsent(property.getPropertyName(), property.getPropertyValue());
            }
        }
        return result;
    }

    @Override
    public void save(String name, String value) {
        throw new UnsupportedOperationException("read-only");
    }

    @Override
    public void delete(String name) {
        throw new UnsupportedOperationException("read-only");
    }
}
