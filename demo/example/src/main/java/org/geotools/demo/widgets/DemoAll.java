/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.demo.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.collections.map.SingletonMap;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.gui.swing.contexttree.JContextTree;
import org.geotools.gui.swing.contexttree.JContextTreePopup;
import org.geotools.gui.swing.contexttree.TreeContextEvent;
import org.geotools.gui.swing.contexttree.TreeContextListener;
import org.geotools.gui.swing.contexttree.column.OpacityTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.SelectionTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.StyleTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.VisibleTreeTableColumn;
import org.geotools.gui.swing.contexttree.node.SourceGroup;
import org.geotools.gui.swing.contexttree.node.StyleGroup;
import org.geotools.gui.swing.contexttree.popup.ContextActiveItem;
import org.geotools.gui.swing.contexttree.popup.ContextPropertyItem;
import org.geotools.gui.swing.contexttree.popup.CopyItem;
import org.geotools.gui.swing.contexttree.popup.CutItem;
import org.geotools.gui.swing.contexttree.popup.DeleteItem;
import org.geotools.gui.swing.contexttree.popup.DuplicateItem;
import org.geotools.gui.swing.contexttree.popup.LayerFeatureItem;
import org.geotools.gui.swing.contexttree.popup.LayerPropertyItem;
import org.geotools.gui.swing.contexttree.popup.LayerVisibilityItem;
import org.geotools.gui.swing.contexttree.popup.LayerZoomItem;
import org.geotools.gui.swing.contexttree.popup.PasteItem;
import org.geotools.gui.swing.contexttree.popup.RuleMaxScaleItem;
import org.geotools.gui.swing.contexttree.popup.RuleMinScaleItem;
import org.geotools.gui.swing.contexttree.popup.SeparatorItem;
import org.geotools.gui.swing.datachooser.DataPanel;
import org.geotools.gui.swing.datachooser.JDataChooser;
import org.geotools.gui.swing.datachooser.JFileDataPanel;
import org.geotools.gui.swing.datachooser.JOracleDataPanel;
import org.geotools.gui.swing.datachooser.JPostGISDataPanel;
import org.geotools.gui.swing.datachooser.JWFSDataPanel;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.map2d.JStreamEditMap;
import org.geotools.gui.swing.map.map2d.Map2D;
import org.geotools.gui.swing.map.map2d.SelectableMap2D;
import org.geotools.gui.swing.map.map2d.decoration.ColorDecoration;
import org.geotools.gui.swing.map.map2d.decoration.ImageDecoration;
import org.geotools.gui.swing.map.map2d.decoration.InformationDecoration.LEVEL;
import org.geotools.gui.swing.misc.Render.RandomStyleFactory;
import org.geotools.gui.swing.propertyedit.LayerCRSPropertyPanel;
import org.geotools.gui.swing.propertyedit.LayerFilterPropertyPanel;
import org.geotools.gui.swing.propertyedit.LayerGeneralPanel;
import org.geotools.gui.swing.propertyedit.LayerStylePropertyPanel;
import org.geotools.gui.swing.propertyedit.PropertyPane;
import org.geotools.gui.swing.propertyedit.filterproperty.JCQLPropertyPanel;
import org.geotools.gui.swing.propertyedit.styleproperty.JSimpleStylePanel;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


/**
 *
 * @author johann sorel
 */
public class DemoAll extends javax.swing.JFrame {

    private final RandomStyleFactory RANDOM_STYLE_FACTORY = new RandomStyleFactory();
    private final JStreamEditMap map;
    private final OpacityTreeTableColumn colOpacity = new OpacityTreeTableColumn();
    private final VisibleTreeTableColumn colVisible = new VisibleTreeTableColumn();
    private final StyleTreeTableColumn colStyle = new StyleTreeTableColumn();
    private final SelectionTreeTableColumn colSelection = new SelectionTreeTableColumn(null);
    private final SourceGroup subsource = new SourceGroup();
    private final StyleGroup substyle = new StyleGroup();
    private final ImageDecoration overBackImage = new ImageDecoration();
    private final ColorDecoration overBackColor = new ColorDecoration();
    private int nb = 1;

