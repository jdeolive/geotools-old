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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class Csv2Shape {

    /**
     * This example takes a CSV file and produces a shapefile.
     * <p>
     * The interesting part of this example is the use of a Factory when creating objects.
     * 
     * <pre>
     * &lt;code&gt;
     * GeometryFactory factory = JTSGeometryFactory.getGeometryFactory(null);
     * Point point = factory.createPoint( new Coordinate(longitude,latitude));
     * &lt;/code&gt;
     * </pre>
     * 
     * These two classes come from the JTS Topology Suite responsible for the "rocket science"
     * aspect of GIS - determining the relationships between geometry shapes.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        
        // read csv file into feature collection
        File file = getCSVFile(args);
        final SimpleFeatureType TYPE = DataUtilities.createType("Location",
                "location:Point,name:String"); // see createFeatureType();

        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = FeatureCollections
                .newCollection();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            String line = reader.readLine();
            System.out.println("Header: " + line);
            GeometryFactory factory = JTSFactoryFinder.getGeometryFactory(null);

            for (line = reader.readLine(); line != null; line = reader.readLine()) {
                String split[] = line.split("\\,");
                double longitude = Double.parseDouble(split[0]);
                double latitude = Double.parseDouble(split[1]);
                String name = split[2];

                Point point = factory.createPoint(new Coordinate(longitude, latitude));
                SimpleFeature feature = SimpleFeatureBuilder.build(TYPE,
                        new Object[] { point, name }, null);

                collection.add(feature);
            }
        } finally {
            reader.close();
        }
        // create shapefile from feature collection
        File newFile = getNewShapeFile(file);

        DataStoreFactorySpi factory = new ShapefileDataStoreFactory();

        Map<String, Serializable> create = new HashMap<String, Serializable>();
        create.put("url", newFile.toURI().toURL());
        create.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = (ShapefileDataStore) factory.createNewDataStore(create);
        newDataStore.createSchema(TYPE);
        newDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);

        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        FeatureStore<SimpleFeatureType, SimpleFeature> featureStore;
        featureStore = (FeatureStore<SimpleFeatureType, SimpleFeature>) newDataStore
                .getFeatureSource(typeName);

        featureStore.setTransaction(transaction);
        try {
            featureStore.addFeatures(collection);
            transaction.commit();
        } catch (Exception problem) {
            problem.printStackTrace();
            transaction.rollback();
        } finally {
            transaction.close();
        }
        // we are actually exiting because we will use a Swing JFileChooser
        System.exit(0); 
    }

    // start getCSVFile
    /**
     * Prompt the user for a CSV file.
     * @param args command line arguments, args[0] will be used if provided
     * @return CSV File 
     * @throws FileNotFoundException If provided file does not exist
     */
    private static File getCSVFile(String[] args) throws FileNotFoundException {
        File file;
        if (args.length == 0) {
            JFileDataStoreChooser chooser = new JFileDataStoreChooser(new String[]{"csv"});
            chooser.setDialogTitle("Open CSV file");

            int returnVal = chooser.showOpenDialog(null);

            if (returnVal != JFileChooser.APPROVE_OPTION) {
                System.exit(0);
            }
            file = chooser.getSelectedFile();

            System.out.println("Opening CVS file: " + file.getName());
        } else {
            file = new File(args[0]);
        }
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        return file;
    }
    // end getCSVFile
    
    // start getNewShapeFile
    /**
     * Prompt for a target "shp" file.
     * 
     * @param file source file; used to create a default
     * @return "shp" file to be created
     */
    private static File getNewShapeFile(File file) {
        String path = file.getAbsolutePath();
        String newPath = path.substring(0, path.length() - 4) + ".shp";

        JFileDataStoreChooser chooser = new JFileDataStoreChooser(new String[]{"shp"});
        chooser.setDialogTitle("Save shapefile");
        chooser.setSelectedFile(new File(newPath));

        int returnVal = chooser.showSaveDialog(null);

        if (returnVal != JFileChooser.APPROVE_OPTION) {
            System.exit(0);
        }
        File newFile = chooser.getSelectedFile();
        if (newFile.equals(file)) {
            System.out.println("Cannot replace " + file);
            System.exit(0);
        }
        return newFile;
    }
    // end getNewShapeFile

    // start createFeatureType
    /**
     * Here is how you can use a SimpleFeatureType builder to create the schema for your shapefile
     * dynamically.
     * <p>
     * This method is an improvement on the origional example as we are specifying
     * DefaultGeographicCRS.WGS84 and a maximum field length.
     * <p>
     * 
     * @return SimpleFeatureType
     */
    static SimpleFeatureType createFeatureType() {

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Location");
        builder.setCRS(DefaultGeographicCRS.WGS84);

        // add attributes in order
        builder.add("Location", Point.class);
        builder.length(15).add("Name", String.class);

        // build the type
        final SimpleFeatureType LOCATION = builder.buildFeatureType();
        return LOCATION;
    }
    // end createFeatureType
}
