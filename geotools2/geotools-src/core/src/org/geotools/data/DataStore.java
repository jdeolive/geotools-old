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
package org.geotools.data;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import java.io.IOException;


/**
 * Represents a Physical Store for FeatureTypes.
 * 
 * <p>
 * The source of data for FeatureTypes. Shapefiles, databases tables, etc. are
 * referenced through this interface.
 * </p>
 * 
 * <p>
 * Summary of our requirements:
 * </p>
 * 
 * <ul>
 * <li>
 * Provides lookup of available Feature Types
 * </li>
 * <li>
 * Provides access to low-level Readers/Writers API for a feature type
 * </li>
 * <li>
 * Provides access to high-level FeatureSource/Store/Locking API a feature type
 * </li>
 * <li>
 * Handles the conversion of filters into data source specific queries
 * </li>
 * <li>
 * Handles creation of new Feature Types
 * </li>
 * <li>
 * Provides access of Feature Type Schema information
 * </li>
 * </ul>
 * 
 * Suggestions:
 * 
 * <ul>
 * <li>
 * Chris - We may need to switch the getFeatureReader to be based on Query * 
 * </li>
 * <li>
 * Jody - I agree, propose:
 * <pre><code>
 * getFeatureReader( Query, Transaction )
 * - Transaction is still orthognal
 * 
 * getFeatureSource( Query )
 * - Query allows override, and reprojection...
 * 
 * getFeatureStore( typeName )
 * - write access is till by typeName
 *   (just like getFeatureWriter)
 * - consider spliting API into to based on read-only, read/write ability? 
 * </code></pre>
 * </li>
 * </ul>
 * 
 *
 * @author Jody Garnett, Refractions Research
 * @version $Id: DataStore.java,v 1.3 2004/01/09 22:29:31 jive Exp $
 */
public interface DataStore {
    /**
     * Retrieves a list of of the available FeatureTypes
     * 
     * <p>
     * This is simply a list of the FeatureType names as aquiring the actual
     * FeatureType schemas may be expensive.
     * </p>
     *
     * @return typeNames for available FeatureTypes.
     */
    String[] getTypeNames();

    /**
     * Retrieve FeatureType metadata by <code>typeName</code>.
     * 
     * <p>
     * Retrieves the Schema information as a FeatureType object.
     * </p>
     *
     * @param typeName typeName of requested FeatureType
     *
     * @return FeatureType for the provided typeName
     *
     * @throws IOException If typeName cannot be found
     */
    FeatureType getSchema(String typeName) throws IOException;

    /**
     * Creates storage for a new <code>featureType</code>.
     * 
     * <p>
     * The provided <code>featureType</code> we be accessable by the typeName
     * provided by featureType.getTypeName().
     * </p>
     *
     * @param featureType FetureType to add to DataStore
     *
     * @throws IOException If featureType cannot be created
     */
    void createSchema(FeatureType featureType) throws IOException;

    /**
     * Used to force namespace and CS info into a persistent change.
     * <p>
     * The provided featureType should completely cover the existing schema.
     * All attributes should be accounted for and the typeName should match.
     * </p>
     * <p>
     * Suggestions:
     * </p>
     * <ul>
     * <li>Sean - don't do this</li>
     * <li>Jody - Just allow changes to metadata: CS, namespace, and others</li> 
     * <li>James - Allow change/addition of attribtues</li> 
     * </ul>
     * @param typeName
     * @throws IOException
     */
    void updateSchema( String typeName, FeatureType featureType ) throws IOException;

    /**
     * Access a FeatureSource for Query providing a high-level API.
     * <p>
     * The provided Query does not need to completely cover the existing
     * schema for Query.getTypeName(). The result will mostly likely only be
     * a FeatureSource and probably wont' allow write access by the
     * FeatureStore method.
     * </p>
     * <p>
     * By using Query we allow support for reprojection, in addition
     * to overriding the CoordinateSystem used by the native FeatureType.
     * </p>
     * <p>
     * We may wish to limit this method to only support Queries using
     * Filter.ALL.
     * </p>
     * <p>
     * Update - GeoServer has an elegatent implementation of this functionality
     * that we could steal. GeoServerFeatureSource, GeoServerFeatureStore and
     * GeoServerFeatureLocking serve as a working prototype.
     * </p> 
     * @param Query Query.getTypeName() locates FeatureType being viewed
     *
     * @return FeatureSource providing opperations for featureType
     * @throws IOException If FeatureSource is not available
     * @throws SchemaException If fetureType is not covered by existing schema
     */
    //FeatureSource getView( Query query ) throws IOException, SchemaException;
    
