/*
 * LabelPlacement.java
 *
 * Created on 03 July 2002, 12:32
 */

package org.geotools.styling;

/**
 *
 * @author  iant
 */
public interface LabelPlacement {
    void accept(StyleVisitor visitor);
}
