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
package org.geotools.styling;

import org.geotools.filter.Expression;


/**
 * An AnchorPoint identifies the location inside a textlabel to use as an
 * "anchor" for positioning it relative to a point geometry. 
 * $Id: AnchorPoint.java,v 1.5 2003/08/19 13:03:03 ianturton Exp $
 *
 * @author Ian Turton
 */
public interface AnchorPoint {
    
    /** get the x coordinate of the anchor point
     * @return the expression which represents the X coordinate
     */    
    Expression getAnchorPointX();

    /** set the X coordinate for the anchor point
     * @param x an expression which represents the X coordinate
     */    
    void setAnchorPointX(Expression x);

    /** get the y coordinate of the anchor point
     * @return the expression which represents the Y coordinate
     */   
    Expression getAnchorPointY();

    /** set the Y coordinate for the anchor point
     * @param y an expression which represents the Y coordinate
     */    
    void setAnchorPointY(Expression y);

    /** calls the visit method of a StyleVisitor
     *
     * @param visitor the style visitor
     */    
    void accept(StyleVisitor visitor);
}
