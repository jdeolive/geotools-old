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
package org.geotools.coverage;

import org.geotools.data.DataSourceException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
    public static String BYTEORDER = "BYTEORDER";
    public static String LAYOUT = "LAYOUT";
    public static String NROWS = "NROWS";
    public static String NCOLS = "NCOLS";
    public static String NBANDS = "NBANDS";
    public static String NBITS = "NBITS";
    public static String BANDROWBYTES = "BANDROWBYTES";
    public static String TOTALROWBYTES = "TOTALROWBYTES";
    public static String BANDGAPBYTES = "BANDGAPBYTES";
    public static String NODATA = "NODATA";
    public static String ULXMAP = "ULXMAP";
    public static String ULYMAP = "ULYMAP";
    public static String XDIM = "XDIM";
    public static String YDIM = "YDIM";
    Map propertyMap;

    /**
     * Creates a new instance of GTOPO30Header
     *
     * @param headerURL URL of a GTOPO30 header (.HDR) file
     *
     * @throws FileNotFoundException if the header file does not exist
     * @throws IOException if some problem is encountered reading the file
     * @throws DataSourceException for problems related to the file content
     */
    public GT30Header(URL headerURL) throws FileNotFoundException, IOException, DataSourceException {
        String path = headerURL.getFile();
        File header = new File(path);

        BufferedReader reader = new BufferedReader(new FileReader(header));
        propertyMap = initMap();

        parseHeaderFile(propertyMap, reader);

        if (!fullPropertySet(propertyMap)) {
            throw new DataSourceException("Needed properties missing in GTOPO30 header file");
        }
    }

    /**
     * Returns a property value
     *
     * @param property use mnemonic constants
     *
     * @return the property value or null if the passed property is not recognized
     */
    public Object getProperty(String property) {
        return propertyMap.get(property);
    }

    public String getByteOrder() {
        return (String) propertyMap.get(BYTEORDER);
    }

    public String getLayout() {
        return (String) propertyMap.get(LAYOUT);
    }

    public int getNRows() {
        return ((Integer) propertyMap.get(NROWS)).intValue();
    }

    public int getNCols() {
        return ((Integer) propertyMap.get(NCOLS)).intValue();
    }

    public int getNBands() {
        return ((Integer) propertyMap.get(NBANDS)).intValue();
    }

    public int getNBits() {
        return ((Integer) propertyMap.get(NBITS)).intValue();
    }

    public int getBandRowBytes() {
        return ((Integer) propertyMap.get(BANDROWBYTES)).intValue();
    }

    public int getRowBytes() {
        return ((Integer) propertyMap.get(TOTALROWBYTES)).intValue();
    }

    public int getBandGapBytes() {
        return ((Integer) propertyMap.get(BANDGAPBYTES)).intValue();
    }

    public int getNoData() {
        return ((Integer) propertyMap.get(NODATA)).intValue();
    }

    public double getULXMap() {
        return ((Double) propertyMap.get(ULXMAP)).doubleValue();
    }

    public double getULYMap() {
        return ((Double) propertyMap.get(ULYMAP)).doubleValue();
    }

    public double getXDim() {
        return ((Double) propertyMap.get(XDIM)).doubleValue();
    }

    public double getYDim() {
        return ((Double) propertyMap.get(YDIM)).doubleValue();
    }

    /**
     * Initializes the map with the known properties, makes it easier to parse the file
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
        map.put(XDIM, new Double(0.00833333333333));
        map.put(YDIM, new Double(0.00833333333333));

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
    private void parseHeaderFile(Map properties, BufferedReader reader)
        throws IOException, DataSourceException {
        String currLine = reader.readLine();

        while (currLine != null) {
            // remove uneeded spaces
            currLine = currLine.trim();

            // get key and value            
            int firstSpaceIndex = currLine.indexOf(' ');

            if (firstSpaceIndex == -1) {
                throw new DataSourceException("Illegal line in GTOPO30 header file");
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
                    throw new DataSourceException("Invalid property value in GTOPO30 header file",
                        nfe);
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
     * @return true if the map is filled in with values, false if at least one value is null
     */
    private boolean fullPropertySet(Map properties) {
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
     * @param key
     *
     * @return the class of the value associated to the passed key
     */
    private Class getPropertyClass(String key) {
        Class propClass = null;

        if (key.equals(BYTEORDER) || key.equals(LAYOUT)) {
            propClass = String.class;
        } else if (key.equals(ULXMAP) || key.equals(ULYMAP) || key.equals(XDIM) ||
            key.equals(YDIM)) {
            propClass = Double.class;
        } else {
            propClass = Integer.class;
        }

        return propClass;
    }
}
