/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.geotools.filter;

import org.apache.log4j.Logger;
import java.io.Writer;

/**
 * Exports a filter as a OGC Filter document.
 *
 * @task TODO: Support Null filters
 * @task TODO: Support Between filters
 * @task TODO: Support Like filters
 * @task TODO: Fully support GeometryExpressions
 * @author  jamesm
 */
public class XMLEncoder implements org.geotools.filter.FilterVisitor {
    
    private static Logger log = Logger.getLogger("filter");
    private Writer out;
    
    /** Creates a new instance of XMLEncoder */
    public XMLEncoder(Writer out) {
        this.out = out;
    }
    
    public void visit(AbstractFilter filter) {
        log.warn("exporting unknown filter type");
    }
    
    public void visit(BetweenFilter filter) {
        log.debug("exporting BetweenFilter");
    }
    
    public void visit(LogicFilter filter){
        log.debug("exporting LogicFilter");
 
        filter.getFilterType();
      
        String type = (String)logical.get(new Integer(filter.getFilterType()));
        try{
            out.write("<"+type+">\n");
            java.util.Iterator list = filter.getFilterIterator();
            while(list.hasNext()){
                ((AbstractFilter)list.next()).accept(this);
            }
            out.write("</"+type+">\n");
        }
        catch(java.io.IOException ioe){
            log.error("Unable to export filter",ioe);
        }
    }
    
    public void visit(CompareFilter filter){
        log.debug("exporting ComparisonFilter");
        
        ExpressionDefault left = (ExpressionDefault)filter.getLeftValue();
        ExpressionDefault right = (ExpressionDefault)filter.getRightValue();
        log.debug("Filter type id is "+filter.getFilterType());
        log.debug("Filter type text is "+comparisions.get(new Integer(filter.getFilterType())));
        String type = (String)comparisions.get(new Integer(filter.getFilterType()));
        try{
            out.write("<"+type+">\n");
            left.accept(this);
            right.accept(this);
            out.write("</"+type+">\n");
        }
        catch(java.io.IOException ioe){
            log.error("Unable to export filter",ioe);
        }
    }
    
    public void visit(GeometryFilter filter){
        log.debug("exporting GeometryFilter");
        ExpressionDefault left = (ExpressionDefault)filter.getLeftGeometry();
        ExpressionDefault right = (ExpressionDefault)filter.getRightGeometry();
        String type = (String)spatial.get(new Integer(filter.getFilterType()));
        try{
            out.write("<"+type+">\n");
            left.accept(this);
            right.accept(this);
            out.write("</"+type+">\n");
        }
        catch(java.io.IOException ioe){
            log.error("Unable to export filter",ioe);
        }
    }
    
    public void visit(NullFilter filter) {
        log.debug("exporting NullFilter");
    }
    
    public void visit(LikeFilter filter) {
        log.debug("exporting NullFilter");
    }
    
    public void visit(ExpressionAttribute expression) {
        log.debug("exporting ExpressionAttribute");
        try{
            out.write("<PropertyName>"+expression.getAttributePath()+"</PropertyName>\n");
        }
        catch(java.io.IOException ioe){
            log.error("Unable to export expresion",ioe);
        }
    }
    
    public void visit(ExpressionDefault expression) {
        log.warn("exporting unknown (default) expression");
    }
    
    public void visit(ExpressionLiteral expression) {
        log.debug("exporting LiteralExpression");
        try{
            out.write("<Literal>"+expression.getLiteral()+"</Literal>\n");
        }
        catch(java.io.IOException ioe){
            log.error("Unable to export expresion",ioe);
        }
    }
    
    public void visit(ExpressionMath expression) {
        log.debug("exporting Expression Math");
        
        String type = (String)expressions.get(new Integer(expression.getType()));
        try{
            out.write("<"+type+">\n");
            ((ExpressionDefault)expression.getLeftValue()).accept(this);
            ((ExpressionDefault)expression.getRightValue()).accept(this);
            out.write("</"+type+">\n");
        }
        catch(java.io.IOException ioe){
            log.error("Unable to export expresion",ioe);
        }
    }
    
    private static java.util.HashMap comparisions = new java.util.HashMap();
    private static java.util.HashMap spatial = new java.util.HashMap();
    private static java.util.HashMap logical = new java.util.HashMap();
    
    private static java.util.HashMap expressions = new java.util.HashMap();
    
    static{
        comparisions.put(new Integer(AbstractFilter.COMPARE_EQUALS),"PropertyIsEqualTo");
        comparisions.put(new Integer(AbstractFilter.COMPARE_GREATER_THAN),"PropertyIsGreaterThan");
        comparisions.put(new Integer(AbstractFilter.COMPARE_GREATER_THAN_EQUAL),"PropertyIsGreaterThanOrEqualTo");
        comparisions.put(new Integer(AbstractFilter.COMPARE_LESS_THAN),"PropertyIsLessThan");
        comparisions.put(new Integer(AbstractFilter.COMPARE_LESS_THAN_EQUAL),"PropertyIsLessThanOrEqualTo");
        comparisions.put(new Integer(AbstractFilter.LIKE),"PropertyIsLike");
        comparisions.put(new Integer(AbstractFilter.NULL),"PropertyIsNull");
        comparisions.put(new Integer(AbstractFilter.BETWEEN),"PropertyIsBetween");
        
        expressions.put(new Integer(ExpressionDefault.MATH_ADD),"Add");
        expressions.put(new Integer(ExpressionDefault.MATH_DIVIDE),"Div");
        expressions.put(new Integer(ExpressionDefault.MATH_MULTIPLY),"Mul");
        expressions.put(new Integer(ExpressionDefault.MATH_SUBTRACT),"Sub");
        //more to come
        
        spatial.put(new Integer(AbstractFilter.GEOMETRY_EQUALS),"Equals");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_DISJOINT),"Disjoint");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_INTERSECTS),"Intersects");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_TOUCHES),"Touches");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_CROSSES),"Crosses");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_WITHIN),"Within");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_CONTAINS),"Contains");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_OVERLAPS),"Overlaps");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_BEYOND),"Beyond");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_BBOX),"BBOX");
        
        logical.put(new Integer(AbstractFilter.LOGIC_AND),"And");
        logical.put(new Integer(AbstractFilter.LOGIC_OR),"Or");
        logical.put(new Integer(AbstractFilter.LOGIC_NOT),"Not");
        
    }
    
}
