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
package org.geotools.data.coverage;

import java.io.IOException;
import java.util.Map;

import org.geotools.data.Parameter;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.factory.Factory;
import org.geotools.factory.Hints;
import org.opengis.feature.type.Name;
import org.opengis.util.InternationalString;
import org.opengis.util.ProgressListener;

/**
 * Constructs a live CoverageAccess from a set of connection parameters.
 * <p>
 * Classes implementing this interface basically act as factory for creating
 * connections to coverage sources like files, WCS services, WMS services, etc.
 * 
 * <p>
 * Purpose of this class is to provide basic information about a certain
 * coverage service/format as well as about the parameters needed in order to connect to a
 * source.
 * 
 * <p>
 * Notice that as part as the "factory" interface this class makes available an
 * {@link #isAvailable()} method which should check if all the needed
 * dependencies which can be jars as well as native libs or configuration files.

 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Jody Garnett
 * @since 2.5
 * 
 * @todo TODO {@link Name} vs {@link InternationalString}
 * @todo TODO {@link Param} array vs {@link Map}
 * 
 */
public interface CoverageAccessFactory extends Factory {

	 /**
     * Unique name (non human readable) that can be used to
     * refer to this implementation.
     * <p>
     * While the Title and Description will change depending
     * on the users local this name will be consistent.
     * </p>
     * @return name of this coverage access
     */
	String getName();
	
	/**
	 * Human readable title for this CoverageService.
	 * 
	 * @return human readable title for presentation in user interfaces
	 */
	InternationalString getTitle();

	/**
	 * Describe the nature of the {@link CoverageAccess} constructed by this
	 * factory.
	 * <p>
	 * A description of this data store type.
	 * </p>
	 * 
	 * @return A human readable description that is suitable for inclusion in a
	 *         list of available datasources.
	 */
	InternationalString getDescription();

	/**
	 * Test to see if this {@link CoverageAccessFactory} is available, if it has all
	 * the appropriate dependencies (jars or libraries).
	 * <p>
	 * One may ask how this is different than canProcess, and basically
	 * available is used by the DataStoreFinder getAvailableDataStore method, so
	 * that DataStores that can not even be used do not show up as options in
	 * gui applications.
	 * 
	 * @return <tt>true</tt> if and only if this factory has all the appropriate
	 *         dependencies on the classpath to create DataStores.
	 */
	boolean isAvailable();

	/**
	 * Describes the required (and optional) parameters that
	 * can be used to open a CoverageAccess.
	 * <p>
	 * @return Param a {@link Map} describing the {@link Map} for {@link #connect(Map)}.
	 */
	Map<String, Parameter<?>> getConnectParameterInfo();

	/**
	 * Open up a connection to a {@link CoverageAccess}.
	 * 
	 * @param params
	 *            required {@link Param}s to connect to a certain
	 *            {@link CoverageStore}
	 * @return a {@link CoverageAccess} which
	 * @throws IOException in case something wrong happens during the connection.
	 */
	public CoverageAccess connect(Map<String, Parameter<?>> params,Hints hints,final ProgressListener listener)
			throws IOException;

	/**
	 * Test to see if this factory is suitable for processing the data pointed
	 * to by the params map.
	 * 
	 * <p>
	 * If this datasource requires a number of parameters then this mehtod
	 * should check that they are all present and that they are all valid. If
	 * the datasource is a file reading data source then the extentions or mime
	 * types of any files specified should be checked. For example, a Shapefile
	 * datasource should check that the url param ends with shp, such tests
	 * should be case insensative.
	 * </p>
	 * 
	 * @param params
	 *            The full set of information needed to construct a live data
	 *            source.
	 * 
	 * @return boolean true if and only if this factory can process the resource
	 *         indicated by the param set and all the required params are
	 *         pressent.
	 */
	boolean canConnect(Map<String, Parameter<?>> params);
}
