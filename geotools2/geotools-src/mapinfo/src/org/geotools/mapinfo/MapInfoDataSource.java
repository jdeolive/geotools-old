/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.mapinfo;

import com.vividsolutions.jts.geom.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.*;

import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.Extent;

import org.geotools.feature.*;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;

import org.geotools.filter.*;

import org.geotools.styling.*;


/**
 * 
 * 
 * @version $Revision: 1.7 $
 * @author $author$
 */
public class MapInfoDataSource implements DataSource {
    private static org.apache.log4j.Logger _log = org.apache.log4j.Logger.getLogger(
                                                          "MapInfoDataSource");

    
    public static final String TYPE_NONE = "none";

    
    public static final String TYPE_POINT = "point";

    
    public static final String TYPE_LINE = "line";

    
    public static final String TYPE_PLINE = "pline";

    
    public static final String TYPE_REGION = "region";

    
    public static final String TYPE_ARC = "arc";

    
    public static final String TYPE_TEXT = "text";

    
    public static final String TYPE_RECT = "rectangle";

    
    public static final String TYPE_ROUNDRECT = "rounded rectangle";

    
    public static final String TYPE_ELLIPSE = "ellipse";

    
    public static final String CLAUSE_SYMBOL = "SYMBOL";

    
    public static final String CLAUSE_PEN = "PEN";

    
    public static final String CLAUSE_SMOOTH = "SMOOTH";

    
    public static final String CLAUSE_CENTER = "CENTER";

    
    public static final String CLAUSE_BRUSH = "BRUSH";

    
    public static final String CLAUSE_VERSION = "Version";

    
    public static final String CLAUSE_CHARSET = "Charset";

    
    public static final String CLAUSE_DELIMETER = "DELIMITER";

    
    public static final String CLAUSE_UNIQUE = "UNIQUE";

    
    public static final String CLAUSE_INDEX = "INDEX";

    
    public static final String CLAUSE_COLUMNS = "COLUMNS";

    // Header information
    String hVersion;
    String hCharset;
    String hDelimeter = "\t";
    ArrayList hColumnsNames;
    ArrayList hColumnsTypes;
    Vector hUnique;
    Vector hIndex;

    // CoordsSys not supported
    // Transform not supported
    // Global variables (for the initial read)
    private String line; // The current Line of the MIF file.
    private Vector pointFeatures; // The point Features read
    private Vector lineFeatures; // The line Features read
    private Vector polygonFeatures; // The polygon Features read

    // FeatureTypes for each supported Feature Type (POINT, LINE, POLYGON) - used to build features
    private FeatureType pointFeatureType;
    private FeatureType lineFeatureType;
    private FeatureType polygonFeatureType;

    // Factories to use to build Features
    private FeatureFactory pointFactory;
    private FeatureFactory lineFactory;
    private FeatureFactory polygonFactory;

    // Factory to use to build Geometries
    private GeometryFactory geomFactory;
    private DefaultStroke stroke = new DefaultStroke();
    private DefaultFill fill = new DefaultFill();

    /**
     * Creates a new MapInfoDataSource object.
     */
    public MapInfoDataSource() {
        geomFactory = new GeometryFactory();
    }

    /**
     * Reads the MIF and MID files and returns a Vector of the Features they contain
     * 
     * @param filename the base file name to be read
     * 
     * @return a vector of features?
     * 
     * @throws DataSourceException if file doesn't exist or is not readable etc
     */
    public Vector readMifMid(String filename) throws DataSourceException {
        if (filename == null) {
            throw new DataSourceException("Invalid filename passed to readMifMid");
        }

        String mifFile = setExtension(filename, "MIF");
        String midFile = setExtension(filename, "MID");

        // Read files
        try {
            Vector features = readMifMid(new BufferedReader(new FileReader(mifFile)), 
                                         new BufferedReader(new FileReader(midFile)));

            return features;
        } catch (FileNotFoundException fnfexp) {
            throw new DataSourceException("FileNotFoundException trying to read mif file : ", 
                                          fnfexp);
        }
    }

    private String setExtension(String filename, String ext) {
        if (ext.indexOf(".") == -1) {
            ext = "." + ext;
        }

        if (filename.lastIndexOf(".") == -1) {
            return filename + ext;
        }

        return filename.substring(0, filename.lastIndexOf(".")) + ext;
    }

