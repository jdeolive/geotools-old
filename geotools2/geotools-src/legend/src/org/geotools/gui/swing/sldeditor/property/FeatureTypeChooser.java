/*
 * Created on 15-feb-2004
 */
package org.geotools.gui.swing.sldeditor.property;

import javax.swing.JComponent;

import org.geotools.gui.swing.sldeditor.SLDEditor;

/**
 * @author wolf
 */
public abstract class FeatureTypeChooser extends JComponent implements SLDEditor {
    public abstract void setFeatureTypeName(String typeName);
    public abstract String getFeatureTypeName();
}