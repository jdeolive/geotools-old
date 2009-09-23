/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */

package org.geotools.demo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This is a visual example of changing the coordinate reference
 * system of a feature layer.
 */
public class CRSDemo {

    public static void main(String[] args) throws Exception {
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            return;
        }

        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        FeatureSource featureSource = store.getFeatureSource();

        // Create a map context and add our shapefile to it
        final MapContext map = new DefaultMapContext();
        map.addLayer(featureSource, null);

        JMapFrame mapFrame = new JMapFrame(map);
        mapFrame.enableTool(JMapFrame.Tool.NONE);
        mapFrame.enableStatusBar(true);

        JToolBar toolbar = mapFrame.getToolBar();
        JButton btn = new JButton("Change CRS");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    CoordinateReferenceSystem crs = getCoordinateReferenceSystem("Select a new CRS");
                    if( crs == null){
                        return; // canceled
                    }
                    map.setCoordinateReferenceSystem(crs);;                    
                } catch (Exception ex) {
                    System.out.println("Could not uses crs "+ex);
                }
            }
        });
        toolbar.add(btn);
        mapFrame.setSize(600, 600);
        mapFrame.setVisible(true);
    }


    /**
     * Prompt the user to select a new coordinate reference system
     * 
     * @param title dialog title
     * @return the selected CRS, or null if none selected
     */
    private static CoordinateReferenceSystem getCoordinateReferenceSystem(
            String title) throws Exception {

        CRSAuthorityFactory authorityFactory = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", null);
        Set<String> epsg = authorityFactory.getAuthorityCodes(CoordinateReferenceSystem.class);
        
        List<String> codes = new ArrayList<String>( epsg );      
        
        List<String> desc = new ArrayList<String>();
        for (String code : codes) {
            desc.add(code + ": " + authorityFactory.getDescriptionText("EPSG:" + code).toString());
        }
        String selected = (String) JOptionPane.showInputDialog(null, title,
                "Choose a Projection", JOptionPane.QUESTION_MESSAGE, null,
                desc.toArray(), "EPSG:4326");

        if (selected == null) {
            return null;
        }
        String selectedCode = selected.substring(0, selected.indexOf(':'));
        return CRS.decode("EPSG:" + selectedCode, true);
    }

}