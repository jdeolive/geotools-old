/*
 * Created on 15-feb-2004
 */
package org.geotools.gui.swing.sldeditor.property;

import javax.swing.JComponent;

import org.geotools.filter.Filter;
import org.geotools.gui.swing.sldeditor.SLDEditor;

/**
 * @author wolf
 */
public abstract class FilterEditor extends JComponent implements SLDEditor {
    /**
     * Returns the Expression typed by the user
     *
     * @return Returns either the Expression or the Filter parsed,
     * or null if no parseable content is present
     *
     * @throws ParseException if the expression contains
     */
    public abstract Filter getFilter();
    /**
     * Returns a formatted error message providing a better description of the problem encountered
     * while parsing the expression
     *
     * @return
     */
    public abstract String getFormattedErrorMessage();
    /**
     * @see org.geotools.gui.swing.sldeditor.basic.ExpressionEditor#setExpression
     */
    public abstract void setFilter(Filter filter);
}