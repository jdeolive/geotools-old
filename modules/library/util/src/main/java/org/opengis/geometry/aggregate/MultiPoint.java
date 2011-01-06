/*$************************************************************************************************
 **
 ** $Id: MultiPoint.java 1128 2007-11-27 19:00:39Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/geometry/aggregate/MultiPoint.java $
 **
 ** Copyright (C) 2003-2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
// This class was modified by Sanjay Jena and Prof. Jackson Roehrig in order to complete
// missing parts of the GeoAPI and submit the results to GeoAPI afterwards as proposal.

package org.opengis.geometry.aggregate;

import java.util.Set;
import org.opengis.geometry.primitive.Point;
import org.opengis.annotation.UML;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * An aggregate class containing only instances of {@link Point}.
 * The association role {@link #getElements element} shall be the set of
 * {@linkplain Point points} contained in this {@code MultiPoint}.
 *
 * @version <A HREF="http://www.opengeospatial.org/standards/as">ISO 19107</A>
 * @since GeoAPI 1.0
 */
public interface MultiPoint extends MultiPrimitive {
    /**
     * Returns the set containing the elements that compose this {@code MultiPoint}.
     * The set may be modified if this geometry {@linkplain #isMutable is mutable}.
     *
     * @since GeoAPI 2.1
     */
    @UML(identifier="element", obligation=MANDATORY, specification=ISO_19107)
    Set<Point> getElements();

//    public java.util.Vector /*DirectPosition*/ position;
//    public void setPosition(java.util.Vector /*DirectPosition*/ position) {  }
//    public java.util.Vector /*DirectPosition*/ getPosition() { return null; }
}