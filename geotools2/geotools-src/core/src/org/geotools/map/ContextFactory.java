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

// OpenGIS dependencies
import org.opengis.cs.CS_CoordinateSystem;

// Geotools dependencies
import org.geotools.factory.Factory;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.factory.FactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.styling.Style;


/**
 * A factory to be used to construct {@link Context} classes. Those classes should not
 * be constructed directly.  Instead it should be created from {@link #createFactory},
 * and <code>ContextFactory</code> methods should be called instead.
 *
 * @author Cameron Shorter
 * @version $Id: ContextFactory.java,v 1.15 2003/08/18 16:32:31 desruisseaux Exp $
 */
public abstract class ContextFactory implements Factory {
    /**
     * A cached factory to be returned by createFactory.
     */
    private static ContextFactory factory = null;

    /**
     * Create an instance of the factory.
     *
     * @return An instance of ContextFactory.
     * @throws FactoryConfigurationError for any errors in configuration.
     */
    public static ContextFactory createFactory() throws FactoryConfigurationError {
        if (factory == null) {
            factory = (ContextFactory) FactoryFinder.findFactory(
                    "org.geotools.map.ContextFactory",
                    "org.geotools.map.DefaultContextFactory");
        }
        return factory;
    }

    /**
     * Construct a default factory.
     */
    protected ContextFactory() {
    }

    /**
     * Create a bounding box.
     *
     * @param bounds The extent associated with the bounding box.
     * @param coordinateSystem The coordinate system associated with the bounding box.
     *
     * @return A BoundingBox.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public abstract BoundingBox createBoundingBox(Envelope            bounds,
                                                  CS_CoordinateSystem coordinateSystem)
        throws IllegalArgumentException;

    /**
     * Create a context.
     *
     * @param bounds The extent associated with the context.
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
    public abstract Context createContext(BoundingBox bounds,
                                          LayerList   layerList,
                                          String      title,
                                          String      cabstract,
                                          String[]    keywords,
                                          String      contactInformation)
        throws IllegalArgumentException;

    /**
     * Create a Context with default parameters.<br>
     * <code>boundingBox</code> = null<br>
     * <code>layerList</code> = empty list<br>
     * <code>title</code> = ""<br>
     * <code>abstract</code> = ""<br>
     * <code>keywords</code> = empty array<br>
     * <code>contactInformation</code> = ""
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
