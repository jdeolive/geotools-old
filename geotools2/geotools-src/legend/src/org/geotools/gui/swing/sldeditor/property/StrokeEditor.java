/*
 * Created on 16-feb-2004
 *
 */
package org.geotools.gui.swing.sldeditor.property;

import javax.swing.JComponent;

import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.styling.Stroke;

/**
 * @author wolf
 */
public abstract class StrokeEditor extends JComponent implements SLDEditor {
    public abstract void setStroke(Stroke stroke);
    public abstract Stroke getStroke();
}