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

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.factory.Factory;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.factory.FactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.styling.Style;
import org.opengis.cs.CS_CoordinateSystem;


/**
 * An implementation of ContextFactory to be used to construct Context classes.
 * It should not be called directly.  Instead it should be created from
 * ContextFactory, and ContextFactory methods should be called instead.
 *
 * @version $Id: ContextFactory.java,v 1.14 2003/08/07 22:11:58 cholmesny Exp $
 */
public abstract class ContextFactory implements Factory {
    /** A cached factory to be returned by createFactory. */
    private static ContextFactory factory = null;

    /**
     * Create an instance of the factory.
     *
     * @return An instance of ContextFactory, or null if ContextFactory could
     *         not be created.
     *
     * @throws FactoryConfigurationError for any errors in configuration.
     */
    public static ContextFactory createFactory()
        throws FactoryConfigurationError {
        if (factory == null) {
            factory = (ContextFactory) FactoryFinder.findFactory(
                    "org.geotools.map.ContextFactory",
                    "org.geotools.map.ContextFactoryImpl");
        }

        return factory;
    }

    /**
     * Create a BoundingBox.
     *
     * @param bbox The extent associated with this class.
     * @param coordinateSystem The coordinate system associated with this
     *        class.
     *
     * @return A BoundingBox.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public abstract BoundingBox createBoundingBox(Envelope bbox,
        CS_CoordinateSystem coordinateSystem) throws IllegalArgumentException;

    /**
     * Create a Context.
     *
     * @param bbox The extent associated with this class.
     * @param layerList The list of layers associated with this context.
     * @param title The name of this context.  Must be set.
     * @param cabstract A description of this context.  Optional, set to null
     *        if none exists.
     * @param keywords An array of keywords to be used when searching for this
     *        context.  Optional, set to null if none exists.
     * @param contactInformation Contact details for the person who created
     *        this context.  Optional, set to null if none exists.
     *
     * @return A Context class.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public abstract Context createContext(BoundingBox bbox,
        LayerList layerList, String title, String cabstract, String[] keywords,
        String contactInformation) throws IllegalArgumentException;

    /**
     * Create a Context with default parameters.<br>
     * boundingBox = layerList = empty list <br>
     * title = "" <br>
     * _abstract = ""<br>
     * keywords = empty array<br>
     * contactInformation = ""
     *
     * @return A default Context class.
     */
    public abstract Context createContext();

    /**
     * Creates a Layer.
     *
     * @param features the features for this layer.
     * @param style The style to use when rendering features associated with
     *        this layer.
     *
     * @return A Layer.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public abstract Layer createLayer(FeatureCollection features, Style style)
        throws IllegalArgumentException;

    /**
     * Create a LayerList.
     *
     * @return An empty LayerList.
     */
    public abstract LayerList createLayerList();
}