    /**
     * This private method constructs the factories used to create the Feature, and Geometries as
     * they are read It takes it's setup values from the value of the COLUMNS clause in the MIF
     * file
     * 
     * @throws DataSourceException 
     */
    private void setUpFactories() throws DataSourceException {
        // Go through each column name, and set up an attribute for each one
        ArrayList colAttribs = new ArrayList();

        // Add attributes for each column
        //Iterator it = hColumns.keySet().iterator();
        for (int i = 0; i < hColumnsNames.size(); i++) {
            String type = ((String) hColumnsTypes.get(i)).toLowerCase();
            Class typeClass = null;

            if (type.equals("float") || type.startsWith("decimal")) {
                typeClass = Double.class;
                hColumnsTypes.set(i, "Double");
            } else if (type.startsWith("char")) {
                typeClass = String.class;
                hColumnsTypes.set(i, "String");
            } else if (type.equals("integer") || type.equals("smallint")) {
                typeClass = Integer.class;
                hColumnsTypes.set(i, "Integer");
            } else {
                _log.warn("unknown type in mif/mid read " + type + " storing as String");
                typeClass = String.class;
                hColumnsTypes.set(i, "String");
            }

            colAttribs.add(new AttributeTypeDefault((String) hColumnsNames.get(i), typeClass));
        }


        // Add default Geometry attribute type
        colAttribs.add(0, new AttributeTypeDefault("point", Geometry.class));

        // create point feature Type & factory
        try {
            pointFeatureType = new FeatureTypeFlat(
                                       (AttributeType[]) colAttribs.toArray(new AttributeType[0]));
            pointFactory = new FeatureFactory(pointFeatureType);
        } catch (SchemaException schexp) {
            throw new DataSourceException("SchemaException setting up point factory : ", schexp);
        }


        // Set up Line factory
        // Add default attribute type
        colAttribs.set(0, new AttributeTypeDefault("line", Geometry.class));

        // create line feature Type & factory
        try {
            lineFeatureType = new FeatureTypeFlat(
                                      (AttributeType[]) colAttribs.toArray(new AttributeType[0]));
            lineFactory = new FeatureFactory(lineFeatureType);
        } catch (SchemaException schexp) {
            throw new DataSourceException("SchemaException setting up line factory : ", schexp);
        }


        // Set up Polygon factory
        // Add default attribute type
        colAttribs.set(0, new AttributeTypeDefault("polygon", Geometry.class));

        // create polygon feature Type & factory
        try {
            polygonFeatureType = new FeatureTypeFlat(
                                         (AttributeType[]) colAttribs.toArray(new AttributeType[0]));
            polygonFactory = new FeatureFactory(polygonFeatureType);
        } catch (SchemaException schexp) {
            throw new DataSourceException("SchemaException setting up polygon factory : ", schexp);
        }
    }

    /**
     * Reads an entire MID/MIF file. (Two files, actually, separately opened)
     * 
     * @param mifReader An opened BufferedReader to the MIF file.
     * @param midReader An opened BufferedReader to the MID file.
     * 
     * @return 
     * 
     * @throws DataSourceException 
     */
    private Vector readMifMid(BufferedReader mifReader, BufferedReader midReader)
                       throws DataSourceException {
        // Read the MIF header
        readMifHeader(mifReader);


        // Set up factories
        setUpFactories();

        Vector features = new Vector();

        // Start by reading first line
        try {
            line = readMifLine(mifReader);
        } catch (IOException ioexp) {
            throw new DataSourceException("No data at start of file", ioexp);
        }

        Feature feature;

        // Read each object in the MIF file
        while ((feature = readObject(mifReader, midReader)) != null) {
            // Figure out which type of feature it is
            // Add to relevent vector
            features.addElement(feature);
        }

        return features;
    }

