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

import org.geotools.data.ResourceInfo;
import org.geotools.factory.Hints;
import org.opengis.feature.type.Name;
import org.opengis.util.ProgressListener;

/**
 * Allows access to a Coverage.
 */
public interface CoverageSource {


    /**
     * Name of the Coverage (ie data product) provided by this CoverageSource.
     * 
     * @since 2.5
     * @return Name of the Coverage (ie data product) provided.
     */
    Name getName(final ProgressListener listener);
    
    /**
     * Information describing the contents of this resource.
     * <p>
     * Please note that for FeatureContent:
     * <ul>
     * <li>name - unqiue with in the context of a Service
     * <li>schema - used to identify the type of resource; usually the format or data product being represented
     * <ul>
     */
    ResourceInfo getInfo(final ProgressListener listener);
    
    //
    // Not quite sure what information we are trying to communicate here?
    // Could it be communicated with a subclass of ResourceInfo?
    // brief may be a reference to catalog record formats?
    //
    public Object getCoverageOffering(final ProgressListener listener, final boolean brief) throws IOException;

    /**
     * Access to the DataStore implementing this FeatureStore.
     *
     * @return DataStore implementing this FeatureStore
     */
    // Removed as it causes problems when implemeing CoverageSource as a wrapper
    // CoverageAccess getParentCoverageDataSet();
    
    public void dispose();
    
    public CoverageResponse read(CoverageRequest request, Hints ints, ProgressListener listener )throws IOException;
    
    
}