/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.filter;

import org.geotools.data.*;
import org.geotools.feature.Feature;

/**
 * Defines a complex filter (could also be called logical filter).
 *
 * This filter holds one or more filters together and relates
 * them logically in an internally defined.
 *
 * @author Rob Hranac, Vision for New York
 * @version alpha, 12/01/01
 */
public interface Expression {


    /**
     * Gets the type of this expression.
     */
    public short getType();


    /**
     * Returns a value for this expression.
     *
     * @param feature Specified feature to use when returning value.
     */
    public Object getValue(Feature feature)        
        throws MalformedFilterException;
    
}
