/*
 * Created on 15/10/2003
 *
 */
package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.AbstractAttributeIO;
import org.geotools.data.AttributeReader;
import org.geotools.data.AttributeWriter;
import org.geotools.data.DataSourceException;
import org.geotools.data.jdbc.QueryData.RowData;
import org.geotools.feature.AttributeType;

/** Provides an AttriuteReader over a result set limiting the columns
 *  to a defined range.  This uses the default SQL->Java type mappings
 *  of the result set.
 * 
 * 	<p>Ranges are based on JDBC ResultSet column indexes which begin at 1</p>
 * 
 *  <p>This reader does not handle any geometries.</p>   
 * 
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 */
public class ResultSetAttributeIO extends AbstractAttributeIO
                implements QueryDataListener, QueryDataObserver, 
                            AttributeWriter, AttributeReader {
	private static final Logger LOGGER = Logger.getLogger("org.geotools.data.jdbc");
	/** A flag to track the status of the result set. */
	private boolean isClosed = false;

    private boolean isInsertRow = false;
    
	protected QueryData queryData;
	
	/** The start of the column range */
	private int startColumn;
	
	/** The end of the column range */
	private int endColumn;
	
	/** Creates a new RangedResultSetAttributeReader.
	 * 
	 *  This will read all the attributes in the columns from
	 *  startColumn to endColumn - 1.  These indexes are 1 based.
	 * 
	 * @param metadata The meta data of the columns.
	 * @param queryData The query data object that encapsulates the
     * results of a JDBC query.  This reader will be registered as a
     * QueryDataListener so whenever the queryData is closed, the reader
     * will also be closed.
	 * @param startColumn The starting column, inclusive.
	 * @param endColumn The ending index, exclusive.
	 */
	public ResultSetAttributeIO(AttributeType[] metadata, QueryData querydata,
										int startColumn, int endColumn) {
		super(metadata);
        LOGGER.finer("Creating Ranged AttributeReader: " + Arrays.asList(metaData) + 
                " range: " + startColumn + "-" + endColumn);
        this.queryData  = querydata;
        queryData.attachObserver(this);
		this.startColumn = startColumn;
		this.endColumn = endColumn;
	}

	/** This closes the QueryData object that the Read was constructed with.
     *  Closing this object will force all other Readers using the
     *  queryData object to be notified of the close if they are registered
     *  as QueryDataListeners of the queryData object.
	 * 
	 *  <p>This method will also set a flag to indicate that the results
	 *  have been closed elsewhere and future calls to the read methods
	 *  of the reader will throw errors.
	 *  
	 *  @see org.geotools.data.AttributeReader#close()
	 *  @throws IOException This will never be thrown by this class.
	 */
	public void close() throws IOException {
        if (!isClosed())  {
            this.isClosed = true;
            queryData.close( null, this );
            queryData.removeObserver(this);
        }
	}

	/** Returns true if there are more rows.
	 * @see org.geotools.data.AttributeReader#hasNext()
	 * @throws IOException This will only occur if an SQLException occurs
	 * on the result set.
	 */
	public boolean hasNext() throws IOException {
		if (isClosed()) {
			throw new IOException("Close has already been called on this AttributeReader.");
		}
		
		try {            
            return queryData.hasNext(this);
		} catch (SQLException sqlException) {
            queryData.close( sqlException, this );
			String msg = "SQL Error calling isLast on result set";
			LOGGER.log(Level.SEVERE,msg,sqlException);			
			throw new DataSourceException(msg + ":" + sqlException.getMessage(), sqlException);
		}
	}

	/** Moves to the next row in the result set.
	 * @see org.geotools.data.AttributeReader#next()
	 */
	public void next() throws IOException {
		if (isClosed()) {
			throw new IOException("Close has already been called on this AttributeReader.");
		}
		        
        queryData.next(this);
	}

	/** Reads an attribute from the given column in the current row.
	 * 
	 *  @see org.geotools.data.AttributeReader#read(int)
	 *  @param i The zero based index into the attributes of this attribute reader.
	 *  This index is based on the index of the attribute in the array of AttributeTypes
	 *  this Reader uses as meta data.  The index is internally mapped to the result set 
	 *  column index so this is transparent to the user.
	 *  @throws IOException If an error occurs setting thr row or retreiving the value
	 *  from the column or the Reader has previously bee closed.
	 *  @throws ArrayIndexOutOfBoundsException If the index passed as i is greater than
	 *  the number of attributes in the schema or outside the range of this reader.
	 *  i is outside the range of the reader if <tt>(startColumn + i) &lt; startColumn</tt>
	 *  or <tt>(startColumn + i) &gt;= endColumn</tt>. 
	 */
	public Object read(final int i) throws IOException, ArrayIndexOutOfBoundsException {
        if (isClosed()) {
			throw new IOException("Close has already been called on this AttributeReader.");
		}
		
		int rsPosition = convertIndex(i);
		
		try {
			RowData rd = queryData.getRowData(this);
            return rd.read(rsPosition);
		} catch (SQLException sqlException) {
            queryData.close( sqlException, this );
            String msg = "Error getting value from column position " + rsPosition;
            LOGGER.log(Level.SEVERE,msg,sqlException);             
			throw new DataSourceException( msg, sqlException);
		}
	}
    /**
     * Converts from schema to resultset based index.
     * <p>
     * Converts from schema based index (zero based) to result set index
     * (starts at one, but needs to account for Feature ID column).
     * The field <code>startColumn</code> is used to account for FID.
     * </p>
     * @param i origional schema based index
     * @return converted result set based column index
     */    
    protected int convertIndex(final int i) {
        if (i > getAttributeCount()) {
			throw new ArrayIndexOutOfBoundsException("read called with " + i + " but there are only " +				getAttributeCount() + " attributes in the schema.");
		}
		
		int rsPosition = startColumn + i;
		
		if (rsPosition < startColumn || rsPosition >= endColumn) {
			throw new ArrayIndexOutOfBoundsException("i must be less than " + (endColumn - i - 1));
		}
        return rsPosition;
    }

    /** This writes the attribute to the position within the ResultSet.
     * 
     *  <p>Currently uses the update methods of result set to write the data.</p>
     * 
     *  <ul>Issues to consider:
     *   <li>Where should updateRow() and insertRow() be called?</li>
     *   <li>Who should handle the positioning of the cursor.  This can either
     *       be done in this method or by the FeatureWriter.  If it is done
     *       here we need to check for insertion.  This could be done by checking
     *       if isAfterLast is true, in which case we go to the insertRow.
     *   </li>
     *   <li>Where should moveToInsertRow() and moveToCurrentRow() be called??</li>
     *  </ul>
     * @see org.geotools.data.AttributeWriter#write(int, java.lang.Object)
     */
    public void write(int position, Object attribute) throws IOException {
        if (isClosed()) {
            throw new IOException("Close has already been called on this AttributeReader.");
        }

        int rsPosition = convertIndex(position);
        
        try {
            LOGGER.info("Setting " + rsPosition + " to " + attribute);  
            RowData rd = queryData.getRowData(this);
            rd.write(attribute, rsPosition);                	    
        } catch (SQLException sqlException) {
            queryData.close( sqlException, this );
            String msg = "Error updating object at " + position + 
                    "/" + rsPosition + " with " + attribute; 
            LOGGER.log(Level.SEVERE,msg,sqlException);
            throw new DataSourceException(msg, sqlException);                        
        }
    }
    
    /* (non-Javadoc)
     * @see org.geotools.data.jdbc.QueryDataListener#queryDataClosed(org.geotools.data.jdbc.JDBCDataStore.QueryData)
     */
    public void queryDataClosed(QueryData queryData) {
    }

    /**
     * @see org.geotools.data.jdbc.QueryDataListener#rowDeleted(org.geotools.data.jdbc.JDBCDataStore.QueryData)
     * @param queryData
     */
    public void rowDeleted(QueryData queryData) {
    }
    
    public boolean isClosed() {
        return this.isClosed;
    }
}
