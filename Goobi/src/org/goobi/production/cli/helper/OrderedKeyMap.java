package org.goobi.production.cli.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
import lombok.Getter;

/**
 * This class extends the {@link HashMap} with ordered keys. The keys are hold in an {@link ArrayList}
 * 
 * @author Robert Sehr
 *
 * @param <K> type of the key objects
 * @param <V> type of the value objects
 */

public class OrderedKeyMap<K, V> extends HashMap<K, V> {

    private static final long serialVersionUID = -4826644528952614394L;

    @Getter
    private List<K> keyList = new ArrayList<>();

    @Override
    public V put(K key, V value) {
        if (!keyList.contains(key)) {
            keyList.add(key);
        }
        return super.put(key, value);
    }

    public int getIndexPosition(K key) {
        return keyList.indexOf(key);
    }

    @Override
    public void clear() {
        keyList.clear();
        super.clear();
    }

    @Override
    public Set<K> keySet() {
        return new LinkedHashSet<K>(keyList);
    }

    @Override
    public V remove(Object key) {
        keyList.remove(key);
        return super.remove(key);
    }
}
