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

package org.geotools.filter;

import com.vividsolutions.jts.geom.Envelope;
import java.io.*;
import java.util.EmptyStackException;
import java.util.Stack;
import org.geotools.filter.*;
import org.geotools.filter.parser.*;

/**
 * ExpressionBuilder is the main entry point for parsing Expressions and Filters
 * from a non-xml language.
 * 
 * @author  Ian Schneider
 */
public class ExpressionBuilder {
    
    /**
     * Parse the input string into either a Filter or an Expression.
     */
    public static Object parse(String input) throws ParseException {
        ExpressionCompiler c = new ExpressionCompiler(new StringReader(input));
        try {
            c.CompilationUnit();
        } catch (TokenMgrError tme) {
            throw new ExpressionException(tme.getMessage(),c.getToken(0));
        }
        if (c.exception != null)
            throw c.exception;
        
        StackItem item = (StackItem) c.stack.peek();
        return item.built;
        
    }
    
    /**
     * Returns a formatted error string, showing the original input, along with
     * a pointer to the location of the error and the error message itself.
     */ 
    public static String getFormattedErrorMessage(ParseException pe,String input) {
        StringBuffer sb = new StringBuffer(input);
        sb.append('\n');
        int column = pe.currentToken.beginColumn - 1;
        for (int i = 0; i < column; i++) {
            sb.append(' ');
        }
        sb.append('^').append('\n');
        sb.append(pe.getMessage());
        return sb.toString();
    }
    
    static class ExpressionCompiler extends ExpressionParser implements ExpressionParserTreeConstants {
        Stack stack = new Stack();
        FilterFactory factory = FilterFactory.createFilterFactory();
        ExpressionException exception = null;
        public ExpressionCompiler(Reader r) {
            super(r);
        }
        
        StackItem popStack() {
            return (StackItem) stack.pop();
        }
        
        Expression expression() throws ExpressionException {
            StackItem item = null;
            try {
                item = popStack();
                return (Expression) item.built;
            } catch (ClassCastException cce) {
                throw new ExpressionException("Current node not expression",item.token);
            } catch (EmptyStackException ese) {
                throw new ExpressionException("No items on stack",getToken(0));
            }
        }
        
        Filter filter() throws ExpressionException {
            StackItem item = null;
            try {
                item = popStack();
                return (Filter) item.built;
            } catch (ClassCastException cce) {
                throw new ExpressionException("Current node not filter",item.token);
            } catch (EmptyStackException ese) {
                throw new ExpressionException("No items on stack",getToken(0));
            }
        }
        
        protected double doubleValue() throws ExpressionException {
            try {
                return ((Number) expression().getValue(null)).doubleValue();
            } catch (ClassCastException cce) {
                throw new ExpressionException("Current node not double",getToken(0));
            }
        }
        
        protected int intValue() throws ExpressionException {
            try {
                return ((Number) expression().getValue(null)).intValue();
            } catch (ClassCastException cce) {
                throw new ExpressionException("Current node not int",getToken(0));
            }
        }
        
        protected String stringValue() throws ExpressionException {
            return expression().getValue(null).toString();
        }
        
        public void jjtreeOpenNodeScope(Node n) {
        }
        
        public void jjtreeCloseNodeScope(Node n) throws ParseException {
            try {
                Object built = buildObject(n);
                if (built == null) throw new RuntimeException("INTERNAL ERROR : Node " + n + " resulted in null build");
                stack.push(new StackItem(built,getToken(0)));
            } finally {
                n.dispose();
            }
        }
        
        protected String token() {
            return getToken(0).image;
        }
        
        protected MathExpression mathExpression(short type) throws ExpressionException {
            try {
                MathExpression e = factory.createMathExpression(type);
                Expression right = expression();
                Expression left = expression();
                e.addLeftValue(left);
                e.addRightValue(right);
                return e;
            } catch (IllegalFilterException ife) {
                throw new ExpressionException("Exception building MathExpression",getToken(0),ife);
            }
        }
        
        protected LogicFilter logicFilter(short type) throws ExpressionException {
            try {
                Filter right = filter();
                Filter left = filter();
                return factory.createLogicFilter(left,right,type);
            } catch (IllegalFilterException ife) {
                throw new ExpressionException("Exception building LogicFilter",getToken(0),ife);
            }
        }
        
        protected CompareFilter compareFilter(short type) throws ExpressionException {
            try {
                CompareFilter f = factory.createCompareFilter(type);
                Expression right = expression();
                Expression left = expression();
                f.addLeftValue(left);
                f.addRightValue(right);
                return f;
            } catch (IllegalFilterException ife) {
                throw new ExpressionException("Exception building CompareFilter",getToken(0),ife);
            }
        }
        
