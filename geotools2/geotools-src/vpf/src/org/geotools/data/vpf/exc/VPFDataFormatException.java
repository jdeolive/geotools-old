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
package org.geotools.data.vpf.exc;

/**
 * Class VPFDataFormatException.java is responsible for
 *
 * <p>
 * Created: Wed Jan 29 10:28:53 2003
 * </p>
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: VPFDataFormatException.java,v 1.1 2003/06/15 11:42:07 kobit Exp $
 */
public class VPFDataFormatException extends RuntimeException {
    /**
     * Creates a new VPFDataFormatException object.
     */
    public VPFDataFormatException() {
        super();
    }

    /**
     * Creates a new VPFDataFormatException object.
     *
     * @param message DOCUMENT ME!
     */
    public VPFDataFormatException(String message) {
        super(message);
    }

    /**
     * Creates a new VPFDataFormatException object.
     *
     * @param message DOCUMENT ME!
     * @param cause DOCUMENT ME!
     */
    public VPFDataFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new VPFDataFormatException object.
     *
     * @param cause DOCUMENT ME!
     */
    public VPFDataFormatException(Throwable cause) {
        super(cause);
    }
}


// VPFDataFormatException
