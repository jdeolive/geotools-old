/* Copyright (c) 2001 Vision for New York - www.vfny.org.  All rights reserved.
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.gml;

import java.util.*;
import java.math.*;

import org.xml.sax.ContentHandler;
import org.xml.sax.*;
import org.xml.sax.helpers.*;


/**
 * LEVEL1 saxGML4j GML filter: Sends basic alerts for GML types to GMLFilterGeometry.
 *
 * <p>This filter simply seperates and passes GML events to a GMLGeometryFilter.  The main
 * simplification that it performs is to pass along coordinates as an abstracted CoordType,
 * regardless of their notation in the GML (Coord vs. Coordinates).</p>
 * 
 * @author Rob Hranac, Vision for New York
 * @version alpha, 12/01/01
 *
 */
public class GMLFilterDocument extends XMLFilterImpl {

		/** content handler definition */
		private GMLHandlerGeometry parent;  

		/** Handles all coordinate parsing */
		private CoordinateReader coordinateReader = new CoordinateReader();

		// types for encoding coordinate types
		private static final String GML_NAMESPACE = "http://www.opengis.net/gml";
		private static final String COORD_NAME = "coord";  
		private static final String COORDINATES_NAME = "coordinates";  
		private static final String X_NAME = "X";  
		private static final String Y_NAME = "Y";  
		private static final String Z_NAME = "Z";  

		//private static Arrays TYPE_FACTORY = new Arrays(); 
		private static final Collection SUB_GEOMETRY_TYPES = new Vector( java.util.Arrays.asList(new String[] {"outerBoundaryIs", "innerBoundaryIs"}) );  
		private static final Collection BASE_GEOMETRY_TYPES = new Vector( java.util.Arrays.asList(new String[] {"Point","LineString","Polygon","LinearRing", "Box", "MultiPoint","MultiLineString","MultiPolygon", "GeometryCollection"}) );  


		/**
		 * Constructor with parent.
		 */
		public GMLFilterDocument () {
		}


		/**
		 * Constructor with parent.
		 */
		public GMLFilterDocument (GMLHandlerGeometry parent) {
				super(parent);
				this.parent = parent;
		}


		/**
		 * Checks for GML element start, sets flag for 'currently inside GML' and directs all relevant
		 * 
		 */
		public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
				throws SAXException {

				// if at a GML element, do lots of checks
				if( namespaceURI.equals(GML_NAMESPACE) ) {

						// if geometry, on down the filter chain
					  if      ( BASE_GEOMETRY_TYPES.contains(localName) )  { this.parent.geometryStart(localName, atts); }								
					  else if ( SUB_GEOMETRY_TYPES.contains(localName) )   { this.parent.geometrySub(localName); }								

						// if coordinate, set internal coordinate handling methods
						else if ( COORDINATES_NAME.equals(localName) )       { coordinateReader.insideCoordinates(true, atts); }								
						else if ( COORD_NAME.equals(localName) )             { coordinateReader.insideCoord(true); }								
						else if ( X_NAME.equals(localName) )                 { coordinateReader.insideX(true); }								
						else if ( Y_NAME.equals(localName) )                 { coordinateReader.insideY(true); }								
						else if ( Z_NAME.equals(localName) )                 { coordinateReader.insideZ(true); }								

						else {
						}
				}

				// all non-GML data passed on down the filter chain without modification
				else {
						super.startElement(namespaceURI, localName, qName, atts);
				}
		}


		/**
		 * Checks for GML element start, sets flag for 'currently inside GML' and directs all relevant
		 * data to the appropriate function of the GeometryFilter.
		 *
		 * @param ch Raw coordinate string from the GML document.
		 * @param start Raw coordinate string from the GML document.
		 * @param length Raw coordinate string from the GML document.
		 * @throws SAXException Some parsing error occured while reading coordinates.
		 */
		public void characters(char[] ch, int start, int length)
				throws SAXException {

				// the only internal characters read by GML parsers are coordinates
				// the methods here read in both coordinates and coords and take the grunt-work out of this task for geometry handlers
				// see the documentation for CoordinatesReader to see what this entails
				String rawCoordinates = new String(ch, start, length);

				if      ( coordinateReader.insideCoordinates() ) { coordinateReader.readCoordinates( rawCoordinates ); }
				else if ( coordinateReader.insideCoord() )       { coordinateReader.readCoord( rawCoordinates ); }

				// all non-coordinate data passed on down the filter chain without modification 
				else { super.characters(ch,start,length); }			

		}
		