    /**
     * Access a FeatureSource for typeName providing a high-level API.
     * 
     * <p>
     * The resulting FeatureSource may implment more functionality:
     * </p>
     * <pre><code>
     * 
     * FeatureSource fsource = dataStore.getFeatureSource( "roads" );
     * FeatureStore fstore = null;
     * if( fsource instanceof FeatureLocking ){
     *     fstore = (FeatureStore) fs;
     * }
     * else {
     *     System.out.println("We do not have write access to roads");
     * }
     * </code>
     * </pre>
     *
     * @param typeName
     *
     * @return FeatureSource (or subclass) providing opperations for typeName
     */
    FeatureSource getFeatureSource(String typeName) throws IOException;

    /**
     * Access a FeatureReader providing access to Feature information.
     * 
     * <p>
     * <b>Filter</b> is used as a low-level indication of constraints.
     * (Implementations may resort to using a FilteredFeatureReader, or
     * provide their own optimizations)
     * </p>
     * 
     * <p>
     * <b>FeatureType</b> provides a template for the returned FeatureReader
     * </p>
     * 
     * <ul>
     * <li>
     * featureType.getTypeName(): used by JDBC as the table reference to query
     * against. Shapefile reader may need to store a lookup to the required
     * filename.
     * </li>
     * <li>
     * featureType.getAttributeTypes(): describes the requested content. This
     * may be a subset of the complete FeatureType defined by the DataStore.
     * </li>
     * <li>
     * getType.getNamespace(): describes the requested namespace for the
     * results (may be different then the one used internally)
     * </li>
     * </ul>
     * 
     * <p>
     * <b>Transaction</b> to externalize DataStore state on a per Transaction
     * basis. The most common example is a JDBC datastore saving a Connection
     * for use across several FeatureReader requests. Similarly a Shapefile
     * reader may wish to redirect FeatureReader requests to a alternate
     * filename over the course of a Transaction.
     * </p>
     * 
     * <p>
     * <b>Notes For Implementing DataStore</b>
     * </p>
     * 
     * <p>
     * Subclasses may need to retrieve additional attributes, beyond those
     * requested by featureType.getAttributeTypes(), in order to correctly
     * apply the <code>filter</code>.<br>
     * These Additional <b>attribtues</b> should be not be returned by
     * FeatureReader. Subclasses may use ReTypeFeatureReader to aid in
     * acomplishing this.
     * </p>
     * <p>
     * Helper classes for implementing a FeatureReader (in order):
     * </p>
     * <ul>
     * <li>
     * DefaultFeatureReader
     * - basic support for creating a FeatureReader for an AttributeReader
     * </li>
     * <li>
     * FilteringFeatureReader
     * - filtering support
     * </li>
     * <li>
     * DiffFeatureReader
     * - In-Process Transaction Support (see TransactionStateDiff)
     * </li>
     * <li>
     * ReTypeFeatureReader
     * - Feature Type schema manipulation of namesspace and attribute type subsets
     * </li>
     * <li>
     * EmptyFeatureReader
     * - provides no content for Filter.ALL optimizations
     * </li>
     * </ul>
     * <p>
     * Sample use (not optimized):
     * </p>
     * <pre><code>
     * if (filter == Filter.ALL) {
     *      return new EmptyFeatureReader(featureType);
     *  }
     *
     *  String typeName = featureType.getTypeName();
     *  FeatureType schema = getSchema( typeName );
     *  FeatureReader reader = new DefaultFeatureReader( getAttributeReaders(), schema );
     *
     *  if (filter != Filter.NONE) {
     *      reader = new FilteringFeatureReader(reader, filter);
     *  }
     *
     *  if (transaction != Transaction.AUTO_COMMIT) {
     *      Map diff = state(transaction).diff(typeName);
     *      reader = new DiffFeatureReader(reader, diff);
     *  }
     *
     *  if (!featureType.equals(reader.getFeatureType())) {
     *      reader = new ReTypeFeatureReader(reader, featureType);
     *  }
     * return reader
     * </code></pre>
     * <p>
     * Locking support does not need to be provided for FeatureReaders.
     * </p>
     * 
     * <p>
     * Suggestions:
     * </p>
     * 
     * <ul>
     * <li>Jody: This method has been updated to use the Query object
     *    (Thanks for the suggestion Sean & Chris)
     * </li>
     * </ul>
     * 
     *
     * @param query Requested form of the returned Features and the filter used
     *              to constraints the results
     * @param transaction Transaction this query opperates against
     *
     * @return FeatureReader Allows Sequential Processing of featureType
     */
    FeatureReader getFeatureReader( Query query, Transaction transaction ) throws IOException;
    //FeatureReader getFeatureReader(FeatureType featureType, Filter filter, Transaction transaction) throws IOException;

