/*
 * PNGDatasource.java
 *
 * Created on 30 October 2002, 16:16
 */

package org.geotools.coverage;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import java.awt.Canvas;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.geotools.cs.CoordinateSystemAuthorityFactory;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.data.DataSourceException;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.NullFilter;


/**
 * $Id: ImageDataSource.java,v 1.2 2002/11/13 17:11:34 ianturton Exp $
 *
 * @author  iant
 */
public class ImageDataSource implements org.geotools.data.DataSource{
    org.geotools.io.coverage.ExoreferencedGridCoverageReader reader;
    org.geotools.io.coverage.PropertyParser parser;
    java.io.File file;
    static private Logger LOGGER = Logger.getLogger("org.geotools.coverage");
    static FilterFactory filterFactory = FilterFactory.createFilterFactory();
    Envelope bbox;
    
    static org.geotools.feature.FeatureType schema;
    static org.geotools.feature.FeatureFactory factory;
    static com.vividsolutions.jts.geom.GeometryFactory geomFac = new com.vividsolutions.jts.geom.GeometryFactory();
    static {
        org.geotools.feature.AttributeTypeDefault geom = new org.geotools.feature.AttributeTypeDefault("geom",com.vividsolutions.jts.geom.Polygon.class);
        org.geotools.feature.AttributeTypeDefault grid = new org.geotools.feature.AttributeTypeDefault("grid",org.geotools.gc.GridCoverage.class);
        try{
            schema = new org.geotools.feature.FeatureTypeFlat(new AttributeType[]{geom,grid});
        } catch (SchemaException e){
            System.err.println("Help - unexpected schema exception thrown\n\t"+e);
        }
        factory = new org.geotools.feature.FeatureFactory(schema);
    }
    /** Creates a new instance of PNGDatasource */
    public ImageDataSource(String name) throws java.io.IOException{
        String worldfile;
        int index = name.lastIndexOf('.');
        if(index<0){
            worldfile = name + ".wld";
        }else{
            worldfile = name.substring(0,index)+".wld";
        }
        load(name,worldfile);
    }
    
    public ImageDataSource(String name, String worldfile) throws java.io.IOException{
        load(name,worldfile);    
    }
    
    private void load(String name, String worldfile) throws java.io.IOException{
        int index = name.lastIndexOf(".");
        String format="";
        if(index > 0 ){
            format = name.substring(index+1,name.length());
        }
        parser = new WorldFileParser(new File(worldfile));
        Image img = Toolkit.getDefaultToolkit().createImage(name);
        Canvas obs = new Canvas();
        while(img.getWidth(obs) < 0){
            try{
                Thread.sleep(100);
            } catch (InterruptedException e){
                // empty
            }
        }
        ((WorldFileParser)parser).setHeight(img.getHeight(obs));
        ((WorldFileParser)parser).setWidth(img.getWidth(obs));
        reader = new org.geotools.io.coverage.ExoreferencedGridCoverageReader(format,parser);
        file = new java.io.File(name);
        
        reader.setInput(file, false);
    }
    
    /** Stops this DataSource from loading.
     *
     */
    public void abortLoading() {
    }
    
