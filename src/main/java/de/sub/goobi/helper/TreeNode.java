package de.sub.goobi.helper;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class TreeNode {
    @Getter
    @Setter
    protected boolean expanded = false;
    @Getter
    @Setter
    protected boolean selected = false;
    @Getter
    @Setter
    protected String label;
    @Getter
    @Setter
    protected String id;
    @Getter
    @Setter
    protected List<TreeNode> children;

    public TreeNode() {
        this.children = new ArrayList<>();
    }

    public TreeNode(boolean expanded, String label, String id) {
        this.expanded = expanded;
        this.label = label;
        this.id = id;
        this.children = new ArrayList<>();
    }

    public void addChild(TreeNode node) {
        this.children.add(node);
    }

    public List<HashMap<String, Object>> getChildrenAsList() {
        List<HashMap<String, Object>> nodes = new ArrayList<>();
        getChildrenAsListMitStrichen(nodes, 0, this, true, true, new ArrayList<>());
        return nodes;
    }

    public List<HashMap<String, Object>> getChildrenAsListAlle() {
        List<HashMap<String, Object>> nodes = new ArrayList<>();
        getChildrenAsListAlle(nodes, 0, this, true, true, new ArrayList<>());
        return nodes;
    }

    /**
     * Collapses or expands all child nodes of this child node, depending on the expand parameter.
     *
     * @param expand Must be true to expand the node and false to collapse them
     */
    public void expandNodes(Boolean expand) {
        expandNode(this, expand.booleanValue());
    }

    /**
     * Collapses or expands all child nodes of the given node, depending on the expand parameter.
     *
     * @param node The tree node to expand or collapse the child nodes for
     * @param expand Must be true to expand the node and false to collapse them
     */
    private void expandNode(TreeNode node, boolean expand) {
        node.expanded = expand;
        for (Iterator<TreeNode> iterator = node.children.iterator(); iterator.hasNext();) {
            TreeNode next = iterator.next();
            expandNode(next, expand);
        }
    }

    @SuppressWarnings({ "unused" })
    private List<HashMap<String, Object>> getChildrenAsList(List<HashMap<String, Object>> nodes, int niveau, List<Boolean> lines,
            boolean parentIsLast) {
        for (Iterator<TreeNode> iterator = this.children.iterator(); iterator.hasNext();) {
            TreeNode node = iterator.next();

            HashMap<String, Object> map = TreeNode.createHashMap(node, niveau, !iterator.hasNext(), lines, parentIsLast);
            nodes.add(map);

            if (node.expanded && node.getHasChildren()) {
                node.getChildrenAsList(nodes, niveau + 1, lines, !iterator.hasNext());
            }
        }
        return nodes;
    }

    private List<HashMap<String, Object>> getChildrenAsListMitStrichen(List<HashMap<String, Object>> nodes, int niveau, TreeNode node, boolean isLast,
            boolean parentIsLast, List<Boolean> lines) {

        HashMap<String, Object> map = TreeNode.createHashMap(node, niveau, isLast, lines, parentIsLast);
        nodes.add(map);

        if (node.getHasChildren() && node.expanded) {
            for (Iterator<TreeNode> iterator = node.getChildren().iterator(); iterator.hasNext();) {
                TreeNode child = iterator.next();
                getChildrenAsListMitStrichen(nodes, niveau + 1, child, !iterator.hasNext(), isLast, lines);
            }
        }

        return nodes;
    }

    private List<HashMap<String, Object>> getChildrenAsListAlle(List<HashMap<String, Object>> nodes, int niveau, TreeNode node, boolean isLast,
            boolean parentIsLast, List<Boolean> lines) {

        HashMap<String, Object> map = TreeNode.createHashMap(node, niveau, isLast, lines, parentIsLast);
        nodes.add(map);

        if (node.getHasChildren()) {
            for (Iterator<TreeNode> iterator = node.getChildren().iterator(); iterator.hasNext();) {
                TreeNode child = iterator.next();
                getChildrenAsListAlle(nodes, niveau + 1, child, !iterator.hasNext(), isLast, lines);
            }
        }

        return nodes;
    }

    private static HashMap<String, Object> createHashMap(TreeNode node, int niveau, boolean isLast, List<Boolean> lines, boolean parentIsLast) {

        HashMap<String, Object> map = new HashMap<>();

        map.put("node", node);
        map.put("niveau", Integer.valueOf(niveau));
        map.put("islast", Boolean.valueOf(isLast));

        // prepare displayed lines
        List<Boolean> newLines = new ArrayList<>(lines);
        newLines.add(Boolean.valueOf(parentIsLast));
        map.put("striche", newLines);

        return map;
    }

    /*
     * Getter und Setter
     */

    public boolean getHasChildren() {
        return this.children != null && !this.children.isEmpty();
    }

}
