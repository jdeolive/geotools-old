/*
 * Copyright (c) 2001 Vision for New York - www.vfny.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root application directory.
 */
package org.geotools.gml;

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.geotools.datasource.*;
import org.geotools.datasource.extents.*;
import org.geotools.featuretable.*;


/**
 * Tests out parsing of regular (simple) GML files.
 *
 * <p>Illustrates the way that you chain the filters together.  Note that TestHandler
 * is considered the 'parent' of GMLFilterGeometry (its 'child').  In turn, GMLFilterGeometry
 * is the 'parent' of GMLFilterDocument, etc.  Each parent recieves pre-processed data from 
 * its child either via the defalut content handler methods or the specialized GMLHandler
 * methods.</p>
 * <p>This small class simply illustrates how simple it is to parse GML into JTS objects using the
 * structure in this package.  You are encouraged to copy the code in this class and use it as a starting
 * point for your own GML parser.  Of course, you should replace <code>TestHandler</code> with your own
 * class.  The only requirement of your new class is that it implements <code>GMLHandlerJTS</code>.  That's it!</p>
 * 
 * @author Rob Hranac, Vision for New York
 * @version alpha, 12/01/01
 *
 */
public class TestParser {

		public static void main (String[] args) {

				// set URI from the command line and echo the submitted URI

				if( args[0].equals("-g") ) {
						parseGeometries(args[1]);
				}
				else if( args[0].equals("-f") ) {
						parseFeatures(args[1]);
				}
				else {
						System.out.println("Incorrect specification...useage:");
						System.out.println(" java org.geotools.gml.TestParser [flags] [URI to parse]");
						System.out.println("");
						System.out.println(" flags:");
						System.out.println("  -g: parse geometries only");
						System.out.println("  -f: parse flat feature collection");
						System.out.println("");
						System.out.println(" example: java org.geotools.gml.TestParser -g /home/rob/myGml.gml");
				}

		}


		public static void parseGeometries(String uri) {

				System.out.println("Parsing just the geometries in this GML resource:" + uri);

				// chains all the appropriate filters together (in correct order)
				//  and initiates parsing
				try {
						TestHandler contentHandler = new TestHandler();
						GMLFilterGeometry geometryFilter = new GMLFilterGeometry(contentHandler);						
						GMLFilterDocument documentFilter = new GMLFilterDocument(geometryFilter);
            XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");

						parser.setContentHandler(documentFilter);
						parser.parse(uri);
				}
				catch (IOException e) {
						System.out.println("Error reading uri: " + uri );
				}
				catch (SAXException e) {
						System.out.println("Error in parsing: " + e.getMessage() );
				}

				
		}


		public static void parseFeatures(String uri) {

				System.out.println("Parsing the flat feature collection in this GML resource:" + uri);

				GMLDataSource data = new GMLDataSource(uri);
				FlatFeatureTable featureCollection = new FlatFeatureTable(data);

				Vector parsedFeatures = new Vector( java.util.Arrays.asList(featureCollection.getFeatures()) );  
				FlatFeature tempFeature;
						
				for( int i = 0; i < parsedFeatures.size() ; i++ ) {
						tempFeature = (FlatFeature) parsedFeatures.get(i);
						System.out.println("Parsed feature is ... " + tempFeature.toString() );						
				}

				//parsedFeatures.

				// chains all the appropriate filters together (in correct order)
				//  and initiates parsing

				
		}

}