    ArrayList loadFeatures(Filter filter) throws java.io.IOException, DataSourceException{
        if(filter == null) { // I think this builds a filter which is true for all none null features?
            filter = filterFactory.createNullFilter();
            try{
                ((NullFilter)filter).nullCheckValue(filterFactory.createAttributeExpression(schema,"geom"));  
            } catch (IllegalFilterException e){
                throw new DataSourceException("",e);
            }
            filter = filter.not(); 
        }
        int numb = reader.getNumImages(true);
        FeatureCollection features = new org.geotools.feature.FeatureCollectionDefault();
        ArrayList featuresList = new ArrayList();
        for(int i=0; i<numb;i++){
            
            org.geotools.pt.Envelope env = reader.getEnvelope(i);
            //
            Envelope jenv = new Envelope(env.getMinimum(0),env.getMaximum(0),env.getMinimum(1),env.getMaximum(1));
            if(bbox != null){
                bbox.expandToInclude(jenv);
            }else{ 
                bbox = jenv;
            }
            LOGGER.fine("Xmin(pt) = " + env.getMinimum(0));
            LOGGER.fine("Xmin = " + jenv.getMinX());
            Coordinate[] c = new Coordinate[5];
            c[0] = new Coordinate(jenv.getMinX(), jenv.getMinY());
            c[1] = new Coordinate(jenv.getMinX(), jenv.getMaxY());
            c[2] = new Coordinate(jenv.getMaxX(), jenv.getMaxY());
            c[3] = new Coordinate(jenv.getMaxX(), jenv.getMinY());
            c[4] = new Coordinate(jenv.getMinX(), jenv.getMinY());
            com.vividsolutions.jts.geom.LinearRing r = null;
            try{
                r = geomFac.createLinearRing(c);
            } catch (com.vividsolutions.jts.geom.TopologyException e){
                throw new DataSourceException(e.toString());
            }
            com.vividsolutions.jts.geom.Polygon p = geomFac.createPolygon(r,null);
            try{
                org.geotools.feature.Feature feature = factory.create(new Object[]{p,reader.getGridCoverage(i)});
                System.out.println("created "+ feature);
                if(filter.contains(feature)){
                    featuresList.add(feature);
                }
            } catch (org.geotools.feature.IllegalFeatureException ife){
                throw new DataSourceException("",ife);
            }
        }
        return featuresList;
    }
    /** Adds all features from the passed feature collection to the datasource.
     *
     * @param collection The collection from which to add the features.
     * @throws DataSourceException If anything goes wrong or if exporting is
     * not supported.
     *
     */
    public void addFeatures(FeatureCollection collection) throws DataSourceException {
        throw new DataSourceException("Non transactional datasource");
    }
    
    /** Gets the bounding box of this datasource using the default speed of
     * this datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     * expensive for the method to calculate.
     * @task REVISIT: Consider changing return of getBbox to Filter once Filters can be unpacked
     *
     */
    public Envelope getBbox() {
        return bbox;
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
     *
     */
    public Envelope getBbox(boolean speed) {
        try{
            if(speed == true){
                return bbox;
            }else{
                bbox = new Envelope();
                int numb = reader.getNumImages(true);
                for(int i=0; i<numb;i++){
                    
                    org.geotools.pt.Envelope env = reader.getEnvelope(i);
                    //
                    Envelope jenv = new Envelope(env.getMinimum(0),env.getMaximum(0),env.getMinimum(1),env.getMaximum(1));
                    if(bbox!=null){
                        bbox.expandToInclude(jenv);
                    }else{
                        bbox = jenv;
                    }
                }
                return bbox;
            }
        } catch (IOException e){
            return null;
        }
    }

    /** Loads features from the datasource into the returned collection, based on
     * the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @return Collection The collection to put the features into.
     * @throws DataSourceException For all data source errors.
     *
     */
    public FeatureCollection getFeatures(Filter filter) throws DataSourceException {
        try{
            FeatureCollection collection = new org.geotools.feature.FeatureCollectionDefault();
            collection.addFeatures((Feature[])loadFeatures(filter).toArray(new Feature[0]));
            return collection;
        } catch (java.io.IOException e){
            throw new DataSourceException("",e);
        }
        
    }
    
    /** Loads features from the datasource into the passed collection, based on
     * the passed filter.  Note that all data sources must support this method
     * at a minimum.
     *
     * @param collection The collection to put the features into.
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @throws DataSourceException For all data source errors.
     *
     */
    public void getFeatures(FeatureCollection collection, Filter filter) throws DataSourceException {
        try{
            if(collection == null) collection = new org.geotools.feature.FeatureCollectionDefault();
            collection.addFeatures((Feature[])loadFeatures(filter).toArray(new Feature[0]));
        } catch (java.io.IOException e){
            throw new DataSourceException("",e);
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
     *
     */
    public void modifyFeatures(org.geotools.feature.AttributeType type, Object value, Filter filter) throws DataSourceException {
        throw new DataSourceException("Non transactional datasource");
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
     *
     */
    public void modifyFeatures(org.geotools.feature.AttributeType[] type, Object[] value, Filter filter) throws DataSourceException {
        throw new DataSourceException("Non transactional datasource");
    }
    
    /** Removes all of the features specificed by the passed filter from the
     * collection.
     *
     * @param filter An OpenGIS filter; specifies which features to remove.
     * @throws DataSourceException If anything goes wrong or if deleting is
     * not supported.
     *
     */
    public void removeFeatures(Filter filter) throws DataSourceException {
        throw new DataSourceException("Non transactional datasource");
    }
    
}
