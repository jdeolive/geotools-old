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
package org.geotools.data.arcsde;

/**
 * Exception that can be thrown if an error occurs while creating a
 * <code>Geometryy</code> from a <code>SeShape</code> or viceversa
 *
 * @author Gabriel Roldán
 * @version $Id: GeometryBuildingException.java,v 1.1 2004/03/11 00:17:09 groldan Exp $
 */
public class GeometryBuildingException extends Exception
{
    /**
     * Creates a new GeometryBuildingException object.
     *
     * @param msg DOCUMENT ME!
     */
    public GeometryBuildingException(String msg)
    {
        this(msg, null);
    }

    /**
     * Creates a new GeometryBuildingException object.
     *
     * @param msg DOCUMENT ME!
     * @param cause DOCUMENT ME!
     */
    public GeometryBuildingException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