        protected Object buildObject(Node n) throws ExpressionException {
            short type;
            switch (n.getType()) {
                
                // Literals
                // note, these should never throw because the parser grammar
                // constrains input before we ever reach here!
                case JJTINTEGERNODE:
                    return factory.createLiteralExpression(Integer.parseInt(token()));
                case JJTFLOATINGNODE:
                    return factory.createLiteralExpression(Double.parseDouble(token()));
                case JJTSTRINGNODE:
                    return factory.createLiteralExpression(n.getToken().image);
                case JJTATTNODE:
                    try {
                        return factory.createAttributeExpression(null,token());
                    } catch (IllegalFilterException ife) {
                        throw new ExpressionException("Exception building AttributeExpression",getToken(0),ife);
                    }
                case JJTFUNCTIONNODE:
                    return parseFunction(n);
                    
                    
                    // Math Nodes
                case JJTADDNODE:
                    return mathExpression(DefaultExpression.MATH_ADD);
                case JJTSUBTRACTNODE:
                    return mathExpression(DefaultExpression.MATH_SUBTRACT);
                case JJTMULNODE:
                    return mathExpression(DefaultExpression.MATH_MULTIPLY);
                case JJTDIVNODE:
                    return mathExpression(DefaultExpression.MATH_DIVIDE);
                    
                    
                    // Logic Nodes
                case JJTORNODE:
                    return logicFilter(AbstractFilter.LOGIC_OR);
                case JJTANDNODE:
                    return logicFilter(AbstractFilter.LOGIC_AND);
                case JJTNOTNODE:
                    return filter().not();
                    
                    
                    // Compare Nodes
                case JJTLENODE:
                    return compareFilter(AbstractFilter.COMPARE_LESS_THAN_EQUAL);
                case JJTLTNODE:
                    return compareFilter(AbstractFilter.COMPARE_LESS_THAN);
                case JJTGENODE:
                    return compareFilter(AbstractFilter.COMPARE_GREATER_THAN_EQUAL);
                case JJTGTNODE:
                    return compareFilter(AbstractFilter.COMPARE_GREATER_THAN);
                case JJTEQNODE:
                    return compareFilter(AbstractFilter.COMPARE_EQUALS);
                case JJTNENODE:
                    return compareFilter(AbstractFilter.COMPARE_NOT_EQUALS);
                    
                    // Unsupported for now
                case JJTTRUENODE:
                case JJTFALSENODE:
                    throw new ExpressionException("Unsupported syntax",getToken(0));
            }
            System.out.println("DUDE : " + n);
            return null;
        }
        
        protected Object parseFunction(Node n) throws ExpressionException {
            String function = n.getToken().image;
            if ("box".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 4) {
                    throw new ExpressionException("Bounding Box filter requires 4 arguments",getToken(0));
                }
                
                double d4 = doubleValue();
                double d3 = doubleValue();
                double d2 = doubleValue();
                double d1 = doubleValue();
                try {
                    return factory.createBBoxExpression(new Envelope(
                    d1,d2,d3,d4
                    ));
                } catch (IllegalFilterException ife) {
                    throw new ExpressionException("Exception building BBoxExpression",getToken(0),ife);
                }
            } else if ("id".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 1) {
                    throw new ExpressionException("Feature ID filter requires 1 argument",getToken(0));
                }
                return factory.createFidFilter(stringValue());
            } else if ("between".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 3) {
                    throw new ExpressionException("Between filter requires 3 arguments",getToken(0));
                }
                Expression two = expression();
                Expression att = expression();
                Expression one = expression();
                try {
                    BetweenFilter b = factory.createBetweenFilter();
                    b.addLeftValue(one);
                    b.addMiddleValue(att);
                    b.addRightValue(two);
                    return b;
                } catch (IllegalFilterException ife) {
                    throw new ExpressionException("Exception building BetweenFilter",getToken(0),ife);
                }
                
            } else if ("like".equalsIgnoreCase(function)) {
                if (n.jjtGetNumChildren() != 2) {
                    throw new ExpressionException("Like filter requires two argument",getToken(0));
                }
                LikeFilter f = factory.createLikeFilter();
                f.setPattern(stringValue(), "*", ".?", "//");
                try {
                    f.setValue(expression());
                } catch (IllegalFilterException ife) {
                    throw new ExpressionException("Exception building LikeFilter",getToken(0),ife);
                }
                return f;
            }
            
            throw new ExpressionException("Could not build function : " + function,getToken(0));
        }
        
    }
    
    static class StackItem {
        Object built;
        Token token;
        public StackItem(Object b,Token t) {
            built = b;
            token = t;
        }
    }
    
    
}
