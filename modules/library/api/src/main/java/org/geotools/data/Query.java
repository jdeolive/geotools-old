/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.data;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.factory.Hints;

/**
 * Encapsulates a request for data, typically as:
 * <pre><code>
 * myFeatureSource.getFeatures(Query);
 * </code></pre>
 *
 * The query class is based on the Web Feature Server specification and offers a 
 * few interesting capabilities such as the ability to sort results and use a filter
 * (similar to the where clause in SQL).
 * <p>
 * Additional capabilities:
 * <ul>
 * <li>
 * {@link #setMaxFeatures(int)} and {@link #setStartIndex(int)} can be used to mirror the idea
 * of paging through content. This is useful when the FeatureSource has an upper limit of the
 * number of features it can return to you in one gulp or you want to limit the number that
 * you process.
 * </li>
 * <li>
 * {@link #setHandle(String) is simply used to name the query; often indicating what you are up to
 * when perform the query. This name will be used in error reporting and logs.
 * </li>
 * <li>
 * {@link #setCoordinateSystem()} is used to to indicate the CoordinateReferenceSystem the results are
 * "forced" into. You can use this to correct when a featureSource is *wrong* about the information
 * it is returning. This is often used to correct a featureSource when the application and the data
 * format have different ideas about the "axis order" issue (where "EPSG:4325" is to be considered
 * in long/lat order or lat/long order)</li>
 * <li>
 * {@link #getCoordinateSystemReproject()} is used to ask for the information to be reproejcted. This should
 * occur "natively" if the featureSource implementor has such facilities in their WFS or database.
 * If not we have provided helper code to allow implementors to reproject at the Java level for
 * formats like shapefile.
 * </li>
 * </ul>
 *
 * Vendor specific:
 * <ul>
 * <li>getHints() is used to access venfor specific capabilities provided by FeatureSource
 * implementations.
 * </li>
 * </ul>
 * Example:<pre><code>
 * Filter filter = CQL.toFilter("NAME like '%land'");
 * Query query = new Query( "countries", filter );
 *
 * FeatureCollection features = featureSource.getFeatures( query );
 * </code></pre>
 *
 * @author Chris Holmes
 * @source $URL$
 * @version $Id$
 */
public class Query {
    /**
     * Constant (actually null) used to represent no namespace restrictions on the returned result, should be considered ANY_URI
     */
    public static final URI NO_NAMESPACE = null;

    /** So getMaxFeatures does not return null we use a very large number. */
    public static final int DEFAULT_MAX = Integer.MAX_VALUE;

    /**
     * Implements a query that will fetch all features from a datasource. This
     * query should retrieve all properties, with no maxFeatures, no
     * filtering, and the default featureType.
     */
    public static final Query ALL = new ALLQuery();

    /**
     * Implements a query that will fetch all the FeatureIDs from a datasource.
     * This query should retrive no properties, with no maxFeatures, no
     * filtering, and the a featureType with no attribtues.
     */
    public static final Query FIDS = new FIDSQuery();

    /**
     * Ask for no properties when used with setPropertyNames.
     *
     * <p>
     * Note the query will still return a result - limited to FeatureIDs.
     * </p>
     */
    public static final String[] NO_NAMES = new String[0];

    /** Ask for all properties when used with setPropertyNames. */
    public static final String[] ALL_NAMES = null;

    /** The properties to fetch */
    protected String[] properties;

    /** The maximum numbers of features to fetch */
    protected int maxFeatures = Query.DEFAULT_MAX;

    protected Integer startIndex = null;
    
    /** The filter to constrain the request. */
    protected Filter filter = Filter.INCLUDE;

    /** The typeName to get */
    protected String typeName;

    /** The namespace to get */
    protected URI namespace =Query.NO_NAMESPACE;

    /** The handle associated with this query. */
    protected String handle;

    /** Coordinate System associated with this query */
    protected CoordinateReferenceSystem coordinateSystem;
    
    /** Reprojection associated associated with this query */
    protected CoordinateReferenceSystem coordinateSystemReproject;
    
    /** Sorting for the query */
    protected SortBy[] sortBy;
    
    /** The version according to WFS 1.0 and 1.1 specs */
    protected String version;
    
    /** The hints to be used during query execution */
    protected Hints hints;
    
