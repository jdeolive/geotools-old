/*
 * Created on 1-gen-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.gui.swing.sldeditor.property.std;

import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.gui.swing.sldeditor.property.ExpressionEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.styling.LinePlacement;

/**
 * @author wolf
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DefaultLinePlacementEditor extends JComponent implements SLDEditor {
	LinePlacement linePlacement;
	JLabel lblOffset;
	ExpressionEditor neOffset;
	
	public DefaultLinePlacementEditor(FeatureType featureType) {
		this(featureType, null);
	}
	
	public DefaultLinePlacementEditor(FeatureType featureType, LinePlacement linePlacement) {
		lblOffset = new JLabel("Perpendicular offset");
		neOffset = propertyEditorFactory.createNumberEditor(new Double(0), new Double(Double.MIN_VALUE), new Double(Double.MAX_VALUE), new Double(1), featureType);
		
		setLayout(new GridBagLayout());
		FormUtils.addRowInGBL(this, 0, 0, lblOffset, neOffset);
		
		setLinePlacement(linePlacement);
	}

	/**
	 * @param placement
	 */
	public void setLinePlacement(LinePlacement linePlacement) {
		if(linePlacement == null) {
			linePlacement = styleBuilder.createLinePlacement(0.0);
		} 
		this.linePlacement = linePlacement;
		
		
		neOffset.setExpression(linePlacement.getPerpendicularOffset());		
	}

	/**
	 * @return
	 */
	public LinePlacement getLinePlacement() {
		linePlacement.setPerpendicularOffset(neOffset.getExpression());
		return linePlacement;
	}

}