		/**
		 * Decides whether or not to end the GML-specific parsing and reset the inside GML flag.
		 *
		 * @param namespaceURI Raw coordinate string from the GML document.
		 * @param localName Raw coordinate string from the GML document.
		 * @param qName Raw coordinate string from the GML document.
		 */
		public void endElement(String namespaceURI, String localName, String qName)
				throws SAXException {

				// if leaving a GML element, handle and pass to appropriate internal or external method
				if( namespaceURI.equals(GML_NAMESPACE) ) {

						// if geometry, pass to appropriate handlers
					  if      ( BASE_GEOMETRY_TYPES.contains(localName) )  { this.parent.geometryEnd(localName); }								
					  else if ( SUB_GEOMETRY_TYPES.contains(localName) )   { this.parent.geometrySub(localName); }								

						// if coordinate, set internal coordinate handling methods
						else if ( COORDINATES_NAME.equals(localName) )       { coordinateReader.insideCoordinates(false); }								
						else if ( COORD_NAME.equals(localName) )             { coordinateReader.insideCoord(false); }								
						else if ( X_NAME.equals(localName) )                 { coordinateReader.insideX(false); }								
						else if ( Y_NAME.equals(localName) )                 { coordinateReader.insideY(false); }								
						else if ( Z_NAME.equals(localName) )                 { coordinateReader.insideZ(false); }								

						else {
						}
				}
				else {
						super.endElement(namespaceURI, localName, qName);
				}
		}		


		/**
		 * This utility serves to simplify the parsing process for GML elements.
		 * 
		 * <p>If you pass it a stream of GML coordinates, it translates them into
		 * single coordinates that you may then iterate through.</p>
		 * 
		 */
		private class CoordinateReader {
				
				
				/** Flag for indicating not inside any tag */
				private static final int NOT_INSIDE = 0;           

				/** Remembers where we are inside the GML coordinate stream */
				private int insideOuterFlag = NOT_INSIDE;
				/** Flag for indicating inside coord tag */
				private static final int INSIDE_COORD = 1;       
				/** Flag for indicating inside coordinates tag */ 
				private static final int INSIDE_COORDINATES = 2;

				/** Remembers where we are inside the GML coordinate stream */
				private int insideInnerFlag = NOT_INSIDE;
				/** Flag for indicating inside x tag */
				private static final int INSIDE_X = 1;           
				/** Flag for indicating inside y tag */
				private static final int INSIDE_Y = 2;           
				/** Flag for indicating inside z tag */
				private static final int INSIDE_Z = 3;           

				/** Remembers last X coordinate read */
				private Double x = new Double( Double.NaN );

				/** Remembers last Y coordinate read */
				private Double y = new Double( Double.NaN );

				/** Remembers last Z coordinate read */
				private Double z = new Double( Double.NaN );
				
				/** Stores requested delimeter for coordinate seperation; default = ','  */
				private String coordinateDelimeter = ",";
				
				/** Stores requested delimeter for tuple seperation; default = ' '  */
				private String tupleDelimeter = " ";
				
				/** Stores requested delimeter for decimal seperation; default = '.'  */
				private StringBuffer decimalDelimeter = new StringBuffer(".");
				
				/** Remembers whether or not the standard decimal is used, to speed up parsing  */
				private boolean standardDecimalFlag = true;
				
				
				/**
				 * Empty constructor.
				 *
				 */
				public CoordinateReader () {}
				
				
				/**
				 * Iterates to next coordinate in set and parses it, returning Coord representation.
				 *
				 * @param coordinateString Raw coordinate string from the GML document.
				 */
				public void readCoordinates (String coordinateString)
						throws SAXException {
						
						//System.out.println("reading coordinates: " + coordinateString );

						if( !standardDecimalFlag ) {
								coordinateString = coordinateString.replace( decimalDelimeter.charAt(0), '.' );
						}

						StringTokenizer coordinateSets = new StringTokenizer( coordinateString.trim(), tupleDelimeter);
						StringTokenizer coordinates;
						
						while( coordinateSets.hasMoreElements() ) {
								coordinates = new StringTokenizer( coordinateSets.nextToken(), coordinateDelimeter);

								x = new Double( coordinates.nextToken().trim() );
								y = new Double( coordinates.nextToken().trim() );

								if ( coordinates.hasMoreElements() ) {
										z = new Double( coordinates.nextToken().trim() );
										parent.gmlCoordinates( x.doubleValue(), y.doubleValue(), z.doubleValue() );								
								}
								else {
										parent.gmlCoordinates( x.doubleValue(), y.doubleValue() );
								}
						}
						
				}
				

