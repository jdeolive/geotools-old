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

import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;


/**
 * DOCUMENT ME!
 *
 * @author not attributable
 * @version $Id: SdeFeatureReader.java,v 1.10 2004/01/09 16:58:24 aaime Exp $
 */
public class SdeFeatureReader implements FeatureReader
{
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.sde");

    /** DOCUMENT ME! */
    private FeatureType type;

    /** DOCUMENT ME! */
    private SDEQuery sdeQuery;

    /** DOCUMENT ME! */
    private SeRow stream;
    private GeometryBuilder geometryBuilder;
    private int currentIndex = 0;
    private int maxFeatures;
    private String fidPrefix;

    /**
     * Creates a new SdeFeatureReader object.
     *
     * @param type DOCUMENT ME!
     * @param sdeQuery DOCUMENT ME!
     * @param maxFeatures DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public SdeFeatureReader(FeatureType type, SDEQuery sdeQuery, int maxFeatures)
        throws DataSourceException
    {
        this.type = type;
        this.sdeQuery = sdeQuery;
        this.maxFeatures = maxFeatures;
        this.fidPrefix = type.getTypeName() + ".";

        try
        {
            this.stream = sdeQuery.fetch();
        }
        catch (SeException ex)
        {
            if(sdeQuery != null)
              sdeQuery.close();
            throw new DataSourceException("Can't fetch query results row: "
                + ex.getMessage(), ex);
        }

        AttributeType geometryAttribute = type.getDefaultGeometry();

        if (geometryAttribute != null)
        {
            this.geometryBuilder = GeometryBuilder.builderFor(geometryAttribute
                    .getType());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public FeatureType getFeatureType()
    {
        return type;
    }

    /**
     * Reads the feature currently pointed in the ArcSDE <code>SeRow</code>
     * stream and advances to the next record, so <code>hasNext()</code> only
     * needs to check if <code>stream != null</code>
     *
     * @return DOCUMENT ME!
     *
     * @throws java.io.IOException DOCUMENT ME!
     * @throws org.geotools.feature.IllegalAttributeException DOCUMENT ME!
     * @throws java.util.NoSuchElementException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public Feature next()
        throws java.io.IOException,
            org.geotools.feature.IllegalAttributeException,
            java.util.NoSuchElementException
    {
        Feature feature = null;

        if ((stream != null) && (currentIndex++ < maxFeatures))
        {
            try
            {
                feature = rowToFeature(stream, this.type);
                stream = sdeQuery.fetch();
            }
            catch (SeException ex)
            {
                close();
                throw new DataSourceException("Exception fetching sde row", ex);
            }

            if (stream == null)
            {
                close();
            }
        }

        return feature;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws java.io.IOException DOCUMENT ME!
     */
    public boolean hasNext() throws java.io.IOException
    {
        if(stream == null)
          close();
        return stream != null;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws java.io.IOException DOCUMENT ME!
     */
    public void close() throws java.io.IOException
    {
        stream = null;

        if (sdeQuery != null)
            sdeQuery.close();
    }

    /////////////////////////////////////////////////////////////
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
                featureId = new StringBuffer(fidPrefix).append(sdeShape.getFeatureId()
                                                                       .longValue())
                                                       .toString();
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
}
