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
/*
 * Created on Jan 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.validation.spatial;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.validation.ValidationResults;
import java.util.Map;


/**
 * PointInsidePolygonValidation purpose.
 * 
 * <p>
 * Ensures a Point is inside the Polygon (not on boundary).
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: PointInsidePolygonValidation.java,v 1.2 2004/02/17 17:19:13 dmzwiers Exp $
 */
public class PointInsidePolygonValidation extends PointPolygonAbstractValidation {
    /**
     * PointInsidePolygonValidation constructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public PointInsidePolygonValidation() {
        super();
    }

    /**
     * Ensure Point is inside the Polygon (not on boundary).
     * 
     * <p></p>
     *
     * @param layers a HashMap of key="TypeName" value="FeatureSource"
     * @param envelope The bounding box of modified features
     * @param results Storage for the error and warning messages
     *
     * @return True if no features intersect. If they do then the validation
     *         failed.
     *
     * @throws Exception DOCUMENT ME!
     *
     * @see org.geotools.validation.IntegrityValidation#validate(java.util.Map,
     *      com.vividsolutions.jts.geom.Envelope,
     *      org.geotools.validation.ValidationResults)
     */
    public boolean validate(Map layers, Envelope envelope,
        ValidationResults results) throws Exception {
        //TODO Fix Me
        return false;
    }
}
