/*
 * Created on 16-feb-2004
 *
 */
package org.geotools.gui.swing.sldeditor.property;

import javax.swing.JComponent;

import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.styling.Symbol;

/**
 * @author wolf
 */
public abstract class SymbolEditor extends JComponent implements SLDEditor {
    public abstract void setSymbol(Symbol symbol);
    public abstract Symbol getSymbol();
}