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
 * GraphicEditor.java
 *
 * Created on 8 dicembre 2003, 10.21
 */
package org.geotools.gui.swing.sldeditor.property.std;

import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.property.ExpressionEditor;
import org.geotools.gui.swing.sldeditor.property.GraphicEditor;
import org.geotools.gui.swing.sldeditor.property.SymbolEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.styling.Graphic;
import org.geotools.styling.Symbol;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DefaultGraphicEditor extends GraphicEditor {
    Graphic graphic;
    JLabel lblSize;
    JLabel lblRotation;
    JLabel lblOpacity;
    ExpressionEditor sizeEditor;
	ExpressionEditor rotationEditor;
	ExpressionEditor opacityEditor;
    SymbolEditor symbolEditor;

    /**
     * Creates a new instance of GraphicEditor
     */
    public DefaultGraphicEditor(FeatureType featureType) {
        this(styleBuilder.createGraphic(), featureType);
    }

    public DefaultGraphicEditor(Graphic graphic, FeatureType featureType) {
        JPanel basicPanel = new JPanel();
        basicPanel.setLayout(new GridBagLayout());
        JPanel symbolPanel = new JPanel();
        symbolPanel.setLayout(new GridBagLayout());
        
        lblSize = new JLabel("Size");
        lblRotation = new JLabel("Rotation");
        lblOpacity = new JLabel("Opacity");
        sizeEditor = propertyEditorFactory.createIntSizeEditor(featureType);
        rotationEditor = propertyEditorFactory.createRotationEditor(featureType);
        opacityEditor = propertyEditorFactory.createOpacityEditor(featureType);
        symbolEditor = propertyEditorFactory.createSymbolEditor(featureType);

        FormUtils.addRowInGBL(basicPanel, 0, 0, FormUtils.getTitleLabel("Basic"));
        FormUtils.addRowInGBL(basicPanel, 1, 0, lblSize, sizeEditor);
        FormUtils.addRowInGBL(basicPanel, 2, 0, lblRotation, rotationEditor);
        FormUtils.addRowInGBL(basicPanel, 3, 0, lblOpacity, opacityEditor);
        FormUtils.addRowInGBL(symbolPanel, 0, 0, FormUtils.getTitleLabel("Symbols"));
        FormUtils.addFiller(symbolPanel, 1, 0, symbolEditor, 1.0, false);

        setLayout(new GridBagLayout());
        FormUtils.addColInGBL(this, 0, 0, basicPanel, symbolPanel);
        
        setGraphic(graphic);
    }

    public void setGraphic(Graphic graphic) {
        sizeEditor.setExpression(graphic.getSize());
        rotationEditor.setExpression(graphic.getRotation());
        opacityEditor.setExpression(graphic.getOpacity());

        Symbol[] symbols = graphic.getSymbols();
        if ((symbols != null) && (symbols.length > 0)) {
            symbolEditor.setSymbol(symbols[0]);
        }

        this.graphic = graphic;
    }

    public Graphic getGraphic() {
        graphic.setSize(sizeEditor.getExpression());
        graphic.setRotation(rotationEditor.getExpression());
        graphic.setOpacity(opacityEditor.getExpression());
        graphic.setSymbols(new Symbol[] { symbolEditor.getSymbol() });

        return this.graphic;
    }

    public static void main(String[] args) {
        FormUtils.show(new DefaultGraphicEditor(null));
    }
}