    /** Creates new form DemoSwingGeowidgets */
    public DemoAll() {

        initComponents();
        setLocationRelativeTo(null);

        map = new JStreamEditMap();

        final MapContext context = buildContext();
        initTree(tree, map);

        pan_mappane.add(BorderLayout.CENTER, map);

        tree.addContext(context);

        gui_map2dnavigation.setMap(map);
        gui_map2dselection.setMap(map);
        gui_map2dinfo.setMap(map);
//        gui_map2dedit.setMap(map);

        overBackImage.setImage(IconBundle.getResource().getIcon("about").getImage());
        overBackImage.setOpaque(true);
        overBackImage.setBackground(new Color(0.7f, 0.7f, 1f, 0.8f));
        overBackImage.setStyle(org.jdesktop.swingx.JXImagePanel.Style.CENTERED);
        map.setBackgroundDecoration(overBackColor);

        tree.addTreeContextListener(new TreeContextListener() {

            public void contextAdded(TreeContextEvent event) {
            }

            public void contextRemoved(TreeContextEvent event) {
            }

            public void contextActivated(TreeContextEvent event) {
                if (event.getContext() != null) {
                    map.getRenderingStrategy().setContext(event.getContext());
                }
            }

            public void contextMoved(TreeContextEvent event) {
            }
        });


        map.getRenderingStrategy().setContext(context);

        Thread t = new Thread() {

            public void run() {
                map.getInformationDecoration().displayMessage("This in an information message", 25000, LEVEL.INFO);
                try {
                    sleep(5000);
                } catch (Exception e) {
                }
                map.getInformationDecoration().displayMessage("This in a warning message", 25000, LEVEL.WARNING);
                try {
                    sleep(5000);
                } catch (Exception e) {
                }
                map.getInformationDecoration().displayMessage("This in an error message", 25000, LEVEL.ERROR);
                try {
                    sleep(5000);
                } catch (Exception e) {
                }
                map.getInformationDecoration().displayMessage("This in a normal message", 25000, LEVEL.NORMAL);

            }
        };
        t.start();

    }

