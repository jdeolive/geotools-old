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

// J2SE dependencies
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.logging.Logger;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

// Geotools dependencies
import org.geotools.feature.*;
import org.geotools.data.*;
import org.geotools.filter.*;

// Java Topology Suite dependencies
import com.vividsolutions.jts.geom.Envelope;


/**
 * The source of data for Features. Shapefiles, databases, etc. are referenced
 * through this interface.
 *
 * @version $Id: GMLDataSource.java,v 1.21 2003/03/28 19:20:50 cholmesny Exp $
 * @author Ian Turton, CCG
 */
public class GMLDataSource extends XMLFilterImpl
implements DataSource, GMLHandlerFeature {

    /**
     * The logger for the GML module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.gml");
    
    /** Specifies the default parser (Xerces). */
    private String defaultParser = "org.apache.xerces.parsers.SAXParser";
    
    /** Holds a URI for the GML data. */
    private URL url;
    
    /** Temporary storage for the features loaded from GML. */
    private Vector features = new Vector();
    
    
    public GMLDataSource(String uri) throws DataSourceException {
        setUri(uri);
    }
    
    public GMLDataSource(URL uri) throws DataSourceException{
        setUri(uri);
    }
    
    
    public void setUri(String uri) throws DataSourceException {
        try{
            url = new URL(uri);
        }
        catch (MalformedURLException mue){
            throw new DataSourceException(mue.toString());
        }
    }
    
    public void setUri(URL uri) throws DataSourceException{
        /*try {
            in = uri.openStream();
        } catch (IOException e){
            throw new DataSourceException("Error reading url " + uri.toString() + " in GMLGeometryDataSource" +
            "\n" + e);
        }
        this.uri = new InputSource(in);*/
        
        
        this.url = uri;
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
    public Set addFeatures(FeatureCollection collection) throws DataSourceException {
         throw new DataSourceException("Removal of features is not yet supported by this datasource");
    }
    
    /** Gets the bounding box of this datasource using the default speed of
     * this datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Envelope getBbox() {
        return getBbox(false);
    }
    
    /** Gets the bounding box of this datasource using the speed of
     * this datasource as set by the parameter.
     *
     * @param speed If true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but accurate extent
     * will be returned
     * @return The extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     * @task TODO: implement quick bbox.  This will return slow no matter what.
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
                p.parse(getInputSource());
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
            p.parse(getInputSource());
        }
        catch (IOException e) {
            throw new DataSourceException("Error reading URI: " + url );
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
                LOGGER.finer("feature filtered out");
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
    
    private InputSource getInputSource() throws DataSourceException{
        InputStream in;
        try {
            in = url.openStream();
        } catch (IOException e){
            throw new DataSourceException("Error reading url " + url.toString() + " in GMLGeometryDataSource" +
            "\n" + e);
        }
        return new InputSource(in);
    }
        
       /**
     * Begins a transaction(add, remove or modify) that does not commit as 
     * each modification call is made.  If an error occurs during a transaction
     * after this method has been called then the datasource should rollback: 
     * none of the transactions performed after this method was called should
     * go through.
     */
    public void startMultiTransaction() throws DataSourceException{
	throw new DataSourceException("multi transactions not supported");
    }

    /**
     * Ends a transaction after startMultiTransaction has been called.  Similar
     * to a commit call in sql, it finalizes all of the transactions called
     * after a startMultiTransaction.
     */
    public void endMultiTransaction() throws DataSourceException {
	throw new DataSourceException("multi transactions not supported");
    }
    /**************************************************
      Data source utility methods.
     **************************************************/

    /**
     * Gets the DatasSourceMetaData object associated with this datasource.  
     * This is the preferred way to find out which of the possible datasource
     * interface methods are actually implemented, query the DataSourceMetaData
     * about which methods the datasource supports.
     */
    public DataSourceMetaData getMetaData(){
	return new DataSourceMetaData() {
		public boolean supportsTransactions(){ return false; }
		public boolean supportsMultiTransactions(){ return false; }
		public boolean supportsSetFeatures(){return false;}
		public boolean supportsSetSchema(){return false;}
		public boolean supportsAbort(){return false;}
		public boolean supportsGetBbox(){return true;}
	    };
    }
	    
    /**
     * Deletes the all the current Features of this datasource and adds the
     * new collection.  Primarily used as a convenience method for file 
     * datasources.  
     * @param collection - the collection to be written
     */
    public void setFeatures(FeatureCollection collection) throws DataSourceException{
	throw new DataSourceException("set feature not supported");
    }

    /**
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.
     * @tasks TODO: implement this, as all datasources _should_ have this 
     * method.
     */
    public FeatureType getSchema(){
	return null;
    }

    /**
     * Sets the schema that features extrated from this datasource will be 
     * created with.  This allows the user to obtain the attributes he wants,
     * by calling getSchema and then creating a new schema using the 
     * attributeTypes from the currently used schema.  
     * @param schema the new schema to be used to create features.
     */
    public void setSchema(FeatureType schema) throws DataSourceException {
	throw new DataSourceException("schema methods not supported");
    }
    
        
    
}

