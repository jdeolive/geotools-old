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
 * Basic typeref functionality for a point-line validation.
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: PointLineAbstractValidation.java,v 1.1 2004/02/13 03:07:59 jive Exp $
 */
public abstract class PointLineAbstractValidation  extends DefaultIntegrityValidation {

	private String restrictedLineTypeRef;
	private String pointTypeRef;
	
	/**
	 * PointCoveredByLineValidation constructor.
	 * <p>
	 * Super
	 * </p>
	 * 
	 */
	public PointLineAbstractValidation() {super();}

	/**
	 * Implementation of getTypeNames. Should be called by sub-classes is being overwritten.
	 *
	 * @return Array of typeNames, or empty array for all, null for disabled
	 *
	 * @see org.geotools.validation.Validation#getTypeNames()
	 */
	public String[] getTypeRefs() {
		if (pointTypeRef == null || restrictedLineTypeRef == null) {
			return null;
		}
		return new String[] { pointTypeRef, restrictedLineTypeRef };
	}

	/**
	 * Access pointTypeRef property.
	 * 
	 * @return Returns the pointTypeRef.
	 */
	public final String getRestrictedLineTypeRef() {
		return restrictedLineTypeRef;
	}

	/**
	 * Set pointTypeRef to pointTypeRef.
	 *
	 * @param pointTypeRef The pointTypeRef to set.
	 */
	public final void setRestrictedLineTypeRef(String lineTypeRef) {
		this.restrictedLineTypeRef = lineTypeRef;
	}

	/**
	 * Access restrictedLineTypeRef property.
	 * 
	 * @return Returns the restrictedLineTypeRef.
	 */
	public final String getPointTypeRef() {
		return pointTypeRef;
	}

	/**
	 * Set restrictedLineTypeRef to restrictedLineTypeRef.
	 *
	 * @param restrictedLineTypeRef The restrictedLineTypeRef to set.
	 */
	public final void setPointTypeRef(String polygonTypeRef) {
		this.pointTypeRef = polygonTypeRef;
	}

}
