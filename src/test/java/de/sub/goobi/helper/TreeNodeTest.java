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
package de.sub.goobi.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;

public class TreeNodeTest {

    @Test
    public void testDefaultConstructor() {
        TreeNode node = new TreeNode();
        assertFalse(node.isExpanded());
        assertFalse(node.isSelected());
        assertNull(node.getLabel());
        assertNull(node.getId());
        assertNotNull(node.getChildren());
        assertTrue(node.getChildren().isEmpty());
    }

    @Test
    public void testParameterizedConstructor() {
        TreeNode node = new TreeNode(true, "Root", "root-id");
        assertTrue(node.isExpanded());
        assertEquals("Root", node.getLabel());
        assertEquals("root-id", node.getId());
        assertNotNull(node.getChildren());
        assertTrue(node.getChildren().isEmpty());
    }

    @Test
    public void testSetters() {
        TreeNode node = new TreeNode();
        node.setExpanded(true);
        node.setSelected(true);
        node.setLabel("Test Label");
        node.setId("test-id");
        assertTrue(node.isExpanded());
        assertTrue(node.isSelected());
        assertEquals("Test Label", node.getLabel());
        assertEquals("test-id", node.getId());
    }

    @Test
    public void testAddChild() {
        TreeNode parent = new TreeNode();
        TreeNode child = new TreeNode(false, "Child", "child-id");
        parent.addChild(child);
        assertEquals(1, parent.getChildren().size());
        assertEquals("Child", parent.getChildren().get(0).getLabel());
    }

    @Test
    public void testGetHasChildrenFalseWhenEmpty() {
        TreeNode node = new TreeNode();
        assertFalse(node.getHasChildren());
    }

    @Test
    public void testGetHasChildrenTrueAfterAddChild() {
        TreeNode parent = new TreeNode();
        parent.addChild(new TreeNode());
        assertTrue(parent.getHasChildren());
    }

    @Test
    public void testGetChildrenAsListWithNoChildren() {
        TreeNode root = new TreeNode(true, "Root", "root");
        List<HashMap<String, Object>> list = root.getChildrenAsList();
        // root itself is added as the first entry
        assertEquals(1, list.size());
        assertEquals(root, list.get(0).get("node"));
    }

    @Test
    public void testGetChildrenAsListWithExpandedChild() {
        TreeNode root = new TreeNode(true, "Root", "root");
        TreeNode child = new TreeNode(false, "Child", "child");
        root.addChild(child);
        List<HashMap<String, Object>> list = root.getChildrenAsList();
        // root + child (root is expanded, child is not expanded so its children not added)
        assertEquals(2, list.size());
    }

    @Test
    public void testGetChildrenAsListAlleIncludesAll() {
        TreeNode root = new TreeNode(false, "Root", "root");
        TreeNode child = new TreeNode(false, "Child", "child");
        TreeNode grandchild = new TreeNode(false, "Grandchild", "grandchild");
        child.addChild(grandchild);
        root.addChild(child);
        // getAlle includes even unexpanded nodes
        List<HashMap<String, Object>> list = root.getChildrenAsListAlle();
        assertEquals(3, list.size());
    }

    @Test
    public void testExpandNodes() {
        TreeNode root = new TreeNode(false, "Root", "root");
        TreeNode child = new TreeNode(false, "Child", "child");
        root.addChild(child);
        root.expandNodes(Boolean.TRUE);
        assertTrue(root.isExpanded());
        assertTrue(child.isExpanded());
    }

    @Test
    public void testCollapseNodes() {
        TreeNode root = new TreeNode(true, "Root", "root");
        TreeNode child = new TreeNode(true, "Child", "child");
        root.addChild(child);
        root.expandNodes(Boolean.FALSE);
        assertFalse(root.isExpanded());
        assertFalse(child.isExpanded());
    }
}
