/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.data.gml;


// Java Topology Suite dependencies
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.AbstractDataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataSourceMetaData;
import org.geotools.data.Query;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import org.geotools.filter.FilteringIteration;
import org.geotools.gml.GMLFilterDocument;
import org.geotools.gml.GMLFilterFeature;
import org.geotools.gml.GMLFilterGeometry;
import org.geotools.gml.GMLReceiver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.ParserAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

// J2SE dependencies
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * Datasource implementation backed by a FeatureCollection constructed from a
 * gml filters.
 *
 * @author Ian Turton, CCG
 * @version $Id: GMLDataSource.java,v 1.5 2003/08/14 17:11:04 cholmesny Exp $
 */
public class GMLDataSource extends AbstractDataSource {
    /** The logger for the GML module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.gml");

    /** Holds a URI for the GML data. */
    private URL url;

    /** Flag indicating that the features is already full of parsed features. */
    private boolean parsed;

    /** Temporary storage for the features loaded from GML. */
    private FeatureCollection features = FeatureCollections.newCollection();

    /**
     * Creates a new GMLDataSource
     *
     * @param uri URI of file to parse
     *
     * @throws DataSourceException if the url is malformed.
     */
    public GMLDataSource(String uri) throws DataSourceException {
        setUri(uri);
    }

    /**
     * Creates a new GMLDataSource
     *
     * @param uri URI of file to parse
     *
     * @throws DataSourceException if the url is malformed.
     */
    public GMLDataSource(URL uri) throws DataSourceException {
        setUri(uri);
    }

    /**
     * Sets the URI of the file to parse
     *
     * @param uri URI of file to parse
     *
     * @throws DataSourceException if the url is malformed.
     */
    public void setUri(String uri) throws DataSourceException {
        try {
            url = new URL(uri);
        } catch (MalformedURLException mue) {
            throw new DataSourceException(mue.toString());
        }
    }

    /**
     * Sets the URI of the file to parse
     *
     * @param uri URI of file to parse
     *
     * @throws DataSourceException if the url is malformed.
     */
    public void setUri(URL uri) throws DataSourceException {
        this.url = uri;
    }

    /**
     * Stops this DataSource from loading. <b>not yet implemented</b>
     *
     * @task TODO: Implement ability to abort loading
     */
    public void abortLoading() {
        //can't be done at the moment
    }

    /**
     * Adds all features from the passed feature collection to the datasource.
     *
     * @param collection The collection from which to add the features.
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException If anything goes wrong or if exporting is
     *         not supported.
     * @throws UnsupportedOperationException if the datasource does not support
     *         this operation
     *
     * @task TODO: Implement addFeatures method
     */
    public Set addFeatures(FeatureCollection collection)
        throws DataSourceException, UnsupportedOperationException {
        return super.addFeatures(collection);
    }

    /**
     * Gets the bounding box of this datasource using the speed of this
     * datasource as set by the parameter.
     *
     * @return The extent of the datasource or null if unknown and too
     *         expensive for the method to calculate.
     *
     * @throws DataSourceException for all errors getting the bounds
     * @throws UnsupportedOperationException if the datasource does not support
     *         this operation
     *
     * @task TODO: implement quick bbox.  This will return slow no matter what.
     */
    public Envelope getBounds()
        throws DataSourceException, UnsupportedOperationException {
        parse();

        return features.getBounds();
    }

    /**
     * Loads features from the datasource into the returned collection, based
     * on the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     *
     * @return Collection The collection to put the features into.
     *
     * @throws DataSourceException For all data source errors.
     */
    public FeatureCollection getFeatures(Filter filter)
        throws DataSourceException {
        FeatureCollection fc = FeatureCollections.newCollection();
        getFeatures(fc, filter);

        return fc;
    }

    /**
     * Loads features from the datasource into the returned collection, based
     * on the passed Query
     *
     * @param query Query to apply to datasource
     *
     * @return FeatureCollection containing the results
     *
     * @throws DataSourceException For all data source errors.
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
     * Loads features into the passed FeatureCollection, based on the passed
     * Query.
     *
     * @param collection FeatureCollection to load features into
     * @param query Query to apply to the datasource
     *
     * @throws DataSourceException for all datasurce errors
     */
    public void getFeatures(FeatureCollection collection, Query query)
        throws DataSourceException {
        Filter filter = null;

        if (query != null) {
            filter = query.getFilter();
        }

        getFeatures(collection, filter);
    }

    /**
     * Loads features from the datasource into the passed collection, based on
     * the passed filter.  Note that all data sources must support this method
     * at a minimum.
     *
     * @param collection The collection to put the features into.
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     *
     * @throws DataSourceException For all data source errors.
     */
    public void getFeatures(FeatureCollection collection, Filter filter)
        throws DataSourceException {
        parse();

        FilteringIteration.filter(features, filter);
        collection.addAll(features);
    }

