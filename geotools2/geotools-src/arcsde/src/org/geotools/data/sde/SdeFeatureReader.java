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

import com.esri.sde.sdk.client.*;
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.DataSourceException;
import org.geotools.data.Query;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import org.geotools.filter.GeometryEncoderException;
import org.geotools.filter.GeometryEncoderSDE;
import org.geotools.filter.SQLEncoderException;
import org.geotools.filter.SQLEncoderSDE;
import org.geotools.filter.SQLUnpacker;
import java.util.logging.Logger;


/**
 * SdeDataSource's delegate for reading an sde resultset and constructing
 * Features
 *
 * @author Gabriel Roldán
 * @version 0.1
 */
public class SdeFeatureReader implements FeatureReader
{
    /** package's logger */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.sde");

    /** SDE datasource working on */
    private SdeDataSource dataSource;

    /** OGS query to filter features */
    private Query query;

    /** Extent of the query's result featureset */
    private Envelope resultEnvelope = null;

    /** generated SDE query, reused in each rewind() */
    private SeQuery sdeQuery;

    /** SDE query's result set */
    private SeRow sdeRow;

    /** type to query */
    private FeatureType queryType;

    /** creates JTS geometry based on SDE SeShape's objects */
    private GeometryBuilder geometryBuilder;

    /** where in the iteration process we are */
    private int currentIndex;

    /** wether this reader is closed */
    boolean closed = true;

    /** To create the sql where statement */
    private SQLEncoderSDE sqlEncoder = new SQLEncoderSDE();

    /** To create the array of sde spatial filters */
    private GeometryEncoderSDE geometryEncoder = new GeometryEncoderSDE();

    /**
     * Creates a new SdeFeatureReader object.
     *
     * @param query DOCUMENT ME!
     * @param dataSource DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public SdeFeatureReader(Query query, SdeDataSource dataSource)
        throws DataSourceException
    {
        //this.sdeConn = sdeConnection;
        this.query = query;

        this.dataSource = dataSource;

        //create a FeatureType based on attributes specified in query
        this.queryType = dataSource.getSchema(query);

        //if the geometry attribute has been queried, set up a GeometryBuilder
        AttributeType geometryAttribute = queryType.getDefaultGeometry();

        if (geometryAttribute != null)
        {
            this.geometryBuilder = GeometryBuilder.builderFor(geometryAttribute
                    .getType());
        }

        rewind();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Envelope getResultEnvelope()
    {
        return resultEnvelope;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int size()
    {
        int size = -1;

        SeConnection sdeConn = null;

        try
        {
            String[] qcols = { "count(*)" };

            sdeConn = dataSource.getConnectionPool().getConnection();

            this.sdeQuery = prepareQuery(query, qcols, sdeConn);

            dataSource.getConnectionPool().release(sdeConn);

            SeRow sdeRow = sdeQuery.fetch();

            size = sdeRow.getInteger(0).intValue();

            size = Math.min(size, query.getMaxFeatures());
        }
        catch (SeException ex)
        {
        }
        catch (Throwable dse)
        {
        }
        finally
        {
            dataSource.getConnectionPool().release(sdeConn);
        }

        return size;
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    private String[] getColumns(Query query) throws DataSourceException
    {
        //attributes to retrieve
        String[] qcols = null;

        if (query.retrieveAllProperties()) //retrieve all properties
        {
            FeatureType schema = dataSource.getSchema();

            AttributeType[] atts = schema.getAttributeTypes();

            qcols = new String[atts.length];

            for (int i = 0; i < atts.length; i++)
                qcols[i] = atts[i].getName();
        }
        else
        {
            qcols = query.getPropertyNames();
        }

        return qcols;
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     * @param qcols DOCUMENT ME!
     * @param sdeConn DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    private SeQuery prepareQuery(Query query, String[] qcols,
        SeConnection sdeConn) throws DataSourceException
    {
        //prepare SDE query
        SeQuery sdeQuery = null;

        SeSqlConstruct sqlConstruct;

        try
        {
            sdeQuery = createSeQuery(sdeConn, qcols);

            sdeQuery.prepareQuery();

            sdeQuery.execute();
        }
        catch (SeException ex)
        {
            throw new DataSourceException("Error preparing sde query: "
                + ex.getMessage(), ex);
        }

        return sdeQuery;
    }

    /**
     * here is where the hard work goes...
     *
     * @param sdeConn
     * @param qcols DOCUMENT ME!
     *
     * @return
     *
     * @throws DataSourceException DOCUMENT ME!
     * @throws SeException DOCUMENT ME!
     */
    private SeQuery createSeQuery(SeConnection sdeConn, String[] qcols)
        throws DataSourceException, SeException
    {
        SeQuery sdeQuery = null;

        SeLayer sdeLayer = dataSource.getSdeLayer();

        SeSqlConstruct seSql = new SeSqlConstruct(sdeLayer.getName());

        Filter filter = query.getFilter();

        SQLUnpacker unpacker = new SQLUnpacker(SQLEncoderSDE.getCapabilities());

        unpacker.unPackAND(filter);

        //unpacker.unPackOR(filter);
        Filter sqlFilter = unpacker.getSupported();

        Filter unsupportedFilter = unpacker.getUnSupported();

        unpacker = new SQLUnpacker(GeometryEncoderSDE.getCapabilities());

        unpacker.unPackAND(unsupportedFilter);

        Filter geometryFilter = unpacker.getSupported();

        unsupportedFilter = unpacker.getUnSupported();

        //figure out which of the filter we can use.
        if (sqlFilter != null)
        {
            try
            {
                sqlEncoder.setLayer(sdeLayer);

                String where = sqlEncoder.encode(sqlFilter);

                LOGGER.fine("applying where clause: '" + where + "'");

                seSql.setWhere(where);
            }
            catch (SQLEncoderException sqle)
            {
                String message = "Encoder error" + sqle.getMessage();

                LOGGER.warning(message);

                throw new DataSourceException(message, sqle);
            }
        }

        sdeQuery = new SeQuery(sdeConn, qcols, seSql);

        if (geometryFilter != null)
        {
            try
            {
                geometryEncoder.setLayer(sdeLayer);

                geometryEncoder.encode(geometryFilter);

                SeFilter[] sdeSpatialFilters = geometryEncoder
                    .getSpatialFilters();

                if ((sdeSpatialFilters != null)
                        && (sdeSpatialFilters.length > 0))
                {
                    LOGGER.fine("applying " + sdeSpatialFilters.length
                        + " spatial filters ");

                    sdeQuery.setSpatialConstraints(SeQuery.SE_OPTIMIZE, false,
                        sdeSpatialFilters);
                }
            }

            catch (GeometryEncoderException ex)
            {
                String message = "Encoder error" + ex.getMessage();

                LOGGER.warning(message);

                throw new DataSourceException(message, ex);
            }
        }

        calculateResultEnvelope(sdeQuery, seSql);

        return sdeQuery;
    }

