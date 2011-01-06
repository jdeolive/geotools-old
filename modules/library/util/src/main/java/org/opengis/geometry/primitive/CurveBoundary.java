/*$************************************************************************************************
 **
 ** $Id: CurveBoundary.java 1128 2007-11-27 19:00:39Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/geometry/primitive/CurveBoundary.java $
 **
 ** Copyright (C) 2003-2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.geometry.primitive;

import org.opengis.annotation.UML;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * The boundary of {@linkplain Curve curves}.
 * A {@code CurveBoundary} contains two {@linkplain Point point} references
 * ({@linkplain #getStartPoint start point} and {@linkplain #getEndPoint end point}).
 *
 * @version <A HREF="http://www.opengeospatial.org/standards/as">ISO 19107</A>
 * @author Martin Desruisseaux (IRD)
 * @since GeoAPI 1.0
 */
@UML(identifier="GM_CurveBoundary", specification=ISO_19107)
public interface CurveBoundary extends PrimitiveBoundary {
    /**
     * Returns the start point.
     *
     * @see #getEndPoint
     */
    @UML(identifier="startPoint", obligation=MANDATORY, specification=ISO_19107)
    Point getStartPoint();

    /**
     * Returns the end point.
     *
     * @see #getStartPoint
     */
    @UML(identifier="endPoint", obligation=MANDATORY, specification=ISO_19107)
    Point getEndPoint();
}
