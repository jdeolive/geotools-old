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
package org.geotools.coverage.io;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.geotools.data.Parameter;
import org.geotools.data.ServiceInfo;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.feature.type.Name;
import org.opengis.metadata.extent.Extent;
import org.opengis.util.ProgressListener;

/**
 * Represents a Physical storage of coverage data (that we have a connection to).
 * <p>
 * Please note that this service may be remote (or otherwise slow). You are doing
 * IO here and should treat this class with respect - please do not access
 * these methods from a display thread.
 * </p>
 * @author Simone Giannecchini, GeoSolutions
 * @author Jody Garnett
 */
public interface CoverageAccess {
	/**
	 * Level of access supported.
	 */
	public enum AccessType{
		/** Read-only access to coverage data */
		READ_ONLY,
		/** Read-write access to coverage data */
		UPDATE;
	}
	
	/**
	 * Retrieves a {@link List} containing the allowed access types for this {@link CoverageAccess}.
	 * 
	 * @return a {@link List} containing the allowed access types for this {@link CoverageAccess}.
	 */
	public List<CoverageAccess.AccessType> allowedAccessTypes();
	
	/**
	 * Description of the CoverageAccess we are connected to here.
	 * 
	 * @todo TODO think about the equivalence with StreamMetadata as we define them once
	 * 
	 * @return Description of the CoverageAccess we are connected to here.
	 */
	ServiceInfo getInfo(final ProgressListener listener);
	
	/**
	 * We need to defined how we want to represent the coverage description
	 * 
	 * @param coverageIndex
	 * @return
	 * 
	 * @todo consider geoapi {@link Extent}
	 */
	GeneralEnvelope getExtent(Name coverageName,final ProgressListener listener);
	// It is nicer to make the getInfo( Name, listener ) method return a subclass
	// of GeoResourceInfo that has additional information about what is going on?
	
	/**
	 * The number of Coverages made available.
	 * 
	 * @param listener
	 * @return getNames( listener ).size()
	 */
	int getNumCoverages(final ProgressListener listener);

	/**
	 * Names of the available Resources.
	 * <p>
	 * For additional information please see getInfo( Name ) and getSchema( Name ).
	 * </p>
	 * 
	 * @return Names of the available contents.
	 * @throws IOException
	 */
	List<Name> getNames(final ProgressListener listener);
	//
	// it is tempting to make Name support children here (the ISO GenericName does)
	// which would allow us to present a tree of data, rather than a flat list.
	// (not sure if this is a good idea - just getting it out there) 
	//
	
	/**
	 * 
	 * @param name
	 * @param params
	 * @param accessType
	 * @param hints
	 * @param listener
	 * @param canCreate tells the {@link CoverageSource} whether or not the request should fail for a non-existent {@link CoverageAccess}.
	 * @return
	 * 
	 * @todo TODO Should we use typename??
	 */
	CoverageSource access(Name name,Map<String, Parameter<?>> params,AccessType accessType,Hints hints, ProgressListener listener, boolean canCreate )throws IOException;
	
	
	/**
	 * Asks this {@link CoverageAccess} to entirely remove a certain Coverage from the available {@link CoverageSource}s.
	 * 
	 * <p>
	 * Many file based formats won't allow to perform such operation, but db based source should be quite happy with it.
	 * 
	 * @param name
	 * @param params
	 * @param accessType
	 * @param hints
	 * @throws IOException
	 */
	void remove(Name name,Map<String, Parameter<?>> params,Hints hints)throws IOException;

	/**
	 * Describes the required (and optional) parameters that
	 * can be used to open a {@link CoverageSource}.
	 * <p>
	 * @return Param a {@link Map} describing the {@link Map} for {@link #connect(Map)}.
	 */
	Map<String, Parameter<?>> getAccessParameterInfo(CoverageAccess.AccessType accessType);	
	
	/**

	 */
	Map<String, Parameter<?>> getConnectParameter();

	/**
	 * The factory/format that was used to connect to this CoverageAccess.
	 * 
	 * @return CoverageAccessFactory used to connect
	 */
	CoverageAccessFactory getFactory();

	/**
	 * This will free any cached info object or header information.
	 * <p>
	 * Often a {@link CoverageAccess} will keep a file channel open, this will clean that
	 * sort of thing up.
	 */
	void dispose();
}