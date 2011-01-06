/**************************************************************************************************
 **
 ** $Id: MismatchedDimensionException.java 1163 2008-01-17 12:18:10Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M1/geoapi/src/main/java/org/opengis/geometry/MismatchedDimensionException.java $
 **
 ** Copyright (C) 2003-2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.geometry;


/**
 * Indicates that an operation cannot be completed properly because
 * of a mismatch in the dimensions of object attributes.
 *
 * @author Martin Desruisseaux (IRD)
 * @since GeoAPI 1.0
 */
public class MismatchedDimensionException extends IllegalArgumentException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3138484331425225155L;

    /**
     * Creates an exception with no message.
     */
    public MismatchedDimensionException() {
        super();
    }

    /**
     * Creates an exception with the specified message.
     *
     * @param  message The detail message. The detail message is saved for
     *         later retrieval by the {@link #getMessage()} method.
     */
    public MismatchedDimensionException(final String message) {
        super(message);
    }

    /**
     * Creates an exception with the specified message and cause.
     *
     * @param  message The detail message. The detail message is saved for
     *         later retrieval by the {@link #getMessage()} method.
     * @param  cause The cause.
     */
    public MismatchedDimensionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
