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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * PointCoveredByEndPointOfLineValidation purpose.
 * <p>
 * Checks to ensure the Point is covered by an endpoint of the Line.
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: PointCoveredByEndPointOfLineValidation.java,v 1.1 2004/02/13 03:07:59 jive Exp $
 */
public class PointCoveredByEndPointOfLineValidation extends LinePointAbstractValidation {

	/**
	 * PointCoveredByEndPointOfLineValidation constructor.
	 * <p>
	 * Description
	 * </p>
	 * 
	 */
	public PointCoveredByEndPointOfLineValidation() {
		super();
	}

	/**
	 * Ensure Point is covered by a Line end point.
	 * <p>
	 * </p>
	 * @see org.geotools.validation.IntegrityValidation#validate(java.util.Map, com.vividsolutions.jts.geom.Envelope, org.geotools.validation.ValidationResults)
	 * 
	 * @param layers a HashMap of key="TypeName" value="FeatureSource"
	 * @param envelope The bounding box of modified features
	 * @param results Storage for the error and warning messages
	 * @return True if no features intersect. If they do then the validation failed.
	 */
	public boolean validate(Map layers, Envelope envelope, ValidationResults results) throws Exception{
		FeatureSource lineSource = (FeatureSource)layers.get(getLineTypeRef());
		FeatureSource pointSource = (FeatureSource)layers.get(getRestrictedPointTypeRef());
		 
		Object[] points = pointSource.getFeatures().collection().toArray();
		Object[] lines = lineSource.getFeatures().collection().toArray();
		if(!envelope.contains(pointSource.getBounds())){
			results.error((Feature)points[0],"Point Feature Source is not contained within the Envelope provided.");
			return false;
		}
		if(!envelope.contains(lineSource.getBounds())){
			results.error((Feature)lines[0],"Line Feature Source is not contained within the Envelope provided.");
			return false;
		}
		for(int i=0;i<lines.length;i++){
			Feature tmp = (Feature)lines[i];
			Geometry gt = tmp.getDefaultGeometry();
			if(gt instanceof LineString){
				LineString ls = (LineString)gt;
				Point str = ls.getStartPoint();
				Point end = ls.getEndPoint();
				for(int j=0;j<points.length;j++){
					Feature tmp2 = (Feature)points[j];
					Geometry gt2 = tmp2.getDefaultGeometry();
					if(gt2 instanceof Point){
						Point pt = (Point)gt2;
						if(pt.equalsExact(str) || pt.equalsExact(end))
							return true;
					}
				}
			}
		}
		
		return false;
	}

}
