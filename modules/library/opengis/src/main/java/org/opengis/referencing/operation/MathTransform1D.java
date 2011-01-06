/*$************************************************************************************************
 **
 ** $Id: MathTransform1D.java 1265 2008-07-09 18:24:37Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi/src/main/java/org/opengis/referencing/operation/MathTransform1D.java $
 **
 ** Copyright (C) 2003-2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.referencing.operation;

import org.opengis.annotation.Extension;


/**
 * Transforms one-dimensional coordinate points.
 * {@link CoordinateOperation#getMathTransform} may returns instance of this
 * interface when source and destination coordinate systems are both one dimensional.
 * {@code MathTransform1D} extends {@link MathTransform} by adding a simple method
 * transforming a value without the overhead of creating data array.
 *
 * @author  Martin Desruisseaux (IRD)
 * @since   GeoAPI 1.0
 */
@Extension
public interface MathTransform1D extends MathTransform {
    /**
     * Transforms the specified value.
     *
     * @param value The value to transform.
     * @return the transformed value.
     * @throws TransformException if the value can't be transformed.
     */
    double transform(double value) throws TransformException;

    /**
     * Gets the derivative of this function at a value. The derivative is the
     * 1&times;1 matrix of the non-translating portion of the approximate affine
     * map at the value.
     *
     * @param  value The value where to evaluate the derivative.
     * @return The derivative at the specified point.
     * @throws TransformException if the derivative can't be evaluated at the
     *         specified point.
     */
    double derivative(double value) throws TransformException;

    /**
     * Creates the inverse transform of this object.
     *
     * @return The inverse transform.
     * @throws NoninvertibleTransformException if the transform can't be inversed.
     *
     * @since GeoAPI 2.2
     */
    MathTransform1D inverse() throws NoninvertibleTransformException;
}
