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
package org.geotools.gui.swing.wizard;

import javax.swing.JComponent;
import javax.swing.JTextField;

import org.geotools.data.Parameter;

/**
 *  Widget for double values
 * 
 * @author gdavis
 */
public class JDoubleField extends ParamField {

    private JTextField blah;
    
	public JDoubleField(Parameter<?> parameter) {
		super( parameter );
	}
	
	public JComponent doLayout() {
		blah = new JTextField(16); 
	    return blah;
	}

	public Object getValue() {
	    String val = blah.getText();
	    if (val == null || val.equals("")) {
	    	return new Double(0);
	    }
	    try {
	    	return new Double(val);
	    }
	    catch (NumberFormatException e) {
	    	return new Double(0);
	    }
	}

	public void setValue(Object value) {
	    blah.setText(((Double)value).toString());
	}

	public boolean validate() {
		String val = blah.getText();
		try {
		    Double d = Double.parseDouble(val);
		    return d != null;
		}
		catch (NumberFormatException e) {
		    return false;
		}
	}

}
