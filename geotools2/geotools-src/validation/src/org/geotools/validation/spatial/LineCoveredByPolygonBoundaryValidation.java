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

import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;


/**
 * PointCoveredByLineValidation purpose.
 * 
 * <p>
 * Checks to ensure the Line is covered by the Polygon Boundary.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: LineCoveredByPolygonBoundaryValidation.java,v 1.4 2004/02/27 19:44:12 dmzwiers Exp $
 */
public class LineCoveredByPolygonBoundaryValidation
    extends LinePolygonAbstractValidation {
    /**
     * PointCoveredByLineValidation constructor.
     * 
     * <p>
     * Super
     * </p>
     */
    public LineCoveredByPolygonBoundaryValidation() {
        super();
    }

    /**
     * Ensure Line is covered by the Polygon Boundary.
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

    	boolean r = true;
    	
        FeatureSource fsLine = (FeatureSource) layers.get(getLineTypeRef());
        FeatureResults frLine = fsLine.getFeatures();
        FeatureCollection fcLine = frLine.collection();
        FeatureIterator fLine = fcLine.features();
        
        FeatureSource fsPoly = (FeatureSource) layers.get(getRestrictedPolygonTypeRef());
        FeatureResults frPoly = fsPoly.getFeatures();
        FeatureCollection fcPoly = frPoly.collection();
                
        while(fLine.hasNext()){
        	Feature line = fLine.next();
            FeatureIterator fPoly = fcPoly.features();
            Geometry lineGeom = line.getDefaultGeometry();
            if(envelope.contains(lineGeom.getEnvelopeInternal())){
            	// 	check for valid comparison
            	if(LineString.class.isAssignableFrom(lineGeom.getClass())){
            		while(fPoly.hasNext()){
            			Feature poly = fPoly.next();
            			Geometry polyGeom = poly.getDefaultGeometry(); 
                        if(envelope.contains(polyGeom.getEnvelopeInternal())){
                        	if(Polygon.class.isAssignableFrom(polyGeom.getClass())){
                        		Geometry polyGeomBoundary = polyGeom.getBoundary();
                        		if(!polyGeomBoundary.contains(lineGeom)){
                        			results.error(poly,"Boundary does not contain the specified Line.");
                        			r = false;
                        		}
                        		// do next.
                        	}else{
                        		fcPoly.remove(poly);
                        		results.warning(poly,"Invalid type: this feature is not a derivative of a Polygon");
                        	}
                        }else{
                    		fcPoly.remove(poly);
                        }
            		}
            	}else{
            		results.warning(line,"Invalid type: this feature is not a derivative of a LineString");
            	}
            }
        }
        return r;
    }

    /**
     * The priority level used to schedule this Validation.
     *
     * @return PRORITY_SIMPLE
     *
     * @see org.geotools.validation.Validation#getPriority()
     */
    public int getPriority() {
        return PRIORITY_SIMPLE;
    }
}
