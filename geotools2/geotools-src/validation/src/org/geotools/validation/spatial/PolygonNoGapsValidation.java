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

import java.util.Map;

import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.validation.DefaultFeatureValidation;
import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;


/**
 * PolygonNoGapsValidation purpose.
 * 
 * <p>
 * Ensures Polygon does not have gaps.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: PolygonNoGapsValidation.java,v 1.4 2004/02/27 23:41:22 dmzwiers Exp $
 */
public class PolygonNoGapsValidation extends DefaultFeatureValidation {
    /**
     * PolygonNoGapsValidation constructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public PolygonNoGapsValidation() {
        super();
    }

    /**
     * Ensure Polygon does not have gaps.
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
        FeatureSource polySource = (FeatureSource) layers.get(getTypeRef());

        Object[] poly1 = polySource.getFeatures().collection().toArray();

        if (!envelope.contains(polySource.getBounds())) {
            results.error((Feature) poly1[0],
                "Polygon Feature Source is not contained within the Envelope provided.");

            return false;
        }

        if(poly1.length>0){
        	Geometry layer = ((Feature)poly1[0]).getDefaultGeometry();
        	for (int i = 1; i < poly1.length; i++) {
            	layer = layer.union(((Feature) poly1[i]).getDefaultGeometry());
        	}
        	if(layer instanceof Polygon){
        		Polygon p = (Polygon)layer;
        		if(p.getNumInteriorRing()!=0){
                	results.error((Feature)poly1[0],"The generated result was had gaps.");
                	return false;
        		}
        		return true;
        	}
        	results.error((Feature)poly1[0],"The generated result was not of type polygon.");
        	return false;
        }

        return true;
    }
}
