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

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.property.DashArrayEditor;
import org.geotools.gui.swing.sldeditor.property.ExpressionEditor;
import org.geotools.gui.swing.sldeditor.property.GraphicEditor;
import org.geotools.gui.swing.sldeditor.property.StrokeEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.gui.swing.sldeditor.util.StringListEditor;
import org.geotools.styling.Stroke;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DefaultStrokeEditor extends StrokeEditor {
    /** The logger for the Legend module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.gui.swing.sldeditor");
    ExpressionEditor colorEditor;
	ExpressionEditor widthEditor;
	ExpressionEditor opacityEditor;
    StringListEditor capEditor;
    StringListEditor joinEditor;
    GraphicEditor gfillEditor;
    GraphicEditor gstrokeEditor;
    JTabbedPane tabbedPane;
    JLabel lblColor;
    JLabel lblOpacity;
    JLabel lblWidth;
    JLabel lblCap;
    JLabel lblJoin;
    JLabel lblDash;
    JLabel lblDashOffset;
    JLabel lblGraphicFill;
    JLabel lblGraphicStroke;
    JCheckBox chkStroke;
    JCheckBox chkDash;
    JCheckBox chkGraphicFill;
    JCheckBox chkGraphicStroke;
    DashArrayEditor dashEditor;
    ExpressionEditor dashOffsetEditor;
    Stroke stroke = null;

    /**
     * Creates a new instance of FillEditor
     */
    public DefaultStrokeEditor(FeatureType featureType) {
        this(featureType, styleBuilder.createStroke());
    }

    public DefaultStrokeEditor(FeatureType featureType, Stroke stroke) {
        initComponents(featureType);
        setStroke(stroke);
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents(FeatureType featureType) {
        setLayout(new java.awt.BorderLayout());

		setLayout(new GridBagLayout());
        JPanel graphicStrokePanel = new JPanel();
        graphicStrokePanel .setLayout(new java.awt.GridBagLayout());
        JPanel graphicFillPanel = new JPanel();
        graphicFillPanel.setLayout(new java.awt.GridBagLayout());
        // tabbedPane.add("Basic", basicPanel);
        // tabbedPane.add("Graphic", graphicPanel);
        
        
        chkStroke = new JCheckBox("Use stroke");
        lblColor = new JLabel("Color");
        colorEditor = propertyEditorFactory.createColorEditor(featureType);
        lblWidth = new JLabel("Width");
        widthEditor = propertyEditorFactory.createIntSizeEditor(featureType);
        lblOpacity = new JLabel("Opacity");
        opacityEditor = propertyEditorFactory.createOpacityEditor(featureType);
        lblCap = new JLabel("Cap");
        capEditor = new StringListEditor(new String[] { "mitre", "round", "bevel" });
        lblJoin = new JLabel("Join");
        joinEditor = new StringListEditor(new String[] { "butt", "round", "square" });
        chkDash = new JCheckBox("Dashed line");
        lblDash = new JLabel("Dash");
        dashEditor = propertyEditorFactory.createDashArrayEditor();
        lblDashOffset = new JLabel("Dash offset");
        dashOffsetEditor = propertyEditorFactory.createIntSizeEditor(featureType);
        chkGraphicFill = new JCheckBox("Graphic fill");
        lblGraphicFill = new JLabel("Graphic fill");
        gfillEditor = propertyEditorFactory.createGraphicFillEditor(featureType);
        chkGraphicStroke = new JCheckBox("Graphic stroke");
        lblGraphicStroke = new JLabel("Graphic stroke");
        gstrokeEditor = propertyEditorFactory.createGraphicStrokeEditor(featureType);

        // setup first panel
        chkStroke.setSelected(true);
        chkStroke.setBorder(BorderFactory.createEmptyBorder());
        chkStroke.setBorderPaintedFlat(true);
        chkStroke.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    boolean enabled = chkStroke.isSelected();
                    lblColor.setEnabled(enabled);
                    colorEditor.setEnabled(enabled);
                    lblWidth.setEnabled(enabled);
                    widthEditor.setEnabled(enabled);
                    lblOpacity.setEnabled(enabled);
                    opacityEditor.setEnabled(enabled);
                    lblCap.setEnabled(enabled);
                    capEditor.setEnabled(enabled);
                    lblJoin.setEnabled(enabled);
                    joinEditor.setEnabled(enabled);
                    chkDash.setEnabled(enabled);
                    chkGraphicFill.setEnabled(enabled);
                    chkGraphicStroke.setEnabled(enabled);
                    updateDashComponentsState();
                    updateGraphicFillState();
                    updateGraphicStrokeState();
                }
            });

        // FormUtils.addRowInGBL(basicPanel, 0, 0, FormUtils.getTitleLabel("Basic"));
        FormUtils.addRowInGBL(this, 1, 0, chkStroke);
		FormUtils.addRowInGBL(this, 2, 0, new JLabel());
        FormUtils.addRowInGBL(this, 3, 0, lblColor, colorEditor);
        FormUtils.addRowInGBL(this, 4, 0, lblWidth, widthEditor);
        FormUtils.addRowInGBL(this, 5, 0, lblOpacity, opacityEditor);
        FormUtils.addRowInGBL(this, 6, 0, lblCap, capEditor);
        FormUtils.addRowInGBL(this, 7, 0, lblJoin, joinEditor);
        updateDashComponentsState();
        chkDash.setBorder(BorderFactory.createEmptyBorder());
        chkDash.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    updateDashComponentsState();
                }
            });
        // FormUtils.addFiller(basicPanel, 8, 0);

        // setup graphic panel
        // FormUtils.addRowInGBL(graphicPanel, 0, 0, FormUtils.getTitleLabel("Graphic"));
        FormUtils.addRowInGBL(this, 3, 2, chkDash, dashEditor);
        // lblDashOffset.setBorder(BorderFactory.createEmptyBorder(0, 17, 0, 0));
        FormUtils.addRowInGBL(this, 4, 2, lblDashOffset, dashOffsetEditor);
        chkDash.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    updateDashComponentsState();
                }
            });

        chkGraphicFill.setBorder(BorderFactory.createEmptyBorder());
        FormUtils.addRowInGBL(this, 5, 2, chkGraphicFill, gfillEditor);
        chkGraphicStroke.setBorder(BorderFactory.createEmptyBorder());
        chkGraphicStroke.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    updateGraphicStrokeState();
                }
            });
        chkGraphicStroke.setSelected(false);
        updateGraphicStrokeState();
        FormUtils.addRowInGBL(this, 6, 2, chkGraphicStroke, gstrokeEditor);
        FormUtils.addFiller(this, 8, 0);
        // FormUtils.addFiller(graphicPanel, 1, 2);
        chkGraphicFill.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    updateGraphicFillState();
                }
            });
        chkGraphicFill.setSelected(false);
        updateGraphicFillState();
        
        // setLayout(new GridBagLayout());
        // FormUtils.addColInGBL(this, 0, 0, basicPanel, graphicPanel);
    }

    /**
     *
     */
    protected void updateGraphicStrokeState() {
        boolean enabled = chkGraphicStroke.isSelected() && chkStroke.isSelected();
        lblGraphicStroke.setEnabled(enabled);
        gstrokeEditor.setEnabled(enabled);
    }

    private void updateGraphicFillState() {
        boolean enabled = chkGraphicFill.isSelected() && chkStroke.isSelected();
        lblGraphicFill.setEnabled(enabled);
        gfillEditor.setEnabled(enabled);
    }

    private void updateDashComponentsState() {
        boolean enabled = chkStroke.isSelected() && chkDash.isSelected();
        lblDash.setEnabled(enabled);
        dashEditor.setEnabled(enabled);
        lblDashOffset.setEnabled(enabled);
        dashOffsetEditor.setEnabled(enabled);
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
        if (stroke == null) {
            chkStroke.setSelected(false);

            return;
        } 
        chkStroke.setSelected(true);

        colorEditor.setExpression(stroke.getColor());
        widthEditor.setExpression(stroke.getWidth());
        opacityEditor.setExpression(stroke.getOpacity());
        capEditor.setExpression(stroke.getLineCap());
        joinEditor.setExpression(stroke.getLineJoin());

        float[] dashArray = stroke.getDashArray();
        if ((dashArray == null) || (dashArray.length == 0)) {
            chkDash.setSelected(false);
        } else {
            chkDash.setSelected(true);
            dashEditor.setDashArray(dashArray);
            dashOffsetEditor.setExpression(stroke.getDashOffset());
        }
    }

    public Stroke getStroke() {
        if (!chkStroke.isSelected()) {
            return null;
        }

        if (stroke == null) {
            stroke = styleBuilder.createStroke();
        }

        stroke.setColor(colorEditor.getExpression());
        stroke.setWidth(widthEditor.getExpression());
        stroke.setOpacity(opacityEditor.getExpression());
        stroke.setLineCap(capEditor.getExpression());
        stroke.setLineJoin(joinEditor.getExpression());
        if (chkDash.isSelected()) {
            stroke.setDashArray(dashEditor.getDashArray());
            stroke.setDashOffset(dashOffsetEditor.getExpression());
        } else {
            stroke.setDashArray(null);
            stroke.setDashOffset(null);
        }

        return stroke;
    }

    

    /**
     * @see java.awt.Component#getPreferredSize()
     */
//    public Dimension getPreferredSize() {
//        Dimension base = super.getPreferredSize();
//        FontMetrics fm = getFontMetrics(getFont());
//        int totalWidth = 0;
//        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
//            String title = tabbedPane.getTitleAt(i);
//            totalWidth += fm.stringWidth(title);
//        }
//
//        totalWidth += (tabbedPane.getTabCount() * fm.stringWidth("MMI"));
//        if (totalWidth > base.width) {
//            base.width = totalWidth;
//        }
//
//        return base;
//    }
    
    public static void main(String[] args) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new DefaultStrokeEditor(null));
        FormUtils.show(panel);
    }
}
