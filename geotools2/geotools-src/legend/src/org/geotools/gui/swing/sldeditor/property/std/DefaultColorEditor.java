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
package org.geotools.gui.swing.sldeditor.property.std;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;

import org.geotools.filter.Expression;
import org.geotools.filter.LiteralExpression;
import org.geotools.gui.swing.sldeditor.property.ExpressionEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;


/**
 * A color editor that supports
 *
 * @author wolf
 */
public class DefaultColorEditor extends ExpressionEditor {
    private JButton btnColor;

    public DefaultColorEditor() {
        this(Color.GRAY);
    }

    /**
     * Creates a new instance of ColorChooser
     *
     * @param color DOCUMENT ME!
     */
    public DefaultColorEditor(Color color) {
        btnColor = new JButton();
        btnColor.setMinimumSize(FormUtils.getColorButtonDimension());
        btnColor.setPreferredSize(FormUtils.getColorButtonDimension());

        setColor(color);

        this.setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(btnColor, gridBagConstraints);

        // make it stay on the west side
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(new JLabel(), gridBagConstraints);

        setColor(color);

        btnColor.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Color newColor = JColorChooser.showDialog(DefaultColorEditor.this, "Choose a color",
                            getColor());

                    if (newColor != null) {
                        setColor(newColor);
                    }
                }
            });
    }

    public Color getColor() {
        return btnColor.getBackground();
    }

    public Expression getExpression() {
        return styleBuilder.colorExpression(getColor());
    }

    public void setColor(Color col) {
        btnColor.setBackground(col);
    }

    public void setExpression(Expression e) {
        // if a simple string, convert immediatly to Color, if not, 
        // for the moment throw and exception
        if (e == null) {
            setColor(Color.GRAY);
        } else {
        	try {
				setColor(Color.decode(e.toString()));
        	} catch (NumberFormatException nfe) {
        		setColor(Color.GRAY);
        	}
        }
    }

    public static void main(String[] args) {
        FormUtils.show(new DefaultColorEditor(Color.BLUE));
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        btnColor.setEnabled(enabled);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.ExpressionEditor#canEdit(org.geotools.filter.Expression)
     */
    public boolean canEdit(Expression expression) {
        if(expression instanceof LiteralExpression) {
        	LiteralExpression le = (LiteralExpression) expression;
        	Object literal = le.getLiteral();
        	if(literal instanceof Color) {
        		return true;
        	} else if(literal instanceof String){
        		try {
        			Color.decode((String) literal);
        			return true;
        		} catch (NumberFormatException nfe) {
        			return false;
        		}
        	} else {
        		return false;
        	}
        } else {
        	return false;
        }
        	
    }
}
