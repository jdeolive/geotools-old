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
/**
 *
 * @author  jamesm
 */
public class XMLEncoder implements org.geotools.filter.FilterVisitor {
     Document dom = new Document().
     private static Logger log = Logger.getLogger("filter");

    
    /** Creates a new instance of XMLEncoder */
    public XMLEncoder() {
    }
    
    public void visit(AbstractFilter filter) {
        log.warn("exporting unknown filter type");
    }
    
    public void visit(BetweenFilter filter) {
        log.debug("exporting BetweenFilter");
    }
    
    public void visit(LogicFilter filter){
        log.debug("exporting LogicFilter");
    }
    
    public void visit(CompareFilter filter){
        log.debug("exporting ComparisonFilter");
        
        ExpressionDefault left = (ExpressionDefault)filter.getLeftValue();
        ExpressionDefault right = (ExpressionDefault)filter.getRightValue();
        // write xml header
        left.accept(this);
        right.accept(this);
        //write closing xml
    }
    
    public void visit(GeometryFilter filter){
        log.debug("exporting GeometryFilter");
    }
    
    public void visit(NullFilter filter) {
        log.debug("exporting NullFilter");
    }
    
    public void visit(LikeFilter filter) {
        log.debug("exporting NullFilter");
    }
    
    public void visit(ExpressionAttribute expression) {
        log.debug("exporting ExpressionAttribute");
    }
    
    public void visit(ExpressionDefault expression) {
        log.warn("exporting unknown (default) expression");
    }
    
    public void visit(ExpressionLiteral expression) {
        log.debug("exporting LiteralExpression");
    }
    
    public void visit(ExpressionMath expression) {
        log.debug("exporting Expression Math");
    }
    
}