    /**
     * Create a query, please configure before use (as the default values
     * will select all content).
     * 
     */
    public Query() {
        // no arg
    }
    /**
     * Query with indicated typeName, will retrieve
     * all contents for the provided typeName.
     * <p>
     * </p>
     * @param typeName the name of the featureType to retrieve
     */
    public Query( String typeName ){
        this( typeName, Filter.INCLUDE );
    }
    
    /**
     * Query content with matching typeName as selected by the provided filter.
     * <p>
     * Please note that current featureSource implementations
     * generally have one kind of content.
     * 
     * @param typeName the name of the featureType to retrieve.
     * @param filter the OGC filter to constrain the request.
     */
    public Query(String typeName, Filter filter) {
        this( typeName, filter, Query.ALL_NAMES );        
    }
    /**
     * Constructor that sets the filter and properties
     * @param typeName 
     *
     * @param filter the OGC filter to constrain the request.
     * @param properties an array of the properties to fetch.
     */
    public Query(String typeName, Filter filter, String[] properties) {
        this( typeName, null, filter, Query.DEFAULT_MAX, properties, null );        
    }    
    /**
     * Constructor that sets all fields.
     *
     * @param typeName the name of the featureType to retrieve.
     * @param filter the OGC filter to constrain the request.
     * @param maxFeatures the maximum number of features to be returned.
     * @param propNames an array of the properties to fetch.
     * @param handle the name to associate with the query.
     */
    public Query(String typeName, Filter filter, int maxFeatures,
        String[] propNames, String handle) {
        this(typeName, null, filter, maxFeatures, propNames, handle );
    }
    
    /**
     * Constructor that sets all fields.
     *
     * @param typeName the name of the featureType to retrieve.
     * @param namespace Namespace for provided typeName, or null if unspecified
     * @param filter the OGC filter to constrain the request.
     * @param maxFeatures the maximum number of features to be returned.
     * @param propNames an array of the properties to fetch.
     * @param handle the name to associate with the query.
     */
    public Query( String typeName, URI namespace, Filter filter, int maxFeatures,
        String[] propNames, String handle) {
        this.typeName = typeName;
        this.filter = filter;
        this.namespace = namespace;
        this.properties = propNames;
        this.maxFeatures = maxFeatures;
        this.handle = handle;
    }
    
    /**
     * Copy contructor, clones the state of a generic Query into a DefaultQuery
     * @param query
     */
    public Query(Query query) {
      this(query.getTypeName(), query.getNamespace(), query.getFilter(), query.getMaxFeatures(),
          query.getPropertyNames(), query.getHandle());
      this.sortBy = query.getSortBy();
      this.coordinateSystem = query.getCoordinateSystem();
      this.coordinateSystemReproject = query.getCoordinateSystemReproject();
      this.version = query.getVersion();
      this.hints = query.getHints();
      this.startIndex = query.getStartIndex();
    }
    /**
     * The properties array is used to specify the attributes that should be
     * selected for the return feature collection.
     *
     * <ul>
     * <li>
     * ALL_NAMES: <code>null</code><br>
     * If no properties are specified (getProperties returns ALL_NAMES or
     * null) then the full schema should  be used (all attributes).
     * </li>
     * <li>
     * NO_NAMES: <code>new String[0]</code><br>
     * If getProperties returns an array of size 0, then the datasource should
     * return features with no attributes, only their ids.
     * </li>
     * </ul>
     *
     * <p>
     * The available properties can be determined with a getSchema call from
     * the DataSource interface.  A datasource can use {@link
     * #retrieveAllProperties()} as a shortcut to determine if all its
     * available properties should be returned (same as checking to see if
     * getProperties is ALL_NAMES, but clearer)
     * </p>
     *
     * <p>
     * If properties that are not part of the datasource's schema are requested
     * then the datasource shall throw an exception.
     * </p>
     *
     * <p>
     * This replaces our funky setSchema method of retrieving select
     * properties.  It makes it easier to understand how to get certain
     * properties out of the datasource, instead of having users get the
     * schema and then compose a new schema using the attributes that they
     * want.  The old way had problems because one couldn't have multiple
     * object reuse the same datasource object, since some other object could
     * come along and change its schema, and would then return the wrong
     * properties.
     * </p>
     *
     * @return the attributes to be used in the returned FeatureCollection.
     *
     * @task REVISIT: make a FidProperties object, instead of an array size 0.
     *       I think Query.FIDS fills this role to some degree.
     *       Query.FIDS.equals( filter ) would meet this need?
     */
    public String[] getPropertyNames() {
        return properties;
    }
    /**
     * Sets the properties to retrieve from the db.  If the boolean to load all
     * properties is set to true then the AttributeTypes that are not in the
     * database's schema will just be filled with null values.
     *
     * @param propNames The names of attributes to load from the datasouce.
     */
    public void setPropertyNames(String[] propNames) {
        this.properties = propNames;
    }
    
