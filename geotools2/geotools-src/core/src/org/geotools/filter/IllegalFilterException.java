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
 * Defines an exception for illegal filters.
 *
 * @author Rob Hranac, Vision for New York
 * @author Chris Holmes, TOPP
 * @version $Id: IllegalFilterException.java,v 1.4 2003/08/07 21:30:36 cholmesny Exp $
 */
public class IllegalFilterException extends Exception {
    /**
     * Constructor with a message.
     *
     * @param message information on the error.
     */
    public IllegalFilterException(String message) {
        super(message);
    }

    /**
     * Constructs an instance of <code>IllegalFilterException</code> with the
     * specified root cause.
     *
     * @param cause the root cause of the exceptions.
     */
    public IllegalFilterException(Exception cause) {
        super(cause);
    }

    /**
     * Constructs an instance of <code>IllegalFilterException</code> with the
     * specified detail message and root cause.
     *
     * @param msg the detail message.
     * @param cause the root cause of the exceptions.
     */
    public IllegalFilterException(String msg, Exception cause) {
        super(msg, cause);
    }
}
