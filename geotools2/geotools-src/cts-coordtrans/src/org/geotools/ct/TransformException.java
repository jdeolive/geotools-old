/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
 * Common superclass for a number of transformation-related exceptions.
 * <code>TransformException</code> are thrown by {@link MathTransform} when a coordinate
 * transformation can't be {@linkplain MathTransform#inverse inverted}
 * ({@link NoninvertibleTransformException}), when the
 * {@linkplain MathTransform#derivative derivative} can't be computed or when a coordinate
 * can't be {@linkplain MathTransform#transform(CoordinatePoint,CoordinatePoint) transformed}.
 * It is also thrown when {@link CoordinateTransformationFactory} fails to find a path
 * between two coordinate systems.
 *
 * @version $Id: TransformException.java,v 1.3 2003/05/13 10:58:48 desruisseaux Exp $
 * @author André Gosselin
 * @author Martin Desruisseaux
 *
 * @see MathTransform#inverse
 * @see MathTransform#derivative
 * @see MathTransform#transform(CoordinatePoint,CoordinatePoint)
 * @see CoordinateTransformationFactory#createFromCoordinateSystems
 */
public class TransformException extends Exception {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -6311418979456076140L;
    
    /**
     * Constructs a new exception with no detail message.
     */
    public TransformException() {
    }
    
    /**
     * Constructs a new exception with the specified detail message.
     */
    public TransformException(final String message) {
        super(message);
    }
    
    /**
     * Constructs a new exception with the specified detail message and cause.
     */
    public TransformException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
