/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.arcsde.data;

import java.io.IOException;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.data.DataSourceException;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;

/**
 * Wrapper for an SeRow so it allows asking multiple times for the same
 * property.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/SdeRow.java $
 * @since 2.4.0
 */
class SdeRow {
    /** The actual SeRow */
    private SeRow row;

    /** cached SeRow values */
    private Object[] values;

    /**
     * Creates a new SdeRow object.
     * 
     * @param row
     *            DOCUMENT ME!
     * @param previousValues
     *            needed in case of its a joined result, thus arcsde does not
     *            returns geometry attributes duplicated, just on their first
     *            occurrence (sigh)
     * 
     * @throws IOException
     *             DOCUMENT ME!
     * @throws DataSourceException
     *             DOCUMENT ME!
     */
    public SdeRow(SeRow row, Object[] previousValues) throws IOException {
        this.row = row;
        int nCols;

        try {
            nCols = row.getNumColumns();
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw e;
        }

        values = new Object[nCols];

        int i = 0;

        int statusIndicator = 0;

        try {
            for (i = 0; i < nCols; i++) {
                statusIndicator = row.getIndicator(i);

                if (statusIndicator == SeRow.SE_IS_ALREADY_FETCHED
                        || statusIndicator == SeRow.SE_IS_REPEATED_FEATURE) {
                    values[i] = previousValues[i];
                } else if (statusIndicator == SeRow.SE_IS_NULL_VALUE) {
                    values[i] = null;
                } else {
                    values[i] = row.getObject(i);
                }
            }
        } catch (SeException e) {
            throw new ArcSdeException("getting property #" + i, e);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param index
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws IOException
     *             DOCUMENT ME!
     */
    public Object getObject(int index) throws IOException {
        return values[index];
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Object[] getAll() {
        return values;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param index
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws IOException
     *             DOCUMENT ME!
     */
    public Long getLong(int index) throws IOException {
        return (Long) getObject(index);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param index
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws IOException
     *             DOCUMENT ME!
     */
    public SeShape getShape(int index) throws IOException {
        return (SeShape) getObject(index);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public SeColumnDefinition[] getColumns() {
        return row.getColumns();
    }
}
