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
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.swing.JCRSChooser;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This is an extension to CRSLab allowing the export
 * of a modified shape file.
 */
public class ReShapeLab {

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
        toolbar.add( new SafeAction("Export") {            
            public void action(ActionEvent e) throws Throwable {
                export( featureSource, map.getCoordinateReferenceSystem(), file );
            }
        });
        mapFrame.setSize(800, 600);
        mapFrame.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    static void export(FeatureSource featureSource,
            CoordinateReferenceSystem crs, File origional) throws Exception {
        FeatureType schema = featureSource.getSchema();
        String typeName = schema.getName().getLocalPart();
        JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
        chooser.setDialogTitle("Save reprojected shapefile");
        chooser.setSaveFile( origional );
        int returnVal = chooser.showSaveDialog(null);
        if (returnVal != JFileDataStoreChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file.equals(file)) {
            JOptionPane.showMessageDialog(null,"Cannot repalce "+file);
            return;
        }
        DefaultQuery query = new DefaultQuery();
        query.setTypeName(typeName);
        query.setCoordinateSystemReproject( crs );
        
        FeatureCollection<SimpleFeatureType,SimpleFeature> featureCollection = featureSource.getFeatures(query);
        
        DataStoreFactorySpi factory = new ShapefileDataStoreFactory();

        Map<String, Serializable> create = new HashMap<String,Serializable>();
        create.put("url", file.toURI().toURL());
        create.put("create spatial index", Boolean.TRUE);
        DataStore newDataStore = factory.createNewDataStore(create);

        newDataStore.createSchema(featureCollection.getSchema());
        Transaction transaction = new DefaultTransaction("Reproject");
        FeatureStore<SimpleFeatureType, SimpleFeature> featureStore;
        featureStore = (FeatureStore<SimpleFeatureType, SimpleFeature>) newDataStore
                .getFeatureSource(typeName);
        featureStore.setTransaction(transaction);
        try {
            featureStore.addFeatures(featureCollection);
            transaction.commit();
        } catch (Exception problem) {
            problem.printStackTrace();
            transaction.rollback();
        }
        finally {
            transaction.close();
        }
    }

}
