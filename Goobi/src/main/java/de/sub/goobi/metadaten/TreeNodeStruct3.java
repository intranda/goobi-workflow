package de.sub.goobi.metadaten;

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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.goobi.production.cli.helper.StringPair;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.TreeNode;
import lombok.Getter;
import lombok.Setter;
import ugh.dl.DocStruct;

public class TreeNodeStruct3 extends TreeNode {

    @Getter
    @Setter
    private DocStruct struct;

    private List<StringPair> displayableMetadata = new ArrayList<>();
    @Getter
    @Setter
    private Pair<String, String> firstImage;
    @Getter
    @Setter
    private Pair<String, String> lastImage;
    @Setter
    private String mainTitle;

    @Getter
    @Setter
    private boolean einfuegenErlaubt = true;

    @Getter
    @Setter
    private boolean validationErrorPresent;
    @Getter
    @Setter
    private String validationMessage;

    /**
     * Konstruktoren
     */
    public TreeNodeStruct3() {
    }

    /* =============================================================== */

    public TreeNodeStruct3(boolean expanded, String label, String id) {
        this.expanded = expanded;
        this.label = label;
        this.id = id;
        this.children = new ArrayList<>();
    }

    /* =============================================================== */

    public TreeNodeStruct3(String label, DocStruct struct) {
        this.label = label;
        this.struct = struct;
    }

    public void addMetadata(String label, String value) {
        if (StringUtils.isNotBlank(label) && StringUtils.isNotBlank(value)) {
            displayableMetadata.add(new StringPair(label, value));
        }
    }

    public String getMainTitle() {

        int maxSize = ConfigurationHelper.getInstance().getMetsEditorMaxTitleLength();
        if (maxSize > 0 && this.mainTitle != null && this.mainTitle.length() > maxSize) {
            return this.mainTitle.substring(0, maxSize - 1);
        }

        return this.mainTitle;
    }

    public String getDescription() {
        return this.label;
    }

    public void setDescription(String description) {
        this.label = description;
    }

    public String getMetadataPopup() {
        StringBuilder answer = new StringBuilder();

        answer.append("<ul class=\"table__structure-popover-ul\">");
        for (StringPair sp : displayableMetadata) {
            answer.append("<li>");
            answer.append(sp.getOne());
            answer.append("</li><li>");
            answer.append(sp.getTwo());
            answer.append("</li>");
        }
        if (validationErrorPresent) {
            answer.append("<li class=\"table__stuct-metadata-error\">");
            answer.append("<i class=\"fa fa-exclamation-circle\"></i>");
            answer.append(validationMessage);
            answer.append("</li>");
        }
        answer.append("</ul>");

        return answer.toString();
    }

}
