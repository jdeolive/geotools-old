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
 * @version $Id: LinePointAbstractValidation.java,v 1.1 2004/02/13 03:07:59 jive Exp $
 */
public abstract class LinePointAbstractValidation  extends DefaultIntegrityValidation {

	private String lineTypeRef;
	private String restrictedPointTypeRef;
	
	/**
	 * PointCoveredByLineValidation constructor.
	 * <p>
	 * Super
	 * </p>
	 * 
	 */
	public LinePointAbstractValidation() {super();}

	/**
	 * Implementation of getTypeNames. Should be called by sub-classes is being overwritten.
	 *
	 * @return Array of typeNames, or empty array for all, null for disabled
	 *
	 * @see org.geotools.validation.Validation#getTypeNames()
	 */
	public String[] getTypeRefs() {
		if (restrictedPointTypeRef == null || lineTypeRef == null) {
			return null;
		}
		return new String[] { restrictedPointTypeRef, lineTypeRef };
	}

	/**
	 * Access restrictedPointTypeRef property.
	 * 
	 * @return Returns the restrictedPointTypeRef.
	 */
	public final String getLineTypeRef() {
		return lineTypeRef;
	}

	/**
	 * Set restrictedPointTypeRef to restrictedPointTypeRef.
	 *
	 * @param restrictedPointTypeRef The restrictedPointTypeRef to set.
	 */
	public final void setLineTypeRef(String lineTypeRef) {
		this.lineTypeRef = lineTypeRef;
	}

	/**
	 * Access lineTypeRef property.
	 * 
	 * @return Returns the lineTypeRef.
	 */
	public final String getRestrictedPointTypeRef() {
		return restrictedPointTypeRef;
	}

	/**
	 * Set lineTypeRef to lineTypeRef.
	 *
	 * @param lineTypeRef The lineTypeRef to set.
	 */
	public final void setRestrictedPointTypeRef(String polygonTypeRef) {
		this.restrictedPointTypeRef = polygonTypeRef;
	}

}
