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

/**
 * An interface for classes that want to perform operations on a Filter
 * hiarachy.
 * It forms part of a GoF Visitor Patern implementation.
 * A call to filter.accept(FilterVisitor) will result in a call to one
 * of the methods in this interface. The responsibility for traversing
 * sub filters is intended to lie with the visitor (this is unusual, but
 * permited under the Visitor pattern).
 *
 * A typical use would be to transcribe a filter into a specific format,
 * e.g. XML or SQL.  Alternativly it may be to extract specific infomration
 * from the Filter strucure, for example a list of all bboxes.
 *
 * @version $Id: FilterVisitor.java,v 1.5 2002/10/25 11:38:30 ianturton Exp $
 * @author James Macgill
 */
public interface FilterVisitor {
    
    /**
     * Called when accept is called on an AbstractFilter.
     * As it is imposible to create an instance of AbstractFilter this should
     * never happen.  If it does it means that a subclass of AbstractFilter
     * has failed to implement accept(FilterVisitor) correctly.
     * Implementers of this method should probaly log a warning.
     *
     * @param filter The filter to visit
     */
    void visit(Filter filter);
    
    /**
     * Called when accept is called on a BetweenFilter.
     * Implementers will want to access the left, middle and right expresions.
     *
     * @param filter The filter to visit
     */ 
    void visit(BetweenFilter filter);
    void visit(CompareFilter filter);
    void visit(GeometryFilter filter);
    void visit(LikeFilter filter);
    void visit(LogicFilter filter);
    void visit(NullFilter filter);
    
    void visit(AttributeExpression expression);
    void visit(Expression expression);
    void visit(LiteralExpression expression);
    void visit(MathExpression expression);
    
    
}
