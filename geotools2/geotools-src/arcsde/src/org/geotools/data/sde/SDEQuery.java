package org.geotools.data.sde;

import com.vividsolutions.jts.geom.Envelope;
import com.esri.sde.sdk.client.*;
import org.geotools.data.DataSourceException;
import java.util.logging.*;

/**
 * Wrapper class extends SeQuery to hold a SeConnection until close() is
 * called.
 *
 * @author Gabriel Roldán
 * @version $Id: SDEQuery.java,v 1.3 2003/11/14 17:21:04 groldan Exp $
 */
public class SDEQuery
{
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.sde");

    private SeConnection connection;
    private SdeConnectionPool connectionPool;
    private SeQuery query;
    private SeSqlConstruct sqlConstruct;

    /**
     * Creates a new SDEQuery object.
     *
     * @param pool DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public SDEQuery(SdeConnectionPool pool) throws DataSourceException
    {
        this(pool, null, null);
    }

    /**
     * Creates a new SDEQuery object.
     *
     * @param pool DOCUMENT ME!
     * @param columns DOCUMENT ME!
     * @param construct DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public SDEQuery(SdeConnectionPool pool, String[] columns,
        SeSqlConstruct sqlConstruct) throws DataSourceException
    {
        this.connectionPool = pool;
        this.connection = pool.getConnection();
        this.sqlConstruct = sqlConstruct;
        try
        {
            this.query = new SeQuery(connection, columns, sqlConstruct);
        }
        catch (SeException ex)
        {
            close();
            throw new DataSourceException("Can't create a SDE query: "
                + ex.getMessage(), ex);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////////////// RELEVANT METHODS WRAPPED FROM SeStreamOp ////////////////
    ////////////////////////////////////////////////////////////////////////

    /**
     * Closes the query and releases the underlying stream back to the stream
     * pool. If reset is TRUE, the query status is set to INACTIVE; also
     * releases the SeConnection back to the SeConnectionPool
     *
     * @throws SeException DOCUMENT ME!
     */
    public void close()
    {
        try {
          if ( (connectionPool != null) && (connection != null)) {
            connectionPool.release(connection);
            connection = null;
            connectionPool = null;
          }
          if(query != null)
            query.close();
        }catch (SeException ex) {
          LOGGER.warning("Trying to close an SeQuery: " + ex.getMessage());
        }
    }

    /**
     * Describes a given column by returning the SeColumnDefinition. Column
     * numbers are sequential left to right, starting at zero. Get an array of
     * SeColumnDefinitions resulting from an executed query through the
     * SeRow.getColumns() method.
     *
     * @param columnNum column position 0-n
     *
     * @return SeColumnDefinition the definition of the column at position
     *         columnNum.
     *
     * @throws SeException DOCUMENT ME!
     */
    public SeColumnDefinition describeColumn(int columnNum)
        throws SeException
    {
        return query.describeColumn(columnNum);
    }

    /**
     * Determines if the stream operation is in use
     *
     * @return true if the stream operation is in use
     *
     * @throws SeException DOCUMENT ME!
     */
    public boolean inProgress() throws SeException
    {
        return query.inProgress();
    }

    /**
     * Tells the server to execute a stream operation.
     *
     * @throws SeException DOCUMENT ME!
     */
    public void execute() throws SeException
    {
        query.execute();
    }

    /**
     * Flushes any outstanding insert/update buffers.
     *
     * @throws SeException DOCUMENT ME!
     */
    public void flushBufferedWrites() throws SeException
    {
        query.flushBufferedWrites();
    }

    /**
     * Cancels the current operation on the stream. If <code>reset</code> is
     * TRUE, the query status is set to INACTIVE. If reset is FALSE the query
     * status is set to CLOSED.
     *
     * @param reset if true the Query is closed, ele it is resetted to be
     *        reused
     *
     * @throws SeException DOCUMENT ME!
     */
    public void cancel(boolean reset) throws SeException
    {
        query.cancel(reset);
    }