    /**
     * Sets the proper attributeTypes constructed from a schema and a  list of
     * propertyNames.
     *
     * @param propNames the names of the properties to check against the
     *        schema. If null then all attributes will be returned.  If a List
     *        of size 0 is used then only the featureIDs should be used.
     *
     * @task REVISIT: This syntax is really obscure.  Consider having an fid or
     *       featureID propertyName that datasource implementors look for
     *       instead of looking to see if the list size is 0.
     */
    public void setPropertyNames(List<String> propNames) {
        if (propNames == null) {
            this.properties = null;

            return;
        }

        String[] stringArr = new String[propNames.size()];
        this.properties = (String[]) propNames.toArray(stringArr);
    }
    
    /**
     * Convenience method to determine if the query should use the full schema
     * (all properties) of the data source for the features returned.  This
     * method is equivalent to if (query.getProperties() == null), but allows
     * for more clarity on the part of datasource implementors, so they do not
     * need to examine and use null values.  All Query implementations should
     * return true for this function if getProperties returns null.
     *
     * @return if all datasource attributes should be included in the schema of
     *         the returned FeatureCollection.
     */
    public boolean retrieveAllProperties() {
        return properties == null;
    }

    /**
     * The optional maxFeatures can be used to limit the number of features
     * that a query request retrieves.  If no maxFeatures is specified then
     * all features should be returned.
     *
     * <p>
     * This is the only method that is not directly out of the Query element in
     * the WFS spec.  It is instead a part of a GetFeature request, which can
     * hold one or more queries.  But each of those in turn will need a
     * maxFeatures, so it is needed here.
     * </p>
     *
     * @return the max features the getFeature call should return.
     */
    public int getMaxFeatures() {
        return this.maxFeatures;
    }
    
    /**
     * Sets the max features to retrieved by this query.
     *
     * @param maxFeatures the maximum number of features the getFeature call
     *        should return.
     */
    public void setMaxFeatures(int maxFeatures) {
        this.maxFeatures = maxFeatures;
    }
    
    public Integer getStartIndex(){
        return this.startIndex;
    }

    public void setStartIndex(Integer startIndex){
        if(startIndex != null && startIndex.intValue() < 0){
            throw new IllegalArgumentException("startIndex shall be a positive integer: " + startIndex);
        }
        this.startIndex = startIndex;
    }
    /**
     * The Filter can be used to define constraints on a query.  If no Filter
     * is present then the query is unconstrained and all feature instances
     * should be retrieved.
     *
     * @return The filter that defines constraints on the query.
     */
    public Filter getFilter() {
        return this.filter;
    }

    /**
     * Sets the filter to constrain the query.
     *
     * @param filter the OGC filter to limit the datasource getFeatures
     *        request.
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }
    
    /**
     * The typeName attribute is used to indicate the name of the feature type
     * to be queried.  If no typename is specified, then the default typeName
     * should be returned from the dataStore.  If the datasstore only supports
     * one feature type then this part of the query may be ignored.
     *
     * @return the name of the feature type to be returned with this query.
     */
    public String getTypeName() {
        return this.typeName;
    }

    /**
     * Sets the typename.
     *
     * @param typeName the name of the featureType to retrieve.
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
    
    /**
     * The namespace attribute is used to indicate the namespace of the schema
     * being represented.
     *
     * @return the gml namespace of the feature type to be returned with this
     *         query
     */
    public URI getNamespace() {
        return namespace;
    }

    /**
     * Set the namespace of the type name.
     * 
     * @param namespace namespace of the type name
     */
    public void setNamespace(URI namespace) {
        this.namespace = namespace;
    }
    
