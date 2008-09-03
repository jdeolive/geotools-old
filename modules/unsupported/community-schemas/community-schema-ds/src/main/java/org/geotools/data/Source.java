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

package org.geotools.data;

import java.util.Collection;

import org.geotools.catalog.GeoResourceInfo;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;


/**
 * A read-only source of geospatial information.
 * <p>
 * Access to the spatial information in a filtered/queried or not. Access
 * is purely <strong>read-only</strong> with this interface.
 * </p>
 *
 * @author Jody Garnett
 * @author Thomas Marti
 * @author Stefan Schmid
 *
 * @source $URL$
 * @version $Id$
 * @since 2.4
 * @deprecated This is a Proposal, we need your feedback!
 * @author Jody Garnett, Refractions Research Inc.
 */
public interface Source /*<Content,Description>*/ {
    /**
     * Information about the data available here.
     * <p>
     * Focus is on human readable description of the service,
     * with enough information for searching.
     * </p>
     * The following information is important for programatic things like rendering:
     * <ul>
     * <li>bounds
     * <li>crs
     * </ul>
     *
     * @return GridServiceInfo ?
     */
    GeoResourceInfo getInfo();

    /**
     * Get the corresponding DataService, that created this Source.
     */

    // Comment this out, if you think it is needed!
    //	DataAccess<Content,Description> getDataAccess();

    /**
     * Description of the supported filter capabilities.
     *
     * @return Supported filter capabilities
     */
    FilterCapabilities getFilterCapabilities();

    /**
     * Get the complete data of this <code>Source</code> implementation. No filters or
     * queries are applied.
     *
     * @return An immutable Collection, may be empty, but never <code>null</code>
     */
    Collection /*<Content>*/ content();

    /**
     * Get the complete data of this <code>Source</code> implementation. No filters are
     * applied.
     *
     * @return A immutable Collection, may be empty, but never <code>null</code>
     */

    //	TODO check catalog service web spec. and change param types accordingly
    Collection /*<Content>*/ content(String query, String queryLanguage);

    /**
     * A collection containing all the data indicated by the filter.
     *
     * @return A immutable Collection, may be empty, but never <code>null</code>
     */
    Collection /*<Content>*/ content(Filter filter);

    /**
     * A collection containing all the data indicated by the filter, with a maximum
     * of <code>countLimit</code> elements.
     *
     * @return A immutable Collection, may be empty, but never <code>null</code>
     */
    Collection /*<Content>*/ content(Filter filter, int countLimit);

    /**
     * Description of content in an appropriate format.
     * <ul>
     *   <li>AttributeDescriptor: when serving up features</li>
     *   <li>Class: when providing access to a java domain model</li>
     *   <li>URL: of XSD document when working with XML document</li>
     *   <li>etc...</li>
     * </ul>
     *
     * @return FeatureType, ResultSetMetaData, Class, whatever?
     */
    Object /*Description*/ describe();

    /**
     * Names of the content this data source provides.
     *
     * @return The type name
     */
    Name getName();

    /**
     * Provides a transaction for commit/rollback control of this <code>Source</code>.
     *
     * @param t The transaction
     */
    void setTransaction(Transaction t);

    /**
     * Clean up any resources, listeners, etc that made use of this Source of data.
     * <p>
     * Please note this <code>Source</code> will not function after this method is
     * called. Any {@link Transaction.State} mementos placed on the current transaction
     * will also be cleaned up (although the transaction itself will not be canceled
     * - as it may be in use by others).
     * </p>
     */
    void dispose();
}
