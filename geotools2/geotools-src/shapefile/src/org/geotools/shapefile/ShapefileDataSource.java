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

import org.geotools.data.DataSourceException;
import org.geotools.feature.AttributeType;
import org.geotools.filter.Filter;

import java.net.URL;
import java.io.IOException;
import java.util.logging.Logger;


/**
 * @version $Id: ShapefileDataSource.java,v 1.16 2002/09/04 16:19:20 jmacgill Exp $
 * @author James Macgill, CCG
 * @task TODO: add support for reading dbf file
 * @task TODO: add support for the optional spatial index files to improve
 *             loading of sub regions
 */
public class ShapefileDataSource implements org.geotools.data.DataSource {
    /**
     * The shapefile object that this datasouce connects to
     **/
    private Shapefile shapefile;
    
    private DbaseFileReader dbf;
    
    /**
     * The logger for this module.
     */
    private static final Logger LOGGER = Logger.getLogger(
    "org.geotools.shapefile");
    
    /**
     *
     */
    public ShapefileDataSource(java.net.URL url) {
        try {
            String filename = java.net.URLDecoder.decode(url.getFile(),"UTF-8");
            LOGGER.fine("filename part of shapefile is " + filename);
            
            String shpext = ".shp";
            String dbfext = ".dbf";
            
            if (filename.endsWith(".shp") || filename.endsWith(".dbf") ||
            filename.endsWith(".shx")) {
                filename = filename.substring(0, filename.length() - 4);
            }
            
            
            URL shpURL = new URL(url, filename + shpext);
            
            URL dbfURL = new URL(url, filename + dbfext);
            shapefile = new Shapefile(shpURL);
            dbf = new DbaseFileReader(dbfURL.getFile());
            LOGGER.fine("dbf url constructed as " + dbfURL);
        } catch (Exception ioe) {
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
     * Creates a new instance of ShapefileDataSource.
     * @deprecated Use URL constructor instead.
     */
    public ShapefileDataSource(Shapefile shapefile,DbaseFileReader dbf) {
        this.shapefile = shapefile;
        this.dbf = dbf;
    }
    
    /**
     * Gets the Column names (used by FeatureTable) for this DataSource.
     * @task HACK: need to add Geometry to column names returned from dbf
     */
    public String[] getColumnNames() {
        if(dbf != null) {
            return dbf.getFieldNames();
        }
        return new String[]{"Geometry"};
    }
    
    /** Stops this DataSource from loading.
     */
    public void abortLoading() {
    }
    
    /** Adds all features from the passed feature collection to the datasource.
     *
     * @param collection The collection from which to add the features.
     * @throws DataSourceException If anything goes wrong or if exporting is
     * not supported.
     * @task TODO: Implement addFeatures method
     */
    public void addFeatures(org.geotools.feature.FeatureCollection collection) throws DataSourceException {
        throw new DataSourceException("Removal of features is not yet supported by this datasource");
    }
    
    /** Gets the bounding box of this datasource using the default speed of
     * this datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public com.vividsolutions.jts.geom.Envelope getBbox() {
        return shapefile.getBounds();
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
    public org.geotools.feature.FeatureCollection getFeatures(Filter filter) throws DataSourceException {
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
    public void getFeatures(org.geotools.feature.FeatureCollection collection, Filter filter) throws DataSourceException {
        LOGGER.entering("ShapefileDataSource","getFeatures");
        try {
            com.vividsolutions.jts.geom.GeometryCollection shapes = shapefile.read(new com.vividsolutions.jts.geom.GeometryFactory());
            java.util.List features = new java.util.ArrayList();
            com.vividsolutions.jts.geom.Geometry typical = shapes.getGeometryN(0);
            AttributeType geometryAttribute = new org.geotools.feature.AttributeTypeDefault(Shapefile.getShapeTypeDescription(Shapefile.getShapeType(typical)), com.vividsolutions.jts.geom.Geometry.class);
            org.geotools.feature.FeatureType shapefileType;
            if(dbf != null) {
                LOGGER.fine("Reading from dbf");
                java.util.ArrayList attribs = dbf.getFieldTypes();
                attribs.add(0,geometryAttribute);
                AttributeType[] types = (AttributeType[])attribs.toArray(new AttributeType[0]);
                try{
                    shapefileType = new org.geotools.feature.FeatureTypeFlat(types);
                }
                catch(org.geotools.feature.SchemaException se){
                    throw new DataSourceException(se.getMessage());
                }
            }
            else {
                shapefileType = new org.geotools.feature.FeatureTypeFlat(geometryAttribute);
            }
            //System.out.println("schema is " + shapefileType);
            LOGGER.fine("Schema is " + shapefileType);
            org.geotools.feature.FeatureFactory fac = new org.geotools.feature.FeatureFactory(shapefileType);
            int count = shapes.getNumGeometries();
            //Feature[] features = new Feature[count];
            for (int i = 0; i < count; i++){
                Object [] row;
                if (dbf == null) {
                    row = new Object[1];
                }
                else {
                    row = new Object[dbf.getFieldNames().length+1]; //+1 for geomety
                    java.util.ArrayList values = dbf.read();
                    System.arraycopy(values.toArray(),0,row,1,dbf.getFieldNames().length);
                }
                row[0] = (com.vividsolutions.jts.geom.Geometry) shapes.getGeometryN(i);
                //System.out.println("adding geometry" + row[0]);
                org.geotools.feature.Feature feature = fac.create(row);
                if (filter.contains(feature)){
                    collection.addFeatures(new org.geotools.feature.Feature[]{feature});
                }
            }
        }
        catch (java.io.IOException ioe){
            throw new DataSourceException("IO Exception loading data : " + ioe.getMessage());
        }
        catch (ShapefileException se){
            throw new DataSourceException("Shapefile Exception loading data : " + se.getMessage());
        }
        catch (com.vividsolutions.jts.geom.TopologyException te){
            throw new DataSourceException("Topology Exception loading data : " + te.getMessage());
        }
        catch (org.geotools.feature.IllegalFeatureException ife){
            throw new DataSourceException("Illegal Feature Exception loading data : " + ife.getMessage());
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
    public void modifyFeatures(AttributeType type, Object value, Filter filter) throws DataSourceException {
        throw new DataSourceException("Modification of features is not yet supported by this datasource");
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
    public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter) throws DataSourceException {
        throw new DataSourceException("Modification of features is not yet supported by this datasource");
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
    
}
