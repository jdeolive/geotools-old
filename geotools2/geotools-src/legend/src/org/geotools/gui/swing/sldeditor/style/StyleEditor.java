/*
 * Created on 21-dic-2003
 *
 */
package org.geotools.gui.swing.sldeditor.style;

import org.geotools.styling.Style;

/**
 * Interface implemented by components that can create and edit a style.
 * It is also required that an object implementing the StyleEditor interface
 * has a no argument public constructor (the so called "default constructor")
 * 
 * @author wolf
 */
public interface StyleEditor {
    /**
     * Returns the style edited thru the style editor
     * @return
     */
    public Style getStyle();
    
    /**
     * Sets the style to be edited. The style won't be cloned, every change
     * occurred in the editor will be reflected in the same object when the
     * getStyle() method will be called. If canEdit(s) returns false, an attempt
     * to use anyway this style will be made, but the style returned by getStyle()
     * may contain only part of the original information, or none at all
     * @param s
     */
    public void setStyle(Style s);
    
    /**
     * Returns true if this style editor is able to completely and properly
     * edit the passed style (for example, a single rule editor cannot edit
     * a style with multiple FeatureTypeStyles or multiple rules)
     * @param s
     * @return
     */
    public boolean canEdit(Style s);
}
