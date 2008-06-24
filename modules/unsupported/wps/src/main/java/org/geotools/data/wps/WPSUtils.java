/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.wps;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.emf.ecore.util.FeatureMap.Entry;

import net.opengis.wps.ComplexDataType;
import net.opengis.wps.DataType;
import net.opengis.wps.LiteralDataType;
import net.opengis.wps.WpsFactory;

/**
 * Contains helpful static util methods for the WPS module
 * 
 * @author gdavis
 */
public class WPSUtils {

    /**
     * Creates a DataType input object from the given object and
     * decides if the input is a literal or complex data 
	 * based on its type.
	 * 
	 * @param obj the base input object
	 * @return the created DataType input object
     */
    public static DataType createInput(Object obj, String schema) {
    	DataType dt = WpsFactory.eINSTANCE.createDataType();
    	
		// is the value a literal?  Do a very basic test here for common
    	// literal types.  TODO:  do a more thorough test here
		if (obj instanceof String ||
				obj instanceof Double ||
				obj instanceof Float ||
				obj instanceof Integer ) {
			
			LiteralDataType ldt = WpsFactory.eINSTANCE.createLiteralDataType();
			ldt.setValue(obj.toString());
			dt.setLiteralData(ldt);
		}
		else {
			// assume complex data
			ComplexDataType cdt = WpsFactory.eINSTANCE.createComplexDataType();
			
			// do I need to add a FeatureMap object, or Entry object, or what?
			//EStructuralFeature eStructuralFeature = null;
			//Entry createEntry = FeatureMapUtil.createEntry(eStructuralFeature, obj);
			cdt.getMixed().add(obj);
			if (schema != null) {
				cdt.setSchema(schema);
			}
			dt.setComplexData(cdt);
		}
		return dt;
	}
}