    /**
     * Reads the header from the given MIF file stream
     * 
     * @param mifReader 
     * 
     * @throws DataSourceException 
     */
    private void readMifHeader(BufferedReader mifReader)
                        throws DataSourceException {
        try {
            while ((readMifLine(mifReader) != null) && !line.trim().equalsIgnoreCase("DATA")) {
                if (clause(line).equalsIgnoreCase(CLAUSE_VERSION)) {
                    // Read Version clause
                    hVersion = line.trim().substring(line.trim().indexOf(' ')).trim();
                    _log.info("version [" + hVersion + "]");
                }

                if (clause(line).equalsIgnoreCase(CLAUSE_CHARSET)) {
                    // Read Charset clause
                    //hCharset = line.replace('\"',' ').trim().substring(line.trim().indexOf(' ')).trim();
                    hCharset = remainder(line).replace('"', ' ').trim();
                    _log.info("Charset [" + hCharset + "]");
                }

                if (clause(line).equalsIgnoreCase(CLAUSE_DELIMETER)) {
                    // Read Delimeter clause
                    hDelimeter = line.replace('\"', ' ').trim().substring(line.trim().indexOf(' '))
                                     .trim();
                    _log.info("delimiter [" + hDelimeter + "]");
                }

                if (clause(line).equalsIgnoreCase(CLAUSE_UNIQUE)) {
                    // Read Unique clause
                    StringTokenizer st = new StringTokenizer(line.trim()
                                                                 .substring(line.trim()
                                                                                .indexOf(' ')), ",");
                    hUnique = new Vector();
                    _log.info("Unique cols ");

                    while (st.hasMoreTokens()) {
                        String uniq = st.nextToken();
                        _log.info("\t" + uniq);
                        hUnique.addElement(uniq);
                    }
                }

                if (clause(line).equalsIgnoreCase(CLAUSE_INDEX)) {
                    // Read Index clause
                    StringTokenizer st = new StringTokenizer(line.trim()
                                                                 .substring(line.trim()
                                                                                .indexOf(' ')), ",");
                    hIndex = new Vector();
                    _log.info("Indexes");

                    while (st.hasMoreTokens()) {
                        String index = st.nextToken();
                        _log.info("\t" + index);
                        hIndex.addElement(index);
                    }
                }

                if (clause(line).equalsIgnoreCase(CLAUSE_COLUMNS)) {
                    // Read Columns clause
                    int cols = 0;

                    try {
                        cols = Integer.parseInt(remainder(line));

                        if (_log.isDebugEnabled()) {
                            _log.debug("Cols " + cols);
                        }
                    } catch (NumberFormatException nfexp) {
                        _log.error("bad number of colums", nfexp);
                    }


                    // Read each of the columns
                    hColumnsNames = new ArrayList();
                    hColumnsTypes = new ArrayList();

                    for (int i = 0; i < cols; i++) {
                        line = readMifLine(mifReader);

                        //StringTokenizer st = new StringTokenizer(line.trim().substring(line.trim().indexOf(' ')), " ");
                        String name = clause(line);
                        String value = remainder(line);

                        if (_log.isDebugEnabled()) {
                            _log.debug("column name " + name + " value " + value);
                        }

                        hColumnsNames.add(name);
                        hColumnsTypes.add(value);
                    }
                }
            }
        } catch (IOException ioexp) {
            throw new DataSourceException("IOException reading MIF header : " + 
                                          ioexp.getMessage());
        }
    }

    /**
     * A 'Clause' is stored as a single string at the start of a line. This rips the clause name
     * out of the given line.
     * 
     * @param line 
     * 
     * @return 
     */
    private String clause(String line) {
        return clause(line, ' ');
    }

    private String clause(String line, char delimiter) {
        line = line.trim();

        int index = line.indexOf(delimiter);

        if (index == -1) {
            return line;
        } else {
            return line.substring(0, index).trim();
        }
    }

    /**
     * returns the last word of the string
     * 
     * @param line 
     * 
     * @return 
     */
    private String remainder(String line) {
        return remainder(line, ' ');
    }

    private String remainder(String line, char delimiter) {
        line = line.trim();

        int index = line.lastIndexOf(delimiter);

        if (index == -1) {
            return "";
        } else {
            return line.substring(index).trim();
        }
    }

