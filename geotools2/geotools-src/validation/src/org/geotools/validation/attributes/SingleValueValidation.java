/*
 * Created on Jan 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.validation.attributes;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.validation.DefaultFeatureValidation;
import org.geotools.validation.ValidationResults;

/**
 * SingleValueValidation purpose.
 * <p>
 * Checks to ensure there is a single value for the feature.
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: SingleValueValidation.java,v 1.1 2004/02/13 03:08:00 jive Exp $
 */
public class SingleValueValidation extends DefaultFeatureValidation {

	/**
	 * SingleValueValidation constructor.
	 * <p>
	 * Super
	 * </p>
	 * 
	 */
	public SingleValueValidation() {
		super();
	}
	


	/**
	 * SingleValueValidation test for feature.
	 * 
	 * <p>
	 * Checks to ensure there is a single value for the feature.
	 * </p>
	 *
	 * @param feature The Feature to be validated
	 * @param type The FeatureType of the feature
	 * @param results The storage for error messages.
	 *
	 * @return <code>true</code> if the feature is a valid geometry.
	 *
	 * @see org.geotools.validation.FeatureValidation#validate
	 */
	public boolean validate(Feature feature, FeatureType type,
			ValidationResults results) {
		return false;
	}

}
