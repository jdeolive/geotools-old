/*
 * Created on 14-feb-2004
 */
package org.geotools.gui.swing.sldeditor.property;

import javax.swing.JComponent;

import org.geotools.filter.Expression;
import org.geotools.gui.swing.sldeditor.SLDEditor;

/**
 * @author wolf
 */
public abstract class ExpressionEditor extends JComponent implements SLDEditor {
    /**
     * Returns the Expression or the Filter typed by the user
     *
     * @return Returns either the Expression or the Filter parsed,
     * or null if no parseable content is presente
     *
     * @throws ParseException if the expression contains
     */
    public abstract Expression getExpression();
    /**
     * Sets the current expression. If the editor cannot edit
     * the passed expression, it will dumb it down to a simplified
     * version or use a default. Use @link ExpressionEditor#canEdit(Expression) 
     * to check if the expression can be fully edited with this editor 
     *
     * @param expression The expression to set.
     */
    public abstract void setExpression(Expression expression);
    
    /**
     * Returns true if this expression editor is able to edit
     * meaningfully an expression
     * @param expression
     * @return
     */
    public abstract boolean canEdit(Expression expression);
}