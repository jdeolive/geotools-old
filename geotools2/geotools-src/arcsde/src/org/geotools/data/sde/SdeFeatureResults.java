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
 * @version 0.1
 */
public class SdeFeatureResults implements FeatureResults
{
    /** DOCUMENT ME! */
    private SdeFeatureSource source;

    /** DOCUMENT ME! */
    private Query query;

    /**
     * Creates a new SdeFeatureResults object.
     *
     * @param source DOCUMENT ME!
     * @param query DOCUMENT ME!
     */
    SdeFeatureResults(SdeFeatureSource source, Query query)
    {
        this.source = source;
        this.query = query;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public org.geotools.data.FeatureReader reader() throws IOException
    {
        SdeFeatureReader featureReader = null;

        SeRow sdeStream = null;
        FeatureType resultType = source.getSchema(query);
        SeQuery sdeQuery = source.createSeQuery(query);
        Envelope resEnv = source.getBounds(sdeQuery);

        featureReader = new SdeFeatureReader(resultType, sdeQuery, resEnv,
                query.getMaxFeatures());

        return featureReader;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public Envelope getBounds() throws IOException
    {
        return source.getBounds(query);
    }

    /**
     * DOCUMENT ME!
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
