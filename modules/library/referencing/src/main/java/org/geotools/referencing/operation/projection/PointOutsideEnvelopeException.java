/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *
 *   (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *   (C) 2001, Institut de Recherche pour le Développement
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
 */
package org.geotools.referencing.operation.projection;


/**
 * Thrown by {@link MapProjection} when a map projection failed because the point is
 * outside the envelope of validity. Bounds are usually 90°S to 90°N and 180°W to 180°E.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class PointOutsideEnvelopeException extends ProjectionException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4384490413772200352L;

    /**
     * Constructs a new exception with no detail message.
     */
    public PointOutsideEnvelopeException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message The error message.
     */
    public PointOutsideEnvelopeException(final String message) {
        super(message);
    }
}
