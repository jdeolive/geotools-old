/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.styling;

import org.geotools.filter.ConstantExpression;
import org.opengis.filter.expression.Expression;


/**
 * A Displacement gives X and Y offset displacements to use for rendering a
 * text label near a point.
 *
 *
 * @author Ian Turton, CCG
 * @version $Id$
 * @source $URL$
 */
public interface Displacement {
    /**
     * Default Displacment instance.
     */
    static final Displacement DEFAULT = new ConstantDisplacement() {
            public Expression getDisplacementX() {
                return ConstantExpression.ZERO;
            }

            public Expression getDisplacementY() {
                return ConstantExpression.ZERO;
            }
        };

    /**
     * Null Displacement instance.
     */
    static final Displacement NULL = new ConstantDisplacement() {
            public Expression getDisplacementX() {
                return ConstantExpression.NULL;
            }

            public Expression getDisplacementY() {
                return ConstantExpression.NULL;
            }
        };

    //TODO: add Displacement to GeoAPI
    /**
     * Returns an expression that computes a pixel offset from the geometry
     * point.  This offset point is where the text's anchor point gets
     * located. If this expression is null, the default offset of zero is
     * used.
     *
     * @return DOCUMENT ME!
     */
    Expression getDisplacementX();

    /**
     * Sets the expression that computes a pixel offset from the geometry
     * point.
     *
     * @param x DOCUMENT ME!
     */
    void setDisplacementX(Expression x);

    /**
     * Returns an expression that computes a pixel offset from the geometry
     * point.  This offset point is where the text's anchor point gets
     * located. If this expression is null, the default offset of zero is
     * used.
     *
     * @return DOCUMENT ME!
     */
    Expression getDisplacementY();

    /**
     * Sets the expression that computes a pixel offset from the geometry
     * point.
     *
     * @param y DOCUMENT ME!
     */
    void setDisplacementY(Expression y);

    void accept(StyleVisitor visitor);
}


abstract class ConstantDisplacement implements Displacement {
    private void cannotModifyConstant() {
        throw new UnsupportedOperationException("Constant Displacement may not be modified");
    }

    public void setDisplacementX(Expression x) {
        cannotModifyConstant();
    }

    public void setDisplacementY(Expression y) {
        cannotModifyConstant();
    }

    public void accept(StyleVisitor visitor) {
        cannotModifyConstant();
    }
}
;
