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

import java.awt.Component;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerChooserDialog;
import org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerEditor;
import org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerEditorFactory;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DefaultSymbolizerEditorFactory extends SymbolizerEditorFactory {
    /**
     * @see org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerEditorFactory#createLineSymbolizerEditor()
     */
    public SymbolizerEditor createLineSymbolizerEditor(FeatureType featureType) {
        return new DefaultLineSymbolizerEditor(featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerEditorFactory#createPointSymbolizerEditor()
     */
    public SymbolizerEditor createPointSymbolizerEditor(FeatureType featureType) {
        return new DefaultPointSymbolizerEditor(featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerEditorFactory#createPolygonSymbolizerEditor()
     */
    public SymbolizerEditor createPolygonSymbolizerEditor(FeatureType featureType) {
        return new DefaultPolygonSymbolizerEditor(featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerEditorFactory#createTextSymbolizerEditor()
     */
    public SymbolizerEditor createTextSymbolizerEditor(FeatureType featureType) {
        return new DefaultTextSymbolizerEditor(featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerEditorFactory#createRasterSymbolizerEditor()
     */
    public SymbolizerEditor createRasterSymbolizerEditor(FeatureType featureType) {
        throw new UnsupportedOperationException("Raster symbolizers are still unsupported");
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerEditorFactory#createSymbolizerChooserDialog(org.geotools.feature.FeatureType)
     */
    public SymbolizerChooserDialog createSymbolizerChooserDialog(Component parent, FeatureType featureType) {
        return new DefaultSymbolizerChooserDialog(parent, featureType);
    }
}
