package org.geotools.gml;

import java.io.*;
import java.util.*;
import java.net.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.geotools.feature.*;
import org.geotools.data.*;
//import org.geotools.filter.*;


/**
 * The source of data for Features. Shapefiles, database, etc. are referenced 
 * through this interface.
 * 
 *@version $Id: GMLDataSource.java,v 1.14 2002/05/23 18:06:52 jmacgill Exp $
 */
public class GMLDataSource extends XMLFilterImpl 
    implements DataSource, GMLHandlerFeature {

    /** Specifies the default parser (Xerces) */
    private String defaultParser = "org.apache.xerces.parsers.SAXParser";
    
    /** Holds a URI for the GML data */
    private InputSource uri;
    
    /** Temporary storage for the features loaded from GML. */
    private Vector features = new Vector();
    
    
    public GMLDataSource(String uri) {
        setUri(uri);
    }
    
    public GMLDataSource(URL uri) throws DataSourceException{
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
        

    public void setParser(String parser) {
        this.defaultParser = parser;
    }
    
    
    public void feature(Feature feature) {
        features.add(feature);
    }
    
    
    /** 
     * Loads Feature rows for the given Extent from the datasource
     *
     * @param featureCollection featureTable to load features into
     * @param filter an extent defining which features to load - null means all features
     * @throws DataSourceException if anything goes wrong
     */
    public void importFeatures(FeatureCollection featureCollection, Extent filter)
        throws DataSourceException {
        
        // chains all the appropriate filters together (in correct order)
        //  and initiates parsing
        try {
            GMLFilterFeature featureFilter = new GMLFilterFeature(this);						
            GMLFilterGeometry geometryFilter = new GMLFilterGeometry(featureFilter);						
            GMLFilterDocument documentFilter = new GMLFilterDocument(geometryFilter);
            XMLReader parser = XMLReaderFactory.createXMLReader(defaultParser);
            
            parser.setContentHandler(documentFilter);
            parser.parse(uri);
        }
        catch (IOException e) {
            throw new DataSourceException("Error reading URI: " + uri );
        }
        catch (SAXException e) {
            throw new DataSourceException("Parsing error: " + e.getMessage());
        }
        
        Feature[] typedFeatures = new Feature[features.size()];
        for ( int i = 0; i < features.size(); i++ ) {
            typedFeatures[i] = (Feature) features.get(i);
        }

        featureCollection.addFeatures( typedFeatures );
        
    }
    
        
    /** 
     * Saves the given features to the datasource
     *
     * @param ft feature table to get features from
     * @param ex extent to define which features to write - null means all
     * @throws DataSourceException if anything goes wrong or if exporting is not supported
     */
    public void exportFeatures(FeatureCollection ft, Extent ex)
        throws DataSourceException {
        throw new DataSourceException("Cannot add features to read only GML: "
                                      + uri);
    }	
    
    
    /**
     * Stops this DataSource from loading
     */
    public void stopLoading() {
    }
    
    /** gets the extent of this data source using the speed of
     * this datasource as set by the parameter.
     * @param quick if true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but acurate extent
     * will be returned
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent(boolean quick) {
        if (quick == true ){
            return getExtent();
        } else {
            //scan whole file
            return null;
        }
    }    
			
    /** gets the extent of this data source using the quicker  
     * method of scanning the file for a boundingbox statement
     *
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent() {
        return null;
    }
    
}

