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
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.FIDReader;
import org.geotools.data.jdbc.QueryData.RowData;

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
public class ResultSetFIDReader implements FIDReader, QueryDataListener, QueryDataObserver {

    private QueryData queryData;
    private int column;
    /** The logger for the jdbc module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.jdbc");
    private String typeName;
    private boolean isClosed = false;

    /** Constructor that takes a QueryData object instead of a ResultSet.
     * 
     * @param queryData The queryData object
     * @param typename  The typename to preppend.
     * @param column the offset at which the fid column is.
     * Starts at 1, corresponding to the java ResultSet way of doing things.
     */
    public ResultSetFIDReader(QueryData queryData, String typename, int column) {
        this.typeName = typename;       
        this.column = column;
        this.queryData = queryData;
        queryData.attachObserver(this);
    }

    public boolean hasNext() throws IOException {
        if (isClosed) {
            throw new IOException("This FIDReader has already been closed");
        }
        
        try {
           boolean hasNext = queryData.hasNext(this);
           queryData.next(this); // need to call next now because of API differences.
           return hasNext;
        } catch (SQLException sqlException) {
            queryData.close( sqlException, this );
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
            RowData rd = queryData.getRowData(this);
            return typeName + "." + rd.read(1);            
        } catch (SQLException sqlException) {
            queryData.close( sqlException, this );
            String msg = "Error obtaining more content"; 
            LOGGER.log(Level.SEVERE,msg,sqlException);
            throw new DataSourceException(msg, sqlException);                        
        }
    }

    public void close() throws IOException {
        if (!isClosed)  {
            isClosed = true;
            if (queryData != null)  {
                queryData.close( null, this );
                queryData.removeObserver(this);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.geotools.data.jdbc.QueryDataListener#queryDataClosed(org.geotools.data.jdbc.JDBCDataStore.QueryData)
     */
    public void queryDataClosed(QueryData queryData) {
        isClosed = true;
    }

    /**
     * @see org.geotools.data.jdbc.QueryDataListener#rowDeleted(org.geotools.data.jdbc.JDBCDataStore.QueryData)
     * @param queryData
     */
    public void rowDeleted(QueryData queryData) {}
}