    private MapContext buildContext() {
        MapContext context = null;
        MapLayer layer;

        try {
            context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
            DataStore store = DataStoreFinder.getDataStore(new SingletonMap("url", DemoAll.class.getResource("/org/geotools/test-data/shapes/roads.shp")));
            FeatureSource<SimpleFeatureType, SimpleFeature> fs = store.getFeatureSource(store.getTypeNames()[0]);
            Style style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_polygon.shp");
            context.addLayer(layer);

            context.setTitle("DemoContext");
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return context;
    }

    private void initTree(JContextTree tree, Map2D map) {
        JContextTreePopup popup = tree.getPopupMenu();

        popup.addItem(new LayerVisibilityItem());           //layer         
        popup.addItem(new SeparatorItem());
        popup.addItem(new LayerZoomItem(map));              //layer
        popup.addItem(new LayerFeatureItem());              //layer
        popup.addItem(new ContextActiveItem(tree));         //context
        popup.addItem(new SeparatorItem());
        popup.addItem(new CutItem(tree));                   //all
        popup.addItem(new CopyItem(tree));                  //all
        popup.addItem(new PasteItem(tree));                 //all
        popup.addItem(new DuplicateItem(tree));             //all        
        popup.addItem(new SeparatorItem());
        popup.addItem(new DeleteItem(tree));                //all
        popup.addItem(new SeparatorItem());

        LayerPropertyItem property = new LayerPropertyItem();
        List<PropertyPane> lstproperty = new ArrayList<PropertyPane>();
        lstproperty.add(new LayerGeneralPanel());
        lstproperty.add(new LayerCRSPropertyPanel());

        LayerFilterPropertyPanel filters = new LayerFilterPropertyPanel();
        filters.addPropertyPanel(new JCQLPropertyPanel());
        lstproperty.add(filters);

        LayerStylePropertyPanel styles = new LayerStylePropertyPanel();
        styles.addPropertyPanel(new JSimpleStylePanel());
        lstproperty.add(styles);

        property.setPropertyPanels(lstproperty);
        
        popup.addItem(property);             //layer
        popup.addItem(new ContextPropertyItem());           //context

        popup.addItem(new RuleMinScaleItem());
        popup.addItem(new RuleMaxScaleItem());


        if (map instanceof SelectableMap2D) {
            colSelection.setMap((SelectableMap2D) map);
        }


        tree.addColumn(colVisible);
        tree.addColumn(colOpacity);
        tree.addColumn(colStyle);
        tree.addColumn(colSelection);

        tree.addSubNodeGroup(subsource);
        tree.addSubNodeGroup(substyle);

        tree.revalidate();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        dia_about = new javax.swing.JDialog();
        jXImagePanel1 = new org.jdesktop.swingx.JXImagePanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        bg_backlayer = new javax.swing.ButtonGroup();
        jSplitPane1 = new javax.swing.JSplitPane();
        jpanel8 = new javax.swing.JPanel();
        pan_mappane = new javax.swing.JPanel();
        gui_map2dinfo = new org.geotools.gui.swing.map.map2d.control.JMap2DInfoBar();
        jPanel4 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        tree = new org.geotools.gui.swing.contexttree.JContextTree();
        jPanel2 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        gui_map2dnavigation = new org.geotools.gui.swing.map.map2d.control.JMap2DNavigationBar();
        gui_map2dselection = new org.geotools.gui.swing.map.map2d.control.JMap2DSelectionBar();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenu5 = new javax.swing.JMenu();
        jMenuItem7 = new javax.swing.JMenuItem();
        guiChkVisible = new javax.swing.JCheckBoxMenuItem();
        guiChkOpacity = new javax.swing.JCheckBoxMenuItem();
        guiChkStyle = new javax.swing.JCheckBoxMenuItem();
        guiChkSelection = new javax.swing.JCheckBoxMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItem9 = new javax.swing.JMenuItem();
        guiChkSubSource = new javax.swing.JCheckBoxMenuItem();
        guiChkSubStyle = new javax.swing.JCheckBoxMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenu6 = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        jRadioButtonMenuItem3 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem2 = new javax.swing.JRadioButtonMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();

        jXImagePanel1.setImage(IconBundle.getResource().getIcon("about").getImage());
        jXImagePanel1.setStyle(org.jdesktop.swingx.JXImagePanel.Style.SCALED_KEEP_ASPECT_RATIO);

        org.jdesktop.layout.GroupLayout jXImagePanel1Layout = new org.jdesktop.layout.GroupLayout(jXImagePanel1);
        jXImagePanel1.setLayout(jXImagePanel1Layout);
        jXImagePanel1Layout.setHorizontalGroup(
            jXImagePanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 137, Short.MAX_VALUE)
        );
        jXImagePanel1Layout.setVerticalGroup(
            jXImagePanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 203, Short.MAX_VALUE)
        );

        jLabel1.setText("GT Swing Widget Team :");

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Johann Sorel" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jList1);

        org.jdesktop.layout.GroupLayout dia_aboutLayout = new org.jdesktop.layout.GroupLayout(dia_about.getContentPane());
        dia_about.getContentPane().setLayout(dia_aboutLayout);
        dia_aboutLayout.setHorizontalGroup(
            dia_aboutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(dia_aboutLayout.createSequentialGroup()
                .addContainerGap()
                .add(jXImagePanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(dia_aboutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE))
                .addContainerGap())
        );
        dia_aboutLayout.setVerticalGroup(
            dia_aboutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(dia_aboutLayout.createSequentialGroup()
                .addContainerGap()
                .add(dia_aboutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(dia_aboutLayout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE))
                    .add(jXImagePanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Swing Widgets Demo");
        setIconImage(IconBundle.getResource().getIcon("about").getImage());

        jSplitPane1.setDividerLocation(300);

        jpanel8.setBackground(new java.awt.Color(102, 102, 102));
        jpanel8.setLayout(new java.awt.BorderLayout());

        pan_mappane.setOpaque(false);
        pan_mappane.setLayout(new java.awt.BorderLayout());
        jpanel8.add(pan_mappane, java.awt.BorderLayout.CENTER);

        gui_map2dinfo.setFloatable(false);
        jpanel8.add(gui_map2dinfo, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setRightComponent(jpanel8);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jTabbedPane1.addTab("ContextTree", tree);

        jPanel4.add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(jPanel4);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/geotools/gui/swing/icon/defaultset/crystalproject/16x16/actions/folder_new.png"))); // NOI18N
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/geotools/gui/swing/icon/defaultset/crystalproject/16x16/actions/edit_add.png"))); // NOI18N
        jButton2.setBorderPainted(false);
        jButton2.setContentAreaFilled(false);
        jButton2.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jToolBar1, gridBagConstraints);

        gui_map2dnavigation.setFloatable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(gui_map2dnavigation, gridBagConstraints);

        gui_map2dselection.setFloatable(false);
        gui_map2dselection.setRollover(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(gui_map2dselection, gridBagConstraints);

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jMenu1.setText("File");

        jMenuItem4.setText("New Context");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionNewContext(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        jMenuItem1.setText("Exit");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitAction(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu4.setText("GUI");

        jMenu5.setText("Context Tree");

        jMenuItem7.setText("Columns-----");
        jMenuItem7.setEnabled(false);
        jMenu5.add(jMenuItem7);

        guiChkVisible.setSelected(true);
        guiChkVisible.setText("Visible");
        guiChkVisible.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkVisibleActionPerformed(evt);
            }
        });
        jMenu5.add(guiChkVisible);

        guiChkOpacity.setSelected(true);
        guiChkOpacity.setText("Opacity");
        guiChkOpacity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkOpacityActionPerformed(evt);
            }
        });
        jMenu5.add(guiChkOpacity);

