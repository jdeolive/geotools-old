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
import com.vividsolutions.jts.geom.Point;


/**
 * PointCoveredByLineValidation purpose.
 * 
 * <p>
 * Checks to ensure the Line End Point is covered by the Line.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: LineEndPointCoveredByLineValidation.java,v 1.4 2004/02/27 19:44:12 dmzwiers Exp $
 */
public class LineEndPointCoveredByLineValidation
    extends LineLineAbstractValidation {
    /**
     * PointCoveredByLineValidation constructor.
     * 
     * <p>
     * Super
     * </p>
     */
    public LineEndPointCoveredByLineValidation() {
        super();
    }

    /**
     * Ensure Line End Point is covered by the Line.
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
        
        FeatureSource fsRLine = (FeatureSource) layers.get(getRestrictedLineTypeRef());
        FeatureResults frRLine = fsRLine.getFeatures();
        FeatureCollection fcRLine = frRLine.collection();
                
        while(fLine.hasNext()){
        	Feature line = fLine.next();
        	FeatureIterator fRLine = fcRLine.features();
        	Geometry lineGeom = line.getDefaultGeometry();
        	if(envelope.contains(lineGeom.getEnvelopeInternal())){
        		// 	check for valid comparison
        		if(LineString.class.isAssignableFrom(lineGeom.getClass())){
        			while(fRLine.hasNext()){
        				Feature rLine = fRLine.next();
        				Geometry rLineGeom = rLine.getDefaultGeometry(); 
        				if(envelope.contains(rLineGeom.getEnvelopeInternal())){
        					if(LineString.class.isAssignableFrom(rLineGeom.getClass())){
        						Point p1 = ((LineString)rLineGeom).getEndPoint();
        						//Point p2 = ((LineString)rLineGeom).getStartPoint(); //include this?
        						if(!lineGeom.contains(p1)){
            					//if(!(lineGeom.contains(p1) || lineGeom.contains(p2))){
        							results.error(rLine,"Line End Point not covered by the specified Line.");
        							r = false;
        						}
                    		// do next.
        					}else{
        						fcRLine.remove(rLine);
        						results.warning(rLine,"Invalid type: this feature is not a derivative of a LineString");
        					}
        				}else{
        					fcRLine.remove(rLine);
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
