/*
 * AnchorPoint.java
 *
 * Created on 03 July 2002, 12:23
 */

package org.geotools.styling;

import org.geotools.filter.Expression;
/**
 * An AnchorPoint identifies the location inside a textlabel to use as an
 * "anchor" for positioning it relative to a point geometry.
 *
 * $Id: AnchorPoint.java,v 1.1 2002/07/03 13:35:21 ianturton Exp $
 * @author  iant
 */
public interface AnchorPoint {
    public Expression getAnchorPointX();
    public Expression getAnchorPointY();
}
