package org.geotools.gml;

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.geotools.featuretable.*;
import org.geotools.datasource.*;


/**
 * The source of data for Features. Shapefiles, database, etc. are referenced through this 
 * interface.
 * 
 *@version $Id: GMLDataSource.java,v 1.9 2002/04/16 18:03:34 robhranac Exp $
 */
public class GMLDataSource extends XMLFilterImpl implements DataSource, GMLHandlerFeature {


		/** Specifies the default parser (Xerces) */
		private static final String defaultParser = "org.apache.xerces.parsers.SAXParser";

		/** Holds a URI for the GML data */
		private String uri;

		/** Temporary storage for the features loaded from GML. */
		private Vector features = new Vector();


		public GMLDataSource(String uri) {
				this.uri = uri;
		}


		public void setUri(String uri) {
				this.uri = uri;
		}


		public void feature(Feature feature) {
				features.add(feature);
		}


		/** 
		 * Loads Feature rows for the given Extent from the datasource
		 *
		 * @param ft featureTable to load features into
		 * @param ex an extent defining which features to load - null means all features
		 * @throws DataSourceException if anything goes wrong
		 */
		public void importFeatures(FeatureTable ft, Extent ex)
				throws DataSourceException {

				// chains all the appropriate filters together (in correct order)
				//  and initiates parsing
				try {
						GMLFilterFeature featureFilter = new GMLFilterFeature(this);						
						GMLFilterGeometry geometryFilter = new GMLFilterGeometry(featureFilter);						
						GMLFilterDocument documentFilter = new GMLFilterDocument(geometryFilter);
            XMLReader parser = XMLReaderFactory.createXMLReader(defaultParser);

						parser.setContentHandler(documentFilter);
						parser.parse(uri);
				}
				catch (IOException e) {
						System.out.println("Error reading uri: " + uri );
				}
				catch (SAXException e) {
						System.out.println("Error in parsing: " + e.getMessage() );
				}

				Feature[] typedFeatures = new Feature[features.size()];
				for ( int i = 0; i < features.size() ; i++ )
						typedFeatures[i] = (Feature) features.get(i);


				ft.addFeatures( typedFeatures );

		}


		/** 
		 * Saves the given features to the datasource
		 *
		 * @param ft feature table to get features from
		 * @param ex extent to define which features to write - null means all
		 * @throws DataSourceException if anything goes wrong or if exporting is not supported
		 */
		public void exportFeatures(FeatureTable ft, Extent ex)
				throws DataSourceException {
				throw new DataSourceException("This data source is read-only!");
		}	


		/**
		 * Stops this DataSource from loading
		 */
		public void stopLoading() {
		}

			
}

