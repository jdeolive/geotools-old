/*
 * FilterVisitor.java
 *
 * Created on 22 July 2002, 16:50
 */

package org.geotools.filter;

/**
 *
 * @author  jamesm
 */
public interface FilterVisitor {
    
   void visit(AbstractFilter filter);
   void visit(BetweenFilter filter);
   void visit(CompareFilter filter);
   void visit(GeometryFilter filter);
   void visit(LikeFilter filter);
   void visit(LogicFilter filter);
   void visit(NullFilter filter);
   
   void visit(ExpressionAttribute expression);
   void visit(ExpressionDefault expression);
   void visit(ExpressionLiteral expression);
   void visit(ExpressionMath expression);
   void visit(BBoxExpression expression);
    
}
