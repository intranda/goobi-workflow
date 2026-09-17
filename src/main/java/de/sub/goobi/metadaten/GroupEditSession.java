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

import lombok.Getter;
import lombok.Setter;
import ugh.dl.MetadataGroup;

public class GroupEditSession implements Serializable {

    private static final long serialVersionUID = -5202277744654943081L;

    private final transient Metadaten bean;

    @Getter
    private final transient MetadataGroup anchor;

    private transient MetadataGroupImpl root;

    @Getter
    @Setter
    private transient MetadataGroupImpl actionTarget;
    @Getter
    @Setter
    private transient String actionTypeName;

    GroupEditSession(Metadaten bean, MetadataGroup anchor) {
        this.bean = bean;
        this.anchor = anchor;
    }

    public boolean isOrphaned() {
        return anchor == null || anchor.getParent() == null;
    }

    public MetadataGroupImpl getRoot() {
        if (root == null && !isOrphaned()) {
            refresh();
        }
        return root;
    }

    public String getTitle() {
        MetadataGroupImpl current = getRoot();
        return current == null ? null : current.getName();
    }

    void refresh() {
        root = isOrphaned() ? null
                : new MetadataGroupImpl(bean.getMyPrefs(), bean.getMyProzess(), anchor, bean, bean.getGroupIdRegistry(), null, 0);
    }

    public String addMetadata() {
        return bean.addEmptyMetadata(actionTarget, actionTypeName);
    }

    public String addPerson() {
        return bean.addEmptyPerson(actionTarget, actionTypeName);
    }

    public String addCorporate() {
        return bean.addEmptyCorporate(actionTarget, actionTypeName);
    }

    public String addGroup() {
        return bean.addEmptyGroup(actionTarget, actionTypeName);
    }
}
