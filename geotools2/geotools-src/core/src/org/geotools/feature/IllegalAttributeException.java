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
package org.geotools.feature;

/**
 * Indicates client class has attempted to create an invalid feature.
 *
 * @author Rob Hranac, Vision for New York
 * @version $Id: IllegalAttributeException.java,v 1.4 2003/08/05 23:24:40 cholmesny Exp $
 */
public class IllegalAttributeException extends Exception {
    /** The expected attribute type. */
    private final AttributeType expected;

    /** The object that does not match the expected type. */
    private final Object invalid;

    /**
     * Constructor with message argument.
     *
     * @param message Reason for the exception being thrown
     */
    public IllegalAttributeException(String message) {
        super(message);
        expected = null;
        invalid = null;
    }

    /**
     * Constructor that makes the message given the expected and invalid.
     *
     * @param expected the expected AttributeType.
     * @param invalid the attribute that does not validate against expected.
     */
    public IllegalAttributeException(AttributeType expected, Object invalid) {
        this(expected, invalid, null);
    }

    /**
     * Constructor that makes the message given the expected and invalid,
     * along with the root cause.
     *
     * @param expected the expected AttributeType.
     * @param invalid the attribute that does not validate against expected.
     * @param cause the root cause of the error.
     */
    public IllegalAttributeException(AttributeType expected, Object invalid,
        Throwable cause) {
        super(errorMessage(expected, invalid), cause);
        this.expected = expected;
        this.invalid = invalid;
    }

    /**
     * Constructs an error message based on expected and invalid.
     *
     * @param expected the expected AttributeType.
     * @param invalid the attribute that does not validate against expected.
     *
     * @return an error message reporting the problem.
     */
    static String errorMessage(AttributeType expected, Object invalid) {
        String message = "expected " + expected.getType().getName();
        message += (" , but got "
        + ((invalid == null) ? "null" : invalid.getClass().getName()));

        return message;
    }
}
