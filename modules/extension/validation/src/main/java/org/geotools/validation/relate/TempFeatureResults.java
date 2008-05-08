/*
 * Created on 18-Jun-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.validation.relate;

import java.util.ArrayList;
import java.util.List;

import org.geotools.validation.Validation;
import org.geotools.validation.ValidationResults;
import org.opengis.feature.simple.SimpleFeature;

/**
 * @source $URL$
 */
final class TempFeatureResults implements ValidationResults {
	Validation trial;
	List error = new ArrayList();
	List warning = new ArrayList();
	public void setValidation(Validation validation) {
		trial = validation;									
	}
	public void error(SimpleFeature feature, String message) {
		String where = feature != null ? feature.getID() : "all"; 
		error.add( where + ":"+ message );
		System.err.println( where + ":"+ message );
	}
	public void warning(SimpleFeature feature, String message) {
		String where = feature != null ? feature.getID() : "all";
		warning.add( where + ":"+ message );
		System.out.println( where + ":"+ message );
	}
}
