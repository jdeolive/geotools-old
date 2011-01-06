/*$************************************************************************************************
 **
 ** $Id: OrdinalReferenceSystem.java 1122 2007-11-24 18:49:16Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/temporal/OrdinalReferenceSystem.java $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.temporal;

import java.util.Collection;
import org.opengis.util.InternationalString;
import org.opengis.annotation.UML;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * Provides only the attributes inherited from temporal reference system.
 *
 * @author Alexander Petkov
 *
 * @todo The javadoc doesn't seem accurate.
 * @todo Missing UML annotations.
 */
public interface OrdinalReferenceSystem extends TemporalReferenceSystem {
    /**
     * Get the set of ordinal eras of which this ordinal reference system consists of.
     *
     * @return A hierarchically-structured collection of ordinal eras.
     *
     * @todo What the structure is exactly?
     */
    @UML(identifier="structure", obligation=MANDATORY,specification=ISO_19108)
    Collection<OrdinalEra> getOrdinalEraSequence();
}
