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
package org.geotools.data.gtopo30;

import org.geotools.data.DataSourceException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Class used to parse a GTOPO30 header (.HDR) file
 *
 * @author aaime
 */
class GT30Header {
    /** Mnemonic constant for line labels in the header file */
    public static final String BYTEORDER = "BYTEORDER";

    /** Mnemonic constant for line labels in the header file */
    public static final String LAYOUT = "LAYOUT";

    /** Mnemonic constant for line labels in the header file */
    public static final String NROWS = "NROWS";

    /** Mnemonic constant for line labels in the header file */
    public static final String NCOLS = "NCOLS";

    /** Mnemonic constant for line labels in the header file */
    public static final String NBANDS = "NBANDS";

    /** Mnemonic constant for line labels in the header file */
    public static final String NBITS = "NBITS";

    /** Mnemonic constant for line labels in the header file */
    public static final String BANDROWBYTES = "BANDROWBYTES";

    /** Mnemonic constant for line labels in the header file */
    public static final String TOTALROWBYTES = "TOTALROWBYTES";

    /** Mnemonic constant for line labels in the header file */
    public static final String BANDGAPBYTES = "BANDGAPBYTES";

    /** Mnemonic constant for line labels in the header file */
    public static final String NODATA = "NODATA";

    /** Mnemonic constant for line labels in the header file */
    public static final String ULXMAP = "ULXMAP";

    /** Mnemonic constant for line labels in the header file */
    public static final String ULYMAP = "ULYMAP";

    /** Mnemonic constant for line labels in the header file */
    public static final String XDIM = "XDIM";

    /** Mnemonic constant for line labels in the header file */
    public static final String YDIM = "YDIM";

    /** The standard cell size of GTOPO30 files */
    private static final double STD_CELL_SIZE = 0.00833333333333;

    /**
     * A map for fast and convenient retrivial of the properties contained in
     * the header file
     */
    private Map propertyMap;

    /**
     * Creates a new instance of GTOPO30Header
     *
     * @param headerURL URL of a GTOPO30 header (.HDR) file
     *
     * @throws IOException if some problem is encountered reading the file
     * @throws DataSourceException for problems related to the file content
     */
    public GT30Header(final URL headerURL)
        throws IOException, DataSourceException {
        String path = headerURL.getFile();
        File header = new File(java.net.URLDecoder.decode(path,"UTF-8"));

        BufferedReader reader = new BufferedReader(new FileReader(header));
        propertyMap = initMap();

        parseHeaderFile(propertyMap, reader);

        if (!fullPropertySet(propertyMap)) {
            throw new DataSourceException(
                "Needed properties missing in GTOPO30 header file");
        }
    }

    /**
     * Returns a property value
     *
     * @param property use mnemonic constants
     *
     * @return the property value or null if the passed property is not
     *         recognized
     */
    public Object getProperty(final String property) {
        return propertyMap.get(property);
    }

    /**
     * Returns a string representing the byte order of the data file
     *
     * @return a string representing the byte order of the data file
     */
    public String getByteOrder() {
        return (String) propertyMap.get(BYTEORDER);
    }

    /**
     * Layout of the bynary file (see gtopo30 file format description)
     *
     * @return a String describing the binary layour
     */
    public String getLayout() {
        return (String) propertyMap.get(LAYOUT);
    }

    /**
     * Returns the number of rows in the file
     *
     * @return the number of rows in the file
     */
    public int getNRows() {
        return ((Integer) propertyMap.get(NROWS)).intValue();
    }

    /**
     * Returns the number of columns in the file
     *
     * @return the number of columns in the file
     */
    public int getNCols() {
        return ((Integer) propertyMap.get(NCOLS)).intValue();
    }

    /**
     * Return the number of bands. Warning: official GTOPO30 files just have
     * one band
     *
     * @return the number of bands
     */
    public int getNBands() {
        return ((Integer) propertyMap.get(NBANDS)).intValue();
    }

    /**
     * Returns the number of bits used to encode a cell
     *
     * @return the number of bits per cell
     */
    public int getNBits() {
        return ((Integer) propertyMap.get(NBITS)).intValue();
    }

    /**
     * Returns the number of bytes per row in a band
     *
     * @return the number of bytes per row in a band
     */
    public int getBandRowBytes() {
        return ((Integer) propertyMap.get(BANDROWBYTES)).intValue();
    }

    /**
     * Returns the number of bytes per row
     *
     * @return the number of bytes per row
     */
    public int getRowBytes() {
        return ((Integer) propertyMap.get(TOTALROWBYTES)).intValue();
    }

