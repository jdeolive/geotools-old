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
package org.geotools.map;


// JTS dependencies
import com.vividsolutions.jts.geom.Envelope;

// Geotools dependencies
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.ct.Adapters;
import org.geotools.feature.FeatureCollection;
import org.geotools.styling.Style;

// OpenGIS dependencies
import org.opengis.cs.CS_CoordinateSystem;


/**
 * Default implementation of {@link ContextFactory}. This class should not be
 * constructed directly; use {@link #createFactory} instead.
 *
 * @author Cameron Shorter
 * @version $Id: DefaultContextFactory.java,v 1.2 2003/08/20 20:51:16 cholmesny Exp $
 */
public class DefaultContextFactory extends ContextFactory {
    /** Translates between coordinate systems API. */
    private Adapters adapters = Adapters.getDefault();

    /**
     * Create an instance of ContextFactoryImpl.  Note that this constructor
     * should only be called from {@link #createFactory}.
     */
    public DefaultContextFactory() {
    }

    /**
     * {@inheritDoc}
     */
    public BoundingBox createBoundingBox(final Envelope bounds,
        final CS_CoordinateSystem coordinateSystem)
        throws IllegalArgumentException {
        return new DefaultBoundingBox(bounds, coordinateSystem);
    }

    /**
     * {@inheritDoc}
     */
    public Context createContext(final BoundingBox bounds,
        final LayerList layerList, final String title, final String cabstract,
        final String[] keywords, final String contactInformation)
        throws IllegalArgumentException {
        return new DefaultContext(bounds, layerList, title, cabstract,
            keywords, contactInformation);
    }

    /**
     * {@inheritDoc}
     */
    public Context createContext() {
        try {
            CoordinateSystem cs = GeographicCoordinateSystem.WGS84;
            org.geotools.pt.Envelope envelope = cs.getDefaultEnvelope();
            Envelope envelope2 = new Envelope(envelope.getMinimum(0),
                    envelope.getMaximum(0), envelope.getMinimum(1),
                    envelope.getMaximum(1));

            CS_CoordinateSystem cs1 = adapters.export(cs);

            return createContext(createBoundingBox(envelope2, cs1),
                createLayerList(), // empty LayerList
                "", // title
                "", // abstract
                null, // keywords
                ""); // contactInformation
        } catch (java.rmi.RemoteException e) {
            // TODO: We should not eat checked exception.
            throw new java.lang.reflect.UndeclaredThrowableException(e,
                "CS RemoteException.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public Layer createLayer(final FeatureCollection features, final Style style)
        throws IllegalArgumentException {
        return new DefaultLayer(features, style);
    }

    /**
     * {@inheritDoc}
     */
    public LayerList createLayerList() {
        return new DefaultLayerList();
    }
}
