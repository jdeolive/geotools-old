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
 * PolygonNotOverlappingLineValidation purpose.
 * 
 * <p>
 * Checks that the line is not touching the interior of the polygon.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: LineNotTouchingPolygonInteriorValidation.java,v 1.4 2004/02/27 19:44:12 dmzwiers Exp $
 */
public class LineNotTouchingPolygonInteriorValidation
    extends LinePolygonAbstractValidation {
    /**
     * PolygonNotOverlappingLineValidation constructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public LineNotTouchingPolygonInteriorValidation() {
        super();
    }

    /**
     * Check that the line is not touching the interior of the polygon.
     *
     * @param layers Map of FeatureSource by "dataStoreID:typeName"
     * @param envelope The bounding box that encloses the unvalidated data
     * @param results Used to coallate results information
     *
     * @return <code>true</code> if all the features pass this test.
     *
     * @throws Exception DOCUMENT ME!
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
        						Polygon p = (Polygon)polyGeom;
        						for(int i=0;i<p.getNumInteriorRing();i++){
        							if(!p.getInteriorRingN(i).touches(lineGeom)){
        								results.error(poly,"Polygon interior touches the specified Line.");
        							}
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
}