    /**
     * Reads the next line in the reader, ignoring lines which are nothing but whitespace. Sets the
     * global 'line' variable to the currently read line
     * 
     * @param reader 
     * 
     * @return 
     * 
     * @throws IOException 
     * @throws DataSourceException 
     */
    private String readMifLine(BufferedReader reader) throws IOException, DataSourceException {
        do {
            line = reader.readLine();

            if (line == null) {
                return null;
            }

            if (isShadingClause(line)) {
                _log.debug("going to process shading");
                processShading(line);
                line = " ";
            }
        } while (line.trim().length() == 0);

        line = line.trim();

        //_log.debug("returning line " + line);
        return line;
    }

    /**
     * Reads a single MIF Object (Point, Line, Region, etc.) as a Feature
     * 
     * @param mifReader 
     * @param midReader 
     * 
     * @return 
     * 
     * @throws DataSourceException 
     */
    private Feature readObject(BufferedReader mifReader, BufferedReader midReader)
                        throws DataSourceException {
        Feature feature = null;

        //_log.debug("line = " + line);
        // examine The current line
        if (line == null) {
            return null;
        }

        int index = line.indexOf(' ');

        if (index == -1) {
            index = line.length();
        }

        if (line.substring(0, index).equalsIgnoreCase(TYPE_POINT)) {
            // Read point data
            _log.debug("Reading POINT");
            feature = readPointObject(mifReader, midReader);
        } else if (line.substring(0, index).equalsIgnoreCase(TYPE_LINE)) {
            // Read line data
            _log.debug("Reading LINE");
            feature = readLineObject(mifReader, midReader);
        } else if (line.substring(0, index).equalsIgnoreCase(TYPE_PLINE)) {
            // Read pline data
            _log.debug("Reading PLINE");
            feature = readPLineObject(mifReader, midReader);
        } else if (line.substring(0, index).equalsIgnoreCase(TYPE_REGION)) {
            // Read region data
            _log.debug("Reading REGION");
            feature = readRegionObject(mifReader, midReader);
        } else {
            _log.debug(line + " unknown object in mif reader");
        }

        return feature;
    }

    /**
     * Reads Point information from the MIF stream
     * 
     * @param mifReader 
     * @param midReader 
     * 
     * @return 
     * 
     * @throws DataSourceException 
     */
    private Feature readPointObject(BufferedReader mifReader, BufferedReader midReader)
                             throws DataSourceException {
        Feature feature = null;

        StringTokenizer st = new StringTokenizer(line.substring(line.indexOf(" ")), ",");

        try {
            double x = Double.parseDouble(st.nextToken());
            double y = Double.parseDouble(st.nextToken());

            // Construct Geomtry
            Geometry pointGeom = geomFactory.createPoint(new Coordinate(x, y));


            // Read next line
            readMifLine(mifReader);

            //Hashtable shading = readShading(mifReader);
            // Shading is not included, as null feature attributes are not supported yet
            ArrayList midValues = readMid(midReader);


            //			midValues.putAll(shading);
            // Create Feature
            feature = buildFeature(pointFeatureType, pointFactory, pointGeom, midValues);

            if (_log.isDebugEnabled()) {
                _log.debug("Built point feature : " + x + " " + y);
            }
        } catch (NumberFormatException nfexp) {
            throw new DataSourceException("Exception reading Point data from MIF file : ", nfexp);
        } catch (IOException ioexp) {
            throw new DataSourceException("IOException reading point data : ", ioexp);
        }

        return feature;
    }

    /**
     * Reads Line information from the MIF stream
     * 
     * @param mifReader 
     * @param midReader 
     * 
     * @return 
     * 
     * @throws DataSourceException 
     */
    private Feature readLineObject(BufferedReader mifReader, BufferedReader midReader)
                            throws DataSourceException {
        Feature feature = null;

        StringTokenizer st = new StringTokenizer(line.substring(line.indexOf(" ")), ",");

        try {
            double x1 = Double.parseDouble(st.nextToken());
            double y1 = Double.parseDouble(st.nextToken());
            double x2 = Double.parseDouble(st.nextToken());
            double y2 = Double.parseDouble(st.nextToken());

            // Construct Geomtry
            Coordinate[] cPoints = new Coordinate[2];
            cPoints[0] = new Coordinate(x1, y1);
            cPoints[1] = new Coordinate(x2, y2);

            Geometry lineGeom = geomFactory.createLineString(cPoints);


            // Read next line
            readMifLine(mifReader);

            //Hashtable shading = readShading(mifReader);
            // Shading is not included, as null feature attributes are not supported yet
            ArrayList midValues = readMid(midReader);


            //			midValues.putAll(shading);
            // Create Feature
            feature = buildFeature(lineFeatureType, lineFactory, lineGeom, midValues);

            if (_log.isDebugEnabled()) {
                _log.debug("Built line feature : " + x1 + " " + y1 + " - " + x2 + " " + y2);
            }
        } catch (NumberFormatException nfexp) {
            throw new DataSourceException("Exception reading Point data from MIF file : " + 
                                          nfexp.getMessage());
        } catch (IOException ioexp) {
            throw new DataSourceException("IOException reading point data : " + 
                                          ioexp.getMessage());
        }

        return feature;
    }

