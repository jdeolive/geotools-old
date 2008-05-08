package org.geotools.filter.expression;

import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.NilExpression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;

/**
 * Empty "abstract" implementation of ExpressionVisitor. Subclasses should
 * override desired methods.
 * 
 * @author Cory Horner, Refractions Research Inc.
 */
public class AbstractExpressionVisitor implements ExpressionVisitor {

    public Object visit(NilExpression expr, Object extraData) {
        return expr;
    }

    public Object visit(Add expr, Object extraData) {
        return expr;
    }

    public Object visit(Divide expr, Object extraData) {
        return expr;
    }

    public Object visit(Function expr, Object extraData) {
        return expr;
    }

    public Object visit(Literal expr, Object extraData) {
        return expr;
    }

    public Object visit(Multiply expr, Object extraData) {
        return expr;
    }

    public Object visit(PropertyName expr, Object extraData) {
        return expr;
    }

    public Object visit(Subtract expr, Object extraData) {
        return expr;
    }

}
