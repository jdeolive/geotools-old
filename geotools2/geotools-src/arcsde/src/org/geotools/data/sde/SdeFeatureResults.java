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
import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.filter.*;
import java.io.IOException;
import java.util.*;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldán
 * @version $Id: SdeFeatureResults.java,v 1.8 2003/11/17 17:12:41 groldan Exp $
 */
public class SdeFeatureResults implements FeatureResults
{
    /** DOCUMENT ME! */
    private SdeFeatureStore source;

    /** DOCUMENT ME! */
    private Query query;

    /***/
    private Envelope resultBounds;

    /**
     * Creates a new SdeFeatureResults object.
     *
     * <p>
     * The <code>Query</code> is stored as instance variable because it will be
     * used in each call to <code>reader()</code> to construct a new
     * <code>SDEQuery</code> instance, wich will serve as the streamed source
     * to the FeatureReader.
     * </p>
     *
     * <p>
     * This constructor also makes use of <code>query</code> to request the
     * results envelope, wich is cached for subsequent calls to
     * <code>getBounds()</code>, anyway, each call to <code>reader()</code>
     * will update the result bounds to avoid inconsistences with the
     * underlying feature set, but it is usefull here so we have an exception
     * to throw if we can't get the envelope, cause FeatureSource.getFeatures
     * requires to throw an exception if something goes wrong, and since we
     * don't really execute the query until reader() is called to keep the
     * method reentrant, it is a good indication if such a query will fail if
     * we can't obtain the envelope
     * </p>
     *
     * @param source DOCUMENT ME!
     * @param query DOCUMENT ME!
     *
     */
    SdeFeatureResults(SdeFeatureStore source, Query query)
    {
        this.source = source;
        this.query = query;
    }

    /**
     * just calls to <code>getSchema(Query)</code> of the SdeFeatureSource
     * that has been created the instance of this class
     *
     * @return DOCUMENT ME!
     */
    public FeatureType getSchema() throws IOException
    {
        return source.getSchema(query);
    }

    /**
     * Using que <code>org.geotools.data.Query</code> this FeatureResults
     * respond for, creates a new instance of <code>SDEQuery</code> on each
     * call, wich will be used as the stream source for returning a new
     * <code>SdeFeatureReader</code>
     *
     * @return a new FeatureReader to iterate over this results
     *
     * @throws IOException see below
     * @throws DataSourceException if an <code>SeException</code> is catched
     *         while prepatring or executing the ArcSDE query, or if the
     *         constructor of <code>SdeFeatureReader</code> throws it, wich
     *         mostly can happen if it can't fetch the first row of data from
     *         the streaming source this method passes as parameter
     */
    public FeatureReader reader() throws IOException
    {
        SdeFeatureReader featureReader = null;

        int maxFeatures = query.getMaxFeatures();
        FeatureType resultType = source.getSchema(query);
        SDEQuery sdeQuery = source.createSeQuery(query);

        try
        {
            this.resultBounds = sdeQuery.calculateLayerExtent();
            sdeQuery.prepareQuery();
            sdeQuery.execute();
        }
        catch (SeException ex)
        {
            if (sdeQuery != null)
                sdeQuery.close();

            throw new DataSourceException("Error executing query:"
                + ex.getMessage(), ex);
        }

        featureReader = new SdeFeatureReader(resultType, sdeQuery, maxFeatures);

        return featureReader;
    }

    /**
     * Returns the cached bounds of the query results that originates this
     * instance of SdeFeatureResults. Such Envelope is obtained at the
     * constructor of this class and is cached for subsecuent calls to
     * getBounds.
     *
     * @return the cached bounds of this results
     *
     * @throws IOException not at all
     */
    public Envelope getBounds() throws IOException
    {
        return resultBounds;
    }

    /**
     * uses it's caller <code>SdeFeatureSource</code> to calculate the result
     * count of the query results this instance represents. No caching is done
     * to avoid inconsistences if the underlying data set changes from call to
     * call
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public int getCount() throws IOException
    {
        return source.getCount(query);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public FeatureCollection collection() throws IOException
    {
        FeatureCollection collection = FeatureCollections.newCollection();
        org.geotools.data.FeatureReader reader = reader();
        Feature f;

        try
        {
            while (reader.hasNext())
            {
                f = reader.next();
                collection.add(f);
            }
        }
        catch (NoSuchElementException ex)
        {
            throw new IOException("No such element: " + ex.getMessage());
        }
        catch (IllegalAttributeException ex)
        {
            throw new IOException(ex.getMessage());
        }

        return collection;
    }
}
