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
 * SQL Validation
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: SQLValidation.java,v 1.1 2004/02/13 03:08:00 jive Exp $
 */
public class SQLValidation  extends DefaultFeatureValidation {

	private String sql;
	
	/**
	 * PointCoveredByLineValidation constructor.
	 * <p>
	 * Super
	 * </p>
	 * 
	 */
	public SQLValidation() {super();}

	/**
	 * SQL Validation
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
	 * Access lineTypeRef property.
	 * 
	 * @return Returns the lineTypeRef.
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * Set lineTypeRef to lineTypeRef.
	 *
	 * @param lineTypeRef The lineTypeRef to set.
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

}
