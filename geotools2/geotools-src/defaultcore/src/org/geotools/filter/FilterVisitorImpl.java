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
 * @version $Id: FilterVisitorImpl.java,v 1.2 2002/10/24 12:03:17 ianturton Exp $
 * @author James Macgill
 */
public interface FilterVisitorImpl extends FilterVisitor{
    /* TO DO - move to actual filterVisitor interface when all impls done */
    /**
     * Called when accept is called on an AbstractFilter.
     * As it is imposible to create an instance of AbstractFilter this should
     * never happen.  If it does it means that a subclass of AbstractFilter
     * has failed to implement accept(FilterVisitor) correctly.
     * Implementers of this method should probaly log a warning.
     *
     * @param filter The filter to visit
     */
    void visit(AbstractFilter filter);
    
    /**
     * Called when accept is called on a BetweenFilter.
     * Implementers will want to access the left, middle and right expresions.
     *
     * @param filter The filter to visit
     */ 
    void visit(BetweenFilterImpl filter);
    void visit(CompareFilterImpl filter);
    void visit(GeometryFilterImpl filter);
    void visit(LikeFilterImpl filter);
    void visit(LogicFilterImpl filter);
    void visit(NullFilterImpl filter);
    
    void visit(AttributeExpressionImpl expression);
    void visit(DefaultExpressionImpl expression);
    void visit(LiteralExpressionImpl expression);
    void visit(MathExpressionImpl expression);
    
    
}
