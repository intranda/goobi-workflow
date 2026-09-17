package de.sub.goobi.metadaten;

/***************************************************************
 * Copyright notice
 *
 * (c) 2013 Robert Sehr <robert.sehr@intranda.com>
 *
 * All rights reserved
 *
 * This file is part of the Goobi project. The Goobi project is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * The GNU General Public License can be found at
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * This script is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * This copyright notice MUST APPEAR in all copies of this file!
 ***************************************************************/

import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;

import ugh.dl.MetadataGroup;

public class MetadataGroupIdRegistry implements Serializable {

    private static final long serialVersionUID = 6204705143229216823L;

    private static final String ID_PREFIX = "g";

    private final Map<MetadataGroup, String> idByGroup = new IdentityHashMap<>();

    private int counter = 0;

    public String idFor(MetadataGroup group) {
        if (group == null) {
            return null;
        }
        return idByGroup.computeIfAbsent(group, g -> ID_PREFIX + (++counter));
    }

    public void clear() {
        idByGroup.clear();
        counter = 0;
    }
}
