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
 * PointCoveredByLineValidation purpose.
 * <p>
 * Completes the specified attribute comparison.
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: AttributeValidation.java,v 1.1 2004/02/13 03:07:59 jive Exp $
 */
public class AttributeValidation  extends DefaultFeatureValidation {

	private String attributeComparisonValue;
	private String attributeName;
	private int attributeComparisonType;
	
	public static final int LESS_THAN = -1;
	public static final int EQUALITY = 0;
	public static final int GREATER_THAN = 1;
	
	/**
	 * PointCoveredByLineValidation constructor.
	 * <p>
	 * Super
	 * </p>
	 * 
	 */
	public AttributeValidation() {super();}

	/**
	 * Completes the specified comparison.
	 * 
	 * @see org.geotools.validation.FeatureValidation#validate(org.geotools.feature.Feature, org.geotools.feature.FeatureType, org.geotools.validation.ValidationResults)
	 * 
	 * @param feature Feature to be Validated
     * @param type FeatureTypeInfo schema of feature
     * @param results coallate results information
	 * @return
	 */
	public boolean validate(Feature feature, FeatureType type,ValidationResults results) {
		return false;
	}

	/**
	 * Access attributeComparisonType property.
	 * 
	 * @return Returns the attributeComparisonType.
	 */
	public int getAttributeComparisonType() {
		return attributeComparisonType;
	}

	/**
	 * Set attributeComparisonType to attributeComparisonType.
	 *
	 * @param attributeComparisonType The attributeComparisonType to set.
	 */
	public void setAttributeComparisonType(int attributeComparisonType) {
		this.attributeComparisonType = attributeComparisonType;
	}

	/**
	 * Access attributeComparisonValue property.
	 * 
	 * @return Returns the attributeComparisonValue.
	 */
	public String getAttributeComparisonValue() {
		return attributeComparisonValue;
	}

	/**
	 * Set attributeComparisonValue to attributeComparisonValue.
	 *
	 * @param attributeComparisonValue The attributeComparisonValue to set.
	 */
	public void setAttributeComparisonValue(String attributeComparisonValue) {
		this.attributeComparisonValue = attributeComparisonValue;
	}

	/**
	 * Access attributeName property.
	 * 
	 * @return Returns the attributeName.
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * Set attributeName to attributeName.
	 *
	 * @param attributeName The attributeName to set.
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

}
