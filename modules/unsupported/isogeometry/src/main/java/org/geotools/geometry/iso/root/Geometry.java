/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.geometry;

// J2SE dependencies
import java.io.Serializable;
import java.util.NoSuchElementException;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.Cloneable;

// Geotools dependencies
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.operation.TransformPathNotFoundException;


/**
 * Root class of the Geotools default implementation of geometric object. {@code Geometry}
 * instances are sets of direct positions in a particular coordinate reference system.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class Geometry implements org.opengis.geometry.Geometry, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -601532429079649232L;

    /**
     * The default {@link CoordinateOperationFactory} to uses for {@link #mathTransform}.
     * Will be constructed only when first requested.
     */
    private static CoordinateOperationFactory coordinateOperationFactory;

    /**
     * The coordinate reference system used in {@linkplain GeneralDirectPosition direct position}
     * coordinates.
     */
    protected final CoordinateReferenceSystem crs;

    /**
     * Constructs a geometry with the specified coordinate reference system.
     *
     * @param crs The coordinate reference system used in
     *            {@linkplain GeneralDirectPosition direct position} coordinates.
     */
    public Geometry(final CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    /**
     * Returns the coordinate reference system used in {@linkplain GeneralDirectPosition
     * direct position} coordinates.
     *
     * @return The coordinate reference system used in {@linkplain GeneralDirectPosition
     *         direct position} coordinates.
     *
     * @see #getCoordinateDimension
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    /**
     * Returns the dimension of the coordinates that define this {@code Geometry}, which must
     * be the same as the coordinate dimension of the coordinate reference system for this
     * {@code Geometry}.
     *
     * @return The coordinate dimension.
     *
     * @see #getDimension
     * @see #getCoordinateReferenceSystem
     */
    public int getCoordinateDimension() {
        return crs.getCoordinateSystem().getDimension();
    }

    /**
     * Returns a new {@code Geometry} that is the coordinate transformation of this
     * {@code Geometry} into the passed coordinate reference system within the accuracy
     * of the transformation.
     *
     * @param  newCRS The new coordinate reference system.
     * @return The transformed {@code Geometry}.
     * @throws TransformException if the transformation failed.
     */
    public org.opengis.geometry.Geometry
            transform(CoordinateReferenceSystem newCRS) throws TransformException
    {
        if (coordinateOperationFactory == null) {
            // No need to synchronize: this is not a problem if this method is invoked
            // twice in two different threads.
            try {
                coordinateOperationFactory = ReferencingFactoryFinder.getCoordinateOperationFactory(null);
            } catch (NoSuchElementException exception) {
                // TODO: localize the message
                throw new TransformException("Can't transform the geometry", exception);
            }
        }
        try {
            return transform(newCRS,
                   coordinateOperationFactory.createOperation(crs, newCRS).getMathTransform());
        } catch (FactoryException exception) {
            // TODO: localize the message
            throw new TransformPathNotFoundException("Can't transform the geometry", exception);
        }
    }

    /**
     * Returns a clone of this geometry with <em>deep</em> copy semantic. Any change on this object
     * will have no impact on the returned clone, and conversely. For big geometries, implementations
     * are encouraged to share as much internal data as possible (as opposed to performing a real
     * copy of the data), while preserving the deep copy semantic.
     * <P>
     * The default implementation throws {@link CloneNotSupportedException}. If subclasses
     * implements {@link Cloneable}, then they should overrides this method.
     *
     * @throws CloneNotSupportedException if this object do not support clone. This exception is
     *         never throws if this object implements {@link Cloneable}.
     */
    @Override
    public Geometry clone() throws CloneNotSupportedException {
        return (Geometry) super.clone();
    }
}
