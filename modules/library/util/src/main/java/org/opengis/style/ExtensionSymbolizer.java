/*$************************************************************************************************
 **
 ** $Id: ExtensionSymbolizer.java 1362 2009-03-02 08:53:16Z eclesia $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/style/ExtensionSymbolizer.java $
 **
 ** Copyright (C) 2008 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.style;

import java.util.Map;

import org.opengis.filter.expression.Expression;

/**
 * An symbolizer interface for all unnormalized symbolizers,
 * This interface should be used for vendor specific symbolizers.
 *
 * @author Open Geospatial Consortium
 * @author Johann Sorel (Geomatys)
 * @since GeoAPI 2.3
 */
public interface ExtensionSymbolizer extends Symbolizer {
    
    /**
     * Returns the name of the extension, this name should be commun to all 
     * implementation of a given extension symbolizer sub class.
     * 
     * @return the symbolizer extension name
     */
    String getExtensionName();

    /**
     * Returns a map of all expressions used in this symbolizer. It can be used
     * for analyze purpose but shoudl not be used for XML parsing.
     *
     * @return map of all expressions.
     */
    Map<String,Expression> getParameters();

    /**
     * Calls the visit method of a StyleVisitor
     *
     * @param visitor the style visitor
     */
    Object accept(StyleVisitor visitor, Object extraData);

}
