/*
 * Created on Jan 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.validation.spatial;

import org.geotools.validation.DefaultIntegrityValidation;

/**
 * PointCoveredByLineValidation purpose.
 * <p>
 * Basic typeref information for a 2 polygon validation.
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: PolygonPolygonAbstractValidation.java,v 1.1 2004/02/13 03:07:59 jive Exp $
 */
public abstract class PolygonPolygonAbstractValidation  extends DefaultIntegrityValidation {

	private String restrictedPolygonTypeRef;
	private String polygonTypeRef;
	
	/**
	 * PointCoveredByLineValidation constructor.
	 * <p>
	 * Super
	 * </p>
	 * 
	 */
	public PolygonPolygonAbstractValidation() {super();}

	/**
	 * Implementation of getTypeNames. Should be called by sub-classes is being overwritten.
	 *
	 * @return Array of typeNames, or empty array for all, null for disabled
	 *
	 * @see org.geotools.validation.Validation#getTypeNames()
	 */
	public String[] getTypeRefs() {
		if (polygonTypeRef == null || restrictedPolygonTypeRef == null) {
			return null;
		}
		return new String[] { polygonTypeRef, restrictedPolygonTypeRef };
	}

	/**
	 * Access polygonTypeRef property.
	 * 
	 * @return Returns the polygonTypeRef.
	 */
	public final String getPolygonTypeRef() {
		return polygonTypeRef;
	}

	/**
	 * Set polygonTypeRef to polygonTypeRef.
	 *
	 * @param polygonTypeRef The polygonTypeRef to set.
	 */
	public final void setPolygonTypeRef(String lineTypeRef) {
		this.polygonTypeRef = lineTypeRef;
	}

	/**
	 * Access restrictedPolygonTypeRef property.
	 * 
	 * @return Returns the restrictedPolygonTypeRef.
	 */
	public final String getRestrictedPolygonTypeRef() {
		return restrictedPolygonTypeRef;
	}

	/**
	 * Set restrictedPolygonTypeRef to restrictedPolygonTypeRef.
	 *
	 * @param restrictedPolygonTypeRef The restrictedPolygonTypeRef to set.
	 */
	public final void setRestrictedPolygonTypeRef(String pointTypeRef) {
		this.restrictedPolygonTypeRef = pointTypeRef;
	}

}
