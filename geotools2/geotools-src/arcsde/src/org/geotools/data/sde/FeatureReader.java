/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data.sde;

import org.geotools.data.DataSourceException;
import org.geotools.feature.Feature;


/**
 * First attempt to get a streaming low level api for feature fetching. This
 * interface will be replaced by new data-exp API, but it still here to
 * support the old style filling of FeatureCollection inside SdeDataSource, so
 * this interface is likely to die.
 *
 * @author Gabriel Roldán
 * @version 0.1
 */
public interface FeatureReader
{
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public boolean hasNext() throws DataSourceException;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public Feature next() throws DataSourceException;

    /**
     * DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public void rewind() throws DataSourceException;

    /**
     * DOCUMENT ME!
     */
    public void close();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isClosed();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int size();
}
