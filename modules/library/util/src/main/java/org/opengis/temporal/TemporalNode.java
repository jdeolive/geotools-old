/*$************************************************************************************************
 **
 ** $Id: TemporalNode.java 1122 2007-11-24 18:49:16Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/temporal/TemporalNode.java $
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
 * A zero dimensional topological primitive in time.
 *
 * @author Alexander Petkov
 */
@UML(identifier="TM_Node", specification=ISO_19108)
public interface TemporalNode extends TemporalTopologicalPrimitive {
    /**
     * An optional association that may link this temporal node
     * to its corresponding instant.
     */
    @UML(identifier="Realization", obligation=OPTIONAL, specification=ISO_19108)
    Instant getRealization();

    /**
     * Links this temporal node to the previous temporal edge.
     */
    @UML(identifier="previousEdge", obligation=MANDATORY, specification=ISO_19108)
    TemporalEdge getPreviousEdge();

    /**
     * Links this temporal node to the next temporal edge.
     */
    @UML(identifier="nextEdge", obligation=MANDATORY, specification=ISO_19108)
    TemporalEdge getNextEdge();
}