    /**
     * Loads all features from the datasource into the returned collection.
     * Filter.NONE can also be used to get all features.  Calling this
     * function is equivalent to using {@link Query#ALL}
     *
     * @return Collection The collection to put the features into.
     *
     * @throws DataSourceException For all data source errors.
     */
    public FeatureCollection getFeatures() throws DataSourceException {
        return getFeatures(Query.ALL);
    }

    /**
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.  Right now only gets the type of the first
     * Feature.  If they are all the same type that's fine, but in gml it
     * could easily not be.
     *
     * @return The featureType of the first Feature.
     *
     * @throws DataSourceException if there are any problems getting the
     *         schema.
     *
     * @task REVISIT: What we really want here is a schema reader, that can get
     *       the featureTypes for all the features that might appear in the
     *       gml.  But our DataSource interface doesn't even support more than
     *       one FeatureType per datasource right now, so that doesn't even
     *       really work.
     */
    public FeatureType getSchema() throws DataSourceException {
        parse();

        if (features.size() > 0) {
            return features.features().next().getFeatureType();
        } else {
            //what to do when no parsing occured?
            throw new DataSourceException("no features were parsed");
        }
    }

    /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.  A convenience
     * method for single attribute modifications.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     *
     * @throws DataSourceException If modificaton is not supported, if the
     *         object type do not match the attribute type.
     * @throws UnsupportedOperationException if the datasource does not support
     *         this operation
     *
     * @task TODO: Implement support for modification of features (single
     *       attribute)
     */
    public void modifyFeatures(AttributeType type, Object value, Filter filter)
        throws DataSourceException, UnsupportedOperationException {
        super.modifyFeatures(type, value, filter);
    }

    /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     *
     * @throws DataSourceException If modificaton is not supported, if the
     *         attribute and object arrays are not eqaul length, or  if the
     *         object types do not match the attribute types.
     * @throws UnsupportedOperationException if the datasource does not support
     *         this operation
     *
     * @task TODO: Implement support for modification of feature (multi
     *       attribute)
     */
    public void modifyFeatures(AttributeType[] type, Object[] value,
        Filter filter)
        throws DataSourceException, UnsupportedOperationException {
        super.modifyFeatures(type, value, filter);
    }

    /**
     * Removes all of the features specificed by the passed filter from the
     * collection.
     *
     * @param filter An OpenGIS filter; specifies which features to remove.
     *
     * @throws DataSourceException If anything goes wrong or if deleting is not
     *         supported.
     * @throws UnsupportedOperationException if the datasource does not support
     *         this operation
     *
     * @task TODO: Implement support for removal of features
     */
    public void removeFeatures(Filter filter)
        throws DataSourceException, UnsupportedOperationException {
        super.removeFeatures(filter);
    }

    /**
     * Gets an InputSource for the URI to parse
     *
     * @return InputSource InputSource for the URI to parse
     *
     * @throws DataSourceException if anything goes wrong
     */
    private InputSource getInputSource() throws DataSourceException {
        InputStream in;

        try {
            in = url.openStream();
        } catch (IOException e) {
            throw new DataSourceException("Error reading url " + url.toString()
                + " in GMLGeometryDataSource" + "\n" + e);
        }

        return new InputSource(in);
    }

    /**
     * Parses the gml held in the URI and stores the Features
     *
     * @throws DataSourceException for any errors
     *
     * @task REVISIT: I'm putting in a quick flag so that the iteration need
     *       not be done every single time getFeatures() is called.  But this
     *       should be thought through some more.  It seems like without this
     *       calling getFeatures() will cause the backend FeatureCollection to
     *       recieve even more features.  But I could be wrong, correct this
     *       if so, but I'm thinking that no one knows this code that well...
     */
    private void parse() throws DataSourceException {
        LOGGER.finer("has this been parsed before? " + parsed);

        if (!parsed) {
            try {
                //GMLFilterFeature featureFilter = new GMLFilterFeature(this);
                GMLFilterFeature featureFilter = new GMLFilterFeature(new GMLReceiver(
                            features));
                GMLFilterGeometry geometryFilter = new GMLFilterGeometry(featureFilter);
                GMLFilterDocument documentFilter = new GMLFilterDocument(geometryFilter);

                //XMLReader parser = XMLReaderFactory.createXMLReader(defaultParser);
                SAXParserFactory fac = SAXParserFactory.newInstance();
                SAXParser parser = fac.newSAXParser();

                ParserAdapter p = new ParserAdapter(parser.getParser());
                p.setContentHandler(documentFilter);
                p.parse(getInputSource());
                parsed = true;
            } catch (IOException e) {
                throw new DataSourceException("Error reading URI: " + url);
            } catch (SAXException e) {
                throw new DataSourceException("Parsing error: "
                    + e.getMessage());
            } catch (ParserConfigurationException e) {
                throw new DataSourceException("Parsing error: "
                    + e.getMessage());
            }
        }
    }

    /**
     * Creates the a metaData object.
     *
     * @return the metadata for this datasource.
     *
     * @see #MetaDataSupport
     */
    protected DataSourceMetaData createMetaData() {
        MetaDataSupport metaData = new MetaDataSupport();
        metaData.setSupportsGetBbox(true);

        return metaData;
    }
}
