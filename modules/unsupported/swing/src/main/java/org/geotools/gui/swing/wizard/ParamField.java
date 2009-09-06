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

import org.geotools.data.Parameter;

/**
 * Super class that provides additional helper methods useful when implementing your own
 * ParamWidget.
 * 
 * @author gdavis
 */
public abstract class ParamField {
    protected final Parameter<?> parameter;

    /**
     * Holds on to the parameter so implementations can consult the type and metadata information.
     * 
     * @param parameter
     */
    ParamField(Parameter<?> parameter) {
        this.parameter = parameter;
    }

    /**
     * Called to build the widget, initialize it (setting defaults or whatever) and setup any
     * listeners needed for validation of the widget value. The returned JComponent will contain the
     * widget for editing.
     * 
     * @return JComponent or null if error
     */
    abstract public JComponent doLayout();

    /**
     * Validates the current value of the widget, returns false if not valid, true otherwise
     * 
     * @return boolean if validated
     */
    abstract public boolean validate();

    /**
     * Sets the value of the widget.
     * 
     * @param Object
     *            an object containing the value to set for the widget
     */
    abstract public void setValue(Object value);

    /**
     * Returns the current value of the widget.
     * 
     * @return Object representing the current value of the widget
     */
    abstract public Object getValue();

}
