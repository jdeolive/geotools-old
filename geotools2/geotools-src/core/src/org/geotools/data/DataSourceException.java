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
package org.geotools.data;

/**
 * Thrown when there is an error in a datasource.
 *
 * @author Ray Gallagher
 * @version $Id: DataSourceException.java,v 1.6 2003/05/16 15:49:06 jmacgill Exp $
 */
public class DataSourceException extends Exception {
    /**
     * Constructs a new instance of DataSourceException
     *
     * @param msg A message explaining the exception
     */
    public DataSourceException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new instance of DataSourceException
     *
     * @param msg A message explaining the exception
     * @param exp the throwable object which caused this exception
     */
    public DataSourceException(String msg, Throwable exp) {
        super(msg, exp);
    }
}