    /**
     * Reads Multi-Line (PLine) information from the MIF stream
     * 
     * @param mifReader 
     * @param midReader 
     * 
     * @return 
     * 
     * @throws DataSourceException 
     */
    private Feature readPLineObject(BufferedReader mifReader, BufferedReader midReader)
                             throws DataSourceException {
        Feature feature = null;

        StringTokenizer st = new StringTokenizer(line.substring(line.indexOf(" ")));

        try {
            int numsections = 1;

            if (st.hasMoreTokens() && st.nextToken().trim().equalsIgnoreCase("MULTIPLE")) {
                numsections = Integer.parseInt(st.nextToken());
            }

            // A vector of coordinates
            Vector coords = new Vector();

            // Read each polygon
            for (int i = 0; i < numsections; i++) {
                // Read line (number of points
                int numpoints = Integer.parseInt(readMifLine(mifReader));

                // Read each point
                for (int p = 0; p < numpoints; p++) {
                    StringTokenizer pst = new StringTokenizer(readMifLine(mifReader));
                    double x = Double.parseDouble(pst.nextToken());
                    double y = Double.parseDouble(pst.nextToken());
                    coords.addElement(new Coordinate(x, y));
                }
            }

            Geometry plineGeom = geomFactory.createLineString(
                                         (Coordinate[]) coords.toArray(
                                                   new Coordinate[coords.size()]));


            // Read next line
            readMifLine(mifReader);

            //Hashtable shading = readShading(mifReader);
            // Shading is not included, as null feature attributes are not supported yet
            ArrayList midValues = readMid(midReader);


            //			midValues.putAll(shading);
            // Create Feature
            feature = buildFeature(lineFeatureType, lineFactory, plineGeom, midValues);

            if (_log.isDebugEnabled()) {
                _log.debug("Read polyline (" + coords.size() + ")");
            }
        } catch (NumberFormatException nfexp) {
            throw new DataSourceException("Exception reading Point data from MIF file : " + 
                                          nfexp.getMessage());
        } catch (IOException ioexp) {
            throw new DataSourceException("IOException reading point data : " + 
                                          ioexp.getMessage());
        }

        return feature;
    }

