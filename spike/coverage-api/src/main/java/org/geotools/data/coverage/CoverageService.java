package org.geotools.data.coverage;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.geotools.data.ServiceInfo;
import org.opengis.util.InternationalString;
import org.opengis.util.ProgressListener;

public interface CoverageService {
	
	ServiceInfo getInfo(ProgressListener monitor) throws IOException;

    /**
     * Describe the nature of the datasource constructed by this factory.
     *
     * <p>
     * A non localized description of this data store type.
     * </p>
     *
     * @return A human readable description that is suitable for inclusion in a
     *         list of available datasources.
     */
    InternationalString getDescription();

    /**
     * Name suitable for display to end user.
     *
     * <p>
     * A non localized display name for this data store type.
     * </p>
     *
     * @return A short name suitable for display in a user interface.
     */
    InternationalString getName();

    /**
     * Test to see if this datastore is available, if it has all the
     * appropriate libraries to construct a datastore.  Most datastores should
     * return true, because geotools will distribute the appropriate
     * libraries.  Though it's not a bad idea for DataStoreFactories to check
     * to make sure that the  libraries are there.  OracleDataStoreFactory is
     * an example of one that may generally return false, since geotools can
     * not distribute the oracle jars, they must be added by the client.  One
     * may ask how this is different than canProcess, and basically available
     * is used by the DataStoreFinder getAvailableDataStore method, so that
     * DataStores that can not even be used do not show up as options in gui
     * applications.
     *
     * @return <tt>true</tt> if and only if this factory has all the
     *         appropriate jars on the classpath to create DataStores.
     */
    boolean isAvailable();

    /**
     * MetaData about the required Parameters (for createDataStore).
     *
     * <p>
     * Interpretation of FeatureDescriptor values:
     * </p>
     *
     * <ul>
     * <li>
     * getDisplayName(): Gets the localized display name of this feature.
     * </li>
     * <li>
     * getName(): Gets the programmatic name of this feature (used as the key
     * in params)
     * </li>
     * <li>
     * getShortDescription(): Gets the short description of this feature.
     * </li>
     * </ul>
     *
     * <p>
     * This should be the same as:
     * </p>
     * <pre><code>
     * Object params = factory.getParameters();
     * BeanInfo info = getBeanInfo( params );
     *
     * return info.getPropertyDescriptors();
     * <code></pre>
     *
     * @return Param array describing the Map for createDataStore
     */
    Param[] getDefaultConnectionParameters();
    
    Param[] getDefaultAccessParameters(CoverageDataStore.AccessType accessType);

	public CoverageDataStore connect(Map<String, Serializable> params) throws IOException;


	/**
	 * Test to see if this factory is suitable for processing the data pointed
	 * to by the params map.
	 *
	 * <p>
	 * If this datasource requires a number of parameters then this mehtod
	 * should check that they are all present and that they are all valid. If
	 * the datasource is a file reading data source then the extentions or
	 * mime types of any files specified should be checked. For example, a
	 * Shapefile datasource should check that the url param ends with shp,
	 * such tests should be case insensative.
	 * </p>
	 *
	 * @param params The full set of information needed to construct a live
	 *        data source.
	 *
	 * @return boolean true if and only if this factory can process the resource
	 *         indicated by the param set and all the required params are
	 *         pressent.
	 */
	boolean canConnect(java.util.Map<String, Serializable> params);
}
