/*
 * Created on 15-feb-2004
 */
package org.geotools.gui.swing.sldeditor.property;

import javax.swing.JComponent;

import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.styling.Graphic;

/**
 * @author wolf
 */
public abstract class GraphicEditor extends JComponent implements SLDEditor {
    public abstract void setGraphic(Graphic graphic);
    public abstract Graphic getGraphic();
}