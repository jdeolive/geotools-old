package org.geotools.data.arcsde;

import com.esri.sde.sdk.client.*;
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.DataSourceException;
import org.geotools.feature.*;
import org.geotools.filter.*;
import org.geotools.filter.Filter;
import java.io.IOException;
import java.util.logging.Logger;


/**
 * Wrapper class extends SeQuery to hold a SeConnection until close() is
 * called.
 *
 * @author Gabriel Roldán
 * @version $Id: ArcSDEQuery.java,v 1.1 2004/03/11 00:17:09 groldan Exp $
 */
public class ArcSDEQuery {
    /** DOCUMENT ME!  */
    private static final Logger LOGGER = Logger.getLogger(ArcSDEQuery.class.getPackage()
                                                                           .getName());

    //private SeConnection connection;
    private ArcSDEConnectionPool connectionPool;
    private FeatureType schema;
    private SeQuery query;
    private SeSqlConstruct sqlConstruct;
    private FilterSet filters;

    /**
     * Creates a new SDEQuery object.
     *
     * @param pool DOCUMENT ME!
     * @param schema DOCUMENT ME!
     * @param sqlConstruct DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     *
     * @see prepareQuery
     */
    public ArcSDEQuery(ArcSDEConnectionPool pool, FeatureType schema,
        SeSqlConstruct sqlConstruct) throws DataSourceException {
        this.schema = schema;
        this.connectionPool = pool;
        this.sqlConstruct = sqlConstruct;

        SeConnection connection = null;

        try {
            connection = this.connectionPool.getConnection();
            this.query = new SeQuery(connection, getPropertyNames(),
                    sqlConstruct);
        } catch (SeException seEx) {
            close();
            throw new DataSourceException(
                "Can't create a SDE query: SDE error no."
                + seEx.getSeError().getSdeError() + " ("
                + seEx.getSeError().getErrDesc() + ")", seEx);
        } catch (Throwable ex) {
            close();
            throw new DataSourceException("Can't create a SDE query: "
                + ex.getMessage(), ex);
        } finally {
            connectionPool.release(connection);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String[] getPropertyNames() {
        String[] attNames = new String[schema.getAttributeCount()];

        for (int i = 0; i < schema.getAttributeCount(); i++) {
            attNames[i] = schema.getAttributeType(i).getName();
        }

        return attNames;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public FeatureType getSchema() {
        return schema;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public SeSqlConstruct getSeSqlConstruct() {
        return this.sqlConstruct;
    }

    /**
     * DOCUMENT ME!
     *
     * @param filters DOCUMENT ME!
     */
    public void setFilterSet(FilterSet filters) {
        this.filters = filters;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public FilterSet getFilters() {
        return filters;
    }

    /**
     * if the query has been parsed as just a where clause filter, or has no
     * filter at all, the result count calculation is optimized by selecting a
     * <code>count()</code> single row. If the filter involves any kind of
     * spatial filter, such as BBOX, the calculation can't be optimized by
     * this way, because the ArcSDE Java API throws a <code>"DATABASE LEVEL
     * ERROR OCURRED"</code> exception. So, in this case, a query over the
     * shape field is made and the result is traversed counting the number of
     * rows inside a while loop
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public int calculateResultCount() throws DataSourceException {
        int count = -1;
        SeConnection connection = null;

        try {
            connection = connectionPool.getConnection();
        } catch (UnavailableConnectionException ex) {
            throw new DataSourceException(ex.getMessage(), ex);
        }

        try {
            if (!filters.getGeometryFilter().equals(Filter.NONE)) {
                count = countResults(connection);
            } else {
                LOGGER.fine(
                    "Using the count(*) optimized result count calculation");

                String[] columns = { "count(*)" };
                SeQuery countQuery = null;
                countQuery = new SeQuery(connection, columns, sqlConstruct);
                countQuery.prepareQuery();
                countQuery.execute();
                count = countQuery.fetch().getInteger(0).intValue();
            }
        } catch (DataSourceException ex) {
            throw ex;
        } catch (SeException ex) {
            throw new DataSourceException("Error obtaining result count: "
                + ex.getMessage(), ex);
        } finally {
            connectionPool.release(connection);
        }

        return count;
    }

    private int countResults(SeConnection connection)
        throws SeException, DataSourceException {
        SeQuery countQuery = null;
        LOGGER.fine("Using brut force result count calculation");

        String[] cols = { schema.getDefaultGeometry().getName() };
        SeFilter[] geometryFilters = null;
        int count = 0;

        try {
            geometryFilters = filters.createSpatialFilters();
        } catch (GeometryEncoderException ex) {
            throw new DataSourceException("Can't create the spatial filter: "
                + ex.getMessage(), ex);
        }

        try {
            countQuery = new SeQuery(connection, cols, sqlConstruct);
            connectionPool.release(connection);
            countQuery.setSpatialConstraints(SeQuery.SE_OPTIMIZE, false,
                geometryFilters);
            countQuery.prepareQuery();
            countQuery.execute();

            while (countQuery.fetch() != null) {
                ++count;
            }
        } finally {
            try {
                if (countQuery != null) {
                    countQuery.close();
                }
            } catch (SeException ex) {
                LOGGER.warning(ex.getMessage());
            }
        }

        return count;
    }

    /**
     * Returns the envelope for all features within the layer that pass any SQL
     * construct, state, or spatial constraints for the stream.
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public Envelope calculateQueryExtent() throws IOException {
        Envelope envelope = null;
        SeExtent extent = null;

        if ((this.sqlConstruct.getWhere() == null)
                && filters.getGeometryFilter().equals(Filter.NONE)) {
            LOGGER.fine("Using optimized full layer extent query");

            SeLayer layer = connectionPool.getSdeLayer(sqlConstruct.getTables()[0]);
            extent = layer.getExtent();
        } else {
            LOGGER.fine(
                "Building a new SeQuery to consult it's resulting envelope");

            //we can't reuse the fetching query because the extent can't
            //be calculated if the stream is opened
            SeQuery extentQuery = null;
            SeConnection connection = null;

            try {
                connection = connectionPool.getConnection();
                extentQuery = new SeQuery(connection, getPropertyNames(),
                        sqlConstruct);

                if (filters.getGeometryFilter() != Filter.NONE) {
                    SeFilter[] geometryFilters = filters.createSpatialFilters();
                    extentQuery.setSpatialConstraints(SeQuery.SE_OPTIMIZE,
                        false, geometryFilters);
                }

                SeQueryInfo sdeQueryInfo = new SeQueryInfo();
                sdeQueryInfo.setConstruct(sqlConstruct);
                extent = query.calculateLayerExtent(sdeQueryInfo);
            } catch (GeometryEncoderException ex) {
                throw new DataSourceException(
                    "Can't create the spatial filter: " + ex.getMessage(), ex);
            } catch (Exception ex) {
                throw new DataSourceException(
                    "Can't consult the query extent: " + ex.getMessage(), ex);
            } finally {
                connectionPool.release(connection);

                try {
                    if (extentQuery != null) {
                        extentQuery.close();
                    }
                } catch (SeException ex) {
                    LOGGER.warning("error closing query: " + ex.getMessage());
                }
            }
        }

        envelope = new Envelope(extent.getMinX(), extent.getMaxX(),
                extent.getMinY(), extent.getMaxY());

        return envelope;
    }

    ////////////////////////////////////////////////////////////////////////
    ////////////// RELEVANT METHODS WRAPPED FROM SeStreamOp ////////////////
    ////////////////////////////////////////////////////////////////////////

    /**
     * Closes the query and releases the underlying stream back to the stream
     * pool. If reset is TRUE, the query status is set to INACTIVE; also
     * releases the SeConnection back to the SeConnectionPool
     */
    public void close() {
        try {
            /*
               if ( (connectionPool != null) && (connection != null)) {
                 connectionPool.release(connection);
                 connection = null;
                 connectionPool = null;
               }
             */
            if (query != null) {
                query.close();
            }
        } catch (SeException ex) {
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
        throws SeException {
        return query.describeColumn(columnNum);
    }

    /**
     * Determines if the stream operation is in use
     *
     * @return true if the stream operation is in use
     *
     * @throws SeException DOCUMENT ME!
     */
    public boolean inProgress() throws SeException {
        return query.inProgress();
    }

    /**
     * Tells the server to execute a stream operation.
     *
     * @throws SeException DOCUMENT ME!
     */
    public void execute() throws SeException {
        query.execute();
    }

    /**
     * Flushes any outstanding insert/update buffers.
     *
     * @throws SeException DOCUMENT ME!
     */
    public void flushBufferedWrites() throws SeException {
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
    public void cancel(boolean reset) throws SeException {
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
        int differencesType) throws SeException {
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
        throws SeException {
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
    public void setRowLocking(int lockActions) throws SeException {
        query.setRowLocking(lockActions);
    }

    ////////////////////////////////////////////////////////////////////////
    ///////////////// METHODS WRAPPED FROM SeQuery /////////////////////
    ////////////////////////////////////////////////////////////////////////

    /**
     * Initializes a stream with a query using a selected set of columns and an
     * SeSqlConstruct object for the where clause. The where clause can?t
     * contain any ORDER BY or GROUP BY clauses.
     *
     * @throws SeException DOCUMENT ME!
     */
    public void prepareQuery() throws SeException {
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
    public void prepareQueryInfo(SeQueryInfo qInfo) throws SeException {
        query.prepareQueryInfo(qInfo);
    }

    /**
     * Returns the number of columns on the query.
     *
     * @return DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     */
    public int getNumColumns() throws SeException {
        return query.getNumColumns();
    }

    /**
     * Tells the server to prepare the sql statement for execution.
     *
     * @param sqlStatement DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     */
    public void prepareSql(String sqlStatement) throws SeException {
        query.prepareSql(sqlStatement);
    }

    /**
     * Fetches an SeRow of data.
     *
     * @return DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     */
    public SeRow fetch() throws SeException {
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
        throws SeException {
        return query.fetchRow(table, seRowId, columns);
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
        throws SeException {
        query.setSpatialConstraints(SeQuery.SE_OPTIMIZE, false, filters);
    }
}
