/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.gml;

import org.geotools.data.*;
import org.geotools.data.gml.GMLDataSource;
import org.geotools.feature.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.*;


/**
 * Tests out parsing of regular (simple) GML files.
 * 
 * <p>
 * Illustrates the way that you chain the filters together.  Note that
 * TestHandler is considered the 'parent' of GMLFilterGeometry  (its 'child').
 * In turn, GMLFilterGeometry is the 'parent' of  GMLFilterDocument, etc. Each
 * parent receives pre-processed data from  its child either via the defalut
 * content handler methods or the specialized GMLHandler methods.
 * </p>
 * 
 * <p>
 * This small class simply illustrates how simple it is to parse GML into  JTS
 * objects using the structure in this package.  You are encouraged to copy
 * the code in this class and use it as a starting point for your own GML
 * parser.  Of course, you should replace <code>TestHandler</code> with your
 * own class.  The only requirement of your new class is that it implements
 * <code>GMLHandlerJTS</code>.  That's it!
 * </p>
 *
 * @author Rob Hranac, Vision for New York
 * @version $Id: TestParser.java,v 1.15 2003/08/14 18:36:47 cholmesny Exp $
 */
public class TestParser {
    public static void main(String[] args) {
        // set URI from the command line and echo the submitted URI
        if ((args.length > 0) && args[0].equals("-g")) {
            parseGeometries(args[1]);
        } else if ((args.length > 0) && args[0].equals("-f")) {
            parseFeatures(args[1]);
        } else {
            System.out.println("Incorrect specification...useage:");
            System.out.println(" java org.geotools.gml.TestParser [flags] "
                + "[URI to parse]");
            System.out.println("");
            System.out.println(" flags:");
            System.out.println("  -g: parse geometries only");
            System.out.println("  -f: parse flat feature collection");
            System.out.println("");
            System.out.println(" example: java org.geotools.gml.TestParser "
                + "-g /home/rob/myGml.gml");
        }
    }

    public static void parseGeometries(String uri) {
        System.out.println("Parsing just the geometries in this GML resource:"
            + uri);

        // chains all the appropriate filters together (in correct order)
        //  and initiates parsing
        try {
            TestHandler contentHandler = new TestHandler();
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
