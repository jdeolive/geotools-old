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
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.Set;
import javax.media.jai.JAI;
import org.geotools.cs.CoordinateSystemAuthorityFactory;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.data.DataSourceException;
import org.geotools.data.AbstractDataSource;
import org.geotools.data.Query;
import org.geotools.data.DataSourceMetaData;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.NullFilter;
import org.geotools.gc.GridCoverage;


/**
 * $Id: ImageDataSource.java,v 1.8 2004/03/14 18:44:27 aaime Exp $
 * @todo : fix the typeName! -IanS
 * @author  iant
 */
public class ImageDataSource extends AbstractDataSource 
    implements org.geotools.data.DataSource{
    org.geotools.io.coverage.ExoreferencedGridCoverageReader reader;
    org.geotools.io.coverage.PropertyParser parser;
    java.io.File file;
    static private Logger LOGGER = Logger.getLogger("org.geotools.coverage");
    static FilterFactory filterFactory = FilterFactory.createFilterFactory();
    Envelope bbox;
    
    static org.geotools.feature.FeatureType schema;
    static com.vividsolutions.jts.geom.GeometryFactory geomFac = new com.vividsolutions.jts.geom.GeometryFactory();
    static {
        AttributeType geom = AttributeTypeFactory.newAttributeType("geom",com.vividsolutions.jts.geom.Polygon.class);
        AttributeType grid = AttributeTypeFactory.newAttributeType("grid",org.geotools.gc.GridCoverage.class);
        try{
          // todo : fix the typeName! -IanS
            schema = FeatureTypeFactory.newFeatureType(new AttributeType[]{geom,grid},"image");
        } catch (SchemaException e){
            System.err.println("Help - unexpected schema exception thrown\n\t"+e);
        }
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
        RenderedImage image = (RenderedImage) JAI.create("fileload", name);
        ((WorldFileParser)parser).setHeight(image.getHeight());
        ((WorldFileParser)parser).setWidth(image.getWidth());
        ((WorldFileParser)parser).setDimension(image.getSampleModel().getNumBands());
        
        reader = new org.geotools.io.coverage.ExoreferencedGridCoverageReader(format,parser);
        file = new java.io.File(name);
        
        reader.setInput(file, false);
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
        FeatureCollection features = FeatureCollections.newCollection();
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
                Feature feature = schema.create(new Object[]{p,reader.getGridCoverage(i)});
                System.out.println("created "+ feature);
                if(filter.contains(feature)){
                    featuresList.add(feature);
                }
            } catch (org.geotools.feature.IllegalAttributeException ife){
                throw new DataSourceException("",ife);
            }
        }
        return featuresList;
    }
    
	public GridCoverage getGridCoverage(int i) throws IOException {
		return reader.getGridCoverage(i);
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
            FeatureCollection collection = FeatureCollections.newCollection();
            collection.addAll(loadFeatures(filter));
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
    public void getFeatures(FeatureCollection collection, Query query) throws DataSourceException {
	Filter filter = null;
	if (query != null) {
	    filter = query.getFilter();
	}
        try{
            if(collection == null) collection = FeatureCollections.newCollection();
            collection.addAll(loadFeatures(filter));
        } catch (java.io.IOException e){
            throw new DataSourceException("",e);
        }
    }
    

    /**
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.
     * @tasks TODO: implement this method.
     */
    public FeatureType getSchema(){
	return null;
    }

    /**
     * Creates the a metaData object.  This method should be overridden in any
     * subclass implementing any functions beyond getFeatures, so that clients
     * recieve the proper information about the datasource's capabilities.  <p>
     * 
     * @return the metadata for this datasource.
     *
     * @see #MetaDataSupport
     */
    protected DataSourceMetaData createMetaData() {
	MetaDataSupport imgMeta = new MetaDataSupport();
	imgMeta.setSupportsGetBbox(true);
	return imgMeta;
    }

}
