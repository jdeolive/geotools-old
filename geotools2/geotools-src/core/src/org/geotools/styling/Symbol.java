/*
 * Symbol.java
 *
 * Created on 01 August 2002, 10:46
 */

package org.geotools.styling;

/**
 * This an empty interface for styling symbol objects to implement
 * @author  iant
 */
public interface Symbol {
    void accept(StyleVisitor visitor);
}
