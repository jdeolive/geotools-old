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
 * The source of data for Features. Shapefiles, databases, etc. are referenced 
 * through this interface.
 * 
 * @version $Id: GMLDataSource.java,v 1.15 2002/06/05 10:09:52 loxnard Exp $
 * @author Ian Turton, CCG
 */
public class GMLDataSource extends XMLFilterImpl 
    implements DataSource, GMLHandlerFeature {

    /** Specifies the default parser (Xerces). */
    private String defaultParser = "org.apache.xerces.parsers.SAXParser";
    
    /** Holds a URI for the GML data. */
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
     * Loads Feature rows for the given Extent from the datasource.
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
     * Saves the given features to the datasource.
     *
     * @param ft feature table to get features from
     * @param ex extent to define which features to write - null means all
     * @throws DataSourceException if anything goes wrong or if exporting is
     * not supported
     */
    public void exportFeatures(FeatureCollection ft, Extent ex)
        throws DataSourceException {
        throw new DataSourceException("Cannot add features to read only GML: "
                                      + uri);
    }	
    
    
    /**
     * Stops this DataSource from loading.
     */
    public void stopLoading() {
    }
    
    /**
     * Gets the extent of this datasource using the speed of
     * this datasource as set by the parameter.
     * @param quick if true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but accurate extent
     * will be returned.
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
			
    /**
     * Gets the extent of this datasource using the quicker  
     * method of scanning the file for a boundingbox statement.
     *
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent() {
        return null;
    }
    
}

