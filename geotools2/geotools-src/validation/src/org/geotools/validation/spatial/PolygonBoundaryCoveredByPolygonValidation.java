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
import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;


/**
 * PolygonBoundaryCoveredByPolygonValidation purpose.
 * 
 * <p>
 * Ensures Polygon Boundary is covered by the Polygon.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: PolygonBoundaryCoveredByPolygonValidation.java,v 1.4 2004/02/27 21:28:50 dmzwiers Exp $
 */
public class PolygonBoundaryCoveredByPolygonValidation
    extends PolygonPolygonAbstractValidation {
    /**
     * PolygonBoundaryCoveredByPolygonValidation constructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public PolygonBoundaryCoveredByPolygonValidation() {
        super();

        // TODO Auto-generated constructor stub
    }

    /**
     * Ensure Polygon Boundary is covered by the Polygon.
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
        FeatureSource polySource = (FeatureSource) layers.get(getPolygonTypeRef());
        FeatureSource polyrSource = (FeatureSource) layers.get(getRestrictedPolygonTypeRef());

        Object[] polys = polyrSource.getFeatures().collection().toArray();
        Object[] polyRs = polySource.getFeatures().collection().toArray();

        if (!envelope.contains(polySource.getBounds())) {
            results.error((Feature) polys[0],
                "Poly Feature Source is not contained within the Envelope provided.");

            return false;
        }

        if (!envelope.contains(polyrSource.getBounds())) {
            results.error((Feature) polyRs[0],
                "Poly Feature Source is not contained within the Envelope provided.");

            return false;
        }

        for (int i = 0; i < polys.length; i++) {
            Feature tmp = (Feature) polys[i];
            Geometry gt = tmp.getDefaultGeometry();

            if (gt instanceof Polygon) {
            	Polygon ls = (Polygon) gt;

                boolean r = false;
                for (int j = 0; j < polyRs.length && !r; j++) {
                    Feature tmp2 = (Feature) polyRs[j];
                    Geometry gt2 = tmp2.getDefaultGeometry();

                    if (gt2 instanceof Polygon) {
                    	Polygon pt = (Polygon) gt2;
                        if(!pt.getBoundary().within(ls)){
                        	r = true;
                        }
                    }
                }
                if(!r){
                    results.error(tmp, "Polygon does not contained one of the specified polygons.");
                	return false;
                }
            }
        }

        return true;
    }
}
