/*
 * Displacement.java
 *
 * Created on 03 July 2002, 12:27
 */

package org.geotools.styling;
import org.geotools.filter.Expression;
/**
 * A Displacement gives X and Y offset displacements to use for rendering a text label
 * near a point.
 *
 * $Id: Displacement.java,v 1.1 2002/07/03 13:35:21 ianturton Exp $
 * @author  iant
 */
public interface Displacement {
    public Expression getDisplacementX();
    public Expression getDisplacementY();
}