    /**
     * Sets state constraints for input and output stream operations. If a
     * differenct type is specified, then only features different in the way
     * supplied are returned.
     *
     * <p>
     * differencesType:
     *
     * <ul>
     * <li>
     * SeState.SE_STATE_DIFF_NOCHECK Returns all features in the source state.
     * It doesn't check the differences between source state and differences
     * state.
     * </li>
     * <li>
     * SeState.SE_STATE_DIFF_NOCHANGE_UPDATE Returns all features that haven't
     * changed in the source state, but have been updated in the differences
     * state.
     * </li>
     * <li>
     * SeState.SE_STATE_DIFF_NOCHANGE_DELETE Returns all features that haven't
     * changed in the source state, but have been deleted in the differences
     * state.
     * </li>
     * <li>
     * SeState.SE_STATE_DIFF_UPDATE_NOCHANGE Returns all features that have
     * been updated in the source state, but unchanged in the differences
     * state.
     * </li>
     * <li>
     * SeState.SE_STATE_DIFF_UPDATE_UPDATE Returns all features that have been
     * updated in both the source and difference states.
     * </li>
     * <li>
     * SeState.SE_STATE_DIFF_UPDATE_DELETE Returns all features that have been
     * updated in the source state but deleted in the difference states.
     * </li>
     * <li>
     * SeState.SE_STATE_DIFF_INSERT Returns all features that were inserted
     * into the source state and that never existed in the differences state.
     * </li>
     * </ul>
     * </p>
     *
     * @param sourceId The id of the state to direct input into and take output
     *        from
     * @param differencesId The id of the second state to take differing output
     *        from.
     * @param differencesType The type of difference detection requested
     *
     * @throws SeException DOCUMENT ME!
     */
    public void setState(SeObjectId sourceId, SeObjectId differencesId,
        int differencesType) throws SeException
    {
        query.setState(sourceId, differencesId, differencesType);
    }

    /**
     * Sets a logfile for auto-logging.
     *
     * <p>
     * If The <code>logfileOnly</code> parameter is set to TRUE - results go to
     * the logfile only. The entire query will be processed at execute time
     * with the feature ids being logged. FALSE - results go to both the
     * logfile and the client. The individual feature ids from each fetched
     * row will be added to the logfile as they are fetched on demand by the
     * client. Note that logfileOnly must be set to FALSE for insert/update
     * operations. In these operations, we don't have ids to log if we don't
     * store the feature in the database. So, logfileOnly will automatically
     * be set to FALSE for insert or update operations.
     * </p>
     *
     * @param log The description of the logfile on the server to log results
     *        to
     * @param logfileOnly send results to logfile only?
     *
     * @throws SeException DOCUMENT ME!
     */
    public void setLogfile(SeLog log, boolean logfileOnly)
        throws SeException
    {
        query.setLogfile(log, logfileOnly);
    }

    /**
     * Sets the row locking environment for a stream.
     *
     * <p>
     * The row locking environment remains in effect until the stream is closed
     * with reset TRUE or the stream is freed. The row lock types are:
     *
     * <ul>
     * <li>
     * SE_ROWLOCKING_LOCK_ON_QUERY - Rows selected by a query are locked.
     * </li>
     * <li>
     * SE_ROWLOCKING_LOCK_ON_INSERT - New rows are locked when inserted.
     * </li>
     * <li>
     * SE_ROWLOCKING_LOCK_ON_UPDATE - Updated rows are locked.
     * </li>
     * <li>
     * SE_ROWLOCKING_UNLOCK_ON_QUERY - Locks are removed upon query.
     * </li>
     * <li>
     * SE_ROWLOCKING_UNLOCK_ON_UPDATE - Modified rows are unlocked.
     * </li>
     * <li>
     * SE_ROWLOCKING_FILTER_MY_LOCKS - Only rows locked by the user are
     * returned on query.
     * </li>
     * <li>
     * SE_ROWLOCKING_FILTER_OTHER_LOCKS - Only rows locked by other users are
     * returned on query.
     * </li>
     * <li>
     * SE_ROWLOCKING_FILTER_UNLOCKED - Only unlocked rows are returned.
     * </li>
     * <li>
     * SE_ROWLOCKING_LOCK_ONLY - Query operations lock but don't return rows.
     * </li>
     * </ul>
     * </p>
     *
     * @param lockActions DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     */
    public void setRowLocking(int lockActions) throws SeException
    {
        query.setRowLocking(lockActions);
    }

