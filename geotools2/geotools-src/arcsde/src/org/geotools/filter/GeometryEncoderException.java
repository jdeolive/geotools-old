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
package org.geotools.filter;

/**
 * Indicates a client class has attempted to encode a filter not supported by
 * the GeometryEncoderSDE being used.
 *
 * @author Gabriel Roldán
 * @version $Id: GeometryEncoderException.java,v 1.3 2003/11/14 17:21:05 groldan Exp $
 */
public class GeometryEncoderException extends Exception
{
    /**
     * Creates a new GeometryEncoderException object.
     *
     * @param msg DOCUMENT ME!
     */
    public GeometryEncoderException(String msg)
    {
        this(msg, null);
    }

    /**
     * Creates a new GeometryEncoderException object.
     *
     * @param msg DOCUMENT ME!
     * @param cause DOCUMENT ME!
     */
    public GeometryEncoderException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
