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
package org.geotools.gml.producer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


//import org.geotools.feature.*;

/*
 * Handles the writing of coordinates for gml.
 *
 * @author Chris Holmes
 */
class CoordinateWriter {
    private static final String DEFAULT_COORD_DELIMITER = ",";
    private static final String DEFAULT_TUPLE_DELIMITER = " ";

    //REVISIT: There is a way in java to change the decimal delimiter using
    //the number formatter.  
    private static final int NUM_DECIMALS_DEFAULT = 4;

    /**
     * Internal representation of coordinate delimeter (',' for GML is default)
     */
    private String coordinateDelimiter = DEFAULT_COORD_DELIMITER;

    /** Internal representation of tuple delimeter (' ' for GML is  default) */
    private String tupleDelimiter = DEFAULT_TUPLE_DELIMITER;

    /** To be used for formatting numbers, uses US locale. */
    private NumberFormat coordFormatter = NumberFormat.getInstance(Locale.US);

    /** Whether delimiters should be printed. */
    private boolean printDelimiters = true;

    public CoordinateWriter() {
        this(NUM_DECIMALS_DEFAULT);
    }

    public CoordinateWriter(int numDecimals) {
        StringBuffer decimalPattern = new StringBuffer();

        for (int i = 0; i < numDecimals; i++) {
            decimalPattern.append("#");
        }

        String numPattern = "#." + decimalPattern.toString();

        if (coordFormatter instanceof DecimalFormat) {
            ((DecimalFormat) coordFormatter).applyPattern(numPattern);
        }
    }

    //TODO: check gml spec - can it be strings?  Or just chars?
    public CoordinateWriter(int numDecimals, String tupleDelim,
        String coordDelim) {
        this(numDecimals);

        //Dont allow nulls or empty spaces - just use defaults
        if ((tupleDelim != null) && !tupleDelim.equals("")) {
            this.tupleDelimiter = tupleDelim;
        }

        if ((coordDelim != null) && !coordDelim.equals("")) {
            this.coordinateDelimiter = coordDelim;
        }
    }

    //TODO: private setNumDecimals, with a public method.
    public void setPrintDelimiters(boolean printDelimiters) {
        this.printDelimiters = printDelimiters;
    }

    public void writeCoordinates(Geometry geometry, ContentHandler output)
        throws SAXException {
        AttributesImpl atts = new org.xml.sax.helpers.AttributesImpl();

        StringBuffer coordBuff = new StringBuffer();

        //if (printDelimiters) {
        atts.addAttribute(GMLUtils.GML_URL, "decimal", "decimal", "decimal", ".");
        atts.addAttribute(GMLUtils.GML_URL, "cs", "cs", "cs",
            coordinateDelimiter);
        atts.addAttribute(GMLUtils.GML_URL, "ts", "ts", "ts", tupleDelimiter);
        output.startElement(GMLUtils.GML_URL, "coordinates", "gml:coordinates",
            atts);

        //int dimension = geometry.getDimension();
        Coordinate[] tempCoordinates = geometry.getCoordinates();

        for (int i = 0, n = geometry.getNumPoints(); i < n; i++) {
            String xCoord = coordFormatter.format(tempCoordinates[i].x);
            String yCoord = coordFormatter.format(tempCoordinates[i].y);
            coordBuff.append(xCoord + coordinateDelimiter + yCoord
                + tupleDelimiter);
        }

        coordBuff.deleteCharAt(coordBuff.length() - 1);

        String coords = coordBuff.toString();
        output.characters(coords.toCharArray(), 0, coords.length());
        output.endElement(GMLUtils.GML_URL, "coordinates", "gml:coordinates");
    }
}
