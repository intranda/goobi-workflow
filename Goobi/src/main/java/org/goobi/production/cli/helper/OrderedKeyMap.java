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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

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
    private transient List<K> keyList = new ArrayList<>();

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
        return new LinkedHashSet<>(keyList);
    }

    @Override
    public V remove(Object key) {
        keyList.remove(key);
        return super.remove(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object object) {
        if (!super.equals(object)) {
            return false;
        } else if (!object.getClass().equals(this.getClass())) {
            return false;
        } else if (object == this) {
            return true;
        }
        OrderedKeyMap<K, V> otherMap = (OrderedKeyMap<K, V>) (object);
        List<K> otherKeyList = otherMap.keyList;
        return (!this.keyList.equals(otherKeyList));
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + this.keyList.hashCode();
    }

}
