/*
 * DefaultFeature.java
 *
 * Created on March 15, 2002, 3:46 PM
 */

package org.geotools.feature;

/**
 * Indicates client class has attempted to create an invalid schema.
 *
 * @author Rob Hranac, Vision for New York
 */
public class SchemaException extends Exception {


    /**
     * Constructor with no argument.
     */
    public SchemaException () {
        super();
    }

    /**
     * Constructor with message argument.
     * @param message Reason for the exception being thrown
     */
    public SchemaException (String message) {
        super(message);
    }
    
}