    /**
     * The handle attribute is included to allow a client to associate  a
     * mnemonic name to the Query request. The purpose of the handle attribute
     * is to provide an error handling mechanism for locating  a statement
     * that might fail.
     *
     * @return the mnemonic name of the query request.
     */
    public String getHandle() {
        return this.handle;
    }

    /**
     * Sets a mnemonic name for the query request.
     *
     * @param handle the name to refer to this query.
     */
    public void setHandle(String handle) {
        this.handle = handle;
    }
    
    /**
     * From WFS Spec:  The version attribute is included in order to
     * accommodate systems that  support feature versioning. A value of ALL
     * indicates that all versions of a feature should be fetched. Otherwise
     * an integer, n, can be specified  to return the n th version of a
     * feature. The version numbers start at '1'  which is the oldest version.
     * If a version value larger than the largest version is specified then
     * the latest version is return. The default action shall be for the query
     * to return the latest version. Systems that do not support versioning
     * can ignore the parameter and return the only version  that they have.
     *
     * @return the version of the feature to return, or null for latest.
     */
    public String getVersion() {
        return version; 
    }
    
    /**
     * @see #getVersion()
     * @param version
     * @since 2.4
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * Specifies the coordinate system that the features being queried are in.
     *
     * <p>
     * This denotes a request to Temporarily to override the coordinate system
     * contained in the SimpleFeatureSource being queried. The same coordinate
     * values will be used, but the features created will appear in this
     * Coordinate System.
     * </p>
     *
     * <p>
     * This change is not persistant at all, indeed it is only for the Features
     * returned by this Query. If used in conjunction with {@link #getCoordinateSystemReproject()}
     * the reprojection will occur from {@link #getCoordinateSystem()} to
     * {@link #getCoordinateSystemReproject()}.
     * </p>
     *
     * @return The coordinate system to be returned for Features from this
     *         Query (override the set coordinate system).
     */
    public CoordinateReferenceSystem getCoordinateSystem() {
        return coordinateSystem;
    }
    
    public void setCoordinateSystem(CoordinateReferenceSystem system) {
        coordinateSystem = system;
    }
    
    /**
     * Request data reprojection.
     *
     * <p>
     * Gets the coordinate System to reproject the data contained in the
     * backend datastore to.
     * </p>
     *
     * <p>
     * If the DataStore can optimize the reprojection it should, if not then a
     * decorator on the reader should perform the reprojection on the fly.
     * </p>
     *
     * <p>
     * If the datastore has the wrong CS then {@link #getCoordinateSystem()} should be set to
     * the CS to be used, this will perform the reprojection on that.
     * </p>
     *
     * @return The coordinate system that Features from the datasource should
     *         be reprojected to.
     */
    public CoordinateReferenceSystem getCoordinateSystemReproject() {
        return coordinateSystemReproject;
    }
    
    public void setCoordinateSystemReproject(CoordinateReferenceSystem system) {
        coordinateSystemReproject = system;
    }
    
    /**
     * SortBy results according to indicated property and order.
     * <p>
     * SortBy is part of the Filter 1.1 specification, it is referenced
     * by WFS1.1 and Catalog 2.0.x specifications and is used to organize
     * results.
     * </p>
     * The SortBy's are ment to be applied in order:
     * <ul>
     * <li>SortBy( year, ascending )
     * <li>SortBy( month, decsending )
     * </ul>
     * Would produce something like: <pre><code>
     * [year=2002 month=4],[year=2002 month=3],[year=2002 month=2],
     * [year=2002 month=1],[year=2003 month=12],[year=2002 month=4],
     * </code></pre>
     * </p>
     * <p>
     *
     * SortBy should be considered at the same level of abstraction as Filter,
     * and like Filter you may sort using properties not listed in
     * getPropertyNames.
     * </p>
     *
     * <p>
     * At a technical level the interface SortBy2 is used to indicate the
     * additional requirements of a GeoTools implementation. The pure
     * WFS 1.1 specification itself is limited to SortBy.
     * </p>
     *
     * @return SortBy array or order of application
     */
    public SortBy[] getSortBy() {
        return sortBy;
    } 

    /**
     * Sets the sort by information.
     * 
     */
    public void setSortBy(SortBy[] sortBy) {
        this.sortBy = sortBy;
    }
    
