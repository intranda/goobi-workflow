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
package org.goobi.managedbeans;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Bean for managing keyboard shortcuts display in the UI
 */
@Named("ShortcutsBean")
@RequestScoped
public class ShortcutsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private LoginBean loginBean;

    /**
     * Get the current user's shortcut prefix
     *
     * @return The shortcut prefix or empty string if not available
     */
    public String getUserShortcutPrefix() {
        if (loginBean != null && loginBean.getMyBenutzer() != null) {
            return loginBean.getMyBenutzer().getShortcutPrefix();
        }
        return "";
    }

    /**
     * Get the formatted shortcut prefix (split if contains " + ")
     *
     * @return Array with [firstPart, secondPart] or [fullPrefix, ""] if no split
     */
    public String[] getUserShortcutPrefixParts() {
        String prefix = getUserShortcutPrefix();
        if (prefix != null && prefix.contains("+")) {
            return prefix.split("\\+");
        }
        return new String[]{prefix != null ? prefix : "", ""};
    }

    /**
     * Get the first part of the user's shortcut prefix
     */
    public String getUserShortcutFirstPart() {
        return getUserShortcutPrefixParts()[0];
    }

    /**
     * Get the second part of the user's shortcut prefix
     */
    public String getUserShortcutSecondPart() {
        return getUserShortcutPrefixParts()[1];
    }

    /**
     * Get keyboard shortcuts as a map with icon names as keys and labels as values
     *
     * @return Map of shortcuts where key=icon name, value=display label
     */
    public Map<String, String> getShortcutKeys() {
        Map<String, String> shortcuts = new LinkedHashMap<>();
        shortcuts.put("arrow-up", "shortcut_imagePrev20");
        shortcuts.put("arrow-down", "shortcut_imageNext20");
        shortcuts.put("arrow-left", "shortcut_imagePrev1");
        shortcuts.put("arrow-right", "shortcut_imageNext1");
        shortcuts.put("pos1", "shortcut_imageFirst");
        shortcuts.put("end", "shortcut_imageLast");
        shortcuts.put("space", "shortcut_toggleCheckboxPagination");
        shortcuts.put("enter", "shortcut_saveMets");
        shortcuts.put("v", "shortcut_validate");
        return shortcuts;
    }
}
