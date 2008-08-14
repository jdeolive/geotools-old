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
import java.util.List;

import org.geotools.data.ServiceInfo;
import org.geotools.factory.Hints;
import org.opengis.feature.type.Name;
import org.opengis.util.ProgressListener;

/**
 * Represents a Physical store of coverage data (that we have a connected to).
 * <p>
 * Please note that this service may be remote (or otherwise slow). You are doing
 * IO here and should treat this class with respect - please do not access
 * these methods from a display thread.
 * </p>
 * @author Simone Giannecchini, GeoSolutions
 */
public interface CoverageAccess {
	/**
	 * Level of access supported.
	 */
	public enum AccessType{
		/** Read-only access to coverage data */
		READ_ONLY,
		/** Read-write access to coverage data */
		READ_WRITE;
	}
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
	 */
	Object getCoverageOfferingBrief(Name coverageName,final ProgressListener listener);
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
	 * @todo TODO Should we use typename??
	 * 
	 * @param Name name of coverage to access
	 * @return
	 */
	CoverageSource access(Name name, Hints hints, AccessType accessType );

	/**
	 * The factory/format that was used to connect to this CoverageAccess.
	 * 
	 * @return CoverageAccessFactory used to connect
	 */
	CoverageAccessFactory getFactory();

	/**
	 * This will free any cached info object or header information.
	 * <p>
	 * Often a GridAccess will keep a file channel open, this will clean that
	 * sort of thing up.
	 */
	void dispose();
}