    /**
     * Reads Region (Polygon) information from the MIF stream
     * 
     * @param mifReader 
     * @param midReader 
     * 
     * @return 
     * 
     * @throws DataSourceException 
     */
    private Feature readRegionObject(BufferedReader mifReader, BufferedReader midReader)
                              throws DataSourceException {
        Feature feature = null;

        StringTokenizer st = new StringTokenizer(line.substring(line.indexOf(" ")));

        try {
            int numpolygons = Integer.parseInt(st.nextToken());

            // A vector of polygons
            Vector polys = new Vector();

            // Read each polygon
            for (int i = 0; i < numpolygons; i++) {
                Vector coords = new Vector();

                // Read number of points
                int numpoints = Integer.parseInt(readMifLine(mifReader));

                // Read each point
                for (int p = 0; p < numpoints; p++) {
                    StringTokenizer pst = new StringTokenizer(readMifLine(mifReader));
                    double x = Double.parseDouble(pst.nextToken());
                    double y = Double.parseDouble(pst.nextToken());
                    coords.addElement(new Coordinate(x, y));
                }


                // Create polygon from points
                coords.addElement(
                        new Coordinate(((Coordinate) coords.get(0)).x, 
                                       ((Coordinate) coords.get(0)).y));

                try {
                    Polygon pol = geomFactory.createPolygon(geomFactory.createLinearRing(
                                                                    (Coordinate[]) coords.toArray(
                                                                              new Coordinate[coords.size()])), 
                                                            null);


                    // Add to vector
                    polys.addElement(pol);
                } catch (TopologyException topexp) {
                    throw new DataSourceException("TopologyException reading Region polygon : ", 
                                                  topexp);
                }
            }

            Geometry polyGeom = geomFactory.createMultiPolygon(
                                        (Polygon[]) polys.toArray(new Polygon[polys.size()]));


            // Read next line
            readMifLine(mifReader);

            //Hashtable shading = readShading(mifReader);
            // Shading is not included, as null feature attributes are not supported yet
            ArrayList midValues = readMid(midReader);


            //			midValues.putAll(shading);
            // Create Feature
            feature = buildFeature(polygonFeatureType, polygonFactory, polyGeom, midValues);

            if (_log.isDebugEnabled()) {
                _log.debug("Read Region (" + polys.size() + ")");
            }
        } catch (NumberFormatException nfexp) {
            throw new DataSourceException("Exception reading Point data from MIF file : ", nfexp);
        } catch (IOException ioexp) {
            throw new DataSourceException("IOException reading point data : ", ioexp);
        }

        return feature;
    }

    /**
     * Builds a complete Feature object using the given FeatureType, with the Geometry geom, and
     * the given attributes.
     * 
     * @param featureType The FeatureType to use to constuct the Feature
     * @param factory 
     * @param geom The Geometry to use as the default Geometry
     * @param attribs The attibutes to use as the Feature's attributes (Attributes must be set up
     *        in the FeatureType)
     * 
     * @return A fully-formed Feature
     * 
     * @throws DataSourceException 
     */
    private Feature buildFeature(FeatureType featureType, FeatureFactory factory, Geometry geom, 
                                 ArrayList attribs) throws DataSourceException {
        int numAttribs = featureType.getAllAttributeTypes().length;


        // add geometry to the attributes
        attribs.add(0, geom);

        if (numAttribs != attribs.size()) {
            _log.error("wrong number of attributes passed to buildFeature");
            throw new DataSourceException("wrong number of attributes passed to buildFeature.\n" + 
                                          "expected " + numAttribs + " got " + attribs.size());
        }

        // Create Feature
        try {
            return factory.create(attribs.toArray());
        } catch (IllegalFeatureException ifexp) {
            throw new DataSourceException("IllegalFeatureException creating feature : ", ifexp);
        }
    }

    /**
     * Reads a single line of the given MID file stream, and returns a hashtable of the data in it,
     * keyed byt he keys in the hColumns hash
     * 
     * @param midReader 
     * 
     * @return 
     * 
     * @throws DataSourceException 
     */
    private ArrayList readMid(BufferedReader midReader)
                       throws DataSourceException {
        ArrayList midValues = new ArrayList();

        if (midReader == null) {
            return new ArrayList();
        }

        // The delimeter is a single delimiting character
        String midLine = "";

        try {
            midLine = midReader.readLine();

            if (_log.isDebugEnabled()) {
                _log.debug("Read MID " + midLine);
            }
        } catch (IOException ioexp) {
            throw new DataSourceException("IOException reading MID file");
        }

        // read MID tokens
        int col = 0;
        StringTokenizer quotes = new StringTokenizer(midLine, "\"");

        while (quotes.hasMoreTokens()) {
            StringTokenizer delimeters = new StringTokenizer(quotes.nextToken(), hDelimeter + 
                                                             "\0");

            // Read each delimited value into the Vector
            while (delimeters.hasMoreTokens()) {
                String token = delimeters.nextToken();
                String type = (String) hColumnsTypes.get(col++);
                addAttribute(type, token, midValues);
            }

            // Store the whole of the next bit (it's a quoted string)
            if (quotes.hasMoreTokens()) {
                String token = quotes.nextToken();
                String type = (String) hColumnsTypes.get(col++);
                addAttribute(type, token, midValues);

                //_log.debug("adding " + token);
            }
        }

        return midValues;
    }

