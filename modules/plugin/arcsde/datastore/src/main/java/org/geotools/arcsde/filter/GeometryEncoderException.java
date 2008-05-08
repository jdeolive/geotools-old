/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.arcsde.filter;

/**
 * Indicates a client class has attempted to encode a filter not supported by
 * the GeometryEncoderSDE being used.
 * 
 * @author Gabriel Roldan
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/filter/GeometryEncoderException.java $
 * @version $Id: GeometryEncoderException.java 27633 2007-10-26 10:33:20Z
 *          desruisseaux $
 */
public class GeometryEncoderException extends Exception {
    /**
     * Serial version uid
     */
    private static final long serialVersionUID = -8926292317791976269L;

    /**
     * Creates a new GeometryEncoderException object.
     * 
     * @param msg
     *            DOCUMENT ME!
     */
    public GeometryEncoderException(String msg) {
        this(msg, null);
    }

    /**
     * Creates a new GeometryEncoderException object.
     * 
     * @param msg
     *            DOCUMENT ME!
     * @param cause
     *            DOCUMENT ME!
     */
    public GeometryEncoderException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
