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

import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.Extent;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.Feature;
import org.geotools.filter.Filter;

import org.geotools.feature.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.Vector;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.StringTokenizer;

import com.vividsolutions.jts.geom.*;

public class MapInfoDataSource implements DataSource {
    private static org.apache.log4j.Logger _log =
    org.apache.log4j.Logger.getLogger("MapInfoDataSource");
    // Header information
    String hVersion;
    String hCharset;
    String hDelimeter = "\t";
    Hashtable hColumns;
    Vector hUnique;
    Vector hIndex;
    // CoordsSys not supported
    // Transform not supported
    
    // Global variables (for the initial read)
    private String line;			// The current Line of the MIF file.
    private Vector pointFeatures;	// The point Features read
    private Vector lineFeatures;	// The line Features read
    private Vector polygonFeatures;	// The polygon Features read
    
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
    public static final String CLAUSE_SMOOTH= "SMOOTH";
    public static final String CLAUSE_CENTER = "CENTER";
    
    public static final String CLAUSE_VERSION = "Version";
    public static final String CLAUSE_CHARSET = "Charset";
    public static final String CLAUSE_DELIMETER = "DELIMITER";
    public static final String CLAUSE_UNIQUE = "UNIQUE";
    public static final String CLAUSE_INDEX = "INDEX";
    public static final String CLAUSE_COLUMNS = "COLUMNS";
    
    public MapInfoDataSource() {
        geomFactory = new GeometryFactory();
    }
    
    /** Reads the MIF and MID files and returns a Vector of the Features they contain
     */
    public Vector readMifMid(String filename) throws DataSourceException {
        if (filename==null)
            throw new DataSourceException("Invalid filename passed to readMifMid");
        
        String mifFile = setExtension(filename, "MIF");
        String midFile = setExtension(filename, "MID");
        
        // Read files
        try {
            Vector features = readMifMid(new BufferedReader(new FileReader(mifFile)), new BufferedReader(new FileReader(midFile)));
            return features;
        }
        catch(FileNotFoundException fnfexp) {
            throw new DataSourceException("FileNotFoundException trying to read mif file : "+fnfexp.getMessage());
        }
    }
    
    private String setExtension(String filename, String ext) {
        if (ext.indexOf(".")==-1)
            ext = "." + ext;
        if (filename.lastIndexOf(".")==-1)
            return filename+ext;
        return filename.substring(0, filename.lastIndexOf(".")) + ext;
    }
    
