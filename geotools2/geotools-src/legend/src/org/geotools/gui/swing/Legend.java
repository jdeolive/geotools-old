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

/**
 * Legend.java Created on 14 June 2003, 20:37
 */
package org.geotools.gui.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.geotools.data.FeatureReader;
import org.geotools.feature.Feature;
import org.geotools.filter.Filter;
import org.geotools.gui.swing.legend.LegendLayerNodeInfo;
import org.geotools.gui.swing.legend.LegendNodeInfo;
import org.geotools.gui.swing.legend.LegendRootNodeInfo;
import org.geotools.gui.swing.legend.LegendRuleNodeInfo;
import org.geotools.gui.swing.legend.LegendTreeCellNameEditor;
import org.geotools.gui.swing.legend.LegendTreeCellRendererSwitcher;
import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.gui.swing.sldeditor.style.StyleDialog;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.renderer.LegendIconMaker;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;


/**
 * to follow the model-viewer-controller design pattern legend is a viewer, so context, layerlist,
 * layer will control it. any editting from the legend will result in the context, layerlist or
 * layer directly then the legend will recontruct itself and its member from the context,
 * layerlist or layer by listener or if doesn't have listener registering in this object or this
 * object doesn't have appropriate listener, it will force itself to do it by calling the
 * contextChanged() method. For example, when user edit the root text that is the title of the
 * legend and context, this editting will result in the title of the context first, then the
 * legend will read the title from the context and set its own title. Editting rule and style
 * title will be the same, editting will set the title of the rule and style first then the Legend
 * will listener to the changes and reconstruct itself.
 *
 * @author jianhuij
 */
