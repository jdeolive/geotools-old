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
package org.geotools.gui.swing.sldeditor.symbolizer.std;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.gui.swing.sldeditor.property.GeometryChooser;
import org.geotools.gui.swing.sldeditor.property.StrokeEditor;
import org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Symbolizer;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DefaultLineSymbolizerEditor extends SymbolizerEditor implements SLDEditor {
    LineSymbolizer symbolizer;
    JLabel lblGeometry;
    GeometryChooser geomChooser;
    StrokeEditor strokeEditor;

    /**
     * Creates a new instance of PolygonSymbolizerEditor
     */
    public DefaultLineSymbolizerEditor(FeatureType featureType) {
        this(null, null);
    }

    public DefaultLineSymbolizerEditor(FeatureType ft, LineSymbolizer symbolizer) {
        setLayout(new GridBagLayout());

        if (symbolizer == null) {
            symbolizer = styleBuilder.createLineSymbolizer();
        }

        lblGeometry = new JLabel("Geometry property");
        geomChooser = propertyEditorFactory.createGeometryChooser(ft);
        strokeEditor = propertyEditorFactory.createStrokeEditor(ft);

        FormUtils.addRowInGBL(this, 0, 0, lblGeometry, geomChooser);
        FormUtils.addFiller(this, 1, 0, strokeEditor);

        if (geomChooser.getGeomPropertiesCount() < 2) {
            lblGeometry.setVisible(false);
            geomChooser.setVisible(false);
        }

        setSymbolizer(symbolizer);
    }

    public void setSymbolizer(Symbolizer symbolizer) {
        if (!(symbolizer instanceof LineSymbolizer)) {
            throw new IllegalArgumentException("Cannot set symbolizer other than a line symbolizer");
        }

        this.symbolizer = (LineSymbolizer) symbolizer;

        geomChooser.setSelectedName(this.symbolizer.getGeometryPropertyName());
        strokeEditor.setStroke(this.symbolizer.getStroke());
    }

    public Symbolizer getSymbolizer() {
        if (geomChooser.isVisible()) {
            symbolizer.setGeometryPropertyName(geomChooser.getSelectedName());
        } else {
            symbolizer.setGeometryPropertyName(null);
        }

        symbolizer.setStroke(strokeEditor.getStroke());

        return symbolizer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        FormUtils.show(new DefaultLineSymbolizerEditor(null));
    }
}
