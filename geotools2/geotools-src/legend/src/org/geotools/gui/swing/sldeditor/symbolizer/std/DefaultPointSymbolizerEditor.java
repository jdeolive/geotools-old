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
 * PolygonSymbolizerEditor.java
 *
 * Created on 13 dicembre 2003, 17.22
 */
package org.geotools.gui.swing.sldeditor.symbolizer.std;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.gui.swing.sldeditor.property.GeometryChooser;
import org.geotools.gui.swing.sldeditor.property.GraphicEditor;
import org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Symbolizer;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DefaultPointSymbolizerEditor extends SymbolizerEditor implements SLDEditor {
    PointSymbolizer symbolizer;
    JLabel lblGeometry;
    GeometryChooser geomChooser;
    GraphicEditor graphicEditor;

    /**
     * Creates a new instance of PolygonSymbolizerEditor
     */
    public DefaultPointSymbolizerEditor(FeatureType featureType) {
        this(featureType, null);
    }

    public DefaultPointSymbolizerEditor(FeatureType ft, PointSymbolizer symbolizer) {
        if (symbolizer == null) {
            symbolizer = styleBuilder.createPointSymbolizer();
        }

        setLayout(new GridBagLayout());

        lblGeometry = new JLabel("Geometry property");
        geomChooser = propertyEditorFactory.createGeometryChooser(ft);
        graphicEditor = propertyEditorFactory.createGraphicEditor(ft);

        FormUtils.addRowInGBL(this, 0, 0, lblGeometry, geomChooser, 0.0, false);
        FormUtils.addFiller(this, 1, 0, graphicEditor, 1.0, false);

        if (geomChooser.getGeomPropertiesCount() < 2) {
            lblGeometry.setVisible(false);
            geomChooser.setVisible(false);
        }

        setSymbolizer(symbolizer);
    }

    public void setSymbolizer(Symbolizer symbolizer) {
        if (!(symbolizer instanceof PointSymbolizer)) {
            throw new IllegalArgumentException(
                "Cannot set symbolizer other than a point symbolizer");
        }

        this.symbolizer = (PointSymbolizer) symbolizer;

        geomChooser.setSelectedName(this.symbolizer.getGeometryPropertyName());
        graphicEditor.setGraphic(this.symbolizer.getGraphic());
    }

    public Symbolizer getSymbolizer() {
        if (geomChooser.isVisible()) {
            symbolizer.setGeometryPropertyName(geomChooser.getSelectedName());
        } else {
            symbolizer.setGeometryPropertyName(null);
        }

        symbolizer.setGraphic(graphicEditor.getGraphic());

        return symbolizer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        FormUtils.show(new DefaultPointSymbolizerEditor(null));
    }
}
