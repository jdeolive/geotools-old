/*
 * Created on Jan 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.validation.spatial;

import java.util.Map;

import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Envelope;

/**
 * PointInsidePolygonValidation purpose.
 * <p>
 * Ensures a Point is inside the Polygon (not on boundary).
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: PointInsidePolygonValidation.java,v 1.1 2004/02/13 03:07:59 jive Exp $
 */
public class PointInsidePolygonValidation
	extends PointPolygonAbstractValidation {

	/**
	 * PointInsidePolygonValidation constructor.
	 * <p>
	 * Description
	 * </p>
	 * 
	 */
	public PointInsidePolygonValidation() {
		super();
	}

	/**
	 * Ensure Point is inside the Polygon (not on boundary).
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
		//TODO Fix Me
		return false;
	}

}
