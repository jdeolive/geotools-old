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
 * PolygonNotOverlappingLineValidation purpose.
 * <p>
 * Checks that the line is not touching the interior of the polygon.
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: LineNotTouchingPolygonInteriorValidation.java,v 1.1 2004/02/13 03:07:59 jive Exp $
 */
public class LineNotTouchingPolygonInteriorValidation
	extends LinePolygonAbstractValidation {

	/**
	 * PolygonNotOverlappingLineValidation constructor.
	 * <p>
	 * Description
	 * </p>
	 * 
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
	 */
	public boolean validate(Map layers, Envelope envelope, ValidationResults results) throws Exception {
		results.warning( null, "Validation not yet implemented" );        
		return false;
	}

}