    private void addAttribute(String type, String token, ArrayList midValues) {
        if (type.equals("String")) {
            midValues.add(token);
        } else if (type.equals("Double")) {
            try {
                midValues.add(new Double(token));
            } catch (NumberFormatException nfe) {
                _log.warn("Bad double " + token);
                midValues.add(new Double(0.0));
            }
        } else if (type.equals("Integer")) {
            try {
                midValues.add(new Integer(token));
            } catch (NumberFormatException nfe) {
                _log.warn("Bad Integer value " + token);
                midValues.add(new Integer(0));
            }
        } else {
            _log.warn("Unknown type " + type);
        }
    }

    /**
     * Reads the shading information at the end of Object data
     * 
     * @param line 
     * 
     * @throws DataSourceException 
     */
    private void processShading(String line) throws DataSourceException {
        int color;
        int r;
        int g;
        int b;

        if (line == null) {
            return;
        }

        String shadeType = line.toLowerCase();
        String name = clause(shadeType, '(');
        String settings = remainder(shadeType, '(');
        StringTokenizer st = new StringTokenizer(settings, "(),");
        String[] values = new String[st.countTokens()];

        for (int i = 0; st.hasMoreTokens(); i++) {
            values[i] = st.nextToken();
        }

        if (name.equals("pen")) {
            try {
                if (_log.isDebugEnabled()) {
                    _log.debug("setting new pen " + settings);
                    _log.debug("width " + values[0]);
                }

                stroke.setWidth(new ExpressionLiteral(new Integer(values[0])));

                int pattern = Integer.parseInt(values[1]);

                if (_log.isDebugEnabled()) {
                    _log.debug("pattern = " + pattern);
                }

                stroke.setDashArray(MifStyles.getPenPattern(new Integer(pattern)));
                color = Integer.parseInt(values[2]);

                String rgb = Integer.toHexString(color);

                if (_log.isDebugEnabled()) {
                    _log.debug("color " + color + " -> " + rgb);
                }

                stroke.setColor(new ExpressionLiteral(rgb));
            } catch (Exception nfe) {
                throw new DataSourceException("Error setting up pen", nfe);
            }

            return;
        } else if (name.equals("brush")) {
            _log.debug("setting new brush " + settings);

            int pattern = Integer.parseInt(values[0]);
            _log.debug("pattern = " + pattern);

            DefaultGraphic dg = new DefaultGraphic();
            dg.addExternalGraphic(MifStyles.getBrushPattern(new Integer(pattern)));
            stroke.setGraphicFill(dg);
            color = Integer.parseInt(values[1]);

            String rgb = Integer.toHexString(color);
            _log.debug("color " + color + " -> " + rgb);
            fill.setColor(rgb); // foreground

            if (values.length == 3) { // optional parameter
                color = Integer.parseInt(values[2]);
                rgb = Integer.toHexString(color);
                _log.debug("color " + color + " -> " + rgb);

                fill.setBackgroundColor(rgb); // background
            } else {
                fill.setBackgroundColor((Expression) null);
            }
        } else if (name.equals("center")) {
            _log.debug("setting center " + settings);
        } else if (name.equals("smooth")) {
            _log.debug("setting smooth on");
        } else if (name.equals("symbol")) {
            _log.debug("setting symbol " + settings);

            Mark symbol = null;
            DefaultExternalGraphic eg = null;

            if (values.length == 3) { // version 3.0

                //symbol = symbols.get(new Integer(symNumb));
            } else if (values.length == 6) {}
            else if (values.length == 4) { // custom bitmap
                eg = new DefaultExternalGraphic();
                eg.setURI("CustSymb/" + values[0]);
            } else {
                _log.warn("unexpected symbol style " + name + settings);
            }
        } else if (name.equals("font")) {
            _log.debug("setting font " + settings);
        } else {
            _log.debug("unknown styling directive " + name + settings);
        }

        return;
    }

    /**
     * Test whether the given line contains a known shading clause keyword (PEN, STYLE, etc.)
     * 
     * @param line 
     * 
     * @return 
     */
    private boolean isShadingClause(String line) {
        line = line.toUpperCase();

        boolean ret = ((line.indexOf(CLAUSE_PEN) != -1) || (line.indexOf(CLAUSE_SYMBOL) != -1) || 
                      (line.indexOf(CLAUSE_SMOOTH) != -1) || 
                      (line.indexOf(CLAUSE_CENTER) != -1) || line.indexOf(CLAUSE_BRUSH) != -1);

        return ret;
    }

