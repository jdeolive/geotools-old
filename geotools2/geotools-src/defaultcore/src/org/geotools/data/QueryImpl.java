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

import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * The query object is used by the {@link DataSource#GetFeature} method of the
 * DataSource interface, to encapsulate a request.  It defines which feature
 * type  to query, what properties to retrieve and what constraints (spatial
 * and non-spatial) to apply to those properties.  It is designed to  closesly
 * match a WFS Query element of a GetFeature request.   The only difference is
 * the addition of the maxFeatures element, which  allows more control over
 * the features selected.  It allows a full  GetFeature request to properly
 * control how many features it gets from each query, instead of requesting
 * and discarding when the max is reached.
 *
 * @author Chris Holmes
 */
public class QueryImpl implements Query {
    /** The properties to fetch */
    private AttributeType[] properties;

    /** The maximum numbers of features to fetch */
    private int maxFeatures = 100000000;

    /** The filter to constrain the request. */
    private Filter filter;

    /** The typeName to get */
    private String typeName;

    /** The handle associated with this query. */
    private String handle;

    /**
     * No argument constructor.
     */
    public QueryImpl() {
    }

    /**
     * Constructor that sets the filter.
     *
     * @param filter the OGC filter to constrain the request.
     */
    public QueryImpl(Filter filter) {
        this.filter = filter;
    }

    /**
     * Constructor that sets the filter and properties
     *
     * @param filter the OGC filter to constrain the request.
     * @param properties an array of the properties to fetch.
     */
    public QueryImpl(Filter filter, AttributeType[] properties) {
        this(filter);
        this.properties = properties;
    }

    /**
     * Constructor that sets all fields.
     *
     * @param typeName the name of the featureType to retrieve.
     * @param filter the OGC filter to constrain the request.
     * @param maxFeatures the maximum number of features to be returned.
     * @param properties an array of the properties to fetch.
     * @param handle the name to associate with the query.
     */
    public QueryImpl(String typeName, Filter filter, int maxFeatures,
        AttributeType[] properties, String handle) {
        this(filter, properties);
        this.maxFeatures = maxFeatures;
        this.handle = handle;
    }

    /**
     * The property names is used to specify the attributes that should be
     * selected for the return feature collection.  If the property array is
     * null, then the datasource should return all available properties, its
     * full schema.  If an array of  specified then the full schema should be
     * used (all property names). The property names can be determined with a
     * getSchema call from the DataSource interface.
     * 
     * <p>
     * This replaces our funky setSchema method of retrieving select
     * properties.  I think it makes it easier to understand how to get
     * certain properties out of the datasource, instead of having users get
     * the  schema and then compose a new schema using the attributes that
     * they want.  The old way was also giving me problems because I couldn't
     * have multiple object reuse the same datasource object, since some other
     * object could come along and change its schema, and would then return
     * the wrong properties.
     * </p>
     * 
     * <p></p>
     *
     * @return the property names to be used in the returned FeatureCollection.
     *
     * @task REVISIT: This could also return an AttributeType[].  This would
     *       require our  setPropertyNames to either take AttributeTypes, or
     *       property names with schemas.  It depends if we want to raise the
     *       property not found error in the datasource, once the request is
     *       made, or in the  construction of the query. <p>
     * @task REVISIT: Another thing to think about, how does this fit in with
     *       creating  features that want to leave fields blank?  Andrea
     *       brought this up awhile ago, and we never came up with a good way
     *       to do it, flags indicating whether a datasource should load the
     *       features or not? I think this object is the place to do it, but
     *       I'm not sure how.
     */
    public AttributeType[] getProperties() {
        return properties;
    }

    /**
     * Sets the properties to retrieve from the db.  If the boolean to load all
     * properties is set to true then the AttributeTypes that are not in the
     * database's schema will just be filled with null values.
     *
     * @param properties The attribute Types to load from the datasouce.
     */
    public void setProperties(AttributeType[] properties) {
        this.properties = properties;
    }

    /**
     * Sets the proper attributeTypes constructed from a schema and a  list of
     * propertyNames.
     *
     * @param schema The schema to validate the propertyNames against.
     * @param propertyNames the names of the properties to check against the
     *        schema. If null or of size 0 then all attributes from the schema
     *        should be fetched.
     *
     * @throws SchemaException if any of the propertyNames do not have an
     *         attributeType of the same name in the schema.
     *
     * @task REVISIT: perhaps a boolean to not throw exceptions?  Just return
     *       all propertynames that match the schema, leave them out if they
     *       don't match.
     */
    public void setProperties(FeatureType schema, List propertyNames)
        throws SchemaException {
        this.properties = getValidProperties(schema, propertyNames);
    }

    /**
     * Convenience method to determine if the query should use the full schema
     * (all properties) of the data source for the features returned.  This
     * method is equivalent to if (query.getProperties() == null), but allows
     * for more clarity on the part of datasource implementors, so they do not
     * need to examine and use null values.  All Query implementations should
     * return true for this function if getProperties returns null.
     *
     * @return if all datasource attributes should be included in the  schema
     *         of the returned FeatureCollection.
     */
    public boolean retrieveAllProperties() {
        return properties == null;
    }

    /**
     * Convenience method to get valid properties given a schema and a list of
     * propertyNames.  It checks the property names against the schema,
     * throwing a schema exception if a requested propertyName is not in the
     * schema.  This method should be used by users who only have a list of
     * the property names and not the attributeTypes.
     *
     * @param schema The schema to validate the propertyNames against.
     * @param propertyNames the names of the properties to check against the
     *        schema. If null or of size 0 then all attributes from the schema
     *        should be fetched.
     *
     * @return an array of properties of the propertyNames with types from the
     *         passed in schema.
     *
     * @throws SchemaException if any of the propertyNames do not have an
     *         attributeType of the same name in the schema.
     *
     * @task REVISIT: perhaps a boolean to not throw exceptions?  Just return
     *       all propertynames that match the schema, leave them out if they
     *       don't match.
     * @task REVISIT: private?  package?  Somewhere in feature package? Someone
     *       might want to use it.
     */
    public static AttributeType[] getValidProperties(FeatureType schema,
        List propertyNames) throws SchemaException {
        if ((propertyNames == null) || (propertyNames.size() == 0)) {
            return schema.getAttributeTypes();
        } else {
            AttributeType[] properties = new AttributeType[propertyNames.size()];
            int i = 0;

            for (Iterator iter = propertyNames.iterator(); iter.hasNext();
                    i++) {
                String curPropName = iter.next().toString();

                //process typeName prefixes here?  Like road.nlanes, road/nlanes,
                //or rns:nlanes?  Change them all to just nlanes?
                properties[i] = schema.getAttributeType(curPropName);

                if (properties[i] == null) {
                    //report the available props in the error report.
                    AttributeType[] available = schema.getAttributeTypes();
                    StringBuffer props = new StringBuffer();

                    for (int j = 0; j < available.length; j++) {
                        props.append(available[j].getName());

                        if (j < (available.length - 1)) {
                            props.append(", ");
                        }
                    }

                    throw new SchemaException("property name: " + curPropName +
                        " is " + "not a part of featureType" +
                        ", the available properties" + " are: " + props);
                }
            }

            return properties;
        }
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
     * to be queried.
     * 
     * <p>
     * All our datasources now assume one feature type per datasource, but it
     * doesn't seem like we should constrain ourselves to that.  This field
     * will allow us to create a postgis datasource that can make use of the
     * whole db, specifying with each request which type to get.
     * </p>
     * 
     * <p>
     * Geotools currently limits datasources to a one to one relationship
     * between featureType and datasource, so datasources can ignore this
     * field of the query for now.
     * </p>
     *
     * @return the name of the feature type to be returned with this query.
     *
     * @task REVISIT: the transaction methods do not work with different
     *       typenames, so that will have to be resolved before typenames are
     *       used. Perhaps a MultiType interface?  We will also need to
     *       rethink the  getSchema, and probably schemas in general, as they
     *       could be more than one FeatureType in datasources that support
     *       multiple types.  If users wish to use more than one type in a
     *       datasource whose backend naturally supports multiple types (like
     *       postgis) they should just construct a datasource for each type,
     *       generally on different connections so as to avoid
     *       getFeature/multi-transaction confusion.
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
     * <p>
     * This will not be used for awhile, but at some future point geotools
     * should support feature versioning.  Obviously none do now, nor are any
     * close to supporting it, so perhaps we should just wait and see.  And of
     * course we'd need the corresponding supportsFeatureVersioning in the
     * datasource metadata object.
     * </p>
     *
     * @return the version of the feature to return.
     *
     * @throws UnsupportedOperationException if a user attempts to use this
     *         method - no versioning supported yet.
     */
    public String getVersion() {
        throw new UnsupportedOperationException("No feature versioning yet");
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
            return returnString + " ALL ]";
        } else {
            for (int i = 0; i < properties.length; i++) {
                returnString.append(properties[i].toString());

                if (i < (properties.length - 1)) {
                    returnString.append(", ");
                }
            }

            returnString.append("]");

            return returnString.toString();
        }
    }
}
