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

package org.geotools.data.gml;

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
import org.geotools.gml.*;

// Java Topology Suite dependencies
import com.vividsolutions.jts.geom.Envelope;


/**
 * The source of data for Features. Shapefiles, databases, etc. are referenced
 * through this interface.
 *
 * @version $Id: GMLDataSource.java,v 1.2 2003/07/26 15:28:36 dledmonds Exp $
 * @author Ian Turton, CCG
 */
public class GMLDataSource extends AbstractDataSource { //extends XMLFilterImpl
//implements DataSource { , GMLHandlerFeature {

    /**
     * The logger for the GML module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.gml");
    
    // unused now ...
    /** Specifies the default parser (Xerces). */
    // private String defaultParser = "org.apache.xerces.parsers.SAXParser";
    
    /** Holds a URI for the GML data. */
    private URL url;
    
    /** Temporary storage for the features loaded from GML. */
    private FeatureCollection features = FeatureCollections.newCollection();
    
    
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
    
    // unused now ...
    //public void setParser(String parser) {
    //    this.defaultParser = parser;
    //}
    
    
//    public void feature(Feature feature) {
//        features.add(feature);
//    }
    
    
    
    
    
    
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
     * @throw UnsupportedOperationException
     * @task TODO: Implement addFeatures method
     */
    public Set addFeatures(FeatureCollection collection) throws DataSourceException, UnsupportedOperationException {
         return super.addFeatures( collection );
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
    public Envelope getBounds() throws DataSourceException, UnsupportedOperationException {
            //scan whole file
//      try {
//        // GMLFilterFeature featureFilter = new GMLFilterFeature(this);
//        GMLFilterFeature featureFilter = new GMLFilterFeature( new GMLReceiver( features ) );
//        GMLFilterGeometry geometryFilter = new GMLFilterGeometry(featureFilter);
//        GMLFilterDocument documentFilter = new GMLFilterDocument(geometryFilter);
//        //XMLReader parser = XMLReaderFactory.createXMLReader(defaultParser);
//        SAXParserFactory fac = SAXParserFactory.newInstance();
//        SAXParser parser = fac.newSAXParser();
//        
//        ParserAdapter p = new ParserAdapter(parser.getParser());
//        p.setContentHandler(documentFilter);
//        p.parse(getInputSource());
//      }
//      catch (Exception e) {
//        return null;
//      }
        parse();
        
//      Envelope bbox = new Envelope();
//      for ( int i = 0; i < features.size(); i++ ) {
//        Envelope bbox2;
//        bbox2 = ((Feature) features.get(i)).getDefaultGeometry().getEnvelopeInternal();
//        bbox.expandToInclude(bbox2);
//      }
//      return bbox;
      return features.getBounds();
        
    }
    
    /** Loads features from the datasource into the returned collection, based on
     * the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @return Collection The collection to put the features into.
     * @throws DataSourceException For all data source errors.
     */
    public FeatureCollection getFeatures(Filter filter) throws DataSourceException {
        FeatureCollection fc = FeatureCollections.newCollection();
        getFeatures(fc,filter);
        return fc;
    }
    
    /**
     *
     */
    public FeatureCollection getFeatures(Query query) 
	throws DataSourceException {
	Filter filter = null;
	if (query != null) {
	    filter = query.getFilter();
	}
	return getFeatures(filter);
    }

    /**
     *
     */
    public void getFeatures(FeatureCollection collection, Query query)
	throws DataSourceException {
	Filter filter = null;
	if (query != null) {
	    filter = query.getFilter();
	}
	getFeatures(collection, filter);
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
//        try {
//            //GMLFilterFeature featureFilter = new GMLFilterFeature(this);
//            GMLFilterFeature featureFilter = new GMLFilterFeature( new GMLReceiver( features ) );
//            GMLFilterGeometry geometryFilter = new GMLFilterGeometry(featureFilter);
//            GMLFilterDocument documentFilter = new GMLFilterDocument(geometryFilter);
//            //XMLReader parser = XMLReaderFactory.createXMLReader(defaultParser);
//            SAXParserFactory fac = SAXParserFactory.newInstance();
//            SAXParser parser = fac.newSAXParser();
//            
//            ParserAdapter p = new ParserAdapter(parser.getParser());
//            p.setContentHandler(documentFilter);
//            p.parse(getInputSource());
//        }
//        catch (IOException e) {
//            throw new DataSourceException("Error reading URI: " + url );
//        }
//        catch (SAXException e) {
//            throw new DataSourceException("Parsing error: " + e.getMessage());
//        }
//        catch (ParserConfigurationException e){
//            throw new DataSourceException("Parsing error: " + e.getMessage());
//        }
        parse();
        
        FilteringIteration.filter(features,filter);
        collection.addAll(features);
        
//        Iterator list = features.iterator();
//        
//        while(list.hasNext()){
//            if (!filter.contains((Feature) list.next())) {
//                list.remove();
//                LOGGER.finer("feature filtered out");
//            }
//        }
        
        
//        Feature[] typedFeatures = new Feature[features.size()];
//        for (int i = 0; i < features.size(); i++) {
//            typedFeatures[i] = (Feature) features.get(i);
//        }
//        collection.addFeatures(typedFeatures);
    }

       /**
     * Loads all features from the datasource into the returned collection.
     * Filter.NONE can also be used to get all features.  Calling this
     * function is equivalent to using {@link Query.ALL}
     *
     * @return Collection The collection to put the features into.
     *
     * @throws DataSourceException For all data source errors.
     */
    public FeatureCollection getFeatures() throws DataSourceException {
        return getFeatures(Query.ALL);
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
    public void modifyFeatures(AttributeType type, Object value, Filter filter) throws DataSourceException, UnsupportedOperationException {
        super.modifyFeatures( type, value, filter );
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
    public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter) throws DataSourceException, UnsupportedOperationException {
        super.modifyFeatures( type, value, filter );
    }
    
    /** Removes all of the features specificed by the passed filter from the
     * collection.
     *
     * @param filter An OpenGIS filter; specifies which features to remove.
     * @throws DataSourceException If anything goes wrong or if deleting is
     * not supported.
     * @task TODO: Implement support for removal of features
     */
    public void removeFeatures(Filter filter) throws DataSourceException, UnsupportedOperationException {
        super.removeFeatures( filter );
    }
    
    /**
     *
     */
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
     *
     */
    private void parse() throws DataSourceException {
        try {
            //GMLFilterFeature featureFilter = new GMLFilterFeature(this);
            GMLFilterFeature featureFilter = new GMLFilterFeature( new GMLReceiver( features ) );
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
    }

    
    /**************************************************
      Data source utility methods.
     **************************************************/

    /**
     * Gets the DatasSourceMetaData object associated with this datasource.  
     * This is the preferred way to find out which of the possible datasource
     * interface methods are actually implemented, query the DataSourceMetaData
     * about which methods the datasource supports.
     * @task REVISIT: way for datasource to change hasFastBbox after bbox is
     * calculated.
     */
//    public DataSourceMetaData getMetaData(){
//	return new DataSourceMetaData() {
//		public boolean supportsAdd(){ return false; }
//		public boolean supportsModify(){ return false; }
//		public boolean supportsRemove(){ return false; }
//		public boolean supportsRollbacks(){ return false; }
//		public boolean supportsSetFeatures(){return false;}
//		public boolean hasFastBbox(){return true;}
//		public boolean supportsAbort(){return false;}
//		public boolean supportsGetBbox(){return true;}
//	    };
//    }
    
    /**
     * Creates the a metaData object.
     *
     * @return the metadata for this datasource.
     *
     * @see #MetaDataSupport
     */
    protected DataSourceMetaData createMetaData() {
        MetaDataSupport metaData = new MetaDataSupport();
        metaData.setSupportsGetBbox( true );
        
        return metaData;
    }

	    
    /**
     * Deletes the all the current Features of this datasource and adds the
     * new collection.  Primarily used as a convenience method for file 
     * datasources.  
     * @param collection - the collection to be written
     */
    public void setFeatures(FeatureCollection collection) throws DataSourceException, UnsupportedOperationException {
	super.setFeatures( collection );
    }

    /**
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.
     *
     * @throws DataSourceException if there are any problems getting the
     *         schema.
     * @tasks TODO: implement this, as all datasources _should_ have this 
     * method.
     */
    public FeatureType getSchema() throws DataSourceException {
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
    
       /**
     * Makes all transactions made since the previous commit/rollback
     * permanent.  This method should be used only when auto-commit mode has
     * been disabled.   If autoCommit is true then this method does nothing.
     *
     * @throws DataSourceException if there are any datasource errors.
     *
     * @see #setAutoCommit(boolean)
     */
    public void commit() throws DataSourceException {
        //Does nothing, as default datasource is in auto commit mode, it
        //commits after every transaction.
    }

    /**
     * Undoes all transactions made since the last commit or rollback. This
     * method should be used only when auto-commit mode has been disabled.
     * This method should only be implemented if
     * <tt>setAutoCommit(boolean)</tt>  is also implemented.
     *
     * @throws DataSourceException if there are problems with the datasource.
     * @throws UnsupportedOperationException if the rollback method is not
     *         supported by this datasource.
     *
     * @see #setAutoCommit(boolean)
     */
    public void rollback()
        throws DataSourceException, UnsupportedOperationException {
	super.rollback();
    }

    /**
     * Sets this datasources auto-commit mode to the given state. If a
     * datasource is in auto-commit mode, then all its add, remove and modify
     * calls will be executed  and committed as individual transactions.
     * Otherwise, those calls are grouped into a single transaction  that is
     * terminated by a call to either the method commit or the method
     * rollback.  By default, new datasources are in auto-commit mode.
     *
     * @param autoCommit <tt>true</tt> to enable auto-commit mode,
     *        <tt>false</tt> to disable it.
     *
     * @throws DataSourceException DOCUMENT ME!
     * @throws UnsupportedOperationException DOCUMENT ME!
     *
     * @see #setAutoCommit(boolean)
     */
    public void setAutoCommit(boolean autoCommit)
        throws DataSourceException, UnsupportedOperationException {
	
	super.setAutoCommit( autoCommit );
    }

    /**
     * Retrieves the current autoCommit mode for the current DataSource.  If
     * the datasource does not implement setAutoCommit, then this method
     * should always return true.
     *
     * @return <tt>true</tt>, as datasources are autoCommit by default.  If
     *         setAutoCommit is implemented then this method should be
     *         overridden.
     *
     * @throws DataSourceException if a datasource access error occurs.
     *
     * @see #setAutoCommit(boolean)
     */
    public boolean getAutoCommit() throws DataSourceException {
        return true;
    }
    
    
}
