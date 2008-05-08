/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
 *    (C) 1999, Fisheries and Oceans Canada
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
package org.geotools.measure;


/**
 * A latitude angle. Positive latitudes are North, while negative
 * latitudes are South. This class has no direct OpenGIS equivalent.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see Longitude
 * @see AngleFormat
 */
public final class Latitude extends Angle {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4496748683919618976L;

    /**
     * Minimum legal value for latitude (-90°).
     */
    public static final double MIN_VALUE = -90;

    /**
     * Maximum legal value for latitude (+90°).
     */
    public static final double MAX_VALUE = +90;

    /**
     * Contruct a new latitude with the specified value.
     *
     * @param theta Angle in degrees.
     */
    public Latitude(final double theta) {
        super(theta);
    }

    /**
     * Constructs a newly allocated {@code Latitude} object that
     * represents the latitude value represented by the string.   The
     * string should represents an angle in either fractional degrees
     * (e.g. 45.5°) or degrees with minutes and seconds (e.g. 45°30').
     * The hemisphere (N or S) is optional (default to North).
     *
     * @param  theta A string to be converted to a {@code Latitude}.
     * @throws NumberFormatException if the string does not contain a parsable latitude.
     */
    public Latitude(final String theta) throws NumberFormatException {
        super(theta);
    }
}