    /**
     * Returns the number of gap bytes used to separate bands, if any
     *
     * @return the number of gap bytes used to separate bands
     */
    public int getBandGapBytes() {
        return ((Integer) propertyMap.get(BANDGAPBYTES)).intValue();
    }

    /**
     * Returns the value used to represent lack of data (usually -9999)
     *
     * @return the value used to represent lack of data
     */
    public int getNoData() {
        return ((Integer) propertyMap.get(NODATA)).intValue();
    }

    /**
     * Returns the x coordinate (latitude) of the tile center
     *
     * @return the x coordinate of the tile center
     */
    public double getULXMap() {
        return ((Double) propertyMap.get(ULXMAP)).doubleValue();
    }

    /**
     * Returns the y coordinate (longitude) of the tile center
     *
     * @return the y coordinate of the tile center
     */
    public double getULYMap() {
        return ((Double) propertyMap.get(ULYMAP)).doubleValue();
    }

    /**
     * Returns the width of the tile in degrees
     *
     * @return the width of the tile in degrees
     */
    public double getXDim() {
        return ((Double) propertyMap.get(XDIM)).doubleValue();
    }

    /**
     * Returns the height of the tile in degrees
     *
     * @return the height of the tile in degrees
     */
    public double getYDim() {
        return ((Double) propertyMap.get(YDIM)).doubleValue();
    }

    /**
     * Initializes the map with the known properties, makes it easier to parse
     * the file
     *
     * @return the initialized map
     */
    private Map initMap() {
        Map map = new HashMap();
        map.put(BYTEORDER, "M");
        map.put(LAYOUT, "BIL");
        map.put(NROWS, null);
        map.put(NCOLS, null);
        map.put(NBANDS, null);
        map.put(NBITS, null);
        map.put(BANDROWBYTES, null);
        map.put(TOTALROWBYTES, null);
        map.put(BANDGAPBYTES, new Integer(0));
        map.put(NODATA, new Integer(0));
        map.put(ULXMAP, null);
        map.put(ULYMAP, null);
        map.put(XDIM, new Double(STD_CELL_SIZE));
        map.put(YDIM, new Double(STD_CELL_SIZE));

        return map;
    }

    /**
     * Parses the reader for the known properties
     *
     * @param properties the map to be filled in
     * @param reader the source data
     *
     * @throws IOException for reading errors
     * @throws DataSourceException for unrecoverable data format violations
     */
    private void parseHeaderFile(
        final Map properties, final BufferedReader reader)
        throws IOException, DataSourceException {
        String currLine = reader.readLine();

        while (currLine != null) {
            // remove uneeded spaces
            currLine = currLine.trim();

            // get key and value
            int firstSpaceIndex = currLine.indexOf(' ');

            if (firstSpaceIndex == -1) {
                throw new DataSourceException(
                    "Illegal line in GTOPO30 header file");
            }

            String key = currLine.substring(0, firstSpaceIndex).toUpperCase();
            String value = currLine.substring(firstSpaceIndex).trim();

            // be tolerant about unknown keys, all we need is a subset of the
            // knows keys, the others will be discarded
            if (properties.containsKey(key)) {
                Class propClass = getPropertyClass(key);

                try {
                    if (propClass == String.class) {
                        properties.put(key, value);
                    } else if (propClass == Integer.class) {
                        properties.put(key, Integer.valueOf(value));
                    } else if (propClass == Double.class) {
                        properties.put(key, Double.valueOf(value));
                    }
                } catch (NumberFormatException nfe) {
                    throw new DataSourceException(
                        "Invalid property value in GTOPO30 header file", nfe);
                }
            }

            // read next line
            currLine = reader.readLine();
        }
    }

    /**
     * Checks wheter all of the properties in the map have been assigned
     *
     * @param properties the property map to be checked
     *
     * @return true if the map is filled in with values, false if at least one
     *         value is null
     */
    private boolean fullPropertySet(final Map properties) {
        boolean full = true;
        Collection values = properties.values();

        for (Iterator it = values.iterator(); it.hasNext();) {
            if (it.next() == null) {
                full = false;

                break;
            }
        }

        return full;
    }

    /**
     * Returns the class of the value associated with a key
     *
     * @param key The key used to insert the class into the map
     *
     * @return the class of the value associated to the passed key
     */
    private Class getPropertyClass(final String key) {
        Class propClass = null;

        if (key.equals(BYTEORDER) || key.equals(LAYOUT)) {
            propClass = String.class;
        } else if (
            key.equals(ULXMAP) || key.equals(ULYMAP) || key.equals(XDIM)
                || key.equals(YDIM)) {
            propClass = Double.class;
        } else {
            propClass = Integer.class;
        }

        return propClass;
    }
}
