/*
 * Copyright (C) 2003 Open GIS Consortium, Inc. All Rights Reserved. http://www.opengis.org/Legal/
 */
package org.opengis.sc;

// OpenGIS dependencies
// import org.opengis.cs.CoordinateSystem;
// import org.opengis.cd.Datum


/**
 * Abstract coordinate reference system, consisting of a single
 * {@linkplain CoordinateSystem Coordinate System} and a single
 * {@linkplain Datum Datum} (as opposed to {@linkplain CompoundCRS Compound CRS}).
 *
 * A coordinate reference system consists of an ordered sequence of coordinate system
 * axes that are related to the earth through a datum. A coordinate reference system
 * is defined by one datum and by one coordinate system. Most coordinate reference system
 * do not move relative to the earth, except for engineering coordinate reference systems
 * defined on moving platforms such as cars, ships, aircraft, and spacecraft.
 *
 * Coordinate reference systems are commonly divided into sub-types. The common classification
 * criterion for sub-typing of coordinate reference systems is the way in which they deal with
 * earth curvature. This has a direct effect on the portion of the earth's surface that can be
 * covered by that type of CRS with an acceptable degree of error. The exception to the rule is
 * the subtype "Temporal" which has been added by analogy.
 *  
 * @author <A HREF="http://www.opengis.org">OpenGIS&reg;</A>
 * @version 2.0
 */
public interface CoordinateReferenceSystem extends CRS {
    /**
     * Returns the coordinate system.
     *
     * @return The coordinate system.
     * @task REVISIT: Why not 'getCoordinateSystem()'?
     */
//    public CoordinateSystem getUsesCS();

    /**
     * Returns the datum.
     *
     * @return The datum.
     * @task REVISIT: Why not 'getDatum()'?
     */
//    public Datum getUsesDatum();
}
