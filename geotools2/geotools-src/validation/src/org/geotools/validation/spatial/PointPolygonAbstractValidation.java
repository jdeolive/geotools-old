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
 * Basic typeref functionality for a point-polygon validation.
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: PointPolygonAbstractValidation.java,v 1.1 2004/02/13 03:07:59 jive Exp $
 */
public abstract class PointPolygonAbstractValidation  extends DefaultIntegrityValidation {

	private String restrictedPolygonTypeRef;
	private String pointTypeRef;
	
	/**
	 * PointCoveredByLineValidation constructor.
	 * <p>
	 * Super
	 * </p>
	 * 
	 */
	public PointPolygonAbstractValidation() {super();}

	/**
	 * Implementation of getTypeNames. Should be called by sub-classes is being overwritten.
	 *
	 * @return Array of typeNames, or empty array for all, null for disabled
	 *
	 * @see org.geotools.validation.Validation#getTypeNames()
	 */
	public String[] getTypeRefs() {
		if (pointTypeRef == null || restrictedPolygonTypeRef == null) {
			return null;
		}
		return new String[] { pointTypeRef, restrictedPolygonTypeRef };
	}

	/**
	 * Access pointTypeRef property.
	 * 
	 * @return Returns the pointTypeRef.
	 */
	public final String getRestrictedPolygonTypeRef() {
		return restrictedPolygonTypeRef;
	}

	/**
	 * Set pointTypeRef to pointTypeRef.
	 *
	 * @param pointTypeRef The pointTypeRef to set.
	 */
	public final void setRestrictedPolygonTypeRef(String lineTypeRef) {
		this.restrictedPolygonTypeRef = lineTypeRef;
	}

	/**
	 * Access restrictedPolygonTypeRef property.
	 * 
	 * @return Returns the restrictedPolygonTypeRef.
	 */
	public final String getPointTypeRef() {
		return pointTypeRef;
	}

	/**
	 * Set restrictedPolygonTypeRef to restrictedPolygonTypeRef.
	 *
	 * @param restrictedPolygonTypeRef The restrictedPolygonTypeRef to set.
	 */
	public final void setPointTypeRef(String polygonTypeRef) {
		this.pointTypeRef = polygonTypeRef;
	}

}