public class Legend extends javax.swing.JPanel implements MapLayerListListener, SLDEditor,
    java.io.Serializable {
    /** The logger for the Legend module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.gui.swing");

    // map context get from mapPane
    private MapContext context;
    private String title;

    // if a node is selected in the JTree, then second right click will bring up
    // a pop up menu
    protected TreePath oldSelectedPath = null;

    // use layerName as key to get layer object;
    private Hashtable layerStore = null;

    // initial legend note icon width, the height will be the same as width
    private int iconWidth = UIManager.getIcon("Tree.openIcon").getIconWidth();
    private javax.swing.JTree legendTree;
    private javax.swing.JScrollPane legendViewerJScrollPane;

    public Legend(MapContext context, String title) {
        legendTree = new javax.swing.JTree();
        setLayout(new java.awt.BorderLayout());
        legendViewerJScrollPane = new javax.swing.JScrollPane(legendTree);
        add(legendViewerJScrollPane, java.awt.BorderLayout.CENTER);

        legendTree.setCellRenderer(new LegendTreeCellRendererSwitcher());
        legendTree.setCellEditor(new LegendTreeCellNameEditor(this));

        legendTree.setShowsRootHandles(true);
        legendTree.putClientProperty("JTree.lineStyle", "None");
        legendTree.setToggleClickCount(3);

        legendTree.setEditable(true);
        legendTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        legendTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    oldSelectedPath = e.getPath();
                }
            });

        legendTree.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    treeClicked(e);
                }
            });

        this.title = title;
        setMapContext(context);
        this.repaint();
    }

    /**
     * DOCUMENT ME!
     *
     * @param e
     */
    protected void treeClicked(MouseEvent e) {
        if (e.getClickCount() >= 2) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) legendTree.getSelectionPath()
                                                                             .getLastPathComponent();

            if (node.getUserObject() instanceof LegendRuleNodeInfo) {
                node = (DefaultMutableTreeNode) node.getParent();
            }

            if (node.getUserObject() instanceof LegendLayerNodeInfo) {
                LegendLayerNodeInfo info = (LegendLayerNodeInfo) node.getUserObject();
                MapLayer layer = info.getMapLayer();

                StyleDialog sd = StyleDialog.createDialog(this, layer.getFeatureSource(),
                        layer.getStyle());
                sd.show();

                if (sd.exitOk()) {
                    layer.setStyle(sd.getStyle());
                }
            }
        }
    }

    

    private DefaultTreeModel contructTreeModelAndLayerList(MapContext context) {
        DefaultTreeModel model = null;

        MapLayer[] layers = context.getLayers();
        boolean isRootSelected = false;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(context.getTitle());

        for (int i = 0; i < layers.length; i++) {
            root.add(contructLayer(layers[layers.length - i - 1]));

            // if all layers are invisible, root will be unselected in its checkbox selection
            if (layers[i].isVisible()) {
                isRootSelected = true;
            }
        }

        root.setUserObject(new LegendRootNodeInfo(context, context.getTitle() + " Legend",
                isRootSelected));
        model = new DefaultTreeModel(root);

        return model;
    }

    private DefaultMutableTreeNode contructLayer(MapLayer layer) {
        DefaultMutableTreeNode layerNode = new DefaultMutableTreeNode(layer.getStyle().getTitle());

        // layerNode.setUserObject( new LegendNoteObject( true, StyleFactory.createStyleFactory().getDefaultGraphic(), LegendNoteObject.LAYER, layer.getStyle().getTitle() ) );
        //layerNode.setUserObject( new LegendTreeCellRender( true, null, LegendTreeCellRender.LAYERNOTE, layer.getStyle().getTitle(), layer ) );
        // if invisable, set the checkbox unselected
        layerNode.setUserObject(new LegendLayerNodeInfo(layer.getStyle().getTitle(),
                layer.isVisible(), layer));

        Style style = layer.getStyle();
        FeatureTypeStyle[] fts = style.getFeatureTypeStyles();
        Feature sample = null;

        // Compute sample, please revisit according to geometric type
        try {
            FeatureReader fr = layer.getFeatureSource().getFeatures().reader();

            if (fr.hasNext()) {
                sample = fr.next();
            }
        } catch (Exception e) {
        }

        for (int i = 0; i < fts.length; i++) {
            Rule[] rules = fts[i].getRules();

            for (int j = 0; j < rules.length; j++) {
                // make legend layer style element here
                layerNode.add(constructRuleNote(rules[j], sample));
            }
        }

        return layerNode;
    }

    private DefaultMutableTreeNode constructRuleNote(Rule rule, Feature sample) {
        //Graphic[] legendGraphic = rule.getLegendGraphic();
        Filter filter = rule.getFilter();

        if (rule.getTitle().equalsIgnoreCase(StyleFactory.createStyleFactory().createRule()
                                                             .getTitle()) && (filter != null)) {
            rule.setTitle(filter.toString());
        }

        //Symbolizer[] symb = rule.getSymbolizers();
        DefaultMutableTreeNode elementNode;
        LegendRuleNodeInfo userObject;

        //elementNode = new DefaultMutableTreeNode( filter.toString() );
        //userObject = new LegendStyleElementNodeInfo( filter.toString(), null );
        elementNode = new DefaultMutableTreeNode(rule.getTitle());
        userObject = new LegendRuleNodeInfo(rule.getTitle(), null, rule, sample);

        userObject.setIcon(LegendIconMaker.makeLegendIcon(iconWidth, rule, sample));
        elementNode.setUserObject(userObject);

        return elementNode;
    }

    public JMenuItem constructSetSelectionMenu(final DefaultMutableTreeNode node) {
        //final LegendTreeCellRender userObject = (LegendTreeCellRender) node.getUserObject();
        final LegendNodeInfo userObject = (LegendNodeInfo) node.getUserObject();

        JMenuItem changeSelectStateMenuItem = new JMenuItem();

        if (userObject.isSelected()) {
            changeSelectStateMenuItem.setText("UnSelect");
        } else {
            changeSelectStateMenuItem.setText("Select");
        }

        changeSelectStateMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    //if ( userObject.getNoteType().equalsIgnoreCase( userObject.LEGENDROOT) ) {
                    if (userObject instanceof LegendRootNodeInfo) {
                        int layerCount = node.getChildCount();

                        for (int i = 0; i < layerCount; i++) {
                            DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode) node
                                .getChildAt(i);
                            LegendLayerNodeInfo layerUserObject = (LegendLayerNodeInfo) layerNode
                                .getUserObject();

                            //if ( userObject.isLayerSelected() && layerUserObject.getNoteType().equalsIgnoreCase( userObject.LAYERNOTE ) ) {
                            if (userObject.isSelected() && (layerUserObject.getMapLayer() != null)) {
                                layerUserObject.getMapLayer().setVisible(false);
                            } else if (!userObject.isSelected()
                                    && (layerUserObject.getMapLayer() != null)) {
                                layerUserObject.getMapLayer().setVisible(true);
                            }
                        }
                    } else {
                        if (userObject.isSelected()) {
                            if (((LegendLayerNodeInfo) userObject).getMapLayer() != null) {
                                ((LegendLayerNodeInfo) userObject).getMapLayer().setVisible(false);
                            }
                        } else if (!userObject.isSelected()) {
                            if (((LegendLayerNodeInfo) userObject).getMapLayer() != null) {
                                ((LegendLayerNodeInfo) userObject).getMapLayer().setVisible(true);
                            }
                        }
                    }

                    contextChanged();
                }
            });

        return changeSelectStateMenuItem;
    }

    public void setMapContext(MapContext context) {
        if (this.context != null) {
            context.removeMapLayerListListener(this);
        }

        if (context == null) {
            context = new DefaultMapContext();
        }

        this.context = context;
        context.addMapLayerListListener(this);
        contextChanged();
    }

    public MapContext getMapContext() {
        return this.context;
    }

    public void layerListChanged(EventObject layerListChangedEvent) {
        DefaultTreeModel treeModel = contructTreeModelAndLayerList(context);

        if (treeModel != null) {
            legendTree.setModel(treeModel);
        }

        legendTree.repaint();
    }

    /**
     * used in forcing the tree to recontruct it's model from the context and repaint itself
     */
    public void contextChanged() {
        DefaultTreeModel treeModel = contructTreeModelAndLayerList(context);

        if (treeModel != null) {
            legendTree.setModel(treeModel);
        }

        legendTree.repaint();
        LOGGER.fine("recontruct done");
    }

    public void styleChanged() {
        DefaultTreeModel treeModel = contructTreeModelAndLayerList(context);

        if (treeModel != null) {
            legendTree.setModel(treeModel);
        }

        legendTree.repaint();
        LOGGER.fine("recontruct done");
    }

    public void layerAdded(org.geotools.map.event.MapLayerListEvent event) {
    }

    public void layerChanged(org.geotools.map.event.MapLayerListEvent event) {
        if (event.getMapLayerEvent() != null) {
            if (event.getMapLayerEvent().getReason() == MapLayerEvent.STYLE_CHANGED) {
                styleChanged();
            }
        }
    }

    public void layerMoved(org.geotools.map.event.MapLayerListEvent event) {
    }

    public void layerRemoved(org.geotools.map.event.MapLayerListEvent event) {
    }

    public static void main(String[] args) {
    }
}
