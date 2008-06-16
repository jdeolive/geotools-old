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
 * 
 * @author Simone Giannecchini, GeoSolutions
 *
 */
public interface CoverageDataStore {

	public enum AccessType{
		READ,   	//read access to a coverage
		READ_WRITE; //read-write access to a coverage (it might unsupported!)
	}

	/**
	 * We need to defined how we want to represent the coverage description
	 * 
	 * @param coverageIndex
	 * @return
	 */
	Object getCoverageOfferingBrief(Name coverageName,final ProgressListener listener);

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

	/**
	 * Metadata about this access point
	 * 
	 * @todo TODO think about the equivalence with StreamMetadata as we definedd  them once
	 * 
	 * @return
	 */
	ServiceInfo getInfo(final ProgressListener listener);

	/**
	 * @todo TODO Should we use typename??
	 * 
	 * @param name
	 * @return
	 */
	CoverageSource createAccessor(Name name, Hints hints,AccessType accessType);

	/**
	 * The format of this dataset
	 * 
	 * @return
	 */
	CoverageService getCoverageService();

	/**
	 * This will free any cached info object or header information.
	 * 
	 * <p>
	 * Often a GridAccess will keep a file channel open, this will clean that
	 * sort of thing up.
	 */
	void dispose();
}
