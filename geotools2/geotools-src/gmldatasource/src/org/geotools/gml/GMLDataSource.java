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

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.geotools.feature.*;
import org.geotools.data.*;
import org.geotools.filter.*;

import com.vividsolutions.jts.geom.Envelope;

//Logging system
import org.apache.log4j.Logger;

/**
 * The source of data for Features. Shapefiles, databases, etc. are referenced
 * through this interface.
 *
 * @version $Id: GMLDataSource.java,v 1.18 2002/07/20 10:42:05 jmacgill Exp $
 * @author Ian Turton, CCG
 */
public class GMLDataSource extends XMLFilterImpl
implements DataSource, GMLHandlerFeature {
    
    private static Logger log = Logger.getLogger("gmldatasource");
    
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
        try {
            in = uri.openStream();
        } catch (IOException e){
            throw new DataSourceException("Error reading url " + uri.toString() + " in GMLGeometryDataSource" +
            "\n" + e);
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
     * Stops this DataSource from loading.
     * @task TODO: Implement ability to abort loading
     */
    public void abortLoading() {
        //can't be done at the moment
    }
    
    
    /** Adds all features from the passed feature collection to the datasource.
     *
     * @param collection The collection from which to add the features.
     * @throws DataSourceException If anything goes wrong or if exporting is
     * not supported.
     * @task TODO: Implement addFeatures method
     */
    public void addFeatures(FeatureCollection collection) throws DataSourceException {
         throw new DataSourceException("Removal of features is not yet supported by this datasource");
    }
    
    /** Gets the bounding box of this datasource using the default speed of
     * this datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     * expensive for the method to calculate.
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
     */
    public Envelope getBbox(boolean quick) {
        if (quick == true){
            return getBbox();
        } else {
            //scan whole file
            try {
                GMLFilterFeature featureFilter = new GMLFilterFeature(this);
                GMLFilterGeometry geometryFilter = new GMLFilterGeometry(featureFilter);
                GMLFilterDocument documentFilter = new GMLFilterDocument(geometryFilter);
                //XMLReader parser = XMLReaderFactory.createXMLReader(defaultParser);
                SAXParserFactory fac = SAXParserFactory.newInstance();
                SAXParser parser = fac.newSAXParser();
                
                ParserAdapter p = new ParserAdapter(parser.getParser());
                p.setContentHandler(documentFilter);
                p.parse(uri);
            }
            catch (Exception e) {
                return null;
            }
            Envelope bbox = new Envelope();
            for ( int i = 0; i < features.size(); i++ ) {
                Envelope bbox2;
                bbox2 = ((Feature) features.get(i)).getDefaultGeometry().getEnvelopeInternal();
                bbox.expandToInclude(bbox2);
            }
            return bbox;
        }
    }
    
    /** Loads features from the datasource into the returned collection, based on
     * the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @return Collection The collection to put the features into.
     * @throws DataSourceException For all data source errors.
     */
    public FeatureCollection getFeatures(Filter filter) throws DataSourceException {
        FeatureCollection fc = new FeatureCollectionDefault();
        getFeatures(fc,filter);
        return fc;
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
        // chains all the appropriate filters together (in correct order)
        //  and initiates parsing
        try {
            GMLFilterFeature featureFilter = new GMLFilterFeature(this);
            GMLFilterGeometry geometryFilter = new GMLFilterGeometry(featureFilter);
            GMLFilterDocument documentFilter = new GMLFilterDocument(geometryFilter);
            //XMLReader parser = XMLReaderFactory.createXMLReader(defaultParser);
            SAXParserFactory fac = SAXParserFactory.newInstance();
            SAXParser parser = fac.newSAXParser();
            
            ParserAdapter p = new ParserAdapter(parser.getParser());
            p.setContentHandler(documentFilter);
            p.parse(uri);
        }
        catch (IOException e) {
            throw new DataSourceException("Error reading URI: " + uri );
        }
        catch (SAXException e) {
            throw new DataSourceException("Parsing error: " + e.getMessage());
        }
        catch (ParserConfigurationException e){
            throw new DataSourceException("Parsing error: " + e.getMessage());
        }
        
        Iterator list = features.iterator();
        
        while(list.hasNext()){
            if (!filter.contains((Feature) list.next())) {
                list.remove();
                log.debug("feature filtered out");
            }
        }
        
        
        Feature[] typedFeatures = new Feature[features.size()];
        for (int i = 0; i < features.size(); i++) {
            typedFeatures[i] = (Feature) features.get(i);
        }
        collection.addFeatures(typedFeatures);
        
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