    /** This private method constructs the factories used to create the Feature, and Geometries as they are read
     * It takes it's setup values from the value of the COLUMNS clause in the MIF file
     */
    private void setUpFactories() throws DataSourceException {
        // Go through each column name, and set up an attribute for each one
        Vector colAttribs = new Vector();
        
        // Add attributes for each column
        Iterator it = hColumns.keySet().iterator();
        while (it.hasNext())
            colAttribs.add(new AttributeTypeDefault((String)it.next(), String.class));
        
        // Set up Point factory
        Vector pointAttribs = new Vector();
        // Add default attribute type
        pointAttribs.add(new AttributeTypeDefault("point", Geometry.class));
        // Add hColumn Attributes
        pointAttribs.addAll(colAttribs);
        // Add point-specific columns (SYMBOL_SHAPE, SYMBOL_COLOR, SYMBOL_SIZE)
/*		pointAttribs.add(new AttributeTypeDefault("SYMBOL_SHAPE", String.class));
    pointAttribs.add(new AttributeTypeDefault("SYMBOL_COLOR", String.class));
    pointAttribs.add(new AttributeTypeDefault("SYMBOL_SIZE", String.class));*/
        
        // create point feature Type & factory
        try {
            pointFeatureType = new FeatureTypeFlat((AttributeType[])pointAttribs.toArray(new AttributeType[pointAttribs.size()]));
            pointFactory = new FeatureFactory(pointFeatureType);
        } catch(SchemaException schexp) {
            throw new DataSourceException("SchemaException setting up point factory : "+schexp.getMessage());
        }
        
        // Set up Line factory
        Vector lineAttribs = new Vector();
        // Add default attribute type
        lineAttribs.add(new AttributeTypeDefault("line", Geometry.class));
        // Add hColumn Attributes
        lineAttribs.addAll(colAttribs);
        // Add line-specific columns (PEN_WIDTH, PEN_PATTERN, PEN_COLOR)
/*		lineAttribs.add(new AttributeTypeDefault("PEN_WIDTH", String.class));
    lineAttribs.add(new AttributeTypeDefault("PEN_PATTERN", String.class));
    lineAttribs.add(new AttributeTypeDefault("PEN_COLOR", String.class));*/
        
        // create line feature Type & factory
        try {
            lineFeatureType = new FeatureTypeFlat((AttributeType[])lineAttribs.toArray(new AttributeType[lineAttribs.size()]));
            lineFactory = new FeatureFactory(lineFeatureType);
        } catch(SchemaException schexp) {
            throw new DataSourceException("SchemaException setting up line factory : "+schexp.getMessage());
        }
        
        // Set up Polygon factory
        Vector polygonAttribs = new Vector();
        // Add default attribute type
        polygonAttribs.add(new AttributeTypeDefault("polygon", Geometry.class));
        // Add hColumn Attributes
        polygonAttribs.addAll(colAttribs);
        // Add polygon-specific columns (PEN_WIDTH, PEN_PATTERN, PEN_COLOR)
/*		polygonAttribs.add(new AttributeTypeDefault("PEN_WIDTH", String.class));
    polygonAttribs.add(new AttributeTypeDefault("PEN_PATTERN", String.class));
    polygonAttribs.add(new AttributeTypeDefault("PEN_COLOR", String.class));
    polygonAttribs.add(new AttributeTypeDefault("BRUSH_PATTERN", String.class));
    polygonAttribs.add(new AttributeTypeDefault("BRUSH_FORECOLOR", String.class));
    polygonAttribs.add(new AttributeTypeDefault("BRUSH_BACKCOLOR", String.class));
    polygonAttribs.add(new AttributeTypeDefault("CENTER_X", String.class));
    polygonAttribs.add(new AttributeTypeDefault("CENTER_Y", String.class));*/
        
        // create polygon feature Type & factory
        try {
            polygonFeatureType = new FeatureTypeFlat((AttributeType[])polygonAttribs.toArray(new AttributeType[polygonAttribs.size()]));
            polygonFactory = new FeatureFactory(polygonFeatureType);
        } catch(SchemaException schexp) {
            throw new DataSourceException("SchemaException setting up polygon factory : "+schexp.getMessage());
        }
        
    }
    
    /** Reads an entire MID/MIF file. (Two files, actually, separately opened)
     * @param mifReader An opened BufferedReader to the MIF file.
     * @param midReader An opened BufferedReader to the MID file.
     */
    private Vector readMifMid(BufferedReader mifReader, BufferedReader midReader) throws DataSourceException {
        // Read the MIF header
        readMifHeader(mifReader);
        
        // Set up factories
        setUpFactories();
        
        Vector features = new Vector();
        
        // Start by reading first line
        try {
            line = readMifLine(mifReader);
        }
        catch(IOException ioexp) {
            throw new DataSourceException("No data at start of file");
        }
        
        Feature feature;
        // Read each object in the MIF file
        while ((feature = readObject(mifReader, midReader))!=null) {
            // Figure out which type of feature it is
            
            // Add to relevent vector
            features.addElement(feature);
        }
        
        return features;
    }
    
