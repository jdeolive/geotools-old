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
package org.geotools.factory;

/**
 * Deadly error. Usually if this is thrown, an application built upon geotools
 * will not be able to function. I make this an error so that the standard bad code<br>
 * <code> try { somthingRisky(); } catch (Exception e) {
 * logger.warning("something happened"); }</code> will be subverted and the
 * error will grind the application to a halt, as it should.
 *
 * @author Ian Schneider
 * @version $Id: FactoryConfigurationError.java,v 1.3 2003/07/30 19:34:49 jmacgill Exp $
 *
 * @see java.lang.Error
 */
public class FactoryConfigurationError extends Error {
    /**
     * Creates a new instance of FactoryConfigurationError
     *
     * @param message Informative statememt about what whent wrong
     */
    public FactoryConfigurationError(String message) {
        super(message);
    }

    /**
     * Creates a new instance of FactoryConfigurationError
     *
     * @param message Informative statememt about what whent wrong
     * @param cause The origional exception which caused the problem
     */
    public FactoryConfigurationError(String message, Throwable cause) {
        super(message, cause);
    }
}
