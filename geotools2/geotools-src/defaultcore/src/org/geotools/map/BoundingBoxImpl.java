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
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.MathTransform;


/**
 * Legacy implementation of {@link BoundingBox}
 *
 * @author Cameron Shorter
 * @version $Id: BoundingBoxImpl.java,v 1.15 2003/08/18 16:33:06 desruisseaux Exp $
 *
 * @deprecated Use {@link DefaultBoundingBox} instead.
 */
public class BoundingBoxImpl extends DefaultBoundingBox {
    /**
     * Construct a bounding box with the given area of interest and coordinate system.
     *
     * @param areaOfInterest The extent associated with this class.
     * @param coordinateSystem The coordinate system associated with this
     *        class.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    protected BoundingBoxImpl(final Envelope         areaOfInterest,
                              final CoordinateSystem coordinateSystem)
            throws IllegalArgumentException
    {
        super(areaOfInterest, coordinateSystem);
    }

    /**
     * Construct a bounding box with the given area of interest and coordinate system.
     *
     * @param areaOfInterest The extent associated with this class.
     * @param coordinateSystem The coordinate system associated with this class.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    protected BoundingBoxImpl(final Envelope            areaOfInterest,
                              final CS_CoordinateSystem coordinateSystem)
            throws IllegalArgumentException
    {
        super(areaOfInterest, coordinateSystem);
    }


    /**
     * Notify all listeners that have registered interest for changes in
     * the area of tnterest.
     *
     * @param transform The transform that has been applied to the area of interest.
     *
     * @deprecated Invoke {@link #fireAreaOfInterestChanged} instead.
     */
    protected void fireAreaOfInterestChangedListener(final MathTransform transform) {
        fireAreaOfInterestChanged((MathTransform2D) transform);
    }
}