    /** Reads the header from the given MIF file stream
     */
    private void readMifHeader(BufferedReader mifReader) throws DataSourceException {
        try {
            while (readMifLine(mifReader)!=null && !line.trim().equalsIgnoreCase("DATA")) {
                if (clause(line).equalsIgnoreCase(CLAUSE_VERSION)) {
                    // Read Version clause
                    hVersion = line.trim().substring(line.trim().indexOf(' ')).trim();
                }
                if (clause(line).equalsIgnoreCase(CLAUSE_CHARSET)) {
                    // Read Charset clause
                    hCharset = line.replace('\"',' ').trim().substring(line.trim().indexOf(' ')).trim();
                }
                if (clause(line).equalsIgnoreCase(CLAUSE_DELIMETER)) {
                    // Read Delimeter clause
                    hDelimeter = line.replace('\"',' ').trim().substring(line.trim().indexOf(' ')).trim();
                }
                if (clause(line).equalsIgnoreCase(CLAUSE_UNIQUE)) {
                    // Read Unique clause
                    StringTokenizer st = new StringTokenizer(line.trim().substring(line.trim().indexOf(' ')), ",");
                    hUnique = new Vector();
                    while (st.hasMoreTokens())
                        hUnique.addElement(st.nextToken());
                }
                if (clause(line).equalsIgnoreCase(CLAUSE_INDEX)) {
                    // Read Index clause
                    StringTokenizer st = new StringTokenizer(line.trim().substring(line.trim().indexOf(' ')), ",");
                    hIndex = new Vector();
                    while (st.hasMoreTokens())
                        hIndex.addElement(st.nextToken());
                }
                if (clause(line).equalsIgnoreCase(CLAUSE_COLUMNS)) {
                    // Read Columns clause
                    int cols =0;
                    try {
                        cols = Integer.parseInt(line.trim().substring(line.trim().indexOf(' ')));
                    } catch(NumberFormatException nfexp) {}
                    // Read each of the columns
                    hColumns = new Hashtable();
                    for (int i=0;i<cols;i++) {
                        line = readMifLine(mifReader);
                        StringTokenizer st = new StringTokenizer(line.trim().substring(line.trim().indexOf(' ')), " ");
                        
                        hColumns.put(st.nextToken(), st.nextToken());
                    }
                }
            }
        }
        catch(IOException ioexp) {
            throw new DataSourceException("IOException reading MIF header : "+ioexp.getMessage());
        }
    }
    
    /** A 'Clause' is stored as a single string at the start of a line. This rips the clause name out of the given line.
     */
    private String clause(String line) {
        if (line.trim().indexOf(' ')==-1)
            return line.trim();
        else
            return line.trim().substring(0, line.trim().indexOf(' '));
    }
    
    /** Reads the next line in the reader, ignoring lines which are nothing but whitespace. Sets the global 'line' variable to the currently read line
     */
    private String readMifLine(BufferedReader reader) throws IOException {
        do {
            line = reader.readLine();
            if (line==null){
                return null;
            }
            if(line.startsWith("BRUSH") || line.startsWith("PEN") || line.startsWith("SMOOTH")){
                line=" ";
            }
        }
        while (line.trim().length()==0);
        
        line = line.trim();
        
        return line;
    }
    
    /** Reads a single MIF Object (Point, Line, Region, etc.) as a Feature
     */
    private Feature readObject(BufferedReader mifReader, BufferedReader midReader) throws DataSourceException {
        Feature feature = null;
        _log.debug("line = " + line);
        // examine The current line
        if (line==null)
            return null;
        int index = line.indexOf(' ');
        if(index == -1) {
            index = line.length();
        }
        if (line.substring(0, index ).equalsIgnoreCase(TYPE_POINT)) {
            // Read point data
            _log.debug("Reading POINT");
            feature = readPointObject(mifReader, midReader);
        }
        else if (line.substring(0, index).equalsIgnoreCase(TYPE_LINE)) {
            // Read line data
            _log.debug("Reading LINE");
            feature = readLineObject(mifReader, midReader);
        }
        else if (line.substring(0, index).equalsIgnoreCase(TYPE_PLINE)) {
            // Read pline data
            _log.debug("Reading PLINE");
            feature = readPLineObject(mifReader, midReader);
        }
        else if (line.substring(0, index).equalsIgnoreCase(TYPE_REGION)) {
            // Read region data
            _log.debug("Reading REGION");
            feature = readRegionObject(mifReader, midReader);
        }
        
        return feature;
    }
    
    /** Reads Point information from the MIF stream
     */
    private Feature readPointObject(BufferedReader mifReader, BufferedReader midReader) throws DataSourceException {
        Feature feature = null;
        
        StringTokenizer st = new StringTokenizer(line.substring(line.indexOf(" ")), ",");
        try {
            double x = Double.parseDouble(st.nextToken());
            double y = Double.parseDouble(st.nextToken());
            // Construct Geomtry
            Geometry pointGeom = geomFactory.createPoint(new Coordinate(x, y));
            // Read next line
            readMifLine(mifReader);
            Hashtable shading = readShading(mifReader);
            // Shading is not included, as null feature attributes are not supported yet
            Hashtable midValues = readMid(midReader);
            //			midValues.putAll(shading);
            // Create Feature
            feature = buildFeature(pointFeatureType, pointFactory, pointGeom, midValues);
            _log.debug("Built point feature : "+x+" "+y);
        }
        catch(NumberFormatException nfexp) {
            throw new DataSourceException("Exception reading Point data from MIF file : "+nfexp.getMessage());
        }
        catch(IOException ioexp) {
            throw new DataSourceException("IOException reading point data : "+ioexp.getMessage());
        }
        return feature;
    }
    