    /**
     * @see org.geotools.data.DataSource#importFeatures(FeatureCollection, Extent)
     */
    public void importFeatures(FeatureCollection ft, Extent ex)
                        throws DataSourceException {}

    /** Exports the features in the datasource into the provided feature collection
     * @param ft the feature collection to recieve the features
     * @param ex the extent of the features to be exported
     * this will soon become a filter
     * @throws DataSourceException if anything goes wrong
     *
     * @see org.geotools.data.DataSource#exportFeatures
     */
    public void exportFeatures(FeatureCollection ft, Extent ex)
                        throws DataSourceException {}

    /**
     * @see org.geotools.data.DataSource#stopLoading()
     */
    public void stopLoading() {}

    /**
     * @see org.geotools.data.DataSource#getExtent()
     * @return
     */
    public Extent getExtent() {
        return null;
    }

    /**
     * @see org.geotools.data.DataSource#getExtent(boolean)
     */
    public Extent getExtent(boolean speed) {
        return null;
    }

    /**
     * Loads features from the datasource into the passed collection, based on the passed filter.
     * Note that all data sources must support this method at a minimum.
     * 
     * @param collection The collection to put the features into.
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * 
     * @throws DataSourceException For all data source errors.
     */
    public void getFeatures(FeatureCollection collection, Filter filter)
                     throws DataSourceException {}

    /**
     * Loads features from the datasource into the returned collection, based on the passed filter.
     * 
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * 
     * @return Collection The collection to put the features into.
     * 
     * @throws DataSourceException For all data source errors.
     */
    public FeatureCollection getFeatures(Filter filter)
                                  throws DataSourceException {
        return null;
    }

    /**
     * Adds all features from the passed feature collection to the datasource.
     * 
     * @param collection The collection from which to add the features.
     * 
     * @throws DataSourceException If anything goes wrong or if exporting is not supported.
     */
    public void addFeatures(FeatureCollection collection)
                     throws DataSourceException {}

    /**
     * Stops this DataSource from loading.
     */
    public void abortLoading() {}

    /**
     * Gets the bounding box of this datasource using the default speed of this datasource as set
     * by the implementer.
     * 
     * @return The bounding box of the datasource or null if unknown and too expensive for the
     *         method to calculate.
     * 
     * @task REVISIT: Consider changing return of getBbox to Filter once Filters can be unpacked
     */
    public Envelope getBbox() {
        return null;
    }

    /**
     * Gets the bounding box of this datasource using the speed of this datasource as set by the
     * parameter.
     * 
     * @param speed If true then a quick (and possibly dirty) estimate of the extent is returned.
     *        If false then a slow but accurate extent will be returned
     * 
     * @return The extent of the datasource or null if unknown and too expensive for the method to
     *         calculate.
     * 
     * @task REVISIT:Consider changing return of getBbox to Filter once Filters can be unpacked
     */
    public Envelope getBbox(boolean speed) {
        return null;
    }

    /**
     * Modifies the passed attribute types with the passed objects in all features that correspond
     * to the passed OGS filter.  A convenience method for single attribute modifications.
     * 
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * 
     * @throws DataSourceException If modificaton is not supported, if the object type do not match
     *         the attribute type.
     */
    public void modifyFeatures(AttributeType type, Object value, Filter filter)
                        throws DataSourceException {}

    /**
     * Modifies the passed attribute types with the passed objects in all features that correspond
     * to the passed OGS filter.
     * 
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * 
     * @throws DataSourceException If modificaton is not supported, if the attribute and object
     *         arrays are not eqaul length, or if the object types do not match the attribute
     *         types.
     */
    public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter)
                        throws DataSourceException {}

    /**
     * Removes all of the features specificed by the passed filter from the collection.
     * 
     * @param filter An OpenGIS filter; specifies which features to remove.
     * 
     * @throws DataSourceException If anything goes wrong or if deleting is not supported.
     */
    public void removeFeatures(Filter filter) throws DataSourceException {}
}