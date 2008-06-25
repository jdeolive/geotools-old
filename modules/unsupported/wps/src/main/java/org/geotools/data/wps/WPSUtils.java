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

import net.opengis.wps.ComplexDataCombinationsType;
import net.opengis.wps.ComplexDataDescriptionType;
import net.opengis.wps.ComplexDataType;
import net.opengis.wps.DataType;
import net.opengis.wps.InputDescriptionType;
import net.opengis.wps.LiteralDataType;
import net.opengis.wps.LiteralInputType;
import net.opengis.wps.ProcessDescriptionType;
import net.opengis.wps.SupportedComplexDataInputType;
import net.opengis.wps.WpsFactory;

/**
 * Contains helpful static util methods for the WPS module
 * 
 * @author gdavis
 */
public class WPSUtils {
	
	/**
	 * static ints representing the input types
	 */
	public static final int INPUTTYPE_LITERAL = 1;
	public static final int INPUTTYPE_COMPLEXDATA = 2;
	
    /**
     * Creates a DataType input object from the given object and
     * InputDescriptionType (from a describeprocess) and decides if 
     * the input is a literal or complex data based on its type.
	 * 
	 * @param obj the base input object
	 * @param idt input description type defining the input
	 * @return the created DataType input object
     */
    public static DataType createInput(Object obj, InputDescriptionType idt) {
    	int inputtype = 0;
    	
    	// first try to figure out if the input is a literal or complex based
    	// on the data in the idt
    	LiteralInputType literalData = idt.getLiteralData();
    	SupportedComplexDataInputType complexData = idt.getComplexData();
    	if (literalData != null) {
    		inputtype = INPUTTYPE_LITERAL;
    	}
    	else if (complexData != null) {
    		inputtype = INPUTTYPE_COMPLEXDATA;
    	}
    	else {
    		// is the value a literal?  Do a very basic test here for common
        	// literal types.  TODO:  figure out a more thorough test here
    		if (obj instanceof String ||
    				obj instanceof Double ||
    				obj instanceof Float ||
    				obj instanceof Integer ) {
    			inputtype = INPUTTYPE_LITERAL;
    		}
    		else {
    			// assume complex data
    			inputtype = INPUTTYPE_COMPLEXDATA;
    		}
    	}
    	
		// now create the input based on its type
    	String schema = null;
		if (inputtype == INPUTTYPE_COMPLEXDATA) {
			ComplexDataCombinationsType supported = complexData.getSupported();
			ComplexDataDescriptionType cddt = (ComplexDataDescriptionType) supported.getFormat().get(0);
			schema = cddt.getSchema(); 
		}
		
		return createInput(obj, inputtype, schema);
	}	

    /**
     * Creates a DataType input object from the given object, schema and
     * type (complex or literal).
	 * 
	 * @param obj the base input object
	 * @param type the input type (literal or complexdata)
	 * @param schema only used for type complexdata
	 * @return the created DataType input object
     */
    public static DataType createInput(Object obj, int type, String schema) {
    	DataType dt = WpsFactory.eINSTANCE.createDataType();
    	
    	if (type == INPUTTYPE_LITERAL) {
			
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
			//cdt.getMixed().add(obj);
			cdt.getData().add(obj);
			
			if (schema != null) {
				cdt.setSchema(schema);
			}
			dt.setComplexData(cdt);
		}
		return dt;
	}
}
