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
 * @version $Id: IllegalAttributeException.java,v 1.3 2003/07/20 15:58:23 aaime Exp $
 */
public class IllegalAttributeException extends Exception {
  
  // I don't believe this is very useful! -IanS
  //    /**
  //     * Constructor with no argument.
  //     */
  //    public IllegalAttributeException() {
  //        super();
  //    }
  
  public final AttributeType expected;
  public final Object invalid;
  
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
  
  public IllegalAttributeException(AttributeType expected,Object invalid) {
    this(expected,invalid,null);
  }
  
  public IllegalAttributeException(AttributeType expected,Object invalid,Throwable cause) {
    super(errorMessage(expected,invalid),cause);
    this.expected = expected;
    this.invalid = invalid;
  }
  
  static String errorMessage(AttributeType expected,Object invalid) {
    String message = "expected " + expected.getType().getName();
    message += " , but got " + ((invalid == null) ? "null" : invalid.getClass().getName());
    return message;
  }
}
