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

import java.util.List;
import java.util.ArrayList;
import org.geotools.filter.Filter;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;

/**
 * The query object is used by the getFeature method of the DataSource
 * interface, to encapsulate a request.  It defines which feature type  to
 * query, what properties to retrieve and what constraints (spatial  and
 * non-spatial) to apply to those properties.  It is designed to  closesly
 * match a WFS Query element of a GetFeature request.   The only difference is
 * the addition of the maxFeatures element, which  allows more control over
 * the features selected.  It allows a full  GetFeature request to properly
 * control how many features it gets from each query, instead of requesting
 * and discarding when the max is reached.
 *
 * @author Chris Holmes
 */
public class QueryImpl implements Query {
    

    private AttributeType[] properties;

    private int maxFeatures = 100000000;

    private Filter filter;

    private String typeName;

    private String handle;    

    public QueryImpl() {
    }

    public QueryImpl(String typeName, Filter filter){
	this.typeName = typeName;
	this.filter = filter;
    }

    public QueryImpl(String typeName, Filter filter, int maxFeatures){
	this(typeName, filter);
	this.maxFeatures = maxFeatures;
	this.properties = properties;
    }

    /**
     * The property names is used to specify the attributes that should be
     * selected for the return feature collection.  If no property names are
     * specified then the full schema should be used (all property names). The
     * property names can be determined with a getSchema call from the
     * DataSource interface.
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
     * Sets the properties to retrieve from the db.  If the boolean to load
     * all properties is set to true then the AttributeTypes that are not
     * in the database's schema will just be filled with null values.
     *
     * @param properties The attribute Types to load from the datasouce.
     */
    public void setProperties(AttributeType[] properties) {
	this.properties = properties;
    }

    /**
     * Convenience method to get valid properties given a schema and a list
     * of propertyNames.  It checks the property
     * names against the schema, throwing a schema exception if a requested
     * propertyName is not in the schema.  
     *
     * @param schema The schema to validate the propertyNames against.
     * @param propertyNames the names of the properties to check against the schema.
     * @return an array of properties of the propertyNames with types from the 
     * passed in schema.
     * @task REVISIT: perhaps a boolean to not throw exceptions?  Just return all
     * propertynames that match the schema, leave them out if they don't match.
     */
    public static AttributeType[] getProperties(FeatureType schema, 
						String[] propertyNames) 
	throws SchemaException {
	AttributeType[] properties = new AttributeType[propertyNames.length];
	for(int i = 0; i < propertyNames.length; i++) {
	    String curPropName = propertyNames[i];
	    //process typeName prefixes here?  Like road.nlanes, road/nlanes,
	    //or rns:nlanes?  Change them all to just nlanes?
	    properties[i] = 
		schema.getAttributeType(curPropName);
 	    if (properties[i] == null) {
		//report the available props in the error report.
		AttributeType[] available = schema.getAttributeTypes();
		StringBuffer props = new StringBuffer();
		for (int j = 0; j < available.length; j++){
		    props.append(available[j].getName());
		    if (j < available.length - 1) {
			props.append(", ");
		    }
		}
		
		throw new SchemaException("property name: " + 
				       curPropName + " is "
				       +"not a part of featureType"
				       + ", the available properties"
				       + " are: " + props);
	    }
	}
	return properties;
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
    public int getMaxFeatures(){
	return this.maxFeatures;
    }

    public void setMaxFeatures(int maxFeatures){
	this.maxFeatures = maxFeatures;
    }

    /**
     * The Filter can be used to define constraints on a query.  If no Filter
     * is present then the query is unconstrained and all feature instances
     * should be retrieved.
     *
     * @return The filter that defines constraints on the query.
     */
    public Filter getFilter(){
	return this.filter;
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
     * @return the name of the feature type to be returned with this query.
     */
    public String getTypeName(){
	return this.typeName;
    }

    /**
     * The handle attribute is included to allow a client to associate  a
     * mnemonic name to the Query request. The purpose of the handle attribute
     * is to provide an error handling mechanism for locating  a statement
     * that might fail.
     *
     * @return the mnemonic name of the query request.
     */
    public String getHandle(){
	return this.handle;
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
     */
    public String getVersion() {
	throw new UnsupportedOperationException("No feature versioning yet");
    }

}
