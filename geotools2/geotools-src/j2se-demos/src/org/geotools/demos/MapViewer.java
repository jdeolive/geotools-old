/*
 * MapViewer.java
 *
 * Created on 20 July 2002, 19:25
 */

package org.geotools.demos;

import org.geotools.data.*;
import org.geotools.gml.*;
import org.geotools.map.*;
import org.geotools.feature.*;
import org.geotools.styling.*;
import org.geotools.gui.swing.MapPane;
import org.geotools.styling.*;

import org.geotools.datasource.extents.EnvelopeExtent;

import javax.swing.JFileChooser;
import javax.swing.JSeparator;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import java.io.File;
import java.io.IOException;

//Logging system
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Envelope;


/**
 *
 * @author  James
 * @author Cameron Shorter
 */
public class MapViewer extends javax.swing.JFrame {
    /**
     * The logger for this module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.j2se-demos");
    
    private FeatureCollection features;
    private Style style;
    private JLabel status;
    private Map map;
    private MapPane master;
    private AreaOfInterestModel aoi;
    
    /** Creates a new instance of MapViewer */
    public MapViewer() {
        initComponents();
        //initComponents2();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        JMenuBar menuBar = new javax.swing.JMenuBar();
        JMenu fileMenu = new javax.swing.JMenu();
        JMenuItem loadGML = new javax.swing.JMenuItem();
        JMenuItem loadSHP = new javax.swing.JMenuItem();
        JSeparator jSeparator1 = new javax.swing.JSeparator();
        JMenuItem loadSLD = new javax.swing.JMenuItem();
        
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Map Viewer");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        
        fileMenu.setMnemonic('F');
        fileMenu.setText("File");
        fileMenu.setToolTipText("null");
        loadGML.setText("Open GML");
        loadGML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadGMLActionPerformed(evt);
            }
        });
        
        loadSLD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSLDActionPerformed(evt);
            }
        });
        
        fileMenu.add(loadGML);
        loadSHP.setText("Item");
        loadSHP.setEnabled(false);
        fileMenu.add(loadSHP);
        fileMenu.add(jSeparator1);
        loadSLD.setText("Open SLD");
        fileMenu.add(loadSLD);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        
        status = new JLabel("[Geometry : False] [Style : False]");
        
        this.getContentPane().add(status,"South");
        map = new DefaultMap();
        aoi = new DefaultAreaOfInterestModel(null,null);
        final MapPane master = new MapPane(map,aoi);
        
        this.getContentPane().add(master.createScrollPane(),"Center");
        
        pack();
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setSize(new java.awt.Dimension(461, 318));
        setLocation((screenSize.width-461)/2,(screenSize.height-318)/2);
    }
    
    private void initComponents2()
    {
        File f=new File("/home/cameron/work/geotools2/geotools-src/svgsupport/tests/unit/testData/simple.sld");
        try {
            GMLDataSource datasource = new GMLDataSource("file:///home/cameron/work/geotools2/geotools-src/svgsupport/tests/unit/testData/simple.gml");
            features = new FeatureCollectionDefault(datasource);
            style = new SLDStyle(f.toURL());
            //setupMap();
        } catch (Exception e){
            LOGGER.warning("Exception: "+e+".  Unable to load predefined GML.");
        }
    }

    private void setupMap(){
        status.setText("[Geometry : " + (features != null) + "] " +
                       "[Style : " + (style != null) + "]");
        if(style == null || features == null){
            LOGGER.fine("abort map setup, style or features is null");
            return;
        }
        
        map.addFeatureTable(features,style);
    }
    
     private void loadSLDActionPerformed(java.awt.event.ActionEvent evt){
        try{
            JFileChooser fc = new JFileChooser();
            int status = fc.showOpenDialog(this);
            if(status != JFileChooser.APPROVE_OPTION){
                return;
            }
            File f = fc.getSelectedFile();
            style = new SLDStyle(f.toURL());
            setupMap();
        }
        catch(Exception e){
            LOGGER.warning("Unable to load SLD file "+e);
        }
     }
    
    private void loadGMLActionPerformed(java.awt.event.ActionEvent evt){
        try{
            JFileChooser fc = new JFileChooser();
            int status = fc.showOpenDialog(this);
            if(status != JFileChooser.APPROVE_OPTION){
                return;
            }
            File f = fc.getSelectedFile();
            GMLDataSource datasource = new GMLDataSource(f.toURL());
            
            features = new FeatureCollectionDefault(datasource);
            Envelope env = datasource.getBbox(false);
            EnvelopeExtent r = new EnvelopeExtent(env);
            features.getFeatures(r);
            features.setExtent(r);
            aoi.setAreaOfInterest(env,null);
            LOGGER.fine("gml data loaded "+features.getFeatures().length  + " features loaded");
            setupMap();
        }
        catch(Exception e){
            LOGGER.info("Unable to load GML file " + e);
        }
    }
    
    private void exitForm(java.awt.event.WindowEvent evt) {
        System.exit(0);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new MapViewer().show();
    }
}
