/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.data.feature;

import org.geotools.data.DataAccess;
import org.geotools.data.DataStore;


/**
 * Provides access to features.
 * <p>
 * This is a placeholder sanity check *only* - it shows that DataStore and DataAccess
 * can co-exist.
 * <p>
 * Additional feature specific convience methods are available via the DataStore interface,
 * there is no need to deprecate these methods (Although some of them have not aged well
 * - ie getTypeNames should now be considered for display purposes only).
 * </p>
 * @since 2.4
 * @deprecated This is a Proposal, we need your feedback!
 * @author Jody Garnett, Refractions Research Inc.
 */
public interface FeatureAccess extends DataStore, DataAccess {
}
