/*
 * Created on 1-gen-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.gui.swing.sldeditor.property.std;

import java.awt.BorderLayout;

import javax.swing.JComboBox;

import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.Expression;
import org.geotools.filter.IllegalFilterException;
import org.geotools.gui.swing.sldeditor.property.ExpressionEditor;

/**
 * A simple GUI to choose one attribute from the featureType
 * @author wolf
 *
 * TODO: merge somehow with GeometryChooser and add the ability to
 * filter the attributes shown to the user with a Filter classs
 */
public class DefaultFeatureAttributeChooser extends ExpressionEditor {
	JComboBox cmbAttributes;
	String[] attNames;

	public DefaultFeatureAttributeChooser(FeatureType ft) {
		this(null, ft);
	}

	public DefaultFeatureAttributeChooser(String selectedAttribute, FeatureType ft) {
		if (ft == null || ft.getAttributeTypes() == null) {
			attNames = new String[0];
		} else {
			AttributeType[] ats = ft.getAttributeTypes();
			attNames = new String[ats.length];
			for (int i = 0; i < ats.length; i++) {
				attNames[i] = ats[i].getName();
			}
		}
		cmbAttributes = new JComboBox(attNames);
		setChosenAttribute(selectedAttribute);

		// layout
		setLayout(new BorderLayout());
		add(cmbAttributes);
	}

	public void setChosenAttribute(String attribute) {
		int selectedIndex = -1;
		for (int i = 0; i < attNames.length; i++) {
			if (attNames[i].equalsIgnoreCase(attribute))
				selectedIndex = i;
		}
		if (selectedIndex != -1)
			cmbAttributes.setSelectedIndex(selectedIndex);
	}

	public String getChosenAttribute() {
		return (String) cmbAttributes.getSelectedItem();
	}
	
	public void setExpression(Expression exp) {
		if(exp instanceof AttributeExpression) {
			AttributeExpression ae = (AttributeExpression) exp;
			setChosenAttribute(ae.getAttributePath());
		}
	}

	public Expression getExpression() {
		String chosen = getChosenAttribute();
		if (chosen == null)
			return null;
		else
			try {
				return styleBuilder.attributeExpression(chosen);
			} catch (IllegalFilterException e) {
				throw new RuntimeException(e);
			}
	}

    /**
     * @see org.geotools.gui.swing.sldeditor.property.ExpressionEditor#canEdit(org.geotools.filter.Expression)
     */
    public boolean canEdit(Expression expression) {
		return expression instanceof AttributeExpression;
    }
}
