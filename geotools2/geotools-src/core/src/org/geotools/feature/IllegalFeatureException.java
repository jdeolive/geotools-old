/*
 * DefaultFeature.java
 *
 * Created on March 15, 2002, 3:46 PM
 */
package org.geotools.feature;

/**
 * Indicates client class has attempted to create an invalid feature.
 * 
 * @author Rob Hranac, Vision for New York
 */
public class IllegalFeatureException extends Exception {


    /**
     * Constructor with no argument.
     */
    public IllegalFeatureException () {
        super();
    }

    /**
     * Constructor with message argument.
     */
    public IllegalFeatureException (String message) {
        super(message);
    }
    
}
