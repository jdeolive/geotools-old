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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map;


/**
 * Constructs a live DataStore from a set of parameters.
 * 
 * <p>
 * An instance of this interface should exist for all data stores which want to
 * take advantage of the dynamic plugin system. In addition to implementing
 * this interface datastores should have a services file:
 * </p>
 * 
 * <p>
 * <code>META-INF/services/org.geotools.data.DataStoreFactorySpi</code>
 * </p>
 * 
 * <p>
 * The file should contain a single line which gives the full name of the
 * implementing class.
 * </p>
 * 
 * <p>
 * example:<br/><code>e.g.
 * org.geotools.data.mytype.MyTypeDataSourceFacotry</code>
 * </p>
 * 
 * <p>
 * The factories are never called directly by users, instead the
 * DataStoreFinder class is used.The DataStoreFinder may implements the
 * Catalog interface
 * </p>
 * 
 * <p>
 * The following example shows how a user might connect to a PostGIS database:
 * </p>
 * 
 * <p>
 * <pre><code>
 * HashMap params = new HashMap();
 * params.put("namespace", "leeds"); 
 * params.put("dbtype", "postgis");
 * params.put("host","feathers.leeds.ac.uk");
 * params.put("port", "5432");
 * params.put("database","postgis_test");
 * params.put("user","postgis_ro");
 * params.put("passwd","postgis_ro");
 * 
 * DataStoreFinder catalog = DataStoreFinder();
 * catalog.addDataStore("leeds", params);
 * 
 * DataStore postgis = catalog.getDataStore( "leeds" );
 * FeatureSource = postgis.getFeatureSource( "table" );
 * </code></pre>
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 */
public interface DataStoreFactorySpi extends org.geotools.factory.Factory {
    /**
     * Construct a live data source using the params specifed.
     *
     * @param params The full set of information needed to construct a live
     *        data store. Typical key values for the map include: url -
     *        location of a resource, used by file reading datasources. dbtype
     *        - the type of the database to connect to, e.g. postgis, mysql
     *
     * @return The created DataSource, this may be null if the required
     *         resource was not found or if insufficent parameters were given.
     *         Note that canProcess() should have returned false if the
     *         problem is to do with insuficent parameters.
     *
     * @throws IOException if there were any problems creating or connecting
     *         the datasource.
     */
    DataStore createDataStore(Map params) throws IOException;

    DataStore createNewDataStore(Map params) throws IOException;
    /**
     * Describe the nature of the datasource constructed by this factory.
     *
     * @return A human readable description that is suitable for inclusion in a
     *         list of available datasources.
     */
    String getDescription();

    /**
     * MetaData about the required Parameters.
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
     * @return
     */
    Param[] getParametersInfo();

    /**
     * Test to see if this factory is suitable for processing the data pointed
     * to by the param tags. If this datasource requires a number of
     * parameters then this mehtod should check that they are all present and
     * that they are all valid. If the datasource is a file reading data
     * source then the extentions or mime types of any files specified should
     * be checked. For example, a Shapefile datasource should check that the
     * url param ends with shp, such tests should be case insensative.
     *
     * @param params The full set of information needed to construct a live
     *        data source.
     *
     * @return booean true if and only if this factory can process the resource
     *         indicated by the param set and all the required params are
     *         pressent.
     */
    boolean canProcess(java.util.Map params);

    /**
     * Data class used to capture Parameter requirements.
     * 
     * <p>
     * Subclasses may provide specific setAsText()/getAsText() requirements
     * </p>
     */
    class Param {
        /** True if Param is required */
        final public boolean required;
        
        /** Key used in Parameter map */
        final public String key;

        /** Type of information required */
        final public Class type;

        /** Short description (less then 40 characters) */
        final public String description;

        /**
         * Provides support for text representations
         * 
         * <p>
         * The parameter type of String is assumed.
         * </p>
         *
         * @param key Key used to file this Param in the Parameter Map for
         *        createDataStore
         */
        public Param(String key) {
            this(key, String.class, null);
        }

        /**
         * Provides support for text representations.
         * 
         * <p>
         * You may specify a <code>type</code> for this Param.
         * </p>
         *
         * @param key Key used to file this Param in the Parameter Map for
         *        createDataStore
         * @param type Class type intended for this Param
         */
        public Param(String key, Class type) {
            this(key, type, null);
        }

        /**
         * Provides support for text representations
         *
         * @param key Key used to file this Param in the Parameter Map for
         *        createDataStore
         * @param type Class type intended for this Param
         * @param description User description of Param (40 chars or less)
         */
        public Param(String key, Class type, String description) {
            this( key, type, description, true );            
        }
        /**
         * Provides support for text representations
         *
         * @param key Key used to file this Param in the Parameter Map for
         *        createDataStore
         * @param type Class type intended for this Param
         * @param description User description of Param (40 chars or less)
         * @param required <code>true</code> is param is required
         */
        public Param(String key, Class type, String description, boolean required) {
            this.key = key;
            this.type = type;
            this.description = description;
            this.required = required;
        }
        public String getAsText(Object value) {
            return value.toString();
        }

        /**
         * Provides support for text representations
         * 
         * <p>
         * Provides basic support for common types using reflection.
         * </p>
         * 
         * <p>
         * If needed you may extend this class to handle your own custome
         * types.
         * </p>
         *
         * @param text Text representation
         *
         * @return Object converted from text representation, or
         *         <code>null</code> if unsuccessful
         */
        public Object setAsText(String text) {
            Constructor constructor;

            try {
                constructor = type.getConstructor(new Class[] { String.class });

                return constructor.newInstance(new Object[] { text, });
            } catch (Exception e) {
                // fail to construct
                return null;
            }
        }
    }
}