    /**
     * DOCUMENT ME!
     *
     * @param sdeQuery DOCUMENT ME!
     * @param seSql DOCUMENT ME!
     */
    private void calculateResultEnvelope(SeQuery sdeQuery, SeSqlConstruct seSql)
    {
        if (resultEnvelope == null)
        {
            try
            {
                SeQueryInfo queryInfo = new SeQueryInfo();

                queryInfo.setConstruct(seSql);

                //sdeQuery.prepareQueryInfo(queryInfo);
                SeExtent ext = sdeQuery.calculateLayerExtent(queryInfo);

                resultEnvelope = new Envelope(ext.getMinX(), ext.getMaxX(),
                        ext.getMinY(), ext.getMaxY());
            }
            catch (SeException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     *
     * @throws DataSourceException
     */
    public boolean hasNext() throws DataSourceException
    {
        if (currentIndex >= query.getMaxFeatures())
        {
            close();

            return false;
        }

        return sdeRow != null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public Feature next() throws DataSourceException
    {
        /*check again, perhaps the user calls next() without calling hasNext()

         *first violating the filter

         */
        if (!hasNext())
        {
            throw new DataSourceException(
                "there're no more features in this collection");
        }

        Feature feature = null;

        if ((sdeRow != null) && (currentIndex++ < query.getMaxFeatures()))
        {
            try
            {
                feature = rowToFeature(sdeRow, queryType);

                sdeRow = sdeQuery.fetch();
            }
            catch (SeException ex)
            {
                throw new DataSourceException("Exception fetching sde row", ex);
            }
            catch (IllegalAttributeException ex)
            {
                throw new DataSourceException("Can't create a feature from sde row",
                    ex);
            }

            if (sdeRow == null)
            {
                close();
            }
        }

        return feature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param row
     * @param type
     *
     * @return
     *
     * @throws IllegalAttributeException
     * @throws SeException
     * @throws DataSourceException DOCUMENT ME!
     */
    private Feature rowToFeature(SeRow row, FeatureType type)
        throws IllegalAttributeException, SeException, DataSourceException
    {
        Feature f = null;

        int nCols = type.getAttributeCount();

        Object[] values = new Object[nCols];

        AttributeType att;

        SeShape sdeShape;

        String featureId = null;

        for (int i = 0; i < nCols; i++)
        {
            att = type.getAttributeType(i);

            if (att.isGeometry())
            {
                sdeShape = row.getShape(i);

                featureId = String.valueOf(sdeShape.getFeatureId().longValue());

                values[i] = geometryBuilder.construct(sdeShape);
            }
            else
            {
                values[i] = att.parse(row.getObject(i));
            }
        }

        //may throw IllegalAttributeException...
        f = type.create(values, featureId);

        return f;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public void rewind() throws DataSourceException
    {
        LOGGER.fine("--------------rewind()-----------");

        this.currentIndex = 0;

        String[] qcols = getColumns(query);

        SeConnection sdeConn = null;

        try
        {
            sdeConn = dataSource.getConnectionPool().getConnection();

            this.sdeQuery = prepareQuery(query, qcols, sdeConn);

            sdeRow = sdeQuery.fetch();

            dataSource.getConnectionPool().release(sdeConn);
        }
        catch (SeException ex)
        {
            throw new DataSourceException("Exception executing sde query: "
                + ex.getMessage(), ex);
        }
        finally
        {
            dataSource.getConnectionPool().release(sdeConn);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void close()
    {
        setClosed(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isClosed()
    {
        return closed;
    }

    /**
     * DOCUMENT ME!
     *
     * @param close DOCUMENT ME!
     */
    private void setClosed(boolean close)
    {
        this.closed = close;

        if (close)
        {
            sdeRow = null;

            if (sdeQuery != null)
            {
                try
                {
                    sdeQuery.close();
                }
                catch (Exception e)
                {
                }

                sdeQuery = null;
            }
        }
    }
}
