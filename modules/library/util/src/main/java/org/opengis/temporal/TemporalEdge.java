/*$************************************************************************************************
 **
 ** $Id: TemporalEdge.java 982 2007-03-27 10:54:51Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/temporal/TemporalEdge.java $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.temporal;

import org.opengis.annotation.UML;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * One-dimensional topological primitive in time.
 *
 * @author Alexander Petkov
 */
@UML(identifier="TM_Edge", specification=ISO_19108)
public interface TemporalEdge extends TemporalTopologicalPrimitive {
    /**
     * An optional association that links this edge to the corresponding period.
     */
    @UML(identifier="Realization", obligation=OPTIONAL, specification=ISO_19108)
    Period getRealization();

    /**
     * Links this edge to the node that is its start.
     */
    @UML(identifier="start", obligation=MANDATORY, specification=ISO_19108)
    TemporalNode getStart();

    /**
     * Links this edge to the node that is its end.
     */
    @UML(identifier="end", obligation=MANDATORY, specification=ISO_19108)
    TemporalNode getEnd();
}
