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

/**
 * Provides a Catalog of available FeatureTypes.
 * 
 * <p>
 * This class is currently serving as a scratching post for gel our ideas about
 * Catalog requirements.
 * </p>
 * 
 * <p>
 * From Chris Holmes email:
 * </p>
 * <pre>
 * <code>
 * As for catalog, I looked a bit through ogc catalog specs over the weekend.  
 * I don't know that I like the implementation one too much, I may have to 
 * look at it some more.  But we may borrow some terms from the abstract one.  
 * I need to read it more closely to see how much we can do with it.  But the 
 * concept of a Catalog Entry might work for our catalog.  I don't think a 
 * metadata url will be sufficient, but I would like to have a somewhat 
 * abbreviated metadata representation, perhaps a bit more than the ogc 
 * service elements, but less than a full fgdc record, as there are a lot of 
 * elements in that.  Perhaps eventually, but I think we should start out
 *  with a Catalog Entry (2.3.4 in the abstract catalog spec).  It 'describes 
 * or summarizes the contents of a set of geospatial data, and is designed to 
 * be queried.  A Catalog Entry is usually a subset of the complete metadata 
 * for the describe geospatial dataset.'  Basically it's the metadata needed 
 * for discovery, the where, what who, when how and why.  I have more 
 * thoughts about this, but I think we should hold off until we get the data
 *  api together.  If gabriel needs it soon he could go ahead and code 
 * something up that works, and we could figure out how to adapt it to OGC 
 * and our Feature stuff later.
 * </code>
 * </pre>
 */
public interface Catalog {
    /**
     * Registers all FeatureTypes provided by dataStore with this catalog
     * service.
     * 
     * <p>
     * Catalog can be seen as aggregating multiple DataStores and providing
     * higher level functionality. Such as derived metadata like lat long
     * bounding box information.
     * </p>
     * 
     * <p>
     * We may need to register individual FeatureTypes instead, to provide
     * FeatureTypes with there own namespace?
     * </p>
     *
     * @param namespace Catalog namespace
     * @param dataStore Datastore providing FeatureTypes
     */
    void registerDataStore(String namespace, DataStore dataStore);

    /**
     * Access to a DataStore for a specific <code>namespace</code>.
     *
     * @param namespace namespace for requested DataStore
     *
     * @return DataStore for the provided <code>namespace</code>
     */
    DataStore getDataStore(String namespace);
}
