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

package io.goobi.workflow.ruleseteditor;

import lombok.Getter;

/**
 * The ways to create a ruleset in the create dialog of the ruleset editor, with the icon and title the dialog shows for each.
 */
@Getter
public enum RulesetCreateMode {

    // an empty ruleset with a small valid skeleton
    EMPTY("plus", "ruleset_create_empty"),
    // an uploaded file
    UPLOAD("upload", "ruleset_create_upload"),
    // a file in the ruleset folder that no ruleset uses yet
    FILE("file-plus", "ruleset_create_from_file"),
    // a copy of another ruleset
    DUPLICATE("clipboard", "ruleset_create_duplicate");

    private final String icon;
    private final String titleKey;

    RulesetCreateMode(String icon, String titleKey) {
        this.icon = icon;
        this.titleKey = titleKey;
    }
}
