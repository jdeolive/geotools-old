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
package org.geotools.data.jdbc;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

//geotools imports
import org.geotools.data.DataSourceException;
import org.geotools.data.Transaction;
import org.geotools.feature.AttributeType;
import java.io.IOException;

//J2SE imports
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * An attribute reader for well known text.  For now this just reads well known
 * text in result sets, but if there are other cases of  wkt that are not in
 * databases we can rethink or adapt this class to them.
 *
 * @author Chris Holmes
 *
 * @task TODO: combine this with ResultSetAttributeReader, get rid of that and
 *       use RangedResultSetAttReader.
 */
public class WKTAttributeReader extends ResultSetAttributeReader {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.postgis");

    /** Factory for producing geometries (from JTS). */
    private static GeometryFactory geometryFactory = new GeometryFactory();

    /** Well Known Text reader (from JTS). */
    private static WKTReader geometryReader = new WKTReader(geometryFactory);

    protected WKTAttributeReader(ResultSet results, AttributeType[] attTypes,
        int startColumn, Transaction transaction, Connection connection) {
        super(results, attTypes, startColumn, transaction, connection);

        for (int i = 0; i < attTypes.length; i++) {
            if (!attTypes[i].isGeometry()) {
                String mesg = "AttributeTypes of a WKTAttributeReader must be "
                    + "geometries, " + attTypes[i] + " is not";
                throw new IllegalArgumentException(mesg);
            }
        }
    }

    protected Object readColumn(int column) throws IOException {
        Object retObject;

        try {
            String wkt = results.getString(column);

            if (wkt == null) {
                retObject = null;
            } else {
                retObject = geometryReader.read(wkt);
            }

            //LOGGER.fine("returning " + retObject);
            return retObject;
        } catch (SQLException sqlException) {
            JDBCDataStore.close(connection, transaction, sqlException);

            String msg = "Problem with sql";
            LOGGER.log(Level.SEVERE, msg, sqlException);
            throw new DataSourceException(msg, sqlException);
        } catch (ParseException pe) {
            throw new DataSourceException("could not parse wkt" + pe);
        }
    }

    //REVISIT: I'm not sure about these method calls.  Does it make
    //it unclear that you're getting a WKTAttributeReader?  The other
    //option is to declare getReader methods as final, and have a 
    //getGeometryReader method.  
    public static ResultSetAttributeReader getReader(ResultSet results,
        AttributeType[] attributes, int startCol, Transaction transaction)
        throws DataSourceException {
        try {
            Connection conn = results.getStatement().getConnection();

            return new WKTAttributeReader(results, attributes, startCol,
                transaction, conn);
        } catch (SQLException e) {
            throw new DataSourceException("could not determine connection", e);
        }
    }

    public static ResultSetAttributeReader getReader(ResultSet results,
        AttributeType attribute, int startCol, Transaction transaction)
        throws DataSourceException {
        AttributeType[] atts = { attribute };

        return getReader(results, atts, startCol, transaction);
    }

    //this is the other way of doing it.  The other thing we could consider
    //is a DefaultResultSetReader, perhaps that defaults with WKT, and then
    //if other readers want to specialize more they can over-ride a readColumn
    //method.  readColumn could perhaps also take an AttributeType so that
    //subclasses could figure out if they needed to do something special.
    //I'm not sure Sean will like this much, as he seems more about the
    //composition.  But this class does demonstrate how easy it is to over
    //ride for geometries.  Though it might be more complicated for oracle.
    public static WKTAttributeReader getGeometryReader(ResultSet results,
        AttributeType attribute, int startCol, Transaction transaction)
        throws DataSourceException {
        AttributeType[] atts = { attribute };

        try {
            Connection conn = results.getStatement().getConnection();

            return new WKTAttributeReader(results, atts, startCol, transaction,
                conn);
        } catch (SQLException e) {
            throw new DataSourceException("could not determine connection", e);
        }
    }
}
