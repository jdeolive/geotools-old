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

import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.gui.swing.sldeditor.property.ExpressionEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.styling.AnchorPoint;
import org.geotools.styling.Displacement;
import org.geotools.styling.PointPlacement;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DefaultPointPlacementEditor extends JComponent implements SLDEditor {
    JLabel lblAnchorX;
    JLabel lblAnchorY;
    JLabel lblDispX;
    JLabel lblDispY;
    JLabel lblRotation;
    ExpressionEditor neAnchorX;
    ExpressionEditor neAnchorY;
    ExpressionEditor neDispX;
    ExpressionEditor neDispY;
    ExpressionEditor neRotation;
    PointPlacement pointPlacement;

    public DefaultPointPlacementEditor(FeatureType featureType) {
        init(featureType);
    }

    public DefaultPointPlacementEditor(FeatureType featureType, PointPlacement pp) {
        init(featureType);
        setPointPlacement(pp);
    }

    private void init(FeatureType featureType) {
        lblAnchorX = new JLabel("Anchor point x");
        lblAnchorY = new JLabel("Anchor point y");
        lblDispX = new JLabel("Displacement x");
        lblDispY = new JLabel("Displacement y");
        lblDispY = new JLabel("Rotation");
        neAnchorX = propertyEditorFactory.createDoubleEditor(featureType);
        neAnchorY = propertyEditorFactory.createDoubleEditor(featureType);
        neDispX = propertyEditorFactory.createDoubleEditor(featureType);
        neDispY = propertyEditorFactory.createDoubleEditor(featureType);
        neRotation = propertyEditorFactory.createRotationEditor(featureType);

        setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 0, 0, lblAnchorX, neAnchorX);
        FormUtils.addRowInGBL(this, 0, 0, lblAnchorY, neAnchorY);
        FormUtils.addRowInGBL(this, 0, 0, lblDispX, neDispX);
        FormUtils.addRowInGBL(this, 0, 0, lblDispY, neDispY);
        FormUtils.addRowInGBL(this, 0, 0, lblRotation, neRotation);
    }

    /**
     * DOCUMENT ME!
     *
     * @param pp
     */
    public void setPointPlacement(PointPlacement pp) {
        if (pp == null) {
            pp = styleBuilder.createPointPlacement();
        }

        this.pointPlacement = pp;

        if (pp.getAnchorPoint() == null) {
            neAnchorX.setExpression(styleBuilder.literalExpression(0.0));
            neAnchorY.setExpression(styleBuilder.literalExpression(0.0));
        } else {
            AnchorPoint ap = pp.getAnchorPoint();
            neAnchorX.setExpression(ap.getAnchorPointX());
            neAnchorY.setExpression(ap.getAnchorPointY());
        }

        if (pp.getDisplacement() == null) {
            neDispX.setExpression(styleBuilder.literalExpression(0.0));
            neDispY.setExpression(styleBuilder.literalExpression(0.0));
        } else {
            Displacement d = pp.getDisplacement();
            neDispX.setExpression(d.getDisplacementX());
            neDispY.setExpression(d.getDisplacementY());
        }

        neRotation.setExpression(pp.getRotation());
    }

    /**
     * Returns the point placement just edited
     *
     * @return
     */
    public PointPlacement getPointPlacement() {
        Expression zero = styleBuilder.literalExpression(0);

        if (neAnchorX.getExpression().equals(zero)
                && neAnchorY.getExpression().equals(zero)) {
            pointPlacement.setAnchorPoint(null);
        } else {
            pointPlacement.setAnchorPoint(styleBuilder.createAnchorPoint(
                    neAnchorX.getExpression(), neAnchorY.getExpression()));
        }

        if (neDispX.getExpression().equals(zero)
                && (neDispY.getExpression().equals(zero))) {
            pointPlacement.setDisplacement(null);
        } else {
            pointPlacement.setDisplacement(styleBuilder.createDisplacement(
                    neDispX.getExpression(), neDispY.getExpression()));
        }

        pointPlacement.setRotation(neRotation.getExpression());

        return pointPlacement;
    }
}
