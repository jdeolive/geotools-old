/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
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
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.ct;


/**
 * Transforms one-dimensional coordinate points.
 * {@link CoordinateTransformation#getMathTransform} may returns instance of this
 * interface when source and destination coordinate systems are both one dimensional.
 * <code>MathTransform1D</code> extends {@link MathTransform} by adding a simple method
 * transforming a value without the overhead of creating data array.
 *
 * @version $Id: MathTransform1D.java,v 1.3 2003/05/13 10:58:48 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public interface MathTransform1D extends MathTransform {
    /**
     * The one dimensional identity transform.
     */
    public static final MathTransform1D IDENTITY = IdentityTransform1D.ONE;

    /**
     * Transforms the specified value.
     *
     * @param value The value to transform.
     * @return the transformed value.
     * @throws TransformException if the value can't be transformed.
     */
    public abstract double transform(final double value) throws TransformException;
    
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
    public abstract double derivative(final double value) throws TransformException;
}
