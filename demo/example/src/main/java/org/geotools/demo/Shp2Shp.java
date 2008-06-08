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
import javax.swing.filechooser.FileFilter;

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
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class Shp2Shp {

	/**
	 * This example let's you connect to a PostGIS database and
	 * perform some queries against the tables.
	 * <p>
	 * The interesting part of this example is the creation of Filters
	 * <ul>
	 * <li>CQL
	 * <li>FilterFactory
	 * </ul>
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		File file = getCSVFile(args);
		SimpleFeatureType type = DataUtilities.createType("Location", "location:Point,name:String");

		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = FeatureCollections.newCollection();
		BufferedReader reader = new BufferedReader( new FileReader( file ));
		try {
			String line = reader.readLine();
			System.out.println( "Header: "+ line );
			for( line = reader.readLine(); line != null; line = reader.readLine()){
				String split[] = line.split("\\,");
				double longitude = Double.parseDouble( split[0] );
				double latitude = Double.parseDouble( split[1] );
				String name = split[2];
				
				GeometryFactory factory = new GeometryFactory();
				Point point = factory.createPoint( new Coordinate(longitude,latitude));
				SimpleFeature feature = SimpleFeatureBuilder.build(  type, new Object[]{point, name}, null );

				collection.add( feature );
			}
		}
		finally {
			reader.close();
		}
		File newFile = getNewShapeFile( file );
		
		DataStoreFactorySpi factory = new ShapefileDataStoreFactory();

		Map<String,Serializable> create = new HashMap<String,Serializable>();
		create.put("url", newFile.toURI().toURL() );
		create.put("create spatial index",Boolean.TRUE);
		
		ShapefileDataStore newDataStore = (ShapefileDataStore) factory.createNewDataStore( create );
		newDataStore.createSchema( type );
		newDataStore.forceSchemaCRS( DefaultGeographicCRS.WGS84 );
		
		Transaction transaction = new DefaultTransaction("create");
		String typeName = newDataStore.getTypeNames()[0];
		FeatureStore<SimpleFeatureType, SimpleFeature> featureStore;
        featureStore = (FeatureStore<SimpleFeatureType, SimpleFeature>) newDataStore.getFeatureSource( typeName );
		featureStore.setTransaction(transaction);
		try {			
			featureStore.addFeatures(collection);
			transaction.commit();
		}
		catch (Exception problem){
			problem.printStackTrace();
			transaction.rollback();
		}
		finally {
			transaction.close();
		}
		System.exit(0);
	}

	private static File getNewShapeFile(File file) {
		String path = file.getAbsolutePath();
		String newPath = path.substring(0,path.length()-4)+".shp";
		
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Save shapefile");
		chooser.setSelectedFile( new File( newPath ));		
		chooser.setFileFilter( new FileFilter(){
            public boolean accept( File f ) {
                return f.isDirectory() || f.getPath().endsWith("shp") || f.getPath().endsWith("SHP");
            }
            public String getDescription() {
                return "Shapefiles";
            }
		});
		int returnVal = chooser.showSaveDialog(null);
		
		if(returnVal != JFileChooser.APPROVE_OPTION) {
			System.exit(0);
		}
		File newFile = chooser.getSelectedFile();
		if( newFile.equals( file )){
			System.out.println("Cannot replace "+file);
			System.exit(0);
		}
		return newFile;
	}

	private static File getCSVFile(String[] args) throws FileNotFoundException {
		File file;
		if (args.length == 0){
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Open CSV file");
			chooser.setFileFilter( new FileFilter(){
                public boolean accept( File f ) {
                    return f.isDirectory() || f.getPath().endsWith("csv") || f.getPath().endsWith("CSV");
                }
                public String getDescription() {
                    return "Comma Seperated Value";
                }
			});
			int returnVal = chooser.showOpenDialog( null );
			
			if(returnVal != JFileChooser.APPROVE_OPTION) {
				System.exit(0);
			}
			file = chooser.getSelectedFile();
			
			System.out.println("Opening CVS file: " + file.getName());
		}
		else {
			file = new File( args[0] );
		}
		if (!file.exists()){
			throw new FileNotFoundException( file.getAbsolutePath() );
		}
		return file;
	}
	
}
