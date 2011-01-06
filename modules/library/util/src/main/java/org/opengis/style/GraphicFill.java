/*$************************************************************************************************
 **
 ** $Id: GraphicFill.java 1391 2009-05-09 13:24:19Z Jive $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/style/GraphicFill.java $
 **
 ** Copyright (C) 2008 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.style;

import org.opengis.annotation.Extension;
import org.opengis.annotation.XmlElement;

/**
 * A GraphicFill is a simple interface with only a graphic but
 * additional parameters for the GraphicFill may be provided in the
 * future to provide more control the exact style of filling.
 * We keep this class to reduce future code changes.
 *
 *
 * @version <A HREF="http://www.opengeospatial.org/standards/symbol">Symbology Encoding Implementation Specification 1.1.0</A>
 * @author Open Geospatial Consortium
 * @author Johann Sorel (Geomatys)
 * @since GeoAPI 2.2
 */
@XmlElement("GraphicFill")
public interface GraphicFill extends Graphic{
    
    /**
     * Calls the visit method of a StyleVisitor
     *
     * @param visitor the style visitor
     */
    @Extension
    Object accept(StyleVisitor visitor, Object extraData);
    
}