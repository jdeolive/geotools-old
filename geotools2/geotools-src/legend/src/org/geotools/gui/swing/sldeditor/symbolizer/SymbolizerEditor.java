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
package org.geotools.gui.swing.sldeditor.symbolizer;

import javax.swing.JComponent;

import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.styling.Symbolizer;


/**
 * simple interface for Symbolizer editor to get a symolizer and set a symolizer in a symbolizer
 * editor
 *
 * @author jianhuij
 */
public abstract class SymbolizerEditor extends JComponent implements SLDEditor {
    public abstract Symbolizer getSymbolizer();

    public abstract void setSymbolizer(Symbolizer s);
}
