/*
 * Created on 15-feb-2004
 */
package org.geotools.gui.swing.sldeditor.property;

import javax.swing.JComponent;

import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.styling.Fill;

/**
 * @author wolf
 */
public abstract class FillEditor extends JComponent implements SLDEditor {
    public abstract void setFill(Fill fill);
    public abstract Fill getFill();
}