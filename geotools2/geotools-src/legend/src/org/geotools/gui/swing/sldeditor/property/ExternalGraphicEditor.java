/*
 * Created on 15-feb-2004
 */
package org.geotools.gui.swing.sldeditor.property;

import javax.swing.JComponent;

import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.styling.ExternalGraphic;

/**
 * @author wolf
 */
public abstract class ExternalGraphicEditor extends JComponent implements SLDEditor {
    public abstract void setExternalGraphic(ExternalGraphic externalGraphic);
    public abstract ExternalGraphic getExternalGraphic();
}