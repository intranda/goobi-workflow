package de.sub.goobi.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi-workflow
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
        this.children = new ArrayList<TreeNode>();
    }

    public TreeNode(boolean expanded, String label, String id) {
        this.expanded = expanded;
        this.label = label;
        this.id = id;
        this.children = new ArrayList<TreeNode>();
    }

    public void addChild(TreeNode inNode) {
        this.children.add(inNode);
    }

    public List<TreeNode> getChildrenAsList() {
        List<TreeNode> myList = new ArrayList<TreeNode>();
        getChildrenAsListMitStrichen(myList, 0, this, true, true, new ArrayList<TreeNode>());
        return myList;
    }

    public List<TreeNode> getChildrenAsListAlle() {
        List<TreeNode> myList = new ArrayList<TreeNode>();
        getChildrenAsListAlle(myList, 0, this, true, true, new ArrayList<TreeNode>());
        return myList;
    }

    /**
     * alle Children des übergebenen Knotens expanden oder collapsen ================================================================
     */
    public void expandNodes(Boolean inExpand) {
        expandNode(this, inExpand.booleanValue());
    }

    private void expandNode(TreeNode inNode, boolean inExpand) {
        inNode.expanded = inExpand;
        for (Iterator<TreeNode> iter = inNode.children.iterator(); iter.hasNext();) {
            TreeNode t = iter.next();
            expandNode(t, inExpand);
        }
    }

    @SuppressWarnings({ "unused", "unchecked", "rawtypes" })
    private List getChildrenAsList(List inList, int niveau, List inStriche, boolean vaterIstLetzter) {
        for (Iterator<TreeNode> it = this.children.iterator(); it.hasNext();) {
            TreeNode kind = it.next();
            HashMap map = new HashMap();
            map.put("node", kind);
            map.put("niveau", Integer.valueOf(niveau));
            map.put("islast", Boolean.valueOf(!it.hasNext()));

            //       die Striche vorbereiten
            List striche = new ArrayList(inStriche);
            striche.add(Boolean.valueOf(vaterIstLetzter));
            map.put("striche", striche);

            inList.add(map);
            if (kind.expanded && kind.getHasChildren()) {
                kind.getChildrenAsList(inList, niveau + 1, striche, !it.hasNext());
            }
        }
        return inList;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List getChildrenAsListMitStrichen(List inList, int niveau, TreeNode inNode, boolean istLetzter, boolean vaterIstLetzter, List inStriche) {

        HashMap map = new HashMap();
        map.put("node", inNode);
        map.put("niveau", Integer.valueOf(niveau));
        map.put("islast", Boolean.valueOf(istLetzter));

        // die Striche vorbereiten
        List striche = new ArrayList(inStriche);
        striche.add(Boolean.valueOf(vaterIstLetzter));
        map.put("striche", striche);

        inList.add(map);

        if (inNode.getHasChildren() && inNode.expanded) {
            for (Iterator it = inNode.getChildren().iterator(); it.hasNext();) {
                TreeNode kind = (TreeNode) it.next();
                getChildrenAsListMitStrichen(inList, niveau + 1, kind, !it.hasNext(), istLetzter, striche);
            }
        }

        return inList;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List getChildrenAsListAlle(List inList, int niveau, TreeNode inNode, boolean istLetzter, boolean vaterIstLetzter, List inStriche) {

        HashMap map = new HashMap();
        map.put("node", inNode);
        map.put("niveau", Integer.valueOf(niveau));
        map.put("islast", Boolean.valueOf(istLetzter));

        // die Striche vorbereiten
        List striche = new ArrayList(inStriche);
        striche.add(Boolean.valueOf(vaterIstLetzter));
        map.put("striche", striche);

        inList.add(map);

        if (inNode.getHasChildren()) {
            for (Iterator it = inNode.getChildren().iterator(); it.hasNext();) {
                TreeNode kind = (TreeNode) it.next();
                getChildrenAsListAlle(inList, niveau + 1, kind, !it.hasNext(), istLetzter, striche);
            }
        }

        return inList;
    }

    /*                                       
    * Getter und Setter                         
    */

    public boolean getHasChildren() {
        return this.children != null && !this.children.isEmpty();
    }

}