    /** Reads Line information from the MIF stream
     */
    private Feature readLineObject(BufferedReader mifReader, BufferedReader midReader) throws DataSourceException {
        Feature feature = null;
        
        StringTokenizer st = new StringTokenizer(line.substring(line.indexOf(" ")), ",");
        try {
            double x1 = Double.parseDouble(st.nextToken());
            double y1 = Double.parseDouble(st.nextToken());
            double x2 = Double.parseDouble(st.nextToken());
            double y2 = Double.parseDouble(st.nextToken());
            // Construct Geomtry
            Coordinate [] cPoints = new Coordinate[2];
            cPoints[0] = new Coordinate(x1, y1);
            cPoints[1] = new Coordinate(x2, y2);
            Geometry lineGeom = geomFactory.createLineString(cPoints);
            // Read next line
            readMifLine(mifReader);
            Hashtable shading = readShading(mifReader);
            // Shading is not included, as null feature attributes are not supported yet
            Hashtable midValues = readMid(midReader);
            //			midValues.putAll(shading);
            // Create Feature
            feature = buildFeature(lineFeatureType, lineFactory, lineGeom, midValues);
            _log.debug("Built line feature : "+x1+" "+y1+" - "+x2+" "+y2);
        }
        catch(NumberFormatException nfexp) {
            throw new DataSourceException("Exception reading Point data from MIF file : "+nfexp.getMessage());
        }
        catch(IOException ioexp) {
            throw new DataSourceException("IOException reading point data : "+ioexp.getMessage());
        }
        return feature;
    }
    
    /** Reads Multi-Line (PLine) information from the MIF stream
     */
    private Feature readPLineObject(BufferedReader mifReader, BufferedReader midReader) throws DataSourceException {
        Feature feature = null;
        
        StringTokenizer st = new StringTokenizer(line.substring(line.indexOf(" ")));
        try {
            int numsections = 1;
            if (st.hasMoreTokens() && st.nextToken().trim().equalsIgnoreCase("MULTIPLE"))
                numsections = Integer.parseInt(st.nextToken());
            
            // A vector of coordinates
            Vector coords = new Vector();
            // Read each polygon
            for (int i=0;i<numsections;i++) {
                // Read line (number of points
                int numpoints = Integer.parseInt(readMifLine(mifReader));
                // Read each point
                for (int p=0;p<numpoints;p++) {
                    StringTokenizer pst = new StringTokenizer(readMifLine(mifReader));
                    double x = Double.parseDouble(pst.nextToken());
                    double y = Double.parseDouble(pst.nextToken());
                    coords.addElement(new Coordinate(x, y));
                }
            }
            Geometry plineGeom = geomFactory.createLineString((Coordinate[])coords.toArray(new Coordinate[coords.size()]));
            // Read next line
            readMifLine(mifReader);
            Hashtable shading = readShading(mifReader);
            // Shading is not included, as null feature attributes are not supported yet
            Hashtable midValues = readMid(midReader);
            //			midValues.putAll(shading);
            // Create Feature
            feature = buildFeature(lineFeatureType, lineFactory, plineGeom, midValues);
            _log.debug("Read polyline ("+coords.size()+")");
        }
        catch(NumberFormatException nfexp) {
            throw new DataSourceException("Exception reading Point data from MIF file : "+nfexp.getMessage());
        }
        catch(IOException ioexp) {
            throw new DataSourceException("IOException reading point data : "+ioexp.getMessage());
        }
        return feature;
    }
    
