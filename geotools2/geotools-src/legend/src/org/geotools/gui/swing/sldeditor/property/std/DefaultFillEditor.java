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
 * FillEditor.java
 *
 * Created on 6 dicembre 2003, 19.28
 */
package org.geotools.gui.swing.sldeditor.property.std;

import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.property.ExpressionEditor;
import org.geotools.gui.swing.sldeditor.property.FillEditor;
import org.geotools.gui.swing.sldeditor.property.GraphicEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.styling.Fill;




/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DefaultFillEditor extends FillEditor {
    /** The logger for the Legend module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gui.swing.sldeditor");
    ExpressionEditor colorEditor;
	ExpressionEditor backgroundEditor;
    ExpressionEditor opacityEditor;
    JLabel lblColor;
    JLabel lblBackground;
    JLabel lblOpacity;
    JCheckBox chkFill;
    JCheckBox chkGraphicFill;
    GraphicEditor gfillEditor;
    Fill fill = null;
    boolean isFillOptional;
    FeatureType featureType;

    /**
     * Creates a new instance of FillEditor
     */
    public DefaultFillEditor(FeatureType featureType) {
        this(featureType, styleBuilder.createFill(), true);
    }
    
	public DefaultFillEditor(FeatureType featureType, Fill fill) {
		this(featureType, fill, true);
	}

    public DefaultFillEditor(FeatureType featureType, Fill fill, boolean isFillOptional) {
        this.featureType = featureType;
        initComponents(false);
		this.isFillOptional = isFillOptional;
		chkFill.setVisible(isFillOptional);

        setFill(fill);
    }

    /**
     * This method is called from within the constructor to initialize the
     * form.
     *
     * @param showBackground DOCUMENT ME!
     */
    private void initComponents(boolean showBackground) {
        setLayout(new java.awt.GridBagLayout());

        // setBorder(new javax.swing.border.TitledBorder("Fill"));
        chkFill = new JCheckBox("Use fill");
        lblColor = new JLabel("Color");
        colorEditor = propertyEditorFactory.createColorEditor(featureType);
        lblBackground = new JLabel("Background");
        backgroundEditor = propertyEditorFactory.createColorEditor(featureType);
        lblOpacity = new JLabel("Opacity");
        opacityEditor = propertyEditorFactory.createOpacityEditor(featureType);
        chkGraphicFill = new JCheckBox("Graphic fill");
        gfillEditor = propertyEditorFactory.createGraphicFillEditor(featureType);

        chkFill.setBorder(BorderFactory.createEmptyBorder());
        chkFill.setBorderPaintedFlat(true);
        chkFill.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    boolean enabled = chkFill.isSelected();
                    lblColor.setEnabled(enabled);
                    colorEditor.setEnabled(enabled);
                    lblBackground.setEnabled(enabled);
                    backgroundEditor.setEnabled(enabled);
                    lblOpacity.setEnabled(enabled);
                    opacityEditor.setEnabled(enabled);
                    chkGraphicFill.setEnabled(enabled);
                    gfillEditor.setEnabled(enabled
                        && chkGraphicFill.isSelected());
                }
            });
        chkFill.setSelected(true);

        chkGraphicFill.setBorder(BorderFactory.createEmptyBorder());
        chkGraphicFill.setBorderPaintedFlat(true);
        chkGraphicFill.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    gfillEditor.setEnabled(chkFill.isSelected()
                        && chkGraphicFill.isSelected());
                }
            });
        chkGraphicFill.setSelected(false);

        FormUtils.addRowInGBL(this, 0, 0, chkFill);
		FormUtils.addRowInGBL(this, 1, 0, new JLabel());
        FormUtils.addRowInGBL(this, 2, 0, lblColor, colorEditor);

        if (showBackground) {
            FormUtils.addRowInGBL(this, 3, 0, lblBackground, backgroundEditor);
        }

        FormUtils.addRowInGBL(this, 4, 0, lblOpacity, opacityEditor);
        FormUtils.addRowInGBL(this, 5, 0, chkGraphicFill, gfillEditor);

        FormUtils.addFiller(this, 6, 0);
    }

    public void setFill(Fill fill) {
        this.fill = fill;
        chkFill.setSelected(fill != null || !isFillOptional);

        if (fill != null) {
            colorEditor.setExpression(fill.getColor());
            backgroundEditor.setExpression(fill.getBackgroundColor());
            opacityEditor.setExpression(fill.getOpacity());

            if (fill.getGraphicFill() != null) {
                chkGraphicFill.setSelected(true);
                gfillEditor.setGraphic(fill.getGraphicFill());
            } else {
                chkGraphicFill.setSelected(false);
            }
        }
    }

    public Fill getFill() {
        if (!chkFill.isSelected()) {
            return null;
        }

        if (fill == null) {
            fill = styleBuilder.createFill();
        }

        fill.setColor(colorEditor.getExpression());
        fill.setBackgroundColor(backgroundEditor.getExpression());
        fill.setOpacity(opacityEditor.getExpression());

        if (chkGraphicFill.isSelected()) {
            fill.setGraphicFill(gfillEditor.getGraphic());
        } else {
            fill.setGraphicFill(null);
        }

        return fill;
    }

    public static void main(String[] args) {
        FormUtils.show(new DefaultFillEditor(null));
    }
}
