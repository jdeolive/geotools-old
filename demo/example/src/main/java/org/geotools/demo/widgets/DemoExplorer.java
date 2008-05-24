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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.geotools.gui.swing.contexttree.JContextTree;
import org.geotools.gui.swing.contexttree.JContextTreePopup;
import org.geotools.gui.swing.contexttree.column.VisibleTreeTableColumn;
import org.geotools.gui.swing.contexttree.popup.DeleteItem;
import org.geotools.gui.swing.contexttree.popup.LayerPropertyItem;
import org.geotools.gui.swing.contexttree.popup.LayerZoomItem;
import org.geotools.gui.swing.datachooser.JPostGISDataPanel;
import org.geotools.gui.swing.map.map2d.JStreamMap;
import org.geotools.gui.swing.map.map2d.control.JMap2DNavigationBar;
import org.geotools.gui.swing.propertyedit.LayerFeaturePropertyPanel;
import org.geotools.gui.swing.propertyedit.PropertyPane;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;


/**
 * @author johann sorel
 */
public class DemoExplorer extends JFrame {

    private JStreamMap map = new JStreamMap();
    private JContextTree tree = new JContextTree();
    private JMap2DNavigationBar control = new JMap2DNavigationBar(map);
    private JTabbedPane tabbed = null;
    private MapContext context = null;

    public DemoExplorer() {
        super();
                
        restoreTabs();
        initGui();

        addWindowListener(new WindowListener() {

                    public void windowOpened(WindowEvent e) {
                    }

                    public void windowClosing(WindowEvent e) {


                        DataSave[] saves = new DataSave[tabbed.getTabCount()];

                        for (int i = 0; i < tabbed.getTabCount(); i++) {
                            DataSave save = new DataSave();
                            save.nom = tabbed.getTitleAt(i);
                            save.map = ((JPostGISDataPanel) tabbed.getComponentAt(i)).getProperties();
                            saves[i] = save;
                        }

                        try {
                            writeObject("database.sav", saves);
                        } catch (IOException ex) {
                            org.geotools.util.logging.Logging.getLogger(DemoExplorer.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }

                    public void windowClosed(WindowEvent e) {

                    }

                    public void windowIconified(WindowEvent e) {

                    }

                    public void windowDeiconified(WindowEvent e) {

                    }

                    public void windowActivated(WindowEvent e) {

                    }

                    public void windowDeactivated(WindowEvent e) {

                    }
                });

    }

    private void restoreTabs() {
        tabbed = new JTabbedPane(JTabbedPane.TOP);

        DataSave[] saves = null;

        try {
            saves = (DataSave[]) readObjet("database.sav");
        } catch (Exception e) {
        }

        if (saves != null) {
            for (DataSave save : saves) {

                JPostGISDataPanel data = new JPostGISDataPanel();
                data.setProperties(save.map);
                tabbed.add(save.nom, data);

//                data.addListener(new DataListener() {
//
//                            public void addLayers(MapLayer[] layers) {
//                                if (context != null) {
//                                    context.addLayers(layers);
//                                }
//                            }
//                        });
            }
        } else {
            JPostGISDataPanel data = new JPostGISDataPanel();
            tabbed.add("Server 1", data);

//            data.addListener(new DataListener() {
//
//                        public void addLayers(MapLayer[] layers) {
//                            if (context != null) {
//                                context.addLayers(layers);
//                            }
//                        }
//                    });
        }

    }

    private void initGui() {
        setTitle("AlterSIG-Explorer");
        //setIconImage(new ImageIcon(DemoExplorer.class.getResource("/altersig/db_status.png")).getImage());

        //set the menubar
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem item = new JMenuItem("Exit");
        menu.add(item);
        bar.add(menu);
        item.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    }
                });
        setJMenuBar(bar);

        //build up the tree
        LayerZoomItem zoom = new LayerZoomItem(map);
        LayerPropertyItem feature = new LayerPropertyItem();
        List<PropertyPane> lst = new ArrayList<PropertyPane>();
        lst.add(new LayerFeaturePropertyPanel());
        feature.setPropertyPanels(lst);

        JContextTreePopup popup = tree.getPopupMenu();
        popup.addItem(zoom);
        popup.addItem(feature);
        popup.addItem(new DeleteItem(tree));
        tree.addColumn(new VisibleTreeTableColumn());
        tree.setPreferredSize(new Dimension(250, 250));
        try {
            context = new DefaultMapContext(CRS.decode("EPSG:4326"));
            context.setTitle("Explore-Context");
            tree.addContext(context);
            map.getRenderingStrategy().setContext(context);
        } catch (Exception e) {
        }


        //build the tabbedpane
        JPanel pan_datastore = new JPanel(new BorderLayout());
        JButton newserver = new JButton("New Server");
        JButton removeserver = new JButton("Remove Server");
        newserver.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        String nom = JOptionPane.showInputDialog("Server name :");
                        if (nom == null) {
                            nom = "Server";
                        }

                        JPostGISDataPanel data = new JPostGISDataPanel();
                        tabbed.add(nom, data);

//                        data.addListener(new DataListener() {
//
//                                    public void addLayers(MapLayer[] layers) {
//                                        if (context != null) {
//                                            context.addLayers(layers);
//                                        }
//                                    }
//                                });
                    }
                });
        removeserver.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        int num = tabbed.getSelectedIndex();

                        if (num >= 0) {
                            tabbed.remove(num);
                        }
                    }
                });
        JPanel group = new JPanel(new FlowLayout(FlowLayout.LEFT));
        group.add(newserver);
        group.add(removeserver);
        tabbed.setPreferredSize(new Dimension(250, 250));
        pan_datastore.add(BorderLayout.NORTH, group);
        pan_datastore.add(BorderLayout.CENTER, tabbed);

        map.setBackground(Color.WHITE);

        JPanel pan_map = new JPanel(new BorderLayout());
        pan_map.add(BorderLayout.NORTH, control);
        pan_map.add(BorderLayout.CENTER, map);

        
        
        JSplitPane center = new JSplitPane();
        center.setLeftComponent(tree);
        center.setRightComponent(pan_map);
        center.setDividerLocation(240);


        //build the overall
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(BorderLayout.CENTER, center);
        getContentPane().add(BorderLayout.NORTH, pan_datastore);


        //the frame
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setVisible(true);

    }

    public Object readObjet(String adress) throws IOException, ClassCastException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(adress);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Object o = in.readObject();
        return o;
    }

    public void writeObject(String adress, Object O) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(adress);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);

        out.writeObject(O);
        out.close();
        fileOut.close();
    }

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


        new DemoExplorer();

    }
}

class DataSave implements Serializable {

    public String nom = "";
    public Map map = new HashMap();
}

