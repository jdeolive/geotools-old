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
 * Base typeRef functionality for a 2 line validation.
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: LineLineAbstractValidation.java,v 1.1 2004/02/13 03:07:59 jive Exp $
 */
public abstract class LineLineAbstractValidation  extends DefaultIntegrityValidation {

	private String restrictedLineTypeRef;
	private String lineTypeRef;
	
	/**
	 * PointCoveredByLineValidation constructor.
	 * <p>
	 * Super
	 * </p>
	 * 
	 */
	public LineLineAbstractValidation() {super();}

	/**
	 * Implementation of getTypeNames. Should be called by sub-classes is being overwritten.
	 *
	 * @return Array of typeNames, or empty array for all, null for disabled
	 *
	 * @see org.geotools.validation.Validation#getTypeNames()
	 */
	public String[] getTypeRefs() {
		if (lineTypeRef == null || restrictedLineTypeRef == null) {
			return null;
		}
		return new String[] { lineTypeRef, restrictedLineTypeRef };
	}

	/**
	 * Access lineTypeRef property.
	 * 
	 * @return Returns the lineTypeRef.
	 */
	public final String getLineTypeRef() {
		return lineTypeRef;
	}

	/**
	 * Set lineTypeRef to lineTypeRef.
	 *
	 * @param lineTypeRef The lineTypeRef to set.
	 */
	public final void setLineTypeRef(String lineTypeRef) {
		this.lineTypeRef = lineTypeRef;
	}

	/**
	 * Access restrictedLineTypeRef property.
	 * 
	 * @return Returns the restrictedLineTypeRef.
	 */
	public final String getRestrictedLineTypeRef() {
		return restrictedLineTypeRef;
	}

	/**
	 * Set restrictedLineTypeRef to restrictedLineTypeRef.
	 *
	 * @param restrictedLineTypeRef The restrictedLineTypeRef to set.
	 */
	public final void setRestrictedLineTypeRef(String pointTypeRef) {
		this.restrictedLineTypeRef = pointTypeRef;
	}

}
