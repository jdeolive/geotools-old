/*
 * GoFrame.java
 *
 * Created on 14 mai 2008, 15:29
 */

package org.geotools.gui.swing.go;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.collections.map.SingletonMap;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.gui.swing.contexttree.JContextTree;
import org.geotools.gui.swing.contexttree.JContextTreePopup;
import org.geotools.gui.swing.contexttree.column.StyleTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.VisibleTreeTableColumn;
import org.geotools.gui.swing.contexttree.popup.ContextActiveItem;
import org.geotools.gui.swing.contexttree.popup.ContextPropertyItem;
import org.geotools.gui.swing.contexttree.popup.CopyItem;
import org.geotools.gui.swing.contexttree.popup.CutItem;
import org.geotools.gui.swing.contexttree.popup.DeleteItem;
import org.geotools.gui.swing.contexttree.popup.DuplicateItem;
import org.geotools.gui.swing.contexttree.popup.LayerFeatureItem;
import org.geotools.gui.swing.contexttree.popup.LayerPropertyItem;
import org.geotools.gui.swing.contexttree.popup.LayerVisibilityItem;
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
import org.geotools.styling.BasicLineStyle;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author  sorel
 */
public class GoFrame extends javax.swing.JFrame {

    private final RandomStyleFactory RANDOM_STYLE_FACTORY = new RandomStyleFactory();
    
    private J2DMap guiMap;
    private MouseHandler mouseHandler;
    private MapContext context;
    
    
    /** Creates new form GoFrame */
    public GoFrame() {
        initComponents();        
        initTree(guiContextTree);        
        
        context = buildContext();        
        guiContextTree.addContext(context);
                        
        guiMap = new J2DMap();
        ((J2DRenderer)guiMap.getCanvas().getRenderer()).setContext(context); 
                        
        try {            
            guiMap.getCanvas().getController().setObjectiveCRS(context.getCoordinateReferenceSystem());            
        } catch (TransformException ex) {
            ex.printStackTrace();
            Logger.getLogger(J2DMap.class.getName()).log(Level.SEVERE, null, ex);
        }
              
        panGeneral.add(BorderLayout.CENTER, guiMap);
        
        guiNavBar.setMap(guiMap);
        guiCoordBar.setMap(guiMap);
        
        
        setSize(1024,768);
        setLocationRelativeTo(null);             
    }

    
    
    private MapContext buildContext() {
        MapContext context = null;
        MapLayer layer;

        try {
            context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
            Map<String,Object> params = new HashMap<String,Object>();
            File shape = new File("/home/sorel/GIS_DATA/RESROU_TRONCON_ROUTE.SHP");
            params.put( "url", shape.toURI().toURL() );
           
            DataStore store = DataStoreFinder.getDataStore(params);
            FeatureSource<SimpleFeatureType, SimpleFeature> fs = store.getFeatureSource(store.getTypeNames()[0]);
            Style style = new BasicLineStyle();
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("Some lines");
            context.addLayer(layer);
            
            
            params = new HashMap<String,Object>();
            shape = new File("/home/sorel/GIS_DATA/ALTI_LIGNE_ISO.SHP");
            params.put( "url", shape.toURI().toURL() );
            
            store = DataStoreFinder.getDataStore(params);
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_line.shp");
            context.addLayer(layer);
            
            params = new HashMap<String,Object>();
            shape = new File("/home/sorel/GIS_DATA/BATIMENT_SURF.SHP");
            params.put( "url", shape.toURI().toURL() );
            
            store = DataStoreFinder.getDataStore(params);
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_line.shp");
            context.addLayer(layer);
            
            

            context.setCoordinateReferenceSystem(layer.getFeatureSource().getSchema().getCRS());
            context.setTitle("DemoContext");
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return context;
        
//         MapContext context = null;
//        MapLayer layer;
//
//        try {
//            context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
//            DataStore store = DataStoreFinder.getDataStore(new SingletonMap("url",  GoFrame.class.getResource("/org/geotools/gui/swing/demo/shape/test_polygon.shp")));
//            FeatureSource<SimpleFeatureType, SimpleFeature> fs = store.getFeatureSource(store.getTypeNames()[0]);
//            Style style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
//            layer = new DefaultMapLayer(fs, style);
//            layer.setTitle("demo_polygon.shp");
//            context.addLayer(layer);
//
//            store = DataStoreFinder.getDataStore(new SingletonMap("url",  GoFrame.class.getResource("/org/geotools/gui/swing/demo/shape/test_ligne.shp")));
//            fs = store.getFeatureSource(store.getTypeNames()[0]);
//            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
//            layer = new DefaultMapLayer(fs, style);
//            layer.setTitle("demo_line.shp");
//            context.addLayer(layer);
//
//            store = DataStoreFinder.getDataStore(new SingletonMap("url",  GoFrame.class.getResource("/org/geotools/gui/swing/demo/shape/test_point.shp")));
//            fs = store.getFeatureSource(store.getTypeNames()[0]);
//            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
//            layer = new DefaultMapLayer(fs, style);
//            layer.setTitle("demo_point.shp");
//            context.addLayer(layer);
//            context.setTitle("DemoContext");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//
//        return context;
        
        
    }
    
    private void initTree(JContextTree tree) {
        JContextTreePopup popup = tree.getPopupMenu();

        popup.addItem(new LayerVisibilityItem());           //layer         
        popup.addItem(new SeparatorItem());
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


        tree.addColumn(new VisibleTreeTableColumn());
        tree.addColumn(new StyleTreeTableColumn());


        tree.revalidate();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jSplitPane1 = new javax.swing.JSplitPane();
        guiContextTree = new org.geotools.gui.swing.contexttree.JContextTree();
        panGeneral = new javax.swing.JPanel();
        guiCoordBar = new org.geotools.gui.swing.go.control.JCoordinateBar();
        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        guiNavBar = new org.geotools.gui.swing.go.control.JNavigationBar();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Go-2 Java2D Renderer");

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setLeftComponent(guiContextTree);

        panGeneral.setLayout(new java.awt.BorderLayout());
        panGeneral.add(guiCoordBar, java.awt.BorderLayout.PAGE_END);

        jSplitPane1.setRightComponent(panGeneral);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/geotools/gui/swing/icon/defaultset/crystalproject/16x16/actions/folder_new.png"))); // NOI18N
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/geotools/gui/swing/icon/defaultset/crystalproject/16x16/actions/edit_add.png"))); // NOI18N
        jButton2.setBorderPainted(false);
        jButton2.setContentAreaFilled(false);
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jToolBar1, gridBagConstraints);

        guiNavBar.setFloatable(false);
        guiNavBar.setRollover(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(guiNavBar, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jMenu1.setText("File");

        jMenuItem1.setText("Quit");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
    System.exit(0);
}//GEN-LAST:event_jMenuItem1ActionPerformed

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (guiContextTree.getActiveContext() != null) {
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
                    guiContextTree.getActiveContext().addLayer(layer);
                }
            }

        }
}//GEN-LAST:event_jButton2ActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        DefaultMapContext context;
        context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
        context.setTitle("Context n");
        guiContextTree.addContext(context);
}//GEN-LAST:event_jButton1ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        
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
        
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GoFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private org.geotools.gui.swing.contexttree.JContextTree guiContextTree;
    private org.geotools.gui.swing.go.control.JCoordinateBar guiCoordBar;
    private org.geotools.gui.swing.go.control.JNavigationBar guiNavBar;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel panGeneral;
    // End of variables declaration//GEN-END:variables

}
