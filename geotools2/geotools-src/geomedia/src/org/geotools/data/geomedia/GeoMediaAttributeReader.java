/*
 *    GeoLBS - OpenSource Location Based Servces toolkit
 *    (C) 2004, Julian J. Ray, All Rights Reserved
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

package org.geotools.data.geomedia;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.DataSourceException;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.QueryData.RowData;
import org.geotools.data.jdbc.ResultSetAttributeIO;
import org.geotools.feature.AttributeType;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Julian Ray, original by Sean Geoghegan.
 */
public class GeoMediaAttributeReader extends ResultSetAttributeIO {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.geomedia");

    // geometry adpater

    /** DOCUMENT ME! */
    private GeoMediaGeometryAdapter mGeometryAdapter = null;

    /** DOCUMENT ME! */
    private int columnIndex;

    /** DOCUMENT ME! */
    private QueryData queryData;

    /**
     * DOCUMENT ME!
     *
     * @param metaData
     * @param queryData
     * @param columnIndex
     *
     * @throws DataSourceException
     */
    public GeoMediaAttributeReader(AttributeType metaData, QueryData queryData, int columnIndex)
        throws DataSourceException {
        super(new AttributeType[] { metaData }, queryData, columnIndex, columnIndex + 1);
        this.queryData = queryData;
        this.columnIndex = columnIndex;

        mGeometryAdapter = new GeoMediaGeometryAdapter();
    }

    /* (non-Javadoc)
     * @see org.geolbs.data.AttributeReader#read(int)
     */
    public Object read(int i) throws IOException, ArrayIndexOutOfBoundsException {
        if (isClosed()) {
            throw new IOException("Close has already been called on this AttributeReader.");
        }

        // this Reader only reads one attribute so i should always be 0
        if (i != 0) {
            throw new ArrayIndexOutOfBoundsException("This Reader only reads one attribute so i should always be 0");
        }

        try {
            RowData rd = queryData.getRowData(this);

            return mGeometryAdapter.deSerialize((byte[]) rd.read(columnIndex));
        } catch (SQLException e) {
            String msg = "SQL Exception reading geometry column";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new DataSourceException(msg, e);
        } catch (GeoMediaGeometryTypeNotKnownException e) {
            String msg = "Geometry Conversion type error";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new DataSourceException(msg, e);
        } catch (GeoMediaUnsupportedGeometryTypeException e) {
            String msg = "Geometry Conversion type error";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new DataSourceException(msg, e);
        }
    }

    /* (non-Javadoc)
     * @see org.geolbs.data.AttributeWriter#write(int, java.lang.Object)
     */
    public void write(int i, Object attribute) throws IOException {
        if (isClosed()) {
            throw new IOException("Close has already been called on this AttributeReader.");
        }

        // this Reader only reads one attribute so i should always be 0
        if (i != 0) {
            throw new ArrayIndexOutOfBoundsException("This Writer only writes one attribute so i should always be 0");
        }

        try {
            byte[]  blob = mGeometryAdapter.serialize((Geometry) attribute);
            RowData rd = queryData.getRowData(this);
            rd.write((Object) blob, columnIndex);
        } catch (SQLException sqlException) {
            queryData.close(sqlException, this);

            String msg = "SQL Exception writing geometry column";
            LOGGER.log(Level.SEVERE, msg, sqlException);
            throw new DataSourceException(msg, sqlException);
        } catch (GeoMediaUnsupportedGeometryTypeException e) {
            String msg = "Geometry Conversion type error";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new DataSourceException(msg, e);
        }
    }
}
