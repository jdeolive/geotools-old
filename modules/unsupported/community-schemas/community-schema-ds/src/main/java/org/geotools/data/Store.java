/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data;

import java.util.Collection;
import org.opengis.filter.Filter;


/**
 * A read-write store for geospatial information.
 *
 * <p>
 * First draft of a Store interface based on brain storming session with Jody, Thomas,
 * Stefan and Cory in Refractions on November 24th. It has since been reviewed by Jesse who
 * contributed the connection information messages.
 * </p>
 *
 * <p>The basic idea is to have simple, general interface to access and query data that is in some way or
 * another spatially enabled. And we don't want the restriction to {@link org.geotools.feature.Feature},
 * {@link org.geotools.feature.FeatureType}, {@link org.geotools.data.FeatureSource}, etc. as we have right
 * now in {@link org.geotools.data.DataStore}.</p>
 * </i>
 *
 * <code>Store</code> extends the <code>Source</code> interfaces with writing capabilities.
 *
 * @source $URL$
 * @version $Id$
 * @since 2.4
 * @deprecated This is a Proposal, we need your feedback!
 * @author Jody Garnett, Refractions Research Inc.
 */
public interface Store extends Source {
    /** Read/Write access to GridCoverage */
    public Collection modifiableContent();

    /** Read/Write access to GridCoverage */
    public Collection modifiableContent(Filter filter);

    /** Read/Write access to GridCoverage */
    public Collection modifiableContent(String filter, String queryLanguage);
}
