/*
 * Created on 15-feb-2004
 */
package org.geotools.gui.swing.sldeditor.property;

import javax.swing.JComponent;

import org.geotools.gui.swing.sldeditor.SLDEditor;

/**
 * @author wolf
 */
public abstract class FontListChooser extends JComponent implements SLDEditor {
    public abstract String[] getFontNames();
    public abstract void setFontNames(String[] fonts);
}