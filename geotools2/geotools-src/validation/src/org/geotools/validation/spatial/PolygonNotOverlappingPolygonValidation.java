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
import java.util.logging.Logger;

import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


/**
 * PolygonBoundaryCoveredByPolygonValidation purpose.
 * 
 * <p>
 * Ensures Polygon Boundary is not covered by the Polygon.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: PolygonNotOverlappingPolygonValidation.java,v 1.5 2004/04/22 08:10:39 jive Exp $
 */
public class PolygonNotOverlappingPolygonValidation
    extends PolygonPolygonAbstractValidation {

	private static final Logger LOGGER = Logger.getLogger("org.geotools.validation");
	
    /**
     * PolygonBoundaryCoveredByPolygonValidation constructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public PolygonNotOverlappingPolygonValidation() {
        super();

        // TODO Auto-generated constructor stub
    }

    /**
     * Ensure Polygon Boundary is not covered by the Polygon.
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
    	
    	LOGGER.finer("Starting test "+getName()+" ("+getClass().getName()+")" );    	
        FeatureSource polySource1 = (FeatureSource) layers.get(getPolygonTypeRef());
        FeatureSource polySource2 = (FeatureSource) layers.get(getRestrictedPolygonTypeRef());

        LOGGER.finer("featureType 1:"+polySource1.getSchema().getTypeName() );        
        FeatureResults features1 = polySource1.getFeatures(); // limit with envelope
        FeatureCollection collection1 = features1.collection();
        Object[] poly1 = collection1.toArray();

        LOGGER.finer("featureType 2:"+polySource2.getSchema().getTypeName() );        
        FeatureResults features2 = polySource2.getFeatures(); // limit with envelope
        FeatureCollection collection2 = features2.collection();
        Object[] poly2 = collection1.toArray();
        
        if (!envelope.contains(collection1.getBounds())) {
            results.error((Feature) poly1[0],
                "Polygon Feature Source is not contained within the Envelope provided.");
            return false;
        }

        if (!envelope.contains(collection2.getBounds())) {
            results.error((Feature) poly2[0],
                "Restricted Polygon Feature Source is not contained within the Envelope provided.");
            return false;
        }

        for (int i = 0; i < poly1.length; i++) {
            Feature tmp = (Feature) poly1[i];
            Geometry gt = tmp.getDefaultGeometry();

            for (int j = 0; j < poly2.length; j++) {
                Feature tmp2 = (Feature) poly2[j];
                Geometry gt2 = tmp2.getDefaultGeometry();

                if (gt2.touches(gt)) {
                    return false;
                }
            }
        }

        return true;
    }
}