    ////////////////////////////////////////////////////////////////////////
    ///////////////// METHODS WRAPPED FROM SeQuery /////////////////////
    ////////////////////////////////////////////////////////////////////////

    /**
     * Initializes a stream with a query using a selected set of columns and an
     * SeSqlConstruct object for the where clause. The where clause can’t
     * contain any ORDER BY or GROUP BY clauses.
     *
     * @throws SeException DOCUMENT ME!
     */
    public void prepareQuery() throws SeException
    {
        query.prepareQuery();
    }

    /**
     * Initializes a stream with a query using an SeQueryInfo object.The
     * SE_QUERYINFO structure includes parameters to define tables, columns,
     * where clause, query type, ORDER BY clauses and DBMS hints.
     *
     * @param qInfo the SeQueryInfo object handle.
     *
     * @throws SeException DOCUMENT ME!
     */
    public void prepareQueryInfo(SeQueryInfo qInfo) throws SeException
    {
        query.prepareQueryInfo(qInfo);
    }

    /**
     * Returns the number of columns on the query.
     *
     * @return DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     */
    public int getNumColumns() throws SeException
    {
        return query.getNumColumns();
    }

    /**
     * Tells the server to prepare the sql statement for execution.
     *
     * @param sqlStatement DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     */
    public void prepareSql(String sqlStatement) throws SeException
    {
        query.prepareSql(sqlStatement);
    }

    /**
     * Fetches an SeRow of data.
     *
     * @return DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     */
    public SeRow fetch() throws SeException
    {
        return query.fetch();
    }

    /**
     * Fetches a single row based on the feature id. A call to this method
     * immeadiately retrieves the row from the database. execute need not be
     * called.
     *
     * @param table DOCUMENT ME!
     * @param seRowId DOCUMENT ME!
     * @param columns DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     */
    public SeRow fetchRow(String table, SeObjectId seRowId, String[] columns)
        throws SeException
    {
        return query.fetchRow(table, seRowId, columns);
    }

    /**
     * Returns the envelope for all features within the layer that pass any SQL
     * construct, state, or spatial constraints for the stream.
     *
     * @param seQueryInfo SeQueryInfo object handle.
     *
     * @return DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     */
    public Envelope calculateLayerExtent()
        throws DataSourceException
    {
        Envelope env = null;
        try {
          SeQueryInfo sdeQueryInfo = new SeQueryInfo();
          sdeQueryInfo.setConstruct(sqlConstruct);
          SeExtent extent = query.calculateLayerExtent(sdeQueryInfo);
          env = new Envelope(extent.getMinX(), extent.getMaxX(),
                             extent.getMinY(), extent.getMaxY());
        }catch (SeException ex) {
          throw new DataSourceException("Error calculating query extent: " +
                                        ex.getMessage(), ex);
        }
        return env;
    }

    /**
     * Sets the spatial filters on the query using SE_OPTIMIZE as the policy
     * for spatial index search
     *
     * @param filters a set of spatial constraints to filter upon
     *
     * @throws SeException DOCUMENT ME!
     */
    public void setSpatialConstraints(SeFilter[] filters)
        throws SeException
    {
        query.setSpatialConstraints(SeQuery.SE_OPTIMIZE, false, filters);
    }
}