        guiChkStyle.setSelected(true);
        guiChkStyle.setText("Style");
        guiChkStyle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkStyleActionPerformed(evt);
            }
        });
        jMenu5.add(guiChkStyle);

        guiChkSelection.setSelected(true);
        guiChkSelection.setText("Selection");
        guiChkSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkSelectionActionPerformed(evt);
            }
        });
        jMenu5.add(guiChkSelection);
        jMenu5.add(jSeparator2);

        jMenuItem9.setText("SubNode Groups-----");
        jMenuItem9.setEnabled(false);
        jMenu5.add(jMenuItem9);

        guiChkSubSource.setSelected(true);
        guiChkSubSource.setText("Source");
        guiChkSubSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkSubSourceActionPerformed(evt);
            }
        });
        jMenu5.add(guiChkSubSource);

        guiChkSubStyle.setSelected(true);
        guiChkSubStyle.setText("Style");
        guiChkSubStyle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkSubStyleActionPerformed(evt);
            }
        });
        jMenu5.add(guiChkSubStyle);
        jMenu5.add(jSeparator4);

        jMenuItem8.setText("Popup Items-----");
        jMenuItem8.setEnabled(false);
        jMenu5.add(jMenuItem8);

        jMenu4.add(jMenu5);

        jMenu6.setText("Map2D");

        jMenuItem5.setText("BackDecoration-----");
        jMenuItem5.setEnabled(false);
        jMenu6.add(jMenuItem5);

        bg_backlayer.add(jRadioButtonMenuItem3);
        jRadioButtonMenuItem3.setText("None");
        jRadioButtonMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItem3ActionPerformed(evt);
            }
        });
        jMenu6.add(jRadioButtonMenuItem3);

        bg_backlayer.add(jRadioButtonMenuItem1);
        jRadioButtonMenuItem1.setSelected(true);
        jRadioButtonMenuItem1.setText("Color");
        jRadioButtonMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItem1ActionPerformed(evt);
            }
        });
        jMenu6.add(jRadioButtonMenuItem1);

        bg_backlayer.add(jRadioButtonMenuItem2);
        jRadioButtonMenuItem2.setText("Image");
        jRadioButtonMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItem2ActionPerformed(evt);
            }
        });
        jMenu6.add(jRadioButtonMenuItem2);

        jMenu4.add(jMenu6);

        jMenuBar1.add(jMenu4);

        jMenu3.setText("?");

        jMenuItem2.setText("About");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAbout(evt);
            }
        });
        jMenu3.add(jMenuItem2);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void exitAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitAction
        System.exit(0);
    }//GEN-LAST:event_exitAction

    private void menuAbout(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAbout
        dia_about.setLocationRelativeTo(null);
        dia_about.setSize(400, 200);
        dia_about.setVisible(true);
    }//GEN-LAST:event_menuAbout

    private void actionNewContext(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionNewContext
        DefaultMapContext context;

        context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
        context.setTitle("Context " + nb);
        tree.addContext(context);
        nb++;
        
    }//GEN-LAST:event_actionNewContext

    private void guiChkVisibleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiChkVisibleActionPerformed

        if (guiChkVisible.isSelected()) {
            tree.addColumn(colVisible);
        } else {
            tree.removeColumn(colVisible);
        }
}//GEN-LAST:event_guiChkVisibleActionPerformed

    private void guiChkOpacityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiChkOpacityActionPerformed
        if (guiChkOpacity.isSelected()) {
            tree.addColumn(colOpacity);
        } else {
            tree.removeColumn(colOpacity);
        }
}//GEN-LAST:event_guiChkOpacityActionPerformed

    private void guiChkStyleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiChkStyleActionPerformed
        if (guiChkStyle.isSelected()) {
            tree.addColumn(colStyle);
        } else {
            tree.removeColumn(colStyle);
        }
}//GEN-LAST:event_guiChkStyleActionPerformed

    private void guiChkSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiChkSelectionActionPerformed
        if (guiChkSelection.isSelected()) {
            tree.addColumn(colSelection);
        } else {
            tree.removeColumn(colSelection);
        }
    }//GEN-LAST:event_guiChkSelectionActionPerformed

    private void jRadioButtonMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem2ActionPerformed
        map.setBackgroundDecoration(overBackImage);
    }//GEN-LAST:event_jRadioButtonMenuItem2ActionPerformed

    private void jRadioButtonMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem1ActionPerformed
        map.setBackgroundDecoration(overBackColor);
    }//GEN-LAST:event_jRadioButtonMenuItem1ActionPerformed

    private void jRadioButtonMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem3ActionPerformed
        map.setBackgroundDecoration(null);
    }//GEN-LAST:event_jRadioButtonMenuItem3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        DefaultMapContext context;
        context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
        context.setTitle("Context " + nb);
        tree.addContext(context);
        nb++;
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed


        if (tree.getActiveContext() != null) {
            List<DataPanel> lst = new ArrayList<DataPanel>();

            lst.add(new JFileDataPanel());
            lst.add(new JPostGISDataPanel());
            lst.add(new JOracleDataPanel());
            lst.add(new JWFSDataPanel());

            JDataChooser jdc = new JDataChooser(null, lst);

            JDataChooser.ACTION ret = jdc.showDialog();

            if (ret == JDataChooser.ACTION.APPROVE) {
                MapLayer[] layers = jdc.getLayers();
                for (MapLayer layer : layers) {
                    tree.getActiveContext().addLayer(layer);
                }
            }

        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void guiChkSubStyleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiChkSubStyleActionPerformed
        if (guiChkSubStyle.isSelected()) {
            tree.addSubNodeGroup(substyle);
        } else {
            tree.removeSubNodeGroup(substyle);
        }
    }//GEN-LAST:event_guiChkSubStyleActionPerformed

    private void guiChkSubSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiChkSubSourceActionPerformed
        if (guiChkSubSource.isSelected()) {
            tree.addSubNodeGroup(subsource);
        } else {
            tree.removeSubNodeGroup(subsource);
        }
}//GEN-LAST:event_guiChkSubSourceActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }


        final DemoAll demo= new DemoAll();
        demo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        javax.swing.SwingUtilities.invokeLater(new Runnable(){

            public void run() {
                demo.pack();
                demo.setVisible(true);
            }});

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bg_backlayer;
    private javax.swing.JDialog dia_about;
    private javax.swing.JCheckBoxMenuItem guiChkOpacity;
    private javax.swing.JCheckBoxMenuItem guiChkSelection;
    private javax.swing.JCheckBoxMenuItem guiChkStyle;
    private javax.swing.JCheckBoxMenuItem guiChkSubSource;
    private javax.swing.JCheckBoxMenuItem guiChkSubStyle;
    private javax.swing.JCheckBoxMenuItem guiChkVisible;
    private org.geotools.gui.swing.map.map2d.control.JMap2DInfoBar gui_map2dinfo;
    private org.geotools.gui.swing.map.map2d.control.JMap2DNavigationBar gui_map2dnavigation;
    private org.geotools.gui.swing.map.map2d.control.JMap2DSelectionBar gui_map2dselection;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jList1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem2;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToolBar jToolBar1;
    private org.jdesktop.swingx.JXImagePanel jXImagePanel1;
    private javax.swing.JPanel jpanel8;
    private javax.swing.JPanel pan_mappane;
    private org.geotools.gui.swing.contexttree.JContextTree tree;
    // End of variables declaration//GEN-END:variables
}
