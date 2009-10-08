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

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JToolBar;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.swing.JCRSChooser;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This is a visual example of changing the coordinate reference
 * system of a feature layer.
 */
public class CRSLab {

    public static void main(String[] args) throws Exception {
        final File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            return;
        }

        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        final FeatureSource featureSource = store.getFeatureSource();

        // Create a map context and add our shapefile to it
        final MapContext map = new DefaultMapContext();
        map.addLayer(featureSource, null);

        JMapFrame mapFrame = new JMapFrame(map);
        mapFrame.enableTool(JMapFrame.Tool.NONE);
        mapFrame.enableStatusBar(true);

        JToolBar toolbar = mapFrame.getToolBar();
        toolbar.add( new AbstractAction("Change CRS") {            
            public void actionPerformed(ActionEvent arg0) {
                try {
                    CoordinateReferenceSystem crs = JCRSChooser.showDialog(
                            null, "Coordinate Reference System", "Choose a new projection:", null);
                    if( crs != null){
                        map.setCoordinateReferenceSystem(crs);
                    }
                } catch (Exception ex) {
                    System.out.println("Could not use crs " + ex);
                }
            }
        }); 
        mapFrame.setSize(800, 600);
        mapFrame.setVisible(true);
    }

}
