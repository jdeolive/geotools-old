/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
/*
 * SLDOpacityEditor.java
 *
 * Created on 7 dicembre 2003, 9.39
 */
package org.geotools.gui.swing.sldeditor.property.std;

import java.awt.BorderLayout;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.geotools.filter.Expression;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LiteralExpression;
import org.geotools.gui.swing.sldeditor.property.ExpressionEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DefaultNumberEditor extends ExpressionEditor {
    JSpinner spnValue;
    double conversionFactor = 1.0;
    Class valueClass;

    /** Holds value of property cyclic. */
    private boolean cyclic = false;

    public DefaultNumberEditor() {
        this(new Double(1.0), new Double(0), new Double(1000), new Double(0.5));
    }

    public DefaultNumberEditor(Number value, Number minimum, Number maximum, Number step) {
        this(value, minimum, maximum, step, 1.0);
    }

    /**
     * Creates a new instance of SLDOpacityEditor
     *
     * @param value DOCUMENT ME!
     * @param minimum DOCUMENT ME!
     * @param maximum DOCUMENT ME!
     * @param step DOCUMENT ME!
     * @param conversionFactor DOCUMENT ME!
     */
    public DefaultNumberEditor(Number value, Number minimum, Number maximum, Number step,
        double conversionFactor) {
        this.conversionFactor = conversionFactor;

        spnValue = new JSpinner(getSpinnerModel(value, minimum, maximum, step));
        spnValue.setMinimumSize(FormUtils.getSpinnerDimension());
        spnValue.setPreferredSize(FormUtils.getSpinnerDimension());

        this.setLayout(new BorderLayout());
        add(spnValue);
    }

    private SpinnerNumberModel getSpinnerModel(Number value, final Number minimum,
        final Number maximum, Number step) {
        final SpinnerNumberModel model;
        if (value instanceof Integer) {
            valueClass = Integer.class;
            model = new SpinnerNumberModel((Integer) value, (Integer) minimum, (Integer) maximum,
                    (Integer) step);
        } else {
            valueClass = Double.class;
            model = new SpinnerNumberModel((Double) value, (Double) minimum, (Double) maximum,
                    (Double) step);
        }

        model.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (cyclic && model.getValue().equals(maximum)) {
                        model.setValue(minimum);
                    }
                }
            });

        return model;
    }

    public void setValue(Number value) {
        if (conversionFactor != 1.0) {
            double uiValue = value.doubleValue() * conversionFactor;
            if (valueClass == Integer.class) {
                spnValue.setValue(new Integer((int) Math.round(uiValue)));
            } else {
                spnValue.setValue(new Double(uiValue));
            }
        } else {
            spnValue.setValue(value);
        }
    }

    public Number getValue() {
        if (conversionFactor != 1.0) {
            return new Double(((Number) spnValue.getValue()).doubleValue() / conversionFactor);
        } else {
            return ((Number) spnValue.getValue());
        }
    }

    public void setExpression(Expression exp) {
    	try {
			setValue(new Double(Double.parseDouble(exp.toString())));
    	} catch (Exception e) {
			setValue(new Integer(10));
		}
    }

    public Expression getExpression() {
        try {
            return styleBuilder.literalExpression(getValue());
        } catch (IllegalFilterException ife) {
            throw new RuntimeException("This should not happen", ife);
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        spnValue.setEnabled(enabled);
    }

    public static void main(String[] args) {
        FormUtils.show(new DefaultNumberEditor());
    }

    /**
     * Getter for property cyclic.
     *
     * @return Value of property cyclic.
     */
    public boolean isCyclic() {
        return this.cyclic;
    }

    /**
     * Setter for property cyclic.
     *
     * @param cyclic New value of property cyclic.
     */
    public void setCyclic(boolean cyclic) {
        if (this.cyclic == cyclic) {
            return;
        }

        this.cyclic = cyclic;
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.ExpressionEditor#canEdit(org.geotools.filter.Expression)
     */
    public boolean canEdit(Expression expression) {
        if(expression instanceof LiteralExpression) {
        	LiteralExpression le = (LiteralExpression) expression;
        	Object literal = le.getLiteral();
        	if(literal instanceof Number) {
        		return true;
        	} else if(literal instanceof String){
        		try {
        			Double.parseDouble((String) literal);
        			return true;
        		} catch (NumberFormatException nfe){
        			return false;
        		}
        	}
        }
        return false;
    }
}
