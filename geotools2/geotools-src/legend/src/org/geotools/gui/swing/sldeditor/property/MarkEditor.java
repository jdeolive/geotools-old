/*
 * Created on 15-feb-2004
 */
package org.geotools.gui.swing.sldeditor.property;

import javax.swing.JComponent;

import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.styling.Mark;

/**
 * @author wolf
 */
public abstract class MarkEditor extends JComponent implements SLDEditor {
    public abstract void setMark(Mark mark);
    public abstract Mark getMark();
}