    /** Reads Region (Polygon) information from the MIF stream
     */
    private Feature readRegionObject(BufferedReader mifReader, BufferedReader midReader) throws DataSourceException {
        Feature feature = null;
        
        StringTokenizer st = new StringTokenizer(line.substring(line.indexOf(" ")));
        try {
            int numpolygons = Integer.parseInt(st.nextToken());
            
            // A vector of polygons
            Vector polys = new Vector();
            // Read each polygon
            for (int i=0;i<numpolygons;i++) {
                Vector coords = new Vector();
                // Read number of points
                int numpoints = Integer.parseInt(readMifLine(mifReader));
                // Read each point
                for (int p=0;p<numpoints;p++) {
                    StringTokenizer pst = new StringTokenizer(readMifLine(mifReader));
                    double x = Double.parseDouble(pst.nextToken());
                    double y = Double.parseDouble(pst.nextToken());
                    coords.addElement(new Coordinate(x, y));
                }
                // Create polygon from points
                coords.addElement(new Coordinate(((Coordinate)coords.get(0)).x, ((Coordinate)coords.get(0)).y));
                try {
                    Polygon pol = geomFactory.createPolygon(geomFactory.createLinearRing((Coordinate[])coords.toArray(new Coordinate[coords.size()])), null);
                    // Add to vector
                    polys.addElement(pol);
                }
                catch(TopologyException topexp) {
                    throw new DataSourceException("TopologyException reading Region polygon : "+topexp.getMessage());
                }
            }
            Geometry polyGeom = geomFactory.createMultiPolygon((Polygon[])polys.toArray(new Polygon[polys.size()]));
            // Read next line
            readMifLine(mifReader);
            
            Hashtable shading = readShading(mifReader);
            // Shading is not included, as null feature attributes are not supported yet
            Hashtable midValues = readMid(midReader);
            //			midValues.putAll(shading);
            // Create Feature
            feature = buildFeature(polygonFeatureType, polygonFactory, polyGeom, midValues);
            _log.debug("Read Region ("+polys.size()+")");
        }
        catch(NumberFormatException nfexp) {
            throw new DataSourceException("Exception reading Point data from MIF file : "+nfexp.getMessage());
        }
        catch(IOException ioexp) {
            throw new DataSourceException("IOException reading point data : "+ioexp.getMessage());
        }
        return feature;
    }
    
    /** Builds a complete Feature object using the given FeatureType, with the Geometry geom, and the given attributes.
     * @param type The FeatureType to use to constuct the Feature
     * @param geom The Geometry to use as the default Geometry
     * @param attribs The attibutes to use as the Feature's attributes (Attributes must be set up in the FeatureType)
     * @return A fully-formed Feature
     */
    private Feature buildFeature(FeatureType featureType, FeatureFactory factory, Geometry geom, Hashtable attribs) throws DataSourceException {
        // Create array of rows (attribs + geometry)
        Object [] rows = new Object[featureType.getAllAttributeTypes().length];
        
        // Place each object in the attribs hashtable into the relevent row
        int numAttribs = featureType.getAllAttributeTypes().length;
        for (int i=0;i<numAttribs;i++)
            rows[i] = attribs.get(featureType.getAttributeType(i).getName());
        
        // Fill in geometry
        rows[0] = geom;
        
        // Create Feature
        try {
            return factory.create(rows);
        }
        catch(IllegalFeatureException ifexp) {
            throw new DataSourceException("IllegalFeatureException creating feature : "+ifexp.getMessage());
        }
    }
    
    /** Reads a single line of the given MID file stream, and returns a hashtable of the data in it, keyed byt he keys in the hColumns hash
     */
    private Hashtable readMid(BufferedReader midReader) throws DataSourceException {
        Vector midValues = new Vector();
        if (midReader==null)
            return new Hashtable();
        // The delimeter is a single delimiting character
        String midLine = "";
        try {
            midLine = midReader.readLine();
            _log.debug("Read MID "+midLine);
        }
        catch(IOException ioexp) {
            throw new DataSourceException("IOException reading MID file");
        }
        // read MID tokens
        StringTokenizer quotes = new StringTokenizer(midLine, "\"");
        while (quotes.hasMoreTokens()) {
            StringTokenizer delimeters = new StringTokenizer(quotes.nextToken(), hDelimeter);
            // Read each delimited value into the Vector
            while (delimeters.hasMoreTokens())
                midValues.addElement(delimeters.nextToken());
            // Store the whole of the next bit (it's a quoted string)
            if (quotes.hasMoreTokens())
                midValues.addElement(quotes.nextToken());
        }
        
        // Place the mid values into a Hashtable
        Hashtable hValues = (Hashtable)hColumns.clone();
        Iterator it = hValues.keySet().iterator();
        int index = 0;
        while (it.hasNext())
            hValues.put(it.next(), midValues.elementAt(index++));
        return hValues;
    }
    
