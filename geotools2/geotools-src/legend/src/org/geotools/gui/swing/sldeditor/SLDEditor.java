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
package org.geotools.gui.swing.sldeditor;

import org.geotools.filter.FilterFactory;
import org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory;
import org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerEditorFactory;
import org.geotools.gui.swing.sldeditor.util.StyleCloner;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;


/**
 * DOCUMENT ME!
 *
 * @author jianhuij
 */
public interface SLDEditor {
    public static StyleFactory styleFactory = StyleFactory.createStyleFactory();
    public static PropertyEditorFactory propertyEditorFactory = PropertyEditorFactory.createPropertyEditorFactory();
    public static SymbolizerEditorFactory symbolizerEditorFactory = SymbolizerEditorFactory.createPropertyEditorFactory();
    public static FilterFactory filterFactory = FilterFactory.createFilterFactory();
    public static StyleCloner styleCloner = new StyleCloner(styleFactory);
    public static StyleBuilder styleBuilder = new StyleBuilder(styleFactory, filterFactory);
}
