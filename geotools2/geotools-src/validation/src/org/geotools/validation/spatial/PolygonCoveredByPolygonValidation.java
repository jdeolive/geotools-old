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

/**
 * PolygonCoveredByPolygonValidation purpose.
 * <p>
 * Checks to ensure the Polygon is covered by the Polygon.
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: PolygonCoveredByPolygonValidation.java,v 1.1 2004/02/13 03:07:59 jive Exp $
 */
public class PolygonCoveredByPolygonValidation  extends PolygonPolygonAbstractValidation {
	
	/**
	 * PointCoveredByLineValidation constructor.
	 * <p>
	 * Super
	 * </p>
	 * 
	 */
	public PolygonCoveredByPolygonValidation() {super();}

	/**
	 * Ensure Polygon is covered by the Polygon.
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
		FeatureSource polySource1 = (FeatureSource)layers.get(getPolygonTypeRef());
		FeatureSource polySource2 = (FeatureSource)layers.get(getRestrictedPolygonTypeRef());
		
		Object[] poly1 = polySource1.getFeatures().collection().toArray();
		Object[] poly2 = polySource2.getFeatures().collection().toArray();
		if(!envelope.contains(polySource1.getBounds())){
			results.error((Feature)poly1[0],"Polygon Feature Source is not contained within the Envelope provided.");
			return false;
		}
		if(!envelope.contains(polySource2.getBounds())){
			results.error((Feature)poly1[0],"Restricted Polygon Feature Source is not contained within the Envelope provided.");
			return false;
		}
		for(int i=0;i<poly2.length;i++){
			Feature tmp = (Feature)poly2[i];
			Geometry gt = tmp.getDefaultGeometry();
			for(int j=0;j<poly1.length;j++){
				Feature tmp2 = (Feature)poly1[j];
				Geometry gt2 = tmp2.getDefaultGeometry();
				if(gt2.within(gt)){
					return true;
				}
				
			}
		}
		
		return false;
	}
}
