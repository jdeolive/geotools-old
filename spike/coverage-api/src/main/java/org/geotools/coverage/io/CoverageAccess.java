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
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.data.Parameter;
import org.geotools.data.ServiceInfo;
import org.geotools.factory.Hints;
import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.Extent;
import org.opengis.util.ProgressListener;

/**
 * Represents a Physical storage of coverage data (that we have a connection to).
 * <p>
 * Please note that this service may be remote (or otherwise slow). You are doing 
 * IO here and should treat this class with respect - please do not access these 
 * methods from a display thread.
 * </p>
 * 
 * @author Simone Giannecchini, GeoSolutions
 * @author Jody Garnett
 */
public interface CoverageAccess {
    /**
     * Level of access supported.
     */
    public enum AccessType {
        /** Read-only access to coverage data */
        READ_ONLY,
        /** Read-write access to coverage data */
        READ_WRITE;
    }

    /**
     * Description of the CoverageAccess we are connected to here.
     * 
     * @todo TODO think about the equivalence with StreamMetadata once we define
     *       them
     * 
     * @return Description of the CoverageAccess we are connected to here.
     */
    public ServiceInfo getInfo(final ProgressListener listener);

    /**
     * We need to defined how we want to represent the coverage description
     * 
     * @param coverageIndex
     * @return
     * 
     * @todo consider geoapi {@link Extent} which can in turn wrap the Envelope
     *       we are providing here In order to do so we need to resolve the
     *       ambiguity of the 3D inseparable CRSs.
     */
    public Envelope getExtent(Name coverageName, final ProgressListener listener);

    /**
     * The number of Coverages made available.
     * 
     * @param listener
     * @return getNames( listener ).size()
     */
    public int getNumCoverages(final ProgressListener listener);

    /**
     * Names of the available Resources.
     * <p>
     * For additional information please see getInfo( Name ) and getSchema( Name ).
     * </p>
     * 
     * @return Names of the available contents.
     * @throws IOException
     */
    public List<Name> getNames(final ProgressListener listener);

    //
    // it is tempting to make Name support children here (the ISO GenericName
    // does)
    // which would allow us to present a tree of data, rather than a flat list.
    // (not sure if this is a good idea - just getting it out there)
    //

    /**
     * Get access to a {@link CoverageSource} specified by the name parameter.
     */
    public CoverageSource access(Name name, Map<String, Serializable> params,
            AccessType accessType, Hints hints, ProgressListener listener)
            throws IOException;

    /**
     * Create a {@link CoverageStore} with the specified name. Implementing
     * subclasses may throw an {@link UnsupportedOperationException} in case the
     * related Driver won't allow {@link CoverageStore} creation.
     */
    public CoverageStore create(Name name, Map<String, Serializable> params,
            Hints hints, ProgressListener listener) throws IOException;

    /**
     * Asks this {@link CoverageAccess} to entirely remove a certain Coverage
     * from the available {@link CoverageSource}s.
     * 
     * <p>
     * Many file based formats won't allow to perform such operation, but db
     * based source should be quite happy with it.
     * 
     * @param name
     * @param params
     * @param accessType
     * @param hints
     * 
     * @return {@code true} in case of success.
     * @throws IOException
     */
    public boolean delete(Name name, Map<String, Serializable> params,
            Hints hints) throws IOException;

    /**
     * Describes the required (and optional) parameters that can be used to open
     * a {@link CoverageSource}.
     * <p>
     * 
     * @return Param a {@link Map} describing the {@link Map} for
     *         {@link #connect(Map)}.
     */
    public Map<String, Parameter<?>> getAccessParameterInfo(
            CoverageAccess.AccessType accessType);

    /**
     * Retrieves the parameters used to connect to this live instance of
     * {@link CoverageAccess}.
     * 
     * 
     * @return the parameters used to connect to this live instance of
     *         {@link CoverageAccess}.
     */
    public Map<String, Serializable> getConnectParameters();

    /**
     * Returns the {@link Driver} which has been used to connect to this
     * CoverageAccess.
     * 
     * @return {@link Driver} used to connect
     */
    public Driver getDriver();

    /**
     * This will free any cached info object or header information.
     * <p>
     * Often a {@link CoverageAccess} will keep a file channel open, this will
     * clean that sort of thing up.
     * 
     * <p>
     * Once a {@link CoverageAccess} has been disposed it can be seen as being
     * in unspecified state, hence calling a method on it may have unpredictable
     * results.
     * 
     */
    public void dispose();

    /**
     * Retrieves the {@link Set} of supported {@link AccessType}s for this
     * {@link CoverageAccess} instance.
     * 
     * @return the {@link Set} of supported {@link AccessType}s for this
     *         {@link CoverageAccess} instance.
     */
    public Set<CoverageAccess.AccessType> getSupportedAccessTypes();

    /**
     * Tells me whether or not this {@link CoverageAccess} supports creation of
     * a new coverage storage.
     * 
     * @return <code>true</code> when removal of of a new coverage storage is
     *         supported, <code>false</code> otherwise.
     */
    public boolean isCreateSupported();

    /**
     * Tells me whether or not this {@link CoverageAccess} supports removal of
     * an existing coverage storage.
     * 
     * @return <code>true</code> when removal of an existing coverage storage
     *         is supported, <code>false</code> otherwise.
     */
    public boolean isDeleteSupported();

    /**
     * Test to see if this coverage access is suitable for creating a
     * {@link CoverageStore} referred by Name, with the specified set of
     * parameters.
     */
    public boolean canCreate(Name name, Map<String, Serializable> params,
            Hints hints, ProgressListener listener) throws IOException;

    /**
     * Test to see if this coverage access is suitable for deleting a
     * {@link CoverageSource} referred by Name, with the specified set of
     * parameters.
     */
    public boolean canDelete(Name name, Map<String, Serializable> params,
            Hints hints) throws IOException;
}