				/**
				 * Constructor with raw coordinates string.
				 *
				 * @param coordinateString The raw coordinate string from the XML document.
				 */
				public void readCoord (String coordString) {

						if( !standardDecimalFlag ) {
								coordString = coordString.replace( decimalDelimeter.charAt(0), '.' );
						}

						switch (insideInnerFlag) {
						case INSIDE_X: 
								x = new Double( coordString.trim() );
								break;
						case INSIDE_Y: 
								y = new Double( coordString.trim() );
								break;
						case INSIDE_Z:
								z = new Double( coordString.trim() );
								break;
						}
				}


				/**
				 * Constructor with raw coordinates string.
				 *
				 * @param coordString The raw coordinate string from the XML document.
				 */
				public void insideCoordinates (boolean isInside, Attributes atts) {

						this.insideCoordinates( isInside );
				}


				/**
				 * Constructor with raw coordinates string.
				 *
				 * @param coordString The raw coordinate string from the XML document.
				 */
				public void insideCoordinates (boolean isInside) {

						if( isInside ) { insideOuterFlag = INSIDE_COORDINATES; }
						else           { insideOuterFlag = NOT_INSIDE; }
				}


				/**
				 * Constructor with raw coordinates string.
				 *
				 * @param isInside The raw coordinate string from the XML document.
				 */
				public void insideCoord (boolean isInside)
						throws SAXException {

						if( isInside ) { 
								insideOuterFlag = INSIDE_COORD; 
						}
						else { 

								// if coordinates exist, send on down the filter chain
								// otherwise, throw an exception
								if      ( ( !x.isNaN() ) && ( !y.isNaN() ) && ( z.isNaN() ) )  { parent.gmlCoordinates( x.doubleValue(), y.doubleValue() ); }
								else if ( ( !x.isNaN() ) && ( !y.isNaN() ) && ( !z.isNaN() ) ) { parent.gmlCoordinates( x.doubleValue(), y.doubleValue(), z.doubleValue() ); }
								else {
								}
								
								// set all coordinates to null and note that we have left the <coord> tag
								x = new Double(Double.NaN);
								y = new Double(Double.NaN);
								z = new Double(Double.NaN);
								insideOuterFlag = NOT_INSIDE;
						}
 				}

				
				/**
				 * Constructor with raw coordinates string.
				 *
				 * @param coordString The raw coordinate string from the XML document.
				 */
				public void insideX (boolean isInside) {

						if( isInside ) { insideInnerFlag = INSIDE_X; }
						else           { insideInnerFlag = NOT_INSIDE; }
				}

				
				/**
				 * Constructor with raw coordinates string.
				 *
				 * @param coordString The raw coordinate string from the XML document.
				 */
				public void insideY (boolean isInside) {

						if( isInside ) { insideInnerFlag = INSIDE_Y; }
						else           { insideInnerFlag = NOT_INSIDE; }
				}

				
				/**
				 * Constructor with raw coordinates string.
				 *
				 * @param coordString The raw coordinate string from the XML document.
				 */
				public void insideZ (boolean isInside) {

						if( isInside ) { insideInnerFlag = INSIDE_Z; }
						else           { insideInnerFlag = NOT_INSIDE; }
				}
				

				/**
				 * Constructor with raw coordinates string.
				 *
				 */
				public boolean insideCoordinates () {
						return insideOuterFlag == INSIDE_COORDINATES;
				}


				/**
				 * Constructor with raw coordinates string.
				 *
				 */
				public boolean insideCoord () {
						return insideOuterFlag == INSIDE_COORD;
				}


		}

}