    /**
     * Access FeatureWriter for modification of the DataStore contents.
     * 
     * <p>
     * The constructed FeatureWriter will be placed at the start of the
     * provided <code>store</code>.
     * </p>
     * 
     * <p>
     * To limit FeatureWriter to the FeatureTypes defined by this DataStore,
     * typeName is used to indicate FeatureType. The resulting 
     * feature writer will allow modifications against the
     * same FeatureType provided by getSchema( typeName )  
     * </p>
     * 
     * <b>Notes For Implementing DataStore</b>
     * </p>
     * 
     * <p>
     * The returned FeatureWriter does not support the addition on new Features
     * to FeatureType (it would need to police your modifications to agree
     * with <code>filer</code>).  As such it will return <code>false</code>
     * for getNext() when it reaches the end of the Query and
     * NoSuchElementException when next() is called.
     * </p>
     * 
     * <p>
     * Helper classes for implementing a FeatureWriter (in order):
     * </p>
     * <li>
     * InProcessLockingManager.checkedWriter( writer )
     * - provides a check against locks before allowing modification
     * 
     * <li>
     * FilteringFeatureWriter
     * - filtering support for FeatureWriter (does not allow new content)
     * </li>
     * <li>
     * DiffFeatureWriter
     * - In-Process Transaction Support (see TransactionStateDiff)
     * </li>
     * <li>
     * EmptyFeatureWriter
     * - provides no content for Filter.ALL optimizations
     * </li>
     * </ul>
     * 
     * @param typeName Indicates featureType to be modified
     * @param filter constraints used to limit the modification
     * @param transaction Transaction this query opperates against
     *
     * @return FeatureWriter Allows Sequential Modification of featureType
     */
    FeatureWriter getFeatureWriter(String typeName, Filter filter,
        Transaction transaction) throws IOException;

    /**
     * Access FeatureWriter for modification of the DataStore typeName.
     * 
     * <p>
     * FeatureWriters will need to be limited to the FeatureTypes defined by
     * the DataStore, the easiest way to express this limitation is to the
     * FeatureType by a provided typeName.
     * </p>
     * 
     * <p>
     * The returned FeatureWriter will return <code>false</code> for getNext()
     * when it reaches the end of the Query.
     * </p>
     *
     * @param typeName Indicates featureType to be modified
     * @param transaction Transaction this query opperates against
     *
     * @return FeatureReader Allows Sequential Processing of featureType
     */
    FeatureWriter getFeatureWriter(String typeName, Transaction transaction)
        throws IOException;

    /**
     * Aquire a FeatureWriter for adding new content to a FeatureType.
     * 
     * <p>
     * This FeatureWriter will return <code>false</code> for hasNext(), however
     * next() may be used to aquire new Features that may be writen out to add
     * new content.
     * </p>
     *
     * @param typeName
     * @param transaction
     *
     * @return
     *
     * @throws IOException
     */
    FeatureWriter getFeatureWriterAppend(String typeName,
        Transaction transaction) throws IOException;

    /**
     * Retrieve a per featureID based locking service from this DataStore.
     * 
     * <p>
     * It is common to return an instanceof InProcessLockingManager for
     * DataStores that do not provide native locking.
     * </p>
     * 
     * <p>
     * AbstractFeatureLocking makes use of this service to provide locking
     * support. You are not limitied by this implementation and may simply
     * return <code>null</code> for this value.
     * </p>
     *
     * @return DataStores may return <code>null</code>, if the handling locking
     *         in another fashion.
     */
    LockingManager getLockingManager();
}
