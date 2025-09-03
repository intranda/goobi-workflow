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
 */

package org.goobi.production.plugin.interfaces;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.DockAnchor;

public interface IGenericPlugin extends IPlugin {
    default String getId() {
        return getTitle().replaceAll("\\s+", "");
    }

    default String getIcon() {
        return null;
    }

    @Override
    default PluginType getType() {
        return PluginType.Generic;
    }

    default boolean isMenuBarDockable() {
        return isDockable(DockAnchor.MENU_BAR);
    }

    default boolean isFooterDockable() {
        return isDockable(DockAnchor.FOOTER);
    }

    boolean isDockable(DockAnchor anchor);

    default void initialize() throws Exception {
        // Nothing to do per default
    }

    default void execute() throws Exception {
        // Nothing to do per default
    }

    default String getModalPath() {
        return null;
    }
}
