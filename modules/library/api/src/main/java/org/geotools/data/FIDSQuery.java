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
package org.geotools.data;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;


/**
 * Implementation for Query.FIDS.
 *
 * <p>
 * This query is used to retrive FeatureIds. Query.FIDS is the only instance of
 * this class.
 * </p>
 *
 * <p>
 * Example:
 * <pre><code>
 * featureSource.getFeatures( Query.FIDS );
 * </code></pre>
 * There is nothing special about this implementation; you can achieve the same effect using:
 * <pre><code>
 * Query fids = new Query();
 * fids.setPropertyNames( new String[0] );
 * featureSource.getFeatures( fids );
 * </code></pre>
 * 
 * @source $URL$
 */
class FIDSQuery extends Query {
    static final String[] NO_PROPERTIES = new String[0];

    public String[] getPropertyNames() {
        return NO_PROPERTIES;
    }

    public boolean retrieveAllProperties() {
        return false;
    }

    public int getMaxFeatures() {
        return DEFAULT_MAX; // consider Integer.MAX_VALUE
    }
    
    public Integer getStartIndex(){
        return null;
    }

    public Filter getFilter() {
        return Filter.INCLUDE;
    }

    public String getTypeName() {
        return null;
    }

    public URI getNamespace() {
        return NO_NAMESPACE;
    }

    public String getHandle() {
        return "Request Feature IDs";
    }

    public String getVersion() {
        return null;
    }

    /**
     * Hashcode based on propertyName, maxFeatures and filter.
     *
     * @return hascode for filter
     */
    public int hashCode() {
        String[] n = getPropertyNames();

        return ((n == null) ? (-1) : ((n.length == 0) ? 0 : (n.length | n[0].hashCode())))
        | getMaxFeatures() | ((getFilter() == null) ? 0 : getFilter().hashCode())
        | ((getTypeName() == null) ? 0 : getTypeName().hashCode())
        | ((getVersion() == null) ? 0 : getVersion().hashCode())
        | ((getCoordinateSystem() == null) ? 0 : getCoordinateSystem().hashCode())
        | ((getCoordinateSystemReproject() == null) ? 0 : getCoordinateSystemReproject().hashCode());
    }

    /**
     * Equality based on propertyNames, maxFeatures, filter, typeName and
     * version.
     *
     * <p>
     * Changing the handle does not change the meaning of the Query.
     * </p>
     *
     * @param obj Other object to compare against
     *
     * @return <code>true</code> if <code>obj</code> retrieves only FIDS
     */
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof Query)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        Query other = (Query) obj;

        return Arrays.equals(getPropertyNames(), other.getPropertyNames())
        && (retrieveAllProperties() == other.retrieveAllProperties())
        && (getMaxFeatures() == other.getMaxFeatures())
        && ((getFilter() == null) ? (other.getFilter() == null)
                                  : getFilter().equals(other.getFilter()))
        && ((getTypeName() == null) ? (other.getTypeName() == null)
                                    : getTypeName().equals(other.getTypeName()))
        && ((getVersion() == null) ? (other.getVersion() == null)
                                   : getVersion().equals(other.getVersion()))
        && ((getCoordinateSystem() == null) ? (other.getCoordinateSystem() == null)
                                            : getCoordinateSystem()
                                                  .equals(other.getCoordinateSystem()))
        && ((getCoordinateSystemReproject() == null) ? (other.getCoordinateSystemReproject() == null)
                                                     : getCoordinateSystemReproject()
                                                           .equals(other
            .getCoordinateSystemReproject()));
    }

    public String toString() {
        return "Query.FIDS";
    }

    /**
     * Return <code>null</code> as FIDSQuery does not require a CS.
     *
     * @return <code>null</code> as override is not required.
     *
     * @see org.geotools.data.Query#getCoordinateSystem()
     */
    public CoordinateReferenceSystem getCoordinateSystem() {
        return null;
    }

    /**
     * Return <code>null</code> as FIDSQuery does not require a CS.
     *
     * @return <code>null</code> as reprojection is not required.
     *
     * @see org.geotools.data.Query#getCoordinateSystemReproject()
     */
    public CoordinateReferenceSystem getCoordinateSystemReproject() {
        return null;
    }

    /**
     * @return {@link SortBy#UNSORTED}.
     */
    public SortBy[] getSortBy() {
        return SortBy.UNSORTED;
    }

    /**
     * Returns the GeoTools default hints {@link GeoTools#getDefaultHints()}
     */
    public Hints getHints() {
        return GeoTools.getDefaultHints();
    }
    
    //
    // Not mutable; all values hard coded
    //
    @Override
    public void setCoordinateSystem(CoordinateReferenceSystem system) {
        new UnsupportedOperationException("Query.ALL cannot be changed, please just use as a default.");
    }
    @Override
    public void setCoordinateSystemReproject(CoordinateReferenceSystem system) {
        new UnsupportedOperationException("Query.ALL cannot be changed, please just use as a default.");
    }
    @Override
    public void setFilter(Filter filter) {
        new UnsupportedOperationException("Query.ALL cannot be changed, please just use as a default.");
    }
    @Override
    public void setHandle(String handle) {
        new UnsupportedOperationException("Query.ALL cannot be changed, please just use as a default.");
    }
    @Override
    public void setHints(Hints hints) {
        new UnsupportedOperationException("Query.ALL cannot be changed, please just use as a default.");
    }
    @Override
    public void setMaxFeatures(int maxFeatures) {
        new UnsupportedOperationException("Query.ALL cannot be changed, please just use as a default.");
    }
    @Override
    public void setNamespace(URI namespace) {
        new UnsupportedOperationException("Query.ALL cannot be changed, please just use as a default.");
    }
    @Override
    public void setPropertyNames(List<String> propNames) {
        new UnsupportedOperationException("Query.FIDS cannot be changed, please just use as a default.");
    }
    @Override
    public void setPropertyNames(String[] propNames) {
        new UnsupportedOperationException("Query.FIDS cannot be changed, please just use as a default.");
    }
    @Override
    public void setSortBy(SortBy[] sortBy) {
        new UnsupportedOperationException("Query.FIDS cannot be changed, please just use as a default.");
    }
    @Override
    public void setStartIndex(Integer startIndex) {
        new UnsupportedOperationException("Query.FIDS cannot be changed, please just use as a default.");
    }
    @Override
    public void setTypeName(String typeName) {
        new UnsupportedOperationException("Query.FIDS cannot be changed, please just use as a default.");
    }
    @Override
    public void setVersion(String version) {
        new UnsupportedOperationException("Query.FIDS cannot be changed, please just use as a default.");
    }
}
