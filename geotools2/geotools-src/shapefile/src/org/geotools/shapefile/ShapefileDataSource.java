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
package org.geotools.shapefile;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.algorithm.*;

import org.geotools.data.DataSourceException;

import org.geotools.feature.*;
import org.geotools.feature.AttributeType;

import org.geotools.filter.Filter;

import org.geotools.shapefile.dbf.*;
import org.geotools.shapefile.shapefile.*;

import java.io.IOException;

import java.net.URL;
import java.util.*;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;


/**
 * @version $Id: ShapefileDataSource.java,v 1.20 2003/03/04 21:22:52 ianschneider Exp $
 * @author James Macgill, CCG
 * @task TODO: add support for reading dbf file
 * @task TODO: add support for the optional spatial index files to improve
 *             loading of sub regions
 */
public class ShapefileDataSource implements org.geotools.data.DataSource {
    /**
     * The logger for this module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.shapefile");
    
    /**
     * The shapefile object that this datasouce connects to
     **/
    private org.geotools.shapefile.shapefile.Shapefile shapefile;
    private URL shpURL;
    private URL dbfURL;
    private URL shxURL;
    
    public ShapefileDataSource(java.net.URL url) {
        try {
            String filename = java.net.URLDecoder.decode(url.getFile(), "UTF-8");
            LOGGER.fine("filename part of shapefile is " + filename);
            
            String shpext = ".shp";
            String dbfext = ".dbf";
            String shxext = ".shx";
            
            if(filename.endsWith(".shp") || filename.endsWith(".dbf") || filename.endsWith(".shx")) {
                
                filename = filename.substring(0, filename.length() - 4);
            }
            if(filename.endsWith(".SHP") || filename.endsWith(".DBF") || filename.endsWith(".SHX")) {
                
                
                filename = filename.substring(0, filename.length() - 4);
                shpext = ".SHP";
                dbfext = ".DBF";
                shxext = ".SHX";
            }
            
            shpURL = new URL(url, filename + shpext);
            dbfURL = new URL(url, filename + dbfext);
            shxURL = new URL(url, filename + shxext);
            
            shapefile = new Shapefile(shpURL);
            LOGGER.fine("dbf url constructed as " + dbfURL);
        } catch(Exception ioe) {
            LOGGER.warning("Unable to construct URL for shapefile " + ioe);
        }
    }
    
    
    /** Creates a new instance of ShapefileDataSource.
     * @deprecated Use URL constructor instead.
     */
    public ShapefileDataSource(Shapefile shapefile) {
        this.shapefile = shapefile;
    }
    
