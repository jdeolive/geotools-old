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

//geotools imports
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.FIDReader;
import org.geotools.data.jdbc.JDBCDataStore.QueryData;

/**
 * FIDReader for JDBC.  Assumes that the column being passed in is
 * a number, as the typeName is always appended on.  This is because
 * id's must start with a letter or an underscore.  If needed we
 * could only append the typeName if the column is an int.   
 *
 * @task TODO: factory construction methods?
 *
 * @author  Chris Holmes
 */
public class ResultSetFIDReader implements FIDReader, QueryDataListener {

    private JDBCDataStore.QueryData queryData;
    private ResultSet results;
    private int column;
    /** The logger for the jdbc module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.jdbc");
    private int index = 0; // this is now zero since Readers have been changed to the Iterator pattern
    private String typeName;
    private boolean isClosed = false;

    /**
     * Constructor, for a continuous array of result columns.  If the geometry
     * column.
     * @param results The ResultSet to read attribute from.
     * @param typeName The typename to append.  
     * @param column the offset at which the fid column is.
     * Starts at 1, corresponding to the java ResultSet way of doing things.
     */
    public ResultSetFIDReader(ResultSet results, String typeName, int column) {
        this.typeName = typeName;
        this.results = results;
        this.column = column;
        LOGGER.finer("resultset fid reader constrcted with " + typeName + ", " + column);
    }

    /** Constructor that takes a QueryData object instead of a ResultSet.
     * 
     * @param queryData The queryData object
     * @param typename  The typename to preppend.
     * @param column the offset at which the fid column is.
     * Starts at 1, corresponding to the java ResultSet way of doing things.
     */
    public ResultSetFIDReader(JDBCDataStore.QueryData queryData, String typename, int column) {
        this.typeName = typename;       
        this.results = queryData.getResultSet();
        this.column = column;
        this.queryData = queryData;
        queryData.addQueryDataListener(this);
    }

    public boolean hasNext() throws IOException {
        if (isClosed) {
            throw new IOException("This FIDReader has already been closed");
        }
        
        try {
            if (index == 0)  {                
                boolean b = results.first();
                return b;
            } else  {
        		results.absolute(index);
        		return ! (results.isLast() || results.isAfterLast());
            }
        } catch (SQLException sqlException) {
            queryData.close( sqlException );
            String msg = "Error checking for more content"; 
            LOGGER.log(Level.SEVERE,msg,sqlException);
            throw new DataSourceException(msg, sqlException);                         
        }
    }

    public String next() throws IOException {
        if (isClosed) {
            throw new IOException("This FIDReader has already been closed");
        }
        
        if (!(hasNext())) {
            throw new IOException("No more fids, check hasNext() before " + "calling next()");
        }
        
        try {            
            index++;
            String fid = null;
            results.absolute(index);
            fid = typeName + "." + results.getString(column);
            return fid;
        } catch (SQLException sqlException) {
            queryData.close( sqlException );
            String msg = "Error obtaining more content"; 
            LOGGER.log(Level.SEVERE,msg,sqlException);
            throw new DataSourceException(msg, sqlException);                        
        }
    }

    public void close() throws IOException {
        if (!isClosed)  {
            isClosed = true;
            if (queryData != null)  {
                queryData.close( null );
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.geotools.data.jdbc.QueryDataListener#queryDataClosed(org.geotools.data.jdbc.JDBCDataStore.QueryData)
     */
    public void queryDataClosed(QueryData queryData) {
        isClosed = true;
    }

}
