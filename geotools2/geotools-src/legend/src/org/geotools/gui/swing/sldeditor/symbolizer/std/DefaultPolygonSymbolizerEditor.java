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

import java.awt.BorderLayout;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.gui.swing.sldeditor.property.FillEditor;
import org.geotools.gui.swing.sldeditor.property.GeometryChooser;
import org.geotools.gui.swing.sldeditor.property.StrokeEditor;
import org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Symbolizer;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DefaultPolygonSymbolizerEditor extends SymbolizerEditor implements SLDEditor {
    PolygonSymbolizer symbolizer;
    JLabel lblGeometry;
    GeometryChooser geomChooser;
    FillEditor fillEditor;
    StrokeEditor strokeEditor;

    /**
     * Creates a new instance of PolygonSymbolizerEditor
     */
    public DefaultPolygonSymbolizerEditor(FeatureType ft) {
        this(ft, styleBuilder.createPolygonSymbolizer());
    }

    public DefaultPolygonSymbolizerEditor(FeatureType ft, PolygonSymbolizer symbolizer) {
        lblGeometry = new JLabel("Geometry property");
        geomChooser = propertyEditorFactory.createGeometryChooser(ft);
        fillEditor = propertyEditorFactory.createFillEditor(ft);
        strokeEditor = propertyEditorFactory.createStrokeEditor(ft);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Fill", fillEditor);
        tabbedPane.add("Stroke", strokeEditor);

        if (geomChooser.getGeomPropertiesCount() < 2) {
            setLayout(new BorderLayout());
            add(tabbedPane);
        } else {
            setLayout(new GridBagLayout());
            FormUtils.addRowInGBL(this, 0, 0, lblGeometry, geomChooser);
            FormUtils.addFiller(this, 1, 0, tabbedPane);
        }

        setSymbolizer(symbolizer);
    }

    public void setSymbolizer(Symbolizer symbolizer) {
        if (!(symbolizer instanceof PolygonSymbolizer)) {
            throw new IllegalArgumentException(
                "Cannot set symbolizer other than a polygon symbolizer");
        }

        this.symbolizer = (PolygonSymbolizer) symbolizer;

        geomChooser.setSelectedName(this.symbolizer.getGeometryPropertyName());
        fillEditor.setFill(this.symbolizer.getFill());
        strokeEditor.setStroke(this.symbolizer.getStroke());
    }

    public Symbolizer getSymbolizer() {
        if (geomChooser.isVisible()) {
            symbolizer.setGeometryPropertyName(geomChooser.getSelectedName());
        } else {
            symbolizer.setGeometryPropertyName(null);
        }

        symbolizer.setFill(fillEditor.getFill());
        symbolizer.setStroke(strokeEditor.getStroke());

        return symbolizer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        FormUtils.show(new DefaultPolygonSymbolizerEditor(null));
    }
}
