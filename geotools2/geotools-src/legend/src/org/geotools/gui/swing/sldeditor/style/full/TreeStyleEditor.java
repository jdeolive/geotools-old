/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.gui.swing.sldeditor.style.full;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.gui.swing.sldeditor.util.SymbolizerUtils;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


/**
 * A component that shows the Style object as a tree depicting its composition hierachy
 *
 * @author wolf To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TreeStyleEditor extends JComponent implements SLDEditor {
    private boolean blockChangeEvents;
    JTree tree;
    Style style;
    FeatureType featureType;
    DefaultTreeModel model;

    public TreeStyleEditor(Style s, FeatureType featureType) {
        init();
        setStyle(s);
        this.featureType = featureType;
    }

    private DefaultMutableTreeNode getRootNode() {
        return (DefaultMutableTreeNode) tree.getModel().getRoot();
    }

    public void addFeatureTypeStyle(FeatureTypeStyle fts) {
        DefaultMutableTreeNode root = getRootNode();
        DefaultMutableTreeNode node = createFeatureTypeStyleNode(fts);
        model.insertNodeInto(node, root, 0);
        tree.setSelectionPath(new TreePath(node.getPath()));
    }

    /**
     * DOCUMENT ME!
     *
     * @param fts
     * @param r
     *
     * @return DOCUMENT ME!
     */
    public boolean addRule(FeatureTypeStyle fts, Rule r) {
        DefaultMutableTreeNode ftsNode = findNodeWithUserObject(getRootNode(), fts);

        if (ftsNode == null) {
            return false;
        }

        DefaultMutableTreeNode ruleNode = createRuleNode(r);
        model.insertNodeInto(ruleNode, ftsNode, 0);
        tree.setSelectionPath(new TreePath(ruleNode.getPath()));

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param rule
     * @param symbolizer
     *
     * @return DOCUMENT ME!
     */
    public boolean addSymbolizer(Rule rule, Symbolizer symbolizer) {
        DefaultMutableTreeNode ruleNode = findNodeWithUserObject(getRootNode(), rule);

        if (ruleNode == null) {
            return false;
        }

        DefaultMutableTreeNode symbolizerNode = createSymbolizerNode(symbolizer);
        model.insertNodeInto(symbolizerNode, ruleNode, 0);
        tree.setSelectionPath(new TreePath(symbolizerNode.getPath()));

        return true;
    }

    /**
     * Adds a <code>ChangeListener</code> to the style editor.
     *
     * @param cl the listener to be added
     */
    public void addChangeListener(ChangeListener cl) {
        listenerList.add(ChangeListener.class, cl);
    }

    /**
     * Notifies all listeners that have registered interest for notification on this event type.
     *
     * @param e the <code>ChangeEvent</code> to be fired;
     *
     * @see EventListenerList
     */
    protected void fireChange(ChangeEvent e) {
        if (blockChangeEvents) {
            return;
        }

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            // TreeSelectionEvent e = null;
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener) listeners[i + 1]).stateChanged(e);
            }
        }
    }

    /**
     * Returns the current selected object
     *
     * @return null if no selection, or a Style, FeatureTypeStyle, Rule, Symbolizer object
     */
    public Object getSelectionObject() {
        TreePath path = tree.getSelectionPath();

        if (path == null) {
            return null;
        } else {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

            return node.getUserObject();
        }
    }

    /**
     * Selectes the node containing the passed object, if any, or cleans the selection if null. If
     * not null and not found, no change is performed
     *
     * @param object either a Style, FeatureTypeStyle, Rule, a Symbolizer or null
     */
    public void setSelectionObject(Object object) {
        if (object == null) {
            tree.clearSelection();
        } else {
            TreePath path = findPathWithUserObject((DefaultMutableTreeNode) tree.getModel().getRoot(),
                    object);

            if (path != null) {
                tree.setSelectionPath(path);
            }
        }
    }

    /**
     * Recursively searches containment hierarchy of the style looking for the passed object
     *
     * @param object The user object to be found
     *
     * @return The parent object, or null if not found of if object is the style itself
     *
     * @throws NullPointerException if the argument is null
     */
    public Object findParentObject(Object object) {
        if (object == null) {
            throw new NullPointerException("Trying to search a null style component");
        }

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        DefaultMutableTreeNode node = findNodeWithUserObject(root, object);

        if (node != null) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();

            if (parent != null) {
                return parent.getUserObject();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param root
     * @param object
     *
     * @return
     */
    private DefaultMutableTreeNode findNodeWithUserObject(DefaultMutableTreeNode root, Object object) {
        TreePath tp = findPathWithUserObject(root, object);

        return (DefaultMutableTreeNode) tp.getLastPathComponent();
    }

    /**
     * Recursively searches the tree looking for a node having <code>object</code> as the user
     * object.
     *
     * @param node The node to start the search from
     * @param object The user object to be found
     *
     * @return The path to the node having the specified user object, or null if not found
     */
    private TreePath findPathWithUserObject(DefaultMutableTreeNode node, Object object) {
        if ((node.getUserObject() != null) && (node.getUserObject() == object)) {
            return new TreePath(node.getPath());
        } else {
            if (node.getChildCount() == 0) {
                return null;
            }

            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getFirstChild();

            while (child != null) {
                TreePath path = findPathWithUserObject(child, object);

                if (path != null) {
                    return path;
                } else {
                    child = child.getNextNode();
                }
            }

            return null;
        }
    }

    /**
     * Set the passed style into the control
     *
     * @param s
     */
    public void setStyle(Style s) {
        this.style = s;

        buildTree(s);
    }

    public Style getStyle() {
        DefaultMutableTreeNode root = getRootNode();
        
        int ftCount = root.getChildCount();
        FeatureTypeStyle[] featureStyles = new FeatureTypeStyle[ftCount];
        for(int i = 0; i < ftCount; i++) {
            DefaultMutableTreeNode ftsNode = (DefaultMutableTreeNode) root.getChildAt(i);
            featureStyles[i] = (FeatureTypeStyle) ftsNode.getUserObject();
            
            int ruleCount = ftsNode.getChildCount();
            Rule[] rules = new Rule[ruleCount];
            for(int j = 0; j < ruleCount; j++) {
                DefaultMutableTreeNode ruleNode = (DefaultMutableTreeNode) ftsNode.getChildAt(j);
                rules[j] = (Rule) ruleNode.getUserObject();
                
                int symCount = ruleNode.getChildCount();
                Symbolizer[] symbolizers = new Symbolizer[symCount];
                for(int k = 0; k < symCount; k++) {
                    DefaultMutableTreeNode symNode = (DefaultMutableTreeNode) ruleNode.getChildAt(k);
                    symbolizers[k] = (Symbolizer) symNode.getUserObject();
                }
                rules[j].setSymbolizers(symbolizers);
            }
            featureStyles[i].setRules(rules);
        }
        style.setFeatureTypeStyles(featureStyles);
        return style;
    }

    public void refreshTree() {
        blockChangeEvents = true;

        try {
            TreePath path = tree.getSelectionPath();
            buildTree(style);

            if (path != null) {
                tree.setSelectionPath(path);
            }
        } finally {
            blockChangeEvents = true;
        }
    }

    public void expandTree() {
        if (tree.getModel() != null) {
            expandTree((DefaultMutableTreeNode) tree.getModel().getRoot());
        }
    }

    private void expandTree(DefaultMutableTreeNode node) {
        int count = node.getChildCount();

        if (count > 0) {
            for (int i = 0; i < count; i++) {
                expandTree((DefaultMutableTreeNode) node.getChildAt(i));
            }
        }

        tree.expandPath(new TreePath(node.getPath()));
    }

    public void collapseTree() {
        if (tree.getModel() == null) {
            return;
        }

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        int count = root.getChildCount();

        for (int i = 0; i < count; i++) {
            collapseTree((DefaultMutableTreeNode) root.getChildAt(i));
        }
    }

    private void collapseTree(DefaultMutableTreeNode node) {
        int count = node.getChildCount();

        if (count > 0) {
            for (int i = 0; i < count; i++) {
                collapseTree((DefaultMutableTreeNode) node.getChildAt(i));
            }
        }

        tree.collapsePath(new TreePath(node.getPath()));
    }

    private void buildTree(Style s) {
        if (s == null) {
            new DefaultMutableTreeNode();
        } else {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(s);
            FeatureTypeStyle[] featureStyles = s.getFeatureTypeStyles();

            for (int i = 0; i < featureStyles.length; i++) {
                root.add(createFeatureTypeStyleNode(featureStyles[i]));
            }

            model = new DefaultTreeModel(root);
        }

        tree.setModel(model);
    }

    private DefaultMutableTreeNode createFeatureTypeStyleNode(FeatureTypeStyle fts) {
        DefaultMutableTreeNode ftsNode = new DefaultMutableTreeNode(fts);
        Rule[] rules = fts.getRules();

        for (int j = 0; j < rules.length; j++) {
            ftsNode.add(createRuleNode(rules[j]));
        }

        return ftsNode;
    }

    private DefaultMutableTreeNode createRuleNode(Rule rule) {
        DefaultMutableTreeNode ruleNode = new DefaultMutableTreeNode(rule);
        Symbolizer[] symbolizers = rule.getSymbolizers();

        for (int k = 0; k < symbolizers.length; k++) {
            ruleNode.add(createSymbolizerNode(symbolizers[k]));
        }

        return ruleNode;
    }

    private DefaultMutableTreeNode createSymbolizerNode(Symbolizer symbolizer) {
        DefaultMutableTreeNode symbNode = new DefaultMutableTreeNode(symbolizer);

        return symbNode;
    }

    /**
     * Builds the user interface and attach event listeners
     */
    private void init() {
        // build the tree with the special cell renderer
        tree = new JTree();
        this.setLayout(new BorderLayout());
        add(tree);
        tree.setCellRenderer(new NodeRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setExpandsSelectedPaths(true);

        // listen to selection change so that we can forward a change event to
        // the listeners
        tree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    ChangeEvent event = new ChangeEvent(this);
                    fireChange(event);
                }
            });
    }

    public static void main(String[] args) throws Exception {
        AttributeType geom = AttributeTypeFactory.newAttributeType("geom",
                com.vividsolutions.jts.geom.Polygon.class);
        AttributeType[] attributeTypes = new AttributeType[] {
                geom, AttributeTypeFactory.newAttributeType("name", String.class),
                AttributeTypeFactory.newAttributeType("population", Long.class)
            };

        FeatureType ft = DefaultFeatureTypeFactory.newFeatureType(attributeTypes, "demo", "",
                false, null, (GeometryAttributeType) geom);
        FeatureTypeStyle featureStyle = styleBuilder.createFeatureTypeStyle(SymbolizerUtils
                .getDefaultSymbolizer(ft));
        Rule r = featureStyle.getRules()[0];
        r.setName("Rule1");
        featureStyle.setName("FeatureTypeStyle1");

        Style style = styleBuilder.createStyle();
        style.addFeatureTypeStyle(featureStyle);
        FormUtils.show(new TreeStyleEditor(style, ft));
    }

    /**
     * DOCUMENT ME!
     *
     * @param selection
     *
     * @return
     */
    public Object[] getSiblings(Object selection) {
        DefaultMutableTreeNode node = findNodeWithUserObject(getRootNode(), selection);
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        List siblings = new ArrayList(parent.getChildCount());

        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode curr = (DefaultMutableTreeNode) parent.getChildAt(i);

            if (curr != node) {
                siblings.add(curr.getUserObject());
            }
        }

        return siblings.toArray();
    }

    /**
     * DOCUMENT ME!
     *
     * @param selection
     *
     * @return
     */
    public boolean wouldRemoveRoot(Object selection) {
        DefaultMutableTreeNode root = getRootNode();

        return wouldRemoveRootInternal(root, findNodeWithUserObject(root, selection));
    }

    private boolean wouldRemoveRootInternal(TreeNode root, TreeNode node) {
        if (node == root) {
            return true;
        } else {
            if (node.getParent().getChildCount() > 1) {
                return false;
            } else {
                return wouldRemoveRootInternal(root, node.getParent());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param selection
     *
     * @return DOCUMENT ME!
     */
    public boolean remove(Object selection) {
        DefaultMutableTreeNode root = getRootNode();

        return removeInternal(root, findNodeWithUserObject(root, selection));
    }

    /**
     * DOCUMENT ME!
     *
     * @param root
     * @param node
     *
     * @return
     */
    private boolean removeInternal(TreeNode root, TreeNode node) {
        if (node == root) {
            return false;
        } else {
            if (node.getParent().getChildCount() > 1) {
                model.removeNodeFromParent((DefaultMutableTreeNode) node);

                return true;
            } else {
                return removeInternal(root, node.getParent());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param selection
     *
     * @return
     */
    public Object getSiblingAfter(Object selection) {
        DefaultMutableTreeNode node = findNodeWithUserObject(getRootNode(), selection);
        DefaultMutableTreeNode next = node.getNextSibling();

        if (next == null) {
            return null;
        } else {
            return next.getUserObject();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param selection
     *
     * @return
     */
    public Object getSiblingBefore(Object selection) {
        DefaultMutableTreeNode node = findNodeWithUserObject(getRootNode(), selection);
        DefaultMutableTreeNode previous = node.getPreviousSibling();

        if (previous == null) {
            return null;
        } else {
            return previous.getUserObject();
        }
    }

    public boolean moveUp(Object selection) {
        DefaultMutableTreeNode node = findNodeWithUserObject(getRootNode(), selection);
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();

        if (parent == null) {
            return false;
        }

        int index = parent.getIndex(node);

        if (index == 0) {
            return false;
        }

        model.removeNodeFromParent(node);
        model.insertNodeInto(node, parent, index - 1);

        return true;
    }

    public boolean moveDown(Object selection) {
        DefaultMutableTreeNode node = findNodeWithUserObject(getRootNode(), selection);
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();

        if (parent == null) {
            return false;
        }

        int index = parent.getIndex(node);

        if (index == (parent.getChildCount() - 1)) {
            return false;
        }

        model.removeNodeFromParent(node);
        model.insertNodeInto(node, parent, index + 1);

        return true;
    }

    /**
     * A simple renderer that shows the right label for each of the style components
     *
     * @author wolf
     */
    private static class NodeRenderer extends DefaultTreeCellRenderer {
        /**
         * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree,
         *      java.lang.Object, boolean, boolean, boolean, int, boolean)
         */
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
            boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObject = node.getUserObject();

                if (userObject instanceof Style) {
                    Style s = (Style) userObject;
                    setText(s.getName());
                } else if (userObject instanceof FeatureTypeStyle) {
                    FeatureTypeStyle fts = (FeatureTypeStyle) userObject;
                    setText(fts.getName());
                } else if (userObject instanceof Rule) {
                    Rule rule = (Rule) userObject;
                    setText(rule.getName());
                } else if (userObject instanceof PolygonSymbolizer) {
                    setText("Polygon");
                } else if (userObject instanceof LineSymbolizer) {
                    setText("Line");
                } else if (userObject instanceof PointSymbolizer) {
                    setText("Point");
                } else if (userObject instanceof TextSymbolizer) {
                    setText("Text");
                } else if (userObject instanceof RasterSymbolizer) {
                    setText("Raster");
                }
            }

            return this;
        }
    }
}
