package org.geotools.gml;

import java.io.*;
import java.util.*;
import java.net.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.geotools.featuretable.*;
import org.geotools.datasource.*;

import com.vividsolutions.jts.geom.*;

/**
 * The source of data for Features. Shapefiles, database, etc. are referenced through this 
 * interface.
 * 
 *@version $Id: GMLGeometryDataSource.java,v 1.2 2002/05/02 17:02:54 ianturton Exp $
 */
public class GMLGeometryDataSource extends XMLFilterImpl implements DataSource, GMLHandlerJTS {


    /** Specifies the default parser (Xerces) */
    private static final String defaultParser = "org.apache.xerces.parsers.SAXParser";
    
    /** Holds a URI for the GML data */
    private InputSource uri;
    
    /** Temporary storage for the features loaded from GML. */
    private Vector geoms = new Vector();
    
    
    public GMLGeometryDataSource(String uri) {
        setUri(uri);
    }
    
    public GMLGeometryDataSource(URL uri) throws DataSourceException{
        setUri(uri);
    }
    
    
    public void setUri(String uri) {
        this.uri = new InputSource(uri);
    }
    
    public void setUri(URL uri) throws DataSourceException{
        InputStream in = null;
        try{
            in = uri.openStream();
        }catch (IOException e){
            throw new DataSourceException("Error reading url "+uri.toString()+" in GMLGeometryDataSource"+
                "\n"+e);
        }
        this.uri = new InputSource(in);
    }
    

    
    
    /** 
     * Loads Feature rows for the given Extent from the datasource
     *
     * @param ft featureTable to load features into
     * @param ex an extent defining which features to load - null means all features
     * @throws DataSourceException if anything goes wrong
     */
    public void importFeatures(FeatureTable ft, Extent ex)
        throws DataSourceException {
        
        // chains all the appropriate filters together (in correct order)
        //  and initiates parsing
        try {
            			
            GMLFilterGeometry geometryFilter = new GMLFilterGeometry(this);						
            GMLFilterDocument documentFilter = new GMLFilterDocument(geometryFilter);
            XMLReader parser = XMLReaderFactory.createXMLReader(defaultParser);
            
            parser.setContentHandler(documentFilter);
            parser.parse(uri);
        }
        catch (IOException e) {
            System.out.println("Error reading uri: " + uri );
        }
        catch (SAXException e) {
            System.out.println("Error in parsing: " + e.getMessage() );
        }
        
        Feature[] typedFeatures = new Feature[geoms.size()];
        for ( int i = 0; i < geoms.size() ; i++ ){
            typedFeatures[i] = new DefaultFeature();
            typedFeatures[i].setGeometry((Geometry) geoms.get(i));
        }

        ft.addFeatures( typedFeatures );
        
    }
    
    
    /** 
     * Saves the given features to the datasource
     *
     * @param ft feature table to get features from
     * @param ex extent to define which features to write - null means all
     * @throws DataSourceException if anything goes wrong or if exporting is not supported
     */
    public void exportFeatures(FeatureTable ft, Extent ex)
        throws DataSourceException {
        throw new DataSourceException("This data source is read-only!");
    }	
    
    
    /**
     * Stops this DataSource from loading
     */
    public void stopLoading() {
    }
    
    /**
     * Recieves OGC simple feature type geometry from parent.
     */
    public void geometry(Geometry geometry) {
        geoms.add(geometry);
    }
    
    /** gets the extent of this data source using the speed of
     * this datasource as set by the parameter.
     * @param quick if true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but acurate extent
     * will be returned
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent(boolean speed) {
        if(speed == true ){
            return getExtent();
        } else {
            return null;
        }
    }
    
    /** gets the extent of this data source using the a fastish method
     * of scanning the file for a boundingBox
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent() {
        return null;
    }
    
}

