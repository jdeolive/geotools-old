/*
 * LinePlacement.java
 *
 * Created on 03 July 2002, 12:34
 */

package org.geotools.styling;

import org.geotools.filter.Expression;
/**
 *
 * @author  iant
 */
public interface LinePlacement extends LabelPlacement {
    public Expression getPerpendicularOffset();
}
