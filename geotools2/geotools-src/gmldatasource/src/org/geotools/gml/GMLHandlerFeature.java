/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.gml;

import com.vividsolutions.jts.geom.*;
import org.geotools.data.*;
import org.geotools.feature.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.*;


/**
 * LEVEL4 saxGML4j GML handler: Gets features.
 * 
 * <p>
 * This handler must be implemented by the parent of a GMLFilterFeature filter
 * in order to handle the features passed to it from the child.
 * </p>
 *
 * @author Rob Hranac, Vision for New York
 * @version $Id: GMLHandlerFeature.java,v 1.6 2003/08/14 18:36:47 cholmesny Exp $
 */
public interface GMLHandlerFeature extends ContentHandler {
    /**
     * Receives OGC simple feature from parent.
     */
    void feature(Feature feature);
}
