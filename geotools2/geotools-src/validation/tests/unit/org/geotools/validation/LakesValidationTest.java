/*
 * Created on Jan 28, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.validation;

import org.geotools.data.*;
import org.geotools.data.gml.GMLDataSource;
import org.geotools.feature.*;
import org.geotools.gml.GMLFilterDocument;
import org.geotools.gml.GMLFilterGeometry;
import org.geotools.gml.GMLHandlerJTS;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.vividsolutions.jts.geom.Geometry;

import java.io.*;
import java.util.*;
/**
 * LakesValidationTest purpose.
 * <p>
 * Description of LakesValidationTest ...
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: sploreg $ (last modification)
 * @version $Id: LakesValidationTest.java,v 1.1 2004/04/29 21:57:32 sploreg Exp $
 */
public class LakesValidationTest {
		public static void main(String[] args) {
				String uri = "file:///c:/tmp/testDataFromDavidSkae.xml";
				parseGeometries(uri);
				parseFeatures(uri);
		}

		public static void parseGeometries(String uri) {
			System.out.println("Parsing just the geometries in this GML resource:"
					+ uri);

			// chains all the appropriate filters together (in correct order)
			//  and initiates parsing
			try {
				LakesTestHandler contentHandler = new LakesTestHandler();
				GMLFilterGeometry geometryFilter = new GMLFilterGeometry(contentHandler);
				GMLFilterDocument documentFilter = new GMLFilterDocument(geometryFilter);
				XMLReader parser = XMLReaderFactory.createXMLReader(
				"org.apache.xerces.parsers.SAXParser");

				parser.setContentHandler(documentFilter);
				parser.parse(uri);
			} catch (IOException e) {
				System.out.println("Error reading uri: " + uri);
			} catch (SAXException e) {
				System.out.println("Error in parsing: " + e.getMessage());
			}
		}

		public static void parseFeatures(String uri) {
			System.out.println("Parsing the flat feature collection in this GML "
					+ "resource:" + uri);

			try {
				GMLDataSource data = new GMLDataSource(uri);
				FeatureCollection featureCollection = data.getFeatures(Query.ALL);

				Iterator i = featureCollection.iterator();

				while (i.hasNext()) {
					System.out.println("Parsed feature is ... " + i.next());
				}
			} catch (DataSourceException e) {
				System.out.println(
						"TestParser->parseFeatures DataSourceException: "
						+ e.toString());
			}

			//parsedFeatures.
			// chains all the appropriate filters together (in correct order)
			// and initiates parsing
		}	
}
/**
 * Simple test implementation of <code>GMLHandlerJTS</code>. This very simple
 * handler just prints every JTS geometry that it gets to the standard output.
 *
 * @author Rob Hranac, Vision for New York
 * @version $Id: LakesValidationTest.java,v 1.1 2004/04/29 21:57:32 sploreg Exp $
 */
class LakesTestHandler extends XMLFilterImpl implements GMLHandlerJTS {
	public LakesTestHandler(){super();}
	public void geometry(Geometry geometry) {
		System.out.println("here is the geometry: " + geometry.toString());
	}
}
