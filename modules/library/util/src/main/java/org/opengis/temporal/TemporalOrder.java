/*$************************************************************************************************
 **
 ** $Id: TemporalOrder.java 1019 2007-04-24 15:21:07Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/temporal/TemporalOrder.java $
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
 * Provides an operation for determining the position of this {@linkplain TemporalPrimitive
 * temporal primitive} relative to another {@linkplain TemporalPrimitive temporal primitive}.
 *
 * @author Stephane Fellah (Image Matters)
 * @author Alexander Petkov
 *
 * @todo The javadoc suggests that this interface should extends some kind of
 *       {@link TemporalPrimitive}.
 */
@UML(identifier="TM_Order", specification=ISO_19108)
public interface TemporalOrder {
    /**
     * Determines the position of this primitive relative to another primitive.
     */
    @UML(identifier="relativePosition", obligation=MANDATORY, specification=ISO_19108)
    RelativePosition relativePosition(TemporalPrimitive other);
}
