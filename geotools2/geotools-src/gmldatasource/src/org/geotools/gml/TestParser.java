/*
 * Copyright (c) 2001 Vision for New York - www.vfny.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root application directory.
 */
package org.geotools.gml;

import java.io.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;


/**
 * Tests out parsing of regular (simple) GML files.
 *
 * <p>Illustrates the way that you chain the filters together.  Note that TestHandler
 * is considered the 'parent' of GMLFilterGeometry (its 'child').  In turn, GMLFilterGeometry
 * is the 'parent' of GMLFilterDocument, etc.  Each parent recieves pre-processed data from 
 * its child either via the defalut content handler methods or the specialized GMLHandler
 * methods.</p>
 * 
 * @author Rob Hranac, Vision for New York
 * @version alpha, 12/01/01
 *
 */
public class TestParser {

		public static void main (String[] args) {

				// set URI from the command line
				String uri = args[0];
				System.out.println("Parsing XML file:" + uri);

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

}