    /**
     * Specifies some hints to drive the query execution and results build-up.
     * Hints examples can be the GeometryFactory to be used, a generalization
     * distance to be applied right in the data store, to data store specific
     * things such as the fetch size to be used in JDBC queries.
     * The set of hints supported can be fetched by calling
     * {@links FeatureSource#getSupportedHints()}.
     * Depending on the actual values of the hints, the data store is free to ignore them.
     * No mechanism is in place, at the moment, to figure out which hints where
     * actually used during the query execution.
     * @return the Hints the data store should try to use when executing the query
     *         (eventually empty but never null).
     */
    public Hints getHints() {
        if(hints == null){
            hints = new Hints(Collections.EMPTY_MAP);
        }
        return hints;
    }
    
    /**
     * Sets the query hints
     * @param hints
     */
    public void setHints(Hints hints) {
        this.hints = hints;
    }
    
    /**
     * Hashcode based on propertyName, maxFeatures and filter.
     *
     * @return hascode for filter
     */
    public int hashCode() {
        String[] n = getPropertyNames();

        return ((n == null) ? (-1)
                                    : ((n.length == 0) ? 0 : (n.length
                | n[0].hashCode()))) | getMaxFeatures()
                | ((getFilter() == null) ? 0 : getFilter().hashCode())
                | ((getTypeName() == null) ? 0 : getTypeName().hashCode())
                | ((getVersion() == null) ? 0 : getVersion().hashCode())
                | ((getCoordinateSystem() == null) ? 0 : getCoordinateSystem().hashCode())
                | ((getCoordinateSystemReproject() == null) ? 0 : getCoordinateSystemReproject().hashCode())
                | getStartIndex();
    }
    
    /**
     * Equality based on propertyNames, maxFeatures, filter, typeName and
     * version.
     * 
     * <p>
     * Changing the handle does not change the meaning of the Query.
     * </p>
     *
     * @param obj Other object to compare against
     *
     * @return <code>true</code> if <code>obj</code> matches this filter
     */
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof Query)) {
            return false;
        }
        if (this == obj) return true;        
        Query other = (Query) obj;
        
        return Arrays.equals(getPropertyNames(), other.getPropertyNames())
        && (retrieveAllProperties() == other.retrieveAllProperties())
        && (getMaxFeatures() == other.getMaxFeatures())
        && ((getFilter() == null) ? (other.getFilter() == null)
                                  : getFilter().equals(other.getFilter()))
        && ((getTypeName() == null) ? (other.getTypeName() == null)
                                    : getTypeName().equals(other.getTypeName()))
        && ((getVersion() == null) ? (other.getVersion() == null)
                                   : getVersion().equals(other.getVersion()))
        && ((getCoordinateSystem() == null) ? (other.getCoordinateSystem() == null)
                                           : getCoordinateSystem().equals(other.getCoordinateSystem()))
        && ((getCoordinateSystemReproject() == null) ? (other.getCoordinateSystemReproject() == null)
                                                   : getCoordinateSystemReproject().equals(other.getCoordinateSystemReproject()))                                           
        && (getStartIndex() == other.getStartIndex());
    }
    
    /**
     * Over ride of toString
     *
     * @return a string representation of this query object.
     */
    public String toString() {
        StringBuffer returnString = new StringBuffer("Query:");

        if (handle != null) {
            returnString.append(" [" + handle + "]");
        }

        returnString.append("\n   feature type: " + typeName);

        if (filter != null) {
            returnString.append("\n   filter: " + filter.toString());
        }

        returnString.append("\n   [properties: ");

        if ((properties == null) || (properties.length == 0)) {
            returnString.append(" ALL ]");
        } else {
            for (int i = 0; i < properties.length; i++) {
                returnString.append(properties[i]);

                if (i < (properties.length - 1)) {
                    returnString.append(", ");
                }
            }

            returnString.append("]");
        }
        
        if(sortBy != null && sortBy.length > 0) {
        returnString.append("\n   [sort by: ");
            for (int i = 0; i < sortBy.length; i++) {
                returnString.append(sortBy[i].getPropertyName().getPropertyName());
                returnString.append(" ");
                returnString.append(sortBy[i].getSortOrder().name());

                if (i < (sortBy.length - 1)) {
                    returnString.append(", ");
                }
            }

            returnString.append("]");
        }
        
        return returnString.toString();
    }
}
