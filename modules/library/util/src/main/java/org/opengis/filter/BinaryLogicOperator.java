/*$************************************************************************************************
 **
 ** $Id: BinaryLogicOperator.java 946 2006-12-20 05:27:27Z jdeolive $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/filter/BinaryLogicOperator.java $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.filter;

// J2SE direct dependencies
import java.util.List;

import org.opengis.annotation.XmlElement;


/**
 * Abstract super-interface for logical operators that accept two or more
 * other logical values as inputs.  Currently, the only two subclasses are
 * {@link And} and {@link Or}.
 *
 * @version <A HREF="http://www.opengis.org/docs/02-059.pdf">Implementation specification 1.0</A>
 * @author Chris Dillard (SYS Technologies)
 * @since GeoAPI 2.0
 */
@XmlElement("BinaryLogicOpType")
public interface BinaryLogicOperator extends Filter {
    /**
     * Returns a list containing all of the child filters of this object.
     * <p>
     * This list will contain at least two elements, and each element will be an
     * instance of {@code Filter}.
     * </p>
     */
    List<Filter> getChildren();
}