    /** Reads the shading information at the end of Object data
     */
    private Hashtable readShading(BufferedReader mifReader) throws IOException {
        Hashtable shading = new Hashtable();
        
        if (line==null)
            return shading;
        
        String shadeType = line.toLowerCase();
        while (shadeType!=null && isShadingClause(shadeType)) {
            // Get clause name
            String name = clause(shadeType);
            _log.debug("Read shading ("+name+")");
            shading.put(name, shadeType.substring(name.length()).trim());
            shadeType = readMifLine(mifReader);
        }
        
        return shading;
    }
    
    /** Test whether the given line contains a known shading clause keyword (PEN, STYLE, etc.)
     */
    private boolean isShadingClause(String line) {
        return (line.indexOf(CLAUSE_PEN.toLowerCase())!=-1 || line.indexOf(CLAUSE_SYMBOL.toLowerCase())!=-1 || line.indexOf(CLAUSE_SMOOTH.toLowerCase())!=-1 || line.indexOf(CLAUSE_CENTER.toLowerCase())!=-1);
    }
    
    /**
     * @see DataSource#importFeatures(FeatureCollection, Extent)
     */
    public void importFeatures(FeatureCollection ft, Extent ex)
    throws DataSourceException {
    }
    
    /**
     * @see DataSource#exportFeatures(FeatureCollection, Extent)
     */
    public void exportFeatures(FeatureCollection ft, Extent ex)
    throws DataSourceException {
    }
    
    /**
     * @see DataSource#stopLoading()
     */
    public void stopLoading() {
    }
    
    /**
     * @see DataSource#getExtent()
     */
    public Extent getExtent() {
        return null;
    }
    
    /**
     * @see DataSource#getExtent(boolean)
     */
    public Extent getExtent(boolean speed) {
        return null;
    }
    
    /** Loads features from the datasource into the passed collection, based on
     * the passed filter.  Note that all data sources must support this method
     * at a minimum.
     *
     * @param collection The collection to put the features into.
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @throws DataSourceException For all data source errors.
     */
    public void getFeatures(FeatureCollection collection, Filter filter) throws DataSourceException {
    }
    
    /** Loads features from the datasource into the returned collection, based on
     * the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @return Collection The collection to put the features into.
     * @throws DataSourceException For all data source errors.
     */
    public FeatureCollection getFeatures(Filter filter) throws DataSourceException {
        return null;
    }
    
    /** Adds all features from the passed feature collection to the datasource.
     *
     * @param collection The collection from which to add the features.
     * @throws DataSourceException If anything goes wrong or if exporting is
     * not supported.
     */
    public void addFeatures(FeatureCollection collection) throws DataSourceException {
    }
    
    /** Stops this DataSource from loading.
     */
    public void abortLoading() {
    }
    
    /** Gets the bounding box of this datasource using the default speed of
     * this datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     * expensive for the method to calculate.
     * @task REVISIT: Consider changing return of getBbox to Filter once Filters can be unpacked
     */
    public Envelope getBbox() {
        return null;
    }
    
    /** Gets the bounding box of this datasource using the speed of
     * this datasource as set by the parameter.
     *
     * @param speed If true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but accurate extent
     * will be returned
     * @return The extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     * @task REVISIT:Consider changing return of getBbox to Filter once Filters can be unpacked
     */
    public Envelope getBbox(boolean speed) {
        return null;
    }
    
    /** Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.  A convenience
     * method for single attribute modifications.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the object type do not match the attribute type.
     */
    public void modifyFeatures(AttributeType type, Object value, Filter filter) throws DataSourceException {
    }
    
    /** Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the attribute and object arrays are not eqaul length, or if the object
     * types do not match the attribute types.
     */
    public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter) throws DataSourceException {
    }
    
    /** Removes all of the features specificed by the passed filter from the
     * collection.
     *
     * @param filter An OpenGIS filter; specifies which features to remove.
     * @throws DataSourceException If anything goes wrong or if deleting is
     * not supported.
     */
    public void removeFeatures(Filter filter) throws DataSourceException {
    }
    
}

