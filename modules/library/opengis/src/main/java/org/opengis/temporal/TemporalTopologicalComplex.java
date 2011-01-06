/*$************************************************************************************************
 **
 ** $Id: TemporalTopologicalComplex.java 1122 2007-11-24 18:49:16Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/temporal/TemporalTopologicalComplex.java $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.temporal;

import java.util.Collection;
import org.opengis.annotation.UML;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * An aggregation of connected {@linkplain TemporalTopologicalPrimitive temporal topological
 * primitives}. This is the only subclass of {@linkplain TemporalComplex temporal complex}.
 *
 * @author Alexander Petkov
 */
@UML(identifier="TM_TopologicalComplex", specification=ISO_19108)
public interface TemporalTopologicalComplex extends TemporalComplex {
    /**
     * The aggregation of connected {@linkplain TemporalTopologicalPrimitive temporal topological
     * primitives}.
     *
     * @todo Missing UML annotation.
     */
    Collection<TemporalTopologicalPrimitive> getTemporalTopologicalPrimitives();
}
