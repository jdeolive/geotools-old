/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geotools.validation.xml;

/**
 * An exception used to collect and generalize errors in the validation system.
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: ValidationException.java,v 1.1 2004/02/13 03:07:59 jive Exp $
 */
public class ValidationException extends Exception {
    public ValidationException() {
        super();
    }

    public ValidationException(String s) {
        super(s);
    }

    public ValidationException(Throwable e) {
        super(e);
    }

    public ValidationException(String s, Throwable e) {
        super(s, e);
    }
}
