package de.sub.goobi.metadaten.search;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EasydbResponseObject {

    private Map<String, String> metadata = new LinkedHashMap<>();
    private int _system_object_id;
    private String _created;
    private String _last_modified;
    private String _uuid;
    private List<Object> _path;
    private int _published_count;
    private String _mask;
    private String _objecttype;
    private boolean _has_children;
    private String _global_object_id;
    private Object _standard;
    private String _format;
    private List<String> _collections;
    private int _level;
    private double _score;

    /**
     * Convert the submap of the json object into the pojo object
     * 
     * @param objectMap
     */

    public EasydbResponseObject(Map<String, Object> objectMap) {
        Object value = objectMap.get("_created");
        if (value != null) {
            _created = (String) value;
        }
        value = objectMap.get("_mask");
        if (value != null) {
            _mask = (String) value;
        }
        value = objectMap.get("_last_modified");
        if (value != null) {
            _last_modified = (String) value;
        }
        value = objectMap.get("_system_object_id");
        if (value != null) {
            _system_object_id = (int) value;
        }
        value = objectMap.get("_objecttype");
        if (value != null) {
            _objecttype = (String) value;
        }
        value = objectMap.get("_uuid");
        if (value != null) {
            _uuid = (String) value;
        }
        value = objectMap.get("_global_object_id");
        if (value != null) {
            _global_object_id = (String) value;
        }
        value = objectMap.get("_format");
        if (value != null) {
            _format = (String) value;
        }

        value = objectMap.get("_has_children");
        if (value != null) {
            _has_children = (boolean) value;
        }
        value = objectMap.get("_score");
        if (value != null) {
            _score = (double) value;
        }
        metadata.put("_uuid", _uuid);
        metadata.put("_global_object_id", _global_object_id);
        metadata.put("_objecttype", _objecttype);
        metadata.put("_system_object_id", "" + _system_object_id);

        @SuppressWarnings("unchecked")
        Map<String, Object> objecttypeMetadata = (Map<String, Object>) objectMap.get(_objecttype);
        readObjectValues(_objecttype + ".", objecttypeMetadata);
    }

    /**
     * fill the metadata map with the content of the hierarchical object
     * 
     * @param prefix
     * @param objectValue
     */

    @SuppressWarnings("unchecked")
    private void readObjectValues(String prefix, Map<String, Object> objectValue) {
        for (Map.Entry<String, Object> entry : objectValue.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                if (value instanceof LinkedHashMap) {
                    readObjectValues(prefix + key + ".", (LinkedHashMap<String, Object>) value);
                } else if (value instanceof String) {
                    metadata.put(prefix + key, (String) value);
                } else if (value instanceof Integer) {
                    metadata.put(prefix + key, String.valueOf((value)));
                } else if (value instanceof Boolean) {
                    metadata.put(prefix + key, String.valueOf((value)));
                } else if (value instanceof Double) {
                    metadata.put(prefix + key, String.valueOf((value)));
                } else {
                    // TODO java.util.ArrayList ?
                }
            }
        }
    }
}