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

import org.geotools.data.AbstractAttributeIO;
import org.geotools.data.Transaction;

//geotools imports
import org.geotools.data.DataSourceException;
import org.geotools.feature.AttributeType;
import java.io.IOException;

//J2SE imports
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * An attributeReader to read a sql ResultSet.  This reads all columns as
 * objects.  The AttributeTypes passed in for each column should be be as
 * constrained as possible, having all be DefaultAttributeTypes with type
 * Object is not very useful for clients.  This should be  sufficient to read
 * most sql columns, but it will not work for geometries. Those require
 * special mapping.  A WKTAttributeReader should work for Simple Features for
 * SQL compliant databases, but is not the most efficient way.  Specialized
 * readers should be constructed for better performance.
 * 
 * <p>
 * Keep in mind when using this that the startColumn parameter is based on
 * ResultSet offsets, meaning that the first is at 1, not 0 as in most java
 * arrays.  This class will return the proper 0 indexed position required by
 * read(int position).
 * </p>
 * 
 * <p>
 * In the simple case of one geometry and a number of other attributes, if the
 * geometry is the last attribute then one need only construct one
 * ResultSetAttributeReader and one GeometryAttributeReader.  If the geometry
 * is not at the end, then one will need two of these readers, one for the
 * attributes before the geometry.  If the geometry is column 5 out of 7 then
 * you would need a reader for cols 1 to 4, a geometry reader for 5, and
 * another one for 6 and 7.  A JoiningFeatureReader should then be used.  One
 * could also construct one of these readers per column, and join them all.
 * </p>
 *
 * @author Chris Holmes
 *
 * @task REVISIT: could also consider passing in an array of offsets?
 * @task REVISIT: Not sure if the index of 1 for offset is what we  want.  Need
 *       to figure out which is more intuitive.
 */
public class ResultSetAttributeReader extends AbstractAttributeIO {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.postgis");

    //private ShapefileReader shp;
    //private DbaseFileReader dbf;

    /** Offset of ResultSet to normal arrays, index starts at 1, not 0 */

    //private static final RESULT_SET_OFF = 1;
    protected Object[] atts;
    protected ResultSet results;
    protected int startColumn;
    protected int index = 0; // start at 0 for iterator pattern

    // used for proper error handling
    protected Transaction transaction;
    protected Connection connection;
    /**
     * Constructor, for a continuous array of result columns.  If the geometry
     * column.
     *
     * @param results The ResultSet to read attribute from.
     * @param attTypes The array of attributeTypes corresponding to the
     *        attributes to be returned by this reader.
     * @param startColumn the offset to start returning attributes from the
     *        resultSet. Starts at 1, corresponding to the java ResultSet way
     *        of doing things.
     */
    protected ResultSetAttributeReader(ResultSet results,
        AttributeType[] attTypes, int startColumn, Transaction transaction, Connection connection) {
        super(attTypes);
        this.results = results;        
        atts = new Object[metaData.length];
        this.startColumn = startColumn;
        this.transaction = transaction;
        this.connection = connection;
        LOGGER.fine("made new result set reader");
    }

    public boolean hasNext() throws IOException {
        try {
            LOGGER.fine("hasNext called, going to return " + !results.isLast());

            return !results.isLast();

            //boolean result = results.next();
            //results.previous(); //no has next, must move forward and back.
            //return result;
        } catch (SQLException sqlException ) {
            JDBCDataStore.close( connection, transaction, sqlException );
            String msg = "Error checking for more content"; 
            LOGGER.log(Level.SEVERE,msg,sqlException);
            throw new DataSourceException(msg, sqlException);                        
        }
    }

    public void next() throws IOException {
        LOGGER.fine("next called");

        //try {
        index++;

        //results.next();
        //} catch (SQLException sqle) {
        //throw new IOException(sqle.getMessage());
        //}
    }

    protected Object readColumn(int column) throws IOException {
        try {
            return results.getObject(column);
        } catch (SQLException sqlException) {
            JDBCDataStore.close( connection, transaction, sqlException );
            String msg = "Problem with SQL"; 
            LOGGER.log(Level.SEVERE,msg,sqlException);
            throw new DataSourceException(msg, sqlException);                        
        }
    }

    public Object read(int position) throws IOException {
        if (getAttributeCount() <= position) {
            String message = "requested array position " + position + " but "
                + "there are only " + getAttributeCount() + " attributes.";
            throw new ArrayIndexOutOfBoundsException(message);
        } else {
            int off = position + startColumn;

            //LOGGER.fine("reading pos " + position + ", geomPos is " + geomPos + 
            //    "name is " + names[position] + "type is " + types[position]);
            try {
                Object retObject = null;
                LOGGER.finest("reading position " + position
                    + " getting column " + off + ", atttype is "
                    + metaData[position]);

                //if (metaData[position].isGeometry()) {
                //REVISIT: throw an error?  Default to reading as a string?
                //don't even check this?
                //} else {
                results.absolute(index);
                retObject = readColumn(off);

                //LOGGER.finest("object class is " + attributes[col].getClass());
                //}
                LOGGER.finest(" returning " + retObject);

                return retObject;
            } catch (SQLException sqlException) {
                JDBCDataStore.close( connection, transaction, sqlException );
                String msg = "Error reading at "+off; 
                LOGGER.log(Level.SEVERE,msg,sqlException);
                throw new DataSourceException(msg, sqlException);                                
            }
        }
    }

    public void close() throws IOException {
        try {
            results.close();
        } catch (SQLException sqlException) {
            JDBCDataStore.close( connection, transaction, sqlException );
            String msg = "Error closing results"; 
            LOGGER.log(Level.SEVERE,msg,sqlException);
            throw new DataSourceException(msg, sqlException);            
        }
    }

    //public static PostgisAttributeReader resultSetReader(ResultSet results, FeatureType schema){
    public static ResultSetAttributeReader getReader(ResultSet results,
        AttributeType[] attributes, int startCol, Transaction transaction) throws DataSourceException {
        Connection conn;
        try {
            conn = results.getStatement().getConnection();
        } catch (SQLException e) {
            throw new DataSourceException("Could not determine connection", e );
        }                                         
        return new ResultSetAttributeReader(results, attributes, startCol, transaction, conn);
    }

    public static ResultSetAttributeReader getReader(ResultSet results,    
        AttributeType attribute, int startCol, Transaction transaction) throws DataSourceException {        
        AttributeType[] atts = { attribute };
        return getReader( results, atts, startCol, transaction );        
    }
}
