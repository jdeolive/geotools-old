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
 * PointCoveredByLineValidation purpose.
 * <p>
 * Checks to ensure the Line is covered by the Polygon Boundary.
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: LineCoveredByPolygonBoundaryValidation.java,v 1.1 2004/02/13 03:07:59 jive Exp $
 */
public class LineCoveredByPolygonBoundaryValidation extends LinePolygonAbstractValidation {
	
	/**
	 * PointCoveredByLineValidation constructor.
	 * <p>
	 * Super
	 * </p>
	 * 
	 */
	public LineCoveredByPolygonBoundaryValidation() {super();}

	/**
	 * Ensure Line is covered by the Polygon Boundary.
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
