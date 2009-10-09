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

import com.vividsolutions.jts.geom.Geometry;
import java.awt.event.ActionEvent;
import java.io.File;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
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
import org.geotools.swing.ProgressWindow;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.jdesktop.swingworker.SwingWorker;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This is a visual example of changing the coordinate reference
 * system of a feature layer.
 */
public class CRSLab {

    private File sourceFile;
    private FeatureSource featureSource;
    private MapContext map;

    public static void main(String[] args) throws Exception {
        CRSLab lab = new CRSLab();
        lab.displayShapefile();
    }

    private void displayShapefile() throws Exception {
        sourceFile = JFileDataStoreChooser.showOpenFile("shp", null);
        if (sourceFile == null) {
            return;
        }

        FileDataStore store = FileDataStoreFinder.getDataStore(sourceFile);
        featureSource = store.getFeatureSource();

        // Create a map context and add our shapefile to it
        map = new DefaultMapContext();
        map.addLayer(featureSource, null);

        JMapFrame mapFrame = new JMapFrame(map);
        mapFrame.enableTool(JMapFrame.Tool.NONE);
        mapFrame.enableStatusBar(true);

        JToolBar toolbar = mapFrame.getToolBar();
        toolbar.add( new JButton( new ChangeCRSAction() ) );
        toolbar.add( new JButton( new ExportShapefileAction() ) );
        toolbar.add( new JButton( new ValidateGeometryAction() ) );
        mapFrame.setSize(800, 600);
        mapFrame.setVisible(true);
    }


    /**
     * This class performs the task of changing the CRS of the map display
     * when the toolbar button is pressed. It also supplies the name and
     * tool tip for the button.
     */
    class ChangeCRSAction extends AbstractAction {

        ChangeCRSAction() {
            super("Reproject...");
            putValue(Action.SHORT_DESCRIPTION, "Display features with in a new CRS");
        }

        public void actionPerformed(ActionEvent arg0) {
            try {
                CoordinateReferenceSystem crs = JCRSChooser.showDialog(
                        null, "Coordinate Reference System", "Choose a new projection:", null);
                if (crs != null) {
                    map.setCoordinateReferenceSystem(crs);
                }
            } catch (Exception ex) {
                System.out.println("Could not use crs " + ex);
            }
        }
    }

    /**
     * This class performs the task of exporting the features to a new shapefile
     * using the map projection that they are currently displayed in. It also
     * supplies the name and tool tip for the toolbar button.
     */
    class ExportShapefileAction extends SafeAction {

        ExportShapefileAction() {
            super("Export...");
            putValue(Action.SHORT_DESCRIPTION, "Export features in the current projection");
        }

        @Override
        public void action(ActionEvent e) throws Throwable {

            FeatureType schema = featureSource.getSchema();
            String typeName = schema.getName().getLocalPart();
            JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
            chooser.setDialogTitle("Save reprojected shapefile");
            chooser.setSaveFile(sourceFile);
            int returnVal = chooser.showSaveDialog(null);
            if (returnVal != JFileDataStoreChooser.APPROVE_OPTION) {
                return;
            }

            File file = chooser.getSelectedFile();
            if (file.equals(sourceFile)) {
                JOptionPane.showMessageDialog(
                        null, "Cannot replace " + file,
                        "File warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // We can now query to retrieve a FeatureCollection
            // in the desired coordiante reference system
            DefaultQuery query = new DefaultQuery();
            query.setTypeName(typeName);
            query.setCoordinateSystemReproject(map.getCoordinateReferenceSystem());

            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures(query);

            // And create a new Shapefile with the results
            DataStoreFactorySpi factory = new ShapefileDataStoreFactory();

            Map<String, Serializable> create = new HashMap<String, Serializable>();
            create.put("url", file.toURI().toURL());
            create.put("create spatial index", Boolean.TRUE);
            DataStore newDataStore = factory.createNewDataStore(create);

            newDataStore.createSchema(featureCollection.getSchema());
            Transaction transaction = new DefaultTransaction("Reproject");
            FeatureStore<SimpleFeatureType, SimpleFeature> featureStore;
            featureStore = (FeatureStore<SimpleFeatureType, SimpleFeature>) newDataStore.getFeatureSource(typeName);
            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(featureCollection);
                transaction.commit();
                JOptionPane.showMessageDialog(null, "Export to shapefile complete", "Export", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();
                JOptionPane.showMessageDialog(null, "Export to shapefile failed", "Export", JOptionPane.ERROR_MESSAGE);

            } finally {
                transaction.close();
            }

        }
    }

    /**
     * This class performs the task of checking that the Geometry of each
     * feature is topologically valid and reports on the results. It also
     * supplies the name and tool tip for the toolbar button.
     */
    class ValidateGeometryAction extends SafeAction {
        
        int numInvalidGeometries = 0;

        ValidateGeometryAction() {
            super("Validate geometry");
            putValue(Action.SHORT_DESCRIPTION, "Check the geometry of each feature");
        }

        @Override
        public void action(ActionEvent e) throws Throwable {

            final FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection =
                    featureSource.getFeatures();
            
            final ProgressWindow progress = new ProgressWindow(null);
            progress.setTitle("Validating feature geometry");

            SwingWorker worker = new SwingWorker<String, Object>() {

                @Override
                protected String doInBackground() throws Exception {
                    numInvalidGeometries = 0;

                    featureCollection.accepts(new FeatureVisitor() {

                        public void visit(Feature feature) {
                            Geometry geom = (Geometry) feature.getDefaultGeometryProperty().getValue();
                            if (geom != null && !geom.isValid()) {
                                numInvalidGeometries++;
                                System.out.println("Invalid Geoemtry: " + feature.getIdentifier());
                            }
                        }
                    }, progress);

                    if (numInvalidGeometries == 0) {
                        return "All feature geometries are valid";
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append(numInvalidGeometries);
                        sb.append(" feature");
                        if (numInvalidGeometries > 1) {
                            sb.append("s");
                        }
                        sb.append(" with invalid geometries (see console output)");
                        return sb.toString();
                    }
                }

                @Override
                protected void done() {
                    try {
                        JOptionPane.showMessageDialog(null, get(), "Geometry results", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ignore) { }
                }
            };

            worker.execute();
        }
    }
}