    /**
     * Gets the Column names (used by FeatureTable) for this DataSource.
     */
    public String[] getColumnNames() throws ShapefileException {
        // open shapefile header and get geometry description
        String[] colNames = null;
        
        try {
            ShapefileHeader header = shapefile.getHeader();
            String geomName = Shapefile.getShapeTypeDescription(header.getShapeType());
            
            // open the dbf file and get type names
            DbaseFileReader dbf = null;
            String dbfFilePath = dbfURL.getFile();
            
            if(dbfFilePath != null) {
                dbf = new DbaseFileReader(dbfURL.getFile());
                
                int numFields = dbf.getNumFields();
                colNames = new String[numFields + 1];
                colNames[0] = geomName;
                
                for(int i = 0; i < numFields; i++) {
                    colNames[i + 1] = dbf.getFieldName(i);
                }
            } else {
                colNames = new String[1];
                colNames[0] = geomName;
            }
        } catch(Exception e) {
            throw new ShapefileException("Error while querying column names", e);
        }
        
        return colNames;
    }
    
    
    /** Stops this DataSource from loading.
     */
    public void abortLoading() {}
    
    
    /** Adds all features from the passed feature collection to the datasource.
     *
     * @param collection The collection from which to add the features.
     * @throws DataSourceException If anything goes wrong or if exporting is
     * not supported.
     * @task TODO: Implement addFeatures method
     */
    public void addFeatures(org.geotools.feature.FeatureCollection collection)
    throws DataSourceException {
        throw new DataSourceException("Removal of features is not supported by this datasource");
    }
    
    
    /** Gets the bounding box of this datasource using the default speed of
     * this datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public com.vividsolutions.jts.geom.Envelope getBbox() {
        com.vividsolutions.jts.geom.Envelope bounds = null;
        
        try {
            bounds = shapefile.getBounds();
        } catch(Exception e) {}
        
        return bounds;
    }
    
    
    /** Gets the bounding box of this datasource using the speed of
     * this datasource as set by the parameter.
     *
     * @param speed If true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but accurate extent
     * will be returned
     * @return The extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public com.vividsolutions.jts.geom.Envelope getBbox(boolean speed) {
        return getBbox();
    }
    
    
    /**
     * Loads features from the datasource into the returned collection, based
     * on the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @return Collection The collection to put the features into.
     * @throws DataSourceException For all data source errors.
     */
    public org.geotools.feature.FeatureCollection getFeatures(Filter filter)
    throws DataSourceException {
        org.geotools.feature.FeatureCollectionDefault fc = new org.geotools.feature.FeatureCollectionDefault();
        getFeatures(fc, filter);
        
        return fc;
    }
    
    
    /**
     * Loads features from the datasource into the passed collection, based
     * on the passed filter.  Note that all data sources must support this
     * method at a minimum.
     *
     * @param collection The collection to put the features into.
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @throws DataSourceException For all data source errors.
     */
    public void getFeatures(org.geotools.feature.FeatureCollection collection, Filter filter)
    throws DataSourceException {
        LOGGER.entering("ShapefileDataSource", "getFeatures");
        
        try {
            // open shapefile header and guess file type
            ShapefileHeader header = shapefile.getHeader();
           AttributeType geometryAttribute = new AttributeTypeDefault(Shapefile.getShapeTypeDescription(
            header.getShapeType()), com.vividsolutions.jts.geom.Geometry.class);
            
            // open dbf file
            DbaseFileReader dbf = null;
            if(dbfURL != null) {
                String dbfFilePath = dbfURL.getFile();

                if(dbfFilePath != null) {
                    dbf = new DbaseFileReader(dbfURL.getFile());
                }
            }
            
            // build the FeatureType
            org.geotools.feature.FeatureType shapefileType;
            java.util.ArrayList attribs = new java.util.ArrayList();
            attribs.add(geometryAttribute);
            
            if(dbf != null) {
                LOGGER.fine("Reading from dbf");
                
                char[] ctype = new char[0];
                
                for(int i = 0; i < dbf.getNumFields(); i++) {
                    String name = dbf.getFieldName(i);
                    Class type = dbf.getFieldType(i);
                    attribs.add(new AttributeTypeDefault(name, type));
                }
            }             
            
            AttributeType[] types = (AttributeType[]) attribs.toArray(new AttributeType[0]);
            shapefileType = new org.geotools.feature.FeatureTypeFlat(types);
            
            LOGGER.fine("Schema is " + shapefileType);
            
            
            // Load the features
            org.geotools.feature.FeatureFactory fac = new org.geotools.feature.FeatureFactory(shapefileType);
            GeometryCollection shapes = shapefile.read(new GeometryFactory());
            List features = new java.util.ArrayList();
            int count = shapes.getNumGeometries();
            
            for(int i = 0; i < count; i++) {
                Object[] row;
                
                if(dbf == null) {
                    row = new Object[1];
                } else {
                    row = new Object[dbf.getNumFields() + 1]; // +1 for geomety
                    dbf.read(row, 1);
                }
                
                row[0] = (com.vividsolutions.jts.geom.Geometry) shapes.getGeometryN(i);
                
                org.geotools.feature.Feature feature = fac.create(row);
                
                if(filter == null || filter.contains(feature)) {
                    collection.addFeatures(new org.geotools.feature.Feature[] { feature });
                }
            }
            
            if(dbf != null)
                dbf.close();
        } catch(java.io.IOException ioe) {
            ioe.printStackTrace();
            throw new DataSourceException("IO Exception loading data", ioe);
        } catch(ShapefileException se) {
            throw new DataSourceException("Shapefile Exception loading data", se);
        } catch(com.vividsolutions.jts.geom.TopologyException te) {
            throw new DataSourceException("Topology Exception loading data", te);
        } catch(org.geotools.feature.IllegalFeatureException ife) {
            throw new DataSourceException("Illegal Feature Exception loading data", ife);
        } catch(org.geotools.feature.SchemaException se) {
            throw new DataSourceException("Error building feature schema", se);
        }
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
     * @task TODO: Implement support for modification of features (single attribute)
     */
    public void modifyFeatures(AttributeType type, Object value, Filter filter)
    throws DataSourceException {
        throw new DataSourceException(
        "Modification of features is not yet supported by this datasource");
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
     * @task TODO: Implement support for modification of feature (multi attribute)
     */
    public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter)
    throws DataSourceException {
        throw new DataSourceException(
        "Modification of features is not yet supported by this datasource");
    }
    
    
    /** Removes all of the features specificed by the passed filter from the
     * collection.
     *
     * @param filter An OpenGIS filter; specifies which features to remove.
     * @throws DataSourceException If anything goes wrong or if deleting is
     * not supported.
     * @task TODO: Implement support for removal of features
     */
    public void removeFeatures(Filter filter) throws DataSourceException {
        throw new DataSourceException("Removal of features is not yet supported by this datasource");
    }
    
    
    /**
     * Overwrites the file writing the feature passed as parameters
     * @param collection - the collection to be written
     */
    public void setFeatures(org.geotools.feature.FeatureCollection collection)
    throws DataSourceException {
        LOGGER.entering("ShapefileDataSource", "setFeatures");
        
        try {
            // create a good geometry collection
            // this gc will be a collection of either multi-points, multi-polygons, or multi-linestrings
            // polygons will have the rings in the correct order
            GeometryCollection gc = makeShapeGeometryCollection(collection);
            
            // guess shape dimensions
            int shapeDims = 2;
            if(gc.getNumGeometries() > 0)
                shapeDims = guessCoorinateDims(gc.getGeometryN(0));
            
            // write shapefile
            Shapefile shapefile = new Shapefile(shpURL);
            shapefile.write(gc, shapeDims);
            
            // write index file
            shapefile.writeIndex(gc, shxURL, shapeDims);
            
            // open dbf file and write it
            DbaseFileWriter dbf = null;
            String dbfFilePath = dbfURL.getFile();
            writeDbf(collection, dbfFilePath);
        } catch(Exception e) {
            throw new DataSourceException("Something went wrong during shapefile saving", e);
        }
    }
    
    
    /**
     * Write a dbf file with the information from the featureCollection.
     * @param featureCollection column data from collection
     * @param fname name of the dbf file to write to
     */
    private void writeDbf(FeatureCollection featureCollection, String filePath)
    throws Exception {
        // precondition: all features have the same schema
        Feature[] features = featureCollection.getFeatures();
        AttributeType[] types = features[0].getSchema().getAttributeTypes();
        
        // compute how many supported attributes are there.
        // TODO: handle Calendar, BigDecimal and BigInteger as well
        int numAttributes = 0;
        
        for(int i = 0; i < types.length; i++) {
            Class currType = types[i].getType();
            
            if((currType == String.class) || (currType == Boolean.class) ||
            (currType == Integer.class) || (currType == Long.class) ||
            (currType == Byte.class) || (currType == Short.class) ||
            (currType == Double.class) || (currType == Float.class) ||
            Date.class.isAssignableFrom(currType))
                numAttributes++;
            else if(Geometry.class.isAssignableFrom(currType)) {
                // do nothing
            } else {
                throw new DataSourceException("Shapefile: unsupported type found in feature schema");
            }
        }
        
        DbaseFileHeader header = new DbaseFileHeader();
        
        for(int i = 0; i < types.length; i++) {
            Class colType = types[i].getType();
            String colName = types[i].getName();
            
            if((colType == Integer.class) || (colType == Short.class) || (colType == Byte.class)) {
                header.addColumn(colName, 'N', 16, 0);
            } else if((colType == Double.class) || (colType == Float.class)) {
                header.addColumn(colName, 'N', 33, 16);
            } else if(Date.class.isAssignableFrom(colType)) {
                header.addColumn(colName, 'D', 8, 0);
            } else if(colType == String.class) {
                int maxlength = findMaxStringLength(featureCollection, i);
                
                if(maxlength > 255) {
                    throw new DataSourceException(
                    "Shapefile does not support strings longer than 255 characters");
                }
                
                header.addColumn(colName, 'C', maxlength, 0);
            }
        }
        header.setNumRecords(features.length);
        
        // write header
        DbaseFileWriter dbf = new DbaseFileWriter(filePath, header);
        
        // write rows. Prepare calendar object for null dates
        Calendar nullCal = Calendar.getInstance();
        nullCal.clear();
        for(int i = 0; i < features.length; i++) {
            Feature feature = features[i];
            Object[] DBFrow = new Object[numAttributes];
            Object[] atts = feature.getAttributes();
            
            // make data for each column in this feature (row)
            int f = 0;
            for(int j = 0; j < atts.length; j++) {
                Class colType = types[j].getType();
                
                if(colType == Integer.class) {
                    if(atts[j] == null) {
                        DBFrow[f] = new Integer(0);
                    } else {
                        DBFrow[f] = atts[j];
                    }
                    f++;
                    
                } else if((colType == Short.class) || (colType == Byte.class)) {
                    if(atts[j] == null) {
                        DBFrow[f] = new Integer(0);
                    } else {
                        DBFrow[f] = new Integer(((Number) atts[j]).intValue());
                    }
                    f++;
                    
                } else if(colType == Double.class) {
                    if(atts[j] == null) {
                        DBFrow[f] = new Double(0.0);
                    } else {
                        DBFrow[f] = atts[j];
                    }
                    f++;
                    
                } else if(colType == Float.class) {
                    if(atts[j] == null) {
                        DBFrow[f] = new Double(0.0);
                    } else {
                        DBFrow[f] = new Double(((Number) atts[j]).doubleValue());
                    }
                    f++;
                    
                } else if(colType == String.class) {
                    if(atts[j] == null) {
                        DBFrow[f] = new String("");
                    } else {
                        if(atts[j] instanceof String)
                            DBFrow[f] = atts[j];
                        else
                            DBFrow[f] = atts[j].toString();
                    }
                    f++;
                } else if(Date.class.isAssignableFrom(colType)) {
                    if(atts[j] == null) {
                        DBFrow[f] = nullCal.getTime();
                    } else {
                        if(atts[j] instanceof Date)
                            DBFrow[f] = atts[j];
                    }
                    f++;
                }
                
            }
            dbf.write(DBFrow);
        }
        
        dbf.close();
    }
    
    
    /**
     *look at all the data in the column of the featurecollection, and find the largest string!
     *@param fc features to look at
     *@param attributeNumber which of the column to test.
     */
    private int findMaxStringLength(FeatureCollection fc, int attributeNumber) {
        Feature[] features = fc.getFeatures();
        
        int maxlen = 0;
        
        for(int i = 0; i < features.length; i++) {
            String s = (String) (features[i].getAttributes())[attributeNumber];
            int len = s.length();
            
            if(len > maxlen) {
                maxlen = len;
            }
        }
        
        return maxlen;
    }
    
    
    /**
     * Find the generic geometry type of the feature collection.
     * Simple method - find the 1st non null geometry and its type
     *  is the generic type.
     * returns 0 - all empty/invalid <br>
     *         1 - point <br>
     *         2 - line <br>
     *         3 - polygon <br>
     *         4 - multipoint <br>
     *@param fc feature collection containing tet geometries.
     **/
    int findBestGeometryType(FeatureCollection fc) {
        Feature[] features = fc.getFeatures();
        Geometry geom = features[0].getDefaultGeometry();
        
        if(geom instanceof Point)
            return 1;
        
        if(geom instanceof MultiPoint)
            return 4;
        
        if(geom instanceof Polygon)
            return 3;
        
        if(geom instanceof MultiPolygon)
            return 3;
        
        if(geom instanceof LineString)
            return 2;
        
        if(geom instanceof MultiLineString)
            return 2;
        
        return 0;
    }
    
    
    /**
     *  reverses the order of points in lr (is CW -> CCW or CCW->CW)
     */
    LinearRing reverseRing(LinearRing lr) {
        int numPoints = lr.getNumPoints();
        Coordinate[] newCoords = new Coordinate[numPoints];
        
        for(int t = 0; t < numPoints; t++) {
            newCoords[t] = lr.getCoordinateN(numPoints - t - 1);
        }
        
        return new LinearRing(newCoords, new PrecisionModel(), 0);
    }
    
    
    /**
     * make sure outer ring is CCW and holes are CW
     *@param p polygon to check
     */
    Polygon makeGoodShapePolygon(Polygon p) {
        LinearRing outer;
        LinearRing[] holes = new LinearRing[p.getNumInteriorRing()];
        Coordinate[] coords;
        CGAlgorithms cga = new RobustCGAlgorithms();
        
        coords = p.getExteriorRing().getCoordinates();
        
        if(cga.isCCW(coords)) {
            outer = reverseRing((LinearRing) p.getExteriorRing());
        } else {
            outer = (LinearRing) p.getExteriorRing();
        }
        
        for(int t = 0; t < p.getNumInteriorRing(); t++) {
            coords = p.getInteriorRingN(t).getCoordinates();
            
            if(!(cga.isCCW(coords))) {
                holes[t] = reverseRing((LinearRing) p.getInteriorRingN(t));
            } else {
                holes[t] = (LinearRing) p.getInteriorRingN(t);
            }
        }
        
        return new Polygon(outer, holes, new PrecisionModel(), 0);
    }
    
    
    /**
     * make sure outer ring is CCW and holes are CW for all the polygons in the Geometry
     *@param mp set of polygons to check
     */
    MultiPolygon makeGoodShapeMultiPolygon(MultiPolygon mp) {
        MultiPolygon result;
        Polygon[] ps = new Polygon[mp.getNumGeometries()];
        
        //check each sub-polygon
        for(int t = 0; t < mp.getNumGeometries(); t++) {
            ps[t] = makeGoodShapePolygon((Polygon) mp.getGeometryN(t));
        }
        
        result = new MultiPolygon(ps, new PrecisionModel(), 0);
        
        return result;
    }
    
    
    /**
     * return a single geometry collection <Br>
     *  result.GeometryN(i) = the i-th feature in the FeatureCollection<br>
     *   All the geometry types will be the same type (ie. all polygons) - or they will be set to<br>
     *     NULL geometries<br>
     *<br>
     * GeometryN(i) = {Multipoint,Multilinestring, or Multipolygon)<br>
     *
     *@param fc feature collection to make homogeneous
     */
    public GeometryCollection makeShapeGeometryCollection(FeatureCollection fc)
    throws Exception {
        GeometryCollection result;
        Feature[] features = fc.getFeatures();
        Geometry[] allGeoms = new Geometry[features.length];
        
        int geomtype = findBestGeometryType(fc);
        
        if(geomtype == 0) {
            throw new Exception(
            "Could not determine shapefile type - data is either all GeometryCollections or empty");
        }
        
        for(int t = 0; t < features.length; t++) {
            Geometry geom;
            geom = features[t].getDefaultGeometry();
            
            switch(geomtype) {
                case 1: //point
                    
                    if((geom instanceof Point)) {
                        allGeoms[t] = geom;
                    } else {
                        allGeoms[t] = new MultiPoint(null, new PrecisionModel(), 0);
                    }
                    
                    break;
                    
                case 2: //line
                    
                    if((geom instanceof LineString)) {
                        LineString[] l = new LineString[1];
                        l[0] = (LineString) geom;
                        
                        allGeoms[t] = new MultiLineString(l, new PrecisionModel(), 0);
                    } else if(geom instanceof MultiLineString) {
                        allGeoms[t] = geom;
                    } else {
                        allGeoms[t] = new MultiLineString(null, new PrecisionModel(), 0);
                    }
                    
                    break;
                    
                case 3: //polygon
                    
                    if(geom instanceof Polygon) {
                        //good!
                        Polygon[] p = new Polygon[1];
                        p[0] = (Polygon) geom;
                        
                        allGeoms[t] = makeGoodShapeMultiPolygon(new MultiPolygon(p,
                        new PrecisionModel(), 0));
                    } else if(geom instanceof MultiPolygon) {
                        allGeoms[t] = makeGoodShapeMultiPolygon((MultiPolygon) geom);
                    } else {
                        allGeoms[t] = new MultiPolygon(null, new PrecisionModel(), 0);
                    }
                    
                    break;
                    
               case 4: //point
                    
                    if((geom instanceof Point)) {
                        Point[] p = new Point[1];
                        p[0] = (Point) geom;
                        
                        allGeoms[t] = new MultiPoint(p, new PrecisionModel(), 0);
                    } else if(geom instanceof MultiPoint) {
                        allGeoms[t] = geom;
                    } else {
                        allGeoms[t] = new MultiPoint(null, new PrecisionModel(), 0);
                    }
                    
                    break;
            }
        }
        
        result = new GeometryCollection(allGeoms, new PrecisionModel(), 0);
        
        return result;
    }
    
    
    /**
     *Returns: <br>
     *2 for 2d (default) <br>
     *4 for 3d  - one of the oordinates has a non-NaN z value <br>
     *(3 is for x,y,m but thats not supported yet) <br>
     *@param g geometry to test - looks at 1st coordinate
     **/
    public int guessCoorinateDims(Geometry g) {
        Coordinate[] cs = g.getCoordinates();
        
        for(int t = 0; t < cs.length; t++) {
            if(!(Double.isNaN(cs[t].z))) {
                return 4;
            }
        }
        
        return 2;
    }
